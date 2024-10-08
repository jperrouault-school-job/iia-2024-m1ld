package fr.formation.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.formation.config.JwtUtil;
import fr.formation.model.Utilisateur;
import fr.formation.repo.UtilisateurRepository;
import fr.formation.request.AuthRequest;
import fr.formation.request.SubscribeRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/utilisateur")
@RequiredArgsConstructor
public class UtilisateurApiController {
    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/auth")
    public String auth(@RequestBody AuthRequest request) {
        // On va demander à SPRING SECURITY d'authentifier l'utilisateur
        try {
            Authentication authentication = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            authentication = this.authenticationManager.authenticate(authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return JwtUtil.generate(request.getUsername());
        }

        catch (BadCredentialsException e) {
            return "";
        }
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public String subscribe(@RequestBody SubscribeRequest request) {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setUsername(request.getUsername());
        utilisateur.setPassword(this.passwordEncoder.encode(request.getPassword()));

        this.repository.save(utilisateur);

        return utilisateur.getId();
    }
}
