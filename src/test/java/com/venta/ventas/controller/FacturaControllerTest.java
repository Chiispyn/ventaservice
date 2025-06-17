package com.venta.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.model.Factura;
import com.venta.ventas.model.Venta;
import com.venta.ventas.service.FacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
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

    private Factura factura;
    private Venta venta;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Para evitar problemas de formato de fecha

        venta = new Venta(1L, LocalDateTime.of(2023, 1, 15, 10, 0), 200.0, MedioEnvio.DESPACHO_A_DOMICILIO, EstadoVenta.COMPLETADA, null, null);
        factura = new Factura(1L, LocalDateTime.of(2023, 1, 15, 10, 30), 200.0, venta);
    }

    @Test
    @DisplayName("GET /api/v1/facturas - Debería retornar todas las facturas")
    void getAllFacturas_shouldReturnAllFacturas() throws Exception {
        when(facturaService.findAll()).thenReturn(Arrays.asList(factura, new Factura(2L, LocalDateTime.now(), 150.0, new Venta())));

        mockMvc.perform(get("/api/v1/facturas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codFactura", is(factura.getCodFactura().intValue())));

        verify(facturaService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/facturas/{id} - Debería retornar una factura por su ID")
    void getFacturaById_shouldReturnFacturaById() throws Exception {
        when(facturaService.findById(factura.getCodFactura())).thenReturn(Optional.of(factura));

        mockMvc.perform(get("/api/v1/facturas/{id}", factura.getCodFactura()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codFactura", is(factura.getCodFactura().intValue())))
                .andExpect(jsonPath("$.totalFactura", is(factura.getTotalFactura())));

        verify(facturaService, times(1)).findById(factura.getCodFactura());
    }

    @Test
    @DisplayName("GET /api/v1/facturas/{id} - Debería retornar 404 si la factura no se encuentra")
    void getFacturaById_shouldReturnNotFound() throws Exception {
        when(facturaService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/facturas/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).findById(99L);
    }

    @Test
    @DisplayName("GET /api/v1/facturas/venta/{ventaId} - Debería retornar una factura por el ID de la venta")
    void getFacturaByVentaId_shouldReturnFacturaByVentaId() throws Exception {
        when(facturaService.findByVentaId(venta.getId())).thenReturn(Optional.of(factura));

        mockMvc.perform(get("/api/v1/facturas/venta/{ventaId}", venta.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codFactura", is(factura.getCodFactura().intValue())))
                .andExpect(jsonPath("$.venta.id", is(venta.getId().intValue())));

        verify(facturaService, times(1)).findByVentaId(venta.getId());
    }

    @Test
    @DisplayName("GET /api/v1/facturas/venta/{ventaId} - Debería retornar 404 si no hay factura para el ID de venta")
    void getFacturaByVentaId_shouldReturnNotFoundIfNoFacturaForVentaId() throws Exception {
        when(facturaService.findByVentaId(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/facturas/venta/{ventaId}", 99L))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).findByVentaId(99L);
    }

    @Test
    @DisplayName("POST /api/v1/facturas/emitir/{ventaId} - Debería emitir y retornar una nueva factura")
    void emitirFactura_shouldEmitNewFactura() throws Exception {
        Factura emittedFactura = new Factura(2L, LocalDateTime.now(), 200.0, venta);
        when(facturaService.emitirFactura(venta.getId())).thenReturn(emittedFactura);

        mockMvc.perform(post("/api/v1/facturas/emitir/{ventaId}", venta.getId()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codFactura", is(emittedFactura.getCodFactura().intValue())))
                .andExpect(jsonPath("$.totalFactura", is(emittedFactura.getTotalFactura())));

        verify(facturaService, times(1)).emitirFactura(venta.getId());
    }

    @Test
    @DisplayName("POST /api/v1/facturas/emitir/{ventaId} - Debería retornar 404 si la venta no se encuentra al emitir factura")
    void emitirFactura_shouldReturnNotFoundIfVentaNotFound() throws Exception {
        when(facturaService.emitirFactura(anyLong())).thenReturn(null);

        mockMvc.perform(post("/api/v1/facturas/emitir/{ventaId}", 99L))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).emitirFactura(99L);
    }

    @Test
    @DisplayName("PUT /api/v1/facturas/{id} - Debería actualizar una factura existente")
    void updateFactura_shouldUpdateExistingFactura() throws Exception {
        Factura updatedDetails = new Factura(null, LocalDateTime.now().plusDays(1), 250.0, venta);
        Factura updatedFacturaResult = new Factura(factura.getCodFactura(), updatedDetails.getFechaEmision(), updatedDetails.getTotalFactura(), updatedDetails.getVenta());

        when(facturaService.update(eq(factura.getCodFactura()), any(Factura.class))).thenReturn(Optional.of(updatedFacturaResult));

        mockMvc.perform(put("/api/v1/facturas/{id}", factura.getCodFactura())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codFactura", is(factura.getCodFactura().intValue())))
                .andExpect(jsonPath("$.totalFactura", is(250.0)));

        verify(facturaService, times(1)).update(eq(factura.getCodFactura()), any(Factura.class));
    }

    @Test
    @DisplayName("PUT /api/v1/facturas/{id} - Debería retornar 404 si la factura a actualizar no se encuentra")
    void updateFactura_shouldReturnNotFound() throws Exception {
        when(facturaService.update(anyLong(), any(Factura.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/facturas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Factura())))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).update(eq(99L), any(Factura.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/facturas/{id} - Debería eliminar una factura exitosamente")
    void deleteFactura_shouldDeleteFactura() throws Exception {
        when(facturaService.deleteById(factura.getCodFactura())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/facturas/{id}", factura.getCodFactura()))
                .andExpect(status().isNoContent());

        verify(facturaService, times(1)).deleteById(factura.getCodFactura());
    }

    @Test
    @DisplayName("DELETE /api/v1/facturas/{id} - Debería retornar 404 si la factura a eliminar no se encuentra")
    void deleteFactura_shouldReturnNotFound() throws Exception {
        when(facturaService.deleteById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/v1/facturas/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(facturaService, times(1)).deleteById(99L);
    }
}