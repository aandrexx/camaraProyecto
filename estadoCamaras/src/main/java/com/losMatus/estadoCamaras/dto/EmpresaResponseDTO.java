package com.losMatus.estadoCamaras.dto;

public record EmpresaResponseDTO(
        Long id,
        String nombre,
        String direccion,
        String contactoTecnico
) {
}
