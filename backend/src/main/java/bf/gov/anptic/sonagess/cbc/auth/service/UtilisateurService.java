package bf.gov.anptic.sonagess.cbc.auth.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.auth.domain.Role;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.dto.UtilisateurDtos;
import bf.gov.anptic.sonagess.cbc.auth.repository.RefreshTokenRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.RoleRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.ComptoirRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final ComptoirRepository comptoirRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UtilisateurDtos.UtilisateurResponse> lister() {
        return utilisateurRepository.findAllByOrderByNomAscPrenomAsc().stream()
                .map(UtilisateurDtos.UtilisateurResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDtos.RoleOption> listerRoles() {
        return roleRepository.findAllByOrderByCodeAsc().stream()
                .map(UtilisateurDtos.RoleOption::from)
                .toList();
    }

    @Transactional
    public UtilisateurDtos.UtilisateurResponse creer(UtilisateurDtos.UtilisateurRequest request,
                                                      Utilisateur acteur) {
        String email = normaliserEmail(request.email());
        String matricule = normaliserMatricule(request.matricule());
        String telephone = request.telephone().trim();
        verifierUnicite(email, matricule, telephone, null);
        if (request.motDePasse() == null || request.motDePasse().isBlank()) {
            throw new BusinessException("Le mot de passe est obligatoire à la création.");
        }
        verifierMotDePasse(request.motDePasse());
        Set<Role> roles = resoudreRoles(request.roles());
        Comptoir comptoir = resoudreComptoir(request.comptoirId(), roles);

        Utilisateur utilisateur = Utilisateur.builder()
                .matricule(matricule)
                .nom(request.nom().trim().toUpperCase(Locale.ROOT))
                .prenom(request.prenom().trim())
                .email(email)
                .telephone(telephone)
                .motDePasseHash(passwordEncoder.encode(request.motDePasse()))
                .actif(request.actif() == null || request.actif())
                .compteBloque(false)
                .tentativesEchouees(0)
                .comptoir(comptoir)
                .roles(roles)
                .build();
        utilisateurRepository.save(utilisateur);
        auditService.succes(acteur, "CREATION_UTILISATEUR", "Utilisateur", utilisateur.getId(),
                "Utilisateur " + utilisateur.getMatricule() + " créé");
        return UtilisateurDtos.UtilisateurResponse.from(utilisateur);
    }

    @Transactional
    public UtilisateurDtos.UtilisateurResponse modifier(Long utilisateurId,
                                                         UtilisateurDtos.UtilisateurRequest request,
                                                         Utilisateur acteur) {
        Utilisateur utilisateur = charger(utilisateurId);
        String email = normaliserEmail(request.email());
        String matricule = normaliserMatricule(request.matricule());
        String telephone = request.telephone().trim();
        verifierUnicite(email, matricule, telephone, utilisateurId);
        Set<Role> roles = resoudreRoles(request.roles());
        Comptoir comptoir = resoudreComptoir(request.comptoirId(), roles);
        boolean actif = request.actif() == null
                ? Boolean.TRUE.equals(utilisateur.getActif())
                : request.actif();
        verifierModificationAdmin(utilisateur, roles, actif);
        if (utilisateurId.equals(acteur.getId()) && !actif) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            verifierMotDePasse(request.motDePasse());
            utilisateur.setMotDePasseHash(passwordEncoder.encode(request.motDePasse()));
        }
        utilisateur.setMatricule(matricule);
        utilisateur.setNom(request.nom().trim().toUpperCase(Locale.ROOT));
        utilisateur.setPrenom(request.prenom().trim());
        utilisateur.setEmail(email);
        utilisateur.setTelephone(telephone);
        utilisateur.setActif(actif);
        utilisateur.setComptoir(comptoir);
        utilisateur.setRoles(roles);
        utilisateurRepository.save(utilisateur);
        auditService.succes(acteur, "MODIFICATION_UTILISATEUR", "Utilisateur", utilisateurId,
                "Utilisateur " + utilisateur.getMatricule() + " modifié");
        return UtilisateurDtos.UtilisateurResponse.from(utilisateur);
    }

    @Transactional
    public UtilisateurDtos.UtilisateurResponse changerActivation(Long utilisateurId, boolean actif,
                                                                  Utilisateur acteur) {
        Utilisateur utilisateur = charger(utilisateurId);
        if (utilisateurId.equals(acteur.getId()) && !actif) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        verifierModificationAdmin(utilisateur, utilisateur.getRoles(), actif);
        utilisateur.setActif(actif);
        if (actif) {
            utilisateur.setCompteBloque(false);
            utilisateur.setTentativesEchouees(0);
        } else {
            refreshTokenRepository.deleteByUtilisateurId(utilisateurId);
        }
        utilisateurRepository.save(utilisateur);
        auditService.succes(acteur, actif ? "ACTIVATION_UTILISATEUR" : "DESACTIVATION_UTILISATEUR",
                "Utilisateur", utilisateurId, "Utilisateur " + utilisateur.getMatricule());
        return UtilisateurDtos.UtilisateurResponse.from(utilisateur);
    }

    @Transactional
    public void supprimer(Long utilisateurId, Utilisateur acteur) {
        changerActivation(utilisateurId, false, acteur);
        auditService.succes(acteur, "SUPPRESSION_UTILISATEUR", "Utilisateur", utilisateurId,
                "Utilisateur désactivé (suppression logique)");
    }

    @Transactional
    public UtilisateurDtos.AffectationComptoirResponse affecterAuComptoir(Long comptoirId,
                                                                          List<Long> utilisateurIds,
                                                                          Utilisateur acteur) {
        Comptoir comptoir = comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
        Set<Long> idsUniques = new LinkedHashSet<>(utilisateurIds);
        if (idsUniques.contains(null)) {
            throw new BusinessException("Un identifiant d'utilisateur est invalide.");
        }
        List<Utilisateur> utilisateurs = utilisateurRepository.findAllById(idsUniques);
        if (utilisateurs.size() != idsUniques.size()) {
            throw new ResourceNotFoundException("Un ou plusieurs utilisateurs sont introuvables.");
        }
        List<Utilisateur> conflits = utilisateurs.stream()
                .filter(utilisateur -> utilisateur.getComptoir() != null
                        && !comptoirId.equals(utilisateur.getComptoir().getId()))
                .toList();
        if (!conflits.isEmpty()) {
            String noms = conflits.stream()
                    .map(Utilisateur::getNomComplet)
                    .limit(5)
                    .reduce((gauche, droite) -> gauche + ", " + droite)
                    .orElse("");
            if (conflits.size() > 5) {
                noms += "…";
            }
            throw new BusinessException(
                    "Un utilisateur ne peut être affecté qu'à un seul comptoir. Déjà rattaché(s) ailleurs : "
                            + noms + ".", HttpStatus.CONFLICT);
        }
        int affectes = 0;
        for (Utilisateur utilisateur : utilisateurs) {
            if (utilisateur.getComptoir() == null) {
                utilisateur.setComptoir(comptoir);
                affectes++;
            }
        }
        if (affectes > 0) {
            utilisateurRepository.saveAll(utilisateurs);
            auditService.succes(acteur, "AFFECTATION_UTILISATEURS_COMPTOIR", "Comptoir", comptoirId,
                    affectes + " utilisateur(s) affecté(s) au comptoir " + comptoir.getCode());
        }
        return new UtilisateurDtos.AffectationComptoirResponse(
                comptoir.getId(), comptoir.getCode(), comptoir.getNom(), affectes);
    }

    @Transactional(readOnly = true)
    public Utilisateur acteur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private Utilisateur charger(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private Comptoir resoudreComptoir(Long comptoirId, Set<Role> roles) {
        boolean profilTerrain = roles.stream().anyMatch(role ->
                "AGENT_COLLECTEUR".equals(role.getCode()) || "CHEF_COMPTOIR".equals(role.getCode()));
        if (comptoirId == null) {
            if (profilTerrain) {
                throw new BusinessException(
                        "Un comptoir de rattachement est obligatoire pour un agent collecteur ou un chef de comptoir.");
            }
            return null;
        }
        return comptoirRepository.findById(comptoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Comptoir introuvable."));
    }

    private Set<Role> resoudreRoles(List<String> codes) {
        Set<Role> roles = new HashSet<>();
        for (String code : codes) {
            roles.add(roleRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new BusinessException("Rôle inconnu : " + code)));
        }
        return roles;
    }

    private void verifierUnicite(String email, String matricule, String telephone, Long utilisateurId) {
        boolean emailExiste = utilisateurId == null
                ? utilisateurRepository.existsByEmailIgnoreCase(email)
                : utilisateurRepository.existsByEmailIgnoreCaseAndIdNot(email, utilisateurId);
        if (emailExiste) {
            throw new BusinessException("Cette adresse e-mail est déjà utilisée.", HttpStatus.CONFLICT);
        }
        boolean matriculeExiste = utilisateurId == null
                ? utilisateurRepository.existsByMatriculeIgnoreCase(matricule)
                : utilisateurRepository.existsByMatriculeIgnoreCaseAndIdNot(matricule, utilisateurId);
        if (matriculeExiste) {
            throw new BusinessException("Ce matricule est déjà utilisé.", HttpStatus.CONFLICT);
        }
        boolean telephoneExiste = utilisateurId == null
                ? utilisateurRepository.existsByTelephone(telephone)
                : utilisateurRepository.existsByTelephoneAndIdNot(telephone, utilisateurId);
        if (telephoneExiste) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé.", HttpStatus.CONFLICT);
        }
    }

    private void verifierMotDePasse(String motDePasse) {
        if (motDePasse.length() < 8) {
            throw new BusinessException("Le mot de passe doit contenir au moins 8 caractères.");
        }
    }

    private void verifierModificationAdmin(Utilisateur utilisateur, Set<Role> nouveauxRoles, boolean actif) {
        boolean etaitAdminActif = Boolean.TRUE.equals(utilisateur.getActif())
                && utilisateur.hasRole("ADMIN");
        boolean resteraAdminActif = actif
                && nouveauxRoles.stream().anyMatch(role -> "ADMIN".equals(role.getCode()));
        if (etaitAdminActif && !resteraAdminActif
                && utilisateurRepository.countByRoles_CodeAndActifTrue("ADMIN") <= 1) {
            throw new BusinessException("Le dernier administrateur actif ne peut pas être désactivé.");
        }
    }

    private String normaliserEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normaliserMatricule(String matricule) {
        return matricule.trim().toUpperCase(Locale.ROOT);
    }
}
