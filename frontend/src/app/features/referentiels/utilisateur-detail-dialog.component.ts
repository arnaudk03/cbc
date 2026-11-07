import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ROLE_LABELS } from '../../core/auth.models';
import { Utilisateur } from '../../core/utilisateur.models';

export interface UtilisateurDetailDialogData {
  utilisateur: Utilisateur;
}

@Component({
  selector: 'app-utilisateur-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './utilisateur-detail-dialog.component.html',
  styleUrl: './utilisateur-detail-dialog.component.scss'
})
export class UtilisateurDetailDialogComponent {
  readonly data = inject<UtilisateurDetailDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<UtilisateurDetailDialogComponent>);

  statutLibelle(): string {
    if (this.data.utilisateur.compteBloque) {
      return 'Bloqué';
    }
    return this.data.utilisateur.actif ? 'Actif' : 'Inactif';
  }

  comptoirLibelle(): string {
    const { comptoirCode, comptoirNom } = this.data.utilisateur;
    if (!comptoirCode && !comptoirNom) {
      return 'Global';
    }
    return [comptoirCode, comptoirNom].filter(Boolean).join(' — ');
  }

  libelleRole(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }

  formaterDate(valeur: string | null): string {
    if (!valeur) {
      return '—';
    }
    if (/^\d{4}-\d{2}-\d{2}$/.test(valeur)) {
      const [annee, mois, jour] = valeur.split('-');
      return `${jour}/${mois}/${annee}`;
    }
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(valeur));
  }

  fermer(): void {
    this.dialogRef.close();
  }
}
