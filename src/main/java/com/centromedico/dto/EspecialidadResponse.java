package com.centromedico.dto;

public record EspecialidadResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activa,
        long cantidadProfesionales
) {}
