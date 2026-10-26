package com.centromedico.repository;

import com.centromedico.model.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    /**
     * El parámetro llega como patrón LIKE ya armado ("%texto%", o "%" si no se filtra).
     */
    @Query("""
            SELECT p FROM Paciente p
            WHERE p.activo = true
              AND (LOWER(p.nombre) LIKE LOWER(:patron)
                   OR LOWER(p.apellido) LIKE LOWER(:patron)
                   OR p.dni LIKE :patron)
            ORDER BY p.apellido ASC, p.nombre ASC
            """)
    List<Paciente> buscar(@Param("patron") String patron);
}
