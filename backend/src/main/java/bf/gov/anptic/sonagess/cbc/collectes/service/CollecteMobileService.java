package bf.gov.anptic.sonagess.cbc.collectes.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.auth.domain.Role;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.collectes.domain.BordereauReceptionBrbc;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteBordChamp;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.ControleQualite;
import bf.gov.anptic.sonagess.cbc.collectes.domain.FichePeseeFp;
import bf.gov.anptic.sonagess.cbc.collectes.domain.StatutValidationBrbc;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteDtos;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteMobileDtos;
import bf.gov.anptic.sonagess.cbc.collectes.repository.BordereauReceptionBrbcRepository;
import bf.gov.anptic.sonagess.cbc.collectes.repository.CollecteBordChampRepository;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.MontantEnLettres;
import bf.gov.anptic.sonagess.cbc.common.PageResponse;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TabletteDevice;
import bf.gov.anptic.sonagess.cbc.equipements.repository.TabletteDeviceRepository;
import bf.gov.anptic.sonagess.cbc.notifications.domain.TypeNotification;
import bf.gov.anptic.sonagess.cbc.notifications.service.NotificationService;
import bf.gov.anptic.sonagess.cbc.paiements.domain.ModePaiement;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Locality;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.PrixGarantie;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.CampagneRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.LocalityRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.PrixGarantieRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ProducteurRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.SpeculationRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.service.ReferentielService;
import bf.gov.anptic.sonagess.cbc.security.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollecteMobileService {

    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<String> OPERATEURS_MOBILE_MONEY = List.of(
            "SANK", "CORIS", "MOOV", "LIGDICASH", "ORANGE"
    );

    private final CollecteBordChampRepository collecteRepository;
    private final BordereauReceptionBrbcRepository bordereauRepository;
    private final CampagneRepository campagneRepository;
    private final ComptoirRepository comptoirRepository;
    private final ProducteurRepository producteurRepository;
    private final SpeculationRepository speculationRepository;
    private final PrixGarantieRepository prixGarantieRepository;
    private final LocalityRepository localityRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TabletteDeviceRepository tabletteDeviceRepository;
    private final ReferentielService referentielService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // ------------------------------------------------------------------------
    // 1. BOOTSTRAP HORS-LIGNE (SYNC INIT)
    // ------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public CollecteMobileDtos.MobileBootstrapResponse bootstrap(Long agentId) {
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent collecteur introuvable."));

        Campagne campagne = campagneRepository.findFirstByStatutOrderByDateDebutDesc(CampagneStatut.OUVERTE)
                .orElseThrow(() -> new BusinessException("Aucune campagne céréalière active ouverte."));

        Comptoir comptoir = agent.getComptoir();
        if (comptoir == null) {
            comptoir = comptoirRepository.findAllByActifTrueOrderByNomAsc().stream()
                    .findFirst()
                    .orElse(null);
        }

        LocalityRegion locRegion = comptoir != null ? extraireRegion(comptoir) : new LocalityRegion(null, null);

        List<Speculation> specs = speculationRepository.findByActiveTrueOrderByNomAsc();
        List<CollecteMobileDtos.SpeculationMobileDto> specsDto = specs.stream()
                .map(s -> new CollecteMobileDtos.SpeculationMobileDto(
                        s.getId(),
                        s.getCode(),
                        s.getNom(),
                        s.getSeuilHumiditeMax(),
                        s.getSeuilImpureteMax(),
                        s.getActive()
                ))
                .toList();

        List<PrixGarantie> prixList = prixGarantieRepository.findByCampagneIdOrderByDateEffetDesc(campagne.getId());
        List<CollecteMobileDtos.PrixGarantieMobileDto> prixDto = prixList.stream()
                .map(p -> new CollecteMobileDtos.PrixGarantieMobileDto(
                        p.getId(),
                        p.getSpeculation().getId(),
                        p.getSpeculation().getCode(),
                        p.getPrixUnitaireKg(),
                        p.getDateEffet(),
                        p.getStatut() != null ? p.getStatut().name() : "APPROUVE"
                ))
                .toList();

        List<Producteur> producteurs = producteurRepository.findAll();
        List<CollecteMobileDtos.ProducteurMobileDto> producteursDto = producteurs.stream()
                .map(p -> new CollecteMobileDtos.ProducteurMobileDto(
                        p.getId(),
                        p.getNomPrenom(),
                        p.getTelephoneMobileMoney(),
                        p.getOperateurMobile(),
                        p.getCnibNumero(),
                        p.getCnibDateDelivrance(),
                        p.getCnibLieuDelivrance(),
                        p.getLocality() != null ? p.getLocality().getId().toString() : null,
                        p.getLocality() != null ? p.getLocality().getName() : null,
                        p.getAdressePrecision()
                ))
                .toList();

        CollecteMobileDtos.ResumeJournalierMobileDto resume = resumeJournalierInterne(agentId, comptoir != null ? comptoir.getId() : null);

        List<String> rolesList = agent.getRoles() != null
                ? agent.getRoles().stream().map(Role::getCode).toList()
                : List.of();

        return new CollecteMobileDtos.MobileBootstrapResponse(
                new CollecteMobileDtos.AgentMobileDto(
                        agent.getId(),
                        agent.getMatricule(),
                        agent.getNomComplet(),
                        agent.getTelephone(),
                        agent.getEmail(),
                        comptoir != null ? comptoir.getId() : null,
                        comptoir != null ? comptoir.getNom() : null,
                        comptoir != null ? comptoir.getCode() : null,
                        rolesList
                ),
                new CollecteMobileDtos.CampagneMobileDto(
                        campagne.getId(),
                        campagne.getCode(),
                        campagne.getLibelle(),
                        campagne.getDateDebut(),
                        campagne.getDateFin(),
                        campagne.getStatut().name()
                ),
                comptoir != null ? new CollecteMobileDtos.ComptoirMobileDto(
                        comptoir.getId(),
                        comptoir.getCode(),
                        comptoir.getNom(),
                        comptoir.getLocality() != null ? comptoir.getLocality().getId().toString() : null,
                        comptoir.getLocality() != null ? comptoir.getLocality().getName() : null,
                        locRegion.region(),
                        locRegion.province(),
                        comptoir.getCapaciteStockageKg(),
                        comptoir.getActif()
                ) : null,
                specsDto,
                prixDto,
                producteursDto,
                Arrays.stream(ModePaiement.values()).map(Enum::name).toList(),
                OPERATEURS_MOBILE_MONEY,
                resume,
                LocalDateTime.now()
        );
    }

    // ------------------------------------------------------------------------
    // 2. SYNCHRONISATION PAR LOT (BATCH OFFLINE SYNC FLUTTER)
    // ------------------------------------------------------------------------
    @Transactional
    public CollecteMobileDtos.MobileBatchSyncResponse synchroniserBatch(
            CollecteMobileDtos.MobileBatchSyncRequest request, Long agentId) {

        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent collecteur introuvable."));

        if (request.tabletteImei() != null && !request.tabletteImei().isBlank()) {
            tabletteDeviceRepository.findByImei(request.tabletteImei().trim()).ifPresent(tab -> {
                tab.setDateDerniereSynchro(LocalDateTime.now());
                tabletteDeviceRepository.save(tab);
            });
        }

        Map<String, Long> producteursMap = new HashMap<>();
        if (request.nouveauxProducteurs() != null && !request.nouveauxProducteurs().isEmpty()) {
            for (CollecteMobileDtos.ProducteurOfflineRequest prodReq : request.nouveauxProducteurs()) {
                Producteur producteur = producteurRepository.findByTelephoneMobileMoney(prodReq.telephoneMobileMoney())
                        .orElseGet(() -> {
                            Locality locality = null;
                            if (prodReq.localityId() != null && !prodReq.localityId().isBlank()) {
                                try {
                                    UUID locUuid = UUID.fromString(prodReq.localityId());
                                    locality = localityRepository.findById(locUuid).orElse(null);
                                } catch (Exception ignored) {
                                }
                            }
                            Producteur nouveau = Producteur.builder()
                                    .nomPrenom(prodReq.nomPrenom().trim())
                                    .telephoneMobileMoney(prodReq.telephoneMobileMoney().trim())
                                    .operateurMobile(prodReq.operateurMobile().trim())
                                    .cnibNumero(prodReq.cnibNumero().trim().toUpperCase())
                                    .cnibDateDelivrance(prodReq.cnibDateDelivrance())
                                    .cnibLieuDelivrance(prodReq.cnibLieuDelivrance())
                                    .locality(locality)
                                    .adressePrecision(prodReq.adressePrecision())
                                    .build();
                            return producteurRepository.save(nouveau);
                        });
                producteursMap.put(prodReq.clientTempId(), producteur.getId());
            }
        }

        List<CollecteMobileDtos.SyncBordereauResult> resultats = new ArrayList<>();
        int succesCount = 0;
        int erreursCount = 0;

        for (CollecteMobileDtos.BordereauOfflineRequest brbcReq : request.bordereaux()) {
            try {
                CollecteMobileDtos.SyncBordereauResult res = synchroniserUnBordereau(brbcReq, agent, producteursMap);
                resultats.add(res);
                succesCount++;
            } catch (Exception e) {
                log.warn("Erreur synchronisation bordereau clientSyncId={}: {}", brbcReq.clientSyncId(), e.getMessage());
                resultats.add(new CollecteMobileDtos.SyncBordereauResult(
                        brbcReq.clientSyncId(),
                        brbcReq.referenceLocale(),
                        CollecteMobileDtos.StatutSync.REJETE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(),
                        e.getMessage() != null ? e.getMessage() : "Erreur interne lors de la validation du bordereau."
                ));
                erreursCount++;
            }
        }

        return new CollecteMobileDtos.MobileBatchSyncResponse(
                LocalDateTime.now(),
                producteursMap,
                resultats,
                request.bordereaux().size(),
                succesCount,
                erreursCount
        );
    }

    private CollecteMobileDtos.SyncBordereauResult synchroniserUnBordereau(
            CollecteMobileDtos.BordereauOfflineRequest request,
            Utilisateur agent,
            Map<String, Long> producteursMap) {

        // Idempotence : si déjà synchronisé avec ce clientSyncId, retourner la réponse sans duplication
        Optional<BordereauReceptionBrbc> existant = bordereauRepository.findByClientSyncId(request.clientSyncId());
        if (existant.isPresent()) {
            BordereauReceptionBrbc b = existant.get();
            List<CollecteMobileDtos.SyncLigneResult> lignes = b.getCollectes().stream()
                    .map(c -> new CollecteMobileDtos.SyncLigneResult(
                            c.getClientLigneId(),
                            c.getId(),
                            c.getReferenceLot(),
                            c.getSpeculation().getCode(),
                            c.getQuantiteKg(),
                            c.getPrixUnitaireKg(),
                            c.getMontantNetCfa()
                    ))
                    .toList();
            return new CollecteMobileDtos.SyncBordereauResult(
                    request.clientSyncId(),
                    request.referenceLocale(),
                    CollecteMobileDtos.StatutSync.DEJA_SYNCHRONISE,
                    b.getId(),
                    b.getNumeroBrbc(),
                    b.getQuantiteKg(),
                    b.getMontantNetPayerCfa(),
                    b.getMontantEnLettres(),
                    lignes,
                    null
            );
        }

        Campagne campagne = campagneRepository.findById(request.campagneId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        if (campagne.getStatut() != CampagneStatut.OUVERTE) {
            throw new BusinessException("La campagne sélectionnée n'est pas ouverte à la collecte.");
        }

        Long comptoirId = agent.getComptoir() != null ? agent.getComptoir().getId() : request.comptoirId();
        if (comptoirId == null) {
            throw new BusinessException("Le comptoir est obligatoire.");
        }
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        if (!Boolean.TRUE.equals(comptoir.getActif())) {
            throw new BusinessException("Le comptoir sélectionné est fermé.");
        }

        Long prodId = request.producteurId();
        if (prodId == null && request.clientTempProducteurId() != null) {
            prodId = producteursMap.get(request.clientTempProducteurId());
        }
        if (prodId == null) {
            throw new BusinessException("Producteur introuvable pour ce bordereau.");
        }
        final Long finalProdId = prodId;
        Producteur producteur = producteurRepository.findById(finalProdId)
                .orElseThrow(() -> new ResourceNotFoundException("Producteur ID " + finalProdId + " introuvable."));

        LocalDateTime dateCollecte = request.dateHeureCollecte() != null ? request.dateHeureCollecte() : LocalDateTime.now();
        LocalDate dateJour = dateCollecte.toLocalDate();

        List<CollecteMobileDtos.LigneCollecteOfflineRequest> lignes = request.lignes();
        Set<Long> speculationsVues = new HashSet<>();
        List<LigneCalculeeOffline> lignesCalculees = new ArrayList<>();
        BigDecimal totalQuantite = BigDecimal.ZERO;
        BigDecimal totalMontant = BigDecimal.ZERO;

        for (int index = 0; index < lignes.size(); index++) {
            CollecteMobileDtos.LigneCollecteOfflineRequest ligne = lignes.get(index);
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

            // Contrôle qualité
            String prefixe = "Ligne " + (index + 1) + " (" + speculation.getNom() + ") : ";
            if (!Boolean.TRUE.equals(ligne.conforme())) {
                throw new BusinessException(prefixe + "le lot est déclaré non conforme par l'agent.");
            }
            if (ligne.tauxHumidite() > speculation.getSeuilHumiditeMax()) {
                throw new BusinessException(prefixe + "taux d'humidité (" + ligne.tauxHumidite()
                        + " %) > seuil autorisé (" + speculation.getSeuilHumiditeMax() + " %).");
            }
            if (ligne.tauxImpuretes() > speculation.getSeuilImpureteMax()) {
                throw new BusinessException(prefixe + "taux d'impuretés (" + ligne.tauxImpuretes()
                        + " %) > tolérance (" + speculation.getSeuilImpureteMax() + " %).");
            }

            double poidsNet = ligne.poidsBrutTotalKg() - ligne.tareEmballageKg();
            if (poidsNet <= 0) {
                throw new BusinessException(prefixe + "poids net nul ou négatif. Vérifiez la pesée.");
            }

            PrixGarantie prixGarantie = referentielService.resoudrePrix(campagne, speculation,
                    comptoir.getLocality(), dateJour);
            BigDecimal quantite = BigDecimal.valueOf(poidsNet).setScale(2, RoundingMode.HALF_UP);
            BigDecimal montant = quantite.multiply(BigDecimal.valueOf(prixGarantie.getPrixUnitaireKg()))
                    .setScale(2, RoundingMode.HALF_UP);

            lignesCalculees.add(new LigneCalculeeOffline(ligne, speculation, prixGarantie, quantite, montant));
            totalQuantite = totalQuantite.add(quantite);
            totalMontant = totalMontant.add(montant);
        }

        totalQuantite = totalQuantite.setScale(2, RoundingMode.HALF_UP);
        totalMontant = totalMontant.setScale(2, RoundingMode.HALF_UP);

        String suffixeBrbc = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BordereauReceptionBrbc bordereau = BordereauReceptionBrbc.builder()
                .numeroBrbc("BRBC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + suffixeBrbc)
                .clientSyncId(request.clientSyncId())
                .dateEmission(dateCollecte)
                .quantiteKg(totalQuantite.doubleValue())
                .prixUnitaireApplique(totalQuantite.signum() > 0
                        ? totalMontant.divide(totalQuantite, 2, RoundingMode.HALF_UP).doubleValue()
                        : 0.0)
                .montantNetPayerCfa(totalMontant.doubleValue())
                .montantEnLettres(MontantEnLettres.pourMontantCfa(totalMontant))
                .modePaiement(request.modePaiement() != null ? request.modePaiement() : ModePaiement.MOBILE_MONEY)
                .observationsReceptionnaire(request.observationsReceptionnaire())
                .signatureProducteurToken(request.signatureProducteurToken())
                .signatureAgentToken(request.signatureAgentToken())
                .statutValidation(StatutValidationBrbc.EN_ATTENTE_EXAMEN)
                .build();

        List<CollecteBordChamp> collectes = new ArrayList<>();
        List<CollecteMobileDtos.SyncLigneResult> syncLignes = new ArrayList<>();

        for (LigneCalculeeOffline calculee : lignesCalculees) {
            String suffixe = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String referenceLot = "LOT-" + campagne.getCode() + "-"
                    + dateCollecte.format(HORODATAGE) + "-" + suffixe;
            String numeroFiche = "FP-" + dateCollecte.format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + suffixe.substring(0, 6);

            CollecteBordChamp collecte = CollecteBordChamp.builder()
                    .clientLigneId(calculee.ligne().clientLigneId())
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
                    .grainsAvaries(calculee.ligne().grainsAvaries() != null ? calculee.ligne().grainsAvaries() : 0.0)
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

        for (CollecteBordChamp c : collectes) {
            syncLignes.add(new CollecteMobileDtos.SyncLigneResult(
                    c.getClientLigneId(),
                    c.getId(),
                    c.getReferenceLot(),
                    c.getSpeculation().getCode(),
                    c.getQuantiteKg(),
                    c.getPrixUnitaireKg(),
                    c.getMontantNetCfa()
            ));
        }

        CollecteBordChamp principale = collectes.get(0);
        String lots = collectes.stream().map(CollecteBordChamp::getReferenceLot).collect(Collectors.joining(", "));
        notificationService.envoyerSmsProducteur(producteur,
                "BRBC enregistré",
                "Votre BRBC " + bordereau.getNumeroBrbc() + " (" + collectes.size()
                        + " spéculation(s)) est enregistré. Lots : " + lots
                        + ". Montant : " + totalMontant.toPlainString() + " F CFA.",
                TypeNotification.CONFIRMATION_BRBC, principale, null);

        auditService.succes(agent, "SYNCHRO_MOBILE_BRBC", "BordereauReceptionBrbc",
                bordereau.getId(),
                "BRBC " + bordereau.getNumeroBrbc() + " synchronisé depuis tablette Flutter ("
                        + collectes.size() + " lot(s) - " + totalQuantite.toPlainString() + " kg)");

        return new CollecteMobileDtos.SyncBordereauResult(
                request.clientSyncId(),
                request.referenceLocale(),
                CollecteMobileDtos.StatutSync.SYNCHRONISE,
                bordereau.getId(),
                bordereau.getNumeroBrbc(),
                bordereau.getQuantiteKg(),
                bordereau.getMontantNetPayerCfa(),
                bordereau.getMontantEnLettres(),
                syncLignes,
                null
        );
    }

    // ------------------------------------------------------------------------
    // 3. TABLEAU DE BORD & STATISTIQUES MOBILES
    // ------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public CollecteMobileDtos.ResumeJournalierMobileDto resumeJournalier(Long agentId) {
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent introuvable."));
        Long comptoirId = agent.getComptoir() != null ? agent.getComptoir().getId() : null;
        return resumeJournalierInterne(agentId, comptoirId);
    }

    private CollecteMobileDtos.ResumeJournalierMobileDto resumeJournalierInterne(Long agentId, Long comptoirId) {
        LocalDate today = LocalDate.now();
        LocalDateTime debut = today.atStartOfDay();
        LocalDateTime fin = today.atTime(LocalTime.MAX);

        List<CollecteBordChamp> list = collecteRepository.findCollectesJournee(agentId, comptoirId, debut, fin);

        double totalKg = 0.0;
        double totalMontant = 0.0;
        long synchro = 0;
        long examine = 0;
        long valide = 0;
        long paye = 0;
        long rejete = 0;

        Set<Long> bordereauxVus = new HashSet<>();
        Map<String, SpeculationCumul> parSpec = new HashMap<>();

        for (CollecteBordChamp c : list) {
            if (c.getBordereauReception() != null) {
                bordereauxVus.add(c.getBordereauReception().getId());
            }
            totalKg += c.getQuantiteKg() != null ? c.getQuantiteKg() : 0.0;
            totalMontant += c.getMontantNetCfa() != null ? c.getMontantNetCfa() : 0.0;

            if (c.getStatut() != null) {
                switch (c.getStatut()) {
                    case ENREGISTREE, SYNCHRONISEE -> synchro++;
                    case EXAMINEE_SCP -> examine++;
                    case VALIDEE_DCT -> valide++;
                    case REJETEE -> rejete++;
                }
            }

            Speculation sp = c.getSpeculation();
            SpeculationCumul cumul = parSpec.computeIfAbsent(sp.getCode(), k -> new SpeculationCumul(sp.getCode(), sp.getNom()));
            cumul.ajouter(c.getQuantiteKg(), c.getMontantNetCfa());
        }

        List<CollecteMobileDtos.QuantiteParSpeculationDto> specDtos = parSpec.values().stream()
                .map(sc -> new CollecteMobileDtos.QuantiteParSpeculationDto(
                        sc.code,
                        sc.nom,
                        Math.round(sc.quantiteKg * 100.0) / 100.0,
                        Math.round(sc.montantCfa * 100.0) / 100.0
                ))
                .toList();

        return new CollecteMobileDtos.ResumeJournalierMobileDto(
                today,
                bordereauxVus.size(),
                list.size(),
                Math.round(totalKg * 100.0) / 100.0,
                Math.round(totalMontant * 100.0) / 100.0,
                specDtos,
                synchro,
                examine,
                valide,
                rejete
        );
    }

    // ------------------------------------------------------------------------
    // 4. CONSULTATION DES COLLECTES DE L'AGENT
    // ------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public PageResponse<CollecteDtos.CollecteResponse> mesCollectes(
            Long agentId, LocalDate date, CollecteStatut statut, Pageable pageable) {
        LocalDateTime dateDebut = date != null ? date.atStartOfDay() : null;
        LocalDateTime dateFin = date != null ? date.atTime(LocalTime.MAX) : null;

        Page<CollecteBordChamp> page = collecteRepository.searchAgentCollectes(agentId, dateDebut, dateFin, statut, pageable);
        return PageResponse.from(page, CollecteDtos.CollecteResponse::from);
    }

    // ------------------------------------------------------------------------
    // 5. TICKET D'IMPRESSION BLUETOOTH ESC/POS
    // ------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public CollecteMobileDtos.TicketBrbcDto genererTicket(Long bordereauId, Long agentId) {
        BordereauReceptionBrbc brbc = bordereauRepository.findById(bordereauId)
                .orElseThrow(() -> new ResourceNotFoundException("Bordereau BRBC introuvable."));

        CollecteBordChamp premiere = brbc.getCollectePrincipale();
        if (premiere == null) {
            throw new BusinessException("Ce bordereau ne contient aucune collecte.");
        }

        Comptoir comptoir = premiere.getComptoir();
        Producteur producteur = premiere.getProducteur();
        Utilisateur agent = premiere.getAgent();
        LocalityRegion region = extraireRegion(comptoir);

        List<CollecteMobileDtos.TicketLigneDto> lignes = new ArrayList<>();
        double totalPoidsBrut = 0.0;
        double totalTare = 0.0;
        double totalPoidsNet = 0.0;

        for (CollecteBordChamp c : brbc.getCollectes()) {
            FichePeseeFp fp = c.getFichePesee();
            ControleQualite cq = c.getControleQualite();
            int sacs = fp != null && fp.getNombreSacs() != null ? fp.getNombreSacs() : 0;
            double brut = fp != null && fp.getPoidsBrutTotalKg() != null ? fp.getPoidsBrutTotalKg() : c.getQuantiteKg();
            double tare = fp != null && fp.getTareEmballageKg() != null ? fp.getTareEmballageKg() : 0.0;
            double net = c.getQuantiteKg() != null ? c.getQuantiteKg() : (brut - tare);

            totalPoidsBrut += brut;
            totalTare += tare;
            totalPoidsNet += net;

            lignes.add(new CollecteMobileDtos.TicketLigneDto(
                    c.getSpeculation().getNom(),
                    c.getSpeculation().getCode(),
                    c.getReferenceLot(),
                    sacs,
                    brut,
                    tare,
                    net,
                    cq != null ? cq.getTauxHumidite() : 0.0,
                    cq != null ? cq.getTauxImpuretes() : 0.0,
                    c.getPrixUnitaireKg(),
                    c.getMontantNetCfa(),
                    cq != null && Boolean.TRUE.equals(cq.getConforme()) ? "CONFORME" : "NON CONFORME"
            ));
        }

        String qrPayload = "SONAGESS|BRBC:" + brbc.getNumeroBrbc()
                + "|PROD:" + producteur.getNomPrenom()
                + "|TEL:" + producteur.getTelephoneMobileMoney()
                + "|NET_KG:" + totalPoidsNet
                + "|MONTANT_CFA:" + brbc.getMontantNetPayerCfa()
                + "|DATE:" + brbc.getDateEmission().format(DateTimeFormatter.ISO_LOCAL_DATE);

        return new CollecteMobileDtos.TicketBrbcDto(
                "BURKINA FASO\nUnite - Progres - Justice",
                "SONAGESS - Stock de Securite Alimentaire",
                "BORDEREAU DE RECEPTION BORD CHAMP",
                brbc.getNumeroBrbc(),
                brbc.getDateEmission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                comptoir.getNom(),
                region.region() != null ? region.region() : "—",
                region.province() != null ? region.province() : "—",
                agent != null ? agent.getMatricule() : "—",
                agent != null ? agent.getNomComplet() : "—",
                producteur.getNomPrenom(),
                producteur.getTelephoneMobileMoney() + " (" + producteur.getOperateurMobile() + ")",
                producteur.getCnibNumero(),
                brbc.getModePaiement() != null ? brbc.getModePaiement().name() : "MOBILE_MONEY",
                lignes,
                Math.round(totalPoidsBrut * 100.0) / 100.0,
                Math.round(totalTare * 100.0) / 100.0,
                Math.round(totalPoidsNet * 100.0) / 100.0,
                brbc.getMontantNetPayerCfa(),
                brbc.getMontantEnLettres(),
                brbc.getObservationsReceptionnaire(),
                "Document officiel SONAGESS emis sur bascule certifiee. Reglement securise Mobile Money apres validation DCT.",
                qrPayload
        );
    }

    // ------------------------------------------------------------------------
    // 6. ENRÔLEMENT PRODUCTEUR DIRECT DEPUIS LE TERRAIN (EN LIGNE)
    // ------------------------------------------------------------------------
    @Transactional
    public CollecteMobileDtos.ProducteurMobileDto creerProducteurDirect(
            CollecteMobileDtos.ProducteurOfflineRequest request, Long agentId) {
        Producteur p = producteurRepository.findByTelephoneMobileMoney(request.telephoneMobileMoney())
                .orElseGet(() -> {
                    Locality locality = null;
                    if (request.localityId() != null && !request.localityId().isBlank()) {
                        try {
                            locality = localityRepository.findById(UUID.fromString(request.localityId())).orElse(null);
                        } catch (Exception ignored) {
                        }
                    }
                    Producteur nouveau = Producteur.builder()
                            .nomPrenom(request.nomPrenom().trim())
                            .telephoneMobileMoney(request.telephoneMobileMoney().trim())
                            .operateurMobile(request.operateurMobile().trim())
                            .cnibNumero(request.cnibNumero().trim().toUpperCase())
                            .cnibDateDelivrance(request.cnibDateDelivrance())
                            .cnibLieuDelivrance(request.cnibLieuDelivrance())
                            .locality(locality)
                            .adressePrecision(request.adressePrecision())
                            .build();
                    return producteurRepository.save(nouveau);
                });

        return new CollecteMobileDtos.ProducteurMobileDto(
                p.getId(),
                p.getNomPrenom(),
                p.getTelephoneMobileMoney(),
                p.getOperateurMobile(),
                p.getCnibNumero(),
                p.getCnibDateDelivrance(),
                p.getCnibLieuDelivrance(),
                p.getLocality() != null ? p.getLocality().getId().toString() : null,
                p.getLocality() != null ? p.getLocality().getName() : null,
                p.getAdressePrecision()
        );
    }

    // ------------------------------------------------------------------------
    // HELPERS INTERNES
    // ------------------------------------------------------------------------
    private LocalityRegion extraireRegion(Comptoir comptoir) {
        Locality locality = comptoir.getLocality();
        String region = null;
        String province = null;
        Locality courant = locality;
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

    private record LocalityRegion(String region, String province) {}

    private record LigneCalculeeOffline(
            CollecteMobileDtos.LigneCollecteOfflineRequest ligne,
            Speculation speculation,
            PrixGarantie prixGarantie,
            BigDecimal quantite,
            BigDecimal montant
    ) {}

    private static class SpeculationCumul {
        final String code;
        final String nom;
        double quantiteKg = 0.0;
        double montantCfa = 0.0;

        SpeculationCumul(String code, String nom) {
            this.code = code;
            this.nom = nom;
        }

        void ajouter(Double kg, Double cfa) {
            if (kg != null) this.quantiteKg += kg;
            if (cfa != null) this.montantCfa += cfa;
        }
    }
}
