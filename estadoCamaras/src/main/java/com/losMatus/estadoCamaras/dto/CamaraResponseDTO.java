package com.losMatus.estadoCamaras.dto;

import com.losMatus.estadoCamaras.enums.EstadoCamara;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record CamaraResponseDTO(
        Long id,
        String nombre,
        String ip,
        Integer puerto,
        String ubicacion,
        String macEsperada,
        String ultimaMacDetectada,
        Boolean macCoincide,
        EstadoCamara estado,
        LocalDateTime ultimaVerificacion,
        Long empresaId
) {
}
