export interface Utilisateur {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  nomComplet: string;
  actif: boolean;
  compteBloque: boolean;
  comptoirId: number | null;
  comptoirCode: string | null;
  comptoirNom: string | null;
  roles: string[];
  dateDerniereConnexion: string | null;
  creeLe: string;
}

export interface RoleOption {
  code: string;
  libelle: string;
}

export interface UtilisateurPayload {
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  motDePasse: string | null;
  roles: string[];
  actif: boolean;
  comptoirId: number | null;
}
