import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { BebcPreviewDialogComponent } from './bebc-preview-dialog.component';
import {
  ReceptionLigne,
  STATUT_BEBC_COLORS,
  STATUT_BEBC_LABELS,
  Transfert
} from './stock.models';

@Component({
  selector: 'app-bebc-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Détail du bordereau {{ data.numeroBebc }}</h2>
    <mat-dialog-content class="detail-content">
      <div class="detail-entete">
        <div>
          <span class="detail-label">Statut</span>
          <span class="status-chip" [ngClass]="couleurStatut()">{{ libelleStatut() }}</span>
        </div>
        <div class="detail-date">
          <span class="detail-label">Créé le</span>
          <strong>{{ formaterDate(data.creeLe) }}</strong>
        </div>
      </div>

      <section>
        <h3>Informations générales</h3>
        <div class="detail-grid">
          <div><span>Comptoir de départ</span><strong>{{ data.comptoirNom }}</strong></div>
          <div><span>Magasin de destination</span><strong>{{ data.magasinDestinationNom ?? '—' }}</strong></div>
          <div><span>Direction</span><strong>{{ data.direction ?? '—' }}</strong></div>
          <div><span>Localité</span><strong>{{ data.localite ?? '—' }}</strong></div>
          <div><span>Bénéficiaire</span><strong>{{ data.beneficiaire ?? '—' }}</strong></div>
          <div><span>Objet de l'enlèvement</span><strong>{{ data.objetEnlevement ?? '—' }}</strong></div>
          <div><span>Date d'enlèvement</span><strong>{{ formaterDate(data.dateEnlevement) }}</strong></div>
          <div><span>Date d'arrivée prévue</span><strong>{{ formaterDateCourte(data.dateArriveePrevue) }}</strong></div>
        </div>
      </section>

      <section>
        <h3>Spéculations enlevées</h3>
        <div class="detail-table-wrapper">
          <table class="detail-table">
            <thead>
              <tr>
                <th>Spéculation</th>
                <th class="align-right">Sacs</th>
                <th class="align-right">Colisage</th>
                <th class="align-right">Quantité</th>
              </tr>
            </thead>
            <tbody>
              @for (ligne of data.lignes; track ligne.id) {
                <tr>
                  <td>
                    <strong>{{ ligne.speculationNom }}</strong>
                    <span class="cell-sub">{{ ligne.speculationCode }}</span>
                  </td>
                  <td class="align-right">{{ ligne.nombreSacs }}</td>
                  <td class="align-right">{{ formaterKg(ligne.colisageKg) }}</td>
                  <td class="align-right"><strong>{{ formaterKg(ligne.quantiteKg) }}</strong></td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="4" class="empty-line">Aucune ligne de spéculation.</td>
                </tr>
              }
            </tbody>
            <tfoot>
              <tr>
                <td>Total</td>
                <td class="align-right">{{ data.nombreSacs }}</td>
                <td></td>
                <td class="align-right">{{ formaterKg(data.tonnageEnleveKg) }}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </section>

      <section>
        <h3>Transport</h3>
        <div class="detail-grid">
          <div><span>Transporteur</span><strong>{{ data.nomTransporteur }}</strong></div>
          <div><span>Véhicule</span><strong>{{ data.numeroVehicule }}</strong></div>
        </div>
      </section>

      @if (data.reception; as reception) {
        <section>
          <h3>Réception en magasin</h3>
          <div class="detail-grid">
            <div><span>Numéro de réception</span><strong>{{ reception.numeroReception }}</strong></div>
            <div><span>Magasin</span><strong>{{ reception.magasinNom }}</strong></div>
            <div><span>Magasinier</span><strong>{{ reception.magasinier }}</strong></div>
            <div><span>Date de déchargement</span><strong>{{ formaterDate(reception.dateDechargement) }}</strong></div>
            <div><span>Sacs reçus</span><strong>{{ reception.nombreSacsRecus }}</strong></div>
            <div><span>Tonnage déchargé</span><strong>{{ formaterKg(reception.tonnageDechargeKg) }}</strong></div>
            <div><span>Écart sacs</span><strong>{{ formaterEcartEntier(reception.ecartSacs) }}</strong></div>
            <div><span>Écart poids</span><strong>{{ formaterEcartKg(reception.ecartPoidsKg) }}</strong></div>
            <div><span>Conformité</span><strong>{{ reception.statutConformite }}</strong></div>
          </div>
          @if (reception.lignes.length) {
            <div class="detail-table-wrapper reception-table-wrapper">
              <table class="detail-table">
                <thead>
                  <tr>
                    <th>Spéculation</th>
                    <th class="align-right">Sacs attendus</th>
                    <th class="align-right">Sacs reçus</th>
                    <th class="align-right">Écart sacs</th>
                    <th class="align-right">Poids attendu</th>
                    <th class="align-right">Poids reçu</th>
                    <th class="align-right">Écart poids</th>
                  </tr>
                </thead>
                <tbody>
                  @for (ligne of reception.lignes; track ligne.speculationId) {
                    <tr>
                      <td>
                        <strong>{{ ligne.speculationNom }}</strong>
                        <span class="cell-sub">{{ ligne.speculationCode }}</span>
                      </td>
                      <td class="align-right">{{ ligne.nombreSacsAttendus }}</td>
                      <td class="align-right">{{ ligne.nombreSacsRecus }}</td>
                      <td
                        class="align-right"
                        [class.ecart-negatif]="ligne.ecartSacs < 0"
                        [class.ecart-positif]="ligne.ecartSacs > 0">{{ formaterEcartEntier(ligne.ecartSacs) }}</td>
                      <td class="align-right">{{ formaterKg(ligne.quantiteAttendueKg) }}</td>
                      <td class="align-right">{{ formaterKg(ligne.quantiteRecueKg) }}</td>
                      <td
                        class="align-right"
                        [class.ecart-negatif]="ligne.ecartPoidsKg < 0"
                        [class.ecart-positif]="ligne.ecartPoidsKg > 0">{{ formaterEcartKg(ligne.ecartPoidsKg) }}</td>
                    </tr>
                  }
                </tbody>
                <tfoot>
                  <tr>
                    <td>Total</td>
                    <td class="align-right">{{ totalSacsAttendus(reception.lignes) }}</td>
                    <td class="align-right">{{ reception.nombreSacsRecus }}</td>
                    <td class="align-right">{{ formaterEcartEntier(reception.ecartSacs) }}</td>
                    <td class="align-right">{{ formaterKg(data.tonnageEnleveKg) }}</td>
                    <td class="align-right">{{ formaterKg(reception.tonnageDechargeKg) }}</td>
                    <td class="align-right">{{ formaterEcartKg(reception.ecartPoidsKg) }}</td>
                  </tr>
                </tfoot>
              </table>
            </div>
          } @else {
            <p class="legacy-note">
              Le détail par spéculation n'a pas été enregistré pour cette réception antérieure.
            </p>
          }
        </section>
      } @else {
        <div class="info-banner">
          <mat-icon>schedule</mat-icon>
          <span>Ce bordereau est en attente de réception en magasin.</span>
        </div>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="fermer()">Fermer</button>
      <button mat-flat-button color="primary" type="button" (click)="apercu()">
        <mat-icon>print</mat-icon>
        Aperçu / imprimer
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .detail-content {
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
        max-height: 72vh;
        overflow-y: auto;
      }

      .detail-entete {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 1rem;
        padding: 0.85rem 1rem;
        border-radius: 10px;
        background: #f8fafc;
      }

      .detail-date {
        text-align: right;
      }

      .detail-label {
        display: block;
        margin-bottom: 3px;
        color: #64748b;
        font-size: 0.7rem;
        font-weight: 700;
        letter-spacing: 0.05em;
        text-transform: uppercase;
      }

      section h3 {
        margin: 0 0 0.65rem;
        padding-bottom: 0.4rem;
        border-bottom: 1px solid #e2e8f0;
        color: #0b4d2c;
        font-size: 0.82rem;
        font-weight: 800;
        letter-spacing: 0.04em;
        text-transform: uppercase;
      }

      .detail-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 0.7rem 1.5rem;
      }

      .detail-grid div {
        display: flex;
        flex-direction: column;
        gap: 2px;
        padding-bottom: 0.4rem;
        border-bottom: 1px solid #f1f5f9;
      }

      .detail-grid span {
        color: #64748b;
        font-size: 0.7rem;
        font-weight: 600;
      }

      .detail-grid strong {
        color: #1e293b;
        font-size: 0.86rem;
      }

      .detail-table-wrapper {
        overflow-x: auto;
      }

      .detail-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.82rem;
      }

      .detail-table th {
        padding: 0.55rem 0.65rem;
        background: #f8fafc;
        border-bottom: 1px solid #e2e8f0;
        color: #64748b;
        font-size: 0.68rem;
        font-weight: 700;
        letter-spacing: 0.05em;
        text-align: left;
        text-transform: uppercase;
      }

      .detail-table td {
        padding: 0.55rem 0.65rem;
        border-bottom: 1px solid #f1f5f9;
        color: #334155;
        vertical-align: top;
      }

      .detail-table tfoot td {
        background: #f8fafc;
        color: #0f172a;
        font-weight: 700;
      }

      .detail-table .cell-sub {
        display: block;
        color: #94a3b8;
        font-size: 0.7rem;
        font-weight: 400;
      }

      .align-right {
        text-align: right !important;
        font-variant-numeric: tabular-nums;
      }

      .empty-line {
        padding: 1.5rem !important;
        color: #64748b !important;
        text-align: center;
      }

      .info-banner {
        display: flex;
        align-items: center;
        gap: 0.6rem;
        padding: 0.75rem 0.9rem;
        border-radius: 8px;
        background: #eff6ff;
        color: #1d4ed8;
        font-size: 0.82rem;
      }

      .reception-table-wrapper {
        margin-top: 0.85rem;
      }

      .ecart-negatif {
        color: #b91c1c !important;
        font-weight: 700;
      }

      .ecart-positif {
        color: #047857 !important;
        font-weight: 700;
      }

      .legacy-note {
        margin: 0.85rem 0 0;
        padding: 0.65rem 0.8rem;
        border-radius: 8px;
        background: #fffbeb;
        color: #92400e;
        font-size: 0.8rem;
      }
    `
  ]
})
export class BebcDetailDialogComponent {
  readonly data = inject<Transfert>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<BebcDetailDialogComponent>);
  private readonly dialog = inject(MatDialog);

  libelleStatut(): string {
    return STATUT_BEBC_LABELS[this.data.statut] ?? this.data.statut;
  }

  couleurStatut(): string {
    return STATUT_BEBC_COLORS[this.data.statut] ?? 'neutre';
  }

  formaterKg(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(valeur)} kg`;
  }

  formaterEcartEntier(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    const signe = valeur > 0 ? '+' : '';
    return `${signe}${new Intl.NumberFormat('fr-FR').format(valeur)}`;
  }

  formaterEcartKg(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    const signe = valeur > 0 ? '+' : '';
    return `${signe}${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(valeur)} kg`;
  }

  totalSacsAttendus(lignes: ReceptionLigne[]): number {
    return lignes.reduce((total, ligne) => total + (Number(ligne.nombreSacsAttendus) || 0), 0);
  }

  formaterDate(valeur: string | null): string {
    if (!valeur) {
      return '—';
    }
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'long',
      timeStyle: 'short'
    }).format(new Date(valeur));
  }

  formaterDateCourte(valeur: string | null): string {
    if (!valeur) {
      return '—';
    }
    return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'long' }).format(new Date(valeur));
  }

  apercu(): void {
    this.dialog.open(BebcPreviewDialogComponent, {
      data: this.data,
      width: '920px',
      maxWidth: '95vw',
      maxHeight: '92vh',
      autoFocus: false
    });
  }

  fermer(): void {
    this.dialogRef.close();
  }
}
