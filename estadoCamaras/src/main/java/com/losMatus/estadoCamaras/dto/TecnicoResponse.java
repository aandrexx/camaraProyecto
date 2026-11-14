package com.losMatus.estadoCamaras.dto;

import lombok.Builder;

@Builder
public record TecnicoResponse(
        Long id,
        String nombre,
        String telefono,
        boolean activo
) {
}
