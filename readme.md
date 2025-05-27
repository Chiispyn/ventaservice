# Pruebas de la API de Ventas

## Prerrequisitos
* Aplicación Spring Boot ejecutándose en http://localhost:8080.
* Cliente HTTP (Postman, Insomnia, curl).
* Base de datos MySQL accesible.

## Inicialización de la Base de Datos
Ejecuta las siguientes sentencias SQL en tu MySQL:
```sql
INSERT INTO productos (nombre, precio) VALUES ('Cepillo de dientes de bambú', 2800.00);
INSERT INTO productos (nombre, precio) VALUES ('Shampoo sólido biodegradable', 3150.00);
INSERT INTO productos (nombre, precio) VALUES ('Bolsa reutilizable de algodón orgánico', 120.00);
INSERT INTO productos (nombre, precio) VALUES ('Botella de agua de acero inoxidable', 2500.00);
INSERT INTO productos (nombre, precio) VALUES ('Juego de cubiertos de bambú', 1800.00);
INSERT INTO productos (nombre, precio) VALUES ('Esponja vegetal compostable', 500.00);
INSERT INTO productos (nombre, precio) VALUES ('Paños de cera de abeja reutilizables (set de 3)', 2000.00);
INSERT INTO productos (nombre, precio) VALUES ('Jabón artesanal con ingredientes naturales', 1000.00);
INSERT INTO productos (nombre, precio) VALUES ('Plato de madera de lenga', 3500.00);
INSERT INTO productos (nombre, precio) VALUES ('Semillas orgánicas para huerto en casa (pack)', 1505.00);


Pruebas de Productos
Crear (POST /api/productos)
Cuerpo: {"nombre": "Nuevo Producto Prueba", "precio": 22.50}

Obtener todos (GET /api/productos)
Acceder a la URL.

Obtener por ID (GET /api/productos/1)
Reemplazar 1.

Actualizar (PUT /api/productos/1)
Cuerpo: {"id": 1, "precio": 9.00}

Eliminar (DELETE /api/productos/11)
Reemplazar 11.

Pruebas de Ventas
Crear (POST /api/ventas)
Cuerpo: {"fechaVenta": "2025-05-27T11:00:00", "medioEnvio": "RETIRO_EN_TIENDA", "detalles": [{"producto": {"id": 2}, "cantidad": 3}, {"producto": {"id": 5}, "cantidad": 1}]}

Obtener todas (GET /api/ventas)
Acceder a la URL.

Obtener por ID (GET /api/ventas/1)
Reemplazar 1.

Actualizar (PUT /api/ventas/1)
Cuerpo: {"id": 1, "estado": "COMPLETADA"}

Eliminar (DELETE /api/ventas/2)
Reemplazar 2.

Descuento (PUT /api/ventas/1/descuento?porcentajeDescuento=15)
Reemplazar 1.

Cancelar (PUT /api/ventas/1/cancelar)
Reemplazar 1.

Factura (GET /api/ventas/1/factura)
Reemplazar 1.

Pruebas de Facturas
Obtener todas (GET /api/facturas)
Acceder a la URL.

Obtener por ID (GET /api/facturas/1)
Reemplazar 1.

Obtener por Venta ID (GET /api/facturas/venta/1)
Reemplazar 1.

Emitir (POST /api/facturas/emitir/1)
Reemplazar 1.

Actualizar (PUT /api/facturas/1)
Cuerpo: {"codFactura": 1, "totalFactura": 160.00}

Eliminar (DELETE /api/facturas/1)
Reemplazar 1.