package com.losMatus.estadoCamaras.controller;

import com.losMatus.estadoCamaras.dto.EmpresaRequestDTO;
import com.losMatus.estadoCamaras.dto.EmpresaResponseDTO;
import com.losMatus.estadoCamaras.entity.Empresa;
import com.losMatus.estadoCamaras.mapper.EmpresaMapper;
import com.losMatus.estadoCamaras.repository.EmpresaRepository;
import com.losMatus.estadoCamaras.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public List<EmpresaResponseDTO> findAll(){
        return empresaService.findAll();
    }
    @PostMapping
    public EmpresaResponseDTO save(@RequestBody EmpresaRequestDTO empresaRequestDTO){
        return empresaService.save(empresaRequestDTO);

    }

}
