package com.losMatus.estadoCamaras.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "camara")
public class Camara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; //entrada principal, salida

    @Column(nullable = false)
    private String ip;

    private Integer puerto;
    private String ubicacion; //pasillo o recepcion

    @Enumerated(EnumType.STRING)
    private EstadoCamara estado;

    private LocalDateTime ultimaVerificacion;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;
}
