package com.venta.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venta.ventas.model.Factura;
import com.venta.ventas.model.Venta;
import com.venta.ventas.service.FacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacturaController.class)
public class FacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacturaService facturaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Factura factura1;
    private Factura factura2;
    private Venta venta1; // Venta asociada a factura1 y factura2
    private Venta venta2; // Otra venta para casos de prueba

    @BeforeEach
    void setUp() {
        venta1 = new Venta();
        venta1.setId(101L);
        venta1.setMontoTotal(150.0); // Necesario para emitirFactura

        venta2 = new Venta();
        venta2.setId(102L);
        venta2.setMontoTotal(200.0); // Necesario para emitirFactura

        factura1 = new Factura();
        factura1.setCodFactura(1L); // Usamos codFactura
        factura1.setFechaEmision(LocalDateTime.of(2024, 5, 10, 10, 0));
        factura1.setTotalFactura(150.0); // Usamos totalFactura
        factura1.setVenta(venta1);

        factura2 = new Factura();
        factura2.setCodFactura(2L); // Usamos codFactura
        factura2.setFechaEmision(LocalDateTime.of(2024, 5, 15, 11, 30));
        factura2.setTotalFactura(250.0); // Usamos totalFactura
        factura2.setVenta(venta1); // Asociada a la misma venta para un escenario
    }

    @Test
    void getAllFacturas_shouldReturnListOfFacturas() throws Exception {
        // Arrange
        when(facturaService.findAll()).thenReturn(Arrays.asList(factura1, factura2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/facturas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codFactura").value(factura1.getCodFactura())) // Corregido a codFactura
                .andExpect(jsonPath("$[1].codFactura").value(factura2.getCodFactura())); // Corregido a codFactura

        verify(facturaService, times(1)).findAll();
    }

    @Test
    void getFacturaById_shouldReturnFacturaWhenFound() throws Exception {
        // Arrange
        when(facturaService.findById(1L)).thenReturn(Optional.of(factura1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/facturas/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codFactura").value(factura1.getCodFactura())) // Corregido a codFactura
                .andExpect(jsonPath("$.totalFactura").value(factura1.getTotalFactura())); // Corregido a totalFactura

        verify(facturaService, times(1)).findById(1L);
    }

    @Test
    void getFacturaById_shouldReturnNotFoundWhenFacturaDoesNotExist() throws Exception {
        // Arrange
        when(facturaService.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/facturas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).findById(99L);
    }

    @Test
    void getFacturaByVentaId_shouldReturnFacturaWhenFound() throws Exception {
        // Arrange
        when(facturaService.findByVentaId(venta1.getId())).thenReturn(Optional.of(factura1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/facturas/venta/{ventaId}", venta1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codFactura").value(factura1.getCodFactura())) // Corregido a codFactura
                .andExpect(jsonPath("$.venta.id").value(venta1.getId()));

        verify(facturaService, times(1)).findByVentaId(venta1.getId());
    }

    @Test
    void getFacturaByVentaId_shouldReturnNotFoundWhenFacturaForVentaDoesNotExist() throws Exception {
        // Arrange
        when(facturaService.findByVentaId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/facturas/venta/{ventaId}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).findByVentaId(999L);
    }

    @Test
    void emitirFactura_shouldEmitFacturaAndReturnCreatedStatus() throws Exception {
        // Arrange
        Long ventaIdToEmit = venta1.getId();
        Factura emittedFactura = new Factura();
        emittedFactura.setCodFactura(3L); // Usamos codFactura
        emittedFactura.setFechaEmision(LocalDateTime.now());
        emittedFactura.setTotalFactura(venta1.getMontoTotal()); // Usamos totalFactura
        emittedFactura.setVenta(venta1);

        when(facturaService.emitirFactura(ventaIdToEmit)).thenReturn(emittedFactura);

        // Act & Assert
        mockMvc.perform(post("/api/v1/facturas/emitir/{ventaId}", ventaIdToEmit)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codFactura").value(emittedFactura.getCodFactura())) // Corregido a codFactura
                .andExpect(jsonPath("$.totalFactura").value(emittedFactura.getTotalFactura())) // Corregido a totalFactura
                .andExpect(jsonPath("$.venta.id").value(venta1.getId()));

        verify(facturaService, times(1)).emitirFactura(ventaIdToEmit);
    }

    @Test
    void emitirFactura_shouldReturnNotFoundWhenVentaNotFoundForEmission() throws Exception {
        // Arrange
        Long ventaIdNotFound = 999L;
        when(facturaService.emitirFactura(ventaIdNotFound)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/facturas/emitir/{ventaId}", ventaIdNotFound)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).emitirFactura(ventaIdNotFound);
    }

    @Test
    void updateFactura_shouldUpdateFacturaAndReturnOkStatus() throws Exception {
        // Arrange
        Long facturaIdToUpdate = 1L;
        Factura updatedDetails = new Factura();
        updatedDetails.setFechaEmision(LocalDateTime.of(2024, 6, 1, 12, 0));
        updatedDetails.setTotalFactura(500.0); // Usamos totalFactura
        updatedDetails.setVenta(venta2); // Simula que la venta asociada también puede cambiar

        // Objeto que el servicio mockeado devolverá
        Factura returnedUpdatedFactura = new Factura();
        returnedUpdatedFactura.setCodFactura(facturaIdToUpdate); // Usamos codFactura
        returnedUpdatedFactura.setFechaEmision(updatedDetails.getFechaEmision());
        returnedUpdatedFactura.setTotalFactura(updatedDetails.getTotalFactura()); // Usamos totalFactura
        returnedUpdatedFactura.setVenta(venta2); // Debe coincidir con lo que se pasa al servicio

        when(facturaService.update(eq(facturaIdToUpdate), any(Factura.class))).thenReturn(Optional.of(returnedUpdatedFactura));

        
        mockMvc.perform(put("/api/v1/facturas/{id}", facturaIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codFactura").value(facturaIdToUpdate)) // Corregido a codFactura
                .andExpect(jsonPath("$.totalFactura").value(updatedDetails.getTotalFactura())) // Corregido a totalFactura
                .andExpect(jsonPath("$.venta.id").value(venta2.getId())); // Verifica que la venta también se actualizó

        verify(facturaService, times(1)).update(eq(facturaIdToUpdate), any(Factura.class));
    }

    @Test
    void updateFactura_shouldReturnNotFoundWhenFacturaDoesNotExist() throws Exception {
        // Arrange
        Factura updatedDetails = new Factura(); 
        updatedDetails.setFechaEmision(LocalDateTime.now());
        updatedDetails.setTotalFactura(100.0);
        updatedDetails.setVenta(venta1); 

        when(facturaService.update(eq(99L), any(Factura.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/facturas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).update(eq(99L), any(Factura.class));
    }

    @Test
    void deleteFactura_shouldReturnNoContentStatus() throws Exception {
        // Arrange
        when(facturaService.deleteById(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/facturas/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(facturaService, times(1)).deleteById(1L);
    }

    @Test
    void deleteFactura_shouldReturnNotFoundWhenFacturaDoesNotExist() throws Exception {
        // Arrange
        when(facturaService.deleteById(99L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/facturas/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).deleteById(99L);
    }
}