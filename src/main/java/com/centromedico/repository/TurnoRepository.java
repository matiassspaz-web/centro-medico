package com.centromedico.repository;

import com.centromedico.model.EstadoTurno;
import com.centromedico.model.Turno;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    boolean existsByProfesionalIdAndFechaAndHoraAndEstadoNot(
            Long profesionalId, LocalDate fecha, LocalTime hora, EstadoTurno estado);

    boolean existsByProfesionalIdAndFechaAndHoraAndEstadoNotAndIdNot(
            Long profesionalId, LocalDate fecha, LocalTime hora, EstadoTurno estado, Long id);

    long countByPacienteId(Long pacienteId);

    long countByProfesionalId(Long profesionalId);

    long countByFechaAndEstadoNot(LocalDate fecha, EstadoTurno estado);

    long countByEstado(EstadoTurno estado);

    List<Turno> findByProfesionalIdAndFechaBetweenAndEstadoNotOrderByFechaAscHoraAsc(
            Long profesionalId, LocalDate desde, LocalDate hasta, EstadoTurno estado);

    List<Turno> findByPacienteIdOrderByFechaDescHoraDesc(Long pacienteId);

    @Query("""
            SELECT t FROM Turno t
            WHERE (LOWER(t.paciente.apellido) LIKE LOWER(:patron)
                   OR LOWER(t.paciente.nombre) LIKE LOWER(:patron)
                   OR t.paciente.dni LIKE :patron)
              AND (:profesionalId IS NULL OR t.profesional.id = :profesionalId)
              AND (:fecha IS NULL OR t.fecha = :fecha)
              AND (:estado IS NULL OR t.estado = :estado)
            ORDER BY t.fecha DESC, t.hora ASC
            """)
    List<Turno> buscar(@Param("patron") String patron,
                       @Param("profesionalId") Long profesionalId,
                       @Param("fecha") LocalDate fecha,
                       @Param("estado") EstadoTurno estado);
}
