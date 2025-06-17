package com.venta.ventas.service;

import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.model.Cliente;
import com.venta.ventas.model.DetalleVenta;
import com.venta.ventas.model.Producto;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.ClienteRepository;
import com.venta.ventas.repository.ProductoRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VentaService ventaService;

    private Venta venta;
    private Cliente cliente;
    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        // Importante: Inicializamos Cliente pasando 'null' para la lista de ventas,
        // ya que no la necesitamos para este test de servicio mockeado.
        cliente = new Cliente(1L, "12345678-9", "Juan Perez", "juan@example.com", "Calle Falsa 123", "987654321", null);

        producto1 = new Producto(101L, "Laptop", 1200.0);
        producto2 = new Producto(102L, "Mouse", 25.0);

        // Los detalles de venta pueden no tener la referencia a la venta al principio
        // (la lógica de servicio la asigna)
        DetalleVenta detalle1 = new DetalleVenta(null, null, producto1, 2, producto1.getPrecio()); // 2400.0
        DetalleVenta detalle2 = new DetalleVenta(null, null, producto2, 1, producto2.getPrecio()); // 25.0

        // La venta se inicializa. El monto total se calculará en el servicio.
        venta = new Venta(1L, LocalDateTime.now(), 0.0, MedioEnvio.DESPACHO_A_DOMICILIO, EstadoVenta.PENDIENTE, Arrays.asList(detalle1, detalle2), cliente);

        // Asegurar que los detalles tengan una referencia a la venta si la lógica del servicio lo requiere
        // (aunque el servicio setVenta() sobreescribirá esto, para consistencia del objeto de prueba)
        detalle1.setVenta(venta);
        detalle2.setVenta(venta);
    }

    @Test
    @DisplayName("findAll - Debe retornar todas las ventas existentes")
    void findAll_shouldReturnAllVentas() {
        when(ventaRepository.findAll()).thenReturn(Arrays.asList(venta));

        List<Venta> ventas = ventaService.findAll();

        assertNotNull(ventas);
        assertFalse(ventas.isEmpty());
        assertEquals(1, ventas.size());
        assertEquals(venta.getId(), ventas.get(0).getId());

        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById - Debe retornar una venta específica por su ID")
    void findById_shouldReturnVentaById() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Optional<Venta> foundVenta = ventaService.findById(1L);

        assertTrue(foundVenta.isPresent());
        assertEquals(venta.getId(), foundVenta.get().getId());

        verify(ventaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - Debe retornar un Optional vacío si la venta no se encuentra por ID")
    void findById_shouldReturnEmptyIfVentaNotFound() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Venta> foundVenta = ventaService.findById(99L);

        assertTrue(foundVenta.isEmpty());

        verify(ventaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("save - Debe guardar una venta nueva, asignar cliente, establecer estado PENDIENTE y calcular monto total")
    void save_shouldSaveVentaAndCalculateTotalAndSetPendingState() {
        // La ventaToSave simula la entrada inicial del front-end o de otra parte del sistema
        // No tiene ID, montoTotal ni estado inicializados (excepto por @PrePersist en Venta)
        Venta ventaToSave = new Venta();
        ventaToSave.setFechaVenta(LocalDateTime.now());
        ventaToSave.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);
        ventaToSave.setDetalles(Arrays.asList(
            new DetalleVenta(null, null, new Producto(producto1.getId(), null, 0.0), 2, 0.0), // Solo ID es relevante para buscar Producto
            new DetalleVenta(null, null, new Producto(producto2.getId(), null, 0.0), 1, 0.0)
        ));

        // Configuramos los mocks para que encuentren el cliente y los productos
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(producto2.getId())).thenReturn(Optional.of(producto2));

        // Cuando ventaRepository.save es llamado, Mockito devolverá el mismo objeto Venta que se le pasó.
        // Esto simula que el repositorio guarda el objeto y potencialmente le asigna un ID,
        // pero aquí nos interesa más verificar los cambios hechos por el servicio.
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta argumentVenta = invocation.getArgument(0);
            argumentVenta.setId(1L); // Simula que la DB asigna un ID
            return argumentVenta;
        });

        Venta savedVenta = ventaService.save(ventaToSave, cliente.getId());

        assertNotNull(savedVenta);
        assertEquals(1L, savedVenta.getId());
        assertEquals(EstadoVenta.PENDIENTE, savedVenta.getEstado()); // Verificamos que el estado se estableció
        assertEquals(cliente, savedVenta.getCliente()); // Verificamos que el cliente se asignó

        // Verificamos que el monto total fue calculado correctamente
        double expectedMontoTotal = (2 * producto1.getPrecio()) + (1 * producto2.getPrecio());
        assertEquals(expectedMontoTotal, savedVenta.getMontoTotal());

        // Verificamos que los detalles tienen el precio unitario y la referencia a la venta
        savedVenta.getDetalles().forEach(detalle -> {
            assertEquals(savedVenta, detalle.getVenta()); // Detalles referencian la venta
            assertTrue(detalle.getPrecioUnitario() > 0); // Precio unitario fue seteado
        });


        verify(clienteRepository, times(1)).findById(cliente.getId());
        verify(productoRepository, times(1)).findById(producto1.getId());
        verify(productoRepository, times(1)).findById(producto2.getId());
        verify(ventaRepository, times(1)).save(ventaToSave);
    }

    @Test
    @DisplayName("save - Debe lanzar una RuntimeException si el cliente no se encuentra al guardar una venta")
    void save_shouldThrowExceptionIfClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ventaService.save(new Venta(), 99L);
        });

        assertTrue(thrown.getMessage().contains("Cliente con ID 99 no encontrado."));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("save - Debe lanzar una RuntimeException si un producto no se encuentra al guardar una venta")
    void save_shouldThrowExceptionIfProductoNotFound() {
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(producto1.getId())).thenReturn(Optional.empty()); // Producto no encontrado

        Venta ventaWithMissingProduct = new Venta();
        ventaWithMissingProduct.setDetalles(Arrays.asList(
            new DetalleVenta(null, null, new Producto(producto1.getId(), null, 0.0), 1, 0.0)
        ));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ventaService.save(ventaWithMissingProduct, cliente.getId());
        });

        assertTrue(thrown.getMessage().contains("Producto con ID 101 no encontrado."));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("save - Debe establecer monto total en 0.0 si no hay detalles de venta")
    void save_shouldSetZeroTotalIfNoDetails() {
        Venta ventaSinDetalles = new Venta();
        ventaSinDetalles.setFechaVenta(LocalDateTime.now());
        ventaSinDetalles.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA);
        ventaSinDetalles.setDetalles(Collections.emptyList()); // Lista de detalles vacía

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta argVenta = invocation.getArgument(0);
            argVenta.setId(2L);
            return argVenta;
        });

        Venta savedVenta = ventaService.save(ventaSinDetalles, cliente.getId());

        assertNotNull(savedVenta);
        assertEquals(0.0, savedVenta.getMontoTotal());
        assertEquals(EstadoVenta.PENDIENTE, savedVenta.getEstado());
        verify(ventaRepository, times(1)).save(savedVenta);
    }


    @Test
    @DisplayName("update - Debe actualizar una venta existente con nuevos detalles")
    void update_shouldUpdateExistingVenta() {
        Venta updatedDetails = new Venta();
        updatedDetails.setFechaVenta(LocalDateTime.now().plusDays(1));
        updatedDetails.setMontoTotal(500.0);
        updatedDetails.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA);
        updatedDetails.setEstado(EstadoVenta.COMPLETADA); // Asumiendo un estado 'COMPLETADA'

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Venta> result = ventaService.update(1L, updatedDetails);

        assertTrue(result.isPresent());
        assertEquals(updatedDetails.getFechaVenta(), result.get().getFechaVenta());
        assertEquals(updatedDetails.getMontoTotal(), result.get().getMontoTotal());
        assertEquals(updatedDetails.getMedioEnvio(), result.get().getMedioEnvio());
        assertEquals(updatedDetails.getEstado(), result.get().getEstado());

        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("update - Debe retornar un Optional vacío si la venta a actualizar no se encuentra")
    void update_shouldReturnEmptyIfVentaNotFound() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Venta> result = ventaService.update(99L, new Venta());

        assertTrue(result.isEmpty());

        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("deleteById - Debe eliminar una venta exitosamente por su ID")
    void deleteById_shouldDeleteVentaById() {
        when(ventaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ventaRepository).deleteById(1L);

        boolean deleted = ventaService.deleteById(1L);

        assertTrue(deleted);

        verify(ventaRepository, times(1)).existsById(1L);
        verify(ventaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - Debe retornar falso si la venta a eliminar no existe")
    void deleteById_shouldReturnFalseIfVentaNotFound() {
        when(ventaRepository.existsById(anyLong())).thenReturn(false);

        boolean deleted = ventaService.deleteById(99L);

        assertFalse(deleted);

        verify(ventaRepository, times(1)).existsById(99L);
        verify(ventaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("aplicarDescuento - Debe aplicar un descuento correctamente a una venta en estado PENDIENTE")
    void aplicarDescuento_shouldApplyDiscountToPendingVenta() {
        venta.setMontoTotal(100.0);
        venta.setEstado(EstadoVenta.PENDIENTE);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Venta> result = ventaService.aplicarDescuento(1L, 10.0);

        assertTrue(result.isPresent());
        assertEquals(90.0, result.get().getMontoTotal(), 0.001); // Usar delta para doubles
        assertEquals(EstadoVenta.PENDIENTE, result.get().getEstado());

        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("aplicarDescuento - No debe aplicar descuento a una venta que no está en estado PENDIENTE")
    void aplicarDescuento_shouldNotApplyDiscountIfNotPending() {
        venta.setMontoTotal(100.0);
        venta.setEstado(EstadoVenta.COMPLETADA); // No PENDIENTE
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Optional<Venta> result = ventaService.aplicarDescuento(1L, 10.0);

        assertTrue(result.isPresent());
        assertEquals(100.0, result.get().getMontoTotal(), 0.001); // Monto no cambia
        verify(ventaRepository, never()).save(any(Venta.class)); // save no debería ser llamado
    }

    @Test
    @DisplayName("aplicarDescuento - Debe retornar Optional vacío si la venta no se encuentra")
    void aplicarDescuento_shouldReturnEmptyIfVentaNotFound() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Venta> result = ventaService.aplicarDescuento(99L, 10.0);
        assertTrue(result.isEmpty());
        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("cancelarVenta - Debe cambiar el estado de una venta a CANCELADA")
    void cancelarVenta_shouldCancelVenta() {
        venta.setEstado(EstadoVenta.PENDIENTE);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Venta> result = ventaService.cancelarVenta(1L);

        assertTrue(result.isPresent());
        assertEquals(EstadoVenta.CANCELADA, result.get().getEstado());

        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("cancelarVenta - Debe retornar Optional vacío si la venta no se encuentra")
    void cancelarVenta_shouldReturnEmptyIfVentaNotFound() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Venta> result = ventaService.cancelarVenta(99L);
        assertTrue(result.isEmpty());
        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("generarFactura - Debe generar una factura en formato String con todos los detalles")
    void generarFactura_shouldGenerateCorrectInvoice() {
        // Preparamos datos específicos para la factura
        Producto p1 = new Producto(1L, "Producto A", 10.0);
        Producto p2 = new Producto(2L, "Producto B", 20.0);
        DetalleVenta d1 = new DetalleVenta(null, venta, p1, 2, 10.0);
        DetalleVenta d2 = new DetalleVenta(null, venta, p2, 1, 20.0);
        venta.setDetalles(Arrays.asList(d1, d2));
        venta.setMontoTotal(40.0); // Monto total pre-calculado para la factura
        venta.setFechaVenta(LocalDateTime.of(2023, 1, 15, 10, 30));
        venta.setCliente(cliente);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        String factura = ventaService.generarFactura(1L);

        assertNotNull(factura);
        assertTrue(factura.contains("Factura para la Venta ID: 1"));
        assertTrue(factura.contains("Fecha: 2023-01-15T10:30"));
        assertTrue(factura.contains("Cliente: Juan Perez (RUT: 12345678-9)"));
        assertTrue(factura.contains("Medio de Envío: DESPACHO_A_DOMICILIO"));
        assertTrue(factura.contains("Estado: PENDIENTE"));
        assertTrue(factura.contains("--- Detalles ---"));
        assertTrue(factura.contains("2 x Producto A @ 10.00 = 20.00"));
        assertTrue(factura.contains("1 x Producto B @ 20.00 = 20.00"));
        assertTrue(factura.contains("Monto Total: 40.00"));

        verify(ventaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("generarFactura - Debe generar una factura correcta para una venta sin detalles")
    void generarFactura_shouldGenerateInvoiceForVentaWithoutDetails() {
        venta.setDetalles(Collections.emptyList());
        venta.setMontoTotal(0.0);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        String factura = ventaService.generarFactura(1L);
        assertNotNull(factura);
        assertTrue(factura.contains("No hay productos en esta venta."));
        assertTrue(factura.contains("Monto Total: 0.00"));
    }

    @Test
    @DisplayName("generarFactura - Debe retornar null si la venta no se encuentra al intentar generar factura")
    void generarFactura_shouldReturnNullIfVentaNotFound() {
        when(ventaRepository.findById(anyLong())).thenReturn(Optional.empty());
        String factura = ventaService.generarFactura(99L);
        assertNull(factura);
        verify(ventaRepository, times(1)).findById(99L);
    }

    // --- Pruebas para métodos de Cliente dentro de VentaService ---

    @Test
    @DisplayName("findAllClientes - Debe retornar todos los clientes existentes")
    void findAllClientes_shouldReturnAllClientes() {
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente));

        List<Cliente> clientes = ventaService.findAllClientes();

        assertNotNull(clientes);
        assertFalse(clientes.isEmpty());
        assertEquals(1, clientes.size());
        assertEquals(cliente.getId(), clientes.get(0).getId());

        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findClienteById - Debe retornar un cliente específico por su ID")
    void findClienteById_shouldReturnClienteById() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Optional<Cliente> foundCliente = ventaService.findClienteById(1L);

        assertTrue(foundCliente.isPresent());
        assertEquals(cliente.getId(), foundCliente.get().getId());

        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findClienteById - Debe retornar un Optional vacío si el cliente no se encuentra por ID")
    void findClienteById_shouldReturnEmptyIfClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Cliente> foundCliente = ventaService.findClienteById(99L);

        assertTrue(foundCliente.isEmpty());

        verify(clienteRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("saveCliente - Debe guardar un nuevo cliente")
    void saveCliente_shouldSaveNewCliente() {
        Cliente newCliente = new Cliente(null, "98765432-1", "Maria Lopez", "maria@example.com", "Av. Siempre Viva 742", "123456789", null);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente savedCliente = invocation.getArgument(0);
            savedCliente.setId(2L); // Simular asignación de ID
            return savedCliente;
        });

        Cliente savedCliente = ventaService.saveCliente(newCliente);

        assertNotNull(savedCliente);
        assertEquals(2L, savedCliente.getId());
        assertEquals("Maria Lopez", savedCliente.getNombreCompleto());

        verify(clienteRepository, times(1)).save(newCliente);
    }

    @Test
    @DisplayName("updateCliente - Debe actualizar un cliente existente")
    void updateCliente_shouldUpdateExistingCliente() {
        Cliente updatedDetails = new Cliente(null, "12345678-9", "Juan Perez Actualizado", "juan.updated@example.com", "Nueva Direccion 456", "999888777", null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Cliente> result = ventaService.updateCliente(1L, updatedDetails);

        assertTrue(result.isPresent());
        assertEquals("Juan Perez Actualizado", result.get().getNombreCompleto());
        assertEquals("juan.updated@example.com", result.get().getEmail());

        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("updateCliente - Debe retornar Optional vacío si el cliente a actualizar no se encuentra")
    void updateCliente_shouldReturnEmptyIfClienteNotFound() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Cliente> result = ventaService.updateCliente(99L, new Cliente());

        assertTrue(result.isEmpty());

        verify(clienteRepository, times(1)).findById(99L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("deleteClienteById - Debe eliminar un cliente exitosamente por su ID")
    void deleteClienteById_shouldDeleteClienteById() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        boolean deleted = ventaService.deleteClienteById(1L);

        assertTrue(deleted);

        verify(clienteRepository, times(1)).existsById(1L);
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteClienteById - Debe retornar falso si el cliente a eliminar no existe")
    void deleteClienteById_shouldReturnFalseIfClienteNotFound() {
        when(clienteRepository.existsById(anyLong())).thenReturn(false);

        boolean deleted = ventaService.deleteClienteById(99L);

        assertFalse(deleted);

        verify(clienteRepository, times(1)).existsById(99L);
        verify(clienteRepository, never()).deleteById(anyLong());
    }
}