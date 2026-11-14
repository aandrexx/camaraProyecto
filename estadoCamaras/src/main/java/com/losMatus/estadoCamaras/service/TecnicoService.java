package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.dto.TecnicoRequest;
import com.losMatus.estadoCamaras.dto.TecnicoResponse;
import com.losMatus.estadoCamaras.entity.Tecnico;
import com.losMatus.estadoCamaras.mapper.TecnicoMapper;
import com.losMatus.estadoCamaras.repository.TecnicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final TecnicoMapper tecnicoMapper;

    public TecnicoService(TecnicoRepository tecnicoRepository, TecnicoMapper tecnicoMapper) {
        this.tecnicoRepository = tecnicoRepository;
        this.tecnicoMapper = tecnicoMapper;
    }

    @Transactional
    public TecnicoResponse crear(TecnicoRequest request) {
        validarTelefonoLibre(request.telefono(), null);
        Tecnico guardado = tecnicoRepository.save(tecnicoMapper.toEntity(request));
        return tecnicoMapper.toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return tecnicoRepository.findAll().stream()
                .map(tecnicoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse obtener(Long id) {
        return tecnicoMapper.toDTO(buscar(id));
    }

    @Transactional
    public TecnicoResponse actualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscar(id);
        validarTelefonoLibre(request.telefono(), id);
        tecnicoMapper.actualizar(tecnico, request);
        return tecnicoMapper.toDTO(tecnico);
    }

    @Transactional
    public void desactivar(Long id) {
        buscar(id).setActivo(false);
    }

    @Transactional
    public void reactivar(Long id) {
        buscar(id).setActivo(true);
    }

    private Tecnico buscar(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Técnico no encontrado con id " + id));
    }

    // id = null al crear; al editar se excluye al propio técnico
    private void validarTelefonoLibre(String telefono, Long id) {
        boolean repetido = (id == null)
                ? tecnicoRepository.existsByTelefono(telefono)
                : tecnicoRepository.existsByTelefonoAndIdNot(telefono, id);
        if (repetido) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un técnico con el teléfono " + telefono);
        }
    }
}
