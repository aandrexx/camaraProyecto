package com.losMatus.estadoCamaras.dto;

import com.losMatus.estadoCamaras.enums.EstadoCamara;

import java.time.LocalDateTime;

public record CamaraResponseDTO(
        Long id,
        String nombre,
        String ip,
        Integer puerto,
        String ubicacion,
        EstadoCamara estado,
        LocalDateTime ultimaVerificacion,
        Long empresaId
) {
}
