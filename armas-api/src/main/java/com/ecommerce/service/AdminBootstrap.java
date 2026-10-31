package com.ecommerce.service;

import com.ecommerce.model.Usuario;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class AdminBootstrap {
    @ConfigProperty(name = "arsenal.admin.email")
    Optional<String> email;

    @ConfigProperty(name = "arsenal.admin.password")
    Optional<String> senha;

    @Transactional
    void iniciar(@Observes StartupEvent event) {
        if (LaunchMode.current() == LaunchMode.TEST) return;
        if (email.isEmpty() || senha.isEmpty() || email.get().isBlank() || senha.get().isBlank()) return;
        String normalizado = email.get().trim().toLowerCase(Locale.ROOT);
        if (Usuario.count("email", normalizado) > 0) return;
        if (senha.get().length() < 12) throw new IllegalStateException("A senha inicial do administrador deve ter ao menos 12 caracteres.");
        Usuario admin = new Usuario();
        admin.email = normalizado;
        admin.senhaHash = BcryptUtil.bcryptHash(senha.get());
        admin.perfil = "ADMIN";
        admin.persist();
    }
}
