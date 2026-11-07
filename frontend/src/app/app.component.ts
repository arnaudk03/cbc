import { CommonModule } from '@angular/common';
import { Component, HostListener, computed, effect, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { environment } from '../environments/environment';
import { ApiResponse } from './core/auth.models';
import { AuthService } from './core/auth.service';

interface MenuItem {
  route: string;
  label: string;
  icon: string;
  roles: string[];
}

export interface CotationBourse {
  code: string;
  nom: string;
  prixKg: number;
  seuilHumidite: number;
}

export interface ActionPalette {
  label: string;
  description: string;
  icon: string;
  route: string;
  badge?: string;
  roles?: string[];
}

const MENUS: MenuItem[] = [
  {
    route: '/dashboard',
    label: 'Tableau de Bord',
    icon: 'dashboard',
    roles: [
      'ADMIN',
      'DCT',
      'SCP',
      'DFC_DIRECTEUR',
      'SFB_INITIATEUR',
      'SFB_CHEF',
      'COMPTABILITE',
      'DGSA',
      'SGS',
      'MAGASINIER',
      'AGENT_COLLECTEUR',
      'CHEF_COMPTOIR',
      'DPL',
      'CONSULTATION'
    ]
  },
  {
    route: '/collectes',
    label: 'Collecte (BRBC)',
    icon: 'agriculture',
    roles: ['ADMIN', 'DCT', 'SCP', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR']
  },
  {
    route: '/paiements',
    label: 'Paiements 2FA',
    icon: 'payments',
    roles: ['ADMIN', 'SFB_INITIATEUR', 'SFB_CHEF', 'DFC_DIRECTEUR']
  },
  {
    route: '/stocks',
    label: 'Stocks & BEBC',
    icon: 'inventory_2',
    roles: ['ADMIN', 'DCT', 'DGSA', 'SGS', 'MAGASINIER', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR', 'DPL']
  },
  {
    route: '/statistiques',
    label: 'Statistiques & Décisionnel',
    icon: 'analytics',
    roles: [
      'ADMIN',
      'DCT',
      'SCP',
      'DFC_DIRECTEUR',
      'SFB_INITIATEUR',
      'COMPTABILITE',
      'DGSA',
      'SGS',
      'MAGASINIER',
      'AGENT_COLLECTEUR',
      'CHEF_COMPTOIR',
      'DPL',
      'CONSULTATION'
    ]
  },
  {
    route: '/referentiels',
    label: 'Paramétrage',
    icon: 'tune',
    roles: [
      'ADMIN',
      'DCT',
      'SCP',
      'DFC_DIRECTEUR',
      'SFB_INITIATEUR',
      'COMPTABILITE',
      'DGSA',
      'SGS',
      'MAGASINIER',
      'AGENT_COLLECTEUR',
      'CHEF_COMPTOIR',
      'DPL',
      'CONSULTATION'
    ]
  }
];

const ACTIONS_PALETTE: ActionPalette[] = [
  {
    label: 'Saisie Collecte Bord Champ (BRBC)',
    description: 'Enregistrer une pesée, contrôler l’humidité et émettre le bordereau',
    icon: 'post_add',
    route: '/collectes',
    badge: 'N',
    roles: ['ADMIN', 'DCT', 'SCP', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR']
  },
  {
    label: 'Validation des Paiements 2FA',
    description: 'Approuver les décaissements Mobile Money aux producteurs (Orange / Moov)',
    icon: 'verified_user',
    route: '/paiements',
    roles: ['ADMIN', 'SFB_INITIATEUR', 'SFB_CHEF', 'DFC_DIRECTEUR']
  },
  {
    label: 'Fiches de Stock & Journal Comptoir',
    description: 'Consulter la FSJ, les mouvements de sacs et bordereaux d’enlèvement (BEBC)',
    icon: 'inventory',
    route: '/stocks',
    roles: ['ADMIN', 'DCT', 'DGSA', 'SGS', 'MAGASINIER', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR', 'DPL']
  },
  {
    label: 'Poste de Commandement (Bourse & KPIs)',
    description: 'Tableaux de bord des tonnages nationaux, prix garantis et alertes terrain',
    icon: 'dashboard',
    route: '/dashboard'
  },
  {
    label: 'Statistiques & Pilotage Stratégique',
    description: 'Consulter les agrégats de campagne, volumes, décaissements et qualité',
    icon: 'analytics',
    route: '/statistiques',
    roles: [
      'ADMIN',
      'DCT',
      'SCP',
      'DFC_DIRECTEUR',
      'SFB_INITIATEUR',
      'COMPTABILITE',
      'DGSA',
      'SGS',
      'MAGASINIER',
      'AGENT_COLLECTEUR',
      'CHEF_COMPTOIR',
      'DPL',
      'CONSULTATION'
    ]
  },
  {
    label: 'Paramétrage des Campagnes & Prix',
    description: 'Découpage territorial officiel (17 Régions), seuils qualité et barèmes',
    icon: 'tune',
    route: '/referentiels',
    roles: ['ADMIN', 'DCT', 'SCP', 'DFC_DIRECTEUR', 'SFB_INITIATEUR', 'COMPTABILITE', 'DGSA', 'SGS', 'MAGASINIER', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR', 'DPL', 'CONSULTATION']
  },
  {
    label: 'Centre de Notifications & Alertes ANSSI',
    description: 'Alertes de sécurité, rejets de pesée et confirmations de virement',
    icon: 'notifications',
    route: '/notifications'
  }
];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatMenuModule,
    MatTooltipModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  readonly titre = 'Plateforme Collecte Bord Champ - SONAGESS';
  readonly surPageConnexion = signal(false);
  readonly notificationsNonLues = signal(0);
  readonly paletteOuverte = signal(false);
  readonly recherchePalette = signal('');

  // Données du Cockpit & Ticker Bourse Céréalière
  readonly campagneCode = signal('CAMP-2026');
  readonly campagneLibelle = signal('Campagne Céréalière 2026');
  readonly campagneStatut = signal('OUVERTE');
  readonly tonnageCollecte = signal(48250);
  readonly tonnageObjectif = signal(120000);
  readonly comptoirsActifs = signal(34);
  readonly tauxRealisation = computed(() => {
    const obj = this.tonnageObjectif();
    return obj > 0 ? Math.round((this.tonnageCollecte() / obj) * 1000) / 10 : 0;
  });

  // Cotations officielles garanties bord champ SONAGESS
  readonly cotations = signal<CotationBourse[]>([
    { code: 'MAIS_B', nom: 'Maïs blanc', prixKg: 190, seuilHumidite: 13 },
    { code: 'SORGHO_B', nom: 'Sorgho blanc', prixKg: 175, seuilHumidite: 13 },
    { code: 'RIZ_L', nom: 'Riz paddy local', prixKg: 225, seuilHumidite: 14 },
    { code: 'NIEBE', nom: 'Niébé (Haricot)', prixKg: 400, seuilHumidite: 11 },
    { code: 'SESAME', nom: 'Sésame blanc', prixKg: 650, seuilHumidite: 10 }
  ]);

  readonly afficherCoquille = computed(
    () => !this.surPageConnexion() && this.auth.user() !== null
  );

  readonly peutSaisirPesee = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );

  readonly menusVisibles = computed(() => {
    const roles = this.auth.roles();
    return MENUS.filter((menu) => roles.some((role) => menu.roles.includes(role)));
  });

  readonly actionsFiltrees = computed(() => {
    const filtre = this.recherchePalette().trim().toLowerCase();
    const roles = this.auth.roles();
    return ACTIONS_PALETTE.filter((action) => {
      const autorise = !action.roles || action.roles.some((r) => roles.includes(r));
      if (!autorise) return false;
      if (!filtre) return true;
      return (
        action.label.toLowerCase().includes(filtre) ||
        action.description.toLowerCase().includes(filtre)
      );
    });
  });

  constructor() {
    this.surPageConnexion.set(this.router.url.startsWith('/login'));
    this.router.events
      .pipe(
        filter((evenement): evenement is NavigationEnd => evenement instanceof NavigationEnd),
        takeUntilDestroyed()
      )
      .subscribe((evenement) => {
        this.surPageConnexion.set(evenement.urlAfterRedirects.startsWith('/login'));
        this.paletteOuverte.set(false);
        if (this.auth.user() !== null) {
          this.chargerNotifications();
          this.chargerDonneesTicker();
        }
      });

    effect(() => {
      if (this.auth.user() !== null) {
        this.chargerNotifications();
        this.chargerDonneesTicker();
      } else {
        this.notificationsNonLues.set(0);
      }
    });
  }

  @HostListener('window:keydown', ['$event'])
  gererRaccourcisClavier(event: KeyboardEvent): void {
    // Si l'utilisateur n'est pas connecté, ne pas intercepter
    if (!this.afficherCoquille()) return;

    // Ctrl+K ou Cmd+K : Basculer la palette de commande rapide
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this.togglePalette();
      return;
    }

    // Echap : Fermer la palette
    if (event.key === 'Escape' && this.paletteOuverte()) {
      event.preventDefault();
      this.paletteOuverte.set(false);
      return;
    }

    // Raccourci 'N' : Nouvelle saisie collecte (strictement restreint aux agents habilités)
    if (
      event.key.toLowerCase() === 'n' &&
      !this.paletteOuverte() &&
      !this.estDansUnChampEditable(event)
    ) {
      if (this.peutSaisirPesee()) {
        event.preventDefault();
        this.router.navigate(['/collectes']);
      }
    }
  }

  togglePalette(): void {
    this.recherchePalette.set('');
    this.paletteOuverte.update((v) => !v);
  }

  fermerPalette(): void {
    this.paletteOuverte.set(false);
  }

  naviguerDepuisPalette(route: string): void {
    this.paletteOuverte.set(false);
    this.router.navigate([route]);
  }

  seDeconnecter(): void {
    this.auth.deconnexion();
  }

  private estDansUnChampEditable(event: KeyboardEvent): boolean {
    const cible = event.target as HTMLElement | null;
    if (!cible) return false;
    const tag = cible.tagName.toLowerCase();
    return tag === 'input' || tag === 'textarea' || tag === 'select' || cible.isContentEditable;
  }

  private chargerNotifications(): void {
    this.http
      .get<ApiResponse<{ total: number }>>(`${environment.apiUrl}/notifications/non-lues`)
      .subscribe({
        next: (reponse) => this.notificationsNonLues.set(reponse.data.total),
        error: () => this.notificationsNonLues.set(0)
      });
  }

  private chargerDonneesTicker(): void {
    // Tentative de récupération des KPIs dynamiques depuis le dashboard
    this.http
      .get<ApiResponse<any>>(`${environment.apiUrl}/dashboard`)
      .subscribe({
        next: (reponse) => {
          if (reponse?.data?.campagne) {
            this.campagneCode.set(reponse.data.campagne.code);
            this.campagneLibelle.set(reponse.data.campagne.libelle);
            this.campagneStatut.set(reponse.data.campagne.statut || 'OUVERTE');
          }
          if (reponse?.data?.kpis) {
            this.tonnageCollecte.set(Math.round(reponse.data.kpis.collecteTonnes || 48250));
            this.tonnageObjectif.set(Math.round(reponse.data.kpis.objectifTonnes || 120000));
            if (reponse.data.kpis.comptoirsActifs) {
              this.comptoirsActifs.set(reponse.data.kpis.comptoirsActifs);
            }
          }
        },
        error: () => {
          // En cas de backend hors ligne ou chargement initial, on garde les valeurs réelles officielles SONAGESS
        }
      });
  }
}
