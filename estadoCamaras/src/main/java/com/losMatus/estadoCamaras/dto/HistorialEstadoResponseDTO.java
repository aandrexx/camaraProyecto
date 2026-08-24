package com.losMatus.estadoCamaras.dto;

import com.losMatus.estadoCamaras.enums.EstadoCamara;

import java.time.LocalDateTime;

public record HistorialEstadoResponseDTO(
        Long id,
        EstadoCamara estadoAnterior,
        EstadoCamara estadoNuevo,
        LocalDateTime fechaCambio
) {
}
