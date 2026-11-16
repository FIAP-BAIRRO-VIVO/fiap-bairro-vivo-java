package br.com.bairrovivo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import br.com.bairrovivo.domain.OrientacaoIA;

import java.util.List;

public interface ServicoRagRepository extends Repository<OrientacaoIA, Long> {

    @Query(value = """
            SELECT DISTINCT s.ID_SERVICO AS "servicoId",
                   o.NOME AS "orgao",
                   s.NOME AS "servico",
                   c.TIPO AS "tipo",
                   c.VALOR AS "valor",
                   c.HORARIO_ATENDIMENTO AS "horario"
              FROM TB_SERVICO_PUBLICO s
              JOIN TB_ORGAO_PUBLICO o ON o.ID_ORGAO = s.ID_ORGAO
              JOIN TB_SERVICO_CATEGORIA sc ON sc.ID_SERVICO = s.ID_SERVICO
              JOIN TB_CANAL_ATENDIMENTO c ON c.ID_SERVICO = s.ID_SERVICO
             WHERE o.ID_MUNICIPIO = :municipioId
               AND sc.ID_CATEGORIA = :categoriaId
               AND o.FL_ATIVO = 'S'
               AND s.FL_ATIVO = 'S'
               AND c.FL_VERIFICADO = 'S'
               AND EXISTS (
                   SELECT 1
                     FROM TB_FONTE_OFICIAL f
                     JOIN VW_TRECHOS_RAG_APROVADOS v ON v.ID_FONTE = f.ID_FONTE
                    WHERE f.ID_SERVICO = s.ID_SERVICO
                      AND v.ID_MUNICIPIO = :municipioId
                      AND v.ID_CATEGORIA = :categoriaId
               )
             ORDER BY s.ID_SERVICO, c.TIPO
            """, nativeQuery = true)
    List<ServicoRagView> buscarServicosComCanaisVerificados(
            @Param("municipioId") Long municipioId,
            @Param("categoriaId") Long categoriaId);
}
