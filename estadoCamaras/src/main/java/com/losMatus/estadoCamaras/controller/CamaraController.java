package com.losMatus.estadoCamaras.controller;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.dto.HistorialEstadoResponseDTO;
import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import com.losMatus.estadoCamaras.mapper.CamaraMapper;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import com.losMatus.estadoCamaras.service.CamaraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camaras")
@RequiredArgsConstructor
public class CamaraController {

    private final CamaraService camaraService;

    //Camaras de una empresa especifica
    @GetMapping("/empresa/{empresaId}")
    public List<CamaraResponseDTO> listarPorEmpresa(@PathVariable Long empresaId){
        return camaraService.listarPorEmpresa(empresaId);
    }
    @PostMapping
    public CamaraResponseDTO save (@RequestBody CamaraRequestDTO camaraRequestDTO){
        return camaraService.save(camaraRequestDTO);
    }
    //Historial de estados de una camara especifica
    @GetMapping("/{camaraId}/historial")
    public List<HistorialEstadoResponseDTO> historialDeCamara(@PathVariable Long camaraId){
        return camaraService.historialDeCamara(camaraId);
    }
    @PutMapping("/{id}")
    public CamaraResponseDTO actualizar(@PathVariable Long id, @RequestBody CamaraRequestDTO dto) {
        return camaraService.actualizar(id, dto);
    }
}
