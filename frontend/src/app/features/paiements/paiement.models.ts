export type PaiementStatut =
  | 'INITIE'
  | 'VALIDE_2FA'
  | 'ENVOYE_OPERATEUR'
  | 'CONFIRME'
  | 'REJETE';

export interface Paiement {
  id: number;
  referenceTransaction: string;
  montantPayeCfa: number;
  operateur: string;
  telephoneDestination: string;
  initiateur: string | null;
  validateur: string | null;
  dateInitiation: string | null;
  dateValidation2FA: string | null;
  dateConfirmation: string | null;
  statut: PaiementStatut;
  referenceOperateur: string | null;
  messageRetourOperateur: string | null;
  motifRejet: string | null;
  bordereauId: number;
  numeroBrbc: string;
  collecteReference: string;
  nombreLots: number;
  producteurNom: string;
  campagneCode: string;
}

export interface OtpChallenge {
  message: string;
  codeDemo: string | null;
}

export const STATUT_PAIEMENT_LABELS: Record<PaiementStatut, string> = {
  INITIE: 'Initié',
  VALIDE_2FA: 'Validé 2FA',
  ENVOYE_OPERATEUR: 'Envoyé opérateur',
  CONFIRME: 'Confirmé',
  REJETE: 'Rejeté'
};

export const STATUT_PAIEMENT_COLORS: Record<PaiementStatut, string> = {
  INITIE: 'alerte',
  VALIDE_2FA: 'info',
  ENVOYE_OPERATEUR: 'violet',
  CONFIRME: 'succes',
  REJETE: 'danger'
};
