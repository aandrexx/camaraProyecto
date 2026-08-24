package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.entity.Camara;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CamaraMapper {

    Camara toEntity(CamaraRequestDTO camaraRequestDTO);

    @Mapping(source = "empresa.id", target = "empresaId")
    CamaraResponseDTO toDTO (Camara camara);
}
