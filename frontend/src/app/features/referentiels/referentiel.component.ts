import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Observable, forkJoin } from 'rxjs';
import {
  Campagne,
  CampagnePayload,
  Comptoir,
  ComptoirPayload,
  Magasin,
  MagasinPayload,
  OPERATEURS_MOBILE,
  Producteur,
  ProducteurPayload,
  Speculation,
  SpeculationPayload
} from '../../core/api.models';
import { ROLE_LABELS } from '../../core/auth.models';
import { AuthService } from '../../core/auth.service';
import { ReferentielService } from '../../core/referentiel.service';
import { RoleOption, Utilisateur, UtilisateurPayload } from '../../core/utilisateur.models';
import { UtilisateurService } from '../../core/utilisateur.service';
import { AffectationComptoirDialogComponent } from './affectation-comptoir-dialog.component';
import { ComptoirDetailDialogComponent } from './comptoir-detail-dialog.component';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';
import { EquipementsComptoirDialogComponent } from './equipements-comptoir-dialog.component';
import {
  FormDialogData,
  FormFieldDefinition,
  FormFieldOption,
  ReferentielFormDialogComponent
} from './referentiel-form-dialog.component';
import {
  Localite,
  PrixGarantie,
  PrixGarantiePayload,
  STATUT_PRIX_COLORS,
  STATUT_PRIX_LABELS
} from './referentiel.models';
import { ReferentielMetierService } from './referentiel.service';
import { UtilisateurDetailDialogComponent } from './utilisateur-detail-dialog.component';

class PaginatorFr extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Éléments par page';
  override nextPageLabel = 'Page suivante';
  override previousPageLabel = 'Page précédente';
  override firstPageLabel = 'Première page';
  override lastPageLabel = 'Dernière page';

  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0 || pageSize === 0) {
      return `0 sur ${length}`;
    }
    const debut = page * pageSize;
    const fin = Math.min(debut + pageSize, length);
    return `${debut + 1} – ${fin} sur ${length}`;
  };
}

