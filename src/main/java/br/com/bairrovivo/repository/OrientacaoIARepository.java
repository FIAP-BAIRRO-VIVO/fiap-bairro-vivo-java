package br.com.bairrovivo.repository;

import br.com.bairrovivo.domain.OrientacaoIA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrientacaoIARepository extends JpaRepository<OrientacaoIA, Long> {
    Page<OrientacaoIA> findByOcorrenciaIdAndPublicaTrueOrderByCriadaEmDesc(
            Long ocorrenciaId, Pageable pageable);

    Optional<OrientacaoIA> findByIdAndPublicaTrue(Long id);
}
