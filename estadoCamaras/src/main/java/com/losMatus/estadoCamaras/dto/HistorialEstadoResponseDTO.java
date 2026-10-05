package com.losMatus.estadoCamaras.dto;

import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;

import java.time.LocalDateTime;

public record HistorialEstadoResponseDTO(
        Long id,
        EstadoCamara estadoAnterior,
        EstadoCamara estadoNuevo,
        TipoEvento tipoEvento,
        LocalDateTime fechaCambio
) {
}
