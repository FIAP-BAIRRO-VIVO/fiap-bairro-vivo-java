package br.com.bairrovivo.domain;

import br.com.bairrovivo.domain.converter.BooleanCharConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "TB_ORIENTACAO_IA")
public class OrientacaoIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ORIENTACAO")
    private Long id;

    @Column(name = "ID_OCORRENCIA", nullable = false)
    private Long ocorrenciaId;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long usuarioId;

    @Column(name = "ID_SERVICO")
    private Long servicoId;

    @Lob
    @Column(name = "TEXTO_ORIENTACAO", nullable = false)
    private String texto;

    @Column(name = "MODELO_LLM", length = 100)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_ORIENTACAO", nullable = false, length = 32)
    private EstadoOrientacao estado;

    @Convert(converter = BooleanCharConverter.class)
    @Column(name = "FL_PUBLICA", nullable = false, length = 1)
    private boolean publica;

    @CreationTimestamp
    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    private Instant criadaEm;

    protected OrientacaoIA() {
    }

    public Long getId() {
        return id;
    }

    public Long getOcorrenciaId() {
        return ocorrenciaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public String getTexto() {
        return texto;
    }

    public String getModelo() {
        return modelo;
    }

    public EstadoOrientacao getEstado() {
        return estado;
    }

    public boolean isPublica() {
        return publica;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
