package com.centromedico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadRequest(

        @NotBlank(message = "El nombre de la especialidad es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String nombre,

        @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
        String descripcion,

        Boolean activa

) {}
