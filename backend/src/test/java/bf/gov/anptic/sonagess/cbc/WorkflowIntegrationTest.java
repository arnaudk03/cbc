package bf.gov.anptic.sonagess.cbc;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.service.UtilisateurService;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.equipements.domain.EtatEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.StatutEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TypeEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.dto.EquipementDtos;
import bf.gov.anptic.sonagess.cbc.equipements.repository.AffectationEquipementRepository;
import bf.gov.anptic.sonagess.cbc.equipements.service.EquipementService;
import bf.gov.anptic.sonagess.cbc.stocks.domain.CategorieMaterielEmballage;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutFicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.UniteMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.dto.EmballageDtos;
import bf.gov.anptic.sonagess.cbc.stocks.service.EmballageService;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Locality;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import bf.gov.anptic.sonagess.cbc.paiements.domain.ModePaiement;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiementStatut;
import bf.gov.anptic.sonagess.cbc.collectes.domain.StatutValidationBrbc;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.CampagneRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ProducteurRepository;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.SpeculationRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.collectes.service.CollecteService;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteDtos;
import bf.gov.anptic.sonagess.cbc.paiements.service.PaiementService;
import bf.gov.anptic.sonagess.cbc.paiements.dto.PaiementDtos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class WorkflowIntegrationTest {

    @Autowired
    private CollecteService collecteService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private CampagneRepository campagneRepository;

    @Autowired
    private ComptoirRepository comptoirRepository;

    @Autowired
    private SpeculationRepository speculationRepository;

    @Autowired
    private ProducteurRepository producteurRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private EquipementService equipementService;

    @Autowired
    private AffectationEquipementRepository affectationEquipementRepository;

    @Autowired
    private EmballageService emballageService;

    @Test
    void ficheStockMaterielEmballageReporteLeStockEtSeValide() {
        var acteur = utilisateurRepository.findByEmailIgnoreCase("admin@sonagess.bf").orElseThrow();
        var comptoir = comptoirRepository.findByCode("CPT-BOB-01").orElseThrow();
        String suffixe = UUID.randomUUID().toString().substring(0, 8);
        var materiel = emballageService.creerMateriel(
                new EmballageDtos.MaterielRequest(
                        null, "Sacs de test " + suffixe, CategorieMaterielEmballage.SACS,
                        UniteMateriel.SAC, 10, true),
                acteur.getId());
        LocalDate date = LocalDate.now().minusDays(1);

        var fiche = emballageService.creerFiche(new EmballageDtos.FicheRequest(
                comptoir.getId(), date, "Inventaire de test",
                List.of(new EmballageDtos.LigneFicheRequest(
                        materiel.id(), 50, 5, null, "DOC-1", null))),
                acteur.getId());

        assertThat(fiche.statut()).isEqualTo(StatutFicheStockMateriel.BROUILLON);
        var ligne = fiche.lignes().get(0);
        assertThat(ligne.stockInitial()).isZero();
        assertThat(ligne.stockTheorique()).isEqualTo(45);
        assertThat(ligne.ecart()).isZero();
        assertThat(ligne.stockFinal()).isEqualTo(45);

        var validee = emballageService.validerFiche(fiche.id(), acteur.getId());
        assertThat(validee.statut()).isEqualTo(StatutFicheStockMateriel.VALIDEE);

        var ficheSuivante = emballageService.creerFiche(new EmballageDtos.FicheRequest(
                comptoir.getId(), date.plusDays(1), null,
                List.of(new EmballageDtos.LigneFicheRequest(
                        materiel.id(), 0, 5, null, null, null))),
                acteur.getId());
        assertThat(ficheSuivante.lignes().get(0).stockInitial()).isEqualTo(45);
        assertThat(ficheSuivante.lignes().get(0).stockFinal()).isEqualTo(40);
        emballageService.validerFiche(ficheSuivante.id(), acteur.getId());

        var stock = emballageService.stockCourant(comptoir.getId()).stream()
                .filter(element -> element.materielId().equals(materiel.id()))
                .findFirst().orElseThrow();
        assertThat(stock.stockFinal()).isEqualTo(40);
        assertThat(stock.alerte()).isFalse();

        assertThatThrownBy(() -> emballageService.creerFiche(new EmballageDtos.FicheRequest(
                comptoir.getId(), date, null,
                List.of(new EmballageDtos.LigneFicheRequest(
                        materiel.id(), 0, 0, null, null, null))),
                acteur.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void affectationEquipementAppliqueLesReglesTablette() {
        var acteur = utilisateurRepository.findByEmailIgnoreCase("admin@sonagess.bf").orElseThrow();
        var comptoir = comptoirRepository.findByCode("CPT-BOB-01").orElseThrow();
        var agent = utilisateurRepository.findByEmailIgnoreCase("agent@sonagess.bf").orElseThrow();

        var tablette1 = equipementService.creer(tablette("Tablette test 1", "IMEI-1"), acteur.getId());
        var tablette2 = equipementService.creer(tablette("Tablette test 2", "IMEI-2"), acteur.getId());

        var affectation = equipementService.affecter(tablette1.id(),
                new EquipementDtos.AffectationEquipementRequest(
                        comptoir.getId(), agent.getId(), "Dotation de collecte"),
                acteur.getId());

        assertThat(affectation.statut()).isEqualTo(StatutEquipement.AFFECTE);
        assertThat(affectation.affectation().utilisateurId()).isEqualTo(agent.getId());

        assertThatThrownBy(() -> equipementService.affecter(tablette1.id(),
                new EquipementDtos.AffectationEquipementRequest(
                        comptoir.getId(), null, "Doublon"),
                acteur.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà affecté");

        assertThatThrownBy(() -> equipementService.affecter(tablette2.id(),
                new EquipementDtos.AffectationEquipementRequest(
                        comptoir.getId(), agent.getId(), "Deuxième tablette"),
                acteur.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà une tablette");

        var liberee = equipementService.retirer(tablette1.id(), "Retour magasin", acteur.getId());
        assertThat(liberee.statut()).isEqualTo(StatutEquipement.DISPONIBLE);
        assertThat(affectationEquipementRepository
                .findByEquipementIdAndDateFinIsNull(tablette1.id())).isEmpty();

        var reaffectation = equipementService.affecter(tablette2.id(),
                new EquipementDtos.AffectationEquipementRequest(
                        comptoir.getId(), agent.getId(), "Remplacement"),
                acteur.getId());
        assertThat(reaffectation.statut()).isEqualTo(StatutEquipement.AFFECTE);
    }

    @Test
    void affectationComptoirRefuseUnUtilisateurDejaRattache() {
        var acteur = utilisateurRepository.findByEmailIgnoreCase("admin@sonagess.bf").orElseThrow();
        var comptoirA = comptoirRepository.findByCode("CPT-OUA-02").orElseThrow();
        var comptoirB = comptoirRepository.findByCode("CPT-BOB-01").orElseThrow();
        var candidats = utilisateurRepository.findAllByOrderByNomAscPrenomAsc().stream()
                .filter(utilisateur -> utilisateur.getComptoir() == null)
                .filter(utilisateur -> !utilisateur.getId().equals(acteur.getId()))
                .limit(2)
                .toList();
        assertThat(candidats).hasSize(2);

        var resultat = utilisateurService.affecterAuComptoir(comptoirA.getId(),
                candidats.stream().map(Utilisateur::getId).toList(), acteur);

        assertThat(resultat.affectes()).isEqualTo(2);
        for (var candidat : candidats) {
            assertThat(utilisateurRepository.findById(candidat.getId()).orElseThrow()
                    .getComptoir().getId()).isEqualTo(comptoirA.getId());
        }

        assertThatThrownBy(() -> utilisateurService.affecterAuComptoir(comptoirB.getId(),
                List.of(candidats.get(0).getId()), acteur))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("un seul comptoir");
    }

    @Test
    void collecteQualityWorkflowThenTwoFactorPayment() {
        var campagne = campagneRepository.findByCode("CAMP-2026-2027").orElseThrow();
        Comptoir comptoir = comptoirRepository.findByCode("CPT-OUA-02").orElseThrow();
        var speculation = speculationRepository.findByCode("MAIS_BLANC").orElseThrow();
        var producteur = producteurRepository.findAll().stream().findFirst().orElseThrow();

        Long agentId = utilisateurId("agent@sonagess.bf");
        Long scpId = utilisateurId("scp@sonagess.bf");
        Long dctId = utilisateurId("dct@sonagess.bf");
        Long sfbId = utilisateurId("sfb@sonagess.bf");
        Long chefSfbId = utilisateurId("chef.sfb@sonagess.bf");

        var creation = new CollecteDtos.CreateCollecteRequest(
                campagne.getId(), comptoir.getId(), producteur.getId(), speculation.getId(),
                1250.0, ModePaiement.MOBILE_MONEY, 11.5, 1.0, 0.2, true, "ACCEPTE",
                null, 25, 1260.0, 10.0, 12.37, -1.52, null);

        var collecte = collecteService.enregistrer(creation, agentId);
        assertThat(collecte.statut()).isEqualTo(CollecteStatut.SYNCHRONISEE.name());
        assertThat(collecte.region()).isEqualTo(regionAttendue(comptoir));
        assertThat(collecte.bordereau()).isNotNull();
        assertThat(collecte.bordereau().prixUnitaireApplique()).isPositive();
        assertThat(collecte.bordereau().montantNetPayerCfa()).isEqualTo(250_000.0);
        assertThat(collecte.bordereau().statutValidation())
                .isEqualTo(StatutValidationBrbc.EN_ATTENTE_EXAMEN.name());

        var examinee = collecteService.examiner(collecte.id(),
                new CollecteDtos.ExamenRequest(true, null), scpId);
        assertThat(examinee.statut()).isEqualTo(CollecteStatut.EXAMINEE_SCP.name());

        var validee = collecteService.valider(collecte.id(),
                new CollecteDtos.ValidationRequest(true, null), dctId);
        assertThat(validee.statut()).isEqualTo(CollecteStatut.VALIDEE_DCT.name());
        assertThat(validee.bordereau().statutValidation())
                .isEqualTo(StatutValidationBrbc.VALIDE_DCT.name());

        var paiement = paiementService.initier(
                new PaiementDtos.InitierPaiementRequest(validee.bordereau().id()), sfbId);
        assertThat(paiement.statut()).isEqualTo(OrdrePaiementStatut.INITIE.name());
        assertThat(paiement.montantPayeCfa()).isEqualTo(250_000.0);

        assertThatThrownBy(() -> paiementService.demanderOtp(paiement.id(), sfbId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Séparation des tâches");

        var challenge = paiementService.demanderOtp(paiement.id(), chefSfbId);
        assertThat(challenge.codeDemo()).hasSize(6);

        var confirme = paiementService.valider(paiement.id(), challenge.codeDemo(), chefSfbId);
        assertThat(confirme.statut()).isEqualTo(OrdrePaiementStatut.CONFIRME.name());
        assertThat(confirme.validateur()).isNotBlank();
        assertThat(confirme.dateConfirmation()).isNotNull();
    }

    private EquipementDtos.EquipementRequest tablette(String designation, String imei) {
        String suffixe = UUID.randomUUID().toString().substring(0, 8);
        return new EquipementDtos.EquipementRequest(
                TypeEquipement.TABLETTE, designation, null, "SN-" + suffixe,
                "Samsung", "Galaxy Tab A", EtatEquipement.NEUF, null, 150_000.0,
                "Tablette de test", imei + "-" + suffixe);
    }

    private Long utilisateurId(String email) {
        return utilisateurRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
    }

    private String regionAttendue(Comptoir comptoir) {
        Locality locality = comptoir.getLocality();
        while (locality != null && locality.getType() != LocalityType.REGION) {
            locality = locality.getParent();
        }
        return locality != null ? locality.getName() : null;
    }
}
