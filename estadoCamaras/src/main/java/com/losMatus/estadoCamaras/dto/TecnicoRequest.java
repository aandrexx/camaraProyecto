package com.losMatus.estadoCamaras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{8,15}",
                message = "El teléfono debe tener entre 8 y 15 dígitos, con código de país y sin '+' ni espacios")
        String telefono
) {
}
