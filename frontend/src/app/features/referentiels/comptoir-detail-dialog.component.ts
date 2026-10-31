import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Comptoir } from '../../core/api.models';

export interface ComptoirDetailDialogData {
  comptoir: Comptoir;
}

@Component({
  selector: 'app-comptoir-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './comptoir-detail-dialog.component.html',
  styleUrl: './comptoir-detail-dialog.component.scss'
})
export class ComptoirDetailDialogComponent {
  readonly data = inject<ComptoirDetailDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ComptoirDetailDialogComponent>);

  libelleType(type: string): string {
    return type === 'FIXE' ? 'Fixe' : type === 'MOBILE' ? 'Mobile' : type;
  }

  coordonnees(): string {
    const { latitudeGps, longitudeGps } = this.data.comptoir;
    if (latitudeGps === null || longitudeGps === null) {
      return '—';
    }
    return `${latitudeGps}, ${longitudeGps}`;
  }

  fermer(): void {
    this.dialogRef.close();
  }
}
