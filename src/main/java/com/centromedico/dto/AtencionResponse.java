package com.centromedico.dto;

import java.time.LocalDate;

public record AtencionResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long profesionalId,
        String profesionalNombre,
        LocalDate fecha,
        String motivo,
        String sintomas,
        String diagnostico,
        String tratamiento,
        String indicaciones,
        String observaciones
) {}
