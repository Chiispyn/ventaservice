// src/main/java/com/venta/ventas/repositories/VentaRepository.java
package com.venta.ventas.repository;

import com.venta.ventas.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Venta.
 * Proporciona métodos CRUD básicos y permite definir consultas personalizadas.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
}