package br.com.bairrovivo.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;

@Configuration
public class JwtConfiguration {

    @Bean
    RSAKey rsaKey(
            @Value("${bairro-vivo.security.private-key-path:}") String privateKeyPath,
            @Value("${bairro-vivo.security.public-key-path:}") String publicKeyPath)
            throws IOException, GeneralSecurityException {
        if (privateKeyPath.isBlank() || publicKeyPath.isBlank()) {
            throw new IllegalStateException("Configure JWT_PRIVATE_KEY_PATH e JWT_PUBLIC_KEY_PATH com chaves RSA.");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(readPem(privateKeyPath)));
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(readPem(publicKeyPath)));
        if (!privateKey.getModulus().equals(publicKey.getModulus())) {
            throw new IllegalStateException("As chaves publica e privada JWT nao formam o mesmo par RSA.");
        }
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("bairro-vivo-rsa-1")
                .build();
    }

    @Bean
    JwtEncoder jwtEncoder(RSAKey rsaKey) {
        ImmutableJWKSet<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    JwtDecoder jwtDecoder(
            RSAKey rsaKey,
            @Value("${bairro-vivo.security.issuer}") String issuer,
            @Value("${bairro-vivo.security.audience}") String audience) throws com.nimbusds.jose.JOSEException {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        OAuth2TokenValidator<Jwt> issuerAndTimeValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<Collection<String>>("aud",
                        values -> values != null && values.contains(audience));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerAndTimeValidator, audienceValidator));
        return decoder;
    }

    @Bean
    JWKSet publicJwkSet(RSAKey rsaKey) throws com.nimbusds.jose.JOSEException {
        return new JWKSet(rsaKey.toPublicJWK());
    }

    private byte[] readPem(String path) throws IOException {
        String pem = Files.readString(Path.of(path))
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }
}
