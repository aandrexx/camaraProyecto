package com.losMatus.estadoCamaras.repository;

import com.losMatus.estadoCamaras.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByCamaraIdOrderByFechaCambioDesc(Long camaraId);
}
