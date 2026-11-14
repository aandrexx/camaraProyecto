package com.losMatus.estadoCamaras.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CamaraRequestDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void permitePuertoNulo() {
        CamaraRequestDTO request = new CamaraRequestDTO(
                "Cámara de prueba",
                "192.168.1.10",
                null,
                null,
                null
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}
