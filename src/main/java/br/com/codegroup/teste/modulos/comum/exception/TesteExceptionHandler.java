package br.com.codegroup.teste.modulos.comum.exception;

import br.com.codegroup.teste.modulos.comum.dto.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class TesteExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ValidacaoException.class)
    public ErrorResponse handleValidacao(ValidacaoException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NaoAutorizadoException.class)
    public ErrorResponse handleNaoAutorizado(NaoAutorizadoException ex) {
        log.error(ex.getMessage());
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var mensagem = ex.getBindingResult().getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .findFirst()
            .orElse("Há itens ausentes no objeto");

        log.error(mensagem);
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), mensagem);
    }
}
