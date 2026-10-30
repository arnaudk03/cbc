package bf.gov.anptic.sonagess.cbc.security;

import bf.gov.anptic.sonagess.cbc.auth.domain.Permission;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String matricule;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String telephone;
    private final String motDePasseHash;
    private final boolean actif;
    private final boolean compteBloque;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.matricule = utilisateur.getMatricule();
        this.nom = utilisateur.getNom();
        this.prenom = utilisateur.getPrenom();
        this.email = utilisateur.getEmail();
        this.telephone = utilisateur.getTelephone();
        this.motDePasseHash = utilisateur.getMotDePasseHash();
        this.actif = Boolean.TRUE.equals(utilisateur.getActif());
        this.compteBloque = Boolean.TRUE.equals(utilisateur.getCompteBloque());
        this.roles = utilisateur.getRoles().stream()
                .map(role -> role.getCode().toUpperCase())
                .collect(Collectors.toCollection(HashSet::new));
        this.permissions = utilisateur.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toCollection(HashSet::new));

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        roles.forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        permissions.forEach(permission -> grantedAuthorities.add(new SimpleGrantedAuthority(permission)));
        this.authorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return motDePasseHash;
    }

    @Override
    public String getUsername() {
        return matricule;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !compteBloque;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
