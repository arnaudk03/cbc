import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { PrixGarantie } from '../referentiels/referentiel.models';
import { AuthService } from '../../core/auth.service';
import {
  Campagne,
  Comptoir,
  OPERATEURS_MOBILE,
  Producteur,
  ProducteurPayload,
  Speculation
} from '../../core/api.models';
import { ReferentielService } from '../../core/referentiel.service';
import {
  FormFieldDefinition,
  ReferentielFormDialogComponent
} from '../referentiels/referentiel-form-dialog.component';
import { Localite } from '../referentiels/referentiel.models';
import { ReferentielMetierService } from '../referentiels/referentiel.service';
import {
  Collecte,
  CollecteStatut,
  CreateCollecteLotRequest,
  STATUT_COLLECTE_COLORS,
  STATUT_COLLECTE_LABELS
} from './collecte.models';
import { CollecteService } from './collecte.service';

export interface LignePeseeForm {
  speculationId: number;
  nombreSacs: number;
  poidsBrutTotalKg: number;
  tareEmballageKg: number;
  tauxHumidite: number;
  tauxImpuretes: number;
  grainsAvaries: number;
  conforme: boolean;
  motifRejet: string;
}

@Component({
  selector: 'app-collecte',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './collecte.component.html',
  styleUrl: './collecte.component.scss'
})
export class CollecteComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly collecteService = inject(CollecteService);
  private readonly referentielService = inject(ReferentielService);
  private readonly referentielMetier = inject(ReferentielMetierService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  readonly auth = inject(AuthService);

  readonly chargement = signal(true);
  readonly enregistrement = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly formulaireVisible = signal(false);
  readonly collectes = signal<Collecte[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly collecteOuverte = signal<number | null>(null);
  readonly brbcVisualise = signal<Collecte | null>(null);
  readonly brbcLignes = signal<Collecte[]>([]);

  readonly statutFiltre = signal<CollecteStatut | ''>('');
  readonly campagnes = signal<Campagne[]>([]);
  readonly comptoirs = signal<Comptoir[]>([]);
  readonly speculations = signal<Speculation[]>([]);
  readonly producteurs = signal<Producteur[]>([]);
  readonly localites = signal<Localite[]>([]);
  readonly prixGarantis = signal<PrixGarantie[]>([]);

  readonly peutCreer = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly peutGererProducteurs = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'DCT', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly peutExaminer = computed(() => this.auth.possedeUnRole('ADMIN', 'SCP'));
  readonly peutValider = computed(() => this.auth.possedeUnRole('ADMIN', 'DCT'));
  readonly profilTerrain = computed(() =>
    this.auth.possedeUnRole('AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
  readonly comptoirAffecte = computed(() => {
    const utilisateur = this.auth.user();
    if (!utilisateur?.comptoirId || !this.profilTerrain()) {
      return null;
    }
    return utilisateur;
  });
  readonly sansComptoir = computed(() => this.profilTerrain() && !this.comptoirAffecte());
  readonly estExaminateur = (collecte: Collecte): boolean =>
    collecte.examinateurId !== null && this.auth.user()?.id === collecte.examinateurId;
  readonly peutValiderCollecte = (collecte: Collecte): boolean =>
    this.peutValider() && !this.estExaminateur(collecte);
  readonly statuts: CollecteStatut[] = [
    'SYNCHRONISEE',
    'EXAMINEE_SCP',
    'VALIDEE_DCT',
    'REJETEE'
  ];

  readonly formulaire = this.fb.nonNullable.group({
    campagneId: [0, [Validators.required, Validators.min(1)]],
    comptoirId: [0, [Validators.required, Validators.min(1)]],
    producteurId: [0, [Validators.required, Validators.min(1)]],
    modePaiement: ['MOBILE_MONEY', Validators.required],
    observationsReceptionnaire: ['']
  });

  readonly lignesPesee = signal<LignePeseeForm[]>([]);

  private readonly producteurIdSignal = toSignal(
    this.formulaire.controls.producteurId.valueChanges,
    { initialValue: this.formulaire.controls.producteurId.value }
  );

  readonly comptoirIdSignal = toSignal(
    this.formulaire.controls.comptoirId.valueChanges,
    { initialValue: this.formulaire.controls.comptoirId.value }
  );

  readonly producteurSelectionne = computed(() => {
    const id = this.producteurIdSignal();
    return this.producteurs().find((p) => p.id === id) ?? null;
  });

  readonly comptoirSelectionne = computed(() => {
    const id = this.comptoirIdSignal();
    return this.comptoirs().find((c) => c.id === id) ?? null;
  });

  readonly totalSacsLignes = computed(() =>
    this.lignesPesee().reduce((somme, l) => somme + (Number(l.nombreSacs) || 0), 0)
  );

  readonly totalPoidsNetLignes = computed(() =>
    Math.round(
      this.lignesPesee().reduce(
        (somme, l) => somme + Math.max(0, (Number(l.poidsBrutTotalKg) || 0) - (Number(l.tareEmballageKg) || 0)),
        0
      ) * 100
    ) / 100
  );

  readonly totalMontantLignes = computed(() =>
    this.lignesPesee().reduce(
      (somme, l) => somme + Math.round(this.poidsNetLigne(l) * this.prixUnitaireLigne(l)),
      0
    )
  );

  readonly totalMontantEnLettres = computed(() =>
    this.nombreEnLettres(this.totalMontantLignes())
  );

  readonly nbLignesNonConformes = computed(() =>
    this.lignesPesee().filter(
      (l) => !l.conforme || this.qualiteAlerteLigne(l)
    ).length
  );

  readonly brbcTotalPoidsNet = computed(() => {
    const bordereau = this.brbcVisualise()?.bordereau;
    if (bordereau) {
      return bordereau.quantiteKg;
    }
    return Math.round(
      this.brbcLignes().reduce((somme, c) => somme + (c.quantiteKg ?? 0), 0) * 100
    ) / 100;
  });

  readonly brbcTotalMontant = computed(() => {
    const bordereau = this.brbcVisualise()?.bordereau;
    if (bordereau) {
      return bordereau.montantNetPayerCfa;
    }
    return this.brbcLignes().reduce((somme, c) => somme + (c.montantNetCfa ?? 0), 0);
  });

  readonly brbcTotalSacs = computed(() =>
    this.brbcLignes().reduce(
      (somme, c) => somme + (c.fichePesee?.nombreSacs ?? 0),
      0
    )
  );

  readonly brbcTotalEnLettres = computed(() =>
    this.nombreEnLettres(this.brbcTotalMontant())
  );

  brbcToutesConformes(): boolean {
    return this.brbcLignes().every((l) => l.controleQualite?.conforme !== false);
  }

  ngOnInit(): void {
    this.chargerReferentiels();
    this.charger();
  }

  chargerReferentiels(): void {
    forkJoin({
      campagnes: this.referentielService.campagnes(),
      comptoirs: this.referentielService.comptoirs(),
      speculations: this.referentielService.speculations(),
      producteurs: this.referentielService.producteurs(),
      localites: this.referentielMetier.localites()
    }).subscribe({
      next: (donnees) => {
        this.campagnes.set(donnees.campagnes);
        this.comptoirs.set(donnees.comptoirs);
        this.speculations.set(donnees.speculations);
        this.producteurs.set(donnees.producteurs);
        this.localites.set(
          [...donnees.localites].sort((a, b) => a.name.localeCompare(b.name, 'fr'))
        );
        const campagne =
          donnees.campagnes.find((element) => element.statut === 'OUVERTE') ?? donnees.campagnes[0];
        const affectation = this.comptoirAffecte();
        const comptoir = affectation?.comptoirId
          ? donnees.comptoirs.find((element) => element.id === affectation.comptoirId)
          : this.profilTerrain()
            ? undefined
            : donnees.comptoirs.find((element) => element.actif) ?? donnees.comptoirs[0];
        const speculation =
          donnees.speculations.find((element) => element.active) ?? donnees.speculations[0];
        this.formulaire.patchValue({
          campagneId: campagne?.id ?? 0,
          comptoirId: comptoir?.id ?? 0,
          producteurId: donnees.producteurs[0]?.id ?? 0
        });
        if (affectation?.comptoirId && comptoir) {
          this.formulaire.controls.comptoirId.disable({ emitEvent: false });
        }
        if (this.lignesPesee().length === 0) {
          this.lignesPesee.set([this.creerLigneVide(speculation?.id ?? 0)]);
        }
        this.chargerPrix(campagne?.id ?? 0);
      },
      error: () =>
        this.erreur.set('Impossible de charger les référentiels de collecte.')
    });
  }

  changerCampagne(campagneId: number): void {
    this.chargerPrix(campagneId);
  }

  private chargerPrix(campagneId: number): void {
    if (!campagneId) {
      this.prixGarantis.set([]);
      return;
    }
    this.referentielMetier.prixGarantis(campagneId).subscribe({
      next: (prix) => this.prixGarantis.set(prix),
      error: () => this.prixGarantis.set([])
    });
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.collecteService
      .lister({
        statut: this.statutFiltre(),
        page: this.page(),
        size: 20
      })
      .subscribe({
        next: (reponse) => {
          this.collectes.set(reponse.content);
          this.totalPages.set(reponse.totalPages);
          this.totalElements.set(reponse.totalElements);
          this.chargement.set(false);
        },
        error: () => {
          this.erreur.set('Impossible de charger les collectes.');
          this.chargement.set(false);
        }
      });
  }

  changerStatut(statut: CollecteStatut | ''): void {
    this.statutFiltre.set(statut);
    this.page.set(0);
    this.charger();
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

  basculerFormulaire(): void {
    this.formulaireVisible.update((valeur) => !valeur);
  }

  basculerDetail(id: number): void {
    this.collecteOuverte.update((actuelle) => (actuelle === id ? null : id));
  }

  examiner(collecte: Collecte, approuve: boolean): void {
    let motif: string | undefined;
    if (!approuve) {
      const saisie = window.prompt('Motif du rejet du dossier :');
      if (saisie === null) {
        return;
      }
      if (!saisie.trim()) {
        this.notifier('Le motif de rejet est obligatoire.');
        return;
      }
      motif = saisie.trim();
    }
    this.collecteService.examiner(collecte.id, approuve, motif).subscribe({
      next: () => {
        this.notifier(approuve ? 'Dossier transmis à la DCT.' : 'Dossier rejeté.');
        this.charger();
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  valider(collecte: Collecte, approuve: boolean): void {
    let motif: string | undefined;
    if (!approuve) {
      const saisie = window.prompt('Motif du rejet du BRBC :');
      if (saisie === null) {
        return;
      }
      if (!saisie.trim()) {
        this.notifier('Le motif de rejet est obligatoire.');
        return;
      }
      motif = saisie.trim();
    }
    this.collecteService.valider(collecte.id, approuve, motif).subscribe({
      next: () => {
        this.notifier(approuve ? 'BRBC validé et transmis à la DFC.' : 'Dossier rejeté.');
        this.charger();
      },
      error: (erreur) => this.notifier(this.messageErreur(erreur))
    });
  }

  enregistrer(): void {
    if (this.sansComptoir()) {
      this.notifier('Aucun comptoir ne vous est affecté. Contactez l’administrateur.');
      return;
    }
    this.formulaire.markAllAsTouched();
    if (this.formulaire.invalid) {
      this.notifier('Veuillez compléter correctement le formulaire.');
      return;
    }
    const lignes = this.lignesPesee();
    if (lignes.length === 0) {
      this.notifier('Ajoutez au moins une ligne de pesée (une spéculation).');
      return;
    }
    const speculationsVues = new Set<number>();
    for (let i = 0; i < lignes.length; i++) {
      const l = lignes[i];
      const net = (Number(l.poidsBrutTotalKg) || 0) - (Number(l.tareEmballageKg) || 0);
      if (net <= 0) {
        this.notifier(`Ligne ${i + 1} : le poids net doit être strictement positif.`);
        return;
      }
      if (!l.conforme && !l.motifRejet.trim()) {
        this.notifier(`Ligne ${i + 1} : un motif est obligatoire pour une ligne non conforme.`);
        return;
      }
      if (!l.speculationId || l.speculationId <= 0) {
        this.notifier(`Ligne ${i + 1} : sélectionnez une spéculation.`);
        return;
      }
      if (speculationsVues.has(l.speculationId)) {
        this.notifier(
          `Ligne ${i + 1} : cette spéculation est déjà présente sur le BRBC. Chaque spéculation ne peut apparaître qu'une seule fois.`
        );
        return;
      }
      if (this.prixUnitaireLigne(l) <= 0) {
        this.notifier(
          `Ligne ${i + 1} : aucun prix garanti approuvé n'est en vigueur pour cette spéculation.`
        );
        return;
      }
      speculationsVues.add(l.speculationId);
    }

    const valeurs = this.formulaire.getRawValue();
    const request: CreateCollecteLotRequest = {
      campagneId: valeurs.campagneId,
      comptoirId: valeurs.comptoirId,
      producteurId: valeurs.producteurId,
      modePaiement: valeurs.modePaiement,
      observationsReceptionnaire: valeurs.observationsReceptionnaire || null,
      lignes: lignes.map((ligne) => ({
        speculationId: ligne.speculationId,
        quantiteKg: this.poidsNetLigne(ligne),
        tauxHumidite: Number(ligne.tauxHumidite) || 0,
        tauxImpuretes: Number(ligne.tauxImpuretes) || 0,
        grainsAvaries: Number(ligne.grainsAvaries) || 0,
        conforme: ligne.conforme,
        decisionAgent: ligne.conforme ? 'ACCEPTE' : 'REJETE',
        motifRejet: ligne.conforme ? null : ligne.motifRejet.trim(),
        nombreSacs: Number(ligne.nombreSacs) || 1,
        poidsBrutTotalKg: Number(ligne.poidsBrutTotalKg) || 0,
        tareEmballageKg: Number(ligne.tareEmballageKg) || 0
      }))
    };

    this.enregistrement.set(true);
    this.collecteService.enregistrerLot(request).subscribe({
      next: (resultat) => {
        this.enregistrement.set(false);
        this.formulaireVisible.set(false);
        this.lignesPesee.set([this.creerLigneVide()]);
        this.formulaire.reset({
          campagneId: valeurs.campagneId,
          comptoirId: valeurs.comptoirId,
          producteurId: valeurs.producteurId,
          modePaiement: 'MOBILE_MONEY',
          observationsReceptionnaire: ''
        });
        this.notifier(
          `BRBC ${resultat.bordereau.numeroBrbc} émis avec ${resultat.collectes.length} lot(s) — ${this.formaterMontant(resultat.bordereau.montantNetPayerCfa)}.`
        );
        this.page.set(0);
        this.charger();
      },
      error: (erreur) => {
        this.enregistrement.set(false);
        this.notifier(this.messageErreur(erreur));
      }
    });
  }

  creerLigneVide(speculationId?: number): LignePeseeForm {
    const specs = this.speculations().filter((s) => s.active);
    const utilisees = new Set(this.lignesPesee().map((ligne) => ligne.speculationId));
    const premiereDisponible = specs.find((s) => !utilisees.has(s.id)) ?? specs[0];
    return {
      speculationId: speculationId ?? (premiereDisponible ? premiereDisponible.id : 0),
      nombreSacs: 1,
      poidsBrutTotalKg: 0,
      tareEmballageKg: 0,
      tauxHumidite: 0,
      tauxImpuretes: 0,
      grainsAvaries: 0,
      conforme: true,
      motifRejet: ''
    };
  }

  ajouterLigne(): void {
    const disponibles = this.speculations().filter((speculation) => speculation.active);
    if (this.lignesPesee().length >= disponibles.length) {
      this.notifier('Toutes les spéculations actives sont déjà présentes sur ce BRBC.');
      return;
    }
    this.lignesPesee.update((lignes) => [...lignes, this.creerLigneVide()]);
  }

  supprimerLigne(index: number): void {
    if (this.lignesPesee().length <= 1) {
      this.notifier('Le bordereau doit contenir au moins une spéculation.');
      return;
    }
    this.lignesPesee.update((lignes) => lignes.filter((_, i) => i !== index));
  }

  mettreAJourLigne(index: number, champ: keyof LignePeseeForm, valeur: unknown): void {
    this.lignesPesee.update((lignes) =>
      lignes.map((ligne, i) =>
        i === index ? { ...ligne, [champ]: valeur } : ligne
      )
    );
  }

  poidsNetLigne(ligne: LignePeseeForm): number {
    return Math.max(
      0,
      Math.round(((Number(ligne.poidsBrutTotalKg) || 0) - (Number(ligne.tareEmballageKg) || 0)) * 100) / 100
    );
  }

  speculationDeLigne(ligne: LignePeseeForm): Speculation | null {
    return this.speculations().find((s) => s.id === ligne.speculationId) ?? null;
  }

  speculationDejaUtilisee(speculationId: number, index: number): boolean {
    return this.lignesPesee().some(
      (ligne, i) => i !== index && ligne.speculationId === speculationId
    );
  }

  prixUnitaireLigne(ligne: LignePeseeForm): number {
    const comptoir = this.comptoirSelectionne();
    const aujourdHui = new Date().toISOString().slice(0, 10);
    const eligibles = this.prixGarantis().filter(
      (prix) =>
        prix.speculationId === ligne.speculationId &&
        prix.statut === 'APPROUVE' &&
        prix.dateEffet <= aujourdHui
    );
    const local = comptoir?.localityId
      ? eligibles
          .filter((prix) => prix.localityId === comptoir.localityId)
          .sort((a, b) => b.dateEffet.localeCompare(a.dateEffet))
      : [];
    if (local.length > 0) {
      return local[0].prixUnitaireKg;
    }
    const global = eligibles
      .filter((prix) => prix.localityId === null)
      .sort((a, b) => b.dateEffet.localeCompare(a.dateEffet));
    return global[0]?.prixUnitaireKg ?? 0;
  }

  montantLigne(ligne: LignePeseeForm): number {
    return Math.round(this.poidsNetLigne(ligne) * this.prixUnitaireLigne(ligne));
  }

  qualiteAlerteLigne(ligne: LignePeseeForm): boolean {
    const spec = this.speculationDeLigne(ligne);
    if (!spec) return false;
    return (Number(ligne.tauxHumidite) || 0) > spec.seuilHumiditeMax;
  }

  ligneInvalide(ligne: LignePeseeForm): boolean {
    return (
      !ligne.speculationId ||
      ligne.speculationId <= 0 ||
      this.poidsNetLigne(ligne) <= 0 ||
      (!ligne.conforme && !ligne.motifRejet.trim()) ||
      this.qualiteAlerteLigne(ligne)
    );
  }

  formaterPoidsNet(ligne: LignePeseeForm): string {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(this.poidsNetLigne(ligne));
  }

  formaterMontantLigne(ligne: LignePeseeForm): string {
    return new Intl.NumberFormat('fr-FR', {
      maximumFractionDigits: 0
    }).format(this.montantLigne(ligne));
  }

  ouvrirCreationProducteur(): void {
    if (!this.peutGererProducteurs()) {
      return;
    }
    const comptoirId = this.formulaire.controls.comptoirId.value;
    const comptoir = this.comptoirs().find((element) => element.id === comptoirId);
    this.dialog
      .open(ReferentielFormDialogComponent, {
        data: {
          title: 'Enregistrer un producteur',
          subtitle:
            'Le producteur absent du référentiel peut être créé et sélectionné directement.',
          submitLabel: 'Enregistrer et sélectionner',
          fields: this.champsProducteur(),
          localites: this.localites(),
          initial: {
            nomPrenom: '',
            telephoneMobileMoney: '',
            operateurMobile: 'SANK',
            cnibNumero: '',
            cnibDateDelivrance: '',
            cnibLieuDelivrance: '',
            localityId: comptoir?.localityId ?? '',
            adressePrecision: ''
          }
        },
        width: '760px',
        maxWidth: '95vw',
        autoFocus: 'first-tabbable'
      })
      .afterClosed()
      .subscribe((valeurs: Record<string, unknown> | undefined) => {
        if (!valeurs) {
          return;
        }
        this.referentielService.creerProducteur(this.payloadProducteur(valeurs)).subscribe({
          next: (producteur) => {
            this.producteurs.update((producteurs) =>
              [...producteurs, producteur].sort((a, b) =>
                a.nomPrenom.localeCompare(b.nomPrenom, 'fr')
              )
            );
            this.formulaire.controls.producteurId.setValue(producteur.id);
            this.formulaire.controls.producteurId.markAsTouched();
            this.notifier(`Producteur ${producteur.nomPrenom} enregistré et sélectionné.`);
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  libelleStatut(statut: CollecteStatut): string {
    return STATUT_COLLECTE_LABELS[statut] ?? statut;
  }

  couleurStatut(statut: CollecteStatut): string {
    return STATUT_COLLECTE_COLORS[statut] ?? 'neutre';
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

  formaterMontant(valeur: number | null | undefined): string {
    if (valeur === null || valeur === undefined) {
      return '—';
    }
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(valeur)} CFA`;
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

  private payloadProducteur(valeurs: Record<string, unknown>): ProducteurPayload {
    return {
      nomPrenom: this.texte(valeurs['nomPrenom']),
      telephoneMobileMoney: this.texte(valeurs['telephoneMobileMoney']),
      operateurMobile: this.texte(valeurs['operateurMobile']),
      cnibNumero: this.texte(valeurs['cnibNumero']),
      cnibDateDelivrance: this.texteOuNull(valeurs['cnibDateDelivrance']),
      cnibLieuDelivrance: this.texteOuNull(valeurs['cnibLieuDelivrance']),
      localityId: this.texteOuNull(valeurs['localityId']),
      adressePrecision: this.texteOuNull(valeurs['adressePrecision'])
    };
  }

  private texte(valeur: unknown): string {
    return typeof valeur === 'string' ? valeur.trim() : '';
  }

  ouvrirBrbc(collecte: Collecte): void {
    this.brbcVisualise.set(collecte);
    this.brbcLignes.set([collecte]);
    const bordereauId = collecte.bordereau?.id;
    if (!bordereauId) {
      this.defilerVersBrbc();
      return;
    }
    this.collecteService.listerParBrbc(bordereauId).subscribe({
      next: (lignes) => {
        this.brbcLignes.set(lignes.length > 0 ? lignes : [collecte]);
        this.defilerVersBrbc();
      },
      error: () => this.defilerVersBrbc()
    });
  }

  private defilerVersBrbc(): void {
    setTimeout(() => {
      document
        .querySelector('.brbc-document-overlay')
        ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 80);
  }

  fermerBrbc(): void {
    this.brbcVisualise.set(null);
    this.brbcLignes.set([]);
  }

  imprimerBrbc(): void {
    window.print();
  }

  private texteOuNull(valeur: unknown): string | null {
    const texte = this.texte(valeur);
    return texte.length > 0 ? texte : null;
  }

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4500 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }

  /** Convertit un montant entier en toutes lettres (français, FCFA) */
  private nombreEnLettres(montant: number): string {
    if (!montant || montant <= 0) return 'Zéro franc CFA';
    const unites = ['', 'un', 'deux', 'trois', 'quatre', 'cinq', 'six', 'sept', 'huit', 'neuf',
      'dix', 'onze', 'douze', 'treize', 'quatorze', 'quinze', 'seize', 'dix-sept', 'dix-huit', 'dix-neuf'];
    const dizaines = ['', '', 'vingt', 'trente', 'quarante', 'cinquante', 'soixante', 'soixante', 'quatre-vingt', 'quatre-vingt'];

    const centaine = (n: number): string => {
      if (n === 0) return '';
      if (n < 20) return unites[n];
      const d = Math.floor(n / 10);
      const u = n % 10;
      if (d === 7) return 'soixante-' + (u === 1 ? 'et-onze' : unites[10 + u]);
      if (d === 9) return 'quatre-vingt-' + (u > 0 ? unites[u] : '');
      return dizaines[d] + (u === 1 && d < 8 ? '-et-' : u > 0 ? '-' : '') + (u > 0 ? unites[u] : '');
    };

    const tranche = (n: number): string => {
      if (n === 0) return '';
      const c = Math.floor(n / 100);
      const r = n % 100;
      let result = '';
      if (c === 1) result += 'cent ';
      else if (c > 1) result += unites[c] + ' cents ';
      result += centaine(r);
      return result.trim();
    };

    const n = Math.floor(montant);
    if (n >= 1_000_000_000) {
      const m = Math.floor(n / 1_000_000_000);
      const r = n % 1_000_000_000;
      return (tranche(m) + (m === 1 ? ' milliard' : ' milliards') + (r > 0 ? ' ' + this.nombreEnLettres(r).replace(' franc CFA', '') : '') + ' francs CFA').trim();
    }
    if (n >= 1_000_000) {
      const m = Math.floor(n / 1_000_000);
      const r = n % 1_000_000;
      return (tranche(m) + (m === 1 ? ' million' : ' millions') + (r > 0 ? ' ' + this.nombreEnLettres(r).replace(' franc CFA', '') : '') + ' francs CFA').trim();
    }
    if (n >= 1_000) {
      const m = Math.floor(n / 1_000);
      const r = n % 1_000;
      const millePart = m === 1 ? 'mille' : tranche(m) + ' mille';
      return (millePart + (r > 0 ? ' ' + tranche(r) : '') + ' francs CFA').trim();
    }
    return (tranche(n) + ' francs CFA').trim();
  }
}
