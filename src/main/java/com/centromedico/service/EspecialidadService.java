package com.centromedico.service;

import com.centromedico.dto.EspecialidadRequest;
import com.centromedico.dto.EspecialidadResponse;
import com.centromedico.exception.RecursoNoEncontradoException;
import com.centromedico.exception.ReglaNegocioException;
import com.centromedico.model.Especialidad;
import com.centromedico.repository.EspecialidadRepository;
import com.centromedico.repository.ProfesionalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalRepository profesionalRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository,
                               ProfesionalRepository profesionalRepository) {
        this.especialidadRepository = especialidadRepository;
        this.profesionalRepository = profesionalRepository;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar(String texto) {
        return especialidadRepository.buscar(patron(texto)).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarActivas() {
        return especialidadRepository.findByActivaTrueOrderByNombreAsc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadResponse obtenerPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional
    public EspecialidadResponse crear(EspecialidadRequest peticion) {
        String nombre = peticion.nombre().trim();

        if (especialidadRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una especialidad llamada " + nombre);
        }

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(peticion.descripcion());
        especialidad.setActiva(peticion.activa() == null || peticion.activa());

        return aRespuesta(especialidadRepository.save(especialidad));
    }

    @Transactional
    public EspecialidadResponse actualizar(Long id, EspecialidadRequest peticion) {
        Especialidad especialidad = buscarEntidad(id);
        String nombre = peticion.nombre().trim();

        if (especialidadRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Otra especialidad ya se llama " + nombre);
        }

        especialidad.setNombre(nombre);
        especialidad.setDescripcion(peticion.descripcion());
        if (peticion.activa() != null) {
            especialidad.setActiva(peticion.activa());
        }

        return aRespuesta(especialidadRepository.save(especialidad));
    }

    @Transactional
    public void eliminar(Long id) {
        Especialidad especialidad = buscarEntidad(id);

        long profesionales = profesionalRepository.countByEspecialidadId(id);
        if (profesionales > 0) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la especialidad porque tiene " + profesionales
                            + " profesional(es) asociado(s). Desactívela en lugar de eliminarla.");
        }

        especialidadRepository.delete(especialidad);
    }

    Especialidad buscarEntidad(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la especialidad", id));
    }

    private EspecialidadResponse aRespuesta(Especialidad especialidad) {
        return new EspecialidadResponse(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion(),
                especialidad.isActiva(),
                profesionalRepository.countByEspecialidadId(especialidad.getId()));
    }

    /** Arma el patrón para el LIKE: "%" devuelve todos los registros. */
    private String patron(String texto) {
        return (texto == null || texto.isBlank()) ? "%" : "%" + texto.trim() + "%";
    }
}
