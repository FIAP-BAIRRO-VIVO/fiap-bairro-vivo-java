package br.com.bairrovivo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import br.com.bairrovivo.domain.OrientacaoIA;

import java.util.List;

public interface TrechoRagRepository extends Repository<OrientacaoIA, Long> {

    @Query(value = """
            SELECT ID_TRECHO AS "trechoId",
                   ID_FONTE AS "fonteId",
                   TITULO_FONTE AS "titulo",
                   URL_FONTE AS "url",
                   TEXTO_TRECHO AS "texto",
                   DT_VERIFICACAO AS "verificadaEm"
              FROM VW_TRECHOS_RAG_APROVADOS
             WHERE ID_MUNICIPIO = :municipioId
               AND ID_CATEGORIA = :categoriaId
             ORDER BY DT_VERIFICACAO DESC, ID_TRECHO
             FETCH FIRST 5 ROWS ONLY
            """, nativeQuery = true)
    List<TrechoRagView> buscarTrechosAprovados(
            @Param("municipioId") Long municipioId,
            @Param("categoriaId") Long categoriaId);
}
