package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.dto.EmpresaRequestDTO;
import com.losMatus.estadoCamaras.dto.EmpresaResponseDTO;
import com.losMatus.estadoCamaras.entity.Empresa;
import com.losMatus.estadoCamaras.mapper.EmpresaMapper;
import com.losMatus.estadoCamaras.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(EmpresaRepository empresaRepository, EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.empresaMapper = empresaMapper;
    }

    public List<EmpresaResponseDTO> findAll(){
        return empresaRepository.findAll()
                .stream()
                .map(empresa -> empresaMapper.toDTO(empresa))
                .toList();
    }
    public EmpresaResponseDTO save (EmpresaRequestDTO empresaRequestDTO){
        Empresa empresa = empresaMapper.toEntity(empresaRequestDTO);
        Empresa guardado = empresaRepository.save(empresa);
        return empresaMapper.toDTO(guardado);
    }
}
