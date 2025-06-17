package com.venta.ventas.service;

import com.venta.ventas.model.Producto;
import com.venta.ventas.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Laptop");
        producto1.setPrecio(1200.00);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Mouse");
        producto2.setPrecio(25.00);
    }

    @Test
    void findAll_shouldReturnAllProductos() {
        // Arrange
        List<Producto> expectedProductos = Arrays.asList(producto1, producto2);
        when(productoRepository.findAll()).thenReturn(expectedProductos);

        // Act
        List<Producto> actualProductos = productoService.findAll();

        // Assert
        assertNotNull(actualProductos);
        assertEquals(2, actualProductos.size());
        assertEquals(expectedProductos, actualProductos);
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnProductoWhenFound() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        // Act
        Optional<Producto> actualProducto = productoService.findById(1L);

        // Assert
        assertTrue(actualProducto.isPresent());
        assertEquals(producto1, actualProducto.get());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> actualProducto = productoService.findById(99L);

        // Assert
        assertFalse(actualProducto.isPresent());
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    void save_shouldReturnSavedProducto() {
        // Arrange
        Producto newProducto = new Producto();
        newProducto.setNombre("Teclado");
        newProducto.setPrecio(75.00);

        Producto savedProducto = new Producto();
        savedProducto.setId(3L); // Simulate ID being set by DB
        savedProducto.setNombre("Teclado");
        savedProducto.setPrecio(75.00);

        when(productoRepository.save(any(Producto.class))).thenReturn(savedProducto);

        // Act
        Producto result = productoService.save(newProducto);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Teclado", result.getNombre());
        assertEquals(75.00, result.getPrecio());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void update_shouldUpdateProductoWhenFound() {
        // Arrange
        Producto updatedDetails = new Producto();
        updatedDetails.setNombre("Laptop Pro");
        updatedDetails.setPrecio(1500.00);

        Producto existingProducto = new Producto();
        existingProducto.setId(1L);
        existingProducto.setNombre("Laptop");
        existingProducto.setPrecio(1200.00);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existingProducto));
        when(productoRepository.save(any(Producto.class))).thenReturn(updatedDetails); // Simulate saving updated entity

        // Act
        Optional<Producto> actualProducto = productoService.update(1L, updatedDetails);

        // Assert
        assertTrue(actualProducto.isPresent());
        assertEquals("Laptop Pro", actualProducto.get().getNombre());
        assertEquals(1500.00, actualProducto.get().getPrecio());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(existingProducto); // Verify the existingProducto was saved
    }

    @Test
    void update_shouldReturnEmptyWhenProductoNotFound() {
        // Arrange
        Producto updatedDetails = new Producto();
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> actualProducto = productoService.update(99L, updatedDetails);

        // Assert
        assertFalse(actualProducto.isPresent());
        verify(productoRepository, times(1)).findById(99L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void deleteById_shouldReturnTrueWhenProductoExists() {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        boolean result = productoService.deleteById(1L);

        // Assert
        assertTrue(result);
        verify(productoRepository, times(1)).existsById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldReturnFalseWhenProductoDoesNotExist() {
        // Arrange
        when(productoRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = productoService.deleteById(99L);

        // Assert
        assertFalse(result);
        verify(productoRepository, times(1)).existsById(99L);
        verify(productoRepository, never()).deleteById(anyLong());
    }
}