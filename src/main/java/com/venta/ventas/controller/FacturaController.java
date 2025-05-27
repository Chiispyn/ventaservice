package com.venta.ventas.controller;

import com.venta.ventas.model.Factura;
import com.venta.ventas.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping
    public List<Factura> getAllFacturas() {
        return facturaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> getFacturaById(@PathVariable Long id) {
        Optional<Factura> factura = facturaService.findById(id);
        return factura.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<Factura> getFacturaByVentaId(@PathVariable Long ventaId) {
        Optional<Factura> factura = facturaService.findByVentaId(ventaId);
        return factura.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/emitir/{ventaId}")
    public ResponseEntity<Factura> emitirFactura(@PathVariable Long ventaId) {
        Factura facturaEmitida = facturaService.emitirFactura(ventaId);
        if (facturaEmitida != null) {
            return new ResponseEntity<>(facturaEmitida, HttpStatus.CREATED);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Factura> updateFactura(@PathVariable Long id, @RequestBody Factura facturaDetails) {
        Optional<Factura> updatedFactura = facturaService.update(id, facturaDetails);
        return updatedFactura.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFactura(@PathVariable Long id) {
        if (facturaService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}