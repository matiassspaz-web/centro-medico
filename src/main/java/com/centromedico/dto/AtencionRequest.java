package com.centromedico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AtencionRequest(

        @NotNull(message = "Debe indicar el paciente")
        Long pacienteId,

        @NotNull(message = "Debe indicar el profesional")
        Long profesionalId,

        Long turnoId,

        @NotNull(message = "La fecha de atención es obligatoria")
        @PastOrPresent(message = "La fecha de atención no puede ser futura")
        LocalDate fecha,

        @NotBlank(message = "El motivo de consulta es obligatorio")
        @Size(max = 300, message = "El motivo no puede superar los 300 caracteres")
        String motivo,

        @NotBlank(message = "Los síntomas son obligatorios")
        @Size(max = 1000, message = "Los síntomas no pueden superar los 1000 caracteres")
        String sintomas,

        @NotBlank(message = "El diagnóstico es obligatorio")
        @Size(max = 1000, message = "El diagnóstico no puede superar los 1000 caracteres")
        String diagnostico,

        @NotBlank(message = "El tratamiento es obligatorio")
        @Size(max = 1000, message = "El tratamiento no puede superar los 1000 caracteres")
        String tratamiento,

        @NotBlank(message = "Las indicaciones son obligatorias")
        @Size(max = 1000, message = "Las indicaciones no pueden superar los 1000 caracteres")
        String indicaciones,

        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
        String observaciones

) {}
