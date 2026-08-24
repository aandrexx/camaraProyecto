package com.losMatus.estadoCamaras.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
    private String direccion;
    private String contactoTecnico;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Camara> camaras;
}
