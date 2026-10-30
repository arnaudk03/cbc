package bf.gov.anptic.sonagess.cbc.stocks.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.stocks.domain.BebcLigneEnlevement;
import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauEnlevementBebc;
import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauReceptionMagasin;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockJournaliereFsj;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.MagasinCentral;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutBebc;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutConformiteReception;
import bf.gov.anptic.sonagess.cbc.notifications.domain.TypeNotification;
import bf.gov.anptic.sonagess.cbc.stocks.repository.BebcLigneEnlevementRepository;
import bf.gov.anptic.sonagess.cbc.stocks.repository.BordereauEnlevementBebcRepository;
import bf.gov.anptic.sonagess.cbc.stocks.repository.BordereauReceptionMagasinRepository;
import bf.gov.anptic.sonagess.cbc.collectes.repository.CollecteBordChampRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.stocks.repository.FicheStockJournaliereFsjRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.MagasinCentralRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.SpeculationRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.security.Roles;
import bf.gov.anptic.sonagess.cbc.stocks.dto.StockDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import bf.gov.anptic.sonagess.cbc.notifications.service.NotificationService;
@Service
@RequiredArgsConstructor
public class StockService {

    private final FicheStockJournaliereFsjRepository ficheStockRepository;
    private final BordereauEnlevementBebcRepository enlevementRepository;
    private final BebcLigneEnlevementRepository bebcLigneRepository;
    private final BordereauReceptionMagasinRepository receptionRepository;
    private final ComptoirRepository comptoirRepository;
    private final MagasinCentralRepository magasinRepository;
    private final SpeculationRepository speculationRepository;
    private final CollecteBordChampRepository collecteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public StockDtos.FsjResponse enregistrerFsj(StockDtos.FsjRequest request, Long utilisateurId) {
        Comptoir comptoir = comptoirRepository.findById(request.comptoirId())
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        Utilisateur acteur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

        FicheStockJournaliereFsj precedente = ficheStockRepository
                .findTopByComptoirIdAndDateFicheLessThanEqualOrderByDateFicheDescCreeLeDesc(
                        comptoir.getId(), request.dateFiche())
                .orElse(null);

        int entreeSacs = valeurOuZero(request.entreeSacs());
        double entreePoids = valeurOuZero(request.entreePoidsKg());
        double entreeValeur = valeurOuZero(request.entreeValeurCfa());
        int sortieSacs = valeurOuZero(request.sortieSacs());
        double sortiePoids = valeurOuZero(request.sortiePoidsKg());
        double sortieValeur = valeurOuZero(request.sortieValeurCfa());

        int stockSacs = (precedente != null ? precedente.getStockFinalSacs() : 0) + entreeSacs - sortieSacs;
        double stockPoids = (precedente != null ? precedente.getStockFinalPoidsKg() : 0.0)
                + entreePoids - sortiePoids;
        double stockValeur = (precedente != null ? precedente.getStockFinalValeurCfa() : 0.0)
                + entreeValeur - sortieValeur;

        if (stockSacs < 0 || stockPoids < 0 || stockValeur < 0) {
            throw new BusinessException("Les sorties déclarées rendent le stock négatif. Vérifiez la fiche.");
        }

        FicheStockJournaliereFsj fiche = FicheStockJournaliereFsj.builder()
                .comptoir(comptoir)
                .dateFiche(request.dateFiche())
                .natureOperation(request.natureOperation())
                .entreeSacs(entreeSacs)
                .entreePoidsKg(entreePoids)
                .entreeValeurCfa(entreeValeur)
                .sortieSacs(sortieSacs)
                .sortiePoidsKg(sortiePoids)
                .sortieValeurCfa(sortieValeur)
                .stockFinalSacs(stockSacs)
                .stockFinalPoidsKg(stockPoids)
                .stockFinalValeurCfa(stockValeur)
                .build();
        ficheStockRepository.save(fiche);

        if (stockPoids > 0 && comptoir.getCapaciteStockageKg() != null
                && stockPoids > comptoir.getCapaciteStockageKg()) {
            notificationService.envoyerInApp(acteur,
                    "Alerte capacité comptoir",
                    "Le stock du comptoir " + comptoir.getNom() + " (" + stockPoids
                            + " kg) dépasse sa capacité de " + comptoir.getCapaciteStockageKg() + " kg.",
                    TypeNotification.ALERTE_SEUIL_STOCK, null, null, null);
        }

        auditService.succes(acteur, "TENUE_FICHE_STOCK", "FicheStockJournaliereFsj", fiche.getId(),
                "Fiche du " + request.dateFiche() + " - stock final " + stockPoids + " kg");
        return StockDtos.FsjResponse.from(fiche);
    }

