package com.centromedico.service;

import com.centromedico.dto.DashboardResponse;
import com.centromedico.model.EstadoTurno;
import com.centromedico.repository.AtencionRepository;
import com.centromedico.repository.EspecialidadRepository;
import com.centromedico.repository.PacienteRepository;
import com.centromedico.repository.ProfesionalRepository;
import com.centromedico.repository.TurnoRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadRepository especialidadRepository;
    private final TurnoRepository turnoRepository;
    private final AtencionRepository atencionRepository;

    public DashboardService(PacienteRepository pacienteRepository,
                            ProfesionalRepository profesionalRepository,
                            EspecialidadRepository especialidadRepository,
                            TurnoRepository turnoRepository,
                            AtencionRepository atencionRepository) {
        this.pacienteRepository = pacienteRepository;
        this.profesionalRepository = profesionalRepository;
        this.especialidadRepository = especialidadRepository;
        this.turnoRepository = turnoRepository;
        this.atencionRepository = atencionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen() {
        return new DashboardResponse(
                pacienteRepository.count(),
                profesionalRepository.count(),
                especialidadRepository.count(),
                turnoRepository.countByFechaAndEstadoNot(LocalDate.now(), EstadoTurno.CANCELADO),
                turnoRepository.countByEstado(EstadoTurno.PENDIENTE),
                atencionRepository.count());
    }
}
