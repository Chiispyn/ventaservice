package com.venta.ventas.service;

import com.venta.ventas.enums.EstadoVenta;
import com.venta.ventas.enums.MedioEnvio;
import com.venta.ventas.model.Cliente; // <-- NUEVA IMPORTACIÓN
import com.venta.ventas.model.DetalleVenta;
import com.venta.ventas.model.Producto;
import com.venta.ventas.model.Venta;
import com.venta.ventas.repository.ClienteRepository; // <-- NUEVA IMPORTACIÓN
import com.venta.ventas.repository.ProductoRepository;
import com.venta.ventas.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock // <-- NUEVO MOCK PARA EL REPOSITORIO DE CLIENTES
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VentaService ventaService;

    private Venta venta1;
    private Venta venta2;
    private Producto productoA;
    private Producto productoB;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;
    private Cliente cliente1; // <-- NUEVO CAMPO DE CLIENTE DE PRUEBA
    private Cliente cliente2; // <-- NUEVO CAMPO DE CLIENTE DE PRUEBA

    @BeforeEach
    void setUp() {
        productoA = new Producto();
        productoA.setId(101L);
        productoA.setNombre("Detergente Ecológico Concentrado");
        productoA.setPrecio(12.50);

        productoB = new Producto();
        productoB.setId(102L);
        productoB.setNombre("Shampoo Sólido Artesanal");
        productoB.setPrecio(8.00);

        // --- Configuración de Clientes ---
        cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setRut("11.111.111-1");
        cliente1.setNombreCompleto("Juan Pérez");
        cliente1.setEmail("juan.perez@example.com");
        cliente1.setTelefono("911112222");

        cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setRut("22.222.222-2");
        cliente2.setNombreCompleto("María García");
        cliente2.setEmail("maria.garcia@example.com");
        cliente2.setTelefono("933334444");
        // --- Fin Configuración de Clientes ---

        detalle1 = new DetalleVenta();
        detalle1.setId(1L);
        detalle1.setProducto(productoA);
        detalle1.setCantidad(2);
        detalle1.setPrecioUnitario(productoA.getPrecio()); 

        detalle2 = new DetalleVenta();
        detalle2.setId(2L);
        detalle2.setProducto(productoB);
        detalle2.setCantidad(3);
        detalle2.setPrecioUnitario(productoB.getPrecio()); 

        venta1 = new Venta();
        venta1.setId(1L);
        venta1.setFechaVenta(LocalDateTime.now());
        venta1.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO); 
        venta1.setEstado(EstadoVenta.PENDIENTE); 
        venta1.setDetalles(Arrays.asList(detalle1, detalle2));
        venta1.setCliente(cliente1); // <-- ASIGNAMOS CLIENTE A VENTA1
        // Asignar la venta a los detalles para consistencia y para calcular correctamente
        detalle1.setVenta(venta1);
        detalle2.setVenta(venta1);
        venta1.calcularMontoTotal(); // Calcula 2*12.50 + 3*8.00 = 25.00 + 24.00 = 49.00

        venta2 = new Venta();
        venta2.setId(2L);
        venta2.setFechaVenta(LocalDateTime.now().minusDays(5));
        venta2.setMontoTotal(30.0);
        venta2.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA); 
        venta2.setEstado(EstadoVenta.COMPLETADA); 
        venta2.setDetalles(Collections.emptyList()); 
        venta2.setCliente(cliente2); // <-- ASIGNAMOS CLIENTE A VENTA2
    }

    @Test
    void findAll_shouldReturnAllVentas() {
        // Arrange
        List<Venta> expectedVentas = Arrays.asList(venta1, venta2);
        when(ventaRepository.findAll()).thenReturn(expectedVentas);

        // Act
        List<Venta> actualVentas = ventaService.findAll();

        // Assert
        assertNotNull(actualVentas);
        assertEquals(2, actualVentas.size());
        assertEquals(expectedVentas, actualVentas);
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnVentaWhenFound() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta1));

        // Act
        Optional<Venta> actualVenta = ventaService.findById(1L);

        // Assert
        assertTrue(actualVenta.isPresent());
        assertEquals(venta1, actualVenta.get());
        verify(ventaRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Venta> actualVenta = ventaService.findById(99L);

        // Assert
        assertFalse(actualVenta.isPresent());
        verify(ventaRepository, times(1)).findById(99L);
    }

    @Test
    void save_shouldProcessDetallesAndCalculateTotalAndSaveVenta() {
        // Arrange
        Venta newVenta = new Venta();
        newVenta.setFechaVenta(LocalDateTime.now());
        newVenta.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);
        // NO asignamos cliente aquí, lo pasamos como argumento al método save del servicio

        DetalleVenta newDetalle1 = new DetalleVenta();
        newDetalle1.setProducto(productoA); 
        newDetalle1.setCantidad(1);

        DetalleVenta newDetalle2 = new DetalleVenta();
        newDetalle2.setProducto(productoB); 
        newDetalle2.setCantidad(3);

        newVenta.setDetalles(Arrays.asList(newDetalle1, newDetalle2));

        // Mocks para la verificación de productos y cliente
        when(productoRepository.findById(productoA.getId())).thenReturn(Optional.of(productoA));
        when(productoRepository.findById(productoB.getId())).thenReturn(Optional.of(productoB));
        when(clienteRepository.findById(cliente1.getId())).thenReturn(Optional.of(cliente1)); // <-- MOCK PARA EL CLIENTE

        // Creamos el objeto Venta que *simula lo que el servicio habría procesado*
        Venta processedVentaByService = new Venta();
        processedVentaByService.setFechaVenta(newVenta.getFechaVenta());
        processedVentaByService.setMedioEnvio(newVenta.getMedioEnvio());
        processedVentaByService.setEstado(EstadoVenta.PENDIENTE); 
        processedVentaByService.setCliente(cliente1); // <-- ASIGNAMOS EL CLIENTE PROCESADO

        // Clonamos y modificamos los detalles tal como el servicio lo haría
        DetalleVenta processedDetalle1 = new DetalleVenta();
        processedDetalle1.setProducto(productoA);
        processedDetalle1.setCantidad(newDetalle1.getCantidad());
        processedDetalle1.setPrecioUnitario(productoA.getPrecio()); 
        processedDetalle1.setVenta(processedVentaByService); 

        DetalleVenta processedDetalle2 = new DetalleVenta();
        processedDetalle2.setProducto(productoB);
        processedDetalle2.setCantidad(newDetalle2.getCantidad());
        processedDetalle2.setPrecioUnitario(productoB.getPrecio()); 
        processedDetalle2.setVenta(processedVentaByService); 

        processedVentaByService.setDetalles(Arrays.asList(processedDetalle1, processedDetalle2));
        processedVentaByService.calcularMontoTotal(); 
        // Calcula (1 * 12.50) + (3 * 8.00) = 12.50 + 24.00 = 36.50

        // El objeto que el mock del repositorio *devuelve* debe tener un ID
        Venta savedVentaWithId = processedVentaByService; 
        savedVentaWithId.setId(3L); 

        when(ventaRepository.save(any(Venta.class))).thenReturn(savedVentaWithId);

        // Act
        Venta result = ventaService.save(newVenta, cliente1.getId()); // <-- PASAMOS EL ID DEL CLIENTE

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(36.50, result.getMontoTotal(), 0.001); 
        assertEquals(EstadoVenta.PENDIENTE, result.getEstado());
        assertEquals(2, result.getDetalles().size());
        assertEquals(productoA.getPrecio(), result.getDetalles().get(0).getPrecioUnitario());
        assertEquals(productoB.getPrecio(), result.getDetalles().get(1).getPrecioUnitario());
        assertEquals(cliente1.getId(), result.getCliente().getId()); // <-- VERIFICAMOS EL CLIENTE ASIGNADO

        // Verificar interacciones con repositorios
        verify(productoRepository, times(1)).findById(productoA.getId());
        verify(productoRepository, times(1)).findById(productoB.getId());
        verify(clienteRepository, times(1)).findById(cliente1.getId()); // <-- VERIFICAMOS BÚSQUEDA DE CLIENTE
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    void save_shouldThrowExceptionWhenProductoNotFoundInDetalle() {
        // Arrange
        Venta newVenta = new Venta();
        newVenta.setFechaVenta(LocalDateTime.now());
        newVenta.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);

        Producto nonExistentProduct = new Producto();
        nonExistentProduct.setId(999L); 

        DetalleVenta newDetalle = new DetalleVenta();
        newDetalle.setProducto(nonExistentProduct);
        newDetalle.setCantidad(1);

        newVenta.setDetalles(Collections.singletonList(newDetalle));

        when(productoRepository.findById(nonExistentProduct.getId())).thenReturn(Optional.empty());
        when(clienteRepository.findById(cliente1.getId())).thenReturn(Optional.of(cliente1)); // <-- MOCK PARA CLIENTE

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ventaService.save(newVenta, cliente1.getId()); // <-- PASAMOS EL ID DEL CLIENTE
        });

        assertTrue(thrown.getMessage().contains("Producto con ID 999 no encontrado."));
        verify(productoRepository, times(1)).findById(nonExistentProduct.getId());
        verify(clienteRepository, times(1)).findById(cliente1.getId()); // <-- VERIFICAMOS BÚSQUEDA DE CLIENTE
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void save_shouldThrowExceptionWhenClienteNotFound() { // <-- NUEVO TEST PARA CLIENTE NO ENCONTRADO
        // Arrange
        Venta newVenta = new Venta();
        newVenta.setFechaVenta(LocalDateTime.now());
        newVenta.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);
        newVenta.setDetalles(Collections.emptyList());

        Long nonExistentClienteId = 999L;
        when(clienteRepository.findById(nonExistentClienteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ventaService.save(newVenta, nonExistentClienteId);
        });

        assertTrue(thrown.getMessage().contains("Cliente con ID " + nonExistentClienteId + " no encontrado."));
        verify(clienteRepository, times(1)).findById(nonExistentClienteId);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void save_shouldSetTotalToZeroIfNoDetalles() {
        // Arrange
        Venta newVenta = new Venta();
        newVenta.setFechaVenta(LocalDateTime.now());
        newVenta.setMedioEnvio(MedioEnvio.RETIRO_EN_TIENDA);
        newVenta.setDetalles(Collections.emptyList()); 

        Venta savedVenta = new Venta();
        savedVenta.setId(4L);
        savedVenta.setMontoTotal(0.0);
        savedVenta.setEstado(EstadoVenta.PENDIENTE);
        savedVenta.setCliente(cliente1); // <-- ASIGNAMOS CLIENTE A LA VENTA GUARDADA
        when(ventaRepository.save(any(Venta.class))).thenReturn(savedVenta);
        when(clienteRepository.findById(cliente1.getId())).thenReturn(Optional.of(cliente1)); // <-- MOCK PARA CLIENTE

        // Act
        Venta result = ventaService.save(newVenta, cliente1.getId()); // <-- PASAMOS EL ID DEL CLIENTE

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getMontoTotal(), 0.001); 
        assertEquals(cliente1.getId(), result.getCliente().getId()); // <-- VERIFICAMOS CLIENTE
        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(productoRepository, never()).findById(anyLong()); 
        verify(clienteRepository, times(1)).findById(cliente1.getId()); // <-- VERIFICAMOS BÚSQUEDA DE CLIENTE
    }

    @Test
    void update_shouldUpdateVentaWhenFound() {
        // Arrange
        Venta updatedDetails = new Venta();
        updatedDetails.setFechaVenta(LocalDateTime.now().plusDays(1));
        updatedDetails.setMontoTotal(500.0);
        updatedDetails.setMedioEnvio(MedioEnvio.DESPACHO_A_DOMICILIO);
        updatedDetails.setEstado(EstadoVenta.COMPLETADA);
        // *** CORRECCIÓN AQUÍ: Asegúrate de que el objeto updatedDetails tenga el cliente asignado.
        // Como el método update del servicio mantiene el cliente original, este mock debe reflejarlo.
        updatedDetails.setCliente(venta1.getCliente()); // Asigna el cliente de la venta original al mock de retorno

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta1));
        // Cuando ventaRepository.save es llamado con venta1 (el objeto modificado), devuelve updatedDetails
        when(ventaRepository.save(any(Venta.class))).thenReturn(updatedDetails);

        // Act
        Optional<Venta> actualVenta = ventaService.update(1L, updatedDetails);

        // Assert
        assertTrue(actualVenta.isPresent());
        assertEquals(updatedDetails.getFechaVenta(), actualVenta.get().getFechaVenta());
        assertEquals(updatedDetails.getMontoTotal(), actualVenta.get().getMontoTotal(), 0.001);
        assertEquals(updatedDetails.getMedioEnvio(), actualVenta.get().getMedioEnvio());
        assertEquals(updatedDetails.getEstado(), actualVenta.get().getEstado());
        // Esta línea ahora debería funcionar correctamente porque actualVenta.get().getCliente() no será nulo
        assertEquals(venta1.getCliente().getId(), actualVenta.get().getCliente().getId());
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(venta1);
    }

    @Test
    void update_shouldReturnEmptyWhenVentaNotFound() {
        // Arrange
        Venta updatedDetails = new Venta();
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Venta> actualVenta = ventaService.update(99L, updatedDetails);

        // Assert
        assertFalse(actualVenta.isPresent());
        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void deleteById_shouldReturnTrueWhenVentaExists() {
        // Arrange
        when(ventaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ventaRepository).deleteById(1L);

        // Act
        boolean result = ventaService.deleteById(1L);

        // Assert
        assertTrue(result);
        verify(ventaRepository, times(1)).existsById(1L);
        verify(ventaRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldReturnFalseWhenVentaDoesNotExist() {
        // Arrange
        when(ventaRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = ventaService.deleteById(99L);

        // Assert
        assertFalse(result);
        verify(ventaRepository, times(1)).existsById(99L);
        verify(ventaRepository, never()).deleteById(anyLong());
    }

    @Test
    void aplicarDescuento_shouldApplyDiscountWhenVentaIsPending() {
        // Arrange
        Venta ventaToDiscount = new Venta();
        ventaToDiscount.setId(1L);
        ventaToDiscount.setMontoTotal(100.0);
        ventaToDiscount.setEstado(EstadoVenta.PENDIENTE);
        ventaToDiscount.setCliente(cliente1); // <-- ASIGNAMOS CLIENTE

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaToDiscount));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        Optional<Venta> result = ventaService.aplicarDescuento(1L, 10.0); // 10% de descuento

        // Assert
        assertTrue(result.isPresent());
        assertEquals(90.0, result.get().getMontoTotal(), 0.001); // 100 * 0.9 = 90
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(ventaToDiscount);
    }

    @Test
    void aplicarDescuento_shouldNotApplyDiscountWhenVentaIsNotPending() {
        // Arrange
        Venta ventaCompleted = new Venta();
        ventaCompleted.setId(1L);
ventaCompleted.setMontoTotal(100.0);
        ventaCompleted.setEstado(EstadoVenta.COMPLETADA);
        ventaCompleted.setCliente(cliente1); // <-- ASIGNAMOS CLIENTE

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaCompleted));

        // Act
        Optional<Venta> result = ventaService.aplicarDescuento(1L, 10.0);

        // Assert
        assertFalse(result.isPresent());
        assertEquals(100.0, ventaCompleted.getMontoTotal(), 0.001); // Asegura que no cambió
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void aplicarDescuento_shouldReturnEmptyWhenVentaNotFound() {
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Venta> result = ventaService.aplicarDescuento(99L, 10.0);

        // Assert
        assertFalse(result.isPresent());
        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void cancelarVenta_shouldChangeStatusToCanceladaWhenFound() {
        // Arrange
        Venta ventaToCancel = new Venta();
        ventaToCancel.setId(1L);
        ventaToCancel.setEstado(EstadoVenta.PENDIENTE);
        ventaToCancel.setCliente(cliente1); // <-- ASIGNAMOS CLIENTE

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaToCancel));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        Optional<Venta> result = ventaService.cancelarVenta(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(EstadoVenta.CANCELADA, result.get().getEstado());
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(ventaToCancel);
    }

    @Test
    void cancelarVenta_shouldReturnEmptyWhenVentaNotFound() {
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Venta> result = ventaService.cancelarVenta(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void generarFactura_shouldReturnFormattedFacturaString() {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta1));

        String expectedFacturaPartCliente = "Cliente: " + cliente1.getNombreCompleto() + " (RUT: " + cliente1.getRut() + ")\n"; // <-- NUEVA PARTE ESPERADA
        String expectedFacturaPart1 = "Factura para la Venta ID: 1\n";
        String expectedFacturaPart2 = "Medio de Envío: DESPACHO_A_DOMICILIO\n";
        String expectedFacturaPart3 = "Estado: PENDIENTE\n";
        String expectedFacturaPart4_Detalle1 = "2 x Detergente Ecológico Concentrado @ 12.50 = 25.00\n";
        String expectedFacturaPart4_Detalle2 = "3 x Shampoo Sólido Artesanal @ 8.00 = 24.00\n";
        String expectedFacturaPart5 = "Monto Total: 49.00\n";

        // Act
        String actualFactura = ventaService.generarFactura(1L);

        // Assert
        assertNotNull(actualFactura);
        assertTrue(actualFactura.contains(expectedFacturaPart1));
        assertTrue(actualFactura.contains(expectedFacturaPartCliente)); // <-- VERIFICAMOS LA LÍNEA DEL CLIENTE
        assertTrue(actualFactura.contains(venta1.getFechaVenta().toLocalDate().toString()));
        assertTrue(actualFactura.contains(expectedFacturaPart2));
        assertTrue(actualFactura.contains(expectedFacturaPart3));
        assertTrue(actualFactura.contains(expectedFacturaPart4_Detalle1));
        assertTrue(actualFactura.contains(expectedFacturaPart4_Detalle2));
        assertTrue(actualFactura.contains(expectedFacturaPart5));
        verify(ventaRepository, times(1)).findById(1L);
    }

    @Test
    void generarFactura_shouldReturnFacturaForVentaWithoutDetalles() {
        // Arrange
        when(ventaRepository.findById(2L)).thenReturn(Optional.of(venta2)); // venta2 tiene detalles vacíos

        String expectedFacturaPartCliente = "Cliente: " + cliente2.getNombreCompleto() + " (RUT: " + cliente2.getRut() + ")\n"; // <-- NUEVA PARTE ESPERADA
        // La cadena esperada ahora incluye la línea "--- Detalles ---"
        String expectedFacturaPart = "--- Detalles ---\n" +
                                     "No hay productos en esta venta.\n" +
                                     "Monto Total: 30.00\n";

        // Act
        String actualFactura = ventaService.generarFactura(2L);

        // Assert
        assertNotNull(actualFactura);
        assertTrue(actualFactura.contains("Factura para la Venta ID: 2"));
        assertTrue(actualFactura.contains(expectedFacturaPartCliente)); // <-- VERIFICAMOS LA LÍNEA DEL CLIENTE
        assertTrue(actualFactura.contains(expectedFacturaPart)); 
        verify(ventaRepository, times(1)).findById(2L);
    }

    @Test
    void generarFactura_shouldReturnNullWhenVentaNotFound() {
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        String result = ventaService.generarFactura(99L);

        // Assert
        assertNull(result);
        verify(ventaRepository, times(1)).findById(99L);
    }

    // --- NUEVOS TESTS PARA MÉTODOS DE CLIENTE EN VentaService ---

    @Test
    void findAllClientes_shouldReturnAllClientes() {
        // Arrange
        List<Cliente> expectedClientes = Arrays.asList(cliente1, cliente2);
        when(clienteRepository.findAll()).thenReturn(expectedClientes);

        // Act
        List<Cliente> actualClientes = ventaService.findAllClientes();

        // Assert
        assertNotNull(actualClientes);
        assertEquals(2, actualClientes.size());
        assertEquals(expectedClientes, actualClientes);
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void findClienteById_shouldReturnClienteWhenFound() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));

        // Act
        Optional<Cliente> actualCliente = ventaService.findClienteById(1L);

        // Assert
        assertTrue(actualCliente.isPresent());
        assertEquals(cliente1, actualCliente.get());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    void findClienteById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Cliente> actualCliente = ventaService.findClienteById(99L);

        // Assert
        assertFalse(actualCliente.isPresent());
        verify(clienteRepository, times(1)).findById(99L);
    }

    @Test
    void saveCliente_shouldSaveNewCliente() {
        // Arrange
        Cliente newCliente = new Cliente();
        newCliente.setRut("33.333.333-3");
        newCliente.setNombreCompleto("Carlos Díaz");
        newCliente.setEmail("carlos.diaz@example.com");

        Cliente savedCliente = new Cliente();
        savedCliente.setId(3L);
        savedCliente.setRut("33.333.333-3");
        savedCliente.setNombreCompleto("Carlos Díaz");
        savedCliente.setEmail("carlos.diaz@example.com");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(savedCliente);

        // Act
        Cliente result = ventaService.saveCliente(newCliente);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Carlos Díaz", result.getNombreCompleto());
        verify(clienteRepository, times(1)).save(newCliente);
    }

    @Test
    void updateCliente_shouldUpdateClienteWhenFound() {
        // Arrange
        Cliente updatedClienteDetails = new Cliente();
        updatedClienteDetails.setRut("11.111.111-1");
        updatedClienteDetails.setNombreCompleto("Juan Pérez Actualizado");
        updatedClienteDetails.setEmail("juan.perez.updated@example.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(updatedClienteDetails);

        // Act
        Optional<Cliente> actualCliente = ventaService.updateCliente(1L, updatedClienteDetails);

        // Assert
        assertTrue(actualCliente.isPresent());
        assertEquals("Juan Pérez Actualizado", actualCliente.get().getNombreCompleto());
        assertEquals("juan.perez.updated@example.com", actualCliente.get().getEmail());
        verify(clienteRepository, times(1)).findById(1L);
        verify(clienteRepository, times(1)).save(cliente1); // Verifica que se guarda la misma instancia modificada
    }

    @Test
    void updateCliente_shouldReturnEmptyWhenClienteNotFound() {
        // Arrange
        Cliente updatedClienteDetails = new Cliente();
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Cliente> actualCliente = ventaService.updateCliente(99L, updatedClienteDetails);

        // Assert
        assertFalse(actualCliente.isPresent());
        verify(clienteRepository, times(1)).findById(99L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deleteClienteById_shouldReturnTrueWhenClienteExists() {
        // Arrange
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        // Act
        boolean result = ventaService.deleteClienteById(1L);

        // Assert
        assertTrue(result);
        verify(clienteRepository, times(1)).existsById(1L);
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteClienteById_shouldReturnFalseWhenClienteDoesNotExist() {
        // Arrange
        when(clienteRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean result = ventaService.deleteClienteById(99L);

        // Assert
        assertFalse(result);
        verify(clienteRepository, times(1)).existsById(99L);
        verify(clienteRepository, never()).deleteById(anyLong());
    }
}