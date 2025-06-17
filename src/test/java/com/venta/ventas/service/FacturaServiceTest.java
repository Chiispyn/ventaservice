package com.venta.ventas.service;

import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.model.Factura;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.FacturaRepository;
import com.venta.ventas.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Factura factura;
    private Venta venta;

    @BeforeEach
    void setUp() {
        venta = new Venta(1L, LocalDateTime.now(), 250.0, MedioEnvio.DESPACHO_A_DOMICILIO, EstadoVenta.COMPLETADA, null, null);
        factura = new Factura(1L, LocalDateTime.now(), 250.0, venta);
    }

    @Test
    @DisplayName("findAll - Debe retornar todas las facturas existentes")
    void findAll_shouldReturnAllFacturas() {
        when(facturaRepository.findAll()).thenReturn(Arrays.asList(factura));

        List<Factura> facturas = facturaService.findAll();

        assertNotNull(facturas);
        assertFalse(facturas.isEmpty());
        assertEquals(1, facturas.size());
        assertEquals(factura.getCodFactura(), facturas.get(0).getCodFactura());
        verify(facturaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById - Debe retornar una factura específica por su ID")
    void findById_shouldReturnFacturaById() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        Optional<Factura> foundFactura = facturaService.findById(1L);

        assertTrue(foundFactura.isPresent());
        assertEquals(factura.getCodFactura(), foundFactura.get().getCodFactura());
        verify(facturaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe retornar un Optional vacío si la factura no se encuentra por ID")
    void findById_shouldReturnEmptyIfFacturaNotFound() {
        when(facturaRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Factura> foundFactura = facturaService.findById(99L);

        assertTrue(foundFactura.isEmpty());
        verify(facturaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("findByVentaId - Debe retornar una factura por el ID de la venta asociada")
    void findByVentaId_shouldReturnFacturaByVentaId() {
        when(facturaRepository.findByVenta_Id(1L)).thenReturn(Optional.of(factura));

        Optional<Factura> foundFactura = facturaService.findByVentaId(1L);

        assertTrue(foundFactura.isPresent());
        assertEquals(factura.getCodFactura(), foundFactura.get().getCodFactura());
        assertEquals(venta.getId(), foundFactura.get().getVenta().getId());
        verify(facturaRepository, times(1)).findByVenta_Id(1L);
    }

    @Test
    @DisplayName("findByVentaId - Debe retornar Optional vacío si no hay factura para el ID de venta")
    void findByVentaId_shouldReturnEmptyIfNoFacturaForVentaId() {
        when(facturaRepository.findByVenta_Id(anyLong())).thenReturn(Optional.empty());

        Optional<Factura> foundFactura = facturaService.findByVentaId(99L);

        assertTrue(foundFactura.isEmpty());
        verify(facturaRepository, times(1)).findByVenta_Id(99L);
    }


    @Test
    @DisplayName("emitirFactura - Debe crear y guardar una nueva factura para una venta existente")
    void emitirFactura_shouldCreateAndSaveFactura() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> {
            Factura savedFactura = invocation.getArgument(0);
            savedFactura.setCodFactura(2L); // Simular asignación de ID
            return savedFactura;
        });

        Factura emittedFactura = facturaService.emitirFactura(1L);

        assertNotNull(emittedFactura);
        assertEquals(2L, emittedFactura.getCodFactura());
        assertEquals(venta.getMontoTotal(), emittedFactura.getTotalFactura());
        assertEquals(venta, emittedFactura.getVenta());
        assertNotNull(emittedFactura.getFechaEmision());

        verify(ventaRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("emitirFactura - Debe retornar null si la venta no se encuentra al emitir factura")
    void emitirFactura_shouldReturnNullIfVentaNotFound() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());

        Factura emittedFactura = facturaService.emitirFactura(99L);

        assertNull(emittedFactura);
        verify(ventaRepository, times(1)).findById(99L);
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("update - Debe actualizar una factura existente")
    void update_shouldUpdateExistingFactura() {
        // La factura original tiene un ID, la updatedDetails simula el cuerpo de la petición
        Factura updatedDetails = new Factura(null, LocalDateTime.now().plusDays(1), 300.0, venta);
        
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Factura> result = facturaService.update(1L, updatedDetails);

        assertTrue(result.isPresent());
        assertEquals(updatedDetails.getFechaEmision(), result.get().getFechaEmision());
        assertEquals(updatedDetails.getTotalFactura(), result.get().getTotalFactura());
        assertEquals(updatedDetails.getVenta(), result.get().getVenta()); // La venta asociada también puede ser actualizada

        verify(facturaRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("update - Debe retornar Optional vacío si la factura a actualizar no se encuentra")
    void update_shouldReturnEmptyIfFacturaNotFound() {
        when(facturaRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Factura> result = facturaService.update(99L, new Factura());

        assertTrue(result.isEmpty());

        verify(facturaRepository, times(1)).findById(99L);
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    @DisplayName("deleteById - Debe eliminar una factura exitosamente por su ID")
    void deleteById_shouldDeleteFacturaById() {
        when(facturaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(facturaRepository).deleteById(1L);

        boolean deleted = facturaService.deleteById(1L);

        assertTrue(deleted);
        verify(facturaRepository, times(1)).existsById(1L);
        verify(facturaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - Debe retornar falso si la factura a eliminar no existe")
    void deleteById_shouldReturnFalseIfFacturaNotFound() {
        when(facturaRepository.existsById(anyLong())).thenReturn(false);

        boolean deleted = facturaService.deleteById(99L);

        assertFalse(deleted);
        verify(facturaRepository, times(1)).existsById(99L);
        verify(facturaRepository, never()).deleteById(anyLong());
    }
}