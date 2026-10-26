package com.centromedico.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        Map<String, String> errores
) {

    public static ErrorResponse de(int status, String error, String mensaje) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, null);
    }

    public static ErrorResponse deValidacion(int status, String error, String mensaje, Map<String, String> errores) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, errores);
    }
}
