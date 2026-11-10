package com.losMatus.estadoCamaras.repository;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CamaraRepository extends JpaRepository<Camara, Long> {

    List<Camara> findAllByEliminadaFalse();

    List<Camara> findAllByEliminadaTrue();

    Optional<Camara> findByIdAndEliminadaFalse(Long id);

    @Modifying
    @Transactional
    @Query("update Camara c set c.eliminada = true where c.id = :id and c.eliminada = false")
    int marcarComoEliminada(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Camara c set c.eliminada = false where c.id = :id and c.eliminada = true")
    int restaurar(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("""
            update Camara c
               set c.estado = :estado,
                   c.ultimaVerificacion = :verificacion,
                   c.ultimaMacDetectada = :mac,
                   c.fallosConsecutivos = :fallos
             where c.id = :id
            """)
    int actualizarEstadoMonitoreo(@Param("id") Long id,
                                  @Param("estado") EstadoCamara estado,
                                  @Param("verificacion") LocalDateTime verificacion,
                                  @Param("mac") String mac,
                                  @Param("fallos") Integer fallos);
}
