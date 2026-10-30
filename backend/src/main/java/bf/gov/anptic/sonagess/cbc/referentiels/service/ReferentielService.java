package bf.gov.anptic.sonagess.cbc.referentiels.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Locality;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.MagasinCentral;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.ObjectifCampagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.PrixGarantie;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.StatutPrixGarantie;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.CampagneRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.LocalityRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.MagasinCentralRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ObjectifCampagneRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.PrixGarantieRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ProducteurRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.SpeculationRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.dto.ReferentielDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferentielService {

    private final CampagneRepository campagneRepository;
    private final SpeculationRepository speculationRepository;
    private final ObjectifCampagneRepository objectifCampagneRepository;
    private final PrixGarantieRepository prixGarantieRepository;
    private final ComptoirRepository comptoirRepository;
    private final ProducteurRepository producteurRepository;
    private final LocalityRepository localityRepository;
    private final MagasinCentralRepository magasinCentralRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;

    // ------------------------------------------------------------------ Campagnes

    @Transactional(readOnly = true)
    public List<ReferentielDtos.CampagneResponse> listerCampagnes() {
        return campagneRepository.findAllByOrderByDateDebutDesc().stream()
                .map(ReferentielDtos.CampagneResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Campagne campagneActive() {
        return campagneRepository.findFirstByStatutOrderByDateDebutDesc(CampagneStatut.OUVERTE)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune campagne ouverte n'est disponible."));
    }

    @Transactional
    public ReferentielDtos.CampagneResponse creerCampagne(ReferentielDtos.CampagneRequest request,
                                                         Utilisateur acteur) {
        if (request.dateFin().isBefore(request.dateDebut())) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début.");
        }
        if (campagneRepository.existsByCode(request.code())) {
            throw new BusinessException("Une campagne avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        Campagne campagne = Campagne.builder()
                .code(request.code().trim().toUpperCase())
                .libelle(request.libelle().trim())
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .objectifTotalTonnes(request.objectifTotalTonnes() != null ? request.objectifTotalTonnes() : 0.0)
                .statut(request.statut() != null ? request.statut() : CampagneStatut.PLANIFIEE)
                .build();
        campagneRepository.save(campagne);
        auditService.succes(acteur, "CREATION_CAMPAGNE", "Campagne", campagne.getId(),
                "Campagne " + campagne.getCode() + " créée");
        return ReferentielDtos.CampagneResponse.from(campagne);
    }

    @Transactional
    public ReferentielDtos.CampagneResponse changerStatutCampagne(Long campagneId, CampagneStatut statut,
                                                                 Utilisateur acteur) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        campagne.setStatut(statut);
        campagneRepository.save(campagne);
        auditService.succes(acteur, "CHANGEMENT_STATUT_CAMPAGNE", "Campagne", campagneId,
                "Statut passé à " + statut);
        return ReferentielDtos.CampagneResponse.from(campagne);
    }

    @Transactional(readOnly = true)
    public List<ReferentielDtos.ObjectifResponse> listerObjectifs(Long campagneId) {
        return objectifCampagneRepository.findByCampagneId(campagneId).stream()
                .map(ReferentielDtos.ObjectifResponse::from)
                .toList();
    }

    @Transactional
    public ReferentielDtos.ObjectifResponse creerObjectif(Long campagneId,
                                                         ReferentielDtos.ObjectifRequest request,
                                                         Utilisateur acteur) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        Speculation speculation = speculationRepository.findById(request.speculationId())
                .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;

        Optional<ObjectifCampagne> existant = locality == null
                ? objectifCampagneRepository.findByCampagneIdAndSpeculationIdAndLocalityIsNull(
                        campagneId, speculation.getId())
                : objectifCampagneRepository.findByCampagneIdAndSpeculationIdAndLocalityId(
                        campagneId, speculation.getId(), locality.getId());
        if (existant.isPresent()) {
            throw new BusinessException("Un objectif existe déjà pour cette campagne, spéculation et zone.",
                    HttpStatus.CONFLICT);
        }

        ObjectifCampagne objectif = ObjectifCampagne.builder()
                .campagne(campagne)
                .speculation(speculation)
                .locality(locality)
                .objectifTonnes(request.objectifTonnes())
                .budgetPrevisionnelCfa(request.budgetPrevisionnelCfa() != null
                        ? request.budgetPrevisionnelCfa() : 0.0)
                .seuilAlertePct(request.seuilAlertePct() != null ? request.seuilAlertePct() : 80.0)
                .build();
        objectifCampagneRepository.save(objectif);
        auditService.succes(acteur, "CREATION_OBJECTIF", "ObjectifCampagne", objectif.getId(),
                "Objectif de " + objectif.getObjectifTonnes() + " t pour " + speculation.getCode());
        return ReferentielDtos.ObjectifResponse.from(objectif);
    }

    // ---------------------------------------------------------------- Spéculations

    @Transactional(readOnly = true)
    public List<ReferentielDtos.SpeculationResponse> listerSpeculations(boolean activesSeulement) {
        List<Speculation> speculations = activesSeulement
                ? speculationRepository.findByActiveTrueOrderByNomAsc()
                : speculationRepository.findAllByOrderByNomAsc();
        return speculations.stream().map(ReferentielDtos.SpeculationResponse::from).toList();
    }

    @Transactional
    public ReferentielDtos.SpeculationResponse creerSpeculation(ReferentielDtos.SpeculationRequest request,
                                                               Utilisateur acteur) {
        if (speculationRepository.existsByCode(request.code())) {
            throw new BusinessException("Une spéculation avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        Speculation speculation = Speculation.builder()
                .code(request.code().trim().toUpperCase())
                .nom(request.nom().trim())
                .seuilHumiditeMax(request.seuilHumiditeMax())
                .seuilImpureteMax(request.seuilImpureteMax())
                .active(request.active() == null || request.active())
                .build();
        speculationRepository.save(speculation);
        auditService.succes(acteur, "CREATION_SPECULATION", "Speculation", speculation.getId(),
                "Spéculation " + speculation.getCode());
        return ReferentielDtos.SpeculationResponse.from(speculation);
    }

    // ---------------------------------------------------------------------- Prix

    @Transactional(readOnly = true)
    public List<ReferentielDtos.PrixGarantieResponse> listerPrix(Long campagneId, Long speculationId) {
        List<PrixGarantie> prix = speculationId != null
                ? prixGarantieRepository.findByCampagneIdAndSpeculationIdOrderByDateEffetDesc(
                        campagneId, speculationId)
                : prixGarantieRepository.findByCampagneIdOrderByDateEffetDesc(campagneId);
        return prix.stream().map(ReferentielDtos.PrixGarantieResponse::from).toList();
    }

    @Transactional
    public ReferentielDtos.PrixGarantieResponse creerPrix(ReferentielDtos.PrixGarantieRequest request,
                                                         Utilisateur acteur) {
        if (request.dateEffet().isBefore(LocalDate.now())) {
            throw new BusinessException("La date d'effet ne peut pas être antérieure à aujourd'hui.");
        }
        Campagne campagne = campagneRepository.findById(request.campagneId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        Speculation speculation = speculationRepository.findById(request.speculationId())
                .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;

        PrixGarantie prix = PrixGarantie.builder()
                .campagne(campagne)
                .speculation(speculation)
                .locality(locality)
                .prixUnitaireKg(request.prixUnitaireKg())
                .dateEffet(request.dateEffet())
                .statut(StatutPrixGarantie.EN_ATTENTE_APPROBATION)
                .build();
        prixGarantieRepository.save(prix);
        auditService.succes(acteur, "CREATION_PRIX_GARANTI", "PrixGarantie", prix.getId(),
                "Prix " + prix.getPrixUnitaireKg() + " CFA/kg pour " + speculation.getCode());
        return ReferentielDtos.PrixGarantieResponse.from(prix);
    }

    @Transactional
    public ReferentielDtos.PrixGarantieResponse approuverPrix(Long prixId, Utilisateur acteur) {
        PrixGarantie prix = prixGarantieRepository.findById(prixId)
                .orElseThrow(() -> new ResourceNotFoundException("Prix garanti introuvable."));
        prix.setStatut(StatutPrixGarantie.APPROUVE);
        prix.setApprouvePar(acteur);
        prix.setDateApprobation(LocalDateTime.now());
        prixGarantieRepository.save(prix);
        auditService.succes(acteur, "APPROBATION_PRIX_GARANTI", "PrixGarantie", prix.getId(),
                "Prix approuvé pour " + prix.getSpeculation().getCode());
        return ReferentielDtos.PrixGarantieResponse.from(prix);
    }

    @Transactional(readOnly = true)
    public PrixGarantie resoudrePrix(Campagne campagne, Speculation speculation, Locality locality,
                                     LocalDate date) {
        Locality courant = locality;
        while (courant != null) {
            Optional<PrixGarantie> prix = prixGarantieRepository
                    .findFirstByCampagneIdAndSpeculationIdAndLocalityIdAndStatutAndDateEffetLessThanEqualOrderByDateEffetDesc(
                            campagne.getId(), speculation.getId(), courant.getId(),
                            StatutPrixGarantie.APPROUVE, date);
            if (prix.isPresent()) {
                return prix.get();
            }
            courant = courant.getParent();
        }
        return prixGarantieRepository
                .findFirstByCampagneIdAndSpeculationIdAndLocalityIsNullAndStatutAndDateEffetLessThanEqualOrderByDateEffetDesc(
                        campagne.getId(), speculation.getId(), StatutPrixGarantie.APPROUVE, date)
                .orElseThrow(() -> new BusinessException(
                        "Aucun prix garanti approuvé n'est en vigueur pour cette spéculation à la date de collecte.",
                        HttpStatus.UNPROCESSABLE_ENTITY));
    }

    // ------------------------------------------------------------------ Comptoirs

    @Transactional(readOnly = true)
    public List<ReferentielDtos.ComptoirResponse> listerComptoirs(boolean actifsSeulement) {
        List<Comptoir> comptoirs = actifsSeulement
                ? comptoirRepository.findAllByActifTrueOrderByNomAsc()
                : comptoirRepository.findAllByOrderByNomAsc();
        return comptoirs.stream().map(ReferentielDtos.ComptoirResponse::from).toList();
    }

    @Transactional
    public ReferentielDtos.ComptoirResponse creerComptoir(ReferentielDtos.ComptoirRequest request,
                                                         Utilisateur acteur) {
        if (comptoirRepository.existsByCode(request.code())) {
            throw new BusinessException("Un comptoir avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        Locality locality = localityRepository.findById(request.localityId())
                .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."));
        Comptoir comptoir = Comptoir.builder()
                .code(request.code().trim().toUpperCase())
                .nom(request.nom().trim())
                .type(request.type() != null ? request.type() : "FIXE")
                .locality(locality)
                .latitudeGps(request.latitudeGps())
                .longitudeGps(request.longitudeGps())
                .capaciteStockageKg(request.capaciteStockageKg() != null
                        ? request.capaciteStockageKg() : 50000.0)
                .actif(request.actif() == null || request.actif())
                .build();
        comptoirRepository.save(comptoir);
        auditService.succes(acteur, "CREATION_COMPTOIR", "Comptoir", comptoir.getId(),
                "Comptoir " + comptoir.getCode());
        return ReferentielDtos.ComptoirResponse.from(comptoir);
    }

    @Transactional
    public ReferentielDtos.ComptoirResponse changerActivationComptoir(Long comptoirId, boolean actif,
                                                                     Utilisateur acteur) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        comptoir.setActif(actif);
        comptoirRepository.save(comptoir);
        auditService.succes(acteur, "ACTIVATION_COMPTOIR", "Comptoir", comptoirId,
                "Comptoir " + (actif ? "ouvert" : "fermé"));
        return ReferentielDtos.ComptoirResponse.from(comptoir);
    }

    // ---------------------------------------------------------------- Producteurs

    @Transactional(readOnly = true)
    public List<ReferentielDtos.ProducteurResponse> rechercherProducteurs(String recherche) {
        List<Producteur> producteurs = (recherche == null || recherche.isBlank())
                ? producteurRepository.findAll()
                : producteurRepository.search(recherche.trim());
        return producteurs.stream().map(ReferentielDtos.ProducteurResponse::from).toList();
    }

    @Transactional
    public ReferentielDtos.ProducteurResponse creerProducteur(ReferentielDtos.ProducteurRequest request,
                                                             Utilisateur acteur) {
        if (producteurRepository.existsByCnibNumeroIgnoreCase(request.cnibNumero())) {
            throw new BusinessException("Un producteur avec ce numéro CNIB existe déjà.", HttpStatus.CONFLICT);
        }
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;
        Producteur producteur = Producteur.builder()
                .nomPrenom(request.nomPrenom().trim())
                .telephoneMobileMoney(request.telephoneMobileMoney().trim())
                .operateurMobile(request.operateurMobile().trim().toUpperCase())
                .cnibNumero(request.cnibNumero().trim())
                .cnibDateDelivrance(request.cnibDateDelivrance())
                .cnibLieuDelivrance(request.cnibLieuDelivrance())
                .locality(locality)
                .adressePrecision(request.adressePrecision())
                .build();
        producteurRepository.save(producteur);
        auditService.succes(acteur, "CREATION_PRODUCTEUR", "Producteur", producteur.getId(),
                "Producteur " + producteur.getNomPrenom() + " enregistré");
        return ReferentielDtos.ProducteurResponse.from(producteur);
    }

    // ------------------------------------------------------------------ Localités

    @Transactional(readOnly = true)
    public List<ReferentielDtos.LocalityResponse> listerLocalites(LocalityType type, UUID parentId) {
        List<Locality> localites;
        if (parentId != null) {
            localites = localityRepository.findByParentIdOrderByNameAsc(parentId);
        } else if (type != null) {
            localites = localityRepository.findByTypeOrderByNameAsc(type);
        } else {
            localites = localityRepository.findAll();
        }
        return localites.stream().map(ReferentielDtos.LocalityResponse::from).toList();
    }

    // ------------------------------------------------------------------- Magasins

    @Transactional(readOnly = true)
    public List<ReferentielDtos.MagasinResponse> listerMagasins(boolean actifsSeulement) {
        List<MagasinCentral> magasins = actifsSeulement
                ? magasinCentralRepository.findAllByActifTrueOrderByNomAsc()
                : magasinCentralRepository.findAllByOrderByNomAsc();
        return magasins.stream().map(ReferentielDtos.MagasinResponse::from).toList();
    }

    @Transactional
    public ReferentielDtos.MagasinResponse creerMagasin(ReferentielDtos.MagasinRequest request,
                                                       Utilisateur acteur) {
        if (magasinCentralRepository.existsByCodeMagasin(request.codeMagasin())) {
            throw new BusinessException("Un magasin avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;
        MagasinCentral magasin = MagasinCentral.builder()
                .codeMagasin(request.codeMagasin().trim().toUpperCase())
                .nom(request.nom().trim())
                .ville(libelleVille(request.ville(), locality))
                .locality(locality)
                .capaciteTotaleTonnes(request.capaciteTotaleTonnes())
                .stockActuelTonnes(0.0)
                .actif(request.actif() == null || request.actif())
                .build();
        magasinCentralRepository.save(magasin);
        auditService.succes(acteur, "CREATION_MAGASIN", "MagasinCentral", magasin.getId(),
                "Magasin " + magasin.getCodeMagasin());
        return ReferentielDtos.MagasinResponse.from(magasin);
    }

    @Transactional
    public ReferentielDtos.CampagneResponse modifierCampagne(Long campagneId,
                                                             ReferentielDtos.CampagneRequest request,
                                                             Utilisateur acteur) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        verifierPeriodeCampagne(request);
        String code = codeNormalise(request.code());
        if (campagneRepository.existsByCodeAndIdNot(code, campagneId)) {
            throw new BusinessException("Une campagne avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        campagne.setCode(code);
        campagne.setLibelle(request.libelle().trim());
        campagne.setDateDebut(request.dateDebut());
        campagne.setDateFin(request.dateFin());
        campagne.setObjectifTotalTonnes(request.objectifTotalTonnes() != null
                ? request.objectifTotalTonnes() : campagne.getObjectifTotalTonnes());
        if (request.statut() != null) {
            campagne.setStatut(request.statut());
        }
        campagneRepository.save(campagne);
        auditService.succes(acteur, "MODIFICATION_CAMPAGNE", "Campagne", campagneId,
                "Campagne " + campagne.getCode() + " modifiée");
        return ReferentielDtos.CampagneResponse.from(campagne);
    }

    @Transactional
    public void supprimerCampagne(Long campagneId, Utilisateur acteur) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        if (campagne.getStatut() == CampagneStatut.OUVERTE) {
            throw new BusinessException("Clôturez la campagne avant de la supprimer.");
        }
        supprimerAvecControle(() -> {
            campagneRepository.delete(campagne);
            campagneRepository.flush();
        }, "Cette campagne est déjà utilisée par des objectifs, des prix ou des collectes.");
        auditService.succes(acteur, "SUPPRESSION_CAMPAGNE", "Campagne", campagneId,
                "Campagne " + campagne.getCode() + " supprimée");
    }

    @Transactional
    public ReferentielDtos.SpeculationResponse modifierSpeculation(Long speculationId,
                                                                   ReferentielDtos.SpeculationRequest request,
                                                                   Utilisateur acteur) {
        Speculation speculation = speculationRepository.findById(speculationId)
                .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
        String code = codeNormalise(request.code());
        if (speculationRepository.existsByCodeAndIdNot(code, speculationId)) {
            throw new BusinessException("Une spéculation avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        speculation.setCode(code);
        speculation.setNom(request.nom().trim());
        speculation.setSeuilHumiditeMax(request.seuilHumiditeMax());
        speculation.setSeuilImpureteMax(request.seuilImpureteMax());
        if (request.active() != null) {
            speculation.setActive(request.active());
        }
        speculationRepository.save(speculation);
        auditService.succes(acteur, "MODIFICATION_SPECULATION", "Speculation", speculationId,
                "Spéculation " + speculation.getCode() + " modifiée");
        return ReferentielDtos.SpeculationResponse.from(speculation);
    }

    @Transactional
    public void supprimerSpeculation(Long speculationId, Utilisateur acteur) {
        Speculation speculation = speculationRepository.findById(speculationId)
                .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
        supprimerAvecControle(() -> {
            speculationRepository.delete(speculation);
            speculationRepository.flush();
        }, "Cette spéculation est déjà utilisée par des prix, des objectifs ou des collectes.");
        auditService.succes(acteur, "SUPPRESSION_SPECULATION", "Speculation", speculationId,
                "Spéculation " + speculation.getCode() + " supprimée");
    }

    @Transactional
    public ReferentielDtos.PrixGarantieResponse modifierPrix(Long prixId,
                                                              ReferentielDtos.PrixGarantieRequest request,
                                                              Utilisateur acteur) {
        PrixGarantie prix = prixGarantieRepository.findById(prixId)
                .orElseThrow(() -> new ResourceNotFoundException("Prix garanti introuvable."));
        if (prix.getStatut() == StatutPrixGarantie.APPROUVE) {
            throw new BusinessException("Un prix approuvé ne peut plus être modifié. Rejetez-le d'abord.");
        }
        if (request.dateEffet().isBefore(LocalDate.now())) {
            throw new BusinessException("La date d'effet ne peut pas être antérieure à aujourd'hui.");
        }
        Campagne campagne = campagneRepository.findById(request.campagneId())
                .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        Speculation speculation = speculationRepository.findById(request.speculationId())
                .orElseThrow(() -> new ResourceNotFoundException("Spéculation introuvable."));
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;

        prix.setCampagne(campagne);
        prix.setSpeculation(speculation);
        prix.setLocality(locality);
        prix.setPrixUnitaireKg(request.prixUnitaireKg());
        prix.setDateEffet(request.dateEffet());
        prix.setStatut(StatutPrixGarantie.EN_ATTENTE_APPROBATION);
        prix.setApprouvePar(null);
        prix.setDateApprobation(null);
        prixGarantieRepository.save(prix);
        auditService.succes(acteur, "MODIFICATION_PRIX_GARANTI", "PrixGarantie", prixId,
                "Prix " + prix.getPrixUnitaireKg() + " CFA/kg pour " + speculation.getCode());
        return ReferentielDtos.PrixGarantieResponse.from(prix);
    }

    @Transactional
    public ReferentielDtos.PrixGarantieResponse rejeterPrix(Long prixId, Utilisateur acteur) {
        PrixGarantie prix = prixGarantieRepository.findById(prixId)
                .orElseThrow(() -> new ResourceNotFoundException("Prix garanti introuvable."));
        prix.setStatut(StatutPrixGarantie.INACTIF);
        prix.setApprouvePar(null);
        prix.setDateApprobation(null);
        prixGarantieRepository.save(prix);
        auditService.succes(acteur, "REJET_PRIX_GARANTI", "PrixGarantie", prixId,
                "Prix désactivé pour " + prix.getSpeculation().getCode());
        return ReferentielDtos.PrixGarantieResponse.from(prix);
    }

    @Transactional
    public void supprimerPrix(Long prixId, Utilisateur acteur) {
        PrixGarantie prix = prixGarantieRepository.findById(prixId)
                .orElseThrow(() -> new ResourceNotFoundException("Prix garanti introuvable."));
        if (prix.getStatut() == StatutPrixGarantie.APPROUVE) {
            throw new BusinessException("Un prix approuvé ne peut pas être supprimé. Rejetez-le d'abord.");
        }
        prixGarantieRepository.delete(prix);
        prixGarantieRepository.flush();
        auditService.succes(acteur, "SUPPRESSION_PRIX_GARANTI", "PrixGarantie", prixId,
                "Prix " + prix.getPrixUnitaireKg() + " CFA/kg supprimé");
    }

    @Transactional
    public ReferentielDtos.ComptoirResponse modifierComptoir(Long comptoirId,
                                                             ReferentielDtos.ComptoirRequest request,
                                                             Utilisateur acteur) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        String code = codeNormalise(request.code());
        if (comptoirRepository.existsByCodeAndIdNot(code, comptoirId)) {
            throw new BusinessException("Un comptoir avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        Locality locality = localityRepository.findById(request.localityId())
                .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."));
        comptoir.setCode(code);
        comptoir.setNom(request.nom().trim());
        comptoir.setType(request.type() != null ? request.type() : comptoir.getType());
        comptoir.setLocality(locality);
        comptoir.setLatitudeGps(request.latitudeGps());
        comptoir.setLongitudeGps(request.longitudeGps());
        comptoir.setCapaciteStockageKg(request.capaciteStockageKg() != null
                ? request.capaciteStockageKg() : comptoir.getCapaciteStockageKg());
        if (request.actif() != null) {
            comptoir.setActif(request.actif());
        }
        comptoirRepository.save(comptoir);
        auditService.succes(acteur, "MODIFICATION_COMPTOIR", "Comptoir", comptoirId,
                "Comptoir " + comptoir.getCode() + " modifié");
        return ReferentielDtos.ComptoirResponse.from(comptoir);
    }

    @Transactional
    public void supprimerComptoir(Long comptoirId, Utilisateur acteur) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        supprimerAvecControle(() -> {
            comptoirRepository.delete(comptoir);
            comptoirRepository.flush();
        }, "Ce comptoir est déjà utilisé par des collectes, fiches de stock ou bordereaux.");
        auditService.succes(acteur, "SUPPRESSION_COMPTOIR", "Comptoir", comptoirId,
                "Comptoir " + comptoir.getCode() + " supprimé");
    }

    @Transactional
    public ReferentielDtos.ProducteurResponse modifierProducteur(Long producteurId,
                                                                  ReferentielDtos.ProducteurRequest request,
                                                                  Utilisateur acteur) {
        Producteur producteur = producteurRepository.findById(producteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Producteur introuvable."));
        if (producteurRepository.existsByCnibNumeroIgnoreCaseAndIdNot(request.cnibNumero().trim(), producteurId)) {
            throw new BusinessException("Un producteur avec ce numéro CNIB existe déjà.", HttpStatus.CONFLICT);
        }
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;
        producteur.setNomPrenom(request.nomPrenom().trim());
        producteur.setTelephoneMobileMoney(request.telephoneMobileMoney().trim());
        producteur.setOperateurMobile(request.operateurMobile().trim().toUpperCase());
        producteur.setCnibNumero(request.cnibNumero().trim());
        producteur.setCnibDateDelivrance(request.cnibDateDelivrance());
        producteur.setCnibLieuDelivrance(request.cnibLieuDelivrance());
        producteur.setLocality(locality);
        producteur.setAdressePrecision(request.adressePrecision());
        producteurRepository.save(producteur);
        auditService.succes(acteur, "MODIFICATION_PRODUCTEUR", "Producteur", producteurId,
                "Producteur " + producteur.getNomPrenom() + " modifié");
        return ReferentielDtos.ProducteurResponse.from(producteur);
    }

    @Transactional
    public void supprimerProducteur(Long producteurId, Utilisateur acteur) {
        Producteur producteur = producteurRepository.findById(producteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Producteur introuvable."));
        supprimerAvecControle(() -> {
            producteurRepository.delete(producteur);
            producteurRepository.flush();
        }, "Ce producteur est déjà utilisé par des collectes ou des notifications.");
        auditService.succes(acteur, "SUPPRESSION_PRODUCTEUR", "Producteur", producteurId,
                "Producteur " + producteur.getNomPrenom() + " supprimé");
    }

    @Transactional
    public ReferentielDtos.MagasinResponse modifierMagasin(Long magasinId,
                                                            ReferentielDtos.MagasinRequest request,
                                                            Utilisateur acteur) {
        MagasinCentral magasin = magasinCentralRepository.findById(magasinId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin introuvable."));
        String code = codeNormalise(request.codeMagasin());
        if (magasinCentralRepository.existsByCodeMagasinAndIdNot(code, magasinId)) {
            throw new BusinessException("Un magasin avec ce code existe déjà.", HttpStatus.CONFLICT);
        }
        if (request.capaciteTotaleTonnes() < valeurOuZero(magasin.getStockActuelTonnes())) {
            throw new BusinessException("La capacité ne peut pas être inférieure au stock actuel.");
        }
        Locality locality = request.localityId() != null
                ? localityRepository.findById(request.localityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localité introuvable."))
                : null;
        magasin.setCodeMagasin(code);
        magasin.setNom(request.nom().trim());
        magasin.setVille(libelleVille(request.ville(), locality));
        magasin.setLocality(locality);
        magasin.setCapaciteTotaleTonnes(request.capaciteTotaleTonnes());
        if (request.actif() != null) {
            magasin.setActif(request.actif());
        }
        magasinCentralRepository.save(magasin);
        auditService.succes(acteur, "MODIFICATION_MAGASIN", "MagasinCentral", magasinId,
                "Magasin " + magasin.getCodeMagasin() + " modifié");
        return ReferentielDtos.MagasinResponse.from(magasin);
    }

    @Transactional
    public void supprimerMagasin(Long magasinId, Utilisateur acteur) {
        MagasinCentral magasin = magasinCentralRepository.findById(magasinId)
                .orElseThrow(() -> new ResourceNotFoundException("Magasin introuvable."));
        if (valeurOuZero(magasin.getStockActuelTonnes()) > 0) {
            throw new BusinessException("Un magasin avec du stock ne peut pas être supprimé.");
        }
        supprimerAvecControle(() -> {
            magasinCentralRepository.delete(magasin);
            magasinCentralRepository.flush();
        }, "Ce magasin est déjà utilisé par des bordereaux de transfert ou de réception.");
        auditService.succes(acteur, "SUPPRESSION_MAGASIN", "MagasinCentral", magasinId,
                "Magasin " + magasin.getCodeMagasin() + " supprimé");
    }

    private void verifierPeriodeCampagne(ReferentielDtos.CampagneRequest request) {
        if (request.dateFin().isBefore(request.dateDebut())) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début.");
        }
    }

    private String codeNormalise(String code) {
        return code.trim().toUpperCase();
    }

    private void supprimerAvecControle(Runnable suppression, String messageConflit) {
        try {
            suppression.run();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(messageConflit, HttpStatus.CONFLICT);
        }
    }

    private double valeurOuZero(Double valeur) {
        return valeur != null ? valeur : 0.0;
    }

    private String libelleVille(String ville, Locality locality) {
        if (locality == null) {
            return ville.trim();
        }
        Locality courante = locality;
        while (courante != null && courante.getType() != LocalityType.COMMUNE) {
            courante = courante.getParent();
        }
        return courante != null ? courante.getName() : locality.getName();
    }

    @Transactional(readOnly = true)
    public Utilisateur utilisateurCourant(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }
}
