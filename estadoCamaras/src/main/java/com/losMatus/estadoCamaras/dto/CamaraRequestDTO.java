package com.losMatus.estadoCamaras.dto;

import jakarta.validation.constraints.*;

public record CamaraRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @NotBlank(message = "La IP es obligatoria")
        @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$",
                message = "La IP debe ser una IPv4 válida")
        String ip,

        @NotNull(message = "El puerto es obligatorio")
        @Min(value = 1, message = "El puerto mínimo es 1")
        @Max(value = 65535, message = "El puerto máximo es 65535")
        Integer puerto,

        @Size(max = 150, message = "La ubicación no puede pasar de 150 caracteres")
        String ubicacion,

        @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$",
                message = "Formato de MAC inválido (ej: AA:BB:CC:11:22:33)")
        String macEsperada,

        @NotNull(message = "La empresa es obligatoria")
        Long empresaId
) {
}
