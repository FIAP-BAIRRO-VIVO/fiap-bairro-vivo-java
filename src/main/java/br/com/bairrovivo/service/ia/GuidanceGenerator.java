package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.repository.TrechoRagView;

import java.util.List;

public interface GuidanceGenerator {
    String gerar(OcorrenciaContexto ocorrencia, List<TrechoRagView> trechos);
}
