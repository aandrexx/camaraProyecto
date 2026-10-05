package com.losMatus.estadoCamaras.dto;

public record CamaraRequestDTO(
        String nombre,
        String ip,
        Integer puerto,
        String ubicacion,
        String macEsperada,
        Long empresaId
) {
}
