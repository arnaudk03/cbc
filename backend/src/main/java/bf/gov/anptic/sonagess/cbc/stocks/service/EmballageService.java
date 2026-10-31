package bf.gov.anptic.sonagess.cbc.stocks.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.stocks.domain.CategorieMaterielEmballage;
import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.LigneFicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.MaterielEmballage;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutFicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.UniteMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.dto.EmballageDtos;
import bf.gov.anptic.sonagess.cbc.stocks.repository.FicheStockMaterielRepository;
import bf.gov.anptic.sonagess.cbc.stocks.repository.LigneFicheStockMaterielRepository;
import bf.gov.anptic.sonagess.cbc.stocks.repository.MaterielEmballageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmballageService {

    private static final Pageable PREMIERE_LIGNE = PageRequest.of(0, 1);
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MaterielEmballageRepository materielRepository;
    private final FicheStockMaterielRepository ficheRepository;
    private final LigneFicheStockMaterielRepository ligneRepository;
    private final ComptoirRepository comptoirRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<EmballageDtos.MaterielResponse> listerMateriels(boolean actifsSeulement) {
        List<MaterielEmballage> materiels = actifsSeulement
                ? materielRepository.findAllByActifTrueOrderByCategorieAscLibelleAsc()
                : materielRepository.findAllByOrderByCategorieAscLibelleAsc();
        return materiels.stream().map(EmballageDtos.MaterielResponse::from).toList();
    }

    @Transactional
    public EmballageDtos.MaterielResponse creerMateriel(
            EmballageDtos.MaterielRequest request, Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        String code = texteOuNull(request.code());
        code = code == null ? genererCodeMateriel(request.categorie()) : code.toUpperCase(Locale.ROOT);
        if (materielRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Ce code de matériel est déjà utilisé.", HttpStatus.CONFLICT);
        }
        MaterielEmballage materiel = MaterielEmballage.builder()
                .code(code)
                .libelle(request.libelle().trim())
                .categorie(request.categorie() != null
                        ? request.categorie() : CategorieMaterielEmballage.AUTRE)
                .unite(request.unite() != null ? request.unite() : UniteMateriel.UNITE)
                .seuilAlerte(request.seuilAlerte() != null ? request.seuilAlerte() : 0)
                .actif(request.actif() == null || request.actif())
                .build();
        materielRepository.save(materiel);
        auditService.succes(acteur, "CREATION_MATERIEL_EMBALLAGE", "MaterielEmballage",
                materiel.getId(), "Matériel " + materiel.getCode() + " créé");
        return EmballageDtos.MaterielResponse.from(materiel);
    }

    @Transactional
    public EmballageDtos.MaterielResponse modifierMateriel(
            Long materielId, EmballageDtos.MaterielRequest request, Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        MaterielEmballage materiel = chargerMateriel(materielId);
        String code = texteOuNull(request.code());
        if (code != null) {
            code = code.toUpperCase(Locale.ROOT);
            if (materielRepository.existsByCodeIgnoreCaseAndIdNot(code, materielId)) {
                throw new BusinessException("Ce code de matériel est déjà utilisé.",
                        HttpStatus.CONFLICT);
            }
            materiel.setCode(code);
        }
        materiel.setLibelle(request.libelle().trim());
        if (request.categorie() != null) {
            materiel.setCategorie(request.categorie());
        }
        if (request.unite() != null) {
            materiel.setUnite(request.unite());
        }
        if (request.seuilAlerte() != null) {
            materiel.setSeuilAlerte(request.seuilAlerte());
        }
        if (request.actif() != null) {
            materiel.setActif(request.actif());
        }
        materielRepository.save(materiel);
        auditService.succes(acteur, "MODIFICATION_MATERIEL_EMBALLAGE", "MaterielEmballage",
                materielId, "Matériel " + materiel.getCode() + " modifié");
        return EmballageDtos.MaterielResponse.from(materiel);
    }

    @Transactional(readOnly = true)
    public List<EmballageDtos.FicheResponse> listerFiches(Long comptoirId, LocalDate dateFiche) {
        List<FicheStockMateriel> fiches = dateFiche != null
                ? ficheRepository.findByComptoirIdAndDateFicheOrderByCreeLeAsc(comptoirId, dateFiche)
                : ficheRepository.findByComptoirIdOrderByDateFicheDescCreeLeDesc(comptoirId)
                        .stream().limit(60).toList();
        return fiches.stream().map(EmballageDtos.FicheResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmballageDtos.FicheResponse detailFiche(Long ficheId) {
        return EmballageDtos.FicheResponse.from(chargerFiche(ficheId));
    }

    @Transactional
    public EmballageDtos.FicheResponse creerFiche(
            EmballageDtos.FicheRequest request, Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        Comptoir comptoir = chargerComptoir(request.comptoirId());
        ficheRepository.findFirstByComptoirIdAndDateFicheOrderByCreeLeDesc(
                comptoir.getId(), request.dateFiche()).ifPresent(existante -> {
                    throw new BusinessException(
                            "Une fiche de stock existe déjà pour ce comptoir à cette date.",
                            HttpStatus.CONFLICT);
                });

        Set<Long> materielsVus = new HashSet<>();
        Map<Long, MaterielEmballage> materiels = new LinkedHashMap<>();
        for (EmballageDtos.LigneFicheRequest ligne : request.lignes()) {
            if (!materielsVus.add(ligne.materielId())) {
                throw new BusinessException(
                        "Chaque matériel ne peut apparaître qu'une seule fois sur la fiche.");
            }
            MaterielEmballage materiel = chargerMateriel(ligne.materielId());
            if (!Boolean.TRUE.equals(materiel.getActif())) {
                throw new BusinessException(
                        "Le matériel " + materiel.getLibelle() + " n'est plus actif.");
            }
            materiels.put(materiel.getId(), materiel);
        }

        FicheStockMateriel fiche = FicheStockMateriel.builder()
                .numeroFiche(genererNumeroFiche(comptoir, request.dateFiche()))
                .comptoir(comptoir)
                .dateFiche(request.dateFiche())
                .statut(StatutFicheStockMateriel.BROUILLON)
                .observations(texteOuNull(request.observations()))
                .creePar(acteur)
                .build();

        for (EmballageDtos.LigneFicheRequest ligneRequest : request.lignes()) {
            MaterielEmballage materiel = materiels.get(ligneRequest.materielId());
            int stockInitial = stockInitial(comptoir.getId(), materiel.getId(),
                    request.dateFiche());
            int entrees = valeurOuZero(ligneRequest.entrees());
            int sorties = valeurOuZero(ligneRequest.sorties());
            int theorique = stockInitial + entrees - sorties;
            if (theorique < 0) {
                throw new BusinessException("Stock négatif pour " + materiel.getLibelle()
                        + " : vérifiez les entrées et sorties.");
            }
            int stockPhysique = ligneRequest.stockPhysique() != null
                    ? ligneRequest.stockPhysique() : theorique;
            if (stockPhysique < 0) {
                throw new BusinessException("Le stock physique de " + materiel.getLibelle()
                        + " ne peut pas être négatif.");
            }
            int ecart = stockPhysique - theorique;
            fiche.ajouterLigne(LigneFicheStockMateriel.builder()
                    .materiel(materiel)
                    .stockInitial(stockInitial)
                    .entrees(entrees)
                    .sorties(sorties)
                    .stockPhysique(stockPhysique)
                    .ecart(ecart)
                    .stockFinal(stockPhysique)
                    .referenceDoc(texteOuNull(ligneRequest.referenceDoc()))
                    .observation(texteOuNull(ligneRequest.observation()))
                    .build());
        }

        ficheRepository.save(fiche);
        auditService.succes(acteur, "TENUE_FICHE_STOCK_EMBALLAGE", "FicheStockMateriel",
                fiche.getId(), "Fiche " + fiche.getNumeroFiche() + " - "
                        + fiche.getLignes().size() + " ligne(s)");
        return EmballageDtos.FicheResponse.from(fiche);
    }

    @Transactional
    public EmballageDtos.FicheResponse validerFiche(Long ficheId, Long acteurId) {
        Utilisateur acteur = chargerActeur(acteurId);
        FicheStockMateriel fiche = chargerFiche(ficheId);
        if (fiche.getStatut() != StatutFicheStockMateriel.BROUILLON) {
            throw new BusinessException("Seule une fiche brouillon peut être validée.",
                    HttpStatus.CONFLICT);
        }
        fiche.setStatut(StatutFicheStockMateriel.VALIDEE);
        fiche.setValidePar(acteur);
        fiche.setValideLe(LocalDateTime.now());
        ficheRepository.save(fiche);
        auditService.succes(acteur, "VALIDATION_FICHE_STOCK_EMBALLAGE", "FicheStockMateriel",
                ficheId, "Fiche " + fiche.getNumeroFiche() + " validée");
        return EmballageDtos.FicheResponse.from(fiche);
    }

    @Transactional(readOnly = true)
    public List<EmballageDtos.StockMaterielResponse> stockCourant(Long comptoirId) {
        chargerComptoir(comptoirId);
        List<EmballageDtos.StockMaterielResponse> stock = new ArrayList<>();
        for (MaterielEmballage materiel : materielRepository
                .findAllByActifTrueOrderByCategorieAscLibelleAsc()) {
            List<LigneFicheStockMateriel> dernieres = ligneRepository.findDernieresLignes(
                    comptoirId, materiel.getId(), StatutFicheStockMateriel.VALIDEE,
                    LocalDate.now(), PREMIERE_LIGNE);
            int stockFinal = dernieres.isEmpty() ? 0 : dernieres.get(0).getStockFinal();
            int seuil = materiel.getSeuilAlerte() != null ? materiel.getSeuilAlerte() : 0;
            stock.add(new EmballageDtos.StockMaterielResponse(
                    materiel.getId(),
                    materiel.getCode(),
                    materiel.getLibelle(),
                    materiel.getCategorie(),
                    materiel.getUnite(),
                    seuil,
                    stockFinal,
                    seuil > 0 && stockFinal <= seuil
            ));
        }
        return stock;
    }

    private int stockInitial(Long comptoirId, Long materielId, LocalDate dateFiche) {
        List<LigneFicheStockMateriel> dernieres = ligneRepository.findDernieresLignes(
                comptoirId, materielId, StatutFicheStockMateriel.VALIDEE, dateFiche.minusDays(1),
                PREMIERE_LIGNE);
        return dernieres.isEmpty() ? 0 : dernieres.get(0).getStockFinal();
    }

    private FicheStockMateriel chargerFiche(Long ficheId) {
        return ficheRepository.findById(ficheId)
                .orElseThrow(() -> new ResourceNotFoundException("Fiche de stock introuvable."));
    }

    private MaterielEmballage chargerMateriel(Long materielId) {
        return materielRepository.findById(materielId)
                .orElseThrow(() -> new ResourceNotFoundException("Matériel introuvable."));
    }

    private Comptoir chargerComptoir(Long comptoirId) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        if (!Boolean.TRUE.equals(comptoir.getActif())) {
            throw new BusinessException("Le comptoir sélectionné est inactif.");
        }
        return comptoir;
    }

    private Utilisateur chargerActeur(Long acteurId) {
        return utilisateurRepository.findById(acteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private String genererCodeMateriel(CategorieMaterielEmballage categorie) {
        String prefixe = switch (categorie != null ? categorie : CategorieMaterielEmballage.AUTRE) {
            case SACS -> "SAC";
            case BACHES -> "BAC";
            case PRODUITS_PHYTO -> "PHY";
            case AUTRE -> "MAT";
        };
        return "MAT-" + prefixe + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private String genererNumeroFiche(Comptoir comptoir, LocalDate dateFiche) {
        String base = "FSME-" + comptoir.getCode() + "-" + dateFiche.format(FORMAT_DATE) + "-";
        String numero;
        do {
            numero = base + UUID.randomUUID().toString()
                    .substring(0, 4).toUpperCase(Locale.ROOT);
        } while (ficheRepository.existsByNumeroFiche(numero));
        return numero;
    }

    private int valeurOuZero(Integer valeur) {
        return valeur != null ? valeur : 0;
    }

    private String texteOuNull(String valeur) {
        if (valeur == null) {
            return null;
        }
        String texte = valeur.trim();
        return texte.isEmpty() ? null : texte;
    }
}
