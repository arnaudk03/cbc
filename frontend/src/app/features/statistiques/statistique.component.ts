import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { Campagne, Comptoir, Speculation } from '../../core/api.models';
import { ReferentielService } from '../../core/referentiel.service';
import { Localite } from '../referentiels/referentiel.models';
import { ReferentielMetierService } from '../referentiels/referentiel.service';
import { FiltreStatistique, StatistiquesResponse } from './statistique.models';
import { StatistiqueService } from './statistique.service';

@Component({
  selector: 'app-statistique',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTabsModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './statistique.component.html',
  styleUrl: './statistique.component.scss'
})
export class StatistiqueComponent implements OnInit {
  private readonly statsService = inject(StatistiqueService);
  private readonly referentielService = inject(ReferentielService);
  private readonly referentielMetier = inject(ReferentielMetierService);
  private readonly snackBar = inject(MatSnackBar);
  readonly auth = inject(AuthService);

  readonly chargement = signal(true);
  readonly chargementExport = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly donnees = signal<StatistiquesResponse | null>(null);

  // Référentiels pour les filtres
  readonly campagnes = signal<Campagne[]>([]);
  readonly comptoirs = signal<Comptoir[]>([]);
  readonly speculations = signal<Speculation[]>([]);
  readonly localites = signal<Localite[]>([]);

  // Filtres actifs
  readonly filtreCampagneId = signal<number | null>(null);
  readonly filtreRegionId = signal<string | null>(null);
  readonly filtreComptoirId = signal<number | null>(null);
  readonly filtreSpeculationId = signal<number | null>(null);
  readonly filtreDateDebut = signal<string>('');
  readonly filtreDateFin = signal<string>('');

  // Régions extraites de l'arbre des localités
  readonly regions = computed(() =>
    this.localites()
      .filter((l) => l.type === 'REGION')
      .sort((a, b) => a.name.localeCompare(b.name, 'fr'))
  );

  // Comptoirs filtrés par région sélectionnée (si une région est choisie)
  readonly comptoirsFiltres = computed(() => {
    const regId = this.filtreRegionId();
    const liste = this.comptoirs();
    if (!regId) return liste;
    // Si la localité du comptoir correspond à la région ou un descendant
    return liste;
  });

  readonly maxRegionTonnage = computed(() =>
    Math.max(1, ...(this.donnees()?.regions ?? []).map((r) => r.tonnage))
  );

  readonly maxSpecTonnage = computed(() =>
    Math.max(1, ...(this.donnees()?.speculations ?? []).map((s) => s.tonnage))
  );

  ngOnInit(): void {
    this.chargerReferentiels();
    this.charger();
  }

  chargerReferentiels(): void {
    forkJoin({
      campagnes: this.referentielService.campagnes(),
      comptoirs: this.referentielService.comptoirs(),
      speculations: this.referentielService.speculations(),
      localites: this.referentielMetier.localites()
    }).subscribe({
      next: (res) => {
        this.campagnes.set(res.campagnes);
        this.comptoirs.set(res.comptoirs);
        this.speculations.set(res.speculations);
        this.localites.set(res.localites);

        const active = res.campagnes.find((c) => c.statut === 'OUVERTE') ?? res.campagnes[0];
        if (active && this.filtreCampagneId() === null) {
          this.filtreCampagneId.set(active.id);
        }
      },
      error: () => console.warn('Impossible de charger les référentiels de filtrage.')
    });
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    const filtre: FiltreStatistique = {
      campagneId: this.filtreCampagneId(),
      comptoirId: this.filtreComptoirId(),
      regionId: this.filtreRegionId(),
      speculationId: this.filtreSpeculationId(),
      dateDebut: this.filtreDateDebut() ? this.filtreDateDebut() : null,
      dateFin: this.filtreDateFin() ? this.filtreDateFin() : null
    };

    this.statsService.charger(filtre).subscribe({
      next: (donnees) => {
        this.donnees.set(donnees);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set(
          'Impossible de charger les données statistiques. Vérifiez votre connexion et réessayez.'
        );
        this.chargement.set(false);
      }
    });
  }

  appliquerFiltres(): void {
    this.charger();
  }

  reinitialiserFiltres(): void {
    const active = this.campagnes().find((c) => c.statut === 'OUVERTE') ?? this.campagnes()[0];
    this.filtreCampagneId.set(active ? active.id : null);
    this.filtreRegionId.set(null);
    this.filtreComptoirId.set(null);
    this.filtreSpeculationId.set(null);
    this.filtreDateDebut.set('');
    this.filtreDateFin.set('');
    this.charger();
  }

  exporterCsv(): void {
    this.chargementExport.set(true);
    const filtre: FiltreStatistique = {
      campagneId: this.filtreCampagneId(),
      comptoirId: this.filtreComptoirId(),
      regionId: this.filtreRegionId(),
      speculationId: this.filtreSpeculationId(),
      dateDebut: this.filtreDateDebut() ? this.filtreDateDebut() : null,
      dateFin: this.filtreDateFin() ? this.filtreDateFin() : null
    };

    this.statsService.telechargerCsv(filtre).subscribe({
      next: (blob) => {
        this.chargementExport.set(false);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const code = this.donnees()?.campagne?.code || 'CAMPAGNE';
        a.download = `rapport-statistique-sonagess-${code}-${new Date().toISOString().slice(0, 10)}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Rapport statistique CSV téléchargé.', 'Fermer', { duration: 3500 });
      },
      error: () => {
        this.chargementExport.set(false);
        this.snackBar.open('Échec du téléchargement du rapport CSV.', 'Fermer', { duration: 4000 });
      }
    });
  }

  imprimer(): void {
    window.print();
  }

  pourcentage(valeur: number, maximum: number): number {
    return maximum > 0 ? Math.min(100, Math.round((valeur / maximum) * 100)) : 0;
  }

  formaterNombre(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) return '0';
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 }).format(valeur);
  }

  formaterMontant(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) return '0 FCFA';
    if (valeur >= 1_000_000_000) {
      return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(
        valeur / 1_000_000_000
      )} Mrd CFA`;
    }
    if (valeur >= 1_000_000) {
      return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 }).format(
        valeur / 1_000_000
      )} M CFA`;
    }
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} CFA`;
  }

  formaterDate(valeur: string | null | undefined): string {
    if (!valeur) return '—';
    if (!valeur.includes('-')) return valeur;
    const [annee, mois, jour] = valeur.split('-');
    return `${jour}/${mois}/${annee}`;
  }
}

