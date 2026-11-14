package com.losMatus.estadoCamaras.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tecnicos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // Formato internacional, sin "+" ni espacios (ej: 51987654321)
    @Column(unique = true, nullable = false, length = 15)
    private String telefono;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}
