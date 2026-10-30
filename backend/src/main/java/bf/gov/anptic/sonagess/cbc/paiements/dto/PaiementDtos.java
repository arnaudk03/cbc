package bf.gov.anptic.sonagess.cbc.paiements.dto;

import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public final class PaiementDtos {

    private PaiementDtos() {
    }

    public record InitierPaiementRequest(
            @NotNull(message = "Le bordereau de réception est obligatoire") Long bordereauId
    ) {
    }

    public record ValiderPaiementRequest(
            @NotBlank(message = "Le code OTP est obligatoire")
            @Pattern(regexp = "\\d{6}", message = "Le code OTP doit contenir 6 chiffres")
            String codeOtp
    ) {
    }

    public record RejetPaiementRequest(
            @NotBlank(message = "Le motif de rejet est obligatoire") String motif
    ) {
    }

    public record OtpChallengeResponse(
            String message,
            String codeDemo
    ) {
    }

    public record PaiementResponse(
            Long id,
            String referenceTransaction,
            Double montantPayeCfa,
            String operateur,
            String telephoneDestination,
            String initiateur,
            String validateur,
            LocalDateTime dateInitiation,
            LocalDateTime dateValidation2FA,
            LocalDateTime dateConfirmation,
            String statut,
            String referenceOperateur,
            String messageRetourOperateur,
            String motifRejet,
            Long bordereauId,
            String numeroBrbc,
            String collecteReference,
            Integer nombreLots,
            String producteurNom,
            String campagneCode
    ) {
        public static PaiementResponse from(OrdrePaiement paiement) {
            var brbc = paiement.getBordereauReception();
            var collecte = brbc.getCollectePrincipale();
            String collecteReference = brbc.getCollectes() == null ? null
                    : brbc.getCollectes().stream()
                            .map(c -> c.getReferenceLot())
                            .collect(Collectors.joining(" / "));
            return new PaiementResponse(
                    paiement.getId(),
                    paiement.getReferenceTransaction(),
                    paiement.getMontantPayeCfa(),
                    paiement.getOperateur(),
                    paiement.getTelephoneDestination(),
                    paiement.getInitiateur() != null ? paiement.getInitiateur().getNomComplet() : null,
                    paiement.getValidateur2FA() != null ? paiement.getValidateur2FA().getNomComplet() : null,
                    paiement.getDateInitiation(),
                    paiement.getDateValidation2FA(),
                    paiement.getDateConfirmation(),
                    paiement.getStatut().name(),
                    paiement.getReferenceOperateur(),
                    paiement.getMessageRetourOperateur(),
                    paiement.getMotifRejet(),
                    brbc.getId(),
                    brbc.getNumeroBrbc(),
                    collecteReference,
                    brbc.getCollectes() != null ? brbc.getCollectes().size() : 0,
                    collecte != null ? collecte.getProducteur().getNomPrenom() : null,
                    collecte != null ? collecte.getCampagne().getCode() : null
            );
        }
    }
}
