package com.venta.ventas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "clientes")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) 
    private String rut;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = true) 
    private String email;

    @Column(nullable = true)
    private String direccion;

    @Column(nullable = true)
    private String telefono;

    
    @OneToMany(mappedBy = "cliente")
    @JsonBackReference 
    private List<Venta> ventas;


}