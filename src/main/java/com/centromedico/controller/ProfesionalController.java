package com.centromedico.controller;

import com.centromedico.dto.ProfesionalRequest;
import com.centromedico.dto.ProfesionalResponse;
import com.centromedico.service.ProfesionalService;
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
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @GetMapping
    public List<ProfesionalResponse> listar(@RequestParam(required = false) String buscar,
                                            @RequestParam(required = false) Long especialidadId,
                                            @RequestParam(defaultValue = "false") boolean soloActivos) {
        return soloActivos
                ? profesionalService.listarActivos()
                : profesionalService.listar(buscar, especialidadId);
    }

    @GetMapping("/{id}")
    public ProfesionalResponse obtener(@PathVariable Long id) {
        return profesionalService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ProfesionalResponse> crear(@Valid @RequestBody ProfesionalRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profesionalService.crear(peticion));
    }

    @PutMapping("/{id}")
    public ProfesionalResponse actualizar(@PathVariable Long id,
                                          @Valid @RequestBody ProfesionalRequest peticion) {
        return profesionalService.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesionalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
