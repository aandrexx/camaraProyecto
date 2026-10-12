package com.losMatus.estadoCamaras.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "fallos_consecutivos")
    private Integer fallosConsecutivos;

    @Enumerated(EnumType.STRING)
    private EstadoCamara estado;

    private LocalDateTime ultimaVerificacion;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

}
