package com.venta.ventas.repository;

import com.venta.ventas.model.Factura;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByVenta_Id(Long ventaId);
}