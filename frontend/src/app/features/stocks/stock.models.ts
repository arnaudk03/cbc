export interface FicheStock {
  id: number;
  comptoirId: number;
  comptoirNom: string;
  dateFiche: string;
  natureOperation: string;
  entreeSacs: number;
  entreePoidsKg: number;
  entreeValeurCfa: number;
  sortieSacs: number;
  sortiePoidsKg: number;
  sortieValeurCfa: number;
  stockFinalSacs: number;
  stockFinalPoidsKg: number;
  stockFinalValeurCfa: number;
  creeLe: string;
}

export interface SpeculationDisponible {
  speculationId: number;
  speculationCode: string;
  speculationNom: string;
  quantiteCollecteeKg: number;
  quantiteEnleveeKg: number;
  quantiteDisponibleKg: number;
}

export interface StockDisponible {
  comptoirId: number;
  comptoirNom: string;
  tonnageCollecteValideKg: number;
  tonnageEnleveKg: number;
  tonnageDisponibleKg: number;
  speculations: SpeculationDisponible[];
}

export interface FsjRequest {
  comptoirId: number;
  dateFiche: string;
  natureOperation: string;
  entreeSacs: number;
  entreePoidsKg: number;
  entreeValeurCfa: number;
  sortieSacs: number;
  sortiePoidsKg: number;
  sortieValeurCfa: number;
}

export type StatutBebc = 'EN_TRANSIT' | 'LIVRE' | 'ECART_CONSTATE';

export interface Reception {
  id: number;
  numeroReception: string;
  magasinCentralId: number;
  magasinNom: string;
  magasinier: string;
  dateDechargement: string;
  nombreSacsRecus: number;
  tonnageDechargeKg: number;
  ecartPoidsKg: number;
  statutConformite: string;
}

export interface TransfertLigne {
  id: number;
  speculationId: number;
  speculationCode: string;
  speculationNom: string;
  nombreSacs: number;
  colisageKg: number;
  quantiteKg: number;
}

export interface Transfert {
  id: number;
  numeroBebc: string;
  comptoirId: number;
  comptoirNom: string;
  magasinDestinationId: number | null;
  magasinDestinationNom: string | null;
  direction: string | null;
  localite: string | null;
  magasinDepart: string | null;
  beneficiaire: string | null;
  objetEnlevement: string | null;
  nombreSacs: number;
  tonnageEnleveKg: number;
  lignes: TransfertLigne[];
  nomTransporteur: string;
  numeroVehicule: string;
  statut: StatutBebc;
  dateEnlevement: string;
  dateArriveePrevue: string | null;
  creeLe: string;
  reception: Reception | null;
}

export interface TransfertLigneRequest {
  speculationId: number;
  nombreSacs: number;
  colisageKg: number;
}

export interface TransfertRequest {
  comptoirId: number;
  magasinDestinationId: number;
  direction?: string | null;
  localite?: string | null;
  magasinDepart?: string | null;
  beneficiaire?: string | null;
  objetEnlevement?: string | null;
  lignes: TransfertLigneRequest[];
  nomTransporteur: string;
  numeroVehicule: string;
  dateArriveePrevue?: string | null;
}

export interface ReceptionRequest {
  magasinCentralId: number;
  nombreSacsRecus: number;
  tonnageDechargeKg: number;
}

export const STATUT_BEBC_LABELS: Record<StatutBebc, string> = {
  EN_TRANSIT: 'En transit',
  LIVRE: 'Livré',
  ECART_CONSTATE: 'Écart constaté'
};

export const STATUT_BEBC_COLORS: Record<StatutBebc, string> = {
  EN_TRANSIT: 'alerte',
  LIVRE: 'succes',
  ECART_CONSTATE: 'danger'
};
