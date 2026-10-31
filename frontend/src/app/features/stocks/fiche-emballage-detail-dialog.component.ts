import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {
  CATEGORIE_EMBALLAGE_LABELS,
  FicheStockMateriel,
  STATUT_FICHE_MATERIEL_LABELS,
  UNITE_MATERIEL_LABELS
} from './emballage.models';

export interface FicheEmballageDetailDialogData {
  fiche: FicheStockMateriel;
}

@Component({
  selector: 'app-fiche-emballage-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './fiche-emballage-detail-dialog.component.html',
  styleUrl: './fiche-emballage-detail-dialog.component.scss'
})
export class FicheEmballageDetailDialogComponent {
  readonly data = inject<FicheEmballageDetailDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<FicheEmballageDetailDialogComponent>);

  libelleStatut(statut: string): string {
    return STATUT_FICHE_MATERIEL_LABELS[statut as keyof typeof STATUT_FICHE_MATERIEL_LABELS]
      ?? statut;
  }

  libelleCategorie(categorie: string): string {
    return CATEGORIE_EMBALLAGE_LABELS[categorie as keyof typeof CATEGORIE_EMBALLAGE_LABELS]
      ?? categorie;
  }

  libelleUnite(unite: string): string {
    return UNITE_MATERIEL_LABELS[unite as keyof typeof UNITE_MATERIEL_LABELS] ?? unite;
  }

  fermer(): void {
    this.dialogRef.close();
  }
}
