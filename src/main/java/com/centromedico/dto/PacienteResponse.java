package com.centromedico.dto;

import java.time.LocalDate;

public record PacienteResponse(
        Long id,
        String dni,
        String nombre,
        String apellido,
        String nombreCompleto,
        LocalDate fechaNacimiento,
        int edad,
        String telefono,
        String email,
        String direccion,
        String obraSocial,
        String nroAfiliado,
        String grupoSanguineo,
        String alergias
) {}
