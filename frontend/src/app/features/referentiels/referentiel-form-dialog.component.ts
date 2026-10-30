import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Localite } from './referentiel.models';

export type FormFieldType =
  | 'text'
  | 'email'
  | 'tel'
  | 'password'
  | 'number'
  | 'date'
  | 'select'
  | 'boolean'
  | 'textarea'
  | 'locality';

export interface FormFieldOption {
  value: string | number | null;
  label: string;
}

export interface FormFieldDefinition {
  key: string;
  label: string;
  type: FormFieldType;
  required?: boolean;
  placeholder?: string;
  hint?: string;
  emptyLabel?: string;
  options?: FormFieldOption[];
  multiple?: boolean;
  min?: string | number;
  max?: string | number;
  step?: string | number;
}

export interface FormDialogData {
  title: string;
  subtitle?: string;
  fields: FormFieldDefinition[];
  initial: Record<string, unknown>;
  submitLabel?: string;
  localites?: Localite[];
}

interface ChaineLocalite {
  regionId: string;
  provinceId: string;
  communeId: string;
  secteurId: string;
  villageId: string;
}

@Component({
  selector: 'app-referentiel-form-dialog',
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
  templateUrl: './referentiel-form-dialog.component.html',
  styleUrl: './referentiel-form-dialog.component.scss'
})
export class ReferentielFormDialogComponent {
  readonly data = inject<FormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ReferentielFormDialogComponent>);

  readonly valeurs: Record<string, unknown> = { ...this.data.initial };
  readonly chainesLocalite: Record<string, ChaineLocalite> = {};
  readonly regions: Localite[];
  private readonly localites: Localite[];
  private readonly localitesParId: Map<string, Localite>;
  erreur: string | null = null;

  constructor() {
    this.localites = [...(this.data.localites ?? [])];
    this.localitesParId = new Map(this.localites.map((localite) => [localite.id, localite]));
    this.regions = this.localites
      .filter((localite) => localite.type === 'REGION')
      .sort((a, b) => a.name.localeCompare(b.name, 'fr'));
    for (const field of this.data.fields) {
      if (field.type === 'locality') {
        this.chainesLocalite[field.key] = this.chainDepuis(this.valeurs[field.key]);
      }
    }
  }

  optionsProvinces(cle: string): Localite[] {
    return this.trier(
      this.localites.filter(
        (localite) =>
          localite.type === 'PROVINCE' &&
          localite.parentId === this.chainesLocalite[cle]?.regionId
      )
    );
  }

  optionsCommunes(cle: string): Localite[] {
    return this.trier(
      this.localites.filter(
        (localite) =>
          localite.type === 'COMMUNE' &&
          localite.parentId === this.chainesLocalite[cle]?.provinceId
      )
    );
  }

  optionsSecteurs(cle: string): Localite[] {
    return this.trier(
      this.localites.filter(
        (localite) =>
          (localite.type === 'SECTOR' || localite.type === 'ARRONDISSEMENT') &&
          localite.parentId === this.chainesLocalite[cle]?.communeId
      )
    );
  }

  optionsVillages(cle: string): Localite[] {
    return this.trier(
      this.localites.filter(
        (localite) =>
          localite.type === 'VILLAGE' &&
          localite.parentId === this.chainesLocalite[cle]?.secteurId
      )
    );
  }

  changerNiveauLocalite(
    cle: string,
    niveau: 'region' | 'province' | 'commune' | 'secteur' | 'village'
  ): void {
    const chaine = this.chainesLocalite[cle];
    if (niveau === 'region') {
      chaine.provinceId = '';
      chaine.communeId = '';
      chaine.secteurId = '';
      chaine.villageId = '';
    } else if (niveau === 'province') {
      chaine.communeId = '';
      chaine.secteurId = '';
      chaine.villageId = '';
    } else if (niveau === 'commune') {
      chaine.secteurId = '';
      chaine.villageId = '';
    } else if (niveau === 'secteur') {
      chaine.villageId = '';
    }
    this.valeurs[cle] =
      chaine.villageId ||
      chaine.secteurId ||
      chaine.communeId ||
      chaine.provinceId ||
      chaine.regionId ||
      '';
  }

  private chainDepuis(valeur: unknown): ChaineLocalite {
    const chaine: ChaineLocalite = {
      regionId: '',
      provinceId: '',
      communeId: '',
      secteurId: '',
      villageId: ''
    };
    let localite =
      typeof valeur === 'string' ? this.localitesParId.get(valeur) : undefined;
    while (localite) {
      if (localite.type === 'REGION') {
        chaine.regionId = localite.id;
      } else if (localite.type === 'PROVINCE') {
        chaine.provinceId = localite.id;
      } else if (localite.type === 'COMMUNE') {
        chaine.communeId = localite.id;
      } else if (localite.type === 'SECTOR' || localite.type === 'ARRONDISSEMENT') {
        chaine.secteurId = localite.id;
      } else if (localite.type === 'VILLAGE') {
        chaine.villageId = localite.id;
      }
      localite = localite.parentId ? this.localitesParId.get(localite.parentId) : undefined;
    }
    return chaine;
  }

  private trier(localites: Localite[]): Localite[] {
    return [...localites].sort((a, b) => a.name.localeCompare(b.name, 'fr'));
  }

  annuler(): void {
    this.dialogRef.close();
  }

  valider(): void {
    this.erreur = null;
    for (const field of this.data.fields) {
      const valeur = this.valeurs[field.key];
      if (
        field.required &&
        (valeur === null ||
          valeur === undefined ||
          (typeof valeur === 'string' && valeur.trim() === '') ||
          (Array.isArray(valeur) && valeur.length === 0))
      ) {
        this.erreur = `${field.label} est obligatoire.`;
        return;
      }
      if (field.type === 'email' && valeur && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(valeur))) {
        this.erreur = `${field.label} est invalide.`;
        return;
      }
      if (field.type === 'locality' && valeur) {
        const chaine = this.chainesLocalite[field.key];
        if (!chaine?.regionId || !chaine.provinceId || !chaine.communeId) {
          this.erreur = `${field.label} : sélectionnez au moins la commune.`;
          return;
        }
      }
    }
    this.dialogRef.close({ ...this.valeurs });
  }
}
