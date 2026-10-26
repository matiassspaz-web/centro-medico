package com.centromedico.controller;

import com.centromedico.dto.PacienteRequest;
import com.centromedico.dto.PacienteResponse;
import com.centromedico.service.PacienteService;
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
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<PacienteResponse> listar(@RequestParam(required = false) String buscar) {
        return pacienteService.listar(buscar);
    }

    @GetMapping("/{id}")
    public PacienteResponse obtener(@PathVariable Long id) {
        return pacienteService.obtenerPorId(id);
    }

    @GetMapping("/dni/{dni}")
    public PacienteResponse obtenerPorDni(@PathVariable String dni) {
        return pacienteService.obtenerPorDni(dni);
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(peticion));
    }

    @PutMapping("/{id}")
    public PacienteResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody PacienteRequest peticion) {
        return pacienteService.actualizar(id, peticion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
