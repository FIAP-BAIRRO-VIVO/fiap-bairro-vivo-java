package br.com.bairrovivo.service;

import br.com.bairrovivo.domain.Papel;
import br.com.bairrovivo.domain.Usuario;
import br.com.bairrovivo.dto.auth.CadastroRequest;
import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.PreferenciaUsuarioRepository;
import br.com.bairrovivo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private PreferenciaUsuarioRepository preferencias;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private ApplicationEventPublisher events;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarios, preferencias, passwordEncoder, jwtEncoder, events,
                "bairro-vivo-java", "bairro-vivo-apis", Duration.ofHours(1));
    }

    @Test
    void cadastroPublicoSempreCriaCidadaoMesmoSemPapelNoRequest() {
        when(usuarios.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash");
        Usuario saved = new Usuario("Ana", "ana@example.com", "hash", Papel.CIDADAO);
        ReflectionTestUtils.setField(saved, "id", 42L);
        when(usuarios.saveAndFlush(any(Usuario.class))).thenReturn(saved);
        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        authService.cadastrar(new CadastroRequest("Ana", "Ana@Example.com",
                "senha-segura", "senha-segura"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(captor.capture());
        assertEquals(Papel.CIDADAO, captor.getValue().getPapel());
        assertEquals("ana@example.com", captor.getValue().getEmail());
        verify(preferencias).save(any());
        verify(events).publishEvent(any(Object.class));
    }

    @Test
    void cadastroComEmailExistenteRetornaConflitoSemPersistir() {
        when(usuarios.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> authService.cadastrar(
                new CadastroRequest("Ana", "ana@example.com", "senha-segura", null)));

        assertEquals("EMAIL_JA_CADASTRADO", exception.getCodigo());
        verify(usuarios, never()).saveAndFlush(any());
        verify(preferencias, never()).save(any());
    }
}
