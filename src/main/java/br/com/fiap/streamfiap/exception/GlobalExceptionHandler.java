package br.com.fiap.streamfiap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConteudoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleConteudoNaoEncontrado(ConteudoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(CreditosInsuficientesException.class)
    public ResponseEntity<Map<String, String>> handleCreditosInsuficientes(CreditosInsuficientesException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(ConteudoIndisponivelException.class)
    public ResponseEntity<Map<String, String>> handleConteudoIndisponivel(ConteudoIndisponivelException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }
    @ExceptionHandler(DuracaoInvalidaException.class)
    public ResponseEntity<String> handleDuracaoInvalida(DuracaoInvalidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<String> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException e){
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(ClassificacaoIndicativaException.class)
    public ResponseEntity<String> handleClassificacaoIndicativa(ClassificacaoIndicativaException e){
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
