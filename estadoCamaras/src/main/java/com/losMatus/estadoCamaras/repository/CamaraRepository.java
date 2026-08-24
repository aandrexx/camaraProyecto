package com.losMatus.estadoCamaras.repository;

import com.losMatus.estadoCamaras.entity.Camara;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CamaraRepository extends JpaRepository<Camara, Long> {

    List<Camara> findByEmpresaId(Long empresaId);
}
