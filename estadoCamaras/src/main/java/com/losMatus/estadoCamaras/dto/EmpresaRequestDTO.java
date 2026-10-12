package com.losMatus.estadoCamaras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @Size(max = 200) String direccion,
        @Size(max = 100) String contactoTecnico
) {
}
