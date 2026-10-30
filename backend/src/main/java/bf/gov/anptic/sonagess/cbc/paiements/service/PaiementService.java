package bf.gov.anptic.sonagess.cbc.paiements.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.config.OtpProperties;
import bf.gov.anptic.sonagess.cbc.collectes.domain.BordereauReceptionBrbc;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiement;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.paiements.domain.ModePaiement;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiementStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.StatutValidationBrbc;
import bf.gov.anptic.sonagess.cbc.notifications.domain.TypeNotification;
import bf.gov.anptic.sonagess.cbc.collectes.repository.BordereauReceptionBrbcRepository;
import bf.gov.anptic.sonagess.cbc.paiements.repository.OrdrePaiementRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.security.Roles;
import bf.gov.anptic.sonagess.cbc.security.TwoFactorService;
import bf.gov.anptic.sonagess.cbc.paiements.dto.PaiementDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import bf.gov.anptic.sonagess.cbc.notifications.service.NotificationService;
@Service
@RequiredArgsConstructor
public class PaiementService {

    private final OrdrePaiementRepository ordrePaiementRepository;
    private final BordereauReceptionBrbcRepository bordereauReceptionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TwoFactorService twoFactorService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OtpProperties otpProperties;

    @Transactional
    public PaiementDtos.PaiementResponse initier(PaiementDtos.InitierPaiementRequest request,
                                                 Long initiateurId) {
        BordereauReceptionBrbc bordereau = bordereauReceptionRepository.findById(request.bordereauId())
                .orElseThrow(() -> new ResourceNotFoundException("Bordereau de réception introuvable."));

        if (bordereau.getStatutValidation() != StatutValidationBrbc.VALIDE_DCT) {
            throw new BusinessException("Seul un BRBC validé par la DCT peut être mis en paiement.");
        }
        if (ordrePaiementRepository.existsByBordereauReceptionId(bordereau.getId())) {
            throw new BusinessException("Un ordre de paiement existe déjà pour ce BRBC.", HttpStatus.CONFLICT);
        }

        Utilisateur initiateur = utilisateurRepository.findById(initiateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiateur introuvable."));
        var producteur = bordereau.getCollectePrincipale().getProducteur();

        String operateur = bordereau.getModePaiement() == ModePaiement.MOBILE_MONEY
                ? producteur.getOperateurMobile()
                : bordereau.getModePaiement().name();

        OrdrePaiement paiement = OrdrePaiement.builder()
                .bordereauReception(bordereau)
                .referenceTransaction("PAY-" + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-"
                        + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .montantPayeCfa(bordereau.getMontantNetPayerCfa())
                .operateur(operateur)
                .telephoneDestination(producteur.getTelephoneMobileMoney())
                .initiateur(initiateur)
                .dateInitiation(LocalDateTime.now())
                .statut(OrdrePaiementStatut.INITIE)
                .build();
        ordrePaiementRepository.save(paiement);

        utilisateurRepository.findByRoles_Code(Roles.SFB_CHEF).forEach(chef ->
                notificationService.envoyerInApp(chef,
                        "Paiement à valider",
                        "L'ordre " + paiement.getReferenceTransaction() + " de "
                                + paiement.getMontantPayeCfa() + " F CFA attend votre validation 2FA.",
                        TypeNotification.DEMANDE_VALIDATION_PAIEMENT, bordereau.getCollectePrincipale(), paiement, null));

        auditService.succes(initiateur, "INITIATION_PAIEMENT", "OrdrePaiement", paiement.getId(),
                "Ordre " + paiement.getReferenceTransaction() + " - " + paiement.getMontantPayeCfa() + " F CFA");

        return PaiementDtos.PaiementResponse.from(paiement);
    }

    public PaiementDtos.OtpChallengeResponse demanderOtp(Long paiementId, Long utilisateurId) {
        OrdrePaiement paiement = ordrePaiementRepository.findDetailedById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement introuvable."));
        if (paiement.getStatut() != OrdrePaiementStatut.INITIE) {
            throw new BusinessException("Cet ordre de paiement n'est plus en attente de validation.");
        }
        if (paiement.getInitiateur().getId().equals(utilisateurId)) {
            throw new BusinessException(
                    "Séparation des tâches : l'initiateur ne peut pas valider son propre paiement.",
                    HttpStatus.FORBIDDEN);
        }
        Utilisateur validateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Validateur introuvable."));

        String code = twoFactorService.envoyerOtp(validateur,
                "validation du paiement " + paiement.getReferenceTransaction());
        auditService.succes(validateur, "DEMANDE_OTP_PAIEMENT", "OrdrePaiement", paiementId,
                "Challenge OTP envoyé");
        return new PaiementDtos.OtpChallengeResponse(
                "Un code OTP a été envoyé au " + validateur.getTelephone() + ".",
                otpProperties.isDemoMode() ? code : null);
    }

    @Transactional
    public PaiementDtos.PaiementResponse valider(Long paiementId, String codeOtp, Long utilisateurId) {
        OrdrePaiement paiement = ordrePaiementRepository.findDetailedById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement introuvable."));
        if (paiement.getStatut() != OrdrePaiementStatut.INITIE) {
            throw new BusinessException("Cet ordre de paiement n'est plus en attente de validation.");
        }
        if (paiement.getInitiateur().getId().equals(utilisateurId)) {
            throw new BusinessException(
                    "Séparation des tâches : l'initiateur ne peut pas valider son propre paiement.",
                    HttpStatus.FORBIDDEN);
        }
        Utilisateur validateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Validateur introuvable."));

        twoFactorService.verifierOtp(validateur, codeOtp);

        paiement.setValidateur2FA(validateur);
        paiement.setDateValidation2FA(LocalDateTime.now());
        paiement.setStatut(OrdrePaiementStatut.VALIDE_2FA);
        ordrePaiementRepository.save(paiement);

        paiement.setStatut(OrdrePaiementStatut.ENVOYE_OPERATEUR);
        paiement.setReferenceOperateur("OP-" + paiement.getOperateur() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        paiement.setMessageRetourOperateur("Ordre transmis à l'opérateur " + paiement.getOperateur());
        ordrePaiementRepository.save(paiement);

        paiement.setStatut(OrdrePaiementStatut.CONFIRME);
        paiement.setDateConfirmation(LocalDateTime.now());
        paiement.setMessageRetourOperateur("Paiement confirmé par l'opérateur (simulation de passerelle).");
        ordrePaiementRepository.save(paiement);

        notificationService.envoyerSmsProducteur(paiement.getBordereauReception().getCollectePrincipale().getProducteur(),
                "Paiement mobile reçu",
                "Un paiement de " + paiement.getMontantPayeCfa() + " F CFA a été crédité via "
                        + paiement.getOperateur() + ". Référence : " + paiement.getReferenceTransaction() + ".",
                TypeNotification.PAIEMENT_MOBILE_RECU,
                paiement.getBordereauReception().getCollectePrincipale(), paiement);

        auditService.succes(validateur, "VALIDATION_PAIEMENT", "OrdrePaiement", paiementId,
                "Ordre " + paiement.getReferenceTransaction() + " confirmé - Réf. opérateur "
                        + paiement.getReferenceOperateur());

        return PaiementDtos.PaiementResponse.from(paiement);
    }

    @Transactional
    public PaiementDtos.PaiementResponse rejeter(Long paiementId, String motif, Long utilisateurId) {
        OrdrePaiement paiement = ordrePaiementRepository.findDetailedById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement introuvable."));
        if (paiement.getStatut() != OrdrePaiementStatut.INITIE) {
            throw new BusinessException("Seul un paiement initié peut être rejeté.");
        }
        Utilisateur acteur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        paiement.setStatut(OrdrePaiementStatut.REJETE);
        paiement.setMotifRejet(motif);
        paiement.setValidateur2FA(acteur);
        ordrePaiementRepository.save(paiement);

        notificationService.envoyerInApp(paiement.getInitiateur(),
                "Paiement rejeté",
                "L'ordre " + paiement.getReferenceTransaction() + " a été rejeté. Motif : " + motif,
                TypeNotification.DEMANDE_VALIDATION_PAIEMENT,
                paiement.getBordereauReception().getCollectePrincipale(), paiement, null);
        auditService.echec(acteur, "REJET_PAIEMENT", "OrdrePaiement", paiementId, "Motif : " + motif);
        return PaiementDtos.PaiementResponse.from(paiement);
    }

    @Transactional(readOnly = true)
    public List<PaiementDtos.PaiementResponse> lister(OrdrePaiementStatut statut) {
        List<OrdrePaiement> paiements = statut != null
                ? ordrePaiementRepository.findByStatutOrderByDateInitiationDesc(statut)
                : ordrePaiementRepository.findAllByOrderByDateInitiationDesc();
        return paiements.stream().map(PaiementDtos.PaiementResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PaiementDtos.PaiementResponse detail(Long id) {
        return ordrePaiementRepository.findDetailedById(id)
                .map(PaiementDtos.PaiementResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement introuvable."));
    }
}
