import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  TemplateRef,
  computed,
  inject,
  signal,
  viewChild,
  effect
} from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { Comptoir, Magasin } from '../../core/api.models';
import { ReferentielService } from '../../core/referentiel.service';
import {
  FicheStock,
  STATUT_BEBC_COLORS,
  STATUT_BEBC_LABELS,
  StockDisponible,
  Transfert
} from './stock.models';
import { StockService } from './stock.service';
import { BebcDetailDialogComponent } from './bebc-detail-dialog.component';
import { BebcPreviewDialogComponent } from './bebc-preview-dialog.component';
import { EmballagesStockComponent } from './emballages-stock.component';

class PaginatorFr extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Éléments par page';
  override nextPageLabel = 'Page suivante';
  override previousPageLabel = 'Page précédente';
  override firstPageLabel = 'Première page';
  override lastPageLabel = 'Dernière page';

  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0 || pageSize === 0) {
      return `0 sur ${length}`;
    }
    const debut = page * pageSize;
    const fin = Math.min(debut + pageSize, length);
    return `${debut + 1} – ${fin} sur ${length}`;
  };
}

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatDialogModule,
    EmballagesStockComponent
  ],
  templateUrl: './stock.component.html',
  styleUrl: './stock.component.scss',
  providers: [{ provide: MatPaginatorIntl, useClass: PaginatorFr }]
})
export class StockComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly stockService = inject(StockService);
  private readonly referentielService = inject(ReferentielService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  readonly auth = inject(AuthService);

  readonly paginator = viewChild(MatPaginator);
  readonly sort = viewChild(MatSort);
  readonly formulaireBebc = viewChild.required<TemplateRef<unknown>>('formulaireBebc');
  readonly formulaireReception = viewChild.required<TemplateRef<unknown>>('formulaireReception');
  private bebcDialogRef: MatDialogRef<unknown> | null = null;
  private receptionDialogRef: MatDialogRef<unknown> | null = null;

  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly comptoirs = signal<Comptoir[]>([]);
  readonly magasins = signal<Magasin[]>([]);
  readonly comptoirSelectionne = signal<number | null>(null);
  readonly comptoirCourant = computed(
    () => this.comptoirs().find((comptoir) => comptoir.id === this.comptoirSelectionne()) ?? null
  );
  readonly stock = signal<StockDisponible | null>(null);
  readonly fiches = signal<FicheStock[]>([]);
  readonly transfertsDataSource = new MatTableDataSource<Transfert>([]);
  readonly colonnesTransfert = [
    'dateEnlevement',
    'comptoirNom',
    'magasinDestinationNom',
    'tonnageEnleveKg',
    'nomTransporteur',
    'reception',
    'statut',
    'actions'
  ];
  readonly transfertEnReception = signal<Transfert | null>(null);
  readonly receptionLignes = signal<{ speculationId: number; speculationNom: string; nombreSacsRecus: number; colisageKg: number }[]>([]);
  readonly receptionMagasin = signal<number | null>(null);

  readonly receptionSacsTotaux = computed(() =>
    this.receptionLignes().reduce((acc, l) => acc + (Number(l.nombreSacsRecus) || 0), 0)
  );

  readonly receptionTonnageTotal = computed(() =>
    this.receptionLignes().reduce((acc, l) => acc + (Number(l.nombreSacsRecus) || 0) * (Number(l.colisageKg) || 0), 0)
  );
  readonly enregistrementFsj = signal(false);
  readonly enregistrementTransfert = signal(false);
  readonly receptionEnCours = signal(false);

  readonly peutTenirFsj = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')
  );
  readonly peutCreerTransfert = computed(() =>
    this.auth.possedeUnRole(
      'ADMIN',
      'DCT',
      'DGSA',
      'SGS',
      'DPL',
      'CHEF_COMPTOIR',
      'AGENT_COLLECTEUR'
    )
  );
  readonly peutReceptionner = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'MAGASINIER', 'SGS')
  );
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
  readonly sansComptoir = computed(() => {
    const affecte = this.comptoirAffecte();
    if (!this.profilTerrain()) {
      return false;
    }
    return !affecte?.comptoirId
      || !this.comptoirs().some((comptoir) => comptoir.id === affecte.comptoirId);
  });
  readonly speculationsDisponibles = computed(() =>
    (this.stock()?.speculations ?? []).filter(
      (speculation) => speculation.quantiteDisponibleKg > 0
    )
  );

  readonly natureOperations = [
    'RECEPTION_COLLECTE',
    'TRANSFERT_VERS_MAGASIN',
    'AJUSTEMENT_INVENTAIRE',
    'PERTE'
  ];

  readonly fsjForm = this.fb.nonNullable.group({
    comptoirId: [0, [Validators.required, Validators.min(1)]],
    dateFiche: [new Date().toISOString().slice(0, 10), Validators.required],
    natureOperation: ['RECEPTION_COLLECTE', Validators.required],
    entreeSacs: [0, [Validators.min(0)]],
    entreePoidsKg: [0, [Validators.min(0)]],
    entreeValeurCfa: [0, [Validators.min(0)]],
    sortieSacs: [0, [Validators.min(0)]],
    sortiePoidsKg: [0, [Validators.min(0)]],
    sortieValeurCfa: [0, [Validators.min(0)]]
  });

  readonly transfertForm = this.fb.nonNullable.group({
    comptoirId: [0, [Validators.required, Validators.min(1)]],
    magasinDestinationId: [0, [Validators.required, Validators.min(1)]],
    nomTransporteur: ['', Validators.required],
    numeroVehicule: ['', Validators.required],
    dateArriveePrevue: [''],
    direction: ['SGS'],
    localite: [''],
    beneficiaire: [''],
    objetEnlevement: ['Transfert de stock vers magasin central'],
    lignes: this.fb.array([this.creerLigneTransfert()])
  });

  constructor() {
    effect(() => {
      const p = this.paginator();
      if (p) {
        this.transfertsDataSource.paginator = p;
      }
      const s = this.sort();
      if (s) {
        this.transfertsDataSource.sort = s;
      }
    });
  }

  get lignesTransfert(): FormArray {
    return this.transfertForm.controls.lignes;
  }

  ngOnInit(): void {
    this.chargerReferentiels();
    this.charger();
  }

  chargerReferentiels(): void {
    forkJoin({
      comptoirs: this.referentielService.comptoirs(),
      magasins: this.referentielService.magasins()
    }).subscribe({
      next: (donnees) => {
        const comptoirsActifs = donnees.comptoirs.filter((comptoir) => comptoir.actif);
        this.comptoirs.set(comptoirsActifs);
        this.magasins.set(donnees.magasins.filter((magasin) => magasin.actif));
        const affectation = this.comptoirAffecte();
        const comptoir = affectation
          ? comptoirsActifs.find((element) => element.id === affectation.comptoirId)
          : comptoirsActifs[0];
        const magasin = donnees.magasins.find((element) => element.actif);
        this.comptoirSelectionne.set(comptoir?.id ?? null);
        this.fsjForm.patchValue({ comptoirId: comptoir?.id ?? 0 });
        this.transfertForm.patchValue({
          comptoirId: comptoir?.id ?? 0,
          magasinDestinationId: magasin?.id ?? 0
        });
        if (affectation && comptoir) {
          this.transfertForm.controls.comptoirId.disable({ emitEvent: false });
          this.fsjForm.controls.comptoirId.disable({ emitEvent: false });
        }
        this.chargerDonneesComptoir();
      },
      error: () => this.erreur.set('Impossible de charger les comptoirs et magasins.')
    });
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    forkJoin({
      transferts: this.stockService.listerTransferts()
    }).subscribe({
      next: (donnees) => {
        this.transfertsDataSource.data = donnees.transferts;
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les mouvements de stock.');
        this.chargement.set(false);
      }
    });
  }

  chargerDonneesComptoir(): void {
    const comptoirId = this.comptoirSelectionne();
    if (!comptoirId) {
      return;
    }
    this.erreur.set(null);
    forkJoin({
      stock: this.stockService.stockDisponible(comptoirId),
      fiches: this.stockService.listerFsj(comptoirId)
    }).subscribe({
      next: (donnees) => {
        this.stock.set(donnees.stock);
        this.fiches.set(donnees.fiches);
        this.initialiserPremiereLigneTransfert();
      },
      error: () => this.erreur.set('Impossible de charger les données du comptoir.')
    });
  }

  changerComptoir(comptoirId: number): void {
    if (this.profilTerrain() && this.comptoirAffecte()?.comptoirId !== comptoirId) {
      return;
    }
    const precedent = this.comptoirSelectionne();
    this.comptoirSelectionne.set(comptoirId);
    this.fsjForm.patchValue({ comptoirId });
    this.transfertForm.patchValue({ comptoirId });
    if (precedent !== null && precedent !== comptoirId) {
      this.reinitialiserLignesTransfert();
    }
    this.chargerDonneesComptoir();
  }

  ajouterLigneTransfert(): void {
    const disponibles = this.speculationsDisponibles();
    const utilisees = new Set(
      this.lignesTransfert.controls
        .map((controle) => Number(controle.get('speculationId')?.value))
        .filter((id) => id > 0)
    );
    const prochaine = disponibles.find(
      (speculation) => !utilisees.has(speculation.speculationId)
    );
    if (!prochaine) {
      this.notifier('Toutes les spéculations disponibles sont déjà présentes sur le BEBC.');
      return;
    }
    const ligne = this.creerLigneTransfert();
    ligne.patchValue({ speculationId: prochaine.speculationId });
    this.lignesTransfert.push(ligne);
  }

  supprimerLigneTransfert(index: number): void {
    if (this.lignesTransfert.length <= 1) {
      return;
    }
    this.lignesTransfert.removeAt(index);
  }

  quantiteLigne(index: number): number {
    const ligne = this.lignesTransfert.at(index);
    const sacs = Number(ligne.get('nombreSacs')?.value) || 0;
    const colisage = Number(ligne.get('colisageKg')?.value) || 0;
    return Math.round(sacs * colisage * 100) / 100;
  }

  disponibleLigne(index: number): number {
    const speculationId = Number(
      this.lignesTransfert.at(index).get('speculationId')?.value
    );
    return (
      this.stock()?.speculations.find(
        (speculation) => speculation.speculationId === speculationId
      )?.quantiteDisponibleKg ?? 0
    );
  }

  depassementLigne(index: number): boolean {
    return this.quantiteLigne(index) > this.disponibleLigne(index) + 0.01;
  }

  speculationDejaChoisie(speculationId: number, index: number): boolean {
    return this.lignesTransfert.controls.some(
      (controle, position) =>
        position !== index
        && Number(controle.get('speculationId')?.value) === speculationId
    );
  }

  totalSacsTransfert(): number {
    return this.lignesTransfert.controls.reduce(
      (total, controle) => total + (Number(controle.get('nombreSacs')?.value) || 0),
      0
    );
  }

  totalTonnageTransfert(): number {
    return this.lignesTransfert.controls.reduce(
      (total, _controle, index) => total + this.quantiteLigne(index),
      0
    );
  }

  resumeSpeculations(transfert: Transfert): string {
    return transfert.lignes
      .map((ligne) => `${ligne.speculationNom} ${this.formaterKg(ligne.quantiteKg)}`)
      .join(' · ');
  }

  formaterKg(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    return `${new Intl.NumberFormat('fr-FR', {
      maximumFractionDigits: 2
    }).format(valeur)} kg`;
  }

  private creerLigneTransfert(): FormGroup {
    return this.fb.nonNullable.group({
      speculationId: [0, [Validators.required, Validators.min(1)]],
      nombreSacs: [1, [Validators.required, Validators.min(1)]],
      colisageKg: [50, [Validators.required, Validators.min(0.01)]]
    });
  }

  private initialiserPremiereLigneTransfert(): void {
    const premiere = this.lignesTransfert.at(0);
    if (!premiere) {
      return;
    }
    const disponibles = this.speculationsDisponibles();
    const speculationId = Number(premiere.get('speculationId')?.value);
    const selectionValide = disponibles.some(
      (speculation) => speculation.speculationId === speculationId
    );
    if (!selectionValide && disponibles.length > 0) {
      premiere.patchValue({ speculationId: disponibles[0].speculationId });
    }
  }

  private reinitialiserLignesTransfert(): void {
    this.lignesTransfert.clear();
    const ligne = this.creerLigneTransfert();
    const premiere = this.speculationsDisponibles()[0];
    if (premiere) {
      ligne.patchValue({ speculationId: premiere.speculationId });
    }
    this.lignesTransfert.push(ligne);
  }

  enregistrerFsj(): void {
    this.fsjForm.markAllAsTouched();
    if (this.fsjForm.invalid) {
      this.notifier('Veuillez compléter la fiche de stock.');
      return;
    }
    const valeurs = this.fsjForm.getRawValue();
    const mouvementEntree =
      valeurs.entreeSacs > 0 || valeurs.entreePoidsKg > 0 || valeurs.entreeValeurCfa > 0;
    const mouvementSortie =
      valeurs.sortieSacs > 0 || valeurs.sortiePoidsKg > 0 || valeurs.sortieValeurCfa > 0;
    if (!mouvementEntree && !mouvementSortie) {
      this.notifier("Saisissez au moins un mouvement d'entrée ou de sortie.");
      return;
    }
    this.enregistrementFsj.set(true);
    this.stockService.enregistrerFsj(valeurs).subscribe({
      next: (fiche) => {
        this.enregistrementFsj.set(false);
        this.notifier(`Fiche ${fiche.dateFiche} enregistrée (stock ${fiche.stockFinalPoidsKg} kg).`);
        this.fsjForm.patchValue({
          entreeSacs: 0,
          entreePoidsKg: 0,
          entreeValeurCfa: 0,
          sortieSacs: 0,
          sortiePoidsKg: 0,
          sortieValeurCfa: 0
        });
        this.changerComptoir(fiche.comptoirId);
      },
      error: (erreur) => {
        this.enregistrementFsj.set(false);
        this.notifier(this.messageErreur(erreur));
      }
    });
  }

  creerTransfert(): void {
    if (this.sansComptoir()) {
      this.notifier('Aucun comptoir actif n’est affecté à votre compte.');
      return;
    }
    this.transfertForm.markAllAsTouched();
    if (this.transfertForm.invalid || this.lignesTransfert.length === 0) {
      this.notifier('Veuillez compléter le bordereau BEBC.');
      return;
    }
    const valeurs = this.transfertForm.getRawValue();
    const lignes = this.lignesTransfert.controls.map((controle) => ({
      speculationId: Number(controle.get('speculationId')?.value),
      nombreSacs: Number(controle.get('nombreSacs')?.value),
      colisageKg: Number(controle.get('colisageKg')?.value)
    }));
    const speculationsVues = new Set<number>();
    for (let index = 0; index < lignes.length; index++) {
      const ligne = lignes[index];
      if (speculationsVues.has(ligne.speculationId)) {
        this.notifier('Chaque spéculation ne peut apparaître qu’une seule fois sur le BEBC.');
        return;
      }
      speculationsVues.add(ligne.speculationId);
      if (this.depassementLigne(index)) {
        this.notifier(
          `La quantité de la ligne ${index + 1} dépasse le stock disponible pour cette spéculation.`
        );
        return;
      }
    }
    this.enregistrementTransfert.set(true);
    this.stockService
      .creerTransfert({
        comptoirId: valeurs.comptoirId,
        magasinDestinationId: valeurs.magasinDestinationId,
        direction: valeurs.direction,
        localite: valeurs.localite,
        magasinDepart: null,
        beneficiaire: valeurs.beneficiaire,
        objetEnlevement: valeurs.objetEnlevement,
        lignes,
        nomTransporteur: valeurs.nomTransporteur,
        numeroVehicule: valeurs.numeroVehicule,
        dateArriveePrevue: valeurs.dateArriveePrevue || null
      })
      .subscribe({
        next: (transfert) => {
          this.enregistrementTransfert.set(false);
          this.notifier(`BEBC ${transfert.numeroBebc} créé (${transfert.tonnageEnleveKg} kg).`);
          this.reinitialiserLignesTransfert();
          this.fermerFormulaireBebc();
          this.charger();
          this.chargerDonneesComptoir();
        },
        error: (erreur) => {
          this.enregistrementTransfert.set(false);
          this.notifier(this.messageErreur(erreur));
        }
      });
  }

  ouvrirReception(transfert: Transfert): void {
    this.transfertEnReception.set(transfert);
    this.receptionMagasin.set(
      transfert.magasinDestinationId ?? this.magasins()[0]?.id ?? null
    );
    this.receptionLignes.set(
      transfert.lignes.map(l => ({
        speculationId: l.speculationId,
        speculationNom: l.speculationNom,
        nombreSacsRecus: l.nombreSacs,
        colisageKg: l.colisageKg
      }))
    );
    this.receptionDialogRef = this.dialog.open(this.formulaireReception(), {
      width: '1040px',
      maxWidth: '96vw',
      maxHeight: '92vh',
      autoFocus: false,
      panelClass: 'bebc-create-dialog'
    });
  }

  confirmerReception(): void {
    const transfert = this.transfertEnReception();
    const magasinId = this.receptionMagasin();
    if (!transfert || !magasinId) {
      this.notifier('Sélectionnez le magasin de réception.');
      return;
    }
    if (this.receptionLignes().length === 0) {
      this.notifier('Aucune ligne de réception à enregistrer.');
      return;
    }
    this.receptionEnCours.set(true);
    this.stockService
      .receptionner(transfert.id, {
        magasinCentralId: magasinId,
        lignes: this.receptionLignes().map((ligne) => ({
          speculationId: ligne.speculationId,
          nombreSacsRecus: Number(ligne.nombreSacsRecus) || 0
        }))
      })
      .subscribe({
        next: (resultat) => {
          this.receptionEnCours.set(false);
          this.fermerFormulaireReception();
          const ecart = resultat.reception?.ecartPoidsKg ?? 0;
          const ecartSacs = resultat.reception?.ecartSacs ?? 0;
          this.notifier(
            `Réception enregistrée (${resultat.reception?.statutConformite}). Écart : ${ecartSacs} sac(s), ${ecart} kg.`
          );
          this.charger();
          this.chargerDonneesComptoir();
        },
        error: (erreur) => {
          this.receptionEnCours.set(false);
          this.notifier(this.messageErreur(erreur));
        }
      });
  }

  modifierSacsRecus(index: number, valeur: number | string | null): void {
    const recus = Math.max(0, Math.trunc(Number(valeur) || 0));
    this.receptionLignes.update((lignes) =>
      lignes.map((ligne, position) =>
        position === index ? { ...ligne, nombreSacsRecus: recus } : ligne
      )
    );
  }

  annulerReception(): void {
    this.fermerFormulaireReception();
  }

  fermerFormulaireReception(): void {
    this.receptionDialogRef?.close();
    this.receptionDialogRef = null;
    this.transfertEnReception.set(null);
  }

  ouvrirFormulaireBebc(): void {
    this.bebcDialogRef = this.dialog.open(this.formulaireBebc(), {
      width: '1040px',
      maxWidth: '96vw',
      maxHeight: '92vh',
      autoFocus: false,
      panelClass: 'bebc-create-dialog'
    });
  }

  fermerFormulaireBebc(): void {
    this.bebcDialogRef?.close();
    this.bebcDialogRef = null;
  }

  voirDetails(transfert: Transfert): void {
    this.dialog.open(BebcDetailDialogComponent, {
      data: transfert,
      width: '860px',
      maxWidth: '95vw',
      maxHeight: '92vh',
      autoFocus: false
    });
  }

  imprimerBebc(transfert: Transfert): void {
    this.dialog.open(BebcPreviewDialogComponent, {
      data: transfert,
      width: '920px',
      maxWidth: '95vw',
      maxHeight: '92vh',
      autoFocus: false
    });
  }

  libelleStatut(statut: Transfert['statut']): string {
    return STATUT_BEBC_LABELS[statut] ?? statut;
  }

  couleurStatut(statut: Transfert['statut']): string {
    return STATUT_BEBC_COLORS[statut] ?? 'neutre';
  }

  formaterTonnage(kg: number | null | undefined): string {
    if (kg === null || kg === undefined) {
      return '—';
    }
    if (Math.abs(kg) < 1000) {
      return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(kg)} kg`;
    }
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(kg / 1000)} t`;
  }

  formaterEcartSacs(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    const signe = valeur > 0 ? '+' : '';
    return `${signe}${new Intl.NumberFormat('fr-FR').format(valeur)} sac(s)`;
  }

  formaterMontant(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} CFA`;
  }

  formaterDate(valeur: string | null): string {
    if (!valeur) {
      return '—';
    }
    return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short' }).format(new Date(valeur));
  }

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 5000 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }
}
