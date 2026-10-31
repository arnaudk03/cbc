package bf.gov.anptic.sonagess.cbc.equipements.repository;

import bf.gov.anptic.sonagess.cbc.equipements.domain.TabletteDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TabletteDeviceRepository extends JpaRepository<TabletteDevice, Long> {

    Optional<TabletteDevice> findByEquipementId(Long equipementId);

    boolean existsByImeiIgnoreCase(String imei);

    boolean existsByImeiIgnoreCaseAndIdNot(String imei, Long id);
}
