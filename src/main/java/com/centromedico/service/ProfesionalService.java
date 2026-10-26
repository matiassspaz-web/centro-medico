package com.centromedico.service;

import com.centromedico.dto.ProfesionalRequest;
import com.centromedico.dto.ProfesionalResponse;
import com.centromedico.exception.RecursoNoEncontradoException;
import com.centromedico.exception.ReglaNegocioException;
import com.centromedico.model.Especialidad;
import com.centromedico.model.Profesional;
import com.centromedico.repository.AtencionRepository;
import com.centromedico.repository.ProfesionalRepository;
import com.centromedico.repository.TurnoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadService especialidadService;
    private final TurnoRepository turnoRepository;
    private final AtencionRepository atencionRepository;

    public ProfesionalService(ProfesionalRepository profesionalRepository,
                              EspecialidadService especialidadService,
                              TurnoRepository turnoRepository,
                              AtencionRepository atencionRepository) {
        this.profesionalRepository = profesionalRepository;
        this.especialidadService = especialidadService;
        this.turnoRepository = turnoRepository;
        this.atencionRepository = atencionRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> listar(String texto, Long especialidadId) {
        return profesionalRepository.buscar(patron(texto), especialidadId).stream()
                .map(ProfesionalService::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> listarActivos() {
        return profesionalRepository.findByActivoTrueOrderByApellidoAscNombreAsc().stream()
                .map(ProfesionalService::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfesionalResponse obtenerPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional
    public ProfesionalResponse crear(ProfesionalRequest peticion) {
        String matricula = peticion.matricula().trim();

        if (profesionalRepository.existsByMatriculaIgnoreCase(matricula)) {
            throw new ReglaNegocioException("Ya existe un profesional con la matrícula " + matricula);
        }

        Profesional profesional = new Profesional();
        profesional.setMatricula(matricula);
        copiarDatos(peticion, profesional);
        profesional.setActivo(peticion.activo() == null || peticion.activo());

        return aRespuesta(profesionalRepository.save(profesional));
    }

    @Transactional
    public ProfesionalResponse actualizar(Long id, ProfesionalRequest peticion) {
        Profesional profesional = buscarEntidad(id);
        String matricula = peticion.matricula().trim();

        if (profesionalRepository.existsByMatriculaIgnoreCaseAndIdNot(matricula, id)) {
            throw new ReglaNegocioException("Otro profesional ya tiene la matrícula " + matricula);
        }

        profesional.setMatricula(matricula);
        copiarDatos(peticion, profesional);
        if (peticion.activo() != null) {
            profesional.setActivo(peticion.activo());
        }

        return aRespuesta(profesionalRepository.save(profesional));
    }

    /**
     * Si el profesional ya atendió o tiene turnos cargados se lo marca como
     * inactivo, para conservar la trazabilidad de esos registros.
     */
    @Transactional
    public void eliminar(Long id) {
        Profesional profesional = buscarEntidad(id);

        boolean tieneHistorial = turnoRepository.countByProfesionalId(id) > 0
                || atencionRepository.countByProfesionalId(id) > 0;

        if (tieneHistorial) {
            profesional.setActivo(false);
            profesionalRepository.save(profesional);
        } else {
            profesionalRepository.delete(profesional);
        }
    }

    Profesional buscarEntidad(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el profesional", id));
    }

    private void copiarDatos(ProfesionalRequest peticion, Profesional profesional) {
        Especialidad especialidad = especialidadService.buscarEntidad(peticion.especialidadId());

        profesional.setNombre(peticion.nombre().trim());
        profesional.setApellido(peticion.apellido().trim());
        profesional.setEspecialidad(especialidad);
        profesional.setTelefono(peticion.telefono());
        profesional.setEmail(peticion.email());
    }

    static ProfesionalResponse aRespuesta(Profesional profesional) {
        return new ProfesionalResponse(
                profesional.getId(),
                profesional.getMatricula(),
                profesional.getNombre(),
                profesional.getApellido(),
                profesional.getApellido() + " " + profesional.getNombre(),
                profesional.getEspecialidad().getId(),
                profesional.getEspecialidad().getNombre(),
                profesional.getTelefono(),
                profesional.getEmail(),
                profesional.isActivo());
    }

    /** Arma el patrón para el LIKE: "%" devuelve todos los registros. */
    private String patron(String texto) {
        return (texto == null || texto.isBlank()) ? "%" : "%" + texto.trim() + "%";
    }
}
