package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.dto.RegisterRequestDTO;
import com.losMatus.estadoCamaras.dto.UsuarioResponseDTO;
import com.losMatus.estadoCamaras.entity.Usuario;
import com.losMatus.estadoCamaras.enums.Rol;
import com.losMatus.estadoCamaras.mapper.UsuarioMapper;
import com.losMatus.estadoCamaras.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO create(RegisterRequestDTO requestDTO){
        Usuario usuario = usuarioMapper.toEntity(requestDTO);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(Rol.USER);
        Usuario save = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(save);
    }
    public UsuarioResponseDTO findById (Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toDTO(usuario);
    }
    public UsuarioResponseDTO findByEmail (String email){
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuarioMapper.toDTO(usuario);
    }
    public void deleteById(Long id){
        if (!usuarioRepository.existsById(id)){
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
