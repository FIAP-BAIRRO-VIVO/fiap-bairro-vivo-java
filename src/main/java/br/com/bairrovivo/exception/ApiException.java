package br.com.bairrovivo.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final String codigo;
    private final HttpStatus status;

    public ApiException(String codigo, String mensagem, HttpStatus status) {
        super(mensagem);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
