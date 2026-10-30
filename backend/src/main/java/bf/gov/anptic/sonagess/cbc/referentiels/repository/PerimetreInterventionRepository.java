package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.PerimetreIntervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerimetreInterventionRepository extends JpaRepository<PerimetreIntervention, Long> {

    List<PerimetreIntervention> findByUtilisateurId(Long utilisateurId);
}
