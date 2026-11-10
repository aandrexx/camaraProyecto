package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.dto.HistorialEstadoResponseDTO;
import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import lombok.Builder;
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
        // estado (DESCONOCIDO) y fallosConsecutivos (0) -> los pone @Builder.Default
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
                .macCoincide(camara.getMacEsperada() == null || camara.getUltimaMacDetectada() == null
                        ? null
                        : camara.getMacEsperada().equals(camara.getUltimaMacDetectada()))
                .fallosConsecutivos(camara.getFallosConsecutivos())
                .estado(camara.getEstado())
                .ultimaVerificacion(camara.getUltimaVerificacion())
                .build();
    }

    public void actualizar(CamaraRequestDTO camaraRequestDTO, Camara camara) {
        camara.setNombre(camaraRequestDTO.nombre());
        camara.setIp(camaraRequestDTO.ip());
        camara.setPuerto(camaraRequestDTO.puerto());
        camara.setUbicacion(camaraRequestDTO.ubicacion());
        camara.setMacEsperada(camaraRequestDTO.macEsperada());
    }
    public HistorialEstadoResponseDTO toHistorialDTO(HistorialEstado historial) {
        return HistorialEstadoResponseDTO.builder()
                .id(historial.getId())
                .camaraId(historial.getCamara().getId())
                .estadoAnterior(historial.getEstadoAnterior())
                .estadoNuevo(historial.getEstadoNuevo())
                .tipoEvento(historial.getTipoEvento())
                .motivo(historial.getMotivo())
                .fechaCambio(historial.getFechaCambio())
                .build();
    }
}