    @Transactional(readOnly = true)
    public List<StockDtos.FsjResponse> listerFsj(Long comptoirId, LocalDate date) {
        List<FicheStockJournaliereFsj> fiches;
        if (date != null) {
            fiches = ficheStockRepository.findByComptoirIdAndDateFicheOrderByCreeLeAsc(comptoirId, date);
        } else {
            LocalDate fin = LocalDate.now();
            fiches = ficheStockRepository.findByComptoirIdAndDateFicheBetweenOrderByDateFicheAscCreeLeAsc(
                    comptoirId, fin.minusDays(30), fin);
        }
        return fiches.stream().map(StockDtos.FsjResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StockDtos.StockDisponibleResponse stockDisponible(Long comptoirId) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        double collecte = valeurOuZero(collecteRepository
                .sumQuantiteValideeParComptoir(comptoirId, CollecteStatut.VALIDEE_DCT));
        double enleve = valeurOuZero(enlevementRepository.sumTonnageEnleveParComptoir(comptoirId));
        Map<Long, Double> quantitesEnlevees = quantitesEnleveesParSpeculation(comptoirId);
        List<StockDtos.SpeculationDisponibleResponse> speculations = new ArrayList<>();
        for (Object[] ligne : collecteRepository.sumQuantiteValideeParComptoirEtSpeculation(
                comptoirId, CollecteStatut.VALIDEE_DCT)) {
            Long speculationId = ((Number) ligne[0]).longValue();
            double quantiteCollectee = ((Number) ligne[3]).doubleValue();
            double quantiteEnlevee = quantitesEnlevees.getOrDefault(speculationId, 0.0);
            speculations.add(new StockDtos.SpeculationDisponibleResponse(
                    speculationId,
                    (String) ligne[1],
                    (String) ligne[2],
                    quantiteCollectee,
                    quantiteEnlevee,
                    Math.max(0, quantiteCollectee - quantiteEnlevee)
            ));
        }
        return new StockDtos.StockDisponibleResponse(
                comptoir.getId(), comptoir.getNom(), collecte, enleve,
                Math.max(0, collecte - enleve), speculations);
    }

    @Transactional
    public StockDtos.TransfertResponse creerTransfert(StockDtos.TransfertRequest request, Long utilisateurId) {
        Utilisateur acteur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        Long comptoirImpose = idComptoirImpose(acteur);
        if (comptoirImpose != null && !comptoirImpose.equals(request.comptoirId())) {
            throw new BusinessException(
                    "Vous ne pouvez créer un enlèvement que depuis votre comptoir affecté.",
                    HttpStatus.FORBIDDEN);
        }
        Comptoir comptoir = comptoirRepository.findById(request.comptoirId())
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        MagasinCentral magasin = magasinRepository.findById(request.magasinDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Magasin de destination introuvable."));
        if (!Boolean.TRUE.equals(magasin.getActif())) {
            throw new BusinessException("Le magasin de destination est inactif.");
        }
        if (request.lignes() == null || request.lignes().isEmpty()) {
            throw new BusinessException("Au moins une spéculation est obligatoire.");
        }

        Map<Long, Double> quantitesCollectees = quantitesCollecteesParSpeculation(comptoir.getId());
        Map<Long, Double> quantitesEnlevees = quantitesEnleveesParSpeculation(comptoir.getId());
        Set<Long> speculationsVues = new HashSet<>();
        List<BebcLigneEnlevement> lignes = new ArrayList<>();
        int totalSacs = 0;
        double totalTonnage = 0.0;

        for (int index = 0; index < request.lignes().size(); index++) {
            StockDtos.TransfertLigneRequest ligneRequest = request.lignes().get(index);
            if (!speculationsVues.add(ligneRequest.speculationId())) {
                throw new BusinessException("Ligne " + (index + 1)
                        + " : chaque spéculation ne peut apparaître qu'une seule fois sur un BEBC.");
            }
            Speculation speculation = speculationRepository.findById(ligneRequest.speculationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
            if (!Boolean.TRUE.equals(speculation.getActive())) {
                throw new BusinessException("Ligne " + (index + 1)
                        + " : la spéculation sélectionnée n'est plus active.");
            }

            double quantite = arrondir(ligneRequest.nombreSacs() * ligneRequest.colisageKg());
            double disponible = Math.max(0,
                    quantitesCollectees.getOrDefault(speculation.getId(), 0.0)
                            - quantitesEnlevees.getOrDefault(speculation.getId(), 0.0));
            if (quantite > disponible + 0.01) {
                throw new BusinessException("Stock insuffisant pour la spéculation "
                        + speculation.getNom() + " : disponible " + disponible
                        + " kg, demandé " + quantite + " kg.", HttpStatus.CONFLICT);
            }

            lignes.add(BebcLigneEnlevement.builder()
                    .speculation(speculation)
                    .nombreSacs(ligneRequest.nombreSacs())
                    .colisageKg(ligneRequest.colisageKg())
                    .quantiteKg(quantite)
                    .build());
            totalSacs += ligneRequest.nombreSacs();
            totalTonnage = arrondir(totalTonnage + quantite);
        }

        double collecteTotale = valeurOuZero(collecteRepository
                .sumQuantiteValideeParComptoir(comptoir.getId(), CollecteStatut.VALIDEE_DCT));
        double enleveTotal = valeurOuZero(
                enlevementRepository.sumTonnageEnleveParComptoir(comptoir.getId()));
        double disponibleTotal = Math.max(0, collecteTotale - enleveTotal);
        if (totalTonnage > disponibleTotal + 0.01) {
            throw new BusinessException("Stock total insuffisant au comptoir : disponible "
                    + disponibleTotal + " kg, demandé " + totalTonnage + " kg.",
                    HttpStatus.CONFLICT);
        }

        String numeroBebc = "BEBC-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BordereauEnlevementBebc bebc = BordereauEnlevementBebc.builder()
                .numeroBebc(numeroBebc)
                .comptoirDepart(comptoir)
                .magasinDestination(magasin)
                .direction(request.direction())
                .localite(request.localite() != null ? request.localite() : magasin.getVille())
                .magasinDepart(request.magasinDepart() != null ? request.magasinDepart() : magasin.getNom())
                .beneficiaire(request.beneficiaire())
                .objetEnlevement(request.objetEnlevement())
                .nombreSacs(totalSacs)
                .tonnageEnleveKg(totalTonnage)
                .nomTransporteur(request.nomTransporteur())
                .numeroVehicule(request.numeroVehicule())
                .statut(StatutBebc.EN_TRANSIT)
                .dateEnlevement(LocalDateTime.now())
                .dateArriveePrevue(request.dateArriveePrevue())
                .build();
        lignes.forEach(bebc::ajouterLigne);
        enlevementRepository.save(bebc);

        utilisateurRepository.findByRoles_Code(Roles.SGS).forEach(destinataire ->
                notificationService.envoyerInApp(destinataire,
                        "Nouveau transfert de stock",
                        "Le BEBC " + numeroBebc + " (" + bebc.getTonnageEnleveKg() + " kg) quitte le comptoir "
                                + comptoir.getNom() + " vers " + magasin.getNom() + ".",
                        TypeNotification.ALERTE_ECART_TRANSFERT, null, null, bebc));

        auditService.succes(acteur, "CREATION_BEBC", "BordereauEnlevementBebc", bebc.getId(),
                "BEBC " + numeroBebc + " - " + totalTonnage + " kg vers " + magasin.getNom());
        return StockDtos.TransfertResponse.from(bebc, null);
    }

    @Transactional
    public StockDtos.TransfertResponse receptionner(Long bebcId, StockDtos.ReceptionRequest request,
                                                    Long utilisateurId) {
        BordereauEnlevementBebc bebc = enlevementRepository.findById(bebcId)
                .orElseThrow(() -> new ResourceNotFoundException("Bordereau d'enlèvement introuvable."));
        if (bebc.getStatut() != StatutBebc.EN_TRANSIT) {
            throw new BusinessException("Ce bordereau n'est pas en transit.");
        }
        if (receptionRepository.findByBordereauEnlevementId(bebcId).isPresent()) {
            throw new BusinessException("Une réception existe déjà pour ce bordereau.", HttpStatus.CONFLICT);
        }
        MagasinCentral magasin = magasinRepository.findById(request.magasinCentralId())
                .orElseThrow(() -> new ResourceNotFoundException("Magasin de réception introuvable."));
        Utilisateur magasinier = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasinier introuvable."));

        double ecart = request.tonnageDechargeKg() - bebc.getTonnageEnleveKg();
        double tolerance = Math.max(0.5, bebc.getTonnageEnleveKg() * 0.02);
        StatutConformiteReception conformite;
        if (ecart < -tolerance) {
            conformite = StatutConformiteReception.MANQUANT;
            bebc.setStatut(StatutBebc.ECART_CONSTATE);
        } else if (ecart > tolerance) {
            conformite = StatutConformiteReception.EXCEDENT;
            bebc.setStatut(StatutBebc.ECART_CONSTATE);
        } else {
            conformite = StatutConformiteReception.CONFORME;
            bebc.setStatut(StatutBebc.LIVRE);
        }
        bebc.setMagasinDestination(magasin);
        enlevementRepository.save(bebc);

        BordereauReceptionMagasin reception = BordereauReceptionMagasin.builder()
                .bordereauEnlevement(bebc)
                .magasinCentral(magasin)
                .magasinier(magasinier)
                .numeroReception("REC-" + LocalDate.now().getYear() + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateDechargement(LocalDateTime.now())
                .nombreSacsRecus(request.nombreSacsRecus())
                .tonnageDechargeKg(request.tonnageDechargeKg())
                .ecartPoidsKg(ecart)
                .statutConformite(conformite)
                .build();
        receptionRepository.save(reception);

        magasin.setStockActuelTonnes(valeurOuZero(magasin.getStockActuelTonnes())
                + request.tonnageDechargeKg() / 1000.0);
        magasinRepository.save(magasin);

        if (conformite != StatutConformiteReception.CONFORME) {
            for (String role : List.of(Roles.SGS, Roles.DGSA)) {
                utilisateurRepository.findByRoles_Code(role).forEach(destinataire ->
                        notificationService.envoyerInApp(destinataire,
                                "Écart constaté à la réception",
                                "Le BEBC " + bebc.getNumeroBebc() + " présente un écart de " + ecart
                                        + " kg (" + conformite.name() + ") au magasin " + magasin.getNom() + ".",
                                TypeNotification.ALERTE_ECART_TRANSFERT, null, null, bebc));
            }
        }

        auditService.succes(magasinier, "RECEPTION_MAGASIN", "BordereauReceptionMagasin", reception.getId(),
                "BEBC " + bebc.getNumeroBebc() + " réceptionné - écart " + ecart + " kg");
        return StockDtos.TransfertResponse.from(bebc, reception);
    }

    @Transactional(readOnly = true)
    public List<StockDtos.TransfertResponse> listerTransferts(StatutBebc statut) {
        List<BordereauEnlevementBebc> bebcList = statut != null
                ? enlevementRepository.findByStatutOrderByCreeLeDesc(statut)
                : enlevementRepository.findAllByOrderByCreeLeDesc();
        return bebcList.stream()
                .map(bebc -> StockDtos.TransfertResponse.from(bebc,
                        receptionRepository.findByBordereauEnlevementId(bebc.getId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public StockDtos.TransfertResponse detailTransfert(Long id) {
        BordereauEnlevementBebc bebc = enlevementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bordereau d'enlèvement introuvable."));
        return StockDtos.TransfertResponse.from(bebc,
                receptionRepository.findByBordereauEnlevementId(id).orElse(null));
    }

    private Map<Long, Double> quantitesCollecteesParSpeculation(Long comptoirId) {
        Map<Long, Double> quantites = new HashMap<>();
        for (Object[] ligne : collecteRepository.sumQuantiteValideeParComptoirEtSpeculation(
                comptoirId, CollecteStatut.VALIDEE_DCT)) {
            quantites.put(((Number) ligne[0]).longValue(), ((Number) ligne[3]).doubleValue());
        }
        return quantites;
    }

    private Map<Long, Double> quantitesEnleveesParSpeculation(Long comptoirId) {
        Map<Long, Double> quantites = new HashMap<>();
        for (Object[] ligne : bebcLigneRepository
                .sumQuantiteEnleveeParComptoirEtSpeculation(comptoirId)) {
            quantites.put(((Number) ligne[0]).longValue(), ((Number) ligne[1]).doubleValue());
        }
        return quantites;
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

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }

    private int valeurOuZero(Integer valeur) {
        return valeur != null ? valeur : 0;
    }

    private double valeurOuZero(Double valeur) {
        return valeur != null ? valeur : 0.0;
    }
}
