export type CollecteStatut =
  | 'SYNCHRONISEE'
  | 'EXAMINEE_SCP'
  | 'VALIDEE_DCT'
  | 'REJETEE';

export interface ControleQualite {
  id: number;
  tauxHumidite: number;
  tauxImpuretes: number;
  grainsAvaries: number;
  conforme: boolean;
  decisionAgent: string;
  motifRejet: string | null;
}

export interface FichePesee {
  id: number;
  numeroFiche: string;
  directionRegionale: string | null;
  directionProvinciale: string | null;
  siteComptoir: string;
  nombreSacs: number;
  poidsBrutTotalKg: number;
  tareEmballageKg: number;
  poidsNetTotalKg: number;
}

export interface BordereauReception {
  id: number;
  numeroBrbc: string;
  dateEmission: string;
  quantiteKg: number;
  prixUnitaireApplique: number;
  montantNetPayerCfa: number;
  montantEnLettres: string;
  modePaiement: string;
  observationsReceptionnaire: string | null;
  statutValidation: string;
  nombreLots: number;
}

export interface Collecte {
  id: number;
  referenceLot: string;
  campagneId: number;
  campagneCode: string;
  comptoirId: number;
  comptoirCode: string;
  comptoirNom: string;
  region: string | null;
  producteurId: number;
  producteurNom: string;
  telephoneProducteur: string;
  operateurMobile: string;
  speculationId: number;
  speculationCode: string;
  speculationNom: string;
  dateHeureCollecte: string;
  quantiteKg: number;
  prixUnitaireKg: number;
  montantNetCfa: number;
  statut: CollecteStatut;
  latitudeGps: number | null;
  longitudeGps: number | null;
  motifRejet: string | null;
  examinateurId: number | null;
  examinateur: string | null;
  dateExamen: string | null;
  validateur: string | null;
  dateValidation: string | null;
  controleQualite: ControleQualite | null;
  fichePesee: FichePesee | null;
  bordereau: BordereauReception | null;
}

export interface LigneCollectePayload {
  speculationId: number;
  quantiteKg: number;
  tauxHumidite: number;
  tauxImpuretes: number;
  grainsAvaries: number;
  conforme: boolean;
  decisionAgent: string;
  motifRejet?: string | null;
  nombreSacs: number;
  poidsBrutTotalKg: number;
  tareEmballageKg: number;
}

export interface CreateCollecteLotRequest {
  campagneId: number;
  comptoirId: number;
  producteurId: number;
  modePaiement: string;
  observationsReceptionnaire?: string | null;
  latitudeGps?: number | null;
  longitudeGps?: number | null;
  lignes: LigneCollectePayload[];
}

export interface CollecteLotResponse {
  bordereau: BordereauReception;
  collectes: Collecte[];
}

export const STATUT_COLLECTE_LABELS: Record<CollecteStatut, string> = {
  SYNCHRONISEE: 'Synchronisée',
  EXAMINEE_SCP: 'Examinée SCP',
  VALIDEE_DCT: 'Validée DCT',
  REJETEE: 'Rejetée'
};

export const STATUT_COLLECTE_COLORS: Record<CollecteStatut, string> = {
  SYNCHRONISEE: 'info',
  EXAMINEE_SCP: 'alerte',
  VALIDEE_DCT: 'succes',
  REJETEE: 'danger'
};
