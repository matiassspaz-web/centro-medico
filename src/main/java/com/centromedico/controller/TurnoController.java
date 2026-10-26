package com.centromedico.controller;

import com.centromedico.dto.CambioEstadoTurnoRequest;
import com.centromedico.dto.TurnoRequest;
import com.centromedico.dto.TurnoResponse;
import com.centromedico.service.TurnoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping
    public List<TurnoResponse> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String estado) {
        return turnoService.listar(buscar, profesionalId, fecha, estado);
    }

    /**
     * Turnos de un profesional entre dos fechas. La agenda semanal arma
     * la grilla a partir de esta lista.
     */
    @GetMapping("/agenda")
    public List<TurnoResponse> agenda(
            @RequestParam Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return turnoService.listarAgenda(profesionalId, desde, hasta);
    }

    @GetMapping("/{id}")
    public TurnoResponse obtener(@PathVariable Long id) {
        return turnoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crear(peticion));
    }

    @PutMapping("/{id}")
    public TurnoResponse actualizar(@PathVariable Long id,
                                    @Valid @RequestBody TurnoRequest peticion) {
        return turnoService.actualizar(id, peticion);
    }

    @PatchMapping("/{id}/estado")
    public TurnoResponse cambiarEstado(@PathVariable Long id,
                                       @Valid @RequestBody CambioEstadoTurnoRequest peticion) {
        return turnoService.cambiarEstado(id, peticion.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        turnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
