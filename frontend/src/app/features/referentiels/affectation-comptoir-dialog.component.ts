import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Comptoir } from '../../core/api.models';
import { ROLE_LABELS } from '../../core/auth.models';
import { Utilisateur } from '../../core/utilisateur.models';

export interface AffectationComptoirDialogData {
  comptoir: Comptoir;
  utilisateurs: Utilisateur[];
}

@Component({
  selector: 'app-affectation-comptoir-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './affectation-comptoir-dialog.component.html',
  styleUrl: './affectation-comptoir-dialog.component.scss'
})
export class AffectationComptoirDialogComponent {
  readonly data = inject<AffectationComptoirDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<AffectationComptoirDialogComponent>);

  readonly recherche = signal('');
  readonly selection = signal<number[]>([]);

  readonly disponibles = computed(() => {
    const recherche = this.recherche().trim().toLowerCase();
    return this.data.utilisateurs
      .filter((utilisateur) => utilisateur.comptoirId === null)
      .filter(
        (utilisateur) =>
          !recherche ||
          utilisateur.nomComplet.toLowerCase().includes(recherche) ||
          utilisateur.matricule.toLowerCase().includes(recherche) ||
          utilisateur.email.toLowerCase().includes(recherche)
      )
      .sort((a, b) => a.nomComplet.localeCompare(b.nomComplet, 'fr'));
  });

  readonly dejaAffectes = computed(
    () =>
      this.data.utilisateurs.filter(
        (utilisateur) => utilisateur.comptoirId === this.data.comptoir.id
      ).length
  );

  readonly affectesAilleurs = computed(
    () =>
      this.data.utilisateurs.filter(
        (utilisateur) =>
          utilisateur.comptoirId !== null && utilisateur.comptoirId !== this.data.comptoir.id
      ).length
  );

  readonly tousSelectionnes = computed(
    () =>
      this.disponibles().length > 0 &&
      this.disponibles().every((utilisateur) => this.selection().includes(utilisateur.id))
  );

  estSelectionne(id: number): boolean {
    return this.selection().includes(id);
  }

  basculer(id: number, coche: boolean): void {
    if (coche) {
      if (!this.estSelectionne(id)) {
        this.selection.set([...this.selection(), id]);
      }
      return;
    }
    this.selection.set(this.selection().filter((selectionne) => selectionne !== id));
  }

  basculerTout(coche: boolean): void {
    if (coche) {
      const ids = new Set(this.selection());
      for (const utilisateur of this.disponibles()) {
        ids.add(utilisateur.id);
      }
      this.selection.set([...ids]);
      return;
    }
    const disponibles = new Set(this.disponibles().map((utilisateur) => utilisateur.id));
    this.selection.set(this.selection().filter((id) => !disponibles.has(id)));
  }

  libelleRole(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }

  annuler(): void {
    this.dialogRef.close();
  }

  valider(): void {
    if (this.selection().length === 0) {
      return;
    }
    this.dialogRef.close([...this.selection()]);
  }
}
