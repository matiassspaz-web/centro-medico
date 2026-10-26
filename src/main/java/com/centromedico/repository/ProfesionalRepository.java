package com.centromedico.repository;

import com.centromedico.model.Profesional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);

    long countByEspecialidadId(Long especialidadId);

    List<Profesional> findByActivoTrueOrderByApellidoAscNombreAsc();

    @Query("""
            SELECT p FROM Profesional p
            WHERE (LOWER(p.nombre) LIKE LOWER(:patron)
                   OR LOWER(p.apellido) LIKE LOWER(:patron)
                   OR LOWER(p.matricula) LIKE LOWER(:patron))
              AND (:especialidadId IS NULL OR p.especialidad.id = :especialidadId)
            ORDER BY p.apellido ASC, p.nombre ASC
            """)
    List<Profesional> buscar(@Param("patron") String patron,
                             @Param("especialidadId") Long especialidadId);
}
