import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
  FicheStockMaterielPayload,
  MaterielEmballage,
  StockMateriel,
  UNITE_MATERIEL_LABELS
} from './emballage.models';

export interface FicheEmballageDialogData {
  comptoirId: number;
  comptoirCode: string;
  materiels: MaterielEmballage[];
  stock: StockMateriel[];
}

@Component({
  selector: 'app-fiche-emballage-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './fiche-emballage-dialog.component.html',
  styleUrl: './fiche-emballage-dialog.component.scss'
})
export class FicheEmballageDialogComponent {
  readonly data = inject<FicheEmballageDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<FicheEmballageDialogComponent>);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    dateFiche: [this.aujourdhui(), Validators.required],
    observations: [''],
    lignes: this.fb.array(
      this.data.materiels.map((materiel) =>
        this.fb.group({
          selectionne: [false],
          materielId: [materiel.id],
          entrees: [0, [Validators.min(0)]],
          sorties: [0, [Validators.min(0)]],
          stockPhysique: this.fb.control<number | null>(null, [Validators.min(0)])
        })
      )
    )
  });

  erreur: string | null = null;

  get lignes(): FormArray {
    return this.form.get('lignes') as FormArray;
  }

  get nombreSelectionnes(): number {
    return this.lignes.controls.filter((ligne) => ligne.get('selectionne')?.value).length;
  }

  ligneGroupe(index: number): FormGroup {
    return this.lignes.at(index) as FormGroup;
  }

  stockInitial(materielId: number): number {
    return (
      this.data.stock.find((element) => element.materielId === materielId)?.stockFinal ?? 0
    );
  }

  libelleUnite(materiel: MaterielEmballage): string {
    return UNITE_MATERIEL_LABELS[materiel.unite] ?? materiel.unite;
  }

  theorique(index: number): number {
    const ligne = this.lignes.at(index);
    const initial = this.stockInitial(Number(ligne.get('materielId')?.value));
    const entrees = Number(ligne.get('entrees')?.value) || 0;
    const sorties = Number(ligne.get('sorties')?.value) || 0;
    return initial + entrees - sorties;
  }

  ecart(index: number): number | null {
    const physique = this.lignes.at(index).get('stockPhysique')?.value;
    if (physique === null || physique === undefined || physique === '') {
      return null;
    }
    return Number(physique) - this.theorique(index);
  }

  basculerTout(coche: boolean): void {
    for (const ligne of this.lignes.controls) {
      ligne.get('selectionne')?.setValue(coche);
    }
  }

  annuler(): void {
    this.dialogRef.close();
  }

  valider(): void {
    this.erreur = null;
    if (this.form.invalid) {
      this.erreur = 'Vérifiez la date et les quantités saisies.';
      return;
    }
    if (this.nombreSelectionnes === 0) {
      this.erreur = 'Sélectionnez au moins un matériel.';
      return;
    }
    if (this.lignes.controls.some((ligne) => ligne.get('selectionne')?.value
      && Number(ligne.get('entrees')?.value || 0) < 0)) {
      this.erreur = 'Les entrées ne peuvent pas être négatives.';
      return;
    }

    const valeurs = this.form.getRawValue();
    const lignes: FicheStockMaterielPayload['lignes'] = [];
    for (const ligne of this.lignes.controls) {
      if (!ligne.get('selectionne')?.value) {
        continue;
      }
      const brute = ligne.getRawValue();
      lignes.push({
        materielId: brute.materielId,
        entrees: Number(brute.entrees) || 0,
        sorties: Number(brute.sorties) || 0,
        stockPhysique:
          brute.stockPhysique === null || brute.stockPhysique === undefined
            ? null
            : Number(brute.stockPhysique),
        referenceDoc: null,
        observation: null
      });
    }

    const payload: FicheStockMaterielPayload = {
      comptoirId: this.data.comptoirId,
      dateFiche: valeurs.dateFiche as string,
      observations: (valeurs.observations as string | null)?.trim() || null,
      lignes
    };
    this.dialogRef.close(payload);
  }

  private aujourdhui(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
