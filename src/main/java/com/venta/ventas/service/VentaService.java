package com.venta.ventas.service;

import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.model.Cliente;
import com.venta.ventas.model.DetalleVenta;
import com.venta.ventas.model.Producto;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.ClienteRepository;
import com.venta.ventas.repository.VentaRepository;
import com.venta.ventas.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> findById(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public Venta save(Venta venta, Long clienteId) {
        Optional<Cliente> clienteOptional = clienteRepository.findById(clienteId);
        if (clienteOptional.isEmpty()) {
            throw new RuntimeException("Cliente con ID " + clienteId + " no encontrado.");
        }
        venta.setCliente(clienteOptional.get());

        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            venta.getDetalles().forEach(detalle -> {
                Optional<Producto> productoOptional = productoRepository.findById(detalle.getProducto().getId());
                if (productoOptional.isEmpty()) {
                    throw new RuntimeException("Producto con ID " + detalle.getProducto().getId() + " no encontrado.");
                }
                detalle.setVenta(venta);
                detalle.setPrecioUnitario(productoOptional.get().getPrecio());
            });
            venta.calcularMontoTotal();
        } else {
            venta.setMontoTotal(0.0);
        }

        if (venta.getEstado() == null) {
            venta.setEstado(EstadoVenta.PENDIENTE);
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
            // Si la venta está pendiente, aplica el descuento.
            if (venta.getEstado() == EstadoVenta.PENDIENTE) {
                // Opcional: Validar el porcentaje de descuento aquí también, si no lo haces en otro lado.
                // if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
                //     throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100.");
                // }
                double montoOriginal = venta.getMontoTotal();
                double montoConDescuento = montoOriginal * (1 - (porcentajeDescuento / 100.0));
                venta.setMontoTotal(montoConDescuento);
                return Optional.of(ventaRepository.save(venta));
            } else {
                // Si la venta no está en estado PENDIENTE, no se aplica el descuento,
                // pero la venta fue encontrada, así que la retornamos sin modificar.
                return Optional.of(venta); // <--- ESTE ES EL CAMBIO CLAVE
            }
        }
        // Si la venta no se encontró por ID, retornamos Optional.empty().
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
            if (venta.getCliente() != null) {
                factura.append("Cliente: ").append(venta.getCliente().getNombreCompleto())
                       .append(" (RUT: ").append(venta.getCliente().getRut()).append(")\n");
            } else {
                factura.append("Cliente: No Asignado\n");
            }

            factura.append("Medio de Envío: ").append(venta.getMedioEnvio()).append("\n");
            factura.append("Estado: ").append(venta.getEstado()).append("\n");
            factura.append("--- Detalles ---\n");

            if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    factura.append(detalle.getCantidad()).append(" x ")
                            .append(detalle.getProducto().getNombre()).append(" @ ")
                            .append(String.format(Locale.ROOT, "%.2f", detalle.getPrecioUnitario()))
                            .append(" = ").append(String.format(Locale.ROOT, "%.2f", detalle.getSubtotal())).append("\n");
                }
                factura.append("-------------------\n");
            } else {
                factura.append("No hay productos en esta venta.\n");
            }
            factura.append("Monto Total: ").append(String.format(Locale.ROOT, "%.2f", venta.getMontoTotal())).append("\n");
            return factura.toString();
        }
        return null;
    }

    public List<Cliente> findAllClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> findClienteById(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional
    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Optional<Cliente> updateCliente(Long id, Cliente clienteDetails) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(id);
        if (optionalCliente.isPresent()) {
            Cliente existingCliente = optionalCliente.get();
            existingCliente.setRut(clienteDetails.getRut());
            existingCliente.setNombreCompleto(clienteDetails.getNombreCompleto());
            existingCliente.setEmail(clienteDetails.getEmail());
            existingCliente.setDireccion(clienteDetails.getDireccion());
            existingCliente.setTelefono(clienteDetails.getTelefono());
            return Optional.of(clienteRepository.save(existingCliente));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean deleteClienteById(Long id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}