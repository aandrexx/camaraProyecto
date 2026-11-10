package com.losMatus.estadoCamaras.entity;

import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
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

    @Column(length = 1000)
    private String motivo;

    private LocalDateTime fechaCambio;

    public HistorialEstado(){

    }

    public HistorialEstado(Long id, Camara camara, EstadoCamara estadoAnterior, EstadoCamara estadoNuevo, TipoEvento tipoEvento, String motivo, LocalDateTime fechaCambio) {
        this.id = id;
        this.camara = camara;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.tipoEvento = tipoEvento;
        this.motivo = motivo;
        this.fechaCambio = fechaCambio;
    }

    public HistorialEstado(Long id, Camara camara, EstadoCamara estadoAnterior, EstadoCamara estadoNuevo, TipoEvento tipoEvento, LocalDateTime fechaCambio) {
        this(id, camara, estadoAnterior, estadoNuevo, tipoEvento, null, fechaCambio);
    }
}
