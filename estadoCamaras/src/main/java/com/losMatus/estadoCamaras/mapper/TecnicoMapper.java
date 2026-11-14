package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.TecnicoRequest;
import com.losMatus.estadoCamaras.dto.TecnicoResponse;
import com.losMatus.estadoCamaras.entity.Tecnico;
import org.springframework.stereotype.Component;

@Component
public class TecnicoMapper {

    public Tecnico toEntity(TecnicoRequest request) {
        return Tecnico.builder()
                .nombre(request.nombre())
                .telefono(request.telefono())
                .build();
    }

    public TecnicoResponse toDTO(Tecnico tecnico) {
        return TecnicoResponse.builder()
                .id(tecnico.getId())
                .nombre(tecnico.getNombre())
                .telefono(tecnico.getTelefono())
                .activo(tecnico.isActivo())
                .build();
    }

    public void actualizar(Tecnico tecnico, TecnicoRequest request) {
        tecnico.setNombre(request.nombre());
        tecnico.setTelefono(request.telefono());
    }
}
