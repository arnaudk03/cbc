package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauReceptionMagasin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BordereauReceptionMagasinRepository extends JpaRepository<BordereauReceptionMagasin, Long> {

    Optional<BordereauReceptionMagasin> findByBordereauEnlevementId(Long bordereauEnlevementId);

    List<BordereauReceptionMagasin> findByMagasinCentralIdOrderByDateDechargementDesc(Long magasinCentralId);
}
