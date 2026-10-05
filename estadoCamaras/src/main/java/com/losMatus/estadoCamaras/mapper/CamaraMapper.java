package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.entity.Camara;
import lombok.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

//CAMBIAN DE MAPSTRUCT A PATRON BUILDER (POR COMODIDAD)

@Component
@Builder
public class CamaraMapper {

    public Camara toEntity(CamaraRequestDTO camaraRequestDTO) {
        return Camara.builder()
                .nombre(camaraRequestDTO.nombre())
                .ip(camaraRequestDTO.ip())
                .puerto(camaraRequestDTO.puerto())
                .ubicacion(camaraRequestDTO.ubicacion())
                .macEsperada(camaraRequestDTO.macEsperada())
                .build();
        // sin .id() -> lo genera la BD
        // sin .empresa() -> se setea aparte en el service (necesita empresaRepository.findById primero)
        // sin .estado() -> se setea aparte en el service (DESCONOCIDO al crear)
    }

    public CamaraResponseDTO toDTO(Camara camara) {
        return CamaraResponseDTO.builder()
                .id(camara.getId())
                .nombre(camara.getNombre())
                .ip(camara.getIp())
                .puerto(camara.getPuerto())
                .ubicacion(camara.getUbicacion())
                .macEsperada(camara.getMacEsperada())
                .ultimaMacDetectada(camara.getUltimaMacDetectada())
                .estado(camara.getEstado())
                .ultimaVerificacion(camara.getUltimaVerificacion())
                .empresaId(camara.getEmpresa().getId())
                .build();
        // macCoincide se agrega después en el service con .toBuilder()
    }
}
