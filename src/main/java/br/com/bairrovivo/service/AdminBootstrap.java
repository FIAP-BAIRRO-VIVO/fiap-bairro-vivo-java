package br.com.bairrovivo.service;

import br.com.bairrovivo.domain.Papel;
import br.com.bairrovivo.domain.Usuario;
import br.com.bairrovivo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            @Value("${bairro-vivo.admin.email:}") String email,
            @Value("${bairro-vivo.admin.password:}") String password) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) {
            return;
        }
        if (email.isBlank() || password.isBlank() || password.length() < 12) {
            throw new IllegalStateException(
                    "Configure BAIRROVIVO_ADMIN_EMAIL e BAIRROVIVO_ADMIN_PASSWORD (minimo 12 caracteres) juntos.");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!usuarios.existsByEmailIgnoreCase(normalizedEmail)) {
            usuarios.save(new Usuario(normalizedEmail, normalizedEmail,
                    passwordEncoder.encode(password), Papel.ADMIN));
        }
    }
}
