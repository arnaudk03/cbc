package bf.gov.anptic.sonagess.cbc.paiements.domain;

public enum OrdrePaiementStatut {
    INITIE,
    VALIDE_2FA,
    ENVOYE_OPERATEUR,
    CONFIRME,
    ECHEC,
    REJETE
}
