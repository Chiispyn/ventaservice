package com.venta.ventas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codFactura; // Cambiamos a Long para la convención de JPA

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private Double totalFactura;

    @OneToOne
    @JoinColumn(name = "venta_id", unique = true)
    private Venta venta;

    // Podría haber más campos como detalles de pago, información del cliente, etc.
}