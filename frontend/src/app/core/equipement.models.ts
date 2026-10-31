export type TypeEquipement =
  | 'TABLETTE'
  | 'BALANCE'
  | 'KIT_COLLECTE'
  | 'VANNEUSE'
  | 'TELEPHONE'
  | 'AUTRE';

export type EtatEquipement = 'NEUF' | 'BON' | 'A_USURE' | 'HORS_SERVICE';

export type StatutEquipement =
  | 'DISPONIBLE'
  | 'AFFECTE'
  | 'MAINTENANCE'
  | 'VERROUILLEE'
  | 'REFORME';

export const TYPE_EQUIPEMENT_LABELS: Record<TypeEquipement, string> = {
  TABLETTE: 'Tablette',
  BALANCE: 'Balance',
  KIT_COLLECTE: 'Kit de collecte',
  VANNEUSE: 'Vanneuse',
  TELEPHONE: 'Téléphone',
  AUTRE: 'Autre'
};

export const ETAT_EQUIPEMENT_LABELS: Record<EtatEquipement, string> = {
  NEUF: 'Neuf',
  BON: 'Bon',
  A_USURE: 'Usé',
  HORS_SERVICE: 'Hors service'
};

export const STATUT_EQUIPEMENT_LABELS: Record<StatutEquipement, string> = {
  DISPONIBLE: 'Disponible',
  AFFECTE: 'Affecté',
  MAINTENANCE: 'Maintenance',
  VERROUILLEE: 'Verrouillée',
  REFORME: 'Réformé'
};

export interface Tablette {
  id: number;
  imei: string | null;
  dateDerniereSynchro: string | null;
}

export interface AffectationEquipement {
  id: number;
  comptoirId: number;
  comptoirCode: string;
  comptoirNom: string;
  utilisateurId: number | null;
  utilisateurNomComplet: string | null;
  dateDebut: string;
}

export interface Equipement {
  id: number;
  code: string;
  type: TypeEquipement;
  designation: string;
  numeroSerie: string | null;
  marque: string | null;
  modele: string | null;
  etat: EtatEquipement;
  statut: StatutEquipement;
  dateAcquisition: string | null;
  valeurCfa: number | null;
  observation: string | null;
  creeLe: string | null;
  affectation: AffectationEquipement | null;
  tablette: Tablette | null;
}

export interface EquipementPayload {
  type: TypeEquipement;
  designation: string;
  code: string | null;
  numeroSerie: string | null;
  marque: string | null;
  modele: string | null;
  etat: EtatEquipement;
  dateAcquisition: string | null;
  valeurCfa: number | null;
  observation: string | null;
  imei: string | null;
}

export interface AffectationEquipementPayload {
  equipementIds: number[];
  comptoirId: number;
  utilisateurId: number | null;
  motif: string | null;
}
