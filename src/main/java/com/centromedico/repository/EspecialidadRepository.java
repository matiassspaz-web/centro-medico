package com.centromedico.repository;

import com.centromedico.model.Especialidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Especialidad> findByActivaTrueOrderByNombreAsc();

    @Query("""
            SELECT e FROM Especialidad e
            WHERE LOWER(e.nombre) LIKE LOWER(:patron)
            ORDER BY e.nombre ASC
            """)
    List<Especialidad> buscar(@Param("patron") String patron);
}
