package bf.gov.anptic.sonagess.cbc.auth.repository;

import bf.gov.anptic.sonagess.cbc.auth.domain.TabletteDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TabletteDeviceRepository extends JpaRepository<TabletteDevice, Long> {

    List<TabletteDevice> findByUtilisateurId(Long utilisateurId);

    Optional<TabletteDevice> findByNumeroSerie(String numeroSerie);
}
