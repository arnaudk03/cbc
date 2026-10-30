export interface CampagneInfo {
  id: number;
  code: string;
  libelle: string;
  statut: string;
  dateDebut: string;
  dateFin: string;
}

export interface DashboardKpis {
  objectifTonnes: number;
  collecteTonnes: number;
  tauxRealisation: number;
  brbcValides: number;
  montantDecaisseCfa: number;
  comptoirsActifs: number;
  collectesEnAttente: number;
  paiementsEnAttente: number;
}

export interface SpeculationStat {
  code: string;
  nom: string;
  tonnage: number;
}

export interface RegionStat {
  region: string;
  tonnage: number;
}

export interface EvolutionPoint {
  periode: string;
  tonnage: number;
}

export interface DashboardResponse {
  campagne: CampagneInfo | null;
  kpis: DashboardKpis;
  speculations: SpeculationStat[];
  regions: RegionStat[];
  evolution: EvolutionPoint[];
}
