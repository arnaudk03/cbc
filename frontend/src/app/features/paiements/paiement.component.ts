import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { Collecte } from '../collectes/collecte.models';
import { CollecteService } from '../collectes/collecte.service';
import { PaiementDetailDialogComponent } from './paiement-detail-dialog.component';
import {
  Paiement,
  PaiementStatut,
  STATUT_PAIEMENT_COLORS,
  STATUT_PAIEMENT_LABELS
} from './paiement.models';
import { PaiementService } from './paiement.service';

interface CandidatPaiement {
  collecte: Collecte;
  montant: number;
  numeroBrbc: string;
  nombreLots: number;
}

@Component({
  selector: 'app-paiement',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './paiement.component.html',
  styleUrl: './paiement.component.scss'
})
export class PaiementComponent implements OnInit {
  private readonly paiementService = inject(PaiementService);
  private readonly collecteService = inject(CollecteService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  readonly auth = inject(AuthService);

  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly paiements = signal<Paiement[]>([]);
  readonly collectesValidees = signal<Collecte[]>([]);
  readonly initEnCours = signal<number | null>(null);
  readonly statutFiltre = signal<PaiementStatut | ''>('');
  readonly paiement2fa = signal<Paiement | null>(null);
  readonly codeOtp = signal('');
  readonly codeDemo = signal<string | null>(null);
  readonly validationEnCours = signal(false);

  readonly peutInitier = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'SFB_INITIATEUR')
  );
  readonly peutValider = computed(() => this.auth.possedeUnRole('ADMIN', 'SFB_CHEF'));
  readonly peutRejeter = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'SFB_CHEF', 'DFC_DIRECTEUR')
  );

  readonly candidats = computed<CandidatPaiement[]>(() => {
    const bordereauxPayes = new Set(this.paiements().map((paiement) => paiement.bordereauId));
    const bordereauxVus = new Set<number>();
    return this.collectesValidees()
      .filter((collecte) => collecte.bordereau !== null)
      .filter((collecte) => {
        const bordereau = collecte.bordereau!;
        if (bordereauxPayes.has(bordereau.id) || bordereauxVus.has(bordereau.id)) {
          return false;
        }
        bordereauxVus.add(bordereau.id);
        return true;
      })
      .map((collecte) => ({
        collecte,
        montant: collecte.bordereau!.montantNetPayerCfa,
        numeroBrbc: collecte.bordereau!.numeroBrbc,
        nombreLots: collecte.bordereau!.nombreLots
      }));
  });

  readonly statuts: PaiementStatut[] = [
    'INITIE',
    'VALIDE_2FA',
    'ENVOYE_OPERATEUR',
    'CONFIRME',
    'REJETE'
  ];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    forkJoin({
      paiements: this.paiementService.lister(this.statutFiltre()),
      collectes: this.collecteService.lister({ statut: 'VALIDEE_DCT', size: 100 })
    }).subscribe({
      next: (donnees) => {
        this.paiements.set(donnees.paiements);
        this.collectesValidees.set(donnees.collectes.content);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les paiements.');
        this.chargement.set(false);
      }
    });
  }

  changerStatut(statut: PaiementStatut | ''): void {
    this.statutFiltre.set(statut);
    this.paiementService.lister(statut).subscribe({
      next: (paiements) => this.paiements.set(paiements),
      error: () => this.erreur.set('Impossible de filtrer les paiements.')
    });
  }

  initier(candidat: CandidatPaiement): void {
    this.initEnCours.set(candidat.collecte.bordereau!.id);
    this.paiementService.initier(candidat.collecte.bordereau!.id).subscribe({
      next: (paiement) => {
        this.initEnCours.set(null);
        this.notifier(`Ordre ${paiement.referenceTransaction} initié.`);
        this.charger();
      },
      error: (erreur) => {
        this.initEnCours.set(null);
        this.notifier(this.messageErreur(erreur));
      }
    });
  }

  activer2fa(paiement: Paiement): void {
    this.paiementService.demanderOtp(paiement.id).subscribe({
      next: (challenge) => {
        this.paiement2fa.set(paiement);
        this.codeOtp.set('');
        this.codeDemo.set(challenge.codeDemo);
        this.notifier(challenge.message);
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  valider2fa(): void {
    const paiement = this.paiement2fa();
    const code = this.codeOtp().trim();
    if (!paiement || !/^\d{6}$/.test(code)) {
      this.notifier('Saisissez un code OTP à 6 chiffres.');
      return;
    }
    this.validationEnCours.set(true);
    this.paiementService.valider(paiement.id, code).subscribe({
      next: (resultat) => {
        this.validationEnCours.set(false);
        this.paiement2fa.set(null);
        this.codeDemo.set(null);
        this.notifier(`Paiement ${resultat.referenceTransaction} confirmé.`);
        this.charger();
      },
      error: (erreur) => {
        this.validationEnCours.set(false);
        this.notifier(this.messageErreur(erreur));
      }
    });
  }

  annuler2fa(): void {
    this.paiement2fa.set(null);
    this.codeOtp.set('');
    this.codeDemo.set(null);
  }

  rejeter(paiement: Paiement): void {
    const motif = window.prompt('Motif du rejet du paiement :');
    if (motif === null) {
      return;
    }
    if (!motif.trim()) {
      this.notifier('Le motif de rejet est obligatoire.');
      return;
    }
    this.paiementService.rejeter(paiement.id, motif.trim()).subscribe({
      next: () => {
        this.notifier('Paiement rejeté.');
        this.charger();
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  ouvrirDetail(paiement: Paiement): void {
    const ref = this.dialog.open(PaiementDetailDialogComponent, {
      data: {
        paiement,
        peutValider: this.peutValider(),
        peutRejeter: this.peutRejeter()
      },
      width: '680px',
      maxWidth: '95vw',
      autoFocus: false
    });

    ref.afterClosed().subscribe((res) => {
      if (!res) return;
      if (res.action === 'valider2fa' && res.paiement) {
        this.activer2fa(res.paiement);
      } else if (res.action === 'rejeter' && res.paiement) {
        this.rejeter(res.paiement);
      }
    });
  }

  ouvrirDetailCandidat(candidat: CandidatPaiement): void {
    const ref = this.dialog.open(PaiementDetailDialogComponent, {
      data: {
        candidat: {
          collecte: {
            id: candidat.collecte.id,
            referenceLot: candidat.collecte.referenceLot,
            producteurNom: candidat.collecte.producteurNom,
            producteurTelephone: candidat.collecte.telephoneProducteur,
            comptoirNom: candidat.collecte.comptoirNom,
            campagneCode: candidat.collecte.campagneCode,
            speculationNom: candidat.collecte.speculationNom,
            poidsNetKg: candidat.collecte.quantiteKg,
            prixUnitaireCfa: candidat.collecte.prixUnitaireKg,
            operateurMobile: candidat.collecte.operateurMobile
          },
          numeroBrbc: candidat.numeroBrbc,
          nombreLots: candidat.nombreLots,
          montant: candidat.montant
        }
      },
      width: '680px',
      maxWidth: '95vw',
      autoFocus: false
    });

    ref.afterClosed().subscribe((res) => {
      if (res?.action === 'initier') {
        this.initier(candidat);
      }
    });
  }

  libelleStatut(statut: PaiementStatut): string {
    return STATUT_PAIEMENT_LABELS[statut] ?? statut;
  }

  couleurStatut(statut: PaiementStatut): string {
    return STATUT_PAIEMENT_COLORS[statut] ?? 'neutre';
  }

  formaterMontant(valeur: number): string {
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} CFA`;
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

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 5000 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }
}
