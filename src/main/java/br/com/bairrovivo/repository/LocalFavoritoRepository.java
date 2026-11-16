package br.com.bairrovivo.repository;

import br.com.bairrovivo.domain.LocalFavorito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalFavoritoRepository extends JpaRepository<LocalFavorito, Long> {
    Page<LocalFavorito> findAllByUsuarioId(Long usuarioId, Pageable pageable);

    Optional<LocalFavorito> findByIdAndUsuarioId(Long id, Long usuarioId);
}
