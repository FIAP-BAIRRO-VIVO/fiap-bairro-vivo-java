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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "TB_PREFERENCIA_USUARIO")
public class PreferenciaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PREFERENCIA")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false, unique = true)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "TEMA", nullable = false, length = 12)
    private Tema tema = Tema.SISTEMA;

    @Convert(converter = BooleanCharConverter.class)
    @Column(name = "ALTO_CONTRASTE", nullable = false, length = 1)
    private boolean altoContraste;

    @UpdateTimestamp
    @Column(name = "DT_ATUALIZACAO", nullable = false)
    private Instant atualizadoEm;

    protected PreferenciaUsuario() {
    }

    public PreferenciaUsuario(Usuario usuario, Tema tema, boolean altoContraste) {
        this.usuario = usuario;
        this.tema = tema;
        this.altoContraste = altoContraste;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public boolean isAltoContraste() {
        return altoContraste;
    }

    public void setAltoContraste(boolean altoContraste) {
        this.altoContraste = altoContraste;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
