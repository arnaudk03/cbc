import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { Comptoir } from '../../core/api.models';
import {
  ETAT_EQUIPEMENT_LABELS,
  EtatEquipement,
  Equipement,
  EquipementPayload,
  TYPE_EQUIPEMENT_LABELS,
  TypeEquipement
} from '../../core/equipement.models';
import { EquipementService } from '../../core/equipement.service';
import { Utilisateur } from '../../core/utilisateur.models';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';
import {
  FormFieldDefinition,
  ReferentielFormDialogComponent
} from './referentiel-form-dialog.component';

export interface EquipementsComptoirDialogData {
  comptoir: Comptoir;
  utilisateurs: Utilisateur[];
}

const ROLES_TERRAIN = ['AGENT_COLLECTEUR', 'CHEF_COMPTOIR'];

@Component({
  selector: 'app-equipements-comptoir-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './equipements-comptoir-dialog.component.html',
  styleUrl: './equipements-comptoir-dialog.component.scss'
})
export class EquipementsComptoirDialogComponent implements OnInit {
  readonly data = inject<EquipementsComptoirDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<EquipementsComptoirDialogComponent>);
  private readonly equipementService = inject(EquipementService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly affectes = signal<Equipement[]>([]);
  readonly disponibles = signal<Equipement[]>([]);
  readonly selection = signal<number[]>([]);
  readonly utilisateurId = signal<number | null>(null);
  readonly motif = signal('');
  readonly recherche = signal('');
  readonly typeFiltre = signal<TypeEquipement | 'TOUS'>('TOUS');

  readonly types = Object.entries(TYPE_EQUIPEMENT_LABELS) as [
    TypeEquipement,
    string
  ][];
  readonly etats = Object.entries(ETAT_EQUIPEMENT_LABELS) as [EtatEquipement, string][];

  readonly utilisateursEligibles = computed(() =>
    this.data.utilisateurs
      .filter(
        (utilisateur) =>
          utilisateur.comptoirId === this.data.comptoir.id &&
          utilisateur.actif &&
          utilisateur.roles.some((role) => ROLES_TERRAIN.includes(role))
      )
      .sort((a, b) => a.nomComplet.localeCompare(b.nomComplet, 'fr'))
  );

  readonly disponiblesFiltres = computed(() => {
    const recherche = this.recherche().trim().toLowerCase();
    const type = this.typeFiltre();
    return this.disponibles()
      .filter((equipement) => type === 'TOUS' || equipement.type === type)
      .filter(
        (equipement) =>
          !recherche ||
          equipement.designation.toLowerCase().includes(recherche) ||
          equipement.code.toLowerCase().includes(recherche) ||
          (equipement.numeroSerie ?? '').toLowerCase().includes(recherche)
      )
      .sort((a, b) => a.designation.localeCompare(b.designation, 'fr'));
  });

  readonly tousSelectionnes = computed(
    () =>
      this.disponiblesFiltres().length > 0 &&
      this.disponiblesFiltres().every((equipement) =>
        this.selection().includes(equipement.id)
      )
  );

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    forkJoin({
      affectes: this.equipementService.lister({ comptoirId: this.data.comptoir.id }),
      disponibles: this.equipementService.lister({ disponibles: true })
    }).subscribe({
      next: (donnees) => {
        this.affectes.set(donnees.affectes);
        this.disponibles.set(donnees.disponibles);
        this.selection.set([]);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les équipements du comptoir.');
        this.chargement.set(false);
      }
    });
  }

  estSelectionne(id: number): boolean {
    return this.selection().includes(id);
  }

  basculer(id: number, coche: boolean): void {
    if (coche) {
      if (!this.estSelectionne(id)) {
        this.selection.set([...this.selection(), id]);
      }
      return;
    }
    this.selection.set(this.selection().filter((selectionne) => selectionne !== id));
  }

  basculerTout(coche: boolean): void {
    if (coche) {
      const ids = new Set(this.selection());
      for (const equipement of this.disponiblesFiltres()) {
        ids.add(equipement.id);
      }
      this.selection.set([...ids]);
      return;
    }
    const visibles = new Set(this.disponiblesFiltres().map((equipement) => equipement.id));
    this.selection.set(this.selection().filter((id) => !visibles.has(id)));
  }

  affecterSelection(): void {
    if (this.selection().length === 0) {
      return;
    }
    const tablettes = this.disponibles().filter(
      (equipement) =>
        this.selection().includes(equipement.id) && equipement.type === 'TABLETTE'
    );
    if (this.utilisateurId() !== null && tablettes.length > 1) {
      this.notifier('Un utilisateur ne peut détenir qu’une seule tablette.');
      return;
    }
    this.equipementService
      .affecterEnLot({
        equipementIds: [...this.selection()],
        comptoirId: this.data.comptoir.id,
        utilisateurId: this.utilisateurId(),
        motif: this.motif().trim() || null
      })
      .subscribe({
        next: (equipements) => {
          this.notifier(
            `${equipements.length} équipement(s) affecté(s) au comptoir ${this.data.comptoir.code}.`
          );
          this.utilisateurId.set(null);
          this.motif.set('');
          this.charger();
        },
        error: (erreur) => this.notifier(this.messageErreur(erreur))
      });
  }

  retirer(equipement: Equipement): void {
    this.dialog
      .open(ConfirmationDialogComponent, {
        data: {
          titre: 'Retirer l’équipement',
          message: `Retirer ${equipement.designation} (${equipement.code}) du comptoir ${this.data.comptoir.code} ?`,
          actionLabel: 'Retirer',
          danger: true
        },
        width: '480px',
        maxWidth: '95vw'
      })
      .afterClosed()
      .subscribe((confirme: boolean | undefined) => {
        if (!confirme) {
          return;
        }
        this.equipementService.retirer(equipement.id, null).subscribe({
          next: () => {
            this.notifier(`${equipement.designation} retiré du comptoir.`);
            this.charger();
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  creerEquipement(): void {
    this.dialog
      .open(ReferentielFormDialogComponent, {
        data: {
          title: 'Nouvel équipement',
          subtitle: `Affecté au comptoir ${this.data.comptoir.code}`,
          submitLabel: 'Créer et affecter',
          fields: this.champsEquipement(),
          initial: {
            type: 'TABLETTE',
            designation: '',
            code: '',
            numeroSerie: '',
            marque: '',
            modele: '',
            etat: 'BON',
            dateAcquisition: null,
            valeurCfa: null,
            imei: '',
            observation: ''
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
        this.equipementService.creer(this.payloadEquipement(valeurs)).subscribe({
          next: (equipement) => {
            this.equipementService
              .affecterEnLot({
                equipementIds: [equipement.id],
                comptoirId: this.data.comptoir.id,
                utilisateurId: null,
                motif: 'Création et affectation au comptoir'
              })
              .subscribe({
                next: () => {
                  this.notifier(
                    `${equipement.designation} créé et affecté au comptoir ${this.data.comptoir.code}.`
                  );
                  this.charger();
                },
                error: (erreur) => {
                  this.notifier(
                    `Équipement créé, mais affectation impossible : ${this.messageErreur(erreur)}`
                  );
                  this.charger();
                }
              });
          },
          error: (erreur) => this.notifier(this.messageErreur(erreur))
        });
      });
  }

  libelleType(type: TypeEquipement): string {
    return TYPE_EQUIPEMENT_LABELS[type] ?? type;
  }

  libelleEtat(etat: EtatEquipement): string {
    return ETAT_EQUIPEMENT_LABELS[etat] ?? etat;
  }

  classeEtat(etat: EtatEquipement): string {
    switch (etat) {
      case 'NEUF':
      case 'BON':
        return 'succes';
      case 'A_USURE':
        return 'alerte';
      default:
        return 'danger';
    }
  }

  icone(type: TypeEquipement): string {
    switch (type) {
      case 'TABLETTE':
        return 'tablet_mac';
      case 'BALANCE':
        return 'scale';
      case 'KIT_COLLECTE':
        return 'inventory_2';
      case 'VANNEUSE':
        return 'agriculture';
      case 'TELEPHONE':
        return 'smartphone';
      default:
        return 'devices_other';
    }
  }

  fermer(): void {
    this.dialogRef.close();
  }

  private champsEquipement(): FormFieldDefinition[] {
    return [
      {
        key: 'type',
        label: 'Type d’équipement',
        type: 'select',
        required: true,
        options: this.types.map(([value, label]) => ({ value, label }))
      },
      {
        key: 'designation',
        label: 'Désignation',
        type: 'text',
        required: true,
        placeholder: 'Ex. Tablette de collecte 01'
      },
      {
        key: 'code',
        label: 'Code',
        type: 'text',
        hint: 'Laissez vide pour générer automatiquement.'
      },
      {
        key: 'numeroSerie',
        label: 'Numéro de série',
        type: 'text',
        hint: 'Obligatoire pour une tablette.'
      },
      { key: 'marque', label: 'Marque', type: 'text' },
      { key: 'modele', label: 'Modèle', type: 'text' },
      {
        key: 'etat',
        label: 'État',
        type: 'select',
        options: this.etats.map(([value, label]) => ({ value, label }))
      },
      { key: 'dateAcquisition', label: 'Date d’acquisition', type: 'date' },
      { key: 'valeurCfa', label: 'Valeur (FCFA)', type: 'number', min: 0 },
      {
        key: 'imei',
        label: 'IMEI',
        type: 'text',
        hint: 'Tablettes uniquement.'
      },
      { key: 'observation', label: 'Observation', type: 'textarea' }
    ];
  }

  private payloadEquipement(valeurs: Record<string, unknown>): EquipementPayload {
    return {
      type: String(valeurs['type']) as TypeEquipement,
      designation: String(valeurs['designation']).trim(),
      code: this.texteOuNull(valeurs['code']),
      numeroSerie: this.texteOuNull(valeurs['numeroSerie']),
      marque: this.texteOuNull(valeurs['marque']),
      modele: this.texteOuNull(valeurs['modele']),
      etat: String(valeurs['etat'] ?? 'BON') as EtatEquipement,
      dateAcquisition: this.texteOuNull(valeurs['dateAcquisition']),
      valeurCfa: this.nombreOuNull(valeurs['valeurCfa']),
      observation: this.texteOuNull(valeurs['observation']),
      imei: this.texteOuNull(valeurs['imei'])
    };
  }

  private texteOuNull(valeur: unknown): string | null {
    if (valeur === null || valeur === undefined) {
      return null;
    }
    const texte = String(valeur).trim();
    return texte === '' ? null : texte;
  }

  private nombreOuNull(valeur: unknown): number | null {
    if (valeur === null || valeur === undefined || valeur === '') {
      return null;
    }
    const nombre = Number(valeur);
    return Number.isNaN(nombre) ? null : nombre;
  }

  private notifier(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 4500 });
  }

  private messageErreur(erreur: unknown): string {
    const contenu = erreur as { error?: { message?: string } };
    return contenu?.error?.message ?? 'Une erreur est survenue.';
  }
}
