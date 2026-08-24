package com.losMatus.estadoCamaras.repository;

import com.losMatus.estadoCamaras.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
