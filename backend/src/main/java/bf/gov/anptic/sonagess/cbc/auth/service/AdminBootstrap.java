package bf.gov.anptic.sonagess.cbc.auth.service;

import bf.gov.anptic.sonagess.cbc.auth.domain.Role;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.RoleRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.bootstrap.admin.enabled:false}")
    private boolean enabled;

    @Value("${application.bootstrap.admin.matricule:ADM001}")
    private String matricule;

    @Value("${application.bootstrap.admin.nom:ADMIN}")
    private String nom;

    @Value("${application.bootstrap.admin.prenom:SONAGESS}")
    private String prenom;

    @Value("${application.bootstrap.admin.email:}")
    private String email;

    @Value("${application.bootstrap.admin.telephone:}")
    private String telephone;

    @Value("${application.bootstrap.admin.mot-de-passe:}")
    private String motDePasse;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }
        if (!enabled) {
            log.warn("Aucun utilisateur en base. Activez BOOTSTRAP_ADMIN_ENABLED pour créer le premier administrateur.");
            return;
        }
        if (email.isBlank() || telephone.isBlank() || motDePasse.isBlank()) {
            log.warn("Bootstrap administrateur incomplet : renseignez BOOTSTRAP_ADMIN_EMAIL, "
                    + "BOOTSTRAP_ADMIN_TELEPHONE et BOOTSTRAP_ADMIN_PASSWORD.");
            return;
        }
        if (motDePasse.length() < 8) {
            log.warn("Bootstrap administrateur ignoré : le mot de passe doit contenir au moins 8 caractères.");
            return;
        }
        Role roleAdmin = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Rôle ADMIN introuvable."));
        Utilisateur administrateur = Utilisateur.builder()
                .matricule(matricule.trim().toUpperCase(Locale.ROOT))
                .nom(nom.trim().toUpperCase(Locale.ROOT))
                .prenom(prenom.trim())
                .email(email.trim().toLowerCase(Locale.ROOT))
                .telephone(telephone.trim())
                .motDePasseHash(passwordEncoder.encode(motDePasse))
                .actif(true)
                .compteBloque(false)
                .tentativesEchouees(0)
                .roles(Set.of(roleAdmin))
                .build();
        utilisateurRepository.save(administrateur);
        log.info("Administrateur initial créé : {}", administrateur.getMatricule());
    }
}
