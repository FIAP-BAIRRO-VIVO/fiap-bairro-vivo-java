package br.com.bairrovivo.repository;

import br.com.bairrovivo.domain.Papel;
import br.com.bairrovivo.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByAtivoTrue();

    Optional<Usuario> findByIdAndAtivoTrue(Long id);
}
