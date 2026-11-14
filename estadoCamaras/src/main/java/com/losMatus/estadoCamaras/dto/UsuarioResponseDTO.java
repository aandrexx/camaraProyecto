package com.losMatus.estadoCamaras.dto;

import com.losMatus.estadoCamaras.enums.Rol;
import lombok.Builder;

@Builder
public record UsuarioResponseDTO(
        Long id,
        String email,
        Rol rol
) {
}
