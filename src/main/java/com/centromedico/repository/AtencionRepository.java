package com.centromedico.repository;

import com.centromedico.model.Atencion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    List<Atencion> findByPacienteIdOrderByFechaDescIdDesc(Long pacienteId);

    long countByPacienteId(Long pacienteId);

    long countByProfesionalId(Long profesionalId);
}
