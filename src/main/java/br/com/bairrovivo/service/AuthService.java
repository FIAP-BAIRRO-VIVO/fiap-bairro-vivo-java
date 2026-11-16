package br.com.bairrovivo.service;

import br.com.bairrovivo.domain.Papel;
import br.com.bairrovivo.domain.PreferenciaUsuario;
import br.com.bairrovivo.domain.Tema;
import br.com.bairrovivo.domain.Usuario;
import br.com.bairrovivo.dto.auth.CadastroRequest;
import br.com.bairrovivo.dto.auth.LoginRequest;
import br.com.bairrovivo.dto.auth.TokenResponse;
import br.com.bairrovivo.dto.auth.UsuarioResumo;
import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.PreferenciaUsuarioRepository;
import br.com.bairrovivo.repository.UsuarioRepository;
import br.com.bairrovivo.service.event.UsuarioCadastradoEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PreferenciaUsuarioRepository preferencias;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final String issuer;
    private final String audience;
    private final Duration tokenTtl;

    public AuthService(
            UsuarioRepository usuarios,
            PreferenciaUsuarioRepository preferencias,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            ApplicationEventPublisher events,
            @Value("${bairro-vivo.security.issuer}") String issuer,
            @Value("${bairro-vivo.security.audience}") String audience,
            @Value("${bairro-vivo.security.token-ttl}") Duration tokenTtl) {
        this.usuarios = usuarios;
        this.preferencias = preferencias;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.events = events;
        this.issuer = issuer;
        this.audience = audience;
        this.tokenTtl = tokenTtl;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public TokenResponse cadastrar(CadastroRequest request) {
        if (request.confirmacaoSenha() != null
                && !request.confirmacaoSenha().isBlank()
                && !request.senha().equals(request.confirmacaoSenha())) {
            throw new ApiException("SENHAS_DIFERENTES", "A confirmacao da senha nao corresponde.",
                    HttpStatus.BAD_REQUEST);
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ApiException("EMAIL_JA_CADASTRADO", "Ja existe uma conta com este email.",
                    HttpStatus.CONFLICT);
        }

        Usuario usuario = usuarios.saveAndFlush(new Usuario(
                request.nome().trim(), email, passwordEncoder.encode(request.senha()), Papel.CIDADAO));
        preferencias.save(new PreferenciaUsuario(usuario, Tema.SISTEMA, false));
        events.publishEvent(new UsuarioCadastradoEvent(usuario.getId()));
        return emitirToken(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email().trim())
                .filter(Usuario::isAtivo)
                .filter(conta -> passwordEncoder.matches(request.senha(), conta.getSenhaHash()))
                .orElseThrow(() -> new ApiException(
                        "CREDENCIAIS_INVALIDAS", "Email ou senha invalidos.", HttpStatus.UNAUTHORIZED));
        return emitirToken(usuario);
    }

    private TokenResponse emitirToken(Usuario usuario) {
        Instant agora = clock.instant();
        Instant expiraEm = agora.plus(tokenTtl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("roles", List.of(usuario.getPapel().name()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).keyId("bairro-vivo-rsa-1").build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", tokenTtl.toSeconds(),
                new UsuarioResumo(usuario.getId().toString(), usuario.getNome(), usuario.getEmail(), usuario.getPapel()));
    }
}
