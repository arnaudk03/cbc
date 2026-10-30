package bf.gov.anptic.sonagess.cbc.audit.repository;

import bf.gov.anptic.sonagess.cbc.audit.domain.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long>, JpaSpecificationExecutor<JournalAudit> {

    Page<JournalAudit> findAllByOrderByDateActionDesc(Pageable pageable);
}
