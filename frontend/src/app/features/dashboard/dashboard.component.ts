import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { DashboardService } from './dashboard.service';
import { DashboardResponse } from './dashboard.models';

export interface SpeculationTableItem {
  code: string;
  nom: string;
  tonnage: number;
  partPourcent: number;
  prixGarantiKg: number;
  valeurEstimeeCfa: number;
}

const PRIX_REFERENTIELS_MAP: Record<string, number> = {
  MAIS_B: 190,
  SORGHO_B: 175,
  RIZ_L: 225,
  NIEBE: 400,
  SESAME: 650
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  readonly auth = inject(AuthService);

  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly donnees = signal<DashboardResponse | null>(null);

  readonly estProfilTerrain = computed(() =>
    this.auth.possedeUnRole('AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly peutVoirCollectes = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'DCT', 'SCP', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly peutVoirPaiements = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'SFB_INITIATEUR', 'SFB_CHEF', 'DFC_DIRECTEUR')
  );
  readonly peutVoirFinances = computed(() =>
    this.auth.possedeUnRole(
      'ADMIN',
      'DCT',
      'DGSA',
      'DFC_DIRECTEUR',
      'SFB_INITIATEUR',
      'SFB_CHEF',
      'COMPTABILITE',
      'CONSULTATION'
    )
  );
  readonly peutVoirReseau = computed(() => !this.estProfilTerrain());
  readonly aDesActions = computed(() => this.peutVoirCollectes() || this.peutVoirPaiements());
  readonly actionsEnAttente = computed(() => {
    const kpis = this.donnees()?.kpis;
    const collectes = this.peutVoirCollectes() ? kpis?.collectesEnAttente ?? 0 : 0;
    const paiements = this.peutVoirPaiements() ? kpis?.paiementsEnAttente ?? 0 : 0;
    return collectes + paiements;
  });

  readonly totalTonnage = computed(() => {
    const list = this.donnees()?.speculations ?? [];
    return list.reduce((somme, item) => somme + item.tonnage, 0) || 1;
  });

  readonly speculationsTable = computed<SpeculationTableItem[]>(() => {
    const list = this.donnees()?.speculations ?? [];
    const total = this.totalTonnage();

    return list.map((item) => {
      const prixKg = PRIX_REFERENTIELS_MAP[item.code] ?? 200;
      const part = Math.round((item.tonnage / total) * 1000) / 10;
      const valeurCfa = item.tonnage * 1000 * prixKg;

      return {
        code: item.code,
        nom: item.nom,
        tonnage: item.tonnage,
        partPourcent: part,
        prixGarantiKg: prixKg,
        valeurEstimeeCfa: valeurCfa
      };
    });
  });

  readonly regionsTriees = computed(() => {
    const list = [...(this.donnees()?.regions ?? [])];
    const total = this.totalTonnage();
    return list
      .sort((a, b) => b.tonnage - a.tonnage)
      .map((r) => ({
        region: r.region,
        tonnage: r.tonnage,
        pourcent: Math.round((r.tonnage / total) * 1000) / 10
      }));
  });

  readonly maxRegion = computed(() =>
    Math.max(1, ...(this.donnees()?.regions ?? []).map((e) => e.tonnage))
  );

  readonly campagneActive = computed(() => {
    const campagne = this.donnees()?.campagne;
    const kpis = this.donnees()?.kpis;
    if (!campagne) {
      return null;
    }
    return {
      code: campagne.code,
      libelle: campagne.libelle,
      statut: campagne.statut || 'OUVERTE',
      dateDebut: this.formaterDate(campagne.dateDebut),
      dateFin: this.formaterDate(campagne.dateFin),
      objectifTonnes: kpis?.objectifTonnes ?? 0,
      collecteTonnes: kpis?.collecteTonnes ?? 0,
      tauxRealisation: kpis?.tauxRealisation ?? 0
    };
  });

  readonly kpis = computed(() => {
    const kpis = this.donnees()?.kpis;
    const paiementsEnAttente = kpis?.paiementsEnAttente ?? 0;
    const cartes = [
      {
        titre: this.estProfilTerrain() ? 'Collecté par mon comptoir' : 'Collecté cette campagne',
        valeur: `${this.formaterNombre(kpis?.collecteTonnes ?? 0)} t`,
        detail: `${this.estProfilTerrain() ? 'Objectif national' : 'Objectif'} : ${this.formaterNombre(
          kpis?.objectifTonnes ?? 0
        )} t`,
        icone: 'warehouse',
        type: 'principal'
      },
      {
        titre: 'BRBC validés',
        valeur: this.formaterNombre(kpis?.brbcValides ?? 0),
        detail: 'Liasses certifiées DCT',
        icone: 'verified',
        type: 'succes'
      }
    ];

    if (this.peutVoirFinances()) {
      cartes.push({
        titre: 'Décaissé Mobile Money',
        valeur: this.formaterMontant(kpis?.montantDecaisseCfa ?? 0),
        detail:
          paiementsEnAttente > 0
            ? `${paiementsEnAttente} paiement(s) en attente`
            : 'Aucun paiement en attente',
        icone: 'payments',
        type: 'or'
      });
    }

    if (this.peutVoirReseau()) {
      cartes.push({
        titre: 'Comptoirs actifs',
        valeur: this.formaterNombre(kpis?.comptoirsActifs ?? 0),
        detail: 'Réseau national de collecte',
        icone: 'storefront',
        type: 'info'
      });
    }

    return cartes;
  });

  constructor() {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.dashboardService.charger().subscribe({
      next: (donnees) => {
        this.donnees.set(donnees);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set(
          'Impossible de charger les données du tableau de bord. Vérifiez votre connexion puis réessayez.'
        );
        this.chargement.set(false);
      }
    });
  }

  pourcentage(valeur: number, maximum: number): number {
    return maximum > 0 ? Math.min(100, Math.round((valeur / maximum) * 100)) : 0;
  }

  formaterNombre(valeur: number): string {
    return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 }).format(valeur);
  }

  formaterMontant(valeur: number): string {
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
    return `${this.formaterNombre(valeur)} CFA`;
  }

  private formaterDate(dateIso: string): string {
    if (!dateIso || !dateIso.includes('-')) return dateIso || '—';
    const [annee, mois, jour] = dateIso.split('-');
    return `${jour}/${mois}/${annee}`;
  }
}
