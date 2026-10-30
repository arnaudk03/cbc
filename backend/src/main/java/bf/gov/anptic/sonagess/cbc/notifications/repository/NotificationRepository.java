package bf.gov.anptic.sonagess.cbc.notifications.repository;

import bf.gov.anptic.sonagess.cbc.notifications.domain.Notification;
import bf.gov.anptic.sonagess.cbc.notifications.domain.StatutNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId, Pageable pageable);

    Optional<Notification> findByIdAndUtilisateurId(Long id, Long utilisateurId);

    long countByUtilisateurIdAndStatutNot(Long utilisateurId, StatutNotification statut);

    List<Notification> findTop20ByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);
}
