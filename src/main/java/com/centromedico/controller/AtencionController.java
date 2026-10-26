package com.centromedico.controller;

import com.centromedico.dto.AtencionRequest;
import com.centromedico.dto.AtencionResponse;
import com.centromedico.service.AtencionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    private final AtencionService atencionService;

    public AtencionController(AtencionService atencionService) {
        this.atencionService = atencionService;
    }

    /**
     * Historia clínica del paciente indicado.
     */
    @GetMapping
    public List<AtencionResponse> listarPorPaciente(@RequestParam Long pacienteId) {
        return atencionService.listarPorPaciente(pacienteId);
    }

    @GetMapping("/{id}")
    public AtencionResponse obtener(@PathVariable Long id) {
        return atencionService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<AtencionResponse> crear(@Valid @RequestBody AtencionRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atencionService.crear(peticion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        atencionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
