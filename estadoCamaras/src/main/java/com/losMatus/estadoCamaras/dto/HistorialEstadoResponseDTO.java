package com.losMatus.estadoCamaras.dto;

import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HistorialEstadoResponseDTO(
        Long id,
        Long camaraId,
        EstadoCamara estadoAnterior,
        EstadoCamara estadoNuevo,
        TipoEvento tipoEvento,
        String motivo,
        LocalDateTime fechaCambio
) {
}
