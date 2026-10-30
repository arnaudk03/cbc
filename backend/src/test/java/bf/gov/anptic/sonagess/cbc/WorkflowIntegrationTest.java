package bf.gov.anptic.sonagess.cbc;

import bf.gov.anptic.sonagess.cbc.common.BusinessException;
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
