import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { Comptoir } from '../../core/api.models';
import { AuthService } from '../../core/auth.service';
import {
  CATEGORIE_EMBALLAGE_LABELS,
  CategorieMaterielEmballage,
  FicheStockMateriel,
  MaterielEmballage,
  MaterielEmballagePayload,
  STATUT_FICHE_MATERIEL_LABELS,
  StatutFicheStockMateriel,
  StockMateriel,
  UNITE_MATERIEL_LABELS,
  UniteMateriel
} from './emballage.models';
import { EmballageService } from './emballage.service';
import { FicheEmballageDetailDialogComponent } from './fiche-emballage-detail-dialog.component';
import {
  FicheEmballageDialogComponent
} from './fiche-emballage-dialog.component';
import { ConfirmationDialogComponent } from '../referentiels/confirmation-dialog.component';
import {
  FormFieldDefinition,
  ReferentielFormDialogComponent
} from '../referentiels/referentiel-form-dialog.component';

@Component({
  selector: 'app-emballages-stock',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './emballages-stock.component.html',
  styleUrl: './emballages-stock.component.scss'
})
export class EmballagesStockComponent {
  private readonly emballageService = inject(EmballageService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly comptoir = input<Comptoir | null>(null);

  readonly chargement = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly stock = signal<StockMateriel[]>([]);
  readonly fiches = signal<FicheStockMateriel[]>([]);
  readonly materiels = signal<MaterielEmballage[]>([]);

  readonly peutGererFiche = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'DCT', 'CHEF_COMPTOIR')
  );
  readonly peutValider = computed(() => this.auth.possedeUnRole('ADMIN', 'DCT'));
  readonly peutGererMateriels = computed(() => this.auth.possedeUnRole('ADMIN', 'DCT'));
  readonly nombreAlertes = computed(
    () => this.stock().filter((element) => element.alerte).length
  );
  readonly fichesBrouillon = computed(
    () => this.fiches().filter((fiche) => fiche.statut === 'BROUILLON').length
  );

  readonly categories = Object.entries(CATEGORIE_EMBALLAGE_LABELS) as [
    CategorieMaterielEmballage,
    string
  ][];
  readonly unites = Object.entries(UNITE_MATERIEL_LABELS) as [UniteMateriel, string][];

  constructor() {
    effect(() => {
      const comptoir = this.comptoir();
      if (!comptoir) {
        this.stock.set([]);
        this.fiches.set([]);
        this.materiels.set([]);
        return;
      }
      this.charger(comptoir);
    });
  }

  charger(comptoir: Comptoir): void {
    this.chargement.set(true);
    this.erreur.set(null);
    forkJoin({
      stock: this.emballageService.stockCourant(comptoir.id),
      fiches: this.emballageService.fiches(comptoir.id),
      materiels: this.emballageService.materiels(true)
    }).subscribe({
      next: (donnees) => {
        this.stock.set(donnees.stock);
        this.fiches.set(donnees.fiches);
        this.materiels.set(donnees.materiels);
        this.chargement.set(false);
      },
      error: (erreur) => {
        this.erreur.set(this.messageErreur(erreur));
        this.chargement.set(false);
      }
    });
  }

  creerFiche(): void {
    const comptoir = this.comptoir();
    if (!comptoir) {
      return;
    }
    this.dialog
      .open(FicheEmballageDialogComponent, {
        data: {
          comptoirId: comptoir.id,
          comptoirCode: comptoir.code,
          materiels: this.materiels(),
          stock: this.stock()
        },
        width: '980px',
        maxWidth: '97vw',
        maxHeight: '94vh'
      })
      .afterClosed()
      .subscribe((payload) => {
        if (!payload) {
          return;
        }
        this.emballageService.creerFiche(payload).subscribe({
          next: (fiche) => {
            this.notifier(`Fiche ${fiche.numeroFiche} enregistrée.`);
            this.charger(comptoir);
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  voirFiche(fiche: FicheStockMateriel): void {
    this.dialog.open(FicheEmballageDetailDialogComponent, {
      data: { fiche },
      width: '960px',
      maxWidth: '97vw',
      maxHeight: '94vh'
    });
  }

  validerFiche(fiche: FicheStockMateriel): void {
    const comptoir = this.comptoir();
    if (!comptoir) {
      return;
    }
    this.dialog
      .open(ConfirmationDialogComponent, {
        data: {
          titre: 'Valider la fiche',
          message: `Valider la fiche ${fiche.numeroFiche} du ${this.formaterDate(fiche.dateFiche)} ? Les stocks seront reportés sur la fiche suivante.`,
          actionLabel: 'Valider',
          danger: false
        },
        width: '520px',
        maxWidth: '95vw'
      })
      .afterClosed()
      .subscribe((confirme: boolean | undefined) => {
        if (!confirme) {
          return;
        }
        this.emballageService.validerFiche(fiche.id).subscribe({
          next: () => {
            this.notifier(`Fiche ${fiche.numeroFiche} validée.`);
            this.charger(comptoir);
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  creerMateriel(): void {
    const comptoir = this.comptoir();
    this.dialog
      .open(ReferentielFormDialogComponent, {
        data: {
          title: 'Nouveau matériel d’emballage',
          subtitle: 'Référentiel commun à tous les comptoirs.',
          submitLabel: 'Créer le matériel',
          fields: this.champsMateriel(),
          initial: {
            code: '',
            libelle: '',
            categorie: 'SACS',
            unite: 'SAC',
            seuilAlerte: 0,
            actif: true
          }
        },
        width: '680px',
        maxWidth: '95vw'
      })
      .afterClosed()
      .subscribe((valeurs: Record<string, unknown> | undefined) => {
        if (!valeurs) {
          return;
        }
        this.emballageService.creerMateriel(this.payloadMateriel(valeurs)).subscribe({
          next: (materiel) => {
            this.notifier(`Matériel ${materiel.libelle} créé.`);
            if (comptoir) {
              this.charger(comptoir);
            }
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  libelleCategorie(categorie: CategorieMaterielEmballage): string {
    return CATEGORIE_EMBALLAGE_LABELS[categorie] ?? categorie;
  }

  libelleUnite(unite: UniteMateriel): string {
    return UNITE_MATERIEL_LABELS[unite] ?? unite;
  }

  libelleStatut(statut: StatutFicheStockMateriel): string {
    return STATUT_FICHE_MATERIEL_LABELS[statut] ?? statut;
  }

  classeStatut(statut: StatutFicheStockMateriel): string {
    return statut === 'VALIDEE' ? 'succes' : statut === 'ANNULEE' ? 'danger' : 'alerte';
  }

  formaterDate(date: string | null): string {
    if (!date) {
      return '—';
    }
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(new Date(`${date}T00:00:00`));
  }

  private champsMateriel(): FormFieldDefinition[] {
    return [
      {
        key: 'code',
        label: 'Code',
        type: 'text',
        hint: 'Laissez vide pour générer automatiquement.'
      },
      { key: 'libelle', label: 'Libellé', type: 'text', required: true },
      {
        key: 'categorie',
        label: 'Catégorie',
        type: 'select',
        required: true,
        options: this.categories.map(([value, label]) => ({ value, label }))
      },
      {
        key: 'unite',
        label: 'Unité',
        type: 'select',
        required: true,
        options: this.unites.map(([value, label]) => ({ value, label }))
      },
      { key: 'seuilAlerte', label: 'Seuil d’alerte', type: 'number', min: 0 },
      { key: 'actif', label: 'Matériel actif', type: 'boolean' }
    ];
  }

  private payloadMateriel(valeurs: Record<string, unknown>): MaterielEmballagePayload {
    return {
      code: this.texteOuNull(valeurs['code']),
      libelle: String(valeurs['libelle']).trim(),
      categorie: String(valeurs['categorie']) as CategorieMaterielEmballage,
      unite: String(valeurs['unite']) as UniteMateriel,
      seuilAlerte: Number(valeurs['seuilAlerte'] ?? 0),
      actif: Boolean(valeurs['actif'])
    };
  }

  private texteOuNull(valeur: unknown): string | null {
    if (valeur === null || valeur === undefined) {
      return null;
    }
    const texte = String(valeur).trim();
    return texte === '' ? null : texte;
  }

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4500 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }
}
