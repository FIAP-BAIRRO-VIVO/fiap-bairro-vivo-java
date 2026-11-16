package br.com.bairrovivo.config;

import br.com.bairrovivo.repository.UsuarioRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ActiveAccountJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UsuarioRepository usuarios;
    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public ActiveAccountJwtAuthenticationConverter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long usuarioId;
        try {
            usuarioId = Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("O identificador do usuario no token e invalido.");
        }
        if (usuarios.findByIdAndAtivoTrue(usuarioId).isEmpty()) {
            throw new BadCredentialsException("A conta associada ao token esta desativada ou nao existe.");
        }
        return new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt), jwt.getSubject());
    }
}
