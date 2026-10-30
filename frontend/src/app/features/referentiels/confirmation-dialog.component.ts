import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmationDialogData {
  titre: string;
  message: string;
  actionLabel?: string;
  danger?: boolean;
}

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ data.titre }}</h2>
    <mat-dialog-content>
      <div class="confirmation-message" [class.danger]="data.danger">
        <mat-icon>{{ data.danger ? 'warning_amber' : 'help_outline' }}</mat-icon>
        <p>{{ data.message }}</p>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button type="button" (click)="dialogRef.close(false)">Annuler</button>
      <button
        mat-flat-button
        type="button"
        [color]="data.danger ? 'warn' : 'primary'"
        (click)="dialogRef.close(true)">
        {{ data.actionLabel ?? 'Confirmer' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .confirmation-message {
        display: flex;
        align-items: flex-start;
        gap: 0.75rem;
        color: #334155;
        line-height: 1.5;
      }

      .confirmation-message mat-icon {
        color: #0369a1;
      }

      .confirmation-message.danger mat-icon {
        color: #b91c1c;
      }

      .confirmation-message p {
        margin: 0;
      }
    `
  ]
})
export class ConfirmationDialogComponent {
  readonly data = inject<ConfirmationDialogData>(MAT_DIALOG_DATA);
  readonly dialogRef = inject(MatDialogRef<ConfirmationDialogComponent>);
}
