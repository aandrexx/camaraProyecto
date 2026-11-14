package com.losMatus.estadoCamaras.controller;

import com.losMatus.estadoCamaras.dto.TecnicoRequest;
import com.losMatus.estadoCamaras.dto.TecnicoResponse;
import com.losMatus.estadoCamaras.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TecnicoResponse crear(@Valid @RequestBody TecnicoRequest request) {
        return tecnicoService.crear(request);
    }

    @GetMapping
    public List<TecnicoResponse> listar() {
        return tecnicoService.listar();
    }

    @GetMapping("/{id}")
    public TecnicoResponse obtener(@PathVariable Long id) {
        return tecnicoService.obtener(id);
    }

    @PutMapping("/{id}")
    public TecnicoResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody TecnicoRequest request) {
        return tecnicoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        tecnicoService.desactivar(id);
    }

    @PostMapping("/{id}/reactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivar(@PathVariable Long id) {
        tecnicoService.reactivar(id);
    }
}
