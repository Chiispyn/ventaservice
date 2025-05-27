package com.venta.ventas.controller;

import com.venta.ventas.model.Venta;
import com.venta.ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> getAllVentas() {
        return ventaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> getVentaById(@PathVariable Long id) {
        Optional<Venta> venta = ventaService.findById(id);
        return venta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Venta> createVenta(@RequestBody Venta venta) {
        Venta savedVenta = ventaService.save(venta);
        return new ResponseEntity<>(savedVenta, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venta> updateVenta(@PathVariable Long id, @RequestBody Venta ventaDetails) {
        Optional<Venta> updatedVenta = ventaService.update(id, ventaDetails);
        return updatedVenta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenta(@PathVariable Long id) {
        if (ventaService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/descuento")
    public ResponseEntity<Venta> aplicarDescuento(@PathVariable Long id, @RequestParam Double porcentajeDescuento) {
        Optional<Venta> updatedVenta = ventaService.aplicarDescuento(id, porcentajeDescuento);
        return updatedVenta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Venta> cancelarVenta(@PathVariable Long id) {
        Optional<Venta> cancelledVenta = ventaService.cancelarVenta(id);
        return cancelledVenta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/factura")
    public ResponseEntity<String> generarFactura(@PathVariable Long id) {
        String factura = ventaService.generarFactura(id);
        if (factura != null) {
            return ResponseEntity.ok(factura);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}