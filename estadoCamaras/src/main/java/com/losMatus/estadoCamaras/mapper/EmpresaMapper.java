package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.EmpresaRequestDTO;
import com.losMatus.estadoCamaras.dto.EmpresaResponseDTO;
import com.losMatus.estadoCamaras.entity.Empresa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    Empresa toEntity (EmpresaRequestDTO requestDTO);
    EmpresaResponseDTO toDTO (Empresa empresa);
}
