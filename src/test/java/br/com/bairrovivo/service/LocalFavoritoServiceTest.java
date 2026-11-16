package br.com.bairrovivo.service;

import br.com.bairrovivo.exception.ApiException;
import br.com.bairrovivo.repository.LocalFavoritoRepository;
import br.com.bairrovivo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalFavoritoServiceTest {

    @Test
    void naoPermiteLerFavoritoDeOutraConta() {
        LocalFavoritoRepository locais = mock(LocalFavoritoRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(locais.findByIdAndUsuarioId(12L, 3L)).thenReturn(Optional.empty());
        LocalFavoritoService service = new LocalFavoritoService(locais, usuarios);

        ApiException exception = assertThrows(ApiException.class, () -> service.obter(3L, 12L));

        assertEquals("LOCAL_NAO_ENCONTRADO", exception.getCodigo());
        verify(locais).findByIdAndUsuarioId(12L, 3L);
    }
}
