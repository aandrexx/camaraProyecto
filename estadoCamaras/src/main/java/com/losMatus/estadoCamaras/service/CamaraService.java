package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.dto.HistorialEstadoResponseDTO;
import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.Empresa;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.mapper.CamaraMapper;
import com.losMatus.estadoCamaras.mapper.HistorialEstadoMapper;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.EmpresaRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CamaraService {

    private final CamaraRepository camaraRepository;
    private final CamaraMapper camaraMapper;
    private final EmpresaRepository empresaRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final HistorialEstadoMapper historialEstadoMapper;

    public List<CamaraResponseDTO> listarPorEmpresa(Long empresaId){
        return camaraRepository.findByEmpresaId(empresaId)
                .stream()
                .map(camara -> camaraMapper.toDTO(camara))
                .toList();
    }
    public CamaraResponseDTO save (CamaraRequestDTO camaraRequestDTO){
        Empresa empresa = empresaRepository.findById(camaraRequestDTO.empresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Camara camara = camaraMapper.toEntity(camaraRequestDTO);
        camara.setEmpresa(empresa);
        camara.setEstado(EstadoCamara.DESCONOCIDO);

        Camara guardada = camaraRepository.save(camara);
        return camaraMapper.toDTO(guardada);
    }
    public List<HistorialEstadoResponseDTO> historialDeCamara(Long camaraId){
        return historialEstadoRepository.findByCamaraIdOrderByFechaCambioDesc(camaraId)
                .stream()
                .map(historialEstado -> historialEstadoMapper.toDTO(historialEstado))
                .toList();
    }
}
