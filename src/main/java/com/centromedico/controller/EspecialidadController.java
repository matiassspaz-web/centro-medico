package com.centromedico.controller;

import com.centromedico.dto.EspecialidadRequest;
import com.centromedico.dto.EspecialidadResponse;
import com.centromedico.service.EspecialidadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    public List<EspecialidadResponse> listar(@RequestParam(required = false) String buscar,
                                             @RequestParam(defaultValue = "false") boolean soloActivas) {
        return soloActivas ? especialidadService.listarActivas() : especialidadService.listar(buscar);
    }

    @GetMapping("/{id}")
    public EspecialidadResponse obtener(@PathVariable Long id) {
        return especialidadService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadService.crear(peticion));
    }

    @PutMapping("/{id}")
    public EspecialidadResponse actualizar(@PathVariable Long id,
                                           @Valid @RequestBody EspecialidadRequest peticion) {
        return especialidadService.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
