export type CategorieMaterielEmballage =
  | 'SACS'
  | 'BACHES'
  | 'PRODUITS_PHYTO'
  | 'AUTRE';

export type UniteMateriel =
  | 'SAC'
  | 'UNITE'
  | 'KG'
  | 'LITRE'
  | 'BIDON'
  | 'ROULEAU'
  | 'AUTRE';

export type StatutFicheStockMateriel = 'BROUILLON' | 'VALIDEE' | 'ANNULEE';

export const CATEGORIE_EMBALLAGE_LABELS: Record<CategorieMaterielEmballage, string> = {
  SACS: 'Sacs',
  BACHES: 'Bâches',
  PRODUITS_PHYTO: 'Produits phytosanitaires',
  AUTRE: 'Autre'
};

export const UNITE_MATERIEL_LABELS: Record<UniteMateriel, string> = {
  SAC: 'Sac',
  UNITE: 'Unité',
  KG: 'Kilogramme',
  LITRE: 'Litre',
  BIDON: 'Bidon',
  ROULEAU: 'Rouleau',
  AUTRE: 'Autre'
};

export const STATUT_FICHE_MATERIEL_LABELS: Record<StatutFicheStockMateriel, string> = {
  BROUILLON: 'Brouillon',
  VALIDEE: 'Validée',
  ANNULEE: 'Annulée'
};

export interface MaterielEmballage {
  id: number;
  code: string;
  libelle: string;
  categorie: CategorieMaterielEmballage;
  unite: UniteMateriel;
  seuilAlerte: number;
  actif: boolean;
}

export interface StockMateriel {
  materielId: number;
  code: string;
  libelle: string;
  categorie: CategorieMaterielEmballage;
  unite: UniteMateriel;
  seuilAlerte: number;
  stockFinal: number;
  alerte: boolean;
}

export interface LigneFicheStockMateriel {
  id: number;
  materielId: number;
  materielCode: string;
  materielLibelle: string;
  categorie: CategorieMaterielEmballage;
  unite: UniteMateriel;
  seuilAlerte: number;
  stockInitial: number;
  entrees: number;
  sorties: number;
  stockTheorique: number;
  stockPhysique: number | null;
  ecart: number | null;
  stockFinal: number;
  referenceDoc: string | null;
  observation: string | null;
  alerte: boolean;
}

export interface FicheStockMateriel {
  id: number;
  numeroFiche: string;
  comptoirId: number;
  comptoirCode: string;
  comptoirNom: string;
  dateFiche: string;
  statut: StatutFicheStockMateriel;
  observations: string | null;
  creePar: string | null;
  validePar: string | null;
  creeLe: string | null;
  valideLe: string | null;
  nombreLignes: number;
  lignes: LigneFicheStockMateriel[];
}

export interface MaterielEmballagePayload {
  code: string | null;
  libelle: string;
  categorie: CategorieMaterielEmballage;
  unite: UniteMateriel;
  seuilAlerte: number;
  actif: boolean;
}

export interface LigneFicheStockMaterielPayload {
  materielId: number;
  entrees: number;
  sorties: number;
  stockPhysique: number | null;
  referenceDoc: string | null;
  observation: string | null;
}

export interface FicheStockMaterielPayload {
  comptoirId: number;
  dateFiche: string;
  observations: string | null;
  lignes: LigneFicheStockMaterielPayload[];
}
