package bf.gov.anptic.sonagess.cbc.collectes.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.MontantEnLettres;
import bf.gov.anptic.sonagess.cbc.common.PageResponse;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.collectes.domain.BordereauReceptionBrbc;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteBordChamp;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.collectes.domain.ControleQualite;
import bf.gov.anptic.sonagess.cbc.collectes.domain.FichePeseeFp;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.PrixGarantie;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.StatutValidationBrbc;
import bf.gov.anptic.sonagess.cbc.notifications.domain.TypeNotification;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.CampagneRepository;
import bf.gov.anptic.sonagess.cbc.collectes.repository.BordereauReceptionBrbcRepository;
import bf.gov.anptic.sonagess.cbc.collectes.repository.CollecteBordChampRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ProducteurRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.SpeculationRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.security.Roles;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import bf.gov.anptic.sonagess.cbc.notifications.service.NotificationService;
import bf.gov.anptic.sonagess.cbc.referentiels.service.ReferentielService;

@Service
@RequiredArgsConstructor
public class CollecteService {

    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CollecteBordChampRepository collecteRepository;
    private final BordereauReceptionBrbcRepository bordereauRepository;
    private final CampagneRepository campagneRepository;
    private final ComptoirRepository comptoirRepository;
    private final ProducteurRepository producteurRepository;
    private final SpeculationRepository speculationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ReferentielService referentielService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public CollecteDtos.CollecteResponse enregistrer(CollecteDtos.CreateCollecteRequest request, Long agentId) {
        CollecteDtos.CreateCollecteLotRequest lot = new CollecteDtos.CreateCollecteLotRequest(
                request.campagneId(),
                request.comptoirId(),
                request.producteurId(),
                request.modePaiement(),
                request.observationsReceptionnaire(),
                request.latitudeGps(),
                request.longitudeGps(),
                List.of(new CollecteDtos.LigneCollecteRequest(
                        request.speculationId(),
                        request.quantiteKg(),
                        request.tauxHumidite(),
                        request.tauxImpuretes(),
                        request.grainsAvaries(),
                        request.conforme(),
                        request.decisionAgent(),
                        request.motifRejet(),
                        request.nombreSacs(),
                        request.poidsBrutTotalKg(),
                        request.tareEmballageKg()
                ))
        );
        return enregistrerLot(lot, agentId).collectes().get(0);
    }

