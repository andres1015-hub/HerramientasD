package com.example.ProyectoDesarrollo.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateInputException;


@ControllerAdvice(basePackages = "com.example.ProyectoDesarrollo.Controllers")
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalMvcExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalMvcExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String recursoNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req,
                                      HttpServletResponse res, Model model) {
        log.warn("Recurso no encontrado en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req, res, model);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public String reglaNegocio(ReglaNegocioException ex, HttpServletRequest req,
                               HttpServletResponse res, Model model) {
        log.warn("Regla de negocio violada en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.CONFLICT, "Operación no permitida", ex.getMessage(), req, res, model);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public String datosInvalidos(DatosInvalidosException ex, HttpServletRequest req,
                                 HttpServletResponse res, Model model) {
        log.warn("Datos inválidos en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.BAD_REQUEST, "Datos inválidos", ex.getMessage(), req, res, model);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String responseStatus(ResponseStatusException ex, HttpServletRequest req,
                                 HttpServletResponse res, Model model) {
        log.warn("ResponseStatusException en {} {} -> {} {}",
                req.getMethod(), req.getRequestURI(), ex.getStatusCode().value(), ex.getReason());
        HttpStatus estado = HttpStatus.resolve(ex.getStatusCode().value());
        if (estado == null) {
            estado = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return vistaError(estado, "Error en la petición", ex.getReason(), req, res, model);
    }

    @ExceptionHandler(TemplateInputException.class)
    public String vistaInexistente(TemplateInputException ex, HttpServletRequest req,
                                   HttpServletResponse res, Model model) {
        log.warn("Plantilla no disponible en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.NOT_FOUND, "Página no encontrada",
                "La página solicitada no está disponible.", req, res, model);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String recursoInexistente(NoResourceFoundException ex, HttpServletRequest req,
                                     HttpServletResponse res, Model model) {
        log.warn("Recurso inexistente en {} {}", req.getMethod(), req.getRequestURI());
        return vistaError(HttpStatus.NOT_FOUND, "Página no encontrada",
                "La página solicitada no existe.", req, res, model);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String accesoDenegado(AccessDeniedException ex, HttpServletRequest req,
                                 HttpServletResponse res, Model model) {
        log.warn("Acceso denegado en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para acceder a esta sección.", req, res, model);
    }

    @ExceptionHandler(AuthenticationException.class)
    public String noAutenticado(AuthenticationException ex, HttpServletRequest req,
                                HttpServletResponse res, Model model) {
        log.warn("Autenticación requerida en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.UNAUTHORIZED, "Sesión requerida",
                "Tu sesión no está iniciada o ha expirado. Inicia sesión para continuar.", req, res, model);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String argumentoInvalido(IllegalArgumentException ex, HttpServletRequest req,
                                    HttpServletResponse res, Model model) {
        log.warn("Argumento invalido en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.BAD_REQUEST, "Datos invalidos",
                ex.getMessage() != null ? ex.getMessage() : "Los datos enviados no son validos.", req, res, model);
    }

    @ExceptionHandler(BindException.class)
    public String formularioInvalido(BindException ex, HttpServletRequest req,
                                     HttpServletResponse res, Model model) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Revisa los datos ingresados.");
        log.warn("Formulario inválido en {} {}: {}", req.getMethod(), req.getRequestURI(), mensaje);
        return vistaError(HttpStatus.BAD_REQUEST, "Datos inválidos", mensaje, req, res, model);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public String peticionIncompleta(ServletRequestBindingException ex, HttpServletRequest req,
                                     HttpServletResponse res, Model model) {
        log.warn("Petición incompleta en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.BAD_REQUEST, "Petición inválida",
                "La petición no contiene todos los datos necesarios.", req, res, model);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String parametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req,
                                    HttpServletResponse res, Model model) {
        log.warn("Parámetro inválido en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "Uno de los parámetros de la dirección no tiene un formato válido.", req, res, model);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public String tipoContenidoNoSoportado(org.springframework.web.HttpMediaTypeNotSupportedException ex,
                                           HttpServletRequest req, HttpServletResponse res, Model model) {
        log.warn("Tipo de contenido no soportado en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getContentType());
        return vistaError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato no soportado",
                "El formato de la peticion no esta soportado.", req, res, model);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public String tipoRespuestaNoAceptable(HttpMediaTypeNotAcceptableException ex,
                                           HttpServletRequest req, HttpServletResponse res, Model model) {
        log.warn("Formato de respuesta no aceptable en {} {}", req.getMethod(), req.getRequestURI());
        return vistaError(HttpStatus.NOT_ACCEPTABLE, "Formato de respuesta no soportado",
                "No hay una representación disponible para el formato solicitado.", req, res, model);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public String cuerpoNoLegible(HttpMessageNotReadableException ex,
                                  HttpServletRequest req, HttpServletResponse res, Model model) {
        log.warn("Cuerpo de petición no legible en {} {}", req.getMethod(), req.getRequestURI());
        return vistaError(HttpStatus.BAD_REQUEST, "Petición inválida",
                "El cuerpo de la petición está vacío o tiene un formato inválido.", req, res, model);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String metodoNoSoportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req,
                                    HttpServletResponse res, Model model) {
        log.warn("Método no soportado en {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return vistaError(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido",
                "El método HTTP utilizado no está permitido para esta ruta.", req, res, model);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integridadViolada(DataIntegrityViolationException ex, HttpServletRequest req,
                                    HttpServletResponse res, Model model) {
        log.error("Violación de integridad de datos en {}", req.getRequestURI(), ex);
        return vistaError(HttpStatus.CONFLICT, "Operación no permitida",
                "La operación entra en conflicto con datos existentes.", req, res, model);
    }

    @ExceptionHandler(DataAccessException.class)
    public String accesoDatos(DataAccessException ex, HttpServletRequest req,
                              HttpServletResponse res, Model model) {
        log.error("Error de acceso a datos en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return vistaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar los datos",
                "No se pudo completar la operación. Inténtalo más tarde.", req, res, model);
    }

    @ExceptionHandler(Exception.class)
    public String errorInesperado(Exception ex, HttpServletRequest req,
                                  HttpServletResponse res, Model model) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return vistaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Vuelve a intentarlo o contacta al administrador.",
                req, res, model);
    }


    private String vistaError(HttpStatus estado, String titulo, String mensaje,
                              HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setStatus(estado.value());
        model.addAttribute("codigoError", estado.value());
        model.addAttribute("tituloError", titulo);
        model.addAttribute("mensajeError",
                mensaje != null ? mensaje : "Ocurrió un error inesperado. Vuelve a intentarlo.");
        model.addAttribute("rutaError", req.getRequestURI());
        return "error";
    }
}
