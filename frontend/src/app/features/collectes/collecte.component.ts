import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import {
  Campagne,
  Comptoir,
  Producteur,
  Speculation
} from '../../core/api.models';
import { ReferentielService } from '../../core/referentiel.service';
import { Localite, PrixGarantie } from '../referentiels/referentiel.models';
import { ReferentielMetierService } from '../referentiels/referentiel.service';
import {
  Collecte,
  CollecteStatut,
  STATUT_COLLECTE_COLORS,
  STATUT_COLLECTE_LABELS
} from './collecte.models';
import { CollecteService } from './collecte.service';
import {
  CollecteDetailDialogComponent,
  CollecteDialogResult
} from './collecte-detail-dialog.component';
import {
  CollecteFormDialogComponent,
  CollecteFormDialogResult
} from './collecte-form-dialog.component';

@Component({
  selector: 'app-collecte',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './collecte.component.html',
  styleUrl: './collecte.component.scss'
})
export class CollecteComponent implements OnInit {
  private readonly collecteService = inject(CollecteService);
  private readonly referentielService = inject(ReferentielService);
  private readonly referentielMetier = inject(ReferentielMetierService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  readonly auth = inject(AuthService);

  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly collectes = signal<Collecte[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly brbcVisualise = signal<Collecte | null>(null);
  readonly brbcLignes = signal<Collecte[]>([]);

  readonly statutFiltre = signal<CollecteStatut | ''>('');
  readonly campagnes = signal<Campagne[]>([]);
  readonly comptoirs = signal<Comptoir[]>([]);
  readonly speculations = signal<Speculation[]>([]);
  readonly producteurs = signal<Producteur[]>([]);
  readonly localites = signal<Localite[]>([]);
  readonly prixGarantis = signal<PrixGarantie[]>([]);

  readonly peutCreer = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly peutExaminer = computed(() => this.auth.possedeUnRole('ADMIN', 'SCP'));
  readonly peutValider = computed(() => this.auth.possedeUnRole('ADMIN', 'DCT'));
  readonly profilTerrain = computed(() =>
    this.auth.possedeUnRole('AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly comptoirAffecte = computed(() => {
    const utilisateur = this.auth.user();
    if (!utilisateur?.comptoirId || !this.profilTerrain()) {
      return null;
    }
    return utilisateur;
  });
  readonly sansComptoir = computed(() => this.profilTerrain() && !this.comptoirAffecte());
  readonly estExaminateur = (collecte: Collecte): boolean =>
    collecte.examinateurId !== null && this.auth.user()?.id === collecte.examinateurId;
  readonly peutValiderCollecte = (collecte: Collecte): boolean =>
    this.peutValider() && !this.estExaminateur(collecte);
  readonly statuts: CollecteStatut[] = [
    'SYNCHRONISEE',
    'EXAMINEE_SCP',
    'VALIDEE_DCT',
    'REJETEE'
  ];

  readonly brbcTotalPoidsNet = computed(() => {
    const bordereau = this.brbcVisualise()?.bordereau;
    if (bordereau) {
      return bordereau.quantiteKg;
    }
    return Math.round(
      this.brbcLignes().reduce((somme, c) => somme + (c.quantiteKg ?? 0), 0) * 100
    ) / 100;
  });

  readonly brbcTotalMontant = computed(() => {
    const bordereau = this.brbcVisualise()?.bordereau;
    if (bordereau) {
      return bordereau.montantNetPayerCfa;
    }
    return this.brbcLignes().reduce((somme, c) => somme + (c.montantNetCfa ?? 0), 0);
  });

  readonly brbcTotalSacs = computed(() =>
    this.brbcLignes().reduce(
      (somme, c) => somme + (c.fichePesee?.nombreSacs ?? 0),
      0
    )
  );

  readonly brbcTotalEnLettres = computed(() =>
    this.nombreEnLettres(this.brbcTotalMontant())
  );

  brbcToutesConformes(): boolean {
    return this.brbcLignes().every((l) => l.controleQualite?.conforme !== false);
  }

  ngOnInit(): void {
    this.chargerReferentiels();
    this.charger();
  }

  chargerReferentiels(): void {
    forkJoin({
      campagnes: this.referentielService.campagnes(),
      comptoirs: this.referentielService.comptoirs(),
      speculations: this.referentielService.speculations(),
      producteurs: this.referentielService.producteurs(),
      localites: this.referentielMetier.localites()
    }).subscribe({
      next: (donnees) => {
        this.campagnes.set(donnees.campagnes);
        this.comptoirs.set(donnees.comptoirs);
        this.speculations.set(donnees.speculations);
        this.producteurs.set(donnees.producteurs);
        this.localites.set(
          [...donnees.localites].sort((a, b) => a.name.localeCompare(b.name, 'fr'))
        );
        const campagne =
          donnees.campagnes.find((element) => element.statut === 'OUVERTE') ?? donnees.campagnes[0];
        if (campagne?.id) {
          this.chargerPrix(campagne.id);
        }
      },
      error: () =>
        this.erreur.set('Impossible de charger les référentiels de collecte.')
    });
  }

  private chargerPrix(campagneId: number): void {
    if (!campagneId) {
      this.prixGarantis.set([]);
      return;
    }
    this.referentielMetier.prixGarantis(campagneId).subscribe({
      next: (prix) => this.prixGarantis.set(prix),
      error: () => this.prixGarantis.set([])
    });
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.collecteService
      .lister({
        statut: this.statutFiltre(),
        page: this.page(),
        size: 20
      })
      .subscribe({
        next: (reponse) => {
          this.collectes.set(reponse.content);
          this.totalPages.set(reponse.totalPages);
          this.totalElements.set(reponse.totalElements);
          this.chargement.set(false);
        },
        error: () => {
          this.erreur.set('Impossible de charger les collectes.');
          this.chargement.set(false);
        }
      });
  }

  changerStatut(statut: CollecteStatut | ''): void {
    this.statutFiltre.set(statut);
    this.page.set(0);
    this.charger();
  }

  pagePrecedente(): void {
    if (this.page() > 0) {
      this.page.update((valeur) => valeur - 1);
      this.charger();
    }
  }

  pageSuivante(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((valeur) => valeur + 1);
      this.charger();
    }
  }

  /**
   * Ouvre la boîte de dialogue de création d'une nouvelle collecte (BRBC)
   */
  ouvrirNouvelleCollecte(): void {
    const dialogRef = this.dialog.open<
      CollecteFormDialogComponent,
      unknown,
      CollecteFormDialogResult
    >(CollecteFormDialogComponent, {
      data: {
        campagnes: this.campagnes(),
        comptoirs: this.comptoirs(),
        speculations: this.speculations(),
        producteurs: this.producteurs(),
        localites: this.localites(),
        prixGarantis: this.prixGarantis()
      },
      width: '980px',
      maxWidth: '96vw',
      maxHeight: '94vh',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((res) => {
      if (res?.producteurs) {
        this.producteurs.set(res.producteurs);
      }
      if (res?.success && res.resultat) {
        this.notifier(
          `BRBC ${res.resultat.bordereau.numeroBrbc} émis avec ${res.resultat.collectes.length} lot(s) — ${this.formaterMontant(res.resultat.bordereau.montantNetPayerCfa)}.`
        );
        this.page.set(0);
        this.charger();
      }
    });
  }

  /**
   * Ouvre la boîte de dialogue de détail complet d'une collecte
   */
  ouvrirDetail(collecte: Collecte): void {
    const dialogRef = this.dialog.open<
      CollecteDetailDialogComponent,
      unknown,
      CollecteDialogResult
    >(CollecteDetailDialogComponent, {
      data: {
        collecte,
        peutExaminer: this.peutExaminer(),
        peutValiderCollecte: this.peutValiderCollecte(collecte),
        estExaminateur: this.estExaminateur(collecte)
      },
      width: '780px',
      maxWidth: '96vw',
      maxHeight: '90vh',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((res) => {
      if (!res) return;
      if (res.action === 'brbc') {
        this.ouvrirBrbc(res.collecte);
      } else if (res.action === 'examiner') {
        this.examiner(res.collecte, res.approuve ?? true);
      } else if (res.action === 'valider') {
        this.valider(res.collecte, res.approuve ?? true);
      }
    });
  }

  examiner(collecte: Collecte, approuve: boolean): void {
    let motif: string | undefined;
    if (!approuve) {
      const saisie = window.prompt('Motif du rejet du dossier :');
      if (saisie === null) {
        return;
      }
      if (!saisie.trim()) {
        this.notifier('Le motif de rejet est obligatoire.');
        return;
      }
      motif = saisie.trim();
    }
    this.collecteService.examiner(collecte.id, approuve, motif).subscribe({
      next: () => {
        this.notifier(approuve ? 'Dossier transmis à la DCT.' : 'Dossier rejeté.');
        this.charger();
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  valider(collecte: Collecte, approuve: boolean): void {
    let motif: string | undefined;
    if (!approuve) {
      const saisie = window.prompt('Motif du rejet du BRBC :');
      if (saisie === null) {
        return;
      }
      if (!saisie.trim()) {
        this.notifier('Le motif de rejet est obligatoire.');
        return;
      }
      motif = saisie.trim();
    }
    this.collecteService.valider(collecte.id, approuve, motif).subscribe({
      next: () => {
        this.notifier(approuve ? 'BRBC validé et transmis à la DFC.' : 'Dossier rejeté.');
        this.charger();
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  libelleStatut(statut: CollecteStatut): string {
    return STATUT_COLLECTE_LABELS[statut] ?? statut;
  }

  couleurStatut(statut: CollecteStatut): string {
    return STATUT_COLLECTE_COLORS[statut] ?? 'neutre';
  }

  formaterDate(valeur: string | null): string {
    if (!valeur) {
      return '—';
    }
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(valeur));
  }

  formaterMontant(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} CFA`;
  }

  ouvrirBrbc(collecte: Collecte): void {
    this.brbcVisualise.set(collecte);
    this.brbcLignes.set([collecte]);
    const bordereauId = collecte.bordereau?.id;
    if (!bordereauId) {
      this.defilerVersBrbc();
      return;
    }
    this.collecteService.listerParBrbc(bordereauId).subscribe({
      next: (lignes) => {
        this.brbcLignes.set(lignes.length > 0 ? lignes : [collecte]);
        this.defilerVersBrbc();
      },
      error: () => this.defilerVersBrbc()
    });
  }

  private defilerVersBrbc(): void {
    setTimeout(() => {
      document
        .querySelector('.brbc-document-overlay')
        ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 80);
  }

  fermerBrbc(): void {
    this.brbcVisualise.set(null);
    this.brbcLignes.set([]);
  }

  imprimerBrbc(): void {
    window.print();
  }

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4500 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }

  /** Convertit un montant entier en toutes lettres (français, FCFA) */
  private nombreEnLettres(montant: number): string {
    if (!montant || montant <= 0) return 'Zéro franc CFA';
    const unites = ['', 'un', 'deux', 'trois', 'quatre', 'cinq', 'six', 'sept', 'huit', 'neuf',
      'dix', 'onze', 'douze', 'treize', 'quatorze', 'quinze', 'seize', 'dix-sept', 'dix-huit', 'dix-neuf'];
    const dizaines = ['', '', 'vingt', 'trente', 'quarante', 'cinquante', 'soixante', 'soixante', 'quatre-vingt', 'quatre-vingt'];

    const centaine = (n: number): string => {
      if (n === 0) return '';
      if (n < 20) return unites[n];
      const d = Math.floor(n / 10);
      const u = n % 10;
      if (d === 7) return 'soixante-' + (u === 1 ? 'et-onze' : unites[10 + u]);
      if (d === 9) return 'quatre-vingt-' + (u > 0 ? unites[u] : '');
      return dizaines[d] + (u === 1 && d < 8 ? '-et-' : u > 0 ? '-' : '') + (u > 0 ? unites[u] : '');
    };

    const tranche = (n: number): string => {
      if (n === 0) return '';
      const c = Math.floor(n / 100);
      const r = n % 100;
      let result = '';
      if (c === 1) result += 'cent ';
      else if (c > 1) result += unites[c] + ' cents ';
      result += centaine(r);
      return result.trim();
    };

    const n = Math.floor(montant);
    if (n >= 1_000_000_000) {
      const m = Math.floor(n / 1_000_000_000);
      const r = n % 1_000_000_000;
      return (tranche(m) + (m === 1 ? ' milliard' : ' milliards') + (r > 0 ? ' ' + this.nombreEnLettres(r).replace(' franc CFA', '') : '') + ' francs CFA').trim();
    }
    if (n >= 1_000_000) {
      const m = Math.floor(n / 1_000_000);
      const r = n % 1_000_000;
      return (tranche(m) + (m === 1 ? ' million' : ' millions') + (r > 0 ? ' ' + this.nombreEnLettres(r).replace(' franc CFA', '') : '') + ' francs CFA').trim();
    }
    if (n >= 1_000) {
      const m = Math.floor(n / 1_000);
      const r = n % 1_000;
      const millePart = m === 1 ? 'mille' : tranche(m) + ' mille';
      return (millePart + (r > 0 ? ' ' + tranche(r) : '') + ' francs CFA').trim();
    }
    return (tranche(n) + ' francs CFA').trim();
  }
}
