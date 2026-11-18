package br.com.bairrovivo.repository;

import br.com.bairrovivo.dto.ia.OcorrenciaContexto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OcorrenciaRepository {
    private static final String CONTEXTO_SQL = """
            SELECT o.ID_OCORRENCIA AS "id",
                   o.TITULO AS "titulo",
                   o.DESCRICAO AS "descricao",
                   c.ID_CATEGORIA AS "categoriaId",
                   c.NOME AS "categoria",
                   m.ID_MUNICIPIO AS "municipioId",
                   m.NOME AS "municipio",
                   m.UF AS "uf"
              FROM TB_OCORRENCIA o
              JOIN TB_CATEGORIA c ON c.ID_CATEGORIA = o.ID_CATEGORIA
              JOIN TB_MUNICIPIO m ON m.ID_MUNICIPIO = o.ID_MUNICIPIO
             WHERE o.ID_OCORRENCIA = ?
               AND o.FL_EXCLUIDA = 'N'
            """;

    private final JdbcTemplate jdbcTemplate;

    public OcorrenciaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<OcorrenciaContexto> buscarContexto(Long ocorrenciaId) {
        return jdbcTemplate.query(CONTEXTO_SQL, (resultSet, rowNumber) ->
                        new OcorrenciaContexto(
                                resultSet.getLong("id"),
                                resultSet.getString("titulo"),
                                resultSet.getString("descricao"),
                                resultSet.getLong("categoriaId"),
                                resultSet.getString("categoria"),
                                resultSet.getLong("municipioId"),
                                resultSet.getString("municipio"),
                                resultSet.getString("uf")),
                        ocorrenciaId)
                .stream()
                .findFirst();
    }
}
