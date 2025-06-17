package com.venta.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.model.Cliente;
import com.venta.ventas.model.Venta;
import com.venta.ventas.service.VentaService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaService ventaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Venta venta1;
    private Venta venta2;
    private Cliente cliente1;

    @BeforeEach
    void setUp() {
        cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNombreCompleto("Juan Pérez");
        cliente1.setRut("11.111.111-1");

        venta1 = new Venta();
        venta1.setId(1L);
        venta1.setFechaVenta(LocalDateTime.of(2023, 1, 15, 10, 0));
        venta1.setMontoTotal(150.0);
        venta1.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA);
        venta1.setEstado(EstadoVenta.COMPLETADA);
        venta1.setCliente(cliente1);

        venta2 = new Venta();
        venta2.setId(2L);
        venta2.setFechaVenta(LocalDateTime.of(2023, 2, 20, 14, 30));
        venta2.setMontoTotal(200.0);
        venta2.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);
        venta2.setEstado(EstadoVenta.PENDIENTE);
        venta2.setCliente(cliente1);
    }

    @Test
    void getAllVentas_shouldReturnListOfVentas() throws Exception {
        when(ventaService.findAll()).thenReturn(Arrays.asList(venta1, venta2));

        mockMvc.perform(get("/api/v1/ventas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(venta1.getId()))
                .andExpect(jsonPath("$[1].id").value(venta2.getId()));

        verify(ventaService, times(1)).findAll();
    }

    @Test
    void getVentaById_shouldReturnVentaWhenFound() throws Exception {
        when(ventaService.findById(1L)).thenReturn(Optional.of(venta1));

        mockMvc.perform(get("/api/v1/ventas/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(venta1.getId()))
                .andExpect(jsonPath("$.montoTotal").value(venta1.getMontoTotal()));

        verify(ventaService, times(1)).findById(1L);
    }

    @Test
    void getVentaById_shouldReturnNotFoundWhenVentaDoesNotExist() throws Exception {
        when(ventaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/ventas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(ventaService, times(1)).findById(99L);
    }

    @Test
    void createVenta_shouldCreateVentaAndReturnCreatedStatus() throws Exception {
        // Arrange
        Venta newVenta = new Venta();
        newVenta.setFechaVenta(LocalDateTime.now());
        newVenta.setMontoTotal(100.0);
        newVenta.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA);
        newVenta.setEstado(EstadoVenta.PENDIENTE);

        Venta savedVenta = new Venta();
        savedVenta.setId(3L); // Asegura que la venta guardada tiene un ID
        savedVenta.setFechaVenta(newVenta.getFechaVenta());
        savedVenta.setMontoTotal(newVenta.getMontoTotal());
        savedVenta.setMedioEnvio(newVenta.getMedioEnvio());
        savedVenta.setEstado(newVenta.getEstado());
        savedVenta.setCliente(cliente1); // El servicio mockeado "devuelve" la venta con el cliente

        when(ventaService.save(any(Venta.class), eq(cliente1.getId()))).thenReturn(savedVenta);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ventas")
                .param("clienteId", String.valueOf(cliente1.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVenta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedVenta.getId())); // Solo verificamos el ID de la venta creada

        verify(ventaService, times(1)).save(any(Venta.class), eq(cliente1.getId()));
    }

    @Test
    void createVenta_shouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        // Arrange
        Venta newVenta = new Venta();
        newVenta.setFechaVenta(LocalDateTime.now());
        newVenta.setMontoTotal(100.0);
        newVenta.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA);
        newVenta.setEstado(EstadoVenta.PENDIENTE);

        when(ventaService.save(any(Venta.class), anyLong())).thenThrow(new RuntimeException("Cliente no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/ventas")
                .param("clienteId", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVenta)))
                .andExpect(status().isBadRequest());

        verify(ventaService, times(1)).save(any(Venta.class), eq(999L));
    }


    @Test
    void updateVenta_shouldUpdateVentaAndReturnOkStatus() throws Exception {
        // Arrange
        Long ventaIdToUpdate = 1L; // ID de la venta a actualizar
        Venta updatedDetails = new Venta();
        updatedDetails.setFechaVenta(LocalDateTime.of(2023, 3, 1, 9, 0));
        updatedDetails.setMontoTotal(300.0);
        updatedDetails.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);
        updatedDetails.setEstado(EstadoVenta.COMPLETADA);
        updatedDetails.setCliente(cliente1); // Mantener la referencia al cliente original para el mock de retorno

        // *** CORRECCIÓN AQUÍ: Asegurarse de que el objeto "actualizado" tenga el ID.
        // El ID viene del PathVariable, no del RequestBody para un PUT.
        updatedDetails.setId(ventaIdToUpdate);

        when(ventaService.update(eq(ventaIdToUpdate), any(Venta.class))).thenReturn(Optional.of(updatedDetails));

        // Act & Assert
        mockMvc.perform(put("/api/v1/ventas/{id}", ventaIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                // *** CORRECCIÓN AQUÍ: Ahora podemos simplemente verificar contra el ID conocido del path.
                .andExpect(jsonPath("$.id").value(ventaIdToUpdate))
                .andExpect(jsonPath("$.montoTotal").value(updatedDetails.getMontoTotal()));

        verify(ventaService, times(1)).update(eq(ventaIdToUpdate), any(Venta.class));
    }

    @Test
    void updateVenta_shouldReturnNotFoundWhenVentaDoesNotExist() throws Exception {
        // Arrange
        Venta updatedDetails = new Venta(); // No importa el contenido para este caso
        when(ventaService.update(eq(99L), any(Venta.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/ventas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(ventaService, times(1)).update(eq(99L), any(Venta.class));
    }

    @Test
    void deleteVenta_shouldReturnNoContentStatus() throws Exception {
        when(ventaService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/ventas/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(ventaService, times(1)).deleteById(1L);
    }

    @Test
    void deleteVenta_shouldReturnNotFoundWhenVentaDoesNotExist() throws Exception {
        when(ventaService.deleteById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/ventas/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(ventaService, times(1)).deleteById(99L);
    }

    @Test
    void aplicarDescuento_shouldApplyDiscountAndReturnOkStatus() throws Exception {
        Venta discountedVenta = new Venta();
        discountedVenta.setId(1L);
        discountedVenta.setMontoTotal(135.0);
        discountedVenta.setEstado(EstadoVenta.PENDIENTE);
        discountedVenta.setCliente(cliente1);

        when(ventaService.aplicarDescuento(eq(1L), eq(10.0))).thenReturn(Optional.of(discountedVenta));

        mockMvc.perform(put("/api/v1/ventas/{id}/descuento", 1L)
                .param("porcentajeDescuento", "10.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoTotal").value(135.0));

        verify(ventaService, times(1)).aplicarDescuento(eq(1L), eq(10.0));
    }

    @Test
    void aplicarDescuento_shouldReturnNotFoundWhenVentaNotFound() throws Exception {
        when(ventaService.aplicarDescuento(eq(99L), anyDouble())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/ventas/{id}/descuento", 99L)
                .param("porcentajeDescuento", "10.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(ventaService, times(1)).aplicarDescuento(eq(99L), eq(10.0));
    }

    @Test
    void cancelarVenta_shouldCancelVentaAndReturnOkStatus() throws Exception {
        Venta cancelledVenta = new Venta();
        cancelledVenta.setId(1L);
        cancelledVenta.setMontoTotal(150.0);
        cancelledVenta.setEstado(EstadoVenta.CANCELADA);
        cancelledVenta.setCliente(cliente1);

        when(ventaService.cancelarVenta(1L)).thenReturn(Optional.of(cancelledVenta));

        mockMvc.perform(put("/api/v1/ventas/{id}/cancelar", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoVenta.CANCELADA.name()));

        verify(ventaService, times(1)).cancelarVenta(1L);
    }

    @Test
    void cancelarVenta_shouldReturnNotFoundWhenVentaNotFound() throws Exception {
        when(ventaService.cancelarVenta(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/ventas/{id}/cancelar", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(ventaService, times(1)).cancelarVenta(99L);
    }

    @Test
    void generarFactura_shouldReturnFacturaStringAndOkStatus() throws Exception {
        String mockFactura = "Factura de prueba para Venta ID: 1";
        when(ventaService.generarFactura(1L)).thenReturn(mockFactura);

        mockMvc.perform(get("/api/v1/ventas/{id}/factura", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(mockFactura));

        verify(ventaService, times(1)).generarFactura(1L);
    }

    @Test
    void generarFactura_shouldReturnNotFoundWhenVentaNotFound() throws Exception {
        when(ventaService.generarFactura(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/ventas/{id}/factura", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(ventaService, times(1)).generarFactura(99L);
    }
}