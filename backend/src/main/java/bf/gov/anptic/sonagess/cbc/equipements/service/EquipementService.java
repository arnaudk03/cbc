package bf.gov.anptic.sonagess.cbc.equipements.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.equipements.domain.AffectationEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.Equipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.EtatEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.StatutEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TabletteDevice;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TypeEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.dto.EquipementDtos;
import bf.gov.anptic.sonagess.cbc.equipements.repository.AffectationEquipementRepository;
import bf.gov.anptic.sonagess.cbc.equipements.repository.EquipementRepository;
import bf.gov.anptic.sonagess.cbc.equipements.repository.TabletteDeviceRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipementService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EquipementRepository equipementRepository;
    private final AffectationEquipementRepository affectationRepository;
    private final TabletteDeviceRepository tabletteRepository;
    private final ComptoirRepository comptoirRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<EquipementDtos.EquipementResponse> lister(Long comptoirId, boolean disponibles) {
        if (comptoirId != null && !disponibles) {
            return affectationRepository.findActivesParComptoir(comptoirId).stream()
                    .map(affectation -> EquipementDtos.EquipementResponse.from(
                            affectation.getEquipement(), affectation))
                    .toList();
        }
        List<Equipement> equipements = equipementRepository.findAllByOrderByTypeAscDesignationAsc();
        if (disponibles) {
            return equipements.stream()
                    .filter(equipement -> !affectationRepository
                            .existsByEquipementIdAndDateFinIsNull(equipement.getId()))
                    .map(equipement -> EquipementDtos.EquipementResponse.from(equipement, null))
                    .toList();
        }
        return equipements.stream()
                .map(equipement -> EquipementDtos.EquipementResponse.from(equipement,
                        affectationRepository.findByEquipementIdAndDateFinIsNull(equipement.getId())
                                .orElse(null)))
                .toList();
    }

    @Transactional
    public EquipementDtos.EquipementResponse creer(EquipementDtos.EquipementRequest request,
                                                    Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        TypeEquipement type = request.type();
        String code = texteOuNull(request.code());
        code = code == null ? genererCode(type) : code.toUpperCase(Locale.ROOT);
        verifierCode(code, null);
        String numeroSerie = texteOuNull(request.numeroSerie());
        if (type == TypeEquipement.TABLETTE && numeroSerie == null) {
            throw new BusinessException(
                    "Le numéro de série est obligatoire pour une tablette.");
        }
        verifierNumeroSerie(numeroSerie, null);
        String imei = texteOuNull(request.imei());
        if (imei != null && type != TypeEquipement.TABLETTE) {
            throw new BusinessException("L'IMEI ne concerne que les tablettes.");
        }
        verifierImei(imei, null);

        Equipement equipement = Equipement.builder()
                .code(code)
                .type(type)
                .designation(request.designation().trim())
                .numeroSerie(numeroSerie)
                .marque(texteOuNull(request.marque()))
                .modele(texteOuNull(request.modele()))
                .etat(request.etat() != null ? request.etat() : EtatEquipement.BON)
                .statut(StatutEquipement.DISPONIBLE)
                .dateAcquisition(request.dateAcquisition())
                .valeurCfa(request.valeurCfa())
                .observation(texteOuNull(request.observation()))
                .build();
        if (type == TypeEquipement.TABLETTE) {
            TabletteDevice tablette = TabletteDevice.builder()
                    .equipement(equipement)
                    .imei(imei)
                    .cleChiffrementLocale(genererCleChiffrement())
                    .build();
            equipement.setTablette(tablette);
        }
        equipementRepository.save(equipement);
        auditService.succes(acteur, "CREATION_EQUIPEMENT", "Equipement", equipement.getId(),
                "Équipement " + equipement.getCode() + " (" + type + ") créé");
        return EquipementDtos.EquipementResponse.from(equipement, null);
    }

    @Transactional
    public EquipementDtos.EquipementResponse modifier(Long equipementId,
                                                       EquipementDtos.EquipementRequest request,
                                                       Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Equipement equipement = charger(equipementId);
        if (request.type() != null && request.type() != equipement.getType()) {
            throw new BusinessException("Le type d'un équipement ne peut pas être modifié.");
        }
        String code = texteOuNull(request.code());
        if (code != null) {
            code = code.toUpperCase(Locale.ROOT);
            verifierCode(code, equipementId);
            equipement.setCode(code);
        }
        String numeroSerie = texteOuNull(request.numeroSerie());
        if (equipement.getType() == TypeEquipement.TABLETTE && numeroSerie == null) {
            throw new BusinessException(
                    "Le numéro de série est obligatoire pour une tablette.");
        }
        verifierNumeroSerie(numeroSerie, equipementId);
        String imei = texteOuNull(request.imei());
        if (imei != null && equipement.getType() != TypeEquipement.TABLETTE) {
            throw new BusinessException("L'IMEI ne concerne que les tablettes.");
        }
        verifierImei(imei, equipement.getTablette() != null
                ? equipement.getTablette().getId() : null);

        equipement.setDesignation(request.designation().trim());
        equipement.setNumeroSerie(numeroSerie);
        equipement.setMarque(texteOuNull(request.marque()));
        equipement.setModele(texteOuNull(request.modele()));
        if (request.etat() != null) {
            equipement.setEtat(request.etat());
        }
        equipement.setDateAcquisition(request.dateAcquisition());
        equipement.setValeurCfa(request.valeurCfa());
        equipement.setObservation(texteOuNull(request.observation()));
        if (equipement.getType() == TypeEquipement.TABLETTE) {
            TabletteDevice tablette = equipement.getTablette();
            if (tablette == null) {
                tablette = TabletteDevice.builder()
                        .equipement(equipement)
                        .cleChiffrementLocale(genererCleChiffrement())
                        .build();
                equipement.setTablette(tablette);
            }
            tablette.setImei(imei);
        }
        equipementRepository.save(equipement);
        auditService.succes(acteur, "MODIFICATION_EQUIPEMENT", "Equipement", equipementId,
                "Équipement " + equipement.getCode() + " modifié");
        return EquipementDtos.EquipementResponse.from(equipement,
                affectationRepository.findByEquipementIdAndDateFinIsNull(equipementId)
                        .orElse(null));
    }

    @Transactional
    public EquipementDtos.EquipementResponse affecter(Long equipementId,
                                                       EquipementDtos.AffectationEquipementRequest request,
                                                       Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Equipement equipement = charger(equipementId);
        Comptoir comptoir = chargerComptoir(request.comptoirId());
        Utilisateur utilisateur = request.utilisateurId() != null
                ? chargerUtilisateur(request.utilisateurId()) : null;
        verifierCoherenceUtilisateur(utilisateur, comptoir);
        verifierAffectation(equipement, utilisateur);
        AffectationEquipement affectation = creerAffectation(equipement, comptoir,
                utilisateur, request.motif(), acteur);
        affectationRepository.save(affectation);
        equipement.setStatut(StatutEquipement.AFFECTE);
        equipementRepository.save(equipement);
        auditService.succes(acteur, "AFFECTATION_EQUIPEMENT", "Equipement", equipementId,
                "Équipement " + equipement.getCode() + " affecté au comptoir "
                        + comptoir.getCode());
        return EquipementDtos.EquipementResponse.from(equipement, affectation);
    }

    @Transactional
    public List<EquipementDtos.EquipementResponse> affecterEnLot(
            EquipementDtos.AffectationEquipementBatchRequest request, Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Comptoir comptoir = chargerComptoir(request.comptoirId());
        Utilisateur utilisateur = request.utilisateurId() != null
                ? chargerUtilisateur(request.utilisateurId()) : null;
        verifierCoherenceUtilisateur(utilisateur, comptoir);

        Set<Long> ids = new LinkedHashSet<>(request.equipementIds());
        if (ids.contains(null)) {
            throw new BusinessException("Un identifiant d'équipement est invalide.");
        }
        List<Equipement> equipements = equipementRepository.findAllById(ids);
        if (equipements.size() != ids.size()) {
            throw new ResourceNotFoundException("Un ou plusieurs équipements sont introuvables.");
        }
        if (utilisateur != null) {
            long tablettes = equipements.stream()
                    .filter(equipement -> equipement.getType() == TypeEquipement.TABLETTE)
                    .count();
            if (tablettes > 1) {
                throw new BusinessException(
                        "Un utilisateur ne peut détenir qu'une seule tablette.");
            }
        }
        for (Equipement equipement : equipements) {
            verifierAffectation(equipement, utilisateur);
        }

        List<AffectationEquipement> affectations = new ArrayList<>();
        for (Equipement equipement : equipements) {
            affectations.add(creerAffectation(equipement, comptoir, utilisateur,
                    request.motif(), acteur));
            equipement.setStatut(StatutEquipement.AFFECTE);
        }
        affectationRepository.saveAll(affectations);
        equipementRepository.saveAll(equipements);
        auditService.succes(acteur, "AFFECTATION_EQUIPEMENT", "Equipement", null,
                affectations.size() + " équipement(s) affecté(s) au comptoir "
                        + comptoir.getCode());
        List<EquipementDtos.EquipementResponse> reponses = new ArrayList<>();
        for (int index = 0; index < equipements.size(); index++) {
            reponses.add(EquipementDtos.EquipementResponse.from(
                    equipements.get(index), affectations.get(index)));
        }
        return reponses;
    }

    @Transactional
    public EquipementDtos.EquipementResponse retirer(Long equipementId, String motif,
                                                      Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Equipement equipement = charger(equipementId);
        AffectationEquipement affectation = affectationRepository
                .findByEquipementIdAndDateFinIsNull(equipementId)
                .orElseThrow(() -> new BusinessException(
                        "Cet équipement n'est affecté à aucun comptoir."));
        affectation.setDateFin(LocalDateTime.now());
        affectation.setRetirePar(acteur);
        if (motif != null && !motif.isBlank()) {
            affectation.setMotif(motif.trim());
        }
        equipement.setStatut(statutApresRetrait(equipement));
        affectationRepository.save(affectation);
        equipementRepository.save(equipement);
        auditService.succes(acteur, "RETRAIT_EQUIPEMENT", "Equipement", equipementId,
                "Équipement " + equipement.getCode() + " retiré du comptoir "
                        + affectation.getComptoir().getCode());
        return EquipementDtos.EquipementResponse.from(equipement, null);
    }

    @Transactional
    public EquipementDtos.EquipementResponse changerStatut(
            Long equipementId, StatutEquipement statut, Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Equipement equipement = charger(equipementId);
        boolean affecte = affectationRepository.existsByEquipementIdAndDateFinIsNull(equipementId);
        if (statut == StatutEquipement.AFFECTE) {
            throw new BusinessException(
                    "Le statut AFFECTE est géré automatiquement par l'affectation.");
        }
        if (affecte && statut == StatutEquipement.REFORME) {
            throw new BusinessException(
                    "Retirez l'équipement du comptoir avant de le réformer.");
        }
        if (affecte && statut == StatutEquipement.DISPONIBLE) {
            throw new BusinessException(
                    "Retirez l'équipement du comptoir avant de le rendre disponible.");
        }
        equipement.setStatut(statut);
        equipementRepository.save(equipement);
        auditService.succes(acteur, "CHANGEMENT_STATUT_EQUIPEMENT", "Equipement", equipementId,
                "Statut de l'équipement " + equipement.getCode() + " : " + statut);
        return EquipementDtos.EquipementResponse.from(equipement,
                affectationRepository.findByEquipementIdAndDateFinIsNull(equipementId)
                        .orElse(null));
    }

    @Transactional
    public EquipementDtos.EquipementResponse synchroniserTablette(Long equipementId,
                                                                   Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Equipement equipement = charger(equipementId);
        if (equipement.getType() != TypeEquipement.TABLETTE
                || equipement.getTablette() == null) {
            throw new BusinessException("Cet équipement n'est pas une tablette.");
        }
        equipement.getTablette().setDateDerniereSynchro(LocalDateTime.now());
        tabletteRepository.save(equipement.getTablette());
        auditService.succes(acteur, "SYNCHRONISATION_TABLETTE", "Equipement", equipementId,
                "Tablette " + equipement.getCode() + " synchronisée");
        return EquipementDtos.EquipementResponse.from(equipement,
                affectationRepository.findByEquipementIdAndDateFinIsNull(equipementId)
                        .orElse(null));
    }

    private AffectationEquipement creerAffectation(Equipement equipement, Comptoir comptoir,
                                                    Utilisateur utilisateur, String motif,
                                                    Utilisateur acteur) {
        return AffectationEquipement.builder()
                .equipement(equipement)
                .comptoir(comptoir)
                .utilisateur(utilisateur)
                .dateDebut(LocalDateTime.now())
                .motif(texteOuNull(motif))
                .affectePar(acteur)
                .build();
    }

    private void verifierCoherenceUtilisateur(Utilisateur utilisateur, Comptoir comptoir) {
        if (utilisateur == null) {
            return;
        }
        if (!Boolean.TRUE.equals(utilisateur.getActif())) {
            throw new BusinessException("L'utilisateur sélectionné est inactif.");
        }
        if (utilisateur.getComptoir() == null
                || !comptoir.getId().equals(utilisateur.getComptoir().getId())) {
            throw new BusinessException(
                    "L'utilisateur doit appartenir au comptoir de destination.");
        }
    }

    private void verifierAffectation(Equipement equipement, Utilisateur utilisateur) {
        if (equipement.getStatut() == StatutEquipement.REFORME) {
            throw new BusinessException(
                    "L'équipement " + equipement.getCode() + " est réformé.");
        }
        if (affectationRepository.existsByEquipementIdAndDateFinIsNull(equipement.getId())) {
            throw new BusinessException(
                    "L'équipement " + equipement.getCode()
                            + " est déjà affecté. Retirez-le d'abord.", HttpStatus.CONFLICT);
        }
        if (utilisateur == null || equipement.getType() != TypeEquipement.TABLETTE) {
            return;
        }
        if (!utilisateur.hasRole("AGENT_COLLECTEUR") && !utilisateur.hasRole("CHEF_COMPTOIR")) {
            throw new BusinessException(
                    "Une tablette ne peut être confiée qu'à un agent collecteur ou un chef de comptoir.");
        }
        if (affectationRepository.countActivesParUtilisateurEtType(
                utilisateur.getId(), TypeEquipement.TABLETTE) > 0) {
            throw new BusinessException(
                    "Cet utilisateur détient déjà une tablette active.", HttpStatus.CONFLICT);
        }
    }

    private StatutEquipement statutApresRetrait(Equipement equipement) {
        if (equipement.getEtat() == EtatEquipement.HORS_SERVICE) {
            return StatutEquipement.REFORME;
        }
        return StatutEquipement.DISPONIBLE;
    }

    private void verifierCode(String code, Long equipementId) {
        boolean existe = equipementId == null
                ? equipementRepository.existsByCodeIgnoreCase(code)
                : equipementRepository.existsByCodeIgnoreCaseAndIdNot(code, equipementId);
        if (existe) {
            throw new BusinessException("Ce code d'équipement est déjà utilisé.",
                    HttpStatus.CONFLICT);
        }
    }

    private void verifierNumeroSerie(String numeroSerie, Long equipementId) {
        if (numeroSerie == null) {
            return;
        }
        boolean existe = equipementId == null
                ? equipementRepository.existsByNumeroSerieIgnoreCase(numeroSerie)
                : equipementRepository.existsByNumeroSerieIgnoreCaseAndIdNot(
                        numeroSerie, equipementId);
        if (existe) {
            throw new BusinessException("Ce numéro de série est déjà utilisé.",
                    HttpStatus.CONFLICT);
        }
    }

    private void verifierImei(String imei, Long tabletteId) {
        if (imei == null) {
            return;
        }
        boolean existe = tabletteId == null
                ? tabletteRepository.existsByImeiIgnoreCase(imei)
                : tabletteRepository.existsByImeiIgnoreCaseAndIdNot(imei, tabletteId);
        if (existe) {
            throw new BusinessException("Cet IMEI est déjà utilisé.", HttpStatus.CONFLICT);
        }
    }

    private Equipement charger(Long equipementId) {
        return equipementRepository.findById(equipementId)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement introuvable."));
    }

    private Comptoir chargerComptoir(Long comptoirId) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        if (!Boolean.TRUE.equals(comptoir.getActif())) {
            throw new BusinessException("Le comptoir de destination est inactif.");
        }
        return comptoir;
    }

    private Utilisateur chargerUtilisateur(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private Utilisateur chargerActeur(Long acteurId) {
        return utilisateurRepository.findById(acteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private String genererCode(TypeEquipement type) {
        String prefixe = switch (type) {
            case TABLETTE -> "TAB";
            case BALANCE -> "BAL";
            case KIT_COLLECTE -> "KIT";
            case VANNEUSE -> "VAN";
            case TELEPHONE -> "TEL";
            case AUTRE -> "AUT";
        };
        return "EQ-" + prefixe + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String genererCleChiffrement() {
        byte[] secret = new byte[32];
        SECURE_RANDOM.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    private String texteOuNull(String valeur) {
        if (valeur == null) {
            return null;
        }
        String texte = valeur.trim();
        return texte.isEmpty() ? null : texte;
    }
}
