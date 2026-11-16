package br.com.bairrovivo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "TB_LOCAL_FAVORITO")
public class LocalFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOCAL")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private Usuario usuario;

    @Column(name = "APELIDO", nullable = false, length = 80)
    private String apelido;

    @Column(name = "LATITUDE", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @CreationTimestamp
    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    private Instant criadoEm;

    protected LocalFavorito() {
    }

    public LocalFavorito(Usuario usuario, String apelido, BigDecimal latitude, BigDecimal longitude) {
        this.usuario = usuario;
        this.apelido = apelido;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
