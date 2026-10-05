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

    public List<CamaraResponseDTO> listarPorEmpresa(Long empresaId) {
        return camaraRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toDTOConMacCoincide)
                .toList();
    }

    public CamaraResponseDTO save(CamaraRequestDTO camaraRequestDTO) {
        Empresa empresa = empresaRepository.findById(camaraRequestDTO.empresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Camara camara = camaraMapper.toEntity(camaraRequestDTO);
        camara.setEmpresa(empresa);
        camara.setEstado(EstadoCamara.DESCONOCIDO);

        Camara guardada = camaraRepository.save(camara);
        return camaraMapper.toDTO(guardada);
    }

    public List<HistorialEstadoResponseDTO> historialDeCamara(Long camaraId) {
        return historialEstadoRepository.findByCamaraIdOrderByFechaCambioDesc(camaraId)
                .stream()
                .map(historialEstado -> historialEstadoMapper.toDTO(historialEstado))
                .toList();
    }

    private CamaraResponseDTO toDTOConMacCoincide(Camara camara) {
        CamaraResponseDTO dto = camaraMapper.toDTO(camara);
        return dto.toBuilder()
                .macCoincide(calcularMacCoincide(camara))
                .build();
    }

    private Boolean calcularMacCoincide(Camara camara) {
        if (camara.getMacEsperada() == null || camara.getUltimaMacDetectada() == null) {
            return null;
        }
        return camara.getUltimaMacDetectada().equalsIgnoreCase(camara.getMacEsperada());
    }

    public CamaraResponseDTO actualizar(Long id, CamaraRequestDTO dto) {
        Camara camara = camaraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cámara no encontrada"));

        camara.setNombre(dto.nombre());
        camara.setIp(dto.ip());
        camara.setPuerto(dto.puerto());
        camara.setUbicacion(dto.ubicacion());
        camara.setMacEsperada(dto.macEsperada());

        Camara actualizada = camaraRepository.save(camara);
        return camaraMapper.toDTO(actualizada);
    }
}
