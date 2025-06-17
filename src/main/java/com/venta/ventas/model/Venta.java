package com.venta.ventas.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.enums.EstadoVenta;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaVenta;

    @Column(nullable = false)
    private Double montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedioEnvio medioEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalleVenta> detalles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoVenta.PENDIENTE;
        }
    }

    public void calcularMontoTotal() {
        if (detalles != null) {
            this.montoTotal = detalles.stream()
                    .mapToDouble(DetalleVenta::getSubtotal)
                    .sum();
        } else {
            this.montoTotal = 0.0;
        }
    }
}