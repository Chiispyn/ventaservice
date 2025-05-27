# Pruebas de la API de Ventas

## Prerrequisitos

* Aplicación Spring Boot ejecutándose en `http://localhost:8080`.
* Cliente HTTP (Postman, Insomnia, curl).
* Base de datos MySQL accesible.

## Inicialización de la Base de Datos

Ejecuta las siguientes sentencias SQL en tu MySQL:

```sql
INSERT INTO productos (nombre, precio) VALUES ('Cepillo de dientes de bambú', 8.00);
INSERT INTO productos (nombre, precio) VALUES ('Shampoo sólido biodegradable', 15.00);
INSERT INTO productos (nombre, precio) VALUES ('Bolsa reutilizable de algodón orgánico', 12.00);
INSERT INTO productos (nombre, precio) VALUES ('Botella de agua de acero inoxidable', 25.00);
INSERT INTO productos (nombre, precio) VALUES ('Juego de cubiertos de bambú', 18.00);
INSERT INTO productos (nombre, precio) VALUES ('Esponja vegetal compostable', 5.00);
INSERT INTO productos (nombre, precio) VALUES ('Paños de cera de abeja reutilizables (set de 3)', 20.00);
INSERT INTO productos (nombre, precio) VALUES ('Jabón artesanal con ingredientes naturales', 10.00);
INSERT INTO productos (nombre, precio) VALUES ('Plato de madera de lenga', 35.00);
INSERT INTO productos (nombre, precio) VALUES ('Semillas orgánicas para huerto en casa (pack)', 15.00);

Pruebas de Productos
Crear un Producto (POST /api/productos)
Cuerpo (JSON):

JSON

{
  "nombre": "Nuevo Producto Prueba",
  "precio": 22.50
}
Obtener todos los Productos (GET /api/productos)
Accede a http://localhost:8080/api/productos.

Obtener un Producto por ID (GET /api/productos/1)
Reemplaza 1 con el ID de un producto existente.

Actualizar un Producto (PUT /api/productos/1)
Cuerpo (JSON):

JSON

{
  "id": 1,
  "precio": 9.00
}
Eliminar un Producto (DELETE /api/productos/11)
Reemplaza 11 con el ID de un producto que quieras eliminar.

Pruebas de Ventas
Crear una Venta con Detalles (POST /api/ventas)
Cuerpo (JSON):

JSON

{
  "fechaVenta": "2025-05-27T11:00:00",
  "medioEnvio": "RETIRO_EN_TIENDA",
  "detalles": [
    {
      "producto": { "id": 2 },
      "cantidad": 3
    },
    {
      "producto": { "id": 5 },
      "cantidad": 1
    }
  ]
}
Obtener todas las Ventas (GET /api/ventas)
Accede a http://localhost:8080/api/ventas.

Obtener una Venta por ID (GET /api/ventas/1)
Reemplaza 1 con el ID de una venta existente.

Actualizar una Venta (PUT /api/ventas/1)
Cuerpo (JSON):

JSON

{
  "id": 1,
  "estado": "COMPLETADA"
}
Eliminar una Venta (DELETE /api/ventas/2)
Reemplaza 2 con el ID de una venta que quieras eliminar.

Aplicar Descuento a una Venta (PUT /api/ventas/1/descuento?porcentajeDescuento=15)
Reemplaza 1 con el ID de la venta. (No requiere cuerpo JSON, se pasa por parámetro en la URL).

Cancelar una Venta (PUT /api/ventas/1/cancelar)
Reemplaza 1 con el ID de la venta. (No requiere cuerpo JSON).

Generar Factura de una Venta (GET /api/ventas/1/factura)
Reemplaza 1 con el ID de la venta.

Pruebas de Facturas
Obtener todas las Facturas (GET /api/facturas)
Accede a http://localhost:8080/api/facturas.

Obtener una Factura por ID (GET /api/facturas/1)
Reemplaza 1 con el ID de una factura existente.

Obtener una Factura por ID de Venta (GET /api/facturas/venta/1)
Reemplaza 1 con el ID de una venta para la que se emitió factura.

Emitir una Factura para una Venta (POST /api/facturas/emitir/1)
Reemplaza 1 con el ID de la venta. No requiere cuerpo JSON.

Actualizar una Factura (PUT /api/facturas/1)
Cuerpo (JSON):

JSON

{
  "codFactura": 1,
  "totalFactura": 160.00
}
Eliminar una Factura (DELETE /api/facturas/1)
Reemplaza 1 con el ID de la factura que quieras eliminar.