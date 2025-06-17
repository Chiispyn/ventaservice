package com.venta.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.model.Cliente;
import com.venta.ventas.model.DetalleVenta; 
import com.venta.ventas.model.Producto;    
import com.venta.ventas.model.Venta;
import com.venta.ventas.service.VentaService;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize; 
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.ArgumentMatchers.*;
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

  
    private Venta testVenta;
    private Cliente testCliente;
    private Producto testProducto;
    private DetalleVenta testDetalleVenta;

    @BeforeEach
    void setUp() {

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        testCliente = new Cliente(1L, "12345678-9", "Juan Perez", "juan@example.com", "Calle Falsa 123", "987654321", null);
        testProducto = new Producto(101L, "Laptop", 1200.0);
        testDetalleVenta = new DetalleVenta(null, null, testProducto, 1, testProducto.getPrecio());
        testVenta = new Venta(1L, LocalDateTime.now(), 1200.0, MedioEnvio.DESPACHO_A_DOMICILIO, EstadoVenta.PENDIENTE, Arrays.asList(testDetalleVenta), testCliente);
        testDetalleVenta.setVenta(testVenta); 

    }


    private Venta crearVentaParaSolicitud(Long id, LocalDateTime fecha, Double monto, MedioEnvio medio, EstadoVenta estado, List<DetalleVenta> detalles, Cliente cliente) {
        Venta venta = new Venta(id, fecha, monto, medio, estado, detalles, cliente);
        if (detalles != null) {
            detalles.forEach(d -> d.setVenta(venta));
        }
        return venta;
    }



    @Test
    @DisplayName("POST /api/v1/ventas - Debería crear una nueva venta")
    public void testCreateVenta() throws Exception {
    Venta newVenta = crearVentaParaSolicitud(null, LocalDateTime.now(), 5000.0, MedioEnvio.DESPACHO_A_DOMICILIO, EstadoVenta.PENDIENTE, Arrays.asList(testDetalleVenta), testCliente);

   
    when(ventaService.save(any(Venta.class), eq(newVenta.getCliente().getId())))
            .thenReturn(newVenta); 
    mockMvc.perform(post("/api/v1/ventas")
                    .param("clienteId", newVenta.getCliente().getId().toString()) 
                    .contentType(MediaType.APPLICATION_JSON) 
                    .content(objectMapper.writeValueAsString(newVenta))) 
            .andExpect(status().isCreated()) 
            .andExpect(jsonPath("$.medioEnvio").value("DESPACHO_A_DOMICILIO"))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andDo(print()); 
    verify(ventaService, times(1)).save(any(Venta.class), eq(newVenta.getCliente().getId()));
    }

    @Test
    @DisplayName("GET /api/v1/ventas/{id} - Debería retornar una venta por su ID")
    public void testGetVentaById() throws Exception {
        when(ventaService.findById(testVenta.getId())).thenReturn(Optional.of(testVenta));

        mockMvc.perform(get("/api/v1/ventas/{id}", testVenta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testVenta.getId().intValue()))
                .andExpect(jsonPath("$.estado").value(EstadoVenta.PENDIENTE.toString()))
                .andDo(print()); 

        verify(ventaService, times(1)).findById(testVenta.getId());
    }

    @Test
    @DisplayName("GET /api/v1/ventas - Debería retornar todas las ventas")
    public void testGetAllVentas() throws Exception {
        List<Venta> ventas = Arrays.asList(testVenta); 
        when(ventaService.findAll()).thenReturn(ventas);

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testVenta.getId().intValue()))
                .andDo(print()); 

        verify(ventaService, times(1)).findAll();
    }

    @Test
    @DisplayName("DELETE /api/v1/ventas/{id} - Debería eliminar una venta exitosamente")
    public void testDeleteVenta() throws Exception {
        when(ventaService.deleteById(testVenta.getId())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/ventas/{id}", testVenta.getId()))
                .andExpect(status().isNoContent()); 

        verify(ventaService, times(1)).deleteById(testVenta.getId());
    }

    @Test
    @DisplayName("PUT /api/v1/ventas/{id}/descuento - Debería aplicar descuento a una venta")
    public void testAplicarDescuento() throws Exception {

        Venta discountedVenta = crearVentaParaSolicitud(testVenta.getId(), testVenta.getFechaVenta(), 4000.0, testVenta.getMedioEnvio(), testVenta.getEstado(), testVenta.getDetalles(), testVenta.getCliente());

        when(ventaService.aplicarDescuento(eq(testVenta.getId()), eq(20.0))).thenReturn(Optional.of(discountedVenta));

        mockMvc.perform(put("/api/v1/ventas/{id}/descuento", testVenta.getId())
                        .param("porcentajeDescuento", "20.0")) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoTotal").value(4000.0))
                .andDo(print()); 

        verify(ventaService, times(1)).aplicarDescuento(eq(testVenta.getId()), eq(20.0));
    }

    @Test
    @DisplayName("PUT /api/v1/ventas/{id}/cancelar - Debería cancelar una venta")
    public void testCancelarVenta() throws Exception {

        Venta cancelledVenta = crearVentaParaSolicitud(testVenta.getId(), testVenta.getFechaVenta(), testVenta.getMontoTotal(), testVenta.getMedioEnvio(), EstadoVenta.CANCELADA, testVenta.getDetalles(), testVenta.getCliente());
        when(ventaService.cancelarVenta(eq(testVenta.getId()))).thenReturn(Optional.of(cancelledVenta));
        mockMvc.perform(put("/api/v1/ventas/{id}/cancelar", testVenta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(EstadoVenta.CANCELADA.toString()))
                .andDo(print()); 
        verify(ventaService, times(1)).cancelarVenta(eq(testVenta.getId()));
    }



    @Test
    @DisplayName("GET /api/v1/ventas/{id}/factura - Debería generar y retornar una factura en formato String")
    public void testGenerarFactura() throws Exception {
        String mockFacturaString = "Factura para la Venta ID: 1\n";
        when(ventaService.generarFactura(eq(testVenta.getId()))).thenReturn(mockFacturaString);

        mockMvc.perform(get("/api/v1/ventas/{id}/factura", testVenta.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(mockFacturaString))
                .andDo(print()); 

        verify(ventaService, times(1)).generarFactura(eq(testVenta.getId()));
    }
}