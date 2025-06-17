package com.venta.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venta.ventas.model.Producto;
import com.venta.ventas.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Monitor Gaming", 350.0);
    }

    @Test
    @DisplayName("GET /api/v1/productos - Debería retornar todos los productos")
    void getAllProductos_shouldReturnAllProductos() throws Exception {
        when(productoService.findAll()).thenReturn(Arrays.asList(producto, new Producto(2L, "Teclado", 100.0)));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is(producto.getNombre())));

        verify(productoService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} - Debería retornar un producto por su ID")
    void getProductoById_shouldReturnProductoById() throws Exception {
        when(productoService.findById(producto.getId())).thenReturn(Optional.of(producto));

        mockMvc.perform(get("/api/v1/productos/{id}", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(producto.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is(producto.getNombre())));

        verify(productoService, times(1)).findById(producto.getId());
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} - Debería retornar 404 si el producto no se encuentra")
    void getProductoById_shouldReturnNotFound() throws Exception {
        when(productoService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/productos/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).findById(99L);
    }

    @Test
    @DisplayName("POST /api/v1/productos - Debería crear un nuevo producto")
    void createProducto_shouldCreateNewProducto() throws Exception {
        Producto newProducto = new Producto(null, "Mouse Inalámbrico", 50.0);
        Producto savedProducto = new Producto(2L, "Mouse Inalámbrico", 50.0);

        when(productoService.save(any(Producto.class))).thenReturn(savedProducto);

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(savedProducto.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is(savedProducto.getNombre())));

        verify(productoService, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/v1/productos/{id} - Debería actualizar un producto existente")
    void updateProducto_shouldUpdateExistingProducto() throws Exception {
        Producto updatedDetails = new Producto(null, "Monitor Curvo", 400.0);
        Producto updatedProductoResult = new Producto(producto.getId(), "Monitor Curvo", 400.0);

        when(productoService.update(eq(producto.getId()), any(Producto.class))).thenReturn(Optional.of(updatedProductoResult));

        mockMvc.perform(put("/api/v1/productos/{id}", producto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(producto.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Monitor Curvo")));

        verify(productoService, times(1)).update(eq(producto.getId()), any(Producto.class));
    }

    @Test
    @DisplayName("PUT /api/v1/productos/{id} - Debería retornar 404 si el producto a actualizar no se encuentra")
    void updateProducto_shouldReturnNotFound() throws Exception {
        when(productoService.update(anyLong(), any(Producto.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/productos/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Producto())))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).update(eq(99L), any(Producto.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/productos/{id} - Debería eliminar un producto exitosamente")
    void deleteProducto_shouldDeleteProducto() throws Exception {
        when(productoService.deleteById(producto.getId())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/productos/{id}", producto.getId()))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).deleteById(producto.getId());
    }

    @Test
    @DisplayName("DELETE /api/v1/productos/{id} - Debería retornar 404 si el producto a eliminar no se encuentra")
    void deleteProducto_shouldReturnNotFound() throws Exception {
        when(productoService.deleteById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/v1/productos/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).deleteById(99L);
    }
}