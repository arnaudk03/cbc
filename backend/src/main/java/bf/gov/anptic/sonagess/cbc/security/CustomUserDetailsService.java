package bf.gov.anptic.sonagess.cbc.security;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return new CustomUserDetails(utilisateur);
    }

    @Transactional(readOnly = true)
    public CustomUserDetails loadUserById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return new CustomUserDetails(utilisateur);
    }
}
