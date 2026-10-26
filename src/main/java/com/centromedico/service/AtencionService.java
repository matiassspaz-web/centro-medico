package com.centromedico.service;

import com.centromedico.dto.AtencionRequest;
import com.centromedico.dto.AtencionResponse;
import com.centromedico.exception.RecursoNoEncontradoException;
import com.centromedico.exception.ReglaNegocioException;
import com.centromedico.model.Atencion;
import com.centromedico.model.EstadoTurno;
import com.centromedico.model.Paciente;
import com.centromedico.model.Profesional;
import com.centromedico.model.Turno;
import com.centromedico.repository.AtencionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtencionService {

    private final AtencionRepository atencionRepository;
    private final PacienteService pacienteService;
    private final ProfesionalService profesionalService;
    private final TurnoService turnoService;

    public AtencionService(AtencionRepository atencionRepository,
                           PacienteService pacienteService,
                           ProfesionalService profesionalService,
                           TurnoService turnoService) {
        this.atencionRepository = atencionRepository;
        this.pacienteService = pacienteService;
        this.profesionalService = profesionalService;
        this.turnoService = turnoService;
    }

    /**
     * Historia clínica de un paciente, de la atención más reciente a la más antigua.
     */
    @Transactional(readOnly = true)
    public List<AtencionResponse> listarPorPaciente(Long pacienteId) {
        pacienteService.buscarEntidad(pacienteId);

        return atencionRepository.findByPacienteIdOrderByFechaDescIdDesc(pacienteId).stream()
                .map(AtencionService::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public AtencionResponse obtenerPorId(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la atención", id));
        return aRespuesta(atencion);
    }

    /**
     * Registra una atención. Si viene asociada a un turno, ese turno queda
     * marcado como atendido automáticamente.
     */
    @Transactional
    public AtencionResponse crear(AtencionRequest peticion) {
        Paciente paciente = pacienteService.buscarEntidad(peticion.pacienteId());
        Profesional profesional = profesionalService.buscarEntidad(peticion.profesionalId());

        Atencion atencion = new Atencion();
        atencion.setPaciente(paciente);
        atencion.setProfesional(profesional);
        atencion.setFecha(peticion.fecha());
        atencion.setMotivo(peticion.motivo().trim());
        atencion.setSintomas(peticion.sintomas().trim());
        atencion.setDiagnostico(peticion.diagnostico().trim());
        atencion.setTratamiento(peticion.tratamiento().trim());
        atencion.setIndicaciones(peticion.indicaciones().trim());
        atencion.setObservaciones(peticion.observaciones());

        if (peticion.turnoId() != null) {
            Turno turno = turnoService.buscarEntidad(peticion.turnoId());

            if (!turno.getPaciente().getId().equals(paciente.getId())) {
                throw new ReglaNegocioException("El turno indicado corresponde a otro paciente");
            }

            turno.setEstado(EstadoTurno.ATENDIDO);
            atencion.setTurno(turno);
        }

        return aRespuesta(atencionRepository.save(atencion));
    }

    @Transactional
    public void eliminar(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la atención", id));
        atencionRepository.delete(atencion);
    }

    static AtencionResponse aRespuesta(Atencion atencion) {
        Paciente paciente = atencion.getPaciente();
        Profesional profesional = atencion.getProfesional();

        return new AtencionResponse(
                atencion.getId(),
                paciente.getId(),
                paciente.getApellido() + ", " + paciente.getNombre(),
                profesional.getId(),
                profesional.getApellido() + " " + profesional.getNombre(),
                atencion.getFecha(),
                atencion.getMotivo(),
                atencion.getSintomas(),
                atencion.getDiagnostico(),
                atencion.getTratamiento(),
                atencion.getIndicaciones(),
                atencion.getObservaciones());
    }
}
