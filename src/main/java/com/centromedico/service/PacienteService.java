package com.centromedico.service;

import com.centromedico.dto.PacienteRequest;
import com.centromedico.dto.PacienteResponse;
import com.centromedico.exception.RecursoNoEncontradoException;
import com.centromedico.exception.ReglaNegocioException;
import com.centromedico.model.Paciente;
import com.centromedico.repository.AtencionRepository;
import com.centromedico.repository.PacienteRepository;
import com.centromedico.repository.TurnoRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final AtencionRepository atencionRepository;

    public PacienteService(PacienteRepository pacienteRepository,
                           TurnoRepository turnoRepository,
                           AtencionRepository atencionRepository) {
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
        this.atencionRepository = atencionRepository;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(String texto) {
        return pacienteRepository.buscar(patron(texto)).stream()
                .map(PacienteService::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorDni(String dni) {
        Paciente paciente = pacienteRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un paciente con DNI " + dni));
        return aRespuesta(paciente);
    }

    @Transactional
    public PacienteResponse crear(PacienteRequest peticion) {
        if (pacienteRepository.existsByDni(peticion.dni())) {
            throw new ReglaNegocioException("Ya existe un paciente registrado con el DNI " + peticion.dni());
        }

        Paciente paciente = new Paciente();
        copiarDatos(peticion, paciente);
        return aRespuesta(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponse actualizar(Long id, PacienteRequest peticion) {
        Paciente paciente = buscarEntidad(id);

        if (pacienteRepository.existsByDniAndIdNot(peticion.dni(), id)) {
            throw new ReglaNegocioException("Otro paciente ya está registrado con el DNI " + peticion.dni());
        }

        copiarDatos(peticion, paciente);
        return aRespuesta(pacienteRepository.save(paciente));
    }

    /**
     * Si el paciente tiene turnos o atenciones se hace baja lógica, para no
     * perder el historial clínico. Si no tiene nada asociado se borra de verdad.
     */
    @Transactional
    public void eliminar(Long id) {
        Paciente paciente = buscarEntidad(id);

        boolean tieneHistorial = turnoRepository.countByPacienteId(id) > 0
                || atencionRepository.countByPacienteId(id) > 0;

        if (tieneHistorial) {
            paciente.setActivo(false);
            pacienteRepository.save(paciente);
        } else {
            pacienteRepository.delete(paciente);
        }
    }

    Paciente buscarEntidad(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el paciente", id));
    }

    private void copiarDatos(PacienteRequest peticion, Paciente paciente) {
        paciente.setDni(peticion.dni());
        paciente.setNombre(peticion.nombre().trim());
        paciente.setApellido(peticion.apellido().trim());
        paciente.setFechaNacimiento(peticion.fechaNacimiento());
        paciente.setTelefono(peticion.telefono());
        paciente.setEmail(peticion.email());
        paciente.setDireccion(peticion.direccion());
        paciente.setObraSocial(peticion.obraSocial());
        paciente.setNroAfiliado(peticion.nroAfiliado());
        paciente.setGrupoSanguineo(peticion.grupoSanguineo());
        paciente.setAlergias(peticion.alergias());
    }

    static PacienteResponse aRespuesta(Paciente paciente) {
        int edad = Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears();
        return new PacienteResponse(
                paciente.getId(),
                paciente.getDni(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getApellido() + ", " + paciente.getNombre(),
                paciente.getFechaNacimiento(),
                edad,
                paciente.getTelefono(),
                paciente.getEmail(),
                paciente.getDireccion(),
                paciente.getObraSocial(),
                paciente.getNroAfiliado(),
                paciente.getGrupoSanguineo(),
                paciente.getAlergias());
    }

    /** Arma el patrón para el LIKE: "%" devuelve todos los registros. */
    private String patron(String texto) {
        return (texto == null || texto.isBlank()) ? "%" : "%" + texto.trim() + "%";
    }
}
