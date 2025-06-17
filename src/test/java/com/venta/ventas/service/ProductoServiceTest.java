package com.venta.ventas.service;

import com.venta.ventas.model.Producto;
import com.venta.ventas.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Teclado Mecánico", 75.0);
    }

    @Test
    @DisplayName("findAll - Debe retornar todos los productos existentes")
    void findAll_shouldReturnAllProductos() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto, new Producto(2L, "Monitor", 300.0)));

        List<Producto> productos = productoService.findAll();

        assertNotNull(productos);
        assertFalse(productos.isEmpty());
        assertEquals(2, productos.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById - Debe retornar un producto específico por su ID")
    void findById_shouldReturnProductoById() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Optional<Producto> foundProducto = productoService.findById(1L);

        assertTrue(foundProducto.isPresent());
        assertEquals(producto.getId(), foundProducto.get().getId());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe retornar un Optional vacío si el producto no se encuentra por ID")
    void findById_shouldReturnEmptyIfProductoNotFound() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Producto> foundProducto = productoService.findById(99L);

        assertTrue(foundProducto.isEmpty());
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("save - Debe guardar un nuevo producto")
    void save_shouldSaveNewProducto() {
        Producto newProducto = new Producto(null, "Ratón Ergonómico", 30.0);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto savedProducto = invocation.getArgument(0);
            savedProducto.setId(2L); // Simular asignación de ID
            return savedProducto;
        });

        Producto savedProducto = productoService.save(newProducto);

        assertNotNull(savedProducto);
        assertEquals(2L, savedProducto.getId());
        assertEquals("Ratón Ergonómico", savedProducto.getNombre());
        verify(productoRepository, times(1)).save(newProducto);
    }

    @Test
    @DisplayName("update - Debe actualizar un producto existente")
    void update_shouldUpdateExistingProducto() {
        Producto updatedDetails = new Producto(null, "Teclado Inalámbrico", 80.0);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Producto> result = productoService.update(1L, updatedDetails);

        assertTrue(result.isPresent());
        assertEquals("Teclado Inalámbrico", result.get().getNombre());
        assertEquals(80.0, result.get().getPrecio());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("update - Debe retornar un Optional vacío si el producto a actualizar no se encuentra")
    void update_shouldReturnEmptyIfProductoNotFound() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Producto> result = productoService.update(99L, new Producto());

        assertTrue(result.isEmpty());
        verify(productoRepository, times(1)).findById(99L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("deleteById - Debe eliminar un producto exitosamente por su ID")
    void deleteById_shouldDeleteProductoById() {
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        boolean deleted = productoService.deleteById(1L);

        assertTrue(deleted);
        verify(productoRepository, times(1)).existsById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - Debe retornar falso si el producto a eliminar no existe")
    void deleteById_shouldReturnFalseIfProductoNotFound() {
        when(productoRepository.existsById(anyLong())).thenReturn(false);

        boolean deleted = productoService.deleteById(99L);

        assertFalse(deleted);
        verify(productoRepository, times(1)).existsById(99L);
        verify(productoRepository, never()).deleteById(anyLong());
    }
}