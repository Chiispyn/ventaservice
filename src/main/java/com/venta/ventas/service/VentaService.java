// src/main/java/com/venta/ventas/services/VentaService.java
package com.venta.ventas.service;

import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.model.DetalleVenta;
import com.venta.ventas.model.Producto;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.VentaRepository;
import com.venta.ventas.repository.ProductoRepository; // Necesitamos esto para verificar productos
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> findById(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public Venta save(Venta venta) {
        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            venta.getDetalles().forEach(detalle -> {
                Optional<Producto> productoOptional = productoRepository.findById(detalle.getProducto().getId());
                if (productoOptional.isEmpty()) {
                    throw new RuntimeException("Producto con ID " + detalle.getProducto().getId() + " no encontrado.");
                }
                detalle.setVenta(venta);
                detalle.setPrecioUnitario(productoOptional.get().getPrecio()); // Establecer el precio de venta actual
            });
            venta.calcularMontoTotal();
        } else {
            venta.setMontoTotal(0.0); // Si no hay detalles, el monto total es 0
        }
        return ventaRepository.save(venta);
    }

    @Transactional
    public Optional<Venta> update(Long id, Venta ventaDetails) {
        Optional<Venta> optionalVenta = ventaRepository.findById(id);
        if (optionalVenta.isPresent()) {
            Venta existingVenta = optionalVenta.get();
            existingVenta.setFechaVenta(ventaDetails.getFechaVenta());
            existingVenta.setMontoTotal(ventaDetails.getMontoTotal());
            existingVenta.setMedioEnvio(ventaDetails.getMedioEnvio());
            existingVenta.setEstado(ventaDetails.getEstado());


            return Optional.of(ventaRepository.save(existingVenta));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (ventaRepository.existsById(id)) {
            ventaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Venta> aplicarDescuento(Long ventaId, Double porcentajeDescuento) {
        Optional<Venta> optionalVenta = ventaRepository.findById(ventaId);
        if (optionalVenta.isPresent()) {
            Venta venta = optionalVenta.get();
            if (venta.getEstado() == EstadoVenta.PENDIENTE) {
                double montoOriginal = venta.getMontoTotal();
                double montoConDescuento = montoOriginal * (1 - (porcentajeDescuento / 100.0));
                venta.setMontoTotal(montoConDescuento);
                return Optional.of(ventaRepository.save(venta));
            }
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Venta> cancelarVenta(Long ventaId) {
        Optional<Venta> optionalVenta = ventaRepository.findById(ventaId);
        if (optionalVenta.isPresent()) {
            Venta venta = optionalVenta.get();
            venta.setEstado(EstadoVenta.CANCELADA);
            return Optional.of(ventaRepository.save(venta));
        }
        return Optional.empty();
    }

    public String generarFactura(Long ventaId) {
        Optional<Venta> optionalVenta = ventaRepository.findById(ventaId);
        if (optionalVenta.isPresent()) {
            Venta venta = optionalVenta.get();
            StringBuilder factura = new StringBuilder("Factura para la Venta ID: " + venta.getId() + "\n");
            factura.append("Fecha: ").append(venta.getFechaVenta()).append("\n");
            factura.append("Medio de Envío: ").append(venta.getMedioEnvio()).append("\n");
            factura.append("Estado: ").append(venta.getEstado()).append("\n");
            factura.append("--- Detalles ---\n");
            if (venta.getDetalles() != null) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    factura.append(detalle.getCantidad()).append(" x ")
                           .append(detalle.getProducto().getNombre()).append(" @ ")
                           .append(String.format("%.2f", detalle.getPrecioUnitario()))
                           .append(" = ").append(String.format("%.2f", detalle.getSubtotal())).append("\n");
                }
                factura.append("-------------------\n");
                factura.append("Monto Total: ").append(String.format("%.2f", venta.getMontoTotal())).append("\n");
            } else {
                factura.append("No hay productos en esta venta.\n");
                factura.append("Monto Total: ").append(String.format("%.2f", venta.getMontoTotal())).append("\n");
            }
            return factura.toString();
        }
        return null;
    }
}