    @Transactional
    public CollecteDtos.CollecteLotResponse enregistrerLot(CollecteDtos.CreateCollecteLotRequest request,
                                                           Long agentId) {
        Campagne campagne = campagneRepository.findById(request.campagneId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        if (campagne.getStatut() != CampagneStatut.OUVERTE) {
            throw new BusinessException("La campagne sélectionnée n'est pas ouverte à la collecte.");
        }
        LocalDate aujourdHui = LocalDate.now();
        if (aujourdHui.isBefore(campagne.getDateDebut()) || aujourdHui.isAfter(campagne.getDateFin())) {
            throw new BusinessException("La date de collecte est en dehors de la période de la campagne.");
        }

        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent collecteur introuvable."));
        Long comptoirImpose = idComptoirImpose(agent);
        Long comptoirId = comptoirImpose != null ? comptoirImpose : request.comptoirId();
        if (comptoirId == null) {
            throw new BusinessException("Le comptoir est obligatoire.");
        }
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        if (!Boolean.TRUE.equals(comptoir.getActif())) {
            throw new BusinessException("Le comptoir sélectionné est fermé.");
        }

        Producteur producteur = producteurRepository.findById(request.producteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Producteur introuvable."));

        LocalDateTime dateCollecte = LocalDateTime.now();
        List<CollecteDtos.LigneCollecteRequest> lignes = request.lignes();
        Set<Long> speculationsVues = new HashSet<>();
        List<LigneCalculee> lignesCalculees = new ArrayList<>();
        BigDecimal totalQuantite = BigDecimal.ZERO;
        BigDecimal totalMontant = BigDecimal.ZERO;

        for (int index = 0; index < lignes.size(); index++) {
            CollecteDtos.LigneCollecteRequest ligne = lignes.get(index);
            if (!speculationsVues.add(ligne.speculationId())) {
                throw new BusinessException("Ligne " + (index + 1)
                        + " : chaque spéculation ne peut apparaître qu'une seule fois sur un BRBC.");
            }
            Speculation speculation = speculationRepository.findById(ligne.speculationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
            if (!Boolean.TRUE.equals(speculation.getActive())) {
                throw new BusinessException("Ligne " + (index + 1)
                        + " : la spéculation sélectionnée n'est plus active.");
            }

            verifierConformiteQualite(ligne, speculation, index);

            double poidsNet = ligne.poidsBrutTotalKg() - ligne.tareEmballageKg();
            if (poidsNet <= 0) {
                throw new BusinessException("Ligne " + (index + 1)
                        + " : le poids net calculé est nul ou négatif. Vérifiez la pesée.");
            }
            double tolerance = Math.max(0.5, poidsNet * 0.005);
            if (Math.abs(poidsNet - ligne.quantiteKg()) > tolerance) {
                throw new BusinessException("Ligne " + (index + 1)
                        + " : la quantité déclarée est incohérente avec le poids net de la pesée.");
            }

            PrixGarantie prixGarantie = referentielService.resoudrePrix(campagne, speculation,
                    comptoir.getLocality(), aujourdHui);
            BigDecimal quantite = BigDecimal.valueOf(poidsNet).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montant = quantite.multiply(BigDecimal.valueOf(prixGarantie.getPrixUnitaireKg()))
                    .setScale(2, RoundingMode.HALF_UP);

            lignesCalculees.add(new LigneCalculee(ligne, speculation, prixGarantie, quantite, montant));
            totalQuantite = totalQuantite.add(quantite);
            totalMontant = totalMontant.add(montant);
        }

        totalQuantite = totalQuantite.setScale(2, RoundingMode.HALF_UP);
        totalMontant = totalMontant.setScale(2, RoundingMode.HALF_UP);

        String suffixeBrbc = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BordereauReceptionBrbc bordereau = BordereauReceptionBrbc.builder()
                .numeroBrbc("BRBC-PRV-" + suffixeBrbc)
                .dateEmission(dateCollecte)
                .quantiteKg(totalQuantite.doubleValue())
                .prixUnitaireApplique(totalQuantite.signum() > 0
                        ? totalMontant.divide(totalQuantite, 2, RoundingMode.HALF_UP).doubleValue()
                        : 0.0)
                .montantNetPayerCfa(totalMontant.doubleValue())
                .montantEnLettres(MontantEnLettres.pourMontantCfa(totalMontant))
                .modePaiement(request.modePaiement())
                .observationsReceptionnaire(request.observationsReceptionnaire())
                .statutValidation(StatutValidationBrbc.EN_ATTENTE_EXAMEN)
                .build();

        List<CollecteBordChamp> collectes = new ArrayList<>();
        for (LigneCalculee calculee : lignesCalculees) {
            String suffixe = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String referenceLot = "LOT-" + campagne.getCode() + "-"
                    + dateCollecte.format(HORODATAGE) + "-" + suffixe;
            String numeroFiche = "FP-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + suffixe.substring(0, 6);

            CollecteBordChamp collecte = CollecteBordChamp.builder()
                    .referenceLot(referenceLot)
                    .campagne(campagne)
                    .comptoir(comptoir)
                    .producteur(producteur)
                    .speculation(calculee.speculation())
                    .agent(agent)
                    .dateHeureCollecte(dateCollecte)
                    .quantiteKg(calculee.quantite().doubleValue())
                    .prixUnitaireKg(calculee.prixGarantie().getPrixUnitaireKg())
                    .montantNetCfa(calculee.montant().doubleValue())
                    .statut(CollecteStatut.SYNCHRONISEE)
                    .latitudeGps(request.latitudeGps())
                    .longitudeGps(request.longitudeGps())
                    .build();

            ControleQualite controle = ControleQualite.builder()
                    .tauxHumidite(calculee.ligne().tauxHumidite())
                    .tauxImpuretes(calculee.ligne().tauxImpuretes())
                    .grainsAvaries(calculee.ligne().grainsAvaries() != null
                            ? calculee.ligne().grainsAvaries() : 0.0)
                    .conforme(true)
                    .decisionAgent(calculee.ligne().decisionAgent())
                    .motifRejet(calculee.ligne().motifRejet())
                    .build();
            collecte.rattacherControleQualite(controle);

            LocalityRegion region = extraireRegion(comptoir);
            FichePeseeFp fiche = FichePeseeFp.builder()
                    .numeroFiche(numeroFiche)
                    .directionRegionale(region.region())
                    .directionProvinciale(region.province())
                    .siteComptoir(comptoir.getNom())
                    .nombreSacs(calculee.ligne().nombreSacs())
                    .poidsBrutTotalKg(calculee.ligne().poidsBrutTotalKg())
                    .tareEmballageKg(calculee.ligne().tareEmballageKg())
                    .poidsNetTotalKg(calculee.quantite().doubleValue())
                    .build();
            collecte.rattacherFichePesee(fiche);
            collecte.rattacherBordereau(bordereau);
            collectes.add(collecte);
        }

        bordereauRepository.saveAndFlush(bordereau);

        CollecteBordChamp principale = collectes.get(0);
        String lots = collectes.stream()
                .map(CollecteBordChamp::getReferenceLot)
                .collect(Collectors.joining(", "));
        notificationService.envoyerSmsProducteur(producteur,
                "BRBC enregistré",
                "Votre BRBC " + bordereau.getNumeroBrbc() + " (" + collectes.size()
                        + " spéculation(s)) est enregistré. Lots : " + lots
                        + ". Montant prévisionnel : " + totalMontant.toPlainString() + " F CFA.",
                TypeNotification.CONFIRMATION_BRBC, principale, null);

        auditService.succes(agent, "ENREGISTREMENT_BRBC", "BordereauReceptionBrbc",
                bordereau.getId(),
                "BRBC " + bordereau.getNumeroBrbc() + " - " + collectes.size() + " lot(s) - "
                        + totalQuantite.toPlainString() + " kg - " + totalMontant.toPlainString()
                        + " F CFA");

        return new CollecteDtos.CollecteLotResponse(
                CollecteDtos.BrbcDto.from(bordereau),
                collectes.stream().map(CollecteDtos.CollecteResponse::from).toList());
    }

    @Transactional
    public CollecteDtos.CollecteResponse examiner(Long collecteId, CollecteDtos.ExamenRequest request,
                                                  Long examinateurId) {
        CollecteBordChamp collecte = collecteRepository.findDetailedById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));
        BordereauReceptionBrbc bordereau = collecte.getBordereauReception();
        List<CollecteBordChamp> lignes = lignesDuBordereau(bordereau, collecte);

        for (CollecteBordChamp ligne : lignes) {
            if (ligne.getStatut() != CollecteStatut.SYNCHRONISEE) {
                throw new BusinessException(
                        "Ce BRBC n'est pas dans l'état permettant son examen.");
            }
        }
        Utilisateur examinateur = utilisateurRepository.findById(examinateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Examinateur introuvable."));
        LocalDateTime maintenant = LocalDateTime.now();

        if (Boolean.TRUE.equals(request.approuve())) {
            for (CollecteBordChamp ligne : lignes) {
                ligne.setExaminateur(examinateur);
                ligne.setDateExamen(maintenant);
                ligne.setStatut(CollecteStatut.EXAMINEE_SCP);
            }
            bordereau.setStatutValidation(StatutValidationBrbc.EXAMINE_SCP);
            auditService.succes(examinateur, "EXAMEN_BRBC", "BordereauReceptionBrbc",
                    bordereau.getId(), "BRBC " + bordereau.getNumeroBrbc() + " - "
                            + lignes.size() + " lot(s) transmis au DCT");
        } else {
            exigerMotif(request.motif());
            for (CollecteBordChamp ligne : lignes) {
                ligne.setExaminateur(examinateur);
                ligne.setDateExamen(maintenant);
                ligne.setStatut(CollecteStatut.REJETEE);
                ligne.setMotifRejet(request.motif());
            }
            bordereau.setStatutValidation(StatutValidationBrbc.REJETE);
            notificationService.envoyerSmsProducteur(collecte.getProducteur(),
                    "BRBC rejeté",
                    "Votre BRBC " + bordereau.getNumeroBrbc() + " (" + lignes.size()
                            + " lot(s)) a été rejeté par le SCP. Motif : " + request.motif(),
                    TypeNotification.REJET_DOSSIER_COLLECTE, collecte, null);
            notificationService.envoyerInApp(collecte.getAgent(),
                    "BRBC rejeté par le SCP",
                    "Le BRBC " + bordereau.getNumeroBrbc() + " (" + lignes.size()
                            + " lot(s)) a été rejeté. Motif : " + request.motif(),
                    TypeNotification.REJET_DOSSIER_COLLECTE, collecte, null, null);
            auditService.echec(examinateur, "REJET_BRBC", "BordereauReceptionBrbc",
                    bordereau.getId(), "Motif : " + request.motif());
        }
        collecteRepository.saveAll(lignes);
        return CollecteDtos.CollecteResponse.from(collecte);
    }

    @Transactional
    public CollecteDtos.CollecteResponse valider(Long collecteId, CollecteDtos.ValidationRequest request,
                                                 Long validateurId) {
        CollecteBordChamp collecte = collecteRepository.findDetailedById(collecteId)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));
        BordereauReceptionBrbc bordereau = collecte.getBordereauReception();
        List<CollecteBordChamp> lignes = lignesDuBordereau(bordereau, collecte);

        for (CollecteBordChamp ligne : lignes) {
            if (ligne.getStatut() != CollecteStatut.EXAMINEE_SCP) {
                throw new BusinessException("Ce BRBC doit d'abord être examiné par le SCP.");
            }
        }
        Utilisateur validateur = utilisateurRepository.findById(validateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Validateur introuvable."));
        boolean examinateurDuBrbc = lignes.stream()
                .anyMatch(ligne -> ligne.getExaminateur() != null
                        && ligne.getExaminateur().getId().equals(validateur.getId()));
        if (examinateurDuBrbc) {
            throw new BusinessException(
                    "Séparation des tâches : l'examinateur ne peut pas valider le BRBC qu'il a examiné.",
                    HttpStatus.FORBIDDEN);
        }

        LocalDateTime maintenant = LocalDateTime.now();
        if (Boolean.TRUE.equals(request.approuve())) {
            String numeroOfficiel = "BRBC-" + LocalDate.now().getYear() + "-"
                    + String.format("%06d", bordereau.getId());
            for (CollecteBordChamp ligne : lignes) {
                ligne.setValidateur(validateur);
                ligne.setDateValidation(maintenant);
                ligne.setStatut(CollecteStatut.VALIDEE_DCT);
            }
            bordereau.setStatutValidation(StatutValidationBrbc.VALIDE_DCT);
            bordereau.setNumeroBrbc(numeroOfficiel);

            notificationService.envoyerSmsProducteur(collecte.getProducteur(),
                    "BRBC validé",
                    "Votre BRBC " + numeroOfficiel + " (" + lignes.size()
                            + " lot(s)) est validé. Montant total : "
                            + bordereau.getMontantNetPayerCfa() + " F CFA. Le paiement sera traité "
                            + "par la DFC.",
                    TypeNotification.VALIDATION_DCT, collecte, null);

            utilisateurRepository.findByRoles_Code(Roles.SFB_INITIATEUR).forEach(destinataire ->
                    notificationService.envoyerInApp(destinataire,
                            "BRBC validé",
                            "Le BRBC " + numeroOfficiel + " (" + lignes.size()
                                    + " lot(s)) est prêt pour initiation du paiement.",
                            TypeNotification.DEMANDE_VALIDATION_PAIEMENT, collecte, null, null));

            auditService.succes(validateur, "VALIDATION_BRBC", "BordereauReceptionBrbc",
                    bordereau.getId(), "BRBC officiel " + numeroOfficiel + " - "
                            + lignes.size() + " lot(s)");
        } else {
            exigerMotif(request.motif());
            for (CollecteBordChamp ligne : lignes) {
                ligne.setValidateur(validateur);
                ligne.setDateValidation(maintenant);
                ligne.setStatut(CollecteStatut.REJETEE);
                ligne.setMotifRejet(request.motif());
            }
            bordereau.setStatutValidation(StatutValidationBrbc.REJETE);
            notificationService.envoyerSmsProducteur(collecte.getProducteur(),
                    "BRBC rejeté",
                    "Votre BRBC " + bordereau.getNumeroBrbc() + " (" + lignes.size()
                            + " lot(s)) a été rejeté par la DCT. Motif : " + request.motif(),
                    TypeNotification.REJET_DOSSIER_COLLECTE, collecte, null);
            notificationService.envoyerInApp(collecte.getAgent(),
                    "BRBC rejeté par la DCT",
                    "Le BRBC " + bordereau.getNumeroBrbc() + " (" + lignes.size()
                            + " lot(s)) a été rejeté. Motif : " + request.motif(),
                    TypeNotification.REJET_DOSSIER_COLLECTE, collecte, null, null);
            auditService.echec(validateur, "REJET_BRBC_DCT", "BordereauReceptionBrbc",
                    bordereau.getId(), "Motif : " + request.motif());
        }
        collecteRepository.saveAll(lignes);
        return CollecteDtos.CollecteResponse.from(collecte);
    }

    @Transactional(readOnly = true)
    public PageResponse<CollecteDtos.CollecteResponse> lister(CollecteStatut statut, Long campagneId,
                                                              Long comptoirId, Long speculationId,
                                                              Pageable pageable, Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        Long comptoirImpose = idComptoirImpose(utilisateur);
        Long comptoirEffectif = comptoirImpose != null ? comptoirImpose : comptoirId;
        Page<CollecteBordChamp> page = collecteRepository.search(statut, campagneId, comptoirEffectif,
                speculationId, pageable);
        return PageResponse.from(page, CollecteDtos.CollecteResponse::from);
    }

    @Transactional(readOnly = true)
    public List<CollecteDtos.CollecteResponse> listerParBordereau(Long bordereauId, Long utilisateurId) {
        List<CollecteBordChamp> lignes = collecteRepository.findDetailedByBordereauId(bordereauId);
        if (lignes.isEmpty()) {
            throw new ResourceNotFoundException("BRBC introuvable.");
        }
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        Long comptoirImpose = idComptoirImpose(utilisateur);
        if (comptoirImpose != null
                && !lignes.get(0).getComptoir().getId().equals(comptoirImpose)) {
            throw new BusinessException("Ce BRBC appartient à un autre comptoir.",
                    HttpStatus.FORBIDDEN);
        }
        return lignes.stream().map(CollecteDtos.CollecteResponse::from).toList();
    }

    private List<CollecteBordChamp> lignesDuBordereau(BordereauReceptionBrbc bordereau,
                                                      CollecteBordChamp collecte) {
        if (bordereau == null) {
            return List.of(collecte);
        }
        List<CollecteBordChamp> lignes = bordereau.getCollectes();
        return lignes == null || lignes.isEmpty() ? List.of(collecte) : lignes;
    }

    private boolean estProfilTerrain(Utilisateur utilisateur) {
        return utilisateur.hasRole("AGENT_COLLECTEUR") || utilisateur.hasRole("CHEF_COMPTOIR");
    }

    private Long idComptoirImpose(Utilisateur utilisateur) {
        if (!estProfilTerrain(utilisateur)) {
            return null;
        }
        if (utilisateur.getComptoir() == null) {
            throw new BusinessException(
                    "Aucun comptoir n'est affecté à votre compte. Contactez l'administrateur.",
                    HttpStatus.FORBIDDEN);
        }
        return utilisateur.getComptoir().getId();
    }

    @Transactional(readOnly = true)
    public CollecteDtos.CollecteResponse detail(Long id, Long utilisateurId) {
        CollecteBordChamp collecte = collecteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collecte introuvable."));
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        Long comptoirImpose = idComptoirImpose(utilisateur);
        if (comptoirImpose != null
                && !collecte.getComptoir().getId().equals(comptoirImpose)) {
            throw new BusinessException("Cette collecte appartient à un autre comptoir.",
                    HttpStatus.FORBIDDEN);
        }
        return CollecteDtos.CollecteResponse.from(collecte);
    }

    private void verifierConformiteQualite(CollecteDtos.LigneCollecteRequest ligne,
                                           Speculation speculation, int index) {
        String prefixe = "Ligne " + (index + 1) + " (" + speculation.getNom() + ") : ";
        if (!Boolean.TRUE.equals(ligne.conforme())) {
            throw new BusinessException(prefixe
                    + "le lot est déclaré non conforme par l'agent. La collecte est refusée.");
        }
        if (ligne.tauxHumidite() > speculation.getSeuilHumiditeMax()) {
            throw new BusinessException(prefixe + "le taux d'humidité (" + ligne.tauxHumidite()
                    + " %) dépasse le seuil autorisé de " + speculation.getSeuilHumiditeMax() + " %.");
        }
        if (ligne.tauxImpuretes() > speculation.getSeuilImpureteMax()) {
            throw new BusinessException(prefixe + "le taux d'impuretés (" + ligne.tauxImpuretes()
                    + " %) dépasse le seuil autorisé de " + speculation.getSeuilImpureteMax() + " %.");
        }
    }

    private void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessException("Le motif de rejet est obligatoire.", HttpStatus.BAD_REQUEST);
        }
    }

    private LocalityRegion extraireRegion(Comptoir comptoir) {
        var locality = comptoir.getLocality();
        String region = null;
        String province = null;
        var courant = locality;
        while (courant != null) {
            if (courant.getType() == LocalityType.REGION) {
                region = courant.getName();
            }
            if (courant.getType() == LocalityType.PROVINCE) {
                province = courant.getName();
            }
            courant = courant.getParent();
        }
        return new LocalityRegion(region, province);
    }

    private record LocalityRegion(String region, String province) {
    }

    private record LigneCalculee(CollecteDtos.LigneCollecteRequest ligne, Speculation speculation,
                                 PrixGarantie prixGarantie, BigDecimal quantite, BigDecimal montant) {
    }
}
