import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import {
  Paiement,
  PaiementStatut,
  STATUT_PAIEMENT_COLORS,
  STATUT_PAIEMENT_LABELS
} from './paiement.models';

export interface CandidatDetailData {
  collecte: {
    id: number;
    referenceLot?: string;
    producteurNom?: string;
    producteurTelephone?: string;
    comptoirNom?: string;
    campagneCode?: string;
    speculationNom?: string;
    poidsNetKg?: number;
    prixUnitaireCfa?: number;
    operateurMobile?: string;
  };
  numeroBrbc: string;
  nombreLots: number;
  montant: number;
}

export interface PaiementDetailDialogData {
  paiement?: Paiement;
  candidat?: CandidatDetailData;
  peutValider?: boolean;
  peutRejeter?: boolean;
}

@Component({
  selector: 'app-paiement-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="dialog-header">
      <div class="header-icon" [ngClass]="estCandidat() ? 'candidat' : data.paiement?.statut?.toLowerCase() || ''">
        <mat-icon>{{ estCandidat() ? 'receipt_long' : 'payments' }}</mat-icon>
      </div>
      <div class="header-text">
        <span class="header-sub">
          {{ estCandidat() ? 'Bordereau BRBC en attente de paiement' : 'Détail de la transaction 2FA' }}
        </span>
        <h2 mat-dialog-title>
          {{ estCandidat() ? data.candidat?.numeroBrbc : data.paiement?.referenceTransaction }}
        </h2>
      </div>
      <button mat-icon-button class="btn-fermer" (click)="fermer()" aria-label="Fermer">
        <mat-icon>close</mat-icon>
      </button>
    </div>

    <mat-dialog-content class="dialog-content">
      <!-- Encart Montant & Statut -->
      <div class="montant-card">
        <div class="montant-valeur">
          <span class="label">Montant Net à Payer</span>
          <strong>{{ formaterMontant(montantTotal()) }}</strong>
        </div>
        <div class="statut-badge-wrap">
          <span class="label">Statut Actuel</span>
          @if (estCandidat()) {
            <span class="status-chip alerte">En attente d'initiation</span>
          } @else {
            <span class="status-chip" [ngClass]="couleurStatut(data.paiement!.statut)">
              {{ libelleStatut(data.paiement!.statut) }}
            </span>
          }
        </div>
      </div>

      <!-- Section Bénéficiaire & Opérateur -->
      <div class="detail-section">
        <div class="section-title">
          <mat-icon>person</mat-icon>
          <span>Bénéficiaire & Opérateur Mobile Money</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="item-label">Producteur</span>
            <strong class="item-value">{{ producteurNom() }}</strong>
          </div>
          <div class="detail-item">
            <span class="item-label">N° Téléphone Destinataire</span>
            <strong class="item-value telephone">
              <mat-icon>phone</mat-icon>
              {{ telephoneDestination() }}
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Opérateur Mobile Money</span>
            <span class="operateur-pill" [ngClass]="operateur().toLowerCase().includes('orange') ? 'orange' : 'moov'">
              {{ operateur() }}
            </span>
          </div>
          <div class="detail-item">
            <span class="item-label">Campagne Agricole</span>
            <strong class="item-value">{{ campagneCode() }}</strong>
          </div>
        </div>
      </div>

      <!-- Section BRBC & Lots -->
      <div class="detail-section">
        <div class="section-title">
          <mat-icon>agriculture</mat-icon>
          <span>Bordereau de Réception BRBC</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="item-label">N° Bordereau BRBC</span>
            <strong class="item-value brbc-ref">{{ numeroBrbc() }}</strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Nombre de lots pesés</span>
            <strong class="item-value">{{ nombreLots() }} lot{{ nombreLots() > 1 ? 's' : '' }}</strong>
          </div>
          @if (referenceLot()) {
            <div class="detail-item">
              <span class="item-label">Référence du Lot</span>
              <strong class="item-value">{{ referenceLot() }}</strong>
            </div>
          }
          @if (comptoirNom()) {
            <div class="detail-item">
              <span class="item-label">Comptoir de pesée</span>
              <strong class="item-value">{{ comptoirNom() }}</strong>
            </div>
          }
        </div>
      </div>

      <!-- Section Traçabilité & Audit Trail 2FA (pour les paiements initiés) -->
      @if (!estCandidat() && data.paiement; as p) {
        <div class="detail-section">
          <div class="section-title">
            <mat-icon>shield</mat-icon>
            <span>Traçabilité & Validation 2FA (SoD)</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="item-label">Initiateur (SFB)</span>
              <strong class="item-value">{{ p.initiateur ?? '—' }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">Date d'initiation</span>
              <strong class="item-value">{{ formaterDate(p.dateInitiation) }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">Validateur 2FA</span>
              <strong class="item-value">{{ p.validateur ?? 'En attente' }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">Date validation 2FA</span>
              <strong class="item-value">{{ formaterDate(p.dateValidation2FA) }}</strong>
            </div>
            @if (p.dateConfirmation) {
              <div class="detail-item">
                <span class="item-label">Date confirmation télécom</span>
                <strong class="item-value">{{ formaterDate(p.dateConfirmation) }}</strong>
              </div>
            }
          </div>
        </div>

        <!-- Section Rapprochement Passerelle Opérateur -->
        <div class="detail-section">
          <div class="section-title">
            <mat-icon>sync_alt</mat-icon>
            <span>Rapprochement Opérateur Télécom</span>
          </div>
          <div class="detail-grid">
            <div class="detail-item full-width">
              <span class="item-label">ID Transaction Opérateur</span>
              <strong class="item-value code-mono">{{ p.referenceOperateur ?? 'Non attribué (en attente)' }}</strong>
            </div>
            @if (p.messageRetourOperateur) {
              <div class="detail-item full-width">
                <span class="item-label">Réponse Passerelle</span>
                <span class="reponse-passerelle">{{ p.messageRetourOperateur }}</span>
              </div>
            }
            @if (p.motifRejet) {
              <div class="detail-item full-width motif-rejet-card">
                <mat-icon>error</mat-icon>
                <div>
                  <span class="item-label">Motif du Rejet</span>
                  <strong class="motif-texte">{{ p.motifRejet }}</strong>
                </div>
              </div>
            }
          </div>
        </div>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-stroked-button type="button" (click)="fermer()">
        Fermer
      </button>

      @if (!estCandidat() && data.paiement?.statut === 'INITIE' && data.peutRejeter) {
        <button mat-stroked-button color="warn" type="button" (click)="rejeterAction()">
          <mat-icon>cancel</mat-icon>
          Rejeter
        </button>
      }

      @if (!estCandidat() && data.paiement?.statut === 'INITIE' && data.peutValider) {
        <button mat-flat-button color="primary" type="button" (click)="valider2faAction()">
          <mat-icon>verified_user</mat-icon>
          Valider 2FA
        </button>
      }

      @if (estCandidat()) {
        <button mat-flat-button color="primary" type="button" (click)="initierAction()">
          <mat-icon>send</mat-icon>
          Initier le paiement
        </button>
      }
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-header {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1.25rem 1.5rem 0.75rem 1.5rem;
        border-bottom: 1px solid #e2e8f0;
        position: relative;
      }

      .header-icon {
        display: grid;
        place-items: center;
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: #f1f5f9;
        color: #475569;
        flex-shrink: 0;

        &.candidat {
          background: #eff6ff;
          color: #2563eb;
        }

        &.initie {
          background: #fef3c7;
          color: #d97706;
        }

        &.valide_2fa,
        &.envoye_operateur {
          background: #e0f2fe;
          color: #0284c7;
        }

        &.confirme {
          background: #dcfce7;
          color: #16a34a;
        }

        &.rejete {
          background: #fee2e2;
          color: #dc2626;
        }

        mat-icon {
          font-size: 28px;
          width: 28px;
          height: 28px;
        }
      }

      .header-text {
        flex: 1;

        .header-sub {
          display: block;
          font-size: 0.78rem;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          color: #64748b;
        }

        h2 {
          margin: 0.15rem 0 0 0;
          font-size: 1.25rem;
          font-weight: 700;
          color: #0f172a;
          line-height: 1.3;
        }
      }

      .btn-fermer {
        color: #94a3b8;
        &:hover {
          color: #334155;
        }
      }

      .dialog-content {
        padding: 1.25rem 1.5rem !important;
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
        max-height: 75vh;
      }

      .montant-card {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 1rem 1.25rem;
        background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        border: 1px solid #cbd5e1;
        border-radius: 10px;

        .montant-valeur {
          display: flex;
          flex-direction: column;

          .label {
            font-size: 0.75rem;
            font-weight: 600;
            color: #64748b;
            text-transform: uppercase;
          }

          strong {
            font-size: 1.6rem;
            font-weight: 800;
            color: #0b6623; // Vert institutionnel SONAGESS
          }
        }

        .statut-badge-wrap {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 0.35rem;

          .label {
            font-size: 0.75rem;
            font-weight: 600;
            color: #64748b;
            text-transform: uppercase;
          }
        }
      }

      .detail-section {
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 1rem;
        background: #ffffff;

        .section-title {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.85rem;
          font-weight: 700;
          color: #1e293b;
          text-transform: uppercase;
          letter-spacing: 0.03em;
          margin-bottom: 0.85rem;
          padding-bottom: 0.5rem;
          border-bottom: 1px solid #f1f5f9;

          mat-icon {
            font-size: 18px;
            width: 18px;
            height: 18px;
            color: #0284c7;
          }
        }
      }

      .detail-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 0.85rem 1.25rem;

        @media (max-width: 600px) {
          grid-template-columns: 1fr;
        }

        .detail-item {
          display: flex;
          flex-direction: column;
          gap: 0.2rem;

          &.full-width {
            grid-column: 1 / -1;
          }

          .item-label {
            font-size: 0.75rem;
            font-weight: 500;
            color: #64748b;
          }

          .item-value {
            font-size: 0.92rem;
            font-weight: 600;
            color: #0f172a;

            &.telephone {
              display: flex;
              align-items: center;
              gap: 0.35rem;
              color: #0284c7;

              mat-icon {
                font-size: 16px;
                width: 16px;
                height: 16px;
              }
            }

            &.brbc-ref {
              font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
              color: #047857;
            }

            &.code-mono {
              font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
              font-size: 0.85rem;
              background: #f8fafc;
              padding: 0.35rem 0.5rem;
              border-radius: 4px;
              border: 1px solid #e2e8f0;
            }
          }
        }
      }

      .operateur-pill {
        display: inline-flex;
        align-items: center;
        padding: 0.2rem 0.65rem;
        border-radius: 9999px;
        font-size: 0.78rem;
        font-weight: 700;
        width: fit-content;

        &.orange {
          background: #ffedd5;
          color: #ea580c;
        }

        &.moov {
          background: #e0f2fe;
          color: #0284c7;
        }
      }

      .status-chip {
        display: inline-flex;
        align-items: center;
        padding: 0.25rem 0.65rem;
        border-radius: 9999px;
        font-size: 0.8rem;
        font-weight: 700;

        &.succes {
          background: #dcfce7;
          color: #166534;
        }
        &.alerte {
          background: #fef3c7;
          color: #92400e;
        }
        &.info {
          background: #e0f2fe;
          color: #075985;
        }
        &.violet {
          background: #f3e8ff;
          color: #6b21a8;
        }
        &.danger {
          background: #fee2e2;
          color: #991b1b;
        }
      }

      .motif-rejet-card {
        display: flex;
        align-items: flex-start;
        gap: 0.75rem;
        padding: 0.75rem;
        background: #fef2f2;
        border: 1px solid #fecaca;
        border-radius: 8px;
        color: #991b1b;

        mat-icon {
          color: #dc2626;
          font-size: 20px;
          width: 20px;
          height: 20px;
          margin-top: 2px;
        }

        .motif-texte {
          color: #b91c1c;
          font-size: 0.88rem;
          display: block;
          margin-top: 0.2rem;
        }
      }

      .reponse-passerelle {
        font-size: 0.82rem;
        color: #475569;
        font-style: italic;
      }

      .dialog-actions {
        padding: 0.85rem 1.5rem !important;
        border-top: 1px solid #e2e8f0;
        gap: 0.5rem;
      }
    `
  ]
})
export class PaiementDetailDialogComponent {
  readonly data = inject<PaiementDetailDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<PaiementDetailDialogComponent>);

  estCandidat(): boolean {
    return !!this.data.candidat;
  }

  montantTotal(): number {
    if (this.estCandidat()) {
      return this.data.candidat?.montant ?? 0;
    }
    return this.data.paiement?.montantPayeCfa ?? 0;
  }

  producteurNom(): string {
    if (this.estCandidat()) {
      return this.data.candidat?.collecte.producteurNom ?? '—';
    }
    return this.data.paiement?.producteurNom ?? '—';
  }

  telephoneDestination(): string {
    if (this.estCandidat()) {
      return this.data.candidat?.collecte.producteurTelephone ?? '—';
    }
    return this.data.paiement?.telephoneDestination ?? '—';
  }

  operateur(): string {
    if (this.estCandidat()) {
      return this.data.candidat?.collecte.operateurMobile ?? 'Mobile Money';
    }
    return this.data.paiement?.operateur ?? 'Mobile Money';
  }

  campagneCode(): string {
    if (this.estCandidat()) {
      return this.data.candidat?.collecte.campagneCode ?? '—';
    }
    return this.data.paiement?.campagneCode ?? '—';
  }

  numeroBrbc(): string {
    if (this.estCandidat()) {
      return this.data.candidat?.numeroBrbc ?? '—';
    }
    return this.data.paiement?.numeroBrbc ?? '—';
  }

  nombreLots(): number {
    if (this.estCandidat()) {
      return this.data.candidat?.nombreLots ?? 1;
    }
    return this.data.paiement?.nombreLots ?? 1;
  }

  referenceLot(): string | null {
    if (this.estCandidat()) {
      return this.data.candidat?.collecte.referenceLot ?? null;
    }
    return this.data.paiement?.collecteReference ?? null;
  }

  comptoirNom(): string | null {
    if (this.estCandidat()) {
      return this.data.candidat?.collecte.comptoirNom ?? null;
    }
    return null;
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

  formaterDate(valeur: string | null | undefined): string {
    if (!valeur) return '—';
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(valeur));
  }

  valider2faAction(): void {
    this.dialogRef.close({ action: 'valider2fa', paiement: this.data.paiement });
  }

  rejeterAction(): void {
    this.dialogRef.close({ action: 'rejeter', paiement: this.data.paiement });
  }

  initierAction(): void {
    this.dialogRef.close({ action: 'initier', candidat: this.data.candidat });
  }

  fermer(): void {
    this.dialogRef.close();
  }
}

