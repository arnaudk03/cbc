export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: unknown;
  timestamp: string;
}

export interface UserSummary {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  comptoirId: number | null;
  comptoirCode: string | null;
  comptoirNom: string | null;
  roles: string[];
  permissions: string[];
}

export interface AuthResult {
  requires2fa: boolean;
  challengeToken?: string;
  otpDemo?: string;
  accessToken?: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  utilisateur: UserSummary;
}

export const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Administrateur système',
  DCT: 'Directeur Central Technique',
  SCP: 'Service Contrôle Qualité',
  DFC_DIRECTEUR: 'Directeur Financier',
  SFB_INITIATEUR: 'SFB Initiateur',
  SFB_CHEF: 'Chef SFB',
  COMPTABILITE: 'Comptabilité',
  DGSA: 'DGSA',
  SGS: 'Gestion des Stocks',
  MAGASINIER: 'Magasinier',
  AGENT_COLLECTEUR: 'Agent collecteur',
  CHEF_COMPTOIR: 'Chef de comptoir',
  DPL: 'Production & Logistique',
  CONSULTATION: 'Consultation'
};
