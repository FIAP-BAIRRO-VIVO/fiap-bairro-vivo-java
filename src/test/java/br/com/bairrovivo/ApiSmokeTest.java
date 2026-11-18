package br.com.bairrovivo;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import br.com.bairrovivo.dto.ia.OcorrenciaContexto;
import br.com.bairrovivo.repository.OcorrenciaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSmokeTest {
    private static final Path KEY_DIRECTORY = createKeys();

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OcorrenciaRepository ocorrencias;

    @LocalServerPort
    private int port;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:bairrovivo;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=true");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.rabbitmq.dynamic", () -> "false");
        registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
        registry.add("bairro-vivo.security.private-key-path",
                () -> KEY_DIRECTORY.resolve("jwt-private.pem").toString());
        registry.add("bairro-vivo.security.public-key-path",
                () -> KEY_DIRECTORY.resolve("jwt-public.pem").toString());
    }

    @AfterAll
    static void removeGeneratedKeys() throws IOException {
        Files.deleteIfExists(KEY_DIRECTORY.resolve("jwt-private.pem"));
        Files.deleteIfExists(KEY_DIRECTORY.resolve("jwt-public.pem"));
        Files.deleteIfExists(KEY_DIRECTORY);
    }

    @Test
    void registersCitizenAuthenticatesAndProtectsAdminRoute() {
        Map<String, Object> request = Map.of(
                "nome", "Ana Silva",
                "email", "ana.smoke@example.com",
                "senha", "senha-segura-123",
                "confirmacaoSenha", "senha-segura-123",
                "papel", "ADMIN");

        ResponseEntity<JsonNode> registration = rest.postForEntity(
                url("/api/v1/auth/registrar"), request, JsonNode.class);

        assertEquals(HttpStatus.CREATED, registration.getStatusCode());
        JsonNode body = registration.getBody();
        assertEquals("CIDADAO", body.path("usuario").path("papel").asText());
        String token = body.path("accessToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<JsonNode> profile = rest.exchange(
                url("/api/v1/usuarios/me"), HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        ResponseEntity<JsonNode> admin = rest.exchange(
                url("/api/v1/admin/health"), HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        ResponseEntity<Void> deactivation = rest.exchange(
                url("/api/v1/usuarios/me"), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        ResponseEntity<JsonNode> deactivatedProfile = rest.exchange(
                url("/api/v1/usuarios/me"), HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);

        assertEquals(HttpStatus.OK, profile.getStatusCode());
        assertEquals("ana.smoke@example.com", profile.getBody().path("email").asText());
        assertEquals(HttpStatus.FORBIDDEN, admin.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, admin.getHeaders().getContentType());
        assertEquals(HttpStatus.NO_CONTENT, deactivation.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, deactivatedProfile.getStatusCode());
        assertFalse(deactivatedProfile.getBody().path("correlationId").asText().isBlank());
    }

    @Test
    void readsOccurrenceContextFromSharedDatabaseAndSkipsDeletedOccurrences() {
        createOccurrenceTables();
        jdbcTemplate.update("INSERT INTO TB_MUNICIPIO (ID_MUNICIPIO, NOME, UF) VALUES (?, ?, ?)",
                3550308L, "São Paulo", "SP");
        jdbcTemplate.update("INSERT INTO TB_CATEGORIA (ID_CATEGORIA, NOME) VALUES (?, ?)",
                12L, "Via pública");
        jdbcTemplate.update("""
                INSERT INTO TB_OCORRENCIA
                    (ID_OCORRENCIA, ID_CATEGORIA, ID_MUNICIPIO, TITULO, DESCRICAO, FL_EXCLUIDA)
                VALUES (?, ?, ?, ?, ?, ?)
                """, 101L, 12L, 3550308L, "Buraco na via", "Buraco próximo ao cruzamento.", "N");
        jdbcTemplate.update("""
                INSERT INTO TB_OCORRENCIA
                    (ID_OCORRENCIA, ID_CATEGORIA, ID_MUNICIPIO, TITULO, DESCRICAO, FL_EXCLUIDA)
                VALUES (?, ?, ?, ?, ?, ?)
                """, 102L, 12L, 3550308L, "Ocorrência removida", "Descrição", "S");

        OcorrenciaContexto contexto = ocorrencias.buscarContexto(101L).orElseThrow();

        assertEquals(101L, contexto.id());
        assertEquals("Buraco na via", contexto.titulo());
        assertEquals("Buraco próximo ao cruzamento.", contexto.descricao());
        assertEquals(12L, contexto.categoriaId());
        assertEquals("Via pública", contexto.categoria());
        assertEquals(3550308L, contexto.municipioId());
        assertEquals("São Paulo", contexto.municipio());
        assertEquals("SP", contexto.uf());
        assertFalse(ocorrencias.buscarContexto(102L).isPresent());
        assertFalse(ocorrencias.buscarContexto(999L).isPresent());
    }

    @Test
    void returnsNotFoundWhenOccurrenceIsAbsentFromSharedDatabase() {
        createOccurrenceTables();
        Map<String, Object> registrationRequest = Map.of(
                "nome", "Maria Teste",
                "email", "maria.occurrence-smoke@example.com",
                "senha", "senha-segura-123",
                "confirmacaoSenha", "senha-segura-123");
        JsonNode registration = rest.postForObject(
                url("/api/v1/auth/registrar"), registrationRequest, JsonNode.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(registration.path("accessToken").asText());
        ResponseEntity<JsonNode> response = rest.exchange(
                url("/api/v1/ia/orientacoes"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("ocorrenciaId", 999L), headers),
                JsonNode.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("OCORRENCIA_NAO_ENCONTRADA", response.getBody().path("codigo").asText());
    }

    private void createOccurrenceTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS TB_MUNICIPIO (
                    ID_MUNICIPIO BIGINT PRIMARY KEY,
                    NOME VARCHAR(120) NOT NULL,
                    UF CHAR(2) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS TB_CATEGORIA (
                    ID_CATEGORIA BIGINT PRIMARY KEY,
                    NOME VARCHAR(100) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS TB_OCORRENCIA (
                    ID_OCORRENCIA BIGINT PRIMARY KEY,
                    ID_CATEGORIA BIGINT NOT NULL,
                    ID_MUNICIPIO BIGINT NOT NULL,
                    TITULO VARCHAR(150) NOT NULL,
                    DESCRICAO CLOB NOT NULL,
                    FL_EXCLUIDA CHAR(1) NOT NULL
                )
                """);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static Path createKeys() {
        try {
            Path directory = Files.createTempDirectory("bairro-vivo-jwt-test");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            Files.writeString(directory.resolve("jwt-private.pem"), pem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
            Files.writeString(directory.resolve("jwt-public.pem"), pem("PUBLIC KEY", keyPair.getPublic().getEncoded()));
            return directory;
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static String pem(String label, byte[] encodedKey) {
        return "-----BEGIN " + label + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encodedKey)
                + "\n-----END " + label + "-----\n";
    }
}
