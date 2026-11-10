package com.losMatus.estadoCamaras.entity;

import com.losMatus.estadoCamaras.enums.EstadoCamara;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "camara")
@Builder
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

    @Column(name = "mac_esperada")
    private String macEsperada;

    @Column(name = "ultima_mac_detectada")
    private String ultimaMacDetectada;

    @Builder.Default
    @Column(name = "fallos_consecutivos", nullable = false)
    private Integer fallosConsecutivos = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCamara estado = EstadoCamara.DESCONOCIDO;

    private LocalDateTime ultimaVerificacion;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private boolean eliminada = false;



}