@Component({
  selector: 'app-referentiel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatPaginatorModule
  ],
  templateUrl: './referentiel.component.html',
  styleUrl: './referentiel.component.scss',
  providers: [{ provide: MatPaginatorIntl, useClass: PaginatorFr }]
})
export class ReferentielComponent implements OnInit {
  private readonly referentielService = inject(ReferentielService);
  private readonly referentielMetier = inject(ReferentielMetierService);
  private readonly utilisateurService = inject(UtilisateurService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  readonly auth = inject(AuthService);
  readonly roleLabels = ROLE_LABELS;

  readonly chargement = signal(true);
  readonly chargementUtilisateurs = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly campagnes = signal<Campagne[]>([]);
  readonly comptoirs = signal<Comptoir[]>([]);
  readonly speculations = signal<Speculation[]>([]);
  readonly producteurs = signal<Producteur[]>([]);
  readonly magasins = signal<Magasin[]>([]);
  readonly localites = signal<Localite[]>([]);
  readonly prix = signal<PrixGarantie[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly roles = signal<RoleOption[]>([]);
  readonly campagnePrix = signal<number | null>(null);
  readonly rechercheProducteur = signal('');

  readonly regions = computed(() => this.localites().filter((localite) => localite.type === 'REGION'));
  readonly peutAdministrer = computed(() => this.auth.possedeUnRole('ADMIN', 'DCT'));
  readonly peutGererPrix = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'DCT', 'DFC_DIRECTEUR')
  );
  readonly peutValiderPrix = computed(() => this.auth.possedeUnRole('ADMIN', 'DCT'));
  readonly peutGererProducteurs = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'DCT', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')
  );
  readonly peutGererMagasins = computed(() => this.auth.possedeUnRole('ADMIN', 'DGSA', 'SGS'));
  readonly peutGererUtilisateurs = computed(() => this.auth.possedeUnRole('ADMIN'));
  readonly producteursFiltres = computed(() => {
    const recherche = this.rechercheProducteur().trim().toLowerCase();
    if (!recherche) {
      return this.producteurs();
    }
    return this.producteurs().filter(
      (producteur) =>
        producteur.nomPrenom.toLowerCase().includes(recherche) ||
        producteur.telephoneMobileMoney.includes(recherche) ||
        producteur.cnibNumero.toLowerCase().includes(recherche)
    );
  });

  readonly pageIndexCampagnes = signal(0);
  readonly pageSizeCampagnes = signal(5);
  readonly campagnesPaginees = computed(() => this.paginer(this.campagnes(), this.pageIndexCampagnes(), this.pageSizeCampagnes()));

  readonly pageIndexPrix = signal(0);
  readonly pageSizePrix = signal(5);
  readonly prixPaginees = computed(() => this.paginer(this.prix(), this.pageIndexPrix(), this.pageSizePrix()));

  readonly pageIndexComptoirs = signal(0);
  readonly pageSizeComptoirs = signal(5);
  readonly comptoirsPaginees = computed(() => this.paginer(this.comptoirs(), this.pageIndexComptoirs(), this.pageSizeComptoirs()));

  readonly pageIndexProducteurs = signal(0);
  readonly pageSizeProducteurs = signal(5);
  readonly producteursPaginees = computed(() => this.paginer(this.producteursFiltres(), this.pageIndexProducteurs(), this.pageSizeProducteurs()));

  readonly pageIndexMagasins = signal(0);
  readonly pageSizeMagasins = signal(5);
  readonly magasinsPaginees = computed(() => this.paginer(this.magasins(), this.pageIndexMagasins(), this.pageSizeMagasins()));

  readonly pageIndexSpeculations = signal(0);
  readonly pageSizeSpeculations = signal(5);
  readonly speculationsPaginees = computed(() => this.paginer(this.speculations(), this.pageIndexSpeculations(), this.pageSizeSpeculations()));

  readonly pageIndexUtilisateurs = signal(0);
  readonly pageSizeUtilisateurs = signal(5);
  readonly utilisateursPaginees = computed(() => this.paginer(this.utilisateurs(), this.pageIndexUtilisateurs(), this.pageSizeUtilisateurs()));

  constructor() {
    effect(() => {
      this.rechercheProducteur();
      this.pageIndexProducteurs.set(0);
    }, { allowSignalWrites: true });
  }

  paginer<T>(liste: T[], pageIndex: number, pageSize: number): T[] {
    if (!liste || liste.length === 0) {
      return [];
    }
    const maxPageIndex = Math.max(0, Math.ceil(liste.length / pageSize) - 1);
    const indexEffectif = Math.min(pageIndex, maxPageIndex);
    const debut = indexEffectif * pageSize;
    return liste.slice(debut, debut + pageSize);
  }

  changerPage(table: 'campagnes' | 'prix' | 'comptoirs' | 'producteurs' | 'magasins' | 'speculations' | 'utilisateurs', event: PageEvent): void {
    switch (table) {
      case 'campagnes':
        this.pageIndexCampagnes.set(event.pageIndex);
        this.pageSizeCampagnes.set(event.pageSize);
        break;
      case 'prix':
        this.pageIndexPrix.set(event.pageIndex);
        this.pageSizePrix.set(event.pageSize);
        break;
      case 'comptoirs':
        this.pageIndexComptoirs.set(event.pageIndex);
        this.pageSizeComptoirs.set(event.pageSize);
        break;
      case 'producteurs':
        this.pageIndexProducteurs.set(event.pageIndex);
        this.pageSizeProducteurs.set(event.pageSize);
        break;
      case 'magasins':
        this.pageIndexMagasins.set(event.pageIndex);
        this.pageSizeMagasins.set(event.pageSize);
        break;
      case 'speculations':
        this.pageIndexSpeculations.set(event.pageIndex);
        this.pageSizeSpeculations.set(event.pageSize);
        break;
      case 'utilisateurs':
        this.pageIndexUtilisateurs.set(event.pageIndex);
        this.pageSizeUtilisateurs.set(event.pageSize);
        break;
    }
  }

  ngOnInit(): void {
    this.charger();
  }

  charger(afficherChargement = true): void {
    if (afficherChargement) {
      this.chargement.set(true);
    }
    this.erreur.set(null);
    forkJoin({
      campagnes: this.referentielService.campagnes(),
      comptoirs: this.referentielService.comptoirs(),
      speculations: this.referentielService.speculations(false),
      producteurs: this.referentielService.producteurs(),
      magasins: this.referentielService.magasins(),
      // localites: this.referentielMetier.localites()
    }).subscribe({
      next: (donnees) => {
        this.campagnes.set(donnees.campagnes);
        this.comptoirs.set(donnees.comptoirs);
        this.speculations.set(donnees.speculations);
        this.producteurs.set(donnees.producteurs);
        this.magasins.set(donnees.magasins);
        // this.localites.set(
        //   [...donnees.localites].sort((a, b) => a.name.localeCompare(b.name, 'fr'))
        // );
        const campagneSelectionnee = this.campagnePrix();
        const active =
          donnees.campagnes.find((campagne) => campagne.id === campagneSelectionnee) ??
          donnees.campagnes.find((campagne) => campagne.statut === 'OUVERTE') ??
          donnees.campagnes[0];
        if (active) {
          this.campagnePrix.set(active.id);
          this.chargerPrix(active.id);
        } else {
          this.campagnePrix.set(null);
          this.prix.set([]);
        }
        this.chargement.set(false);
        if (this.peutGererUtilisateurs()) {
          this.chargerUtilisateurs();
        }
      },
      error: () => {
        this.erreur.set('Impossible de charger les données de paramétrage.');
        this.chargement.set(false);
      }
    });
  }

  chargerPrix(campagneId: number): void {
    this.campagnePrix.set(campagneId);
    this.pageIndexPrix.set(0);
    this.referentielMetier.prixGarantis(campagneId).subscribe({
      next: (prix) => this.prix.set(prix),
      error: () => this.erreur.set('Impossible de charger les prix garantis.')
    });
  }

  chargerUtilisateurs(apresChargement?: () => void): void {
    if (!this.peutGererUtilisateurs()) {
      return;
    }
    this.chargementUtilisateurs.set(true);
    forkJoin({
      utilisateurs: this.utilisateurService.lister(),
      roles: this.utilisateurService.roles()
    }).subscribe({
      next: (donnees) => {
        this.utilisateurs.set(donnees.utilisateurs);
        this.roles.set(donnees.roles);
        this.chargementUtilisateurs.set(false);
        apresChargement?.();
      },
      error: () => {
        this.chargementUtilisateurs.set(false);
        this.notifier('Impossible de charger les utilisateurs.');
      }
    });
  }

  // ---------------------------------------------------------------- Campagnes

  creerCampagne(): void {
    this.ouvrirFormulaire(
      {
        title: 'Nouvelle campagne',
        subtitle: 'Définissez la période et l’objectif global de la campagne.',
        submitLabel: 'Créer la campagne',
        fields: this.champsCampagne(),
        initial: {
          code: '',
          libelle: '',
          dateDebut: this.aujourdhui(),
          dateFin: this.dansSixMois(),
          objectifTotalTonnes: 0,
          statut: 'PLANIFIEE'
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.creerCampagne(this.payloadCampagne(valeurs)),
          'Campagne créée.'
        );
      }
    );
  }

  modifierCampagne(campagne: Campagne): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier la campagne',
        subtitle: campagne.code,
        fields: this.champsCampagne(),
        initial: {
          code: campagne.code,
          libelle: campagne.libelle,
          dateDebut: campagne.dateDebut,
          dateFin: campagne.dateFin,
          objectifTotalTonnes: campagne.objectifTotalTonnes,
          statut: campagne.statut
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.modifierCampagne(campagne.id, this.payloadCampagne(valeurs)),
          'Campagne modifiée.'
        );
      }
    );
  }

  supprimerCampagne(campagne: Campagne): void {
    this.confirmer(
      `Supprimer la campagne ${campagne.code} ? Cette action est impossible si elle est ouverte ou déjà utilisée.`,
      () =>
        this.executer(
          this.referentielService.supprimerCampagne(campagne.id),
          'Campagne supprimée.'
        )
    );
  }

  // -------------------------------------------------------------- Spéculations

  creerSpeculation(): void {
    this.ouvrirFormulaire(
      {
        title: 'Nouvelle spéculation',
        submitLabel: 'Créer la spéculation',
        fields: this.champsSpeculation(),
        initial: {
          code: '',
          nom: '',
          seuilHumiditeMax: 12,
          seuilImpureteMax: 2,
          active: true
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.creerSpeculation(this.payloadSpeculation(valeurs)),
          'Spéculation créée.'
        );
      }
    );
  }

  modifierSpeculation(speculation: Speculation): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier la spéculation',
        subtitle: speculation.code,
        fields: this.champsSpeculation(),
        initial: {
          code: speculation.code,
          nom: speculation.nom,
          seuilHumiditeMax: speculation.seuilHumiditeMax,
          seuilImpureteMax: speculation.seuilImpureteMax,
          active: speculation.active
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.modifierSpeculation(
            speculation.id,
            this.payloadSpeculation(valeurs)
          ),
          'Spéculation modifiée.'
        );
      }
    );
  }

  supprimerSpeculation(speculation: Speculation): void {
    this.confirmer(
      `Supprimer la spéculation ${speculation.code} ? Elle ne doit être utilisée par aucun prix, objectif ou collecte.`,
      () =>
        this.executer(
          this.referentielService.supprimerSpeculation(speculation.id),
          'Spéculation supprimée.'
        )
    );
  }

  // ------------------------------------------------------------------ Comptoirs

  creerComptoir(): void {
    this.ouvrirFormulaire(
      {
        title: 'Nouveau comptoir',
        submitLabel: 'Créer le comptoir',
        fields: this.champsComptoir(),
        initial: {
          code: '',
          nom: '',
          type: 'FIXE',
          capaciteStockageKg: 50000,
          localityId: null,
          latitudeGps: null,
          longitudeGps: null,
          actif: true
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.creerComptoir(this.payloadComptoir(valeurs)),
          'Comptoir créé.'
        );
      }
    );
  }

  ouvrirDetail(comptoir: Comptoir): void {
    this.dialog.open(ComptoirDetailDialogComponent, {
      data: { comptoir },
      width: '560px',
      maxWidth: '96vw'
    });
  }

  ouvrirAffectation(comptoir: Comptoir): void {
    if (this.utilisateurs().length === 0) {
      if (this.chargementUtilisateurs()) {
        this.notifier('Chargement des utilisateurs en cours, veuillez réessayer.');
        return;
      }
      this.chargerUtilisateurs(() => this.afficherAffectation(comptoir));
      return;
    }
    this.afficherAffectation(comptoir);
  }

  ouvrirEquipements(comptoir: Comptoir): void {
    const afficher = () =>
      this.dialog.open(EquipementsComptoirDialogComponent, {
        data: { comptoir, utilisateurs: this.utilisateurs() },
        width: '880px',
        maxWidth: '97vw',
        maxHeight: '94vh'
      });
    if (this.peutGererUtilisateurs() && this.utilisateurs().length === 0) {
      this.chargerUtilisateurs(afficher);
      return;
    }
    afficher();
  }

  private afficherAffectation(comptoir: Comptoir): void {
    this.dialog
      .open(AffectationComptoirDialogComponent, {
        data: { comptoir, utilisateurs: this.utilisateurs() },
        width: '720px',
        maxWidth: '96vw',
        maxHeight: '92vh'
      })
      .afterClosed()
      .subscribe((ids: number[] | undefined) => {
        if (!ids || ids.length === 0) {
          return;
        }
        this.utilisateurService.affecterAuComptoir(comptoir.id, ids).subscribe({
          next: (resultat) => {
            this.notifier(
              `${resultat.affectes} utilisateur(s) affecté(s) au comptoir ${resultat.comptoirCode}.`
            );
            this.chargerUtilisateurs();
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  modifierComptoir(comptoir: Comptoir): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier le comptoir',
        subtitle: comptoir.code,
        fields: this.champsComptoir(),
        initial: {
          code: comptoir.code,
          nom: comptoir.nom,
          type: comptoir.type,
          capaciteStockageKg: comptoir.capaciteStockageKg ?? 0,
          localityId: comptoir.localityId,
          latitudeGps: comptoir.latitudeGps,
          longitudeGps: comptoir.longitudeGps,
          actif: comptoir.actif
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.modifierComptoir(comptoir.id, this.payloadComptoir(valeurs)),
          'Comptoir modifié.'
        );
      }
    );
  }

  supprimerComptoir(comptoir: Comptoir): void {
    this.confirmer(
      `Supprimer le comptoir ${comptoir.code} ? Cette action est impossible s’il est déjà utilisé.`,
      () =>
        this.executer(
          this.referentielService.supprimerComptoir(comptoir.id),
          'Comptoir supprimé.'
        )
    );
  }

  basculerComptoir(comptoir: Comptoir): void {
    this.executer(
      this.referentielMetier.activerComptoir(comptoir.id, !comptoir.actif),
      comptoir.actif ? 'Comptoir fermé.' : 'Comptoir ouvert.'
    );
  }

  // ---------------------------------------------------------------- Producteurs

  creerProducteur(): void {
    this.ouvrirFormulaire(
      {
        title: 'Nouveau producteur',
        submitLabel: 'Enregistrer le producteur',
        fields: this.champsProducteur(),
        initial: {
          nomPrenom: '',
          telephoneMobileMoney: '',
          operateurMobile: 'Orange',
          cnibNumero: '',
          cnibDateDelivrance: null,
          cnibLieuDelivrance: '',
          localityId: '',
          adressePrecision: ''
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.creerProducteur(this.payloadProducteur(valeurs)),
          'Producteur enregistré.'
        );
      }
    );
  }

  modifierProducteur(producteur: Producteur): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier le producteur',
        subtitle: producteur.nomPrenom,
        fields: this.champsProducteur(),
        initial: {
          nomPrenom: producteur.nomPrenom,
          telephoneMobileMoney: producteur.telephoneMobileMoney,
          operateurMobile: producteur.operateurMobile,
          cnibNumero: producteur.cnibNumero,
          cnibDateDelivrance: producteur.cnibDateDelivrance,
          cnibLieuDelivrance: producteur.cnibLieuDelivrance ?? '',
          localityId: producteur.localityId ?? '',
          adressePrecision: producteur.adressePrecision ?? ''
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.modifierProducteur(
            producteur.id,
            this.payloadProducteur(valeurs)
          ),
          'Producteur modifié.'
        );
      }
    );
  }

  supprimerProducteur(producteur: Producteur): void {
    this.confirmer(
      `Supprimer le producteur ${producteur.nomPrenom} ? Cette action est impossible s’il a des collectes.`,
      () =>
        this.executer(
          this.referentielService.supprimerProducteur(producteur.id),
          'Producteur supprimé.'
        )
    );
  }

  // ------------------------------------------------------------------- Magasins

  creerMagasin(): void {
    this.ouvrirFormulaire(
      {
        title: 'Nouveau magasin central',
        submitLabel: 'Créer le magasin',
        fields: this.champsMagasin(),
        initial: {
          codeMagasin: '',
          nom: '',
          capaciteTotaleTonnes: 50000,
          localityId: '',
          actif: true
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.creerMagasin(this.payloadMagasin(valeurs)),
          'Magasin créé.'
        );
      }
    );
  }

  modifierMagasin(magasin: Magasin): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier le magasin central',
        subtitle: magasin.codeMagasin,
        fields: this.champsMagasin(),
        initial: {
          codeMagasin: magasin.codeMagasin,
          nom: magasin.nom,
          capaciteTotaleTonnes: magasin.capaciteTotaleTonnes,
          localityId:
            magasin.localityId ?? this.localiteCorrespondantVille(magasin.ville),
          actif: magasin.actif
        }
      },
      (valeurs) => {
        this.executer(
          this.referentielService.modifierMagasin(magasin.id, this.payloadMagasin(valeurs)),
          'Magasin modifié.'
        );
      }
    );
  }

  supprimerMagasin(magasin: Magasin): void {
    this.confirmer(
      `Supprimer le magasin ${magasin.codeMagasin} ? Cette action est impossible s’il contient du stock.`,
      () =>
        this.executer(
          this.referentielService.supprimerMagasin(magasin.id),
          'Magasin supprimé.'
        )
    );
  }

  // ---------------------------------------------------------------- Prix garantis

  creerPrix(): void {
    if (this.campagnes().length === 0 || this.speculations().length === 0) {
      this.notifier('Créez au moins une campagne et une spéculation avant de saisir un prix.');
      return;
    }
    this.ouvrirFormulaire(
      {
        title: 'Nouveau prix garanti',
        subtitle: 'Le prix sera soumis à approbation avant application.',
        submitLabel: 'Soumettre le prix',
        fields: this.champsPrix(),
        initial: {
          campagneId: this.campagnePrix() ?? this.campagnes()[0].id,
          speculationId: this.speculations()[0].id,
          prixUnitaireKg: 0,
          dateEffet: this.aujourdhui(),
          localityId: ''
        }
      },
      (valeurs) => {
        const payload = this.payloadPrix(valeurs);
        this.referentielMetier.creerPrix(payload).subscribe({
          next: () => {
            this.campagnePrix.set(payload.campagneId);
            this.notifier('Prix garanti soumis à approbation.');
            this.charger(false);
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      }
    );
  }

  modifierPrix(prix: PrixGarantie): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier le prix garanti',
        subtitle: `${prix.speculationNom} — ${prix.campagneCode}`,
        fields: this.champsPrix(),
        initial: {
          campagneId: prix.campagneId,
          speculationId: prix.speculationId,
          prixUnitaireKg: prix.prixUnitaireKg,
          dateEffet: prix.dateEffet,
          localityId: prix.localityId ?? ''
        }
      },
      (valeurs) => {
        const payload = this.payloadPrix(valeurs);
        this.referentielMetier.modifierPrix(prix.id, payload).subscribe({
          next: () => {
            this.campagnePrix.set(payload.campagneId);
            this.notifier('Prix garanti modifié et soumis à approbation.');
            this.charger(false);
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      }
    );
  }

  approuver(prix: PrixGarantie): void {
    this.executer(this.referentielMetier.approuverPrix(prix.id), 'Prix garanti approuvé.');
  }

  rejeterPrix(prix: PrixGarantie): void {
    this.confirmer(
      `Désactiver le prix de ${prix.speculationNom} ?`,
      () => this.executer(this.referentielMetier.rejeterPrix(prix.id), 'Prix garanti désactivé.'),
      'Désactiver',
      false
    );
  }

  supprimerPrix(prix: PrixGarantie): void {
    this.confirmer(
      `Supprimer le prix de ${prix.speculationNom} ? Un prix approuvé doit d’abord être désactivé.`,
      () => this.executer(this.referentielMetier.supprimerPrix(prix.id), 'Prix garanti supprimé.')
    );
  }

  // ----------------------------------------------------------------- Utilisateurs

  ouvrirDetailUtilisateur(utilisateur: Utilisateur): void {
    this.dialog.open(UtilisateurDetailDialogComponent, {
      data: { utilisateur },
      width: '560px',
      maxWidth: '96vw'
    });
  }

  creerUtilisateur(): void {
    if (this.roles().length === 0) {
      this.notifier('Les rôles ne sont pas encore chargés.');
      return;
    }
    this.ouvrirFormulaire(
      {
        title: 'Nouvel utilisateur',
        submitLabel: 'Créer l’utilisateur',
        fields: this.champsUtilisateur(true),
        initial: {
          matricule: '',
          nom: '',
          prenom: '',
          email: '',
          telephone: '',
          motDePasse: '',
          roles: [],
          actif: true,
          comptoirId: null
        }
      },
      (valeurs) => {
        this.executer(
          this.utilisateurService.creer(this.payloadUtilisateur(valeurs)),
          'Utilisateur créé.'
        );
      }
    );
  }

  modifierUtilisateur(utilisateur: Utilisateur): void {
    this.ouvrirFormulaire(
      {
        title: 'Modifier l’utilisateur',
        subtitle: `${utilisateur.matricule} — ${utilisateur.nomComplet}`,
        fields: this.champsUtilisateur(false),
        initial: {
          matricule: utilisateur.matricule,
          nom: utilisateur.nom,
          prenom: utilisateur.prenom,
          email: utilisateur.email,
          telephone: utilisateur.telephone,
          motDePasse: '',
          roles: [...utilisateur.roles],
          actif: utilisateur.actif,
          comptoirId: utilisateur.comptoirId
        }
      },
      (valeurs) => {
        this.executer(
          this.utilisateurService.modifier(utilisateur.id, this.payloadUtilisateur(valeurs)),
          'Utilisateur modifié.'
        );
      }
    );
  }

  basculerUtilisateur(utilisateur: Utilisateur): void {
    if (utilisateur.actif) {
      this.confirmer(
        `Désactiver le compte de ${utilisateur.nomComplet} ?`,
        () =>
          this.executer(
            this.utilisateurService.changerActivation(utilisateur.id, false),
            'Utilisateur désactivé.'
          ),
        'Désactiver',
        false
      );
      return;
    }
    this.executer(
      this.utilisateurService.changerActivation(utilisateur.id, true),
      'Utilisateur activé.'
    );
  }

  supprimerUtilisateur(utilisateur: Utilisateur): void {
    this.confirmer(
      `Supprimer l’utilisateur ${utilisateur.nomComplet} ? Son compte sera désactivé et ses sessions révoquées.`,
      () =>
        this.executer(
          this.utilisateurService.supprimer(utilisateur.id),
          'Utilisateur désactivé.'
        )
    );
  }

  // --------------------------------------------------------------------- Outils

  libelleStatutPrix(statut: string): string {
    return STATUT_PRIX_LABELS[statut] ?? statut;
  }

  couleurStatutPrix(statut: string): string {
    return STATUT_PRIX_COLORS[statut] ?? 'neutre';
  }

  libelleRole(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }

  formaterMontant(valeur: number): string {
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} CFA`;
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

  private champsCampagne(): FormFieldDefinition[] {
    return [
      { key: 'code', label: 'Code', type: 'text', required: true, placeholder: 'CAMP-2026' },
      { key: 'libelle', label: 'Libellé', type: 'text', required: true },
      { key: 'dateDebut', label: 'Date de début', type: 'date', required: true },
      { key: 'dateFin', label: 'Date de fin', type: 'date', required: true },
      {
        key: 'objectifTotalTonnes',
        label: 'Objectif total (tonnes)',
        type: 'number',
        required: true,
        min: 0,
        step: 0.01
      },
      {
        key: 'statut',
        label: 'Statut',
        type: 'select',
        required: true,
        options: [
          { value: 'PLANIFIEE', label: 'Planifiée' },
          { value: 'OUVERTE', label: 'Ouverte' },
          { value: 'CLOTUREE', label: 'Clôturée' }
        ]
      }
    ];
  }

  private champsSpeculation(): FormFieldDefinition[] {
    return [
      { key: 'code', label: 'Code', type: 'text', required: true, placeholder: 'MAIS' },
      { key: 'nom', label: 'Nom', type: 'text', required: true },
      {
        key: 'seuilHumiditeMax',
        label: 'Humidité maximale (%)',
        type: 'number',
        required: true,
        min: 0,
        max: 100,
        step: 0.01
      },
      {
        key: 'seuilImpureteMax',
        label: 'Impuretés maximales (%)',
        type: 'number',
        required: true,
        min: 0,
        max: 100,
        step: 0.01
      },
      { key: 'active', label: 'Spéculation active', type: 'boolean' }
    ];
  }

  private champsComptoir(): FormFieldDefinition[] {
    return [
      { key: 'code', label: 'Code', type: 'text', required: true, placeholder: 'CPT-OUA-03' },
      { key: 'nom', label: 'Nom', type: 'text', required: true },
      {
        key: 'type',
        label: 'Type',
        type: 'select',
        required: true,
        options: [
          { value: 'FIXE', label: 'Fixe' },
          { value: 'MOBILE', label: 'Mobile' }
        ]
      },
      { key: 'capaciteStockageKg', label: 'Capacité (kg)', type: 'number', required: true, min: 0 },
      {
        key: 'localityId',
        label: 'Localité',
        type: 'locality',
        required: true,
        hint: 'Région, province, commune puis secteur ou village.'
      },
      { key: 'latitudeGps', label: 'Latitude GPS', type: 'number', step: 'any' },
      { key: 'longitudeGps', label: 'Longitude GPS', type: 'number', step: 'any' },
      { key: 'actif', label: 'Comptoir ouvert', type: 'boolean' }
    ];
  }

  private champsProducteur(): FormFieldDefinition[] {
    return [
      { key: 'nomPrenom', label: 'Nom et prénom', type: 'text', required: true },
      {
        key: 'telephoneMobileMoney',
        label: 'Téléphone Mobile Money',
        type: 'tel',
        required: true,
        placeholder: '+22676000000'
      },
      {
        key: 'operateurMobile',
        label: 'Opérateur Mobile Money',
        type: 'select',
        required: true,
        options: OPERATEURS_MOBILE
      },
      { key: 'cnibNumero', label: 'Numéro CNIB', type: 'text', required: true },
      { key: 'cnibDateDelivrance', label: 'Date de délivrance CNIB', type: 'date' },
      { key: 'cnibLieuDelivrance', label: 'Lieu de délivrance CNIB', type: 'text' },
      {
        key: 'localityId',
        label: 'Localité',
        type: 'locality',
        emptyLabel: 'Aucune / Non renseignée',
        hint: 'Facultatif : sélectionnez au moins la commune.'
      },
      { key: 'adressePrecision', label: 'Adresse / précision', type: 'textarea' }
    ];
  }

  private champsMagasin(): FormFieldDefinition[] {
    return [
      { key: 'codeMagasin', label: 'Code magasin', type: 'text', required: true },
      { key: 'nom', label: 'Nom', type: 'text', required: true },
      {
        key: 'capaciteTotaleTonnes',
        label: 'Capacité totale (tonnes)',
        type: 'number',
        required: true,
        min: 0,
        step: 0.01
      },
      {
        key: 'localityId',
        label: 'Localité',
        type: 'locality',
        required: true,
        hint: 'La ville affichée sera la commune sélectionnée.'
      },
      { key: 'actif', label: 'Magasin actif', type: 'boolean' }
    ];
  }

  private champsPrix(): FormFieldDefinition[] {
    return [
      {
        key: 'campagneId',
        label: 'Campagne',
        type: 'select',
        required: true,
        options: this.campagnes().map((campagne) => ({
          value: campagne.id,
          label: `${campagne.code} — ${campagne.libelle}`
        }))
      },
      {
        key: 'speculationId',
        label: 'Spéculation',
        type: 'select',
        required: true,
        options: this.speculations().map((speculation) => ({
          value: speculation.id,
          label: `${speculation.code} — ${speculation.nom}`
        }))
      },
      {
        key: 'prixUnitaireKg',
        label: 'Prix unitaire (CFA/kg)',
        type: 'number',
        required: true,
        min: 1,
        step: 0.01
      },
      {
        key: 'dateEffet',
        label: 'Date d’effet',
        type: 'date',
        required: true,
        min: this.aujourdhui()
      },
      {
        key: 'localityId',
        label: 'Localité',
        type: 'locality',
        hint: 'Sans localité, le prix s’applique au niveau national.'
      }
    ];
  }

  private champsUtilisateur(creation: boolean): FormFieldDefinition[] {
    return [
      { key: 'matricule', label: 'Matricule', type: 'text', required: true },
      { key: 'nom', label: 'Nom', type: 'text', required: true },
      { key: 'prenom', label: 'Prénom', type: 'text', required: true },
      { key: 'email', label: 'Adresse e-mail', type: 'email', required: true },
      { key: 'telephone', label: 'Téléphone', type: 'tel', required: true },
      {
        key: 'motDePasse',
        label: 'Mot de passe',
        type: 'password',
        required: creation,
        hint: creation
          ? 'Au moins 8 caractères.'
          : 'Laisser vide pour conserver le mot de passe actuel.'
      },
      {
        key: 'roles',
        label: 'Rôles',
        type: 'select',
        required: true,
        multiple: true,
        options: this.optionsRoles()
      },
      {
        key: 'comptoirId',
        label: 'Comptoir de rattachement',
        type: 'select',
        options: this.optionsComptoirs(),
        hint: 'Obligatoire pour un agent collecteur ou un chef de comptoir.'
      },
      { key: 'actif', label: 'Compte actif', type: 'boolean' }
    ];
  }

  private optionsComptoirs(): FormFieldOption[] {
    return [
      { value: null, label: 'Aucun (profil non terrain)' },
      ...this.comptoirs().map((comptoir) => ({
        value: comptoir.id,
        label: `${comptoir.code} — ${comptoir.nom}${comptoir.actif ? '' : ' (fermé)'}`
      }))
    ];
  }

  private optionsRoles(): FormFieldOption[] {
    return this.roles().map((role) => ({
      value: role.code,
      label: role.libelle ? `${role.libelle} (${role.code})` : this.libelleRole(role.code)
    }));
  }

  private payloadCampagne(valeurs: Record<string, unknown>): CampagnePayload {
    return {
      code: this.texte(valeurs['code']).toUpperCase(),
      libelle: this.texte(valeurs['libelle']),
      dateDebut: this.texte(valeurs['dateDebut']),
      dateFin: this.texte(valeurs['dateFin']),
      objectifTotalTonnes: this.nombre(valeurs['objectifTotalTonnes']),
      statut: this.texte(valeurs['statut']) || 'PLANIFIEE'
    };
  }

  private payloadSpeculation(valeurs: Record<string, unknown>): SpeculationPayload {
    return {
      code: this.texte(valeurs['code']).toUpperCase(),
      nom: this.texte(valeurs['nom']),
      seuilHumiditeMax: this.nombre(valeurs['seuilHumiditeMax']),
      seuilImpureteMax: this.nombre(valeurs['seuilImpureteMax']),
      active: this.booleen(valeurs['active'])
    };
  }

  private payloadComptoir(valeurs: Record<string, unknown>): ComptoirPayload {
    return {
      code: this.texte(valeurs['code']).toUpperCase(),
      nom: this.texte(valeurs['nom']),
      type: this.texte(valeurs['type']) || 'FIXE',
      capaciteStockageKg: this.nombre(valeurs['capaciteStockageKg']),
      localityId: this.texte(valeurs['localityId']),
      latitudeGps: this.nombreOuNull(valeurs['latitudeGps']),
      longitudeGps: this.nombreOuNull(valeurs['longitudeGps']),
      actif: this.booleen(valeurs['actif'])
    };
  }

  private payloadProducteur(valeurs: Record<string, unknown>): ProducteurPayload {
    return {
      nomPrenom: this.texte(valeurs['nomPrenom']),
      telephoneMobileMoney: this.texte(valeurs['telephoneMobileMoney']),
      operateurMobile: this.texte(valeurs['operateurMobile']),
      cnibNumero: this.texte(valeurs['cnibNumero']),
      cnibDateDelivrance: this.texteOuNull(valeurs['cnibDateDelivrance']),
      cnibLieuDelivrance: this.texteOuNull(valeurs['cnibLieuDelivrance']),
      adressePrecision: this.texteOuNull(valeurs['adressePrecision']),
      localityId: this.texteOuNull(valeurs['localityId'])
    };
  }

  private payloadMagasin(valeurs: Record<string, unknown>): MagasinPayload {
    const localityId = this.texteOuNull(valeurs['localityId']);
    return {
      codeMagasin: this.texte(valeurs['codeMagasin']).toUpperCase(),
      nom: this.texte(valeurs['nom']),
      capaciteTotaleTonnes: this.nombre(valeurs['capaciteTotaleTonnes']),
      ville: this.nomVilleLocalite(localityId),
      localityId,
      actif: this.booleen(valeurs['actif'])
    };
  }

  private nomVilleLocalite(localityId: string | null): string {
    if (!localityId) {
      return '';
    }
    const initiale = this.localites().find((localite) => localite.id === localityId);
    let courante = initiale;
    while (courante) {
      if (courante.type === 'COMMUNE') {
        return courante.name;
      }
      courante = this.localites().find((localite) => localite.id === courante?.parentId);
    }
    return initiale?.name ?? '';
  }

  private localiteCorrespondantVille(ville: string): string {
    const recherche = ville.trim().toLowerCase();
    return (
      this.localites().find(
        (localite) =>
          localite.type === 'COMMUNE' && localite.name.trim().toLowerCase() === recherche
      )?.id ?? ''
    );
  }

  private payloadPrix(valeurs: Record<string, unknown>): PrixGarantiePayload {
    return {
      campagneId: this.nombre(valeurs['campagneId']),
      speculationId: this.nombre(valeurs['speculationId']),
      prixUnitaireKg: this.nombre(valeurs['prixUnitaireKg']),
      dateEffet: this.texte(valeurs['dateEffet']),
      localityId: this.texteOuNull(valeurs['localityId'])
    };
  }

  private payloadUtilisateur(valeurs: Record<string, unknown>): UtilisateurPayload {
    return {
      matricule: this.texte(valeurs['matricule']).toUpperCase(),
      nom: this.texte(valeurs['nom']),
      prenom: this.texte(valeurs['prenom']),
      email: this.texte(valeurs['email']),
      telephone: this.texte(valeurs['telephone']),
      motDePasse: this.texteOuNull(valeurs['motDePasse']),
      roles: (valeurs['roles'] as string[] | null) ?? [],
      actif: this.booleen(valeurs['actif']),
      comptoirId: this.nombreOuNull(valeurs['comptoirId'])
    };
  }

  private ouvrirFormulaire(
    data: FormDialogData,
    action: (valeurs: Record<string, unknown>) => void
  ): void {
    this.dialog
      .open(ReferentielFormDialogComponent, {
        data: { ...data, localites: data.localites ?? this.localites() },
        width: '760px',
        maxWidth: '95vw',
        autoFocus: 'first-tabbable'
      })
      .afterClosed()
      .subscribe((valeurs: Record<string, unknown> | undefined) => {
        if (valeurs) {
          action(valeurs);
        }
      });
  }

  private confirmer(
    message: string,
    action: () => void,
    actionLabel = 'Supprimer',
    danger = true
  ): void {
    this.dialog
      .open(ConfirmationDialogComponent, {
        data: { titre: 'Confirmation', message, actionLabel, danger },
        width: '480px',
        maxWidth: '95vw'
      })
      .afterClosed()
      .subscribe((confirme: boolean | undefined) => {
        if (confirme) {
          action();
        }
      });
  }

  private executer(requete: Observable<unknown>, message: string): void {
    requete.subscribe({
      next: () => {
        this.notifier(message);
        this.charger(false);
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  private texte(valeur: unknown): string {
    return valeur === null || valeur === undefined ? '' : String(valeur).trim();
  }

  private texteOuNull(valeur: unknown): string | null {
    const texte = this.texte(valeur);
    return texte === '' ? null : texte;
  }

  private nombre(valeur: unknown): number {
    const nombre = Number(valeur);
    return Number.isFinite(nombre) ? nombre : 0;
  }

  private nombreOuNull(valeur: unknown): number | null {
    if (valeur === null || valeur === undefined || valeur === '') {
      return null;
    }
    const nombre = Number(valeur);
    return Number.isFinite(nombre) ? nombre : null;
  }

  private booleen(valeur: unknown): boolean {
    return valeur === true || valeur === 'true';
  }

  private aujourdhui(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private dansSixMois(): string {
    const date = new Date();
    date.setMonth(date.getMonth() + 6);
    return date.toISOString().slice(0, 10);
  }

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4500 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }
}
