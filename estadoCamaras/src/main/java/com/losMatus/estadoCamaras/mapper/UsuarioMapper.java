package com.losMatus.estadoCamaras.mapper;

import com.losMatus.estadoCamaras.dto.RegisterRequestDTO;
import com.losMatus.estadoCamaras.dto.UsuarioResponseDTO;
import com.losMatus.estadoCamaras.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(RegisterRequestDTO requestDTO){
        return Usuario.builder().
                email(requestDTO.email()).
                password(requestDTO.password())
                .build();
    }

    public UsuarioResponseDTO toDTO(Usuario usuario){
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

}
