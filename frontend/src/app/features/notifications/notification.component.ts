import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import {
  CANAL_LABELS,
  Notification,
  STATUT_NOTIFICATION_COLORS,
  STATUT_NOTIFICATION_LABELS
} from './notification.models';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.scss'
})
export class NotificationComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly snackBar = inject(MatSnackBar);

  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly notifications = signal<Notification[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.notificationService.lister(this.page()).subscribe({
      next: (reponse) => {
        this.notifications.set(reponse.content);
        this.totalPages.set(reponse.totalPages);
        this.totalElements.set(reponse.totalElements);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les notifications.');
        this.chargement.set(false);
      }
    });
  }

  marquerLue(notification: Notification): void {
    if (notification.statut === 'LU') {
      return;
    }
    this.notificationService.marquerLue(notification.id).subscribe({
      next: (miseAJour) => {
        this.notifications.update((liste) =>
          liste.map((element) => (element.id === miseAJour.id ? miseAJour : element))
        );
      },
      error: () => this.snackBar.open('Impossible de marquer la notification.', 'Fermer', {
        duration: 4000
      })
    });
  }

  pagePrecedente(): void {
    if (this.page() > 0) {
      this.page.update((valeur) => valeur - 1);
      this.charger();
    }
  }

  pageSuivante(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((valeur) => valeur + 1);
      this.charger();
    }
  }

  canal(canal: string): string {
    return CANAL_LABELS[canal] ?? canal;
  }

  libelleStatut(statut: string): string {
    return STATUT_NOTIFICATION_LABELS[statut] ?? statut;
  }

  couleurStatut(statut: string): string {
    return STATUT_NOTIFICATION_COLORS[statut] ?? 'neutre';
  }

  formaterDate(valeur: string | null): string {
    if (!valeur) {
      return '—';
    }
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date(valeur));
  }
}
