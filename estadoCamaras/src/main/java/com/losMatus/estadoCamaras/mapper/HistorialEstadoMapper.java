package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.HistorialEstadoResponseDTO;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistorialEstadoMapper {

    HistorialEstadoResponseDTO toDTO (HistorialEstado historialEstado);
}
