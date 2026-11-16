package br.com.bairrovivo.service.ia;

import br.com.bairrovivo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrientacaoRateLimiter {
    private static final long WINDOW_NANOS = 60_000_000_000L;
    private static final int REQUESTS_PER_WINDOW = 5;
    private static final int MAX_TRACKED_USERS = 10_000;
    private final Map<Long, Window> windows = new HashMap<>();

    public synchronized void verificar(Long usuarioId) {
        long now = System.nanoTime();
        Window current = windows.get(usuarioId);
        if (current != null && now - current.startedAtNanos < WINDOW_NANOS) {
            if (current.requests >= REQUESTS_PER_WINDOW) {
                throw new ApiException("LIMITE_DE_REQUISICOES",
                        "Limite de solicitacoes de orientacao excedido. Tente novamente em um minuto.",
                        HttpStatus.TOO_MANY_REQUESTS);
            }
            windows.put(usuarioId, new Window(current.startedAtNanos, current.requests + 1));
            return;
        }

        if (windows.size() >= MAX_TRACKED_USERS) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAtNanos >= WINDOW_NANOS);
            if (windows.size() >= MAX_TRACKED_USERS) {
                throw new ApiException("LIMITE_DE_REQUISICOES",
                        "Servico de orientacao temporariamente ocupado. Tente novamente em um minuto.",
                        HttpStatus.TOO_MANY_REQUESTS);
            }
        }
        windows.put(usuarioId, new Window(now, 1));
    }

    private record Window(long startedAtNanos, int requests) {
    }
}
