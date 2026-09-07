package com.example.ProyectoDesarrollo.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView notFound(NoSuchElementException exception) {
        ModelAndView response = new ModelAndView("error/404");
        response.setStatus(HttpStatus.NOT_FOUND);
        response.addObject("errorTitle", "Registro no encontrado");
        response.addObject("errorMessage", exception.getMessage());
        return response;
    }
}
