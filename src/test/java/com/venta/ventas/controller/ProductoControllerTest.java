package com.venta.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venta.ventas.model.Producto;
import com.venta.ventas.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class) // Anotación para probar solo la capa web del controlador
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para simular peticiones HTTP

    @MockBean // Crea un mock para el ProductoService y lo inyecta en el controlador
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Jabón Artesanal de Romero");
        
        producto1.setPrecio(7.50);
        

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Champú Sólido de Lavanda");
        
        producto2.setPrecio(12.00);
        
    }

    @Test
    void getAllProductos_shouldReturnListOfProductos() throws Exception {
        // Arrange
        when(productoService.findAll()).thenReturn(Arrays.asList(producto1, producto2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera un 200 OK
                .andExpect(jsonPath("$.length()").value(2)) // Espera 2 elementos en la lista JSON
                .andExpect(jsonPath("$[0].id").value(producto1.getId()))
                .andExpect(jsonPath("$[0].nombre").value(producto1.getNombre()))
                .andExpect(jsonPath("$[1].id").value(producto2.getId()))
                .andExpect(jsonPath("$[1].nombre").value(producto2.getNombre()));

        verify(productoService, times(1)).findAll(); // Verifica que el servicio fue llamado una vez
    }

    @Test
    void getProductoById_shouldReturnProductoWhenFound() throws Exception {
        // Arrange
        when(productoService.findById(1L)).thenReturn(Optional.of(producto1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/productos/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera un 200 OK
                .andExpect(jsonPath("$.id").value(producto1.getId()))
                .andExpect(jsonPath("$.nombre").value(producto1.getNombre()))
                .andExpect(jsonPath("$.precio").value(producto1.getPrecio()));

        verify(productoService, times(1)).findById(1L);
    }

    @Test
    void getProductoById_shouldReturnNotFoundWhenProductoDoesNotExist() throws Exception {
        // Arrange
        when(productoService.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/productos/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera un 404 Not Found
        verify(productoService, times(1)).findById(99L);
    }

    @Test
    void createProducto_shouldCreateProductoAndReturnCreatedStatus() throws Exception {
        // Arrange
        Producto newProducto = new Producto();
        newProducto.setNombre("Crema Hidratante Bio");    
        newProducto.setPrecio(25.00);
        

        Producto savedProducto = new Producto();
        savedProducto.setId(3L); // Simula el ID generado por la BD
        savedProducto.setNombre(newProducto.getNombre());       
        savedProducto.setPrecio(newProducto.getPrecio());
        

        when(productoService.save(any(Producto.class))).thenReturn(savedProducto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProducto))) // Cuerpo de la solicitud
                .andExpect(status().isCreated()) // Espera un 201 Created
                .andExpect(jsonPath("$.id").value(savedProducto.getId()))
                .andExpect(jsonPath("$.nombre").value(newProducto.getNombre()))
                .andExpect(jsonPath("$.precio").value(newProducto.getPrecio()));

        verify(productoService, times(1)).save(any(Producto.class));
    }

    @Test
    void updateProducto_shouldUpdateProductoAndReturnOkStatus() throws Exception {
        // Arrange
        Long productIdToUpdate = 1L;
        Producto updatedDetails = new Producto();
        updatedDetails.setNombre("Jabón Artesanal de Lavanda (Actualizado)");
        
        updatedDetails.setPrecio(8.00);
        

        // Asegúrate de que el objeto que mockea el retorno del servicio tenga el ID
        Producto returnedUpdatedProducto = new Producto();
        returnedUpdatedProducto.setId(productIdToUpdate);
        returnedUpdatedProducto.setNombre(updatedDetails.getNombre());    
        returnedUpdatedProducto.setPrecio(updatedDetails.getPrecio());
        

        when(productoService.update(eq(productIdToUpdate), any(Producto.class))).thenReturn(Optional.of(returnedUpdatedProducto));

        // Act & Assert
        mockMvc.perform(put("/api/v1/productos/{id}", productIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails))) // Cuerpo de la solicitud
                .andExpect(status().isOk()) // Espera un 200 OK
                .andExpect(jsonPath("$.id").value(productIdToUpdate))
                .andExpect(jsonPath("$.nombre").value(updatedDetails.getNombre()))
                .andExpect(jsonPath("$.precio").value(updatedDetails.getPrecio()));

        verify(productoService, times(1)).update(eq(productIdToUpdate), any(Producto.class));
    }

    @Test
    void updateProducto_shouldReturnNotFoundWhenProductoDoesNotExist() throws Exception {
        // Arrange
        Producto updatedDetails = new Producto(); // El contenido no es relevante para este caso de prueba
        when(productoService.update(eq(99L), any(Producto.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/productos/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound()); // Espera un 404 Not Found

        verify(productoService, times(1)).update(eq(99L), any(Producto.class));
    }

    @Test
    void deleteProducto_shouldReturnNoContentStatus() throws Exception {
        // Arrange
        when(productoService.deleteById(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/productos/{id}", 1L))
                .andExpect(status().isNoContent()); // Espera un 204 No Content

        verify(productoService, times(1)).deleteById(1L);
    }

    @Test
    void deleteProducto_shouldReturnNotFoundWhenProductoDoesNotExist() throws Exception {
        // Arrange
        when(productoService.deleteById(99L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/productos/{id}", 99L))
                .andExpect(status().isNotFound()); // Espera un 404 Not Found

        verify(productoService, times(1)).deleteById(99L);
    }
}