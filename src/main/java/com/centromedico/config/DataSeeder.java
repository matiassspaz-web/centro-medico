package com.centromedico.config;

import com.centromedico.model.Atencion;
import com.centromedico.model.Especialidad;
import com.centromedico.model.EstadoTurno;
import com.centromedico.model.Paciente;
import com.centromedico.model.Profesional;
import com.centromedico.model.Rol;
import com.centromedico.model.Turno;
import com.centromedico.model.Usuario;
import com.centromedico.repository.AtencionRepository;
import com.centromedico.repository.EspecialidadRepository;
import com.centromedico.repository.PacienteRepository;
import com.centromedico.repository.ProfesionalRepository;
import com.centromedico.repository.TurnoRepository;
import com.centromedico.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos de ejemplo la primera vez que arranca la aplicación.
 * Si la base ya tiene usuarios, no hace nada.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ProfesionalRepository profesionalRepository;
    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final AtencionRepository atencionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      EspecialidadRepository especialidadRepository,
                      ProfesionalRepository profesionalRepository,
                      PacienteRepository pacienteRepository,
                      TurnoRepository turnoRepository,
                      AtencionRepository atencionRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
        this.profesionalRepository = profesionalRepository;
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
        this.atencionRepository = atencionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        crearUsuarios();

        Especialidad clinica = crearEspecialidad("Clínica Médica",
                "Atención general y seguimiento de adultos.", true);
        Especialidad cardiovascular = crearEspecialidad("Cirugía Cardiovascular",
                "Cirugía del corazón y de los vasos sanguíneos.", true);
        Especialidad neurocirugia = crearEspecialidad("Neurocirugía",
                "Cirugía del cerebro, la médula y los nervios.", true);
        crearEspecialidad("Ginecología", "Salud del aparato reproductor femenino.", false);

        Profesional leiva = crearProfesional("MP-12345", "Omar", "Leiva",
                cardiovascular, "3865 24-7452", "omar.leiva@centromedico.com", true);
        Profesional chalon = crearProfesional("MP-23456", "Milagros", "Chalon",
                neurocirugia, "3865 25-8615", "milagros.chalon@centromedico.com", true);
        crearProfesional("MP-34567", "Lukas", "Alderete",
                clinica, "3865 63-2147", "lukas.alderete@centromedico.com", false);

        Paciente ituarte = crearPaciente("30145278", "Lukas", "Ituarte",
                LocalDate.of(1994, 3, 12), "3865 41-2250", "lukas.ituarte@email.com",
                "San Martín 450", "OSDE", "62-4458871-02", "A+", "Gatos");

        Paciente alderete = crearPaciente("41887520", "Matias", "Alderete",
                LocalDate.of(2002, 7, 4), "3865 55-1188", "matias.alderete@email.com",
                "Belgrano 1220", "Sancor Salud", "SS-9981234", "O+", "Ninguna conocida");

        Paciente perez = crearPaciente("28554103", "Juan", "Pérez",
                LocalDate.of(1981, 11, 23), "3865 42-7719", "juan.perez@email.com",
                "Avenida Sarmiento 880", "PAMI", "PA-2255410", "B+", "Penicilina");

        Paciente gomez = crearPaciente("35992014", "María", "Gómez",
                LocalDate.of(1990, 5, 9), "3865 47-3302", "maria.gomez@email.com",
                "Rivadavia 275", "OSESAC", "OS-7741200", "AB-", "Ninguna conocida");

        crearPaciente("26778450", "Carlos", "Rodríguez",
                LocalDate.of(1978, 1, 30), "3865 44-9076", "carlos.rodriguez@email.com",
                "Mitre 1340", "OSDE", "62-3310478-01", "O-", "Ibuprofeno");

        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDate pasado = LocalDate.now().plusDays(2);

        crearTurno(perez, leiva, manana, LocalTime.of(8, 30),
                EstadoTurno.PENDIENTE, "Control post operatorio");
        crearTurno(gomez, chalon, manana, LocalTime.of(10, 0),
                EstadoTurno.PENDIENTE, "Cefaleas persistentes");
        crearTurno(ituarte, leiva, pasado, LocalTime.of(9, 0),
                EstadoTurno.PENDIENTE, "Control de presión arterial");

        crearAtencion(ituarte, leiva, LocalDate.of(2026, 10, 1),
                "Control de presión arterial y mareos leves",
                "Cefalea frontal ocasional, leve mareo al levantarse rápidamente.",
                "Normotenso en consulta, probable mareo ortostático benigno.",
                "Hidratación adecuada. Dieta hiposódica preventiva.",
                "Medición diaria de presión arterial por 1 semana (mañana y noche).",
                "Volver si los síntomas persisten por más de 7 días.");

        crearAtencion(ituarte, leiva, LocalDate.of(2026, 8, 15),
                "Cuadro gripal y congestión nasal",
                "Congestión nasal, tos seca, febrícula (37.8°C) de 48 hs de evolución.",
                "Síndrome febril de vías aéreas superiores (cuadro viral).",
                "Paracetamol 500mg cada 8 hs si presenta fiebre o dolor.",
                "Abundante ingesta de líquidos, vaporizaciones de eucalipto.",
                "Reposo relativo por 48 horas.");

        crearAtencion(alderete, leiva, LocalDate.of(2026, 9, 20),
                "Dolor en tobillo derecho tras actividad física",
                "Edema leve en maléolo externo derecho, dolor a la palpación y a la pisada.",
                "Esguince de tobillo Grado I.",
                "Ibuprofeno 400mg cada 8 horas por 5 días con alimentos.",
                "Hielo local 15 min 3 veces al día, vendaje elástico y reposo deportivo.",
                "Evaluación en 10 días para alta médica deportiva.");
    }

    private void crearUsuarios() {
        crearUsuario("admin", "admin123", "Administrador del Sistema", Rol.ADMIN);
        crearUsuario("recepcion", "recepcion123", "Mesa de Entradas", Rol.RECEPCION);
        crearUsuario("leiva", "leiva123", "Dr. Omar Leiva", Rol.PROFESIONAL);
    }

    private void crearUsuario(String username, String password, String nombreCompleto, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    private Especialidad crearEspecialidad(String nombre, String descripcion, boolean activa) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(descripcion);
        especialidad.setActiva(activa);
        return especialidadRepository.save(especialidad);
    }

    private Profesional crearProfesional(String matricula, String nombre, String apellido,
                                         Especialidad especialidad, String telefono,
                                         String email, boolean activo) {
        Profesional profesional = new Profesional();
        profesional.setMatricula(matricula);
        profesional.setNombre(nombre);
        profesional.setApellido(apellido);
        profesional.setEspecialidad(especialidad);
        profesional.setTelefono(telefono);
        profesional.setEmail(email);
        profesional.setActivo(activo);
        return profesionalRepository.save(profesional);
    }

    private Paciente crearPaciente(String dni, String nombre, String apellido,
                                   LocalDate fechaNacimiento, String telefono, String email,
                                   String direccion, String obraSocial, String nroAfiliado,
                                   String grupoSanguineo, String alergias) {
        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombre(nombre);
        paciente.setApellido(apellido);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setTelefono(telefono);
        paciente.setEmail(email);
        paciente.setDireccion(direccion);
        paciente.setObraSocial(obraSocial);
        paciente.setNroAfiliado(nroAfiliado);
        paciente.setGrupoSanguineo(grupoSanguineo);
        paciente.setAlergias(alergias);
        paciente.setActivo(true);
        return pacienteRepository.save(paciente);
    }

    private void crearTurno(Paciente paciente, Profesional profesional, LocalDate fecha,
                            LocalTime hora, EstadoTurno estado, String motivo) {
        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setFecha(fecha);
        turno.setHora(hora);
        turno.setEstado(estado);
        turno.setMotivo(motivo);
        turnoRepository.save(turno);
    }

    private void crearAtencion(Paciente paciente, Profesional profesional, LocalDate fecha,
                               String motivo, String sintomas, String diagnostico,
                               String tratamiento, String indicaciones, String observaciones) {
        Atencion atencion = new Atencion();
        atencion.setPaciente(paciente);
        atencion.setProfesional(profesional);
        atencion.setFecha(fecha);
        atencion.setMotivo(motivo);
        atencion.setSintomas(sintomas);
        atencion.setDiagnostico(diagnostico);
        atencion.setTratamiento(tratamiento);
        atencion.setIndicaciones(indicaciones);
        atencion.setObservaciones(observaciones);
        atencionRepository.save(atencion);
    }
}
