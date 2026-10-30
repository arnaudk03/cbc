export interface PrixGarantie {
  id: number;
  campagneId: number;
  campagneCode: string;
  speculationId: number;
  speculationCode: string;
  speculationNom: string;
  localityId: string | null;
  localityNom: string | null;
  prixUnitaireKg: number;
  dateEffet: string;
  statut: 'EN_ATTENTE_APPROBATION' | 'APPROUVE' | 'INACTIF';
  approuvePar: string | null;
  dateApprobation: string | null;
}

export interface PrixGarantiePayload {
  campagneId: number;
  speculationId: number;
  localityId: string | null;
  prixUnitaireKg: number;
  dateEffet: string;
}

export interface Localite {
  id: string;
  code: string;
  name: string;
  type: string;
  parentId: string | null;
  parentName: string | null;
  latitude: number | null;
  longitude: number | null;
}

export const STATUT_PRIX_LABELS: Record<string, string> = {
  EN_ATTENTE_APPROBATION: "En attente d'approbation",
  APPROUVE: 'Approuvé',
  INACTIF: 'Inactif'
};

export const STATUT_PRIX_COLORS: Record<string, string> = {
  EN_ATTENTE_APPROBATION: 'alerte',
  APPROUVE: 'succes',
  INACTIF: 'neutre'
};
