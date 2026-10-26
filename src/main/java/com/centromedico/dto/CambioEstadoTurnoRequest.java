package com.centromedico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioEstadoTurnoRequest(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "PENDIENTE|ATENDIDO|CANCELADO",
                message = "El estado debe ser PENDIENTE, ATENDIDO o CANCELADO")
        String estado

) {}
