package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProducteurRepository extends JpaRepository<Producteur, Long> {

    @Query("""
            select p from Producteur p
            where lower(p.nomPrenom) like lower(concat('%', :q, '%'))
               or p.telephoneMobileMoney like concat('%', :q, '%')
               or lower(p.cnibNumero) like lower(concat('%', :q, '%'))
            order by p.nomPrenom
            """)
    List<Producteur> search(@Param("q") String q);

    Optional<Producteur> findByTelephoneMobileMoney(String telephone);

    boolean existsByCnibNumeroIgnoreCase(String cnibNumero);

    boolean existsByCnibNumeroIgnoreCaseAndIdNot(String cnibNumero, Long id);
}
