package com.centromedico.dto;

import java.time.LocalDate;

public record TurnoResponse(
        Long id,
        LocalDate fecha,
        String hora,
        Long pacienteId,
        String pacienteDni,
        String pacienteNombre,
        Long profesionalId,
        String profesionalNombre,
        Long especialidadId,
        String especialidadNombre,
        String estado,
        String motivo
) {}
