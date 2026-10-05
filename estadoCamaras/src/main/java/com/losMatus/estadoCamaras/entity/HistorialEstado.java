package com.losMatus.estadoCamaras.entity;

import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historial_estado")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "camara_id", nullable = false)
    private Camara camara;

    @Enumerated(EnumType.STRING)
    private EstadoCamara estadoAnterior;

    @Enumerated(EnumType.STRING)
    private EstadoCamara estadoNuevo;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    private LocalDateTime fechaCambio;


}
