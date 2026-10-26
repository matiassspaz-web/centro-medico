package com.centromedico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PacienteRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^[0-9]{7,9}$", message = "El DNI debe tener entre 7 y 9 dígitos, sin puntos")
        String dni,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ']+$", message = "El nombre solo puede contener letras")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80, message = "El apellido no puede superar los 80 caracteres")
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ']+$", message = "El apellido solo puede contener letras")
        String apellido,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @Pattern(regexp = "^$|^[0-9 +()-]{6,30}$", message = "El teléfono solo admite números, espacios y los signos + ( ) -")
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no puede superar los 120 caracteres")
        String email,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion,

        @Size(max = 80, message = "La obra social no puede superar los 80 caracteres")
        String obraSocial,

        @Size(max = 50, message = "El número de afiliado no puede superar los 50 caracteres")
        String nroAfiliado,

        @Pattern(regexp = "^$|^(A|B|AB|O)[+-]$", message = "El grupo sanguíneo debe ser A, B, AB u O seguido de + o -")
        String grupoSanguineo,

        @Size(max = 300, message = "Las alergias no pueden superar los 300 caracteres")
        String alergias

) {}
