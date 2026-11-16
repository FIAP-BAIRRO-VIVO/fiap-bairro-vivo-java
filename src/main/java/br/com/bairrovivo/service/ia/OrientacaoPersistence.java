package br.com.bairrovivo.service.ia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrientacaoPersistence {
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public OrientacaoPersistence(EntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long registrar(Long ocorrenciaId, Long usuarioId, Long servicoId, String texto,
                          String modelo, String estado, List<Long> fontes, boolean publica) {
        Long fontePrincipal = fontes.isEmpty() ? null : fontes.get(0);
        StoredProcedureQuery procedure =
                entityManager.createStoredProcedureQuery("PKG_BASE_IA.PR_REGISTRAR_ORIENTACAO");
        procedure.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(2, Long.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(3, Long.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(5, String.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(6, String.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(7, Long.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(8, String.class, ParameterMode.IN);
        procedure.registerStoredProcedureParameter(9, Long.class, ParameterMode.OUT);
        procedure.setParameter(1, ocorrenciaId);
        procedure.setParameter(2, usuarioId);
        procedure.setParameter(3, servicoId);
        procedure.setParameter(4, texto);
        procedure.setParameter(5, modelo);
        procedure.setParameter(6, estado);
        procedure.setParameter(7, fontePrincipal);
        procedure.setParameter(8, publica ? "S" : "N");
        procedure.execute();

        Number generatedId = (Number) procedure.getOutputParameterValue(9);
        Long orientationId = generatedId.longValue();
        for (Long fonteId : fontes.stream().skip(1).distinct().toList()) {
            jdbcTemplate.update("""
                    INSERT INTO TB_ORIENTACAO_FONTE (ID_ORIENTACAO, ID_FONTE)
                    VALUES (?, ?)
                    """, orientationId, fonteId);
        }
        return orientationId;
    }
}
