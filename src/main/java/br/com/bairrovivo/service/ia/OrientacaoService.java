package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.domain.EstadoOrientacao;
import br.com.bairrovivo.domain.OrientacaoIA;
import br.com.bairrovivo.dto.ia.CanalResposta;
import br.com.bairrovivo.dto.ia.FonteResposta;
import br.com.bairrovivo.dto.ia.MunicipioResposta;
import br.com.bairrovivo.dto.ia.OcorrenciaContexto;
import br.com.bairrovivo.dto.ia.OrientacaoResposta;
import br.com.bairrovivo.dto.ia.OrientacaoResumo;
import br.com.bairrovivo.dto.PaginaResposta;
import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.OcorrenciaRepository;
import br.com.bairrovivo.repository.OrientacaoIARepository;
import br.com.bairrovivo.repository.ServicoRagRepository;
import br.com.bairrovivo.repository.ServicoRagView;
import br.com.bairrovivo.repository.TrechoRagRepository;
import br.com.bairrovivo.repository.TrechoRagView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrientacaoService {
    private static final Logger logger = LoggerFactory.getLogger(OrientacaoService.class);
    private static final String RESPOSTA_SEM_FONTE =
            "Nao encontramos fontes oficiais verificadas para esta localidade e categoria.";
    private static final String RESPOSTA_LOCALIDADE =
            "Nao foi possivel confirmar o municipio da ocorrencia na base de dados.";

    private final OcorrenciaRepository ocorrencias;
    private final TrechoRagRepository trechos;
    private final ServicoRagRepository servicos;
    private final OrientacaoPersistence persistence;
    private final OrientacaoIARepository orientacoes;
    private final GuidanceGenerator generator;
    private final GuidanceOutputValidator outputValidator;
    private final OrientacaoRateLimiter rateLimiter;
    private final String modelName;

    public OrientacaoService(
            OcorrenciaRepository ocorrencias,
            TrechoRagRepository trechos,
            ServicoRagRepository servicos,
            OrientacaoPersistence persistence,
            OrientacaoIARepository orientacoes,
            GuidanceOutputValidator outputValidator,
            OrientacaoRateLimiter rateLimiter,
            ObjectProvider<GuidanceGenerator> generatorProvider,
            @Value("${bairro-vivo.ai.model-name}") String modelName) {
        this.ocorrencias = ocorrencias;
        this.trechos = trechos;
        this.servicos = servicos;
        this.persistence = persistence;
        this.orientacoes = orientacoes;
        this.outputValidator = outputValidator;
        this.rateLimiter = rateLimiter;
        this.generator = generatorProvider.getIfAvailable();
        this.modelName = modelName;
    }

    @Transactional
    public OrientacaoResposta solicitar(Long usuarioId, Long ocorrenciaId) {
        rateLimiter.verificar(usuarioId);
        OcorrenciaContexto ocorrencia = ocorrencias.buscarContexto(ocorrenciaId)
                .orElseThrow(() -> new ApiException("OCORRENCIA_NAO_ENCONTRADA",
                        "A ocorrencia nao foi encontrada na base de dados.", HttpStatus.NOT_FOUND));
        if (ocorrencia.municipioId() == null || ocorrencia.municipio() == null || ocorrencia.uf() == null) {
            return registrarFallback(ocorrencia, usuarioId, EstadoOrientacao.LOCALIDADE_NAO_CONFIRMADA,
                    RESPOSTA_LOCALIDADE);
        }
        if (!"São Paulo".equalsIgnoreCase(ocorrencia.municipio().trim())
                || !"SP".equalsIgnoreCase(ocorrencia.uf().trim()) || ocorrencia.categoriaId() == null) {
            return registrarFallback(ocorrencia, usuarioId, EstadoOrientacao.SEM_FONTE, RESPOSTA_SEM_FONTE);
        }

        List<TrechoRagView> fontes = trechos.buscarTrechosAprovados(ocorrencia.municipioId(), ocorrencia.categoriaId());
        if (fontes.isEmpty()) {
            return registrarFallback(ocorrencia, usuarioId, EstadoOrientacao.SEM_FONTE, RESPOSTA_SEM_FONTE);
        }
        if (generator == null) {
            registrar(ocorrencia, usuarioId, null, EstadoOrientacao.ERRO,
                    "Orientacao temporariamente indisponivel.", List.of(), false);
            throw new ApiException("IA_INDISPONIVEL",
                    "O servico de orientacao esta indisponivel no momento.", HttpStatus.SERVICE_UNAVAILABLE);
        }

        List<ServicoRagView> canaisVerificados =
                servicos.buscarServicosComCanaisVerificados(ocorrencia.municipioId(), ocorrencia.categoriaId());
        String texto;
        try {
            texto = generator.gerar(ocorrencia, fontes);
            if (texto == null || texto.isBlank()) {
                throw new IllegalStateException("O modelo retornou uma orientacao vazia.");
            }
            if (texto.length() > 5_000) {
                throw new IllegalStateException("O modelo retornou uma orientacao acima do limite.");
            }
            outputValidator.validar(texto, fontes);
        } catch (RuntimeException exception) {
            logger.error("Falha ao gerar orientacao para ocorrencia {}", ocorrenciaId, exception);
            registrar(ocorrencia, usuarioId, null, EstadoOrientacao.ERRO,
                    "Orientacao temporariamente indisponivel.", List.of(), false);
            throw new ApiException("IA_INDISPONIVEL",
                    "O servico de orientacao esta indisponivel no momento.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        Long servicoId = canaisVerificados.isEmpty() ? null : canaisVerificados.get(0).getServicoId();
        return registrar(ocorrencia, usuarioId, servicoId, EstadoOrientacao.DISPONIVEL,
                texto.strip(), fontes, canaisVerificados, true);
    }

    @Transactional(readOnly = true)
    public PaginaResposta<OrientacaoResumo> listarPublicas(Long ocorrenciaId, Pageable pageable) {
        validarPaginacao(pageable);
        Page<OrientacaoIA> page = orientacoes.findByOcorrenciaIdAndPublicaTrueOrderByCriadaEmDesc(
                ocorrenciaId, pageable);
        return PaginaResposta.de(page, this::resumo);
    }

    @Transactional(readOnly = true)
    public OrientacaoResumo obterPublica(Long id) {
        return orientacoes.findByIdAndPublicaTrue(id)
                .map(this::resumo)
                .orElseThrow(() -> new ApiException("ORIENTACAO_NAO_ENCONTRADA",
                        "Orientacao publica nao encontrada.", HttpStatus.NOT_FOUND));
    }

    private OrientacaoResposta registrarFallback(OcorrenciaContexto ocorrencia, Long usuarioId,
                                                  EstadoOrientacao estado, String texto) {
        return registrar(ocorrencia, usuarioId, null, estado, texto, List.of(), false);
    }

    private OrientacaoResposta registrar(OcorrenciaContexto ocorrencia, Long usuarioId, Long servicoId,
                                         EstadoOrientacao estado, String texto,
                                         List<TrechoRagView> fontes, boolean publica) {
        return registrar(ocorrencia, usuarioId, servicoId, estado, texto, fontes, List.of(), publica);
    }

    private OrientacaoResposta registrar(OcorrenciaContexto ocorrencia, Long usuarioId, Long servicoId,
                                         EstadoOrientacao estado, String texto,
                                         List<TrechoRagView> fontes, List<ServicoRagView> canaisVerificados,
                                         boolean publica) {
        List<Long> fonteIds = fontes.stream().map(TrechoRagView::getFonteId).distinct().toList();
        String model = estado == EstadoOrientacao.DISPONIVEL ? modelName : null;
        Long id = persistence.registrar(ocorrencia.id(), usuarioId, servicoId, texto, model,
                estado.name(), fonteIds, publica);

        List<FonteResposta> fontesResposta = fontes.stream()
                .map(fonte -> new FonteResposta(fonte.getTitulo(), fonte.getUrl(), fonte.getVerificadaEm()))
                .toList();
        List<CanalResposta> canais = canaisVerificados.stream()
                .map(canal -> new CanalResposta(canal.getTipo(), canal.getValor(), canal.getHorario()))
                .toList();
        ServicoRagView servico = canaisVerificados.isEmpty() ? null : canaisVerificados.get(0);
        MunicipioResposta municipio = ocorrencia.municipio() == null
                ? null : new MunicipioResposta(ocorrencia.municipio(), ocorrencia.uf());
        return new OrientacaoResposta(id.toString(), ocorrencia.id().toString(), estado, municipio,
                servico == null ? null : servico.getOrgao(),
                servico == null ? null : servico.getServico(),
                canais, texto, fontesResposta, Instant.now());
    }

    private OrientacaoResumo resumo(OrientacaoIA orientacao) {
        return new OrientacaoResumo(orientacao.getId().toString(), orientacao.getOcorrenciaId().toString(),
                orientacao.getEstado(), orientacao.getTexto(), orientacao.getCriadaEm());
    }

    private void validarPaginacao(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new ApiException("PAGINACAO_INVALIDA",
                    "O tamanho maximo da pagina e 100.", HttpStatus.BAD_REQUEST);
        }
    }
}
