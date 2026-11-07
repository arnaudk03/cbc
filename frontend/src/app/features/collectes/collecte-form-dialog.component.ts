import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
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
import { Localite, PrixGarantie } from '../referentiels/referentiel.models';
import { ReferentielMetierService } from '../referentiels/referentiel.service';
import {
  FormFieldDefinition,
  ReferentielFormDialogComponent
} from '../referentiels/referentiel-form-dialog.component';
import {
  CollecteLotResponse,
  CreateCollecteLotRequest
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

export interface CollecteFormDialogData {
  campagnes?: Campagne[];
  comptoirs?: Comptoir[];
  speculations?: Speculation[];
  producteurs?: Producteur[];
  localites?: Localite[];
  prixGarantis?: PrixGarantie[];
}

export interface CollecteFormDialogResult {
  success: boolean;
  resultat?: CollecteLotResponse;
  producteurs?: Producteur[];
}

@Component({
  selector: 'app-collecte-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './collecte-form-dialog.component.html',
  styleUrl: './collecte-form-dialog.component.scss'
})
export class CollecteFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly collecteService = inject(CollecteService);
  private readonly referentielService = inject(ReferentielService);
  private readonly referentielMetier = inject(ReferentielMetierService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<CollecteFormDialogComponent>);
  readonly data = inject<CollecteFormDialogData>(MAT_DIALOG_DATA, { optional: true });
  readonly auth = inject(AuthService);

  readonly chargement = signal(false);
  readonly enregistrement = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly campagnes = signal<Campagne[]>(this.data?.campagnes ?? []);
  readonly comptoirs = signal<Comptoir[]>(this.data?.comptoirs ?? []);
  readonly speculations = signal<Speculation[]>(this.data?.speculations ?? []);
  readonly producteurs = signal<Producteur[]>(this.data?.producteurs ?? []);
  readonly localites = signal<Localite[]>(this.data?.localites ?? []);
  readonly prixGarantis = signal<PrixGarantie[]>(this.data?.prixGarantis ?? []);

  readonly peutGererProducteurs = computed(() =>
    this.auth.possedeUnRole('ADMIN', 'DCT', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')
  );
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

  readonly nbLignesNonConformes = computed(() =>
    this.lignesPesee().filter((l) => !l.conforme || this.qualiteAlerteLigne(l)).length
  );

  ngOnInit(): void {
    if (
      this.campagnes().length === 0 ||
      this.comptoirs().length === 0 ||
      this.speculations().length === 0
    ) {
      this.chargerReferentiels();
    } else {
      this.initialiserFormulaire();
    }
  }

  chargerReferentiels(): void {
    this.chargement.set(true);
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
        this.initialiserFormulaire();
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les référentiels.');
        this.chargement.set(false);
      }
    });
  }

  private initialiserFormulaire(): void {
    const campagnes = this.campagnes();
    const comptoirs = this.comptoirs();
    const producteurs = this.producteurs();
    const speculations = this.speculations();

    const campagne =
      campagnes.find((element) => element.statut === 'OUVERTE') ?? campagnes[0];
    const affectation = this.comptoirAffecte();
    const comptoir = affectation?.comptoirId
      ? comptoirs.find((element) => element.id === affectation.comptoirId)
      : this.profilTerrain()
        ? undefined
        : comptoirs.find((element) => element.actif) ?? comptoirs[0];
    const speculation =
      speculations.find((element) => element.active) ?? speculations[0];

    this.formulaire.patchValue({
      campagneId: campagne?.id ?? 0,
      comptoirId: comptoir?.id ?? 0,
      producteurId: producteurs[0]?.id ?? 0
    });

    if (affectation?.comptoirId && comptoir) {
      this.formulaire.controls.comptoirId.disable({ emitEvent: false });
    }

    if (this.lignesPesee().length === 0) {
      this.lignesPesee.set([this.creerLigneVide(speculation?.id ?? 0)]);
    }

    if (campagne?.id && this.prixGarantis().length === 0) {
      this.chargerPrix(campagne.id);
    }
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
        this.dialogRef.close({
          success: true,
          resultat,
          producteurs: this.producteurs()
        });
      },
      error: (erreur) => {
        this.enregistrement.set(false);
        this.notifier(this.messageErreur(erreur));
      }
    });
  }

  fermer(): void {
    this.dialogRef.close();
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
}
