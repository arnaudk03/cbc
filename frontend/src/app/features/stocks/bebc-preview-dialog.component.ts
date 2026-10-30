import {
  AfterViewInit,
  Component,
  ElementRef,
  ViewChild,
  inject
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Transfert } from './stock.models';
import { construireDocumentBebc } from './bebc-print.util';

@Component({
  selector: 'app-bebc-preview-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Aperçu du bordereau — {{ data.numeroBebc }}</h2>
    <mat-dialog-content class="preview-content">
      <iframe #cadre class="preview-frame" title="Aperçu du bordereau d'enlèvement"></iframe>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="fermer()">Fermer</button>
      <button mat-flat-button color="primary" type="button" (click)="imprimer()">
        <mat-icon>print</mat-icon>
        Imprimer
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .preview-content {
        padding-top: 0 !important;
        overflow: hidden;
      }

      .preview-frame {
        display: block;
        width: 100%;
        height: 68vh;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        background: #ffffff;
      }
    `
  ]
})
export class BebcPreviewDialogComponent implements AfterViewInit {
  readonly data = inject<Transfert>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<BebcPreviewDialogComponent>);

  @ViewChild('cadre')
  private cadre?: ElementRef<HTMLIFrameElement>;

  ngAfterViewInit(): void {
    const document = this.cadre?.nativeElement.contentDocument;
    if (!document) {
      return;
    }
    document.open();
    document.write(construireDocumentBebc(this.data));
    document.close();
  }

  imprimer(): void {
    this.cadre?.nativeElement.contentWindow?.print();
  }

  fermer(): void {
    this.dialogRef.close();
  }
}
