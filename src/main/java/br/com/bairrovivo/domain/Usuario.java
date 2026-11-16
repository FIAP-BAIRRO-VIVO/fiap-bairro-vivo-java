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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TB_USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    @Column(name = "EMAIL", nullable = false, length = 180, unique = true)
    private String email;

    @Column(name = "SENHA_HASH", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAPEL", nullable = false, length = 12)
    private Papel papel;

    @Convert(converter = BooleanCharConverter.class)
    @Column(name = "FL_ATIVO", nullable = false, length = 1)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "DT_CRIACAO", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "DT_ATUALIZACAO", nullable = false)
    private Instant atualizadoEm;

    @OneToOne(mappedBy = "usuario")
    private PreferenciaUsuario preferencia;

    @OneToMany(mappedBy = "usuario")
    private List<LocalFavorito> locaisFavoritos = new ArrayList<>();

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, Papel papel) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.papel = papel;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Papel getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void desativar() {
        this.ativo = false;
    }
}
