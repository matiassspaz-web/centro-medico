package com.centromedico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoRequest(

        @NotBlank(message = "El DNI del paciente es obligatorio")
        @Pattern(regexp = "^[0-9]{7,9}$", message = "El DNI debe tener entre 7 y 9 dígitos, sin puntos")
        String pacienteDni,

        @NotNull(message = "Debe seleccionar un profesional")
        Long profesionalId,

        @NotNull(message = "La fecha del turno es obligatoria")
        LocalDate fecha,

        @NotNull(message = "La hora del turno es obligatoria")
        LocalTime hora,

        @Size(max = 300, message = "El motivo no puede superar los 300 caracteres")
        String motivo

) {}
