package br.com.bairrovivo.repository;

import java.time.Instant;

public interface TrechoRagView {
    Long getTrechoId();

    Long getFonteId();

    String getTitulo();

    String getUrl();

    String getTexto();

    Instant getVerificadaEm();
}
