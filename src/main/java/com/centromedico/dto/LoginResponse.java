package com.centromedico.dto;

public record LoginResponse(
        String token,
        String username,
        String nombreCompleto,
        String rol
) {}
