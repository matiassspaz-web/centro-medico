package com.centromedico.dto;

public record DashboardResponse(
        long totalPacientes,
        long totalProfesionales,
        long totalEspecialidades,
        long turnosHoy,
        long turnosPendientes,
        long totalAtenciones
) {}
