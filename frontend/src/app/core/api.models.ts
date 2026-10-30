export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Campagne {
  id: number;
  code: string;
  libelle: string;
  dateDebut: string;
  dateFin: string;
  objectifTotalTonnes: number;
  statut: string;
}

export interface Comptoir {
  id: number;
  code: string;
  nom: string;
  type: string;
  localityId: string;
  localityNom: string;
  regionNom: string | null;
  latitudeGps: number | null;
  longitudeGps: number | null;
  capaciteStockageKg: number | null;
  actif: boolean;
}

export interface Speculation {
  id: number;
  code: string;
  nom: string;
  seuilHumiditeMax: number;
  seuilImpureteMax: number;
  active: boolean;
}

export interface Producteur {
  id: number;
  nomPrenom: string;
  telephoneMobileMoney: string;
  operateurMobile: string;
  cnibNumero: string;
  cnibDateDelivrance: string | null;
  cnibLieuDelivrance: string | null;
  localityId: string | null;
  localityNom: string | null;
  adressePrecision: string | null;
}

export interface Magasin {
  id: number;
  codeMagasin: string;
  nom: string;
  ville: string;
  localityId: string | null;
  capaciteTotaleTonnes: number;
  stockActuelTonnes: number;
  actif: boolean;
}

export interface CampagnePayload {
  code: string;
  libelle: string;
  dateDebut: string;
  dateFin: string;
  objectifTotalTonnes: number;
  statut: string;
}

export interface SpeculationPayload {
  code: string;
  nom: string;
  seuilHumiditeMax: number;
  seuilImpureteMax: number;
  active: boolean;
}

export interface ComptoirPayload {
  code: string;
  nom: string;
  type: string;
  localityId: string;
  latitudeGps: number | null;
  longitudeGps: number | null;
  capaciteStockageKg: number;
  actif: boolean;
}

export interface ProducteurPayload {
  nomPrenom: string;
  telephoneMobileMoney: string;
  operateurMobile: string;
  cnibNumero: string;
  cnibDateDelivrance: string | null;
  cnibLieuDelivrance: string | null;
  localityId: string | null;
  adressePrecision: string | null;
}

export const OPERATEURS_MOBILE: { value: string; label: string }[] = [
  { value: 'SANK', label: 'Sank' },
  { value: 'CORIS', label: 'Coris Money' },
  { value: 'MOOV', label: 'Moov Money' },
  { value: 'LIGDICASH', label: 'LigdiCash' },
  { value: 'ORANGE', label: 'Orange Money' },
  { value: 'TELECEL', label: 'Telecel' },
  { value: 'AUTRE', label: 'Autre' }
];

export interface MagasinPayload {
  codeMagasin: string;
  nom: string;
  ville: string;
  localityId: string | null;
  capaciteTotaleTonnes: number;
  actif: boolean;
}
