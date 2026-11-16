package br.com.bairrovivo.repository;

import br.com.bairrovivo.domain.PreferenciaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaUsuarioRepository extends JpaRepository<PreferenciaUsuario, Long> {
    Optional<PreferenciaUsuario> findByUsuarioId(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
