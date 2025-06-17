package com.venta.ventas.service;

import com.venta.ventas.model.Factura;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.FacturaRepository;
import com.venta.ventas.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Factura factura1;
    private Factura factura2;
    private Venta venta1;
    @BeforeEach
    void setUp() {
        venta1 = new Venta();
        venta1.setId(1L);
        venta1.setMontoTotal(100.0);
        venta1.setFechaVenta(LocalDateTime.now());

        factura1 = new Factura();
        factura1.setCodFactura(1L); 
        factura1.setFechaEmision(LocalDateTime.now());
        factura1.setTotalFactura(100.0);
        factura1.setVenta(venta1);

        factura2 = new Factura();
        factura2.setCodFactura(2L); 
        factura2.setFechaEmision(LocalDateTime.now().plusDays(1));
        factura2.setTotalFactura(200.0);
        factura2.setVenta(new Venta());
}

    @Test
    void findAll_shouldReturnAllFacturas() {
        // Arrange
        List<Factura> expectedFacturas = Arrays.asList(factura1, factura2);
        when(facturaRepository.findAll()).thenReturn(expectedFacturas);

        // Act
        List<Factura> actualFacturas = facturaService.findAll();

        // Assert
        assertNotNull(actualFacturas);
        assertEquals(2, actualFacturas.size());
        assertEquals(expectedFacturas, actualFacturas);
        verify(facturaRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnFacturaWhenFound() {
        // Arrange
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura1));

        // Act
        Optional<Factura> actualFactura = facturaService.findById(1L);

        // Assert
        assertTrue(actualFactura.isPresent());
        assertEquals(factura1, actualFactura.get());
        verify(facturaRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Factura> actualFactura = facturaService.findById(99L);

        // Assert
        assertFalse(actualFactura.isPresent());
        verify(facturaRepository, times(1)).findById(99L);
    }

    @Test
    void findByVentaId_shouldReturnFacturaWhenFound() {
        // Arrange
        when(facturaRepository.findByVenta_Id(1L)).thenReturn(Optional.of(factura1));

        // Act
        Optional<Factura> actualFactura = facturaService.findByVentaId(1L);

        // Assert
        assertTrue(actualFactura.isPresent());
        assertEquals(factura1, actualFactura.get());
        verify(facturaRepository, times(1)).findByVenta_Id(1L);
    }

    @Test
    void findByVentaId_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(facturaRepository.findByVenta_Id(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Factura> actualFactura = facturaService.findByVentaId(99L);

        // Assert
        assertFalse(actualFactura.isPresent());
        verify(facturaRepository, times(1)).findByVenta_Id(99L);
    }

    @Test
    void emitirFactura_shouldCreateFacturaWhenVentaExists() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta1));
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura1);

        // Act
        Factura createdFactura = facturaService.emitirFactura(1L);

        // Assert
        assertNotNull(createdFactura);
        assertEquals(venta1.getMontoTotal(), createdFactura.getTotalFactura());
        assertEquals(venta1, createdFactura.getVenta());
        assertNotNull(createdFactura.getFechaEmision());
        verify(ventaRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    void emitirFactura_shouldReturnNullWhenVentaDoesNotExist() {
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Factura createdFactura = facturaService.emitirFactura(99L);

        // Assert
        assertNull(createdFactura);
        verify(ventaRepository, times(1)).findById(99L);
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void update_shouldUpdateFacturaWhenFound() {
        // Arrange
        Factura updatedDetails = new Factura();
        updatedDetails.setFechaEmision(LocalDateTime.now().plusDays(5));
        updatedDetails.setTotalFactura(300.0);
        updatedDetails.setVenta(new Venta()); // Simular una venta diferente si es necesario

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura1));
        when(facturaRepository.save(any(Factura.class))).thenReturn(updatedDetails); // Retornar los detalles actualizados

        // Act
        Optional<Factura> actualFactura = facturaService.update(1L, updatedDetails);

        // Assert
        assertTrue(actualFactura.isPresent());
        assertEquals(updatedDetails.getFechaEmision(), actualFactura.get().getFechaEmision());
        assertEquals(updatedDetails.getTotalFactura(), actualFactura.get().getTotalFactura());
        verify(facturaRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    void update_shouldReturnEmptyWhenFacturaNotFound() {
        // Arrange
        Factura updatedDetails = new Factura();
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Factura> actualFactura = facturaService.update(99L, updatedDetails);

        // Assert
        assertFalse(actualFactura.isPresent());
        verify(facturaRepository, times(1)).findById(99L);
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void deleteById_shouldReturnTrueWhenFacturaExists() {
        // Arrange
        when(facturaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(facturaRepository).deleteById(1L);

        // Act
        boolean result = facturaService.deleteById(1L);

        // Assert
        assertTrue(result);
        verify(facturaRepository, times(1)).existsById(1L);
        verify(facturaRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldReturnFalseWhenFacturaDoesNotExist() {
        // Arrange
        when(facturaRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = facturaService.deleteById(99L);

        // Assert
        assertFalse(result);
        verify(facturaRepository, times(1)).existsById(99L);
        verify(facturaRepository, never()).deleteById(anyLong());
    }
}