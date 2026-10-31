package bf.gov.anptic.sonagess.cbc.equipements.repository;

import bf.gov.anptic.sonagess.cbc.equipements.domain.Equipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TypeEquipement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {

    List<Equipement> findAllByOrderByTypeAscDesignationAsc();

    List<Equipement> findByTypeOrderByDesignationAsc(TypeEquipement type);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByNumeroSerieIgnoreCaseAndIdNot(String numeroSerie, Long id);
}
