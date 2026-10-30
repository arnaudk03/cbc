package bf.gov.anptic.sonagess.cbc.collectes.repository;

import bf.gov.anptic.sonagess.cbc.collectes.domain.BordereauReceptionBrbc;
import bf.gov.anptic.sonagess.cbc.collectes.domain.StatutValidationBrbc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BordereauReceptionBrbcRepository extends JpaRepository<BordereauReceptionBrbc, Long> {

    Optional<BordereauReceptionBrbc> findByNumeroBrbc(String numeroBrbc);

    List<BordereauReceptionBrbc> findByStatutValidationOrderByDateEmissionDesc(StatutValidationBrbc statut);

    long countByStatutValidation(StatutValidationBrbc statut);

    @Query("""
            select coalesce(sum(b.montantNetPayerCfa), 0)
            from BordereauReceptionBrbc b
            where b.statutValidation = :statut
            """)
    Double sumMontantByStatut(@Param("statut") StatutValidationBrbc statut);
}
