export type StatutNotification = 'EN_ATTENTE' | 'ENVOYE' | 'ECHEC' | 'LU';

export interface Notification {
  id: number;
  canal: string;
  typeNotification: string;
  destinataireContact: string;
  titre: string;
  message: string;
  statut: StatutNotification;
  collecteId: number | null;
  ordrePaiementId: number | null;
  bordereauEnlevementId: number | null;
  dateCreation: string;
  dateEnvoi: string | null;
  dateLecture: string | null;
}

export const STATUT_NOTIFICATION_LABELS: Record<string, string> = {
  EN_ATTENTE: 'En attente',
  ENVOYE: 'Envoyée',
  ECHEC: 'Échec',
  LU: 'Lue'
};

export const STATUT_NOTIFICATION_COLORS: Record<string, string> = {
  EN_ATTENTE: 'alerte',
  ENVOYE: 'info',
  ECHEC: 'danger',
  LU: 'neutre'
};

export const CANAL_LABELS: Record<string, string> = {
  SMS: 'SMS',
  IN_APP: 'In-app',
  EMAIL: 'E-mail',
  PUSH: 'Push'
};
