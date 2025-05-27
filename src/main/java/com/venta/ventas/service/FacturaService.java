package com.venta.ventas.service;

import com.venta.ventas.model.Factura;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.FacturaRepository;
import com.venta.ventas.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    public List<Factura> findAll() {
        return facturaRepository.findAll();
    }

    public Optional<Factura> findById(Long id) {
        return facturaRepository.findById(id);
    }

    public Optional<Factura> findByVentaId(Long ventaId) {
        return facturaRepository.findByVenta_Id(ventaId);
    }

    @Transactional
    public Factura emitirFactura(Long ventaId) {
        Optional<Venta> ventaOptional = ventaRepository.findById(ventaId);
        if (ventaOptional.isPresent()) {
            Venta venta = ventaOptional.get();
            Factura factura = new Factura();
            factura.setFechaEmision(LocalDateTime.now());
            factura.setTotalFactura(venta.getMontoTotal());
            factura.setVenta(venta);
            return facturaRepository.save(factura);
        }
        return null;
    }

    @Transactional
    public Optional<Factura> update(Long id, Factura facturaDetails) {
        Optional<Factura> optionalFactura = facturaRepository.findById(id);
        if (optionalFactura.isPresent()) {
            Factura existingFactura = optionalFactura.get();
            existingFactura.setFechaEmision(facturaDetails.getFechaEmision());
            existingFactura.setTotalFactura(facturaDetails.getTotalFactura());
            existingFactura.setVenta(facturaDetails.getVenta());
            return Optional.of(facturaRepository.save(existingFactura));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (facturaRepository.existsById(id)) {
            facturaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}