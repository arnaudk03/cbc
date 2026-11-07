export interface FiltreStatistique {
  campagneId?: number | null;
  comptoirId?: number | null;
  regionId?: string | null;
  speculationId?: number | null;
  dateDebut?: string | null;
  dateFin?: string | null;
}

export interface CampagneInfo {
  id: number;
  code: string;
  libelle: string;
  statut: string;
  dateDebut: string;
  dateFin: string;
  objectifTotalTonnes: number;
}

export interface KpisStatistiques {
  objectifTonnes: number;
  collecteTonnes: number;
  tauxRealisation: number;
  montantTotalNetCfa: number;
  montantDecaisseCfa: number;
  tauxDecaissement: number;
  producteursUniques: number;
  comptoirsActifs: number;
  lotsCollectes: number;
  brbcEmis: number;
  brbcValides: number;
  tauxConformite: number;
}

export interface SpeculationStat {
  speculationId: number;
  code: string;
  nom: string;
  tonnage: number;
  partPourcent: number;
  montantCfa: number;
  prixMoyenKg: number;
  humiditeMoyenne: number;
  seuilHumiditeMax: number;
  tauxConformite: number;
  nombreLots: number;
}

export interface RegionStat {
  regionId: string;
  region: string;
  tonnage: number;
  partPourcent: number;
  montantCfa: number;
  nombreComptoirs: number;
  nombreLots: number;
}

export interface ComptoirStat {
  comptoirId: number;
  code: string;
  nom: string;
  region: string;
  tonnage: number;
  partPourcent: number;
  montantCfa: number;
  nombreLots: number;
  nombreProducteurs: number;
}

export interface OperateurStat {
  operateur: string;
  nombrePaiements: number;
  montantTotalCfa: number;
  montantConfirmeCfa: number;
  partPourcent: number;
  tauxSucces: number;
}

export interface MotifRejetStat {
  motif: string;
  occurrences: number;
}

export interface QualiteStat {
  lotsControles: number;
  lotsConformes: number;
  lotsNonConformes: number;
  tauxConformite: number;
  humiditeMoyenne: number;
  impuretesMoyennes: number;
  grainsAvariesMoyens: number;
  motifsRejet: MotifRejetStat[];
}

export interface EvolutionPoint {
  periode: string;
  tonnage: number;
  montantCfa: number;
  nombreCollectes: number;
}

export interface StatistiquesResponse {
  campagne: CampagneInfo | null;
  kpis: KpisStatistiques;
  speculations: SpeculationStat[];
  regions: RegionStat[];
  comptoirs: ComptoirStat[];
  operateurs: OperateurStat[];
  qualite: QualiteStat;
  evolution: EvolutionPoint[];
}
