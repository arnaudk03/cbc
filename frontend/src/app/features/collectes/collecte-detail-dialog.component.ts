import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  Collecte,
  CollecteStatut,
  STATUT_COLLECTE_COLORS,
  STATUT_COLLECTE_LABELS
} from './collecte.models';

export interface CollecteDetailDialogData {
  collecte: Collecte;
  peutExaminer?: boolean;
  peutValiderCollecte?: boolean;
  estExaminateur?: boolean;
}

export type CollecteDialogAction = 'brbc' | 'examiner' | 'valider';

export interface CollecteDialogResult {
  action: CollecteDialogAction;
  collecte: Collecte;
  approuve?: boolean;
  motif?: string;
}

@Component({
  selector: 'app-collecte-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule
  ],
  template: `
    <div class="dialog-header">
      <div class="header-icon" [ngClass]="data.collecte.statut.toLowerCase()">
        <mat-icon>inventory_2</mat-icon>
      </div>
      <div class="header-text">
        <span class="header-sub">
          Dossier de collecte bord champ · Campagne {{ data.collecte.campagneCode }}
        </span>
        <h2 mat-dialog-title>
          Lot {{ data.collecte.referenceLot }}
        </h2>
      </div>
      <button mat-icon-button class="btn-fermer" (click)="fermer()" aria-label="Fermer">
        <mat-icon>close</mat-icon>
      </button>
    </div>

    <mat-dialog-content class="dialog-content">
      <!-- Synthèse rapide du lot -->
      <div class="hero-summary-card">
        <div class="summary-col">
          <span class="label">Spéculation</span>
          <strong class="valeur-spec">{{ data.collecte.speculationNom }}</strong>
          <small class="sub-spec">{{ data.collecte.fichePesee?.nombreSacs ?? 0 }} sac(s)</small>
        </div>
        <div class="summary-col">
          <span class="label">Poids Net Pesé</span>
          <strong class="valeur-net">{{ data.collecte.quantiteKg | number:'1.0-2' }} <small>kg</small></strong>
          <small class="sub-spec">{{ data.collecte.prixUnitaireKg | number:'1.0-0' }} CFA/kg</small>
        </div>
        <div class="summary-col montant-col">
          <span class="label">Valeur Nette du Lot</span>
          <strong class="valeur-montant">{{ formaterMontant(data.collecte.montantNetCfa) }}</strong>
          <small class="sub-spec">Net à payer producteur</small>
        </div>
        <div class="summary-col status-col">
          <span class="label">Statut du dossier</span>
          <span class="status-chip" [ngClass]="couleurStatut(data.collecte.statut)">
            {{ libelleStatut(data.collecte.statut) }}
          </span>
        </div>
      </div>

      <!-- Section Producteur & Comptoir -->
      <div class="detail-section">
        <div class="section-title">
          <mat-icon>storefront</mat-icon>
          <span>Producteur & Comptoir d'Apport</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="item-label">Producteur apporteur</span>
            <strong class="item-value">{{ data.collecte.producteurNom }}</strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Téléphone Mobile Money</span>
            <strong class="item-value telephone">
              <mat-icon>call</mat-icon>
              {{ data.collecte.telephoneProducteur }}
              @if (data.collecte.operateurMobile) {
                <span class="operateur-badge">({{ data.collecte.operateurMobile }})</span>
              }
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Comptoir de réception</span>
            <strong class="item-value">
              {{ data.collecte.comptoirNom }}
              <span class="code-pill">{{ data.collecte.comptoirCode }}</span>
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Région</span>
            <strong class="item-value">{{ data.collecte.region ?? '—' }}</strong>
          </div>
        </div>
      </div>

      <!-- Section Pesée & Qualité -->
      <div class="detail-section">
        <div class="section-title">
          <mat-icon>scale</mat-icon>
          <span>Fiche de Pesée & Contrôle Qualité</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="item-label">Fiche de pesée N°</span>
            <strong class="item-value code-mono">{{ data.collecte.fichePesee?.numeroFiche ?? '—' }}</strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Poids brut / Tare</span>
            <strong class="item-value">
              {{ data.collecte.fichePesee?.poidsBrutTotalKg ?? '—' }} kg
              / {{ data.collecte.fichePesee?.tareEmballageKg ?? '—' }} kg
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Humidité / Impuretés</span>
            <strong class="item-value">
              {{ data.collecte.controleQualite?.tauxHumidite ?? '—' }} %
              / {{ data.collecte.controleQualite?.tauxImpuretes ?? '—' }} %
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Grains avariés</span>
            <strong class="item-value">{{ data.collecte.controleQualite?.grainsAvaries ?? '—' }} %</strong>
          </div>
          <div class="detail-item full-width">
            <span class="item-label">Conformité aux normes SONAGESS</span>
            @if (data.collecte.controleQualite?.conforme !== false) {
              <div class="qualite-badge conforme">
                <mat-icon>verified</mat-icon>
                <span>Conforme aux normes de sécurité alimentaire SONAGESS</span>
              </div>
            } @else {
              <div class="qualite-badge non-conforme">
                <mat-icon>warning</mat-icon>
                <span>Non conforme aux exigences qualité</span>
              </div>
            }
          </div>
        </div>
      </div>

      <!-- Section Bordereau BRBC -->
      <div class="detail-section">
        <div class="section-title">
          <mat-icon>receipt_long</mat-icon>
          <span>Bordereau de Réception Bord Champ (BRBC)</span>
        </div>
        @if (data.collecte.bordereau; as brbc) {
          <div class="detail-grid">
            <div class="detail-item">
              <span class="item-label">Numéro BRBC</span>
              <strong class="item-value brbc-ref">{{ brbc.numeroBrbc }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">Statut du bordereau</span>
              <strong class="item-value">{{ brbc.statutValidation }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">Lots rattachés au BRBC</span>
              <strong class="item-value">{{ brbc.nombreLots }} lot(s)</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">Montant total net BRBC</span>
              <strong class="item-value montant-vert">{{ formaterMontant(brbc.montantNetPayerCfa) }}</strong>
            </div>
            @if (brbc.montantEnLettres) {
              <div class="detail-item full-width">
                <span class="item-label">Montant arrêté en toutes lettres</span>
                <span class="item-value en-lettres">{{ brbc.montantEnLettres }}</span>
              </div>
            }
          </div>
        } @else {
          <div class="brbc-aucun">
            <mat-icon>info</mat-icon>
            <span>Aucun bordereau BRBC rattaché à ce lot.</span>
          </div>
        }
      </div>

      <!-- Section Traçabilité & Visa -->
      <div class="detail-section">
        <div class="section-title">
          <mat-icon>history_edu</mat-icon>
          <span>Circuit d'Audit & Validation</span>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="item-label">Date & heure de collecte terrain</span>
            <strong class="item-value">{{ formaterDate(data.collecte.dateHeureCollecte) }}</strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Examinateur SCP</span>
            <strong class="item-value">
              {{ data.collecte.examinateur ?? 'En attente d’examen' }}
              @if (data.collecte.dateExamen) {
                <span class="item-sub-date">le {{ formaterDate(data.collecte.dateExamen) }}</span>
              }
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Validateur DCT</span>
            <strong class="item-value">
              {{ data.collecte.validateur ?? 'En attente de validation' }}
              @if (data.collecte.dateValidation) {
                <span class="item-sub-date">le {{ formaterDate(data.collecte.dateValidation) }}</span>
              }
            </strong>
          </div>
          <div class="detail-item">
            <span class="item-label">Mode de règlement</span>
            <strong class="item-value">{{ data.collecte.bordereau?.modePaiement || 'MOBILE_MONEY' }}</strong>
          </div>

          @if (data.collecte.motifRejet) {
            <div class="detail-item full-width motif-rejet-box">
              <mat-icon>cancel</mat-icon>
              <div>
                <span class="item-label">Motif du Rejet</span>
                <strong class="motif-texte">{{ data.collecte.motifRejet }}</strong>
              </div>
            </div>
          }
        </div>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-stroked-button type="button" (click)="fermer()">
        Fermer
      </button>

      <!-- Visualiser le BRBC officiel -->
      <button
        mat-stroked-button
        color="primary"
        type="button"
        (click)="actionOuvrirBrbc()">
        <mat-icon>receipt_long</mat-icon>
        Afficher BRBC Officiel
      </button>

      <!-- Décision SCP -->
      @if (data.peutExaminer && data.collecte.statut === 'SYNCHRONISEE') {
        <button mat-stroked-button color="warn" type="button" (click)="examinerAction(false)">
          <mat-icon>cancel</mat-icon>
          Rejeter SCP
        </button>
        <button mat-flat-button color="primary" type="button" (click)="examinerAction(true)">
          <mat-icon>check_circle</mat-icon>
          Approuver SCP
        </button>
      }

      <!-- Décision DCT -->
      @if (data.peutValiderCollecte && data.collecte.statut === 'EXAMINEE_SCP') {
        <button mat-stroked-button color="warn" type="button" (click)="validerAction(false)">
          <mat-icon>cancel</mat-icon>
          Rejeter DCT
        </button>
        <button mat-flat-button color="primary" type="button" (click)="validerAction(true)">
          <mat-icon>verified</mat-icon>
          Valider DCT
        </button>
      } @else if (data.estExaminateur && data.collecte.statut === 'EXAMINEE_SCP') {
        <span class="sod-warning" matTooltip="Séparation des fonctions : vous avez déjà examiné ce dossier.">
          <mat-icon>shield</mat-icon> SoD : Dossier déjà examiné par vous
        </span>
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

        &.synchronisee {
          background: #e0f2fe;
          color: #0284c7;
        }

        &.examinee_scp {
          background: #fef3c7;
          color: #d97706;
        }

        &.validee_dct {
          background: #dcfce7;
          color: #16a34a;
        }

        &.rejetee {
          background: #fee2e2;
          color: #dc2626;
        }

        mat-icon {
          font-size: 26px;
          width: 26px;
          height: 26px;
        }
      }

      .header-text {
        flex: 1;

        .header-sub {
          display: block;
          font-size: 0.78rem;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.04em;
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
        gap: 1.1rem;
        max-height: 75vh;
      }

      .hero-summary-card {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 0.75rem;
        padding: 1rem 1.25rem;
        background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        border: 1px solid #cbd5e1;
        border-radius: 10px;

        @media (max-width: 700px) {
          grid-template-columns: repeat(2, 1fr);
        }

        .summary-col {
          display: flex;
          flex-direction: column;
          gap: 0.2rem;

          .label {
            font-size: 0.72rem;
            font-weight: 600;
            color: #64748b;
            text-transform: uppercase;
          }

          .valeur-spec {
            font-size: 1.05rem;
            font-weight: 700;
            color: #0f172a;
          }

          .valeur-net {
            font-size: 1.15rem;
            font-weight: 700;
            color: #0284c7;

            small {
              font-size: 0.8rem;
              font-weight: 600;
            }
          }

          .valeur-montant {
            font-size: 1.15rem;
            font-weight: 800;
            color: #0b6623;
          }

          .sub-spec {
            font-size: 0.78rem;
            color: #64748b;
          }

          &.status-col {
            align-items: flex-start;
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
          font-size: 0.82rem;
          font-weight: 700;
          color: #1e293b;
          text-transform: uppercase;
          letter-spacing: 0.03em;
          margin-bottom: 0.85rem;
          padding-bottom: 0.45rem;
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
        gap: 0.8rem 1.25rem;

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
            font-size: 0.74rem;
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

            &.montant-vert {
              color: #0b6623;
              font-weight: 700;
            }

            &.en-lettres {
              font-style: italic;
              color: #334155;
            }

            &.code-mono {
              font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
              font-size: 0.85rem;
            }

            .item-sub-date {
              display: block;
              font-size: 0.78rem;
              font-weight: normal;
              color: #64748b;
            }

            .code-pill {
              font-size: 0.75rem;
              background: #f1f5f9;
              padding: 0.15rem 0.4rem;
              border-radius: 4px;
              margin-left: 0.35rem;
              color: #475569;
            }
          }
        }
      }

      .operateur-badge {
        font-size: 0.75rem;
        background: #e0f2fe;
        color: #0284c7;
        padding: 0.1rem 0.35rem;
        border-radius: 4px;
      }

      .qualite-badge {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.5rem 0.75rem;
        border-radius: 6px;
        font-size: 0.85rem;
        font-weight: 600;

        &.conforme {
          background: #dcfce7;
          color: #166534;
          border: 1px solid #bbf7d0;

          mat-icon {
            color: #16a34a;
            font-size: 20px;
            width: 20px;
            height: 20px;
          }
        }

        &.non-conforme {
          background: #fee2e2;
          color: #991b1b;
          border: 1px solid #fecaca;

          mat-icon {
            color: #dc2626;
            font-size: 20px;
            width: 20px;
            height: 20px;
          }
        }
      }

      .motif-rejet-box {
        display: flex;
        align-items: flex-start;
        gap: 0.75rem;
        padding: 0.75rem 1rem;
        background: #fef2f2;
        border-left: 4px solid #ef4444;
        border-radius: 6px;
        margin-top: 0.25rem;

        mat-icon {
          color: #ef4444;
          font-size: 20px;
          width: 20px;
          height: 20px;
        }

        .motif-texte {
          color: #b91c1c;
          font-size: 0.9rem;
        }
      }

      .brbc-aucun {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        color: #64748b;
        font-size: 0.85rem;
        font-style: italic;

        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
        }
      }

      .status-chip {
        display: inline-flex;
        align-items: center;
        padding: 0.2rem 0.65rem;
        border-radius: 9999px;
        font-size: 0.75rem;
        font-weight: 600;

        &.succes {
          background: #dcfce7;
          color: #15803d;
        }
        &.info {
          background: #e0f2fe;
          color: #0369a1;
        }
        &.alerte {
          background: #fef3c7;
          color: #b45309;
        }
        &.danger {
          background: #fee2e2;
          color: #b91c1c;
        }
        &.neutre {
          background: #f1f5f9;
          color: #475569;
        }
      }

      .dialog-actions {
        padding: 0.85rem 1.5rem !important;
        border-top: 1px solid #e2e8f0;
        gap: 0.5rem;
      }

      .sod-warning {
        display: flex;
        align-items: center;
        gap: 0.35rem;
        font-size: 0.8rem;
        color: #d97706;
        font-weight: 500;
        background: #fef3c7;
        padding: 0.35rem 0.65rem;
        border-radius: 6px;

        mat-icon {
          font-size: 16px;
          width: 16px;
          height: 16px;
        }
      }
    `
  ]
})
export class CollecteDetailDialogComponent {
  readonly data = inject<CollecteDetailDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CollecteDetailDialogComponent>);

  libelleStatut(statut: CollecteStatut): string {
    return STATUT_COLLECTE_LABELS[statut] ?? statut;
  }

  couleurStatut(statut: CollecteStatut): string {
    return STATUT_COLLECTE_COLORS[statut] ?? 'neutre';
  }

  formaterDate(valeur: string | null): string {
    if (!valeur) return '—';
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(valeur));
  }

  formaterMontant(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) return '—';
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} FCFA`;
  }

  actionOuvrirBrbc(): void {
    this.dialogRef.close({ action: 'brbc', collecte: this.data.collecte });
  }

  examinerAction(approuve: boolean): void {
    this.dialogRef.close({ action: 'examiner', approuve, collecte: this.data.collecte });
  }

  validerAction(approuve: boolean): void {
    this.dialogRef.close({ action: 'valider', approuve, collecte: this.data.collecte });
  }

  fermer(): void {
    this.dialogRef.close();
  }
}

