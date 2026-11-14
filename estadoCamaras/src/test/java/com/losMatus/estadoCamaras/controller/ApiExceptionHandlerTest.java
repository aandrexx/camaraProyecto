package com.losMatus.estadoCamaras.controller;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void devuelveErroresDeValidacionPorCampo() throws NoSuchMethodException {
        CamaraRequestDTO request = new CamaraRequestDTO(null, null, null, null, null);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.rejectValue("nombre", "NotBlank", "El nombre es obligatorio");
        Method controllerMethod = CamaraController.class.getMethod("crear", CamaraRequestDTO.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new MethodParameter(controllerMethod, 0),
                bindingResult
        );

        var response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("nombre", response.getBody().errors().getFirst().field());
        assertEquals("El nombre es obligatorio", response.getBody().errors().getFirst().message());
    }

    @Test
    void conservaEstadoYMensajeDeResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cámara no encontrada"
        );

        var response = handler.handleResponseStatus(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Cámara no encontrada", response.getBody().message());
        assertEquals(0, response.getBody().errors().size());
    }
}
