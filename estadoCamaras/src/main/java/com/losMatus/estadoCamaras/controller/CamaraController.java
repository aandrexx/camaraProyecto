package com.losMatus.estadoCamaras.controller;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.dto.HistorialEstadoResponseDTO;
import com.losMatus.estadoCamaras.service.CamaraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camaras")
@RequiredArgsConstructor
public class CamaraController {

    private final CamaraService camaraService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CamaraResponseDTO crear(@Valid @RequestBody CamaraRequestDTO request) {
        return camaraService.crear(request);
    }

    @GetMapping
    public List<CamaraResponseDTO> listar() {
        return camaraService.listar();
    }

    @GetMapping("/retiradas")
    public List<CamaraResponseDTO> listarRetiradas() {
        return camaraService.listarRetiradas();
    }

    @GetMapping("/{id}")
    public CamaraResponseDTO obtener(@PathVariable Long id) {
        return camaraService.obtener(id);
    }

    @PutMapping("/{id}")
    public CamaraResponseDTO actualizar(@PathVariable Long id,
                                     @Valid @RequestBody CamaraRequestDTO request) {
        return camaraService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        camaraService.eliminar(id);
    }

    @PostMapping("/{id}/restaurar")
    public CamaraResponseDTO restaurar(@PathVariable Long id) {
        return camaraService.restaurar(id);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialEstadoResponseDTO> historial(@PathVariable Long id) {
        return camaraService.historial(id);
    }
}
