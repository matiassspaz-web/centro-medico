package com.centromedico.service;

import com.centromedico.dto.TurnoRequest;
import com.centromedico.dto.TurnoResponse;
import com.centromedico.exception.RecursoNoEncontradoException;
import com.centromedico.exception.ReglaNegocioException;
import com.centromedico.model.EstadoTurno;
import com.centromedico.model.Paciente;
import com.centromedico.model.Profesional;
import com.centromedico.model.Turno;
import com.centromedico.repository.PacienteRepository;
import com.centromedico.repository.TurnoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TurnoService {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalService profesionalService;

    public TurnoService(TurnoRepository turnoRepository,
                        PacienteRepository pacienteRepository,
                        ProfesionalService profesionalService) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profesionalService = profesionalService;
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listar(String texto, Long profesionalId, LocalDate fecha, String estado) {
        EstadoTurno estadoFiltro = convertirEstado(estado);
        return turnoRepository.buscar(patron(texto), profesionalId, fecha, estadoFiltro).stream()
                .map(TurnoService::aRespuesta)
                .toList();
    }

    /**
     * Turnos de un profesional dentro de un rango de fechas. Lo usa la agenda
     * semanal para dibujar la grilla de horarios.
     */
    @Transactional(readOnly = true)
    public List<TurnoResponse> listarAgenda(Long profesionalId, LocalDate desde, LocalDate hasta) {
        profesionalService.buscarEntidad(profesionalId);

        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial");
        }

        return turnoRepository
                .findByProfesionalIdAndFechaBetweenAndEstadoNotOrderByFechaAscHoraAsc(
                        profesionalId, desde, hasta, EstadoTurno.CANCELADO)
                .stream()
                .map(TurnoService::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public TurnoResponse obtenerPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional
    public TurnoResponse crear(TurnoRequest peticion) {
        Paciente paciente = buscarPacientePorDni(peticion.pacienteDni());
        Profesional profesional = profesionalService.buscarEntidad(peticion.profesionalId());

        validarProfesionalActivo(profesional);
        validarNoEsPasado(peticion.fecha(), peticion.hora());

        boolean ocupado = turnoRepository.existsByProfesionalIdAndFechaAndHoraAndEstadoNot(
                profesional.getId(), peticion.fecha(), peticion.hora(), EstadoTurno.CANCELADO);

        if (ocupado) {
            throw new ReglaNegocioException("El profesional ya tiene un turno asignado el "
                    + peticion.fecha() + " a las " + peticion.hora().format(FORMATO_HORA));
        }

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setFecha(peticion.fecha());
        turno.setHora(peticion.hora());
        turno.setMotivo(peticion.motivo());
        turno.setEstado(EstadoTurno.PENDIENTE);

        return aRespuesta(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse actualizar(Long id, TurnoRequest peticion) {
        Turno turno = buscarEntidad(id);

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new ReglaNegocioException("No se puede modificar un turno cancelado");
        }

        Paciente paciente = buscarPacientePorDni(peticion.pacienteDni());
        Profesional profesional = profesionalService.buscarEntidad(peticion.profesionalId());

        validarProfesionalActivo(profesional);

        boolean ocupado = turnoRepository.existsByProfesionalIdAndFechaAndHoraAndEstadoNotAndIdNot(
                profesional.getId(), peticion.fecha(), peticion.hora(), EstadoTurno.CANCELADO, id);

        if (ocupado) {
            throw new ReglaNegocioException("El profesional ya tiene otro turno el "
                    + peticion.fecha() + " a las " + peticion.hora().format(FORMATO_HORA));
        }

        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setFecha(peticion.fecha());
        turno.setHora(peticion.hora());
        turno.setMotivo(peticion.motivo());

        return aRespuesta(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cambiarEstado(Long id, String nuevoEstado) {
        Turno turno = buscarEntidad(id);
        EstadoTurno estado = convertirEstado(nuevoEstado);

        if (turno.getEstado() == EstadoTurno.ATENDIDO && estado == EstadoTurno.PENDIENTE) {
            throw new ReglaNegocioException("Un turno ya atendido no puede volver a estado pendiente");
        }

        turno.setEstado(estado);
        return aRespuesta(turnoRepository.save(turno));
    }

    @Transactional
    public void eliminar(Long id) {
        turnoRepository.delete(buscarEntidad(id));
    }

    Turno buscarEntidad(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el turno", id));
    }

    private Paciente buscarPacientePorDni(String dni) {
        return pacienteRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un paciente registrado con el DNI " + dni));
    }

    private void validarProfesionalActivo(Profesional profesional) {
        if (!profesional.isActivo()) {
            throw new ReglaNegocioException("El profesional "
                    + profesional.getApellido() + " está inactivo y no puede recibir turnos");
        }
    }

    private void validarNoEsPasado(LocalDate fecha, java.time.LocalTime hora) {
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se pueden registrar turnos en una fecha y hora que ya pasó");
        }
    }

    private EstadoTurno convertirEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoTurno.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("Estado de turno inválido: " + estado);
        }
    }

    static TurnoResponse aRespuesta(Turno turno) {
        Paciente paciente = turno.getPaciente();
        Profesional profesional = turno.getProfesional();

        return new TurnoResponse(
                turno.getId(),
                turno.getFecha(),
                turno.getHora().format(FORMATO_HORA),
                paciente.getId(),
                paciente.getDni(),
                paciente.getApellido() + ", " + paciente.getNombre(),
                profesional.getId(),
                profesional.getApellido() + " " + profesional.getNombre(),
                profesional.getEspecialidad().getId(),
                profesional.getEspecialidad().getNombre(),
                turno.getEstado().name(),
                turno.getMotivo());
    }

    /** Arma el patrón para el LIKE: "%" devuelve todos los registros. */
    private String patron(String texto) {
        return (texto == null || texto.isBlank()) ? "%" : "%" + texto.trim() + "%";
    }
}
