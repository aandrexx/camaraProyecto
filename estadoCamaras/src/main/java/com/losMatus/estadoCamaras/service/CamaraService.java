package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.dto.CamaraRequestDTO;
import com.losMatus.estadoCamaras.dto.CamaraResponseDTO;
import com.losMatus.estadoCamaras.dto.HistorialEstadoResponseDTO;
import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.mapper.CamaraMapper;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CamaraService {

    private final CamaraRepository camaraRepository;
    private final CamaraMapper camaraMapper;
    private final HistorialEstadoRepository historialEstadoRepository;

    public CamaraService(CamaraRepository camaraRepository, CamaraMapper camaraMapper, HistorialEstadoRepository historialEstadoRepository) {
        this.camaraRepository = camaraRepository;
        this.camaraMapper = camaraMapper;
        this.historialEstadoRepository = historialEstadoRepository;
    }

    public CamaraResponseDTO crear(CamaraRequestDTO request) {
        Camara camara = camaraMapper.toEntity(request);
        camara.setMacEsperada(normalizarMac(camara.getMacEsperada()));
        return camaraMapper.toDTO(camaraRepository.save(camara));
    }

    public List<CamaraResponseDTO> listar() {
        return camaraRepository.findAllByEliminadaFalse().stream()
                .map(camaraMapper::toDTO)
                .toList();
    }

    public List<CamaraResponseDTO> listarRetiradas() {
        return camaraRepository.findAllByEliminadaTrue().stream()
                .map(camaraMapper::toDTO)
                .toList();
    }

    public CamaraResponseDTO obtener(Long id) {
        return camaraMapper.toDTO(buscarActiva(id));
    }

    public CamaraResponseDTO actualizar(Long id, CamaraRequestDTO request) {
        var camara = buscarActiva(id);
        camaraMapper.actualizar(request, camara);
        camara.setMacEsperada(normalizarMac(camara.getMacEsperada()));
        return camaraMapper.toDTO(camaraRepository.save(camara));
    }

    public void eliminar(Long id) {
        buscarActiva(id);
        if (camaraRepository.marcarComoEliminada(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cámara no encontrada con id " + id);
        }
    }

    public CamaraResponseDTO restaurar(Long id) {
        buscar(id);
        if (camaraRepository.restaurar(id) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cámara ya está activa");
        }
        return camaraMapper.toDTO(buscarActiva(id));
    }

    public List<HistorialEstadoResponseDTO> historial(Long id) {
        buscar(id);
        return historialEstadoRepository.findByCamaraIdOrderByFechaCambioDesc(id).stream()
                .map(camaraMapper::toHistorialDTO)
                .toList();
    }

    private Camara buscar(Long id) {
        return camaraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cámara no encontrada con id " + id));
    }

    private Camara buscarActiva(Long id) {
        return camaraRepository.findByIdAndEliminadaFalse(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cámara no encontrada con id " + id));
    }

    // Windows devuelve aa-bb-cc, Linux aa:bb:cc: se guarda siempre AA:BB:CC
    private String normalizarMac(String mac) {
        return mac == null ? null : mac.replace("-", ":").toUpperCase();
    }
}
