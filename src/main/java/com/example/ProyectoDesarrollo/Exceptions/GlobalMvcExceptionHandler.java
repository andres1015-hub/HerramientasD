package com.example.ProyectoDesarrollo.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
