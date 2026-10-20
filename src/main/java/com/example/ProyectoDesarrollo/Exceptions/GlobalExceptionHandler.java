package com.example.ProyectoDesarrollo.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;


@RestControllerAdvice(basePackages = "com.example.ProyectoDesarrollo.Controllers.API")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);



    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> recursoNoEncontrado(RecursoNoEncontradoException ex,
                                                             HttpServletRequest req) {
        log.warn("Recurso no encontrado en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex,
                                                      HttpServletRequest req) {
        log.warn("Regla de negocio violada en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(DatosInvalidosException ex,
                                                        HttpServletRequest req) {
        log.warn("Datos inválidos en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionFallida(MethodArgumentNotValidException ex,
                                                            HttpServletRequest req) {
        List<ErrorResponse.CampoError> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(this::aCampoError)
                .toList();
        log.warn("Validación fallida en {} {}: {} campo(s) inválido(s)",
                req.getMethod(), req.getRequestURI(), campos.size());

        ErrorResponse cuerpo = construir(HttpStatus.BAD_REQUEST,
                "Validación fallida", "Uno o más campos son inválidos", req);
        cuerpo.setErroresCampo(campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> restriccionesVioladas(ConstraintViolationException ex,
                                                               HttpServletRequest req) {
        List<ErrorResponse.CampoError> campos = ex.getConstraintViolations().stream()
                .map(cv -> new ErrorResponse.CampoError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        log.warn("Restricciones violadas en {} {}: {} violación(es)",
                req.getMethod(), req.getRequestURI(), campos.size());

        ErrorResponse cuerpo = construir(HttpStatus.BAD_REQUEST,
                "Validación fallida", "Uno o más valores no cumplen las restricciones", req);
        cuerpo.setErroresCampo(campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex,
                                                        HttpServletRequest req) {
        log.warn("Cuerpo ilegible en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no tiene un formato válido", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> argumentoInvalido(IllegalArgumentException ex,
                                                           HttpServletRequest req) {
        log.warn("Argumento inválido en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "Parámetros inválidos";
        return responder(HttpStatus.BAD_REQUEST, mensaje, req);
    }



    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException ex,
                                                        HttpServletRequest req) {
        log.warn("ResponseStatusException en {} {} -> {} {}",
                req.getMethod(), req.getRequestURI(), ex.getStatusCode().value(), ex.getReason());
        HttpStatus estado = HttpStatus.resolve(ex.getStatusCode().value());
        String mensaje = ex.getReason() != null ? ex.getReason() : "Error en la petición";
        return responder(estado != null ? estado : HttpStatus.INTERNAL_SERVER_ERROR, mensaje, req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> recursoInexistente(NoResourceFoundException ex,
                                                            HttpServletRequest req) {
        log.warn("Recurso inexistente en {} {}", req.getMethod(), req.getRequestURI());
        return responder(HttpStatus.NOT_FOUND, "El recurso solicitado no existe", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridadViolada(DataIntegrityViolationException ex,
                                                           HttpServletRequest req) {
        log.error("Violación de integridad de datos en {} {}",
                req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de datos", req);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> accesoDatos(DataAccessException ex,
                                                     HttpServletRequest req) {
        log.error("Error de acceso a datos en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo consultar la base de datos", req);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Contacte al administrador", req);
    }


    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(estado)
                .body(construir(estado, estado.getReasonPhrase(), mensaje, req));
    }

    private ErrorResponse construir(HttpStatus estado, String error, String mensaje, HttpServletRequest req) {
        return new ErrorResponse(estado.value(), error, mensaje, req.getRequestURI());
    }

    private ErrorResponse.CampoError aCampoError(FieldError fieldError) {
        return new ErrorResponse.CampoError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
