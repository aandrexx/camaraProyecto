package com.losMatus.estadoCamaras.controller;

import com.losMatus.estadoCamaras.dto.AuthResponseDTO;
import com.losMatus.estadoCamaras.dto.LoginRequestDTO;
import com.losMatus.estadoCamaras.dto.RegisterRequestDTO;
import com.losMatus.estadoCamaras.dto.UsuarioResponseDTO;
import com.losMatus.estadoCamaras.security.CustomUserDetailsService;
import com.losMatus.estadoCamaras.security.JwtService;
import com.losMatus.estadoCamaras.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/register")
    public UsuarioResponseDTO register (@RequestBody RegisterRequestDTO dto){
        return usuarioService.create(dto);
    }
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO dto){

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(dto.email());
        String token = jwtService.genereteToken(userDetails);
        String rol = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no tiene un rol asignado."));

        return new AuthResponseDTO(token, rol);
    }
}
