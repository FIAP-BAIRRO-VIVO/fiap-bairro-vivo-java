package br.com.bairrovivo.web;

import br.com.bairrovivo.dto.ApiError;
import br.com.bairrovivo.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getCodigo(), exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiError.DetalheErro> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::detail)
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDACAO_INVALIDA",
                "Confira os dados informados.", details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraint(ConstraintViolationException exception, HttpServletRequest request) {
        List<ApiError.DetalheErro> details = exception.getConstraintViolations().stream()
                .map(error -> new ApiError.DetalheErro(error.getPropertyPath().toString(), error.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDACAO_INVALIDA",
                "Confira os dados informados.", details, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA",
                "O corpo da requisicao nao corresponde ao formato esperado.", List.of(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "Um parametro da requisicao possui formato invalido.",
                List.of(new ApiError.DetalheErro(exception.getName(), "Valor invalido.")), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        logger.warn("Violacao de integridade ao processar requisicao {}", correlationId(request), exception);
        return response(HttpStatus.CONFLICT, "CONFLITO_DE_DADOS",
                "Os dados informados conflitam com um registro existente.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        logger.error("Falha inesperada na requisicao {}", correlationId(request), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Nao foi possivel concluir a requisicao.", List.of(), request);
    }

    private ApiError.DetalheErro detail(FieldError error) {
        return new ApiError.DetalheErro(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               List<ApiError.DetalheErro> details, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(code, message, details, correlationId(request)));
    }

    private String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        return value == null ? null : value.toString();
    }
}
