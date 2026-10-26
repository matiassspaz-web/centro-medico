package com.centromedico.dto;

public record ProfesionalResponse(
        Long id,
        String matricula,
        String nombre,
        String apellido,
        String nombreCompleto,
        Long especialidadId,
        String especialidadNombre,
        String telefono,
        String email,
        boolean activo
) {}
