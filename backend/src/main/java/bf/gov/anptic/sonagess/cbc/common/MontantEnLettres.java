package bf.gov.anptic.sonagess.cbc.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MontantEnLettres {

    private static final String[] UNITES = {
            "zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
            "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize"
    };

    private static final String[] DIZAINES = {
            "", "", "vingt", "trente", "quarante", "cinquante", "soixante"
    };

    private MontantEnLettres() {
    }

    public static String pourMontantCfa(BigDecimal montant) {
        long valeur = montant.setScale(0, RoundingMode.HALF_UP).longValue();
        String lettres = enLettres(valeur);
        return Character.toUpperCase(lettres.charAt(0)) + lettres.substring(1) + " francs CFA";
    }

    public static String enLettres(long nombre) {
        if (nombre == 0) {
            return "zéro";
        }
        if (nombre < 0) {
            return "moins " + enLettres(-nombre);
        }

        StringBuilder resultat = new StringBuilder();
        long milliards = nombre / 1_000_000_000L;
        long millions = (nombre / 1_000_000L) % 1_000L;
        long milliers = (nombre / 1_000L) % 1_000L;
        long reste = nombre % 1_000L;

        if (milliards > 0) {
            resultat.append(milliards == 1 ? "un milliard" : sousMille((int) milliards) + " milliards");
        }
        if (millions > 0) {
            ajouterSeparateur(resultat);
            resultat.append(millions == 1 ? "un million" : sousMille((int) millions) + " millions");
        }
        if (milliers > 0) {
            ajouterSeparateur(resultat);
            if (milliers == 1) {
                resultat.append("mille");
            } else {
                resultat.append(sansPlurielDevantMille(sousMille((int) milliers))).append(" mille");
            }
        }
        if (reste > 0) {
            ajouterSeparateur(resultat);
            resultat.append(sousMille((int) reste));
        }
        return resultat.toString();
    }

    private static String sousMille(int nombre) {
        int centaines = nombre / 100;
        int reste = nombre % 100;
        if (centaines == 0) {
            return sousCent(reste);
        }
        String prefixe;
        if (centaines == 1) {
            prefixe = "cent";
        } else {
            prefixe = UNITES[centaines] + " cent";
            if (reste == 0) {
                prefixe += "s";
            }
        }
        return reste == 0 ? prefixe : prefixe + " " + sousCent(reste);
    }

    private static String sousCent(int nombre) {
        if (nombre < 17) {
            return UNITES[nombre];
        }
        if (nombre < 20) {
            return "dix-" + UNITES[nombre - 10];
        }
        if (nombre < 70) {
            int dizaine = nombre / 10;
            int unite = nombre % 10;
            if (unite == 0) {
                return DIZAINES[dizaine];
            }
            if (unite == 1) {
                return DIZAINES[dizaine] + " et un";
            }
            return DIZAINES[dizaine] + "-" + UNITES[unite];
        }
        if (nombre < 80) {
            int reste = nombre - 60;
            if (reste == 11) {
                return "soixante et onze";
            }
            return "soixante-" + sousCent(reste);
        }
        if (nombre < 100) {
            int reste = nombre - 80;
            if (reste == 0) {
                return "quatre-vingts";
            }
            if (reste < 17) {
                return "quatre-vingt-" + UNITES[reste];
            }
            return "quatre-vingt-" + sousCent(reste);
        }
        return String.valueOf(nombre);
    }

    private static void ajouterSeparateur(StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
    }

    private static String sansPlurielDevantMille(String valeur) {
        if (valeur.endsWith("cents")) {
            return valeur.substring(0, valeur.length() - 1);
        }
        if (valeur.endsWith("quatre-vingts")) {
            return valeur.substring(0, valeur.length() - 1);
        }
        return valeur;
    }
}
