package com.centromedico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfesionalRequest(

        @NotBlank(message = "La matrícula es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "La matrícula solo admite letras, números y guiones (3 a 20 caracteres)")
        String matricula,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ']+$", message = "El nombre solo puede contener letras")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80, message = "El apellido no puede superar los 80 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ']+$", message = "El apellido solo puede contener letras")
        String apellido,

        @NotNull(message = "Debe seleccionar una especialidad")
        Long especialidadId,

        @Pattern(regexp = "^$|^[0-9 +()-]{6,30}$", message = "El teléfono solo admite números, espacios y los signos + ( ) -")
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no puede superar los 120 caracteres")
        String email,

        Boolean activo

) {}
