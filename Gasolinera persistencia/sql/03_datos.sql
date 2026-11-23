-- =====================================================================
-- Gasolinera con persistencia - Fase 1 (datos de ejemplo)
-- 03_datos.sql : carga coherente sobre 01_esquema.sql y 02_triggers.sql
-- Ejecutar TERCERO. Se usan ids explicitos para que las relaciones del
-- enunciado se lean directamente en las comprobaciones de 04.
-- No se desactivan claves foráneas.
-- =====================================================================

USE gasolinera_mysql;

-- ---------------------------------------------------------------------
-- 1. ESTACIONES, FICHAS 1:1 Y SURTIDORES
-- Cinco estaciones, cada una con su ficha. La estacion 1 tiene tres
-- surtidores (numero_visible 1, 2, 3), la estacion 2 tiene dos y las
-- demas una; el numero visible se repite entre estaciones distintas,
-- nunca dentro de la misma.
-- ---------------------------------------------------------------------

INSERT INTO estacion (id, codigo_estacion, nombre_comercial, direccion, telefono, fecha_apertura) VALUES
 (1, 'EST-001', 'Gasolinera Norte',  'Calle Mayor 10, Madrid',   '910000001', '2015-03-01'),
 (2, 'EST-002', 'Gasolinera Centro', 'Avenida Central 22, Toledo','925000002', '2017-06-15'),
 (3, 'EST-003', 'Gasolinera Sur',    'Carretera A4 km 12, Cordoba','957000003', '2019-01-10'),
 (4, 'EST-004', 'Gasolinera Este',   'Calle Sol 5, Valencia',    '960000004', '2020-09-20'),
 (5, 'EST-005', 'Gasolinera Oeste',  'Avenida Mar 8, Malaga',    '952000005', '2022-04-05');

INSERT INTO ficha_facturacion (id, codigo_ficha, estacion_id, serie_tickets, siguiente_numero, texto_pie) VALUES
 (1, 'FIC-001', 1, 'A', 101, 'Gracias por su visita - Gasolinera Norte'),
 (2, 'FIC-002', 2, 'B', 201, 'Gracias por su visita - Gasolinera Centro'),
 (3, 'FIC-003', 3, 'C', 301, 'Gracias por su visita - Gasolinera Sur'),
 (4, 'FIC-004', 4, 'D', 401, 'Gracias por su visita - Gasolinera Este'),
 (5, 'FIC-005', 5, 'E', 501, 'Gracias por su visita - Gasolinera Oeste');

INSERT INTO surtidor (id, codigo_equipo, estacion_id, numero_visible, tipo_combustible, estado_servicio) VALUES
 (1, 'SUR-001', 1, 1, 'GASOLINA 95', 'OPERATIVO'),
 (2, 'SUR-002', 1, 2, 'GASOLINA 98', 'OPERATIVO'),
 (3, 'SUR-003', 1, 3, 'DIESEL',      'AVERIADO'),
 (4, 'SUR-004', 2, 1, 'GASOLINA 95', 'OPERATIVO'),
 (5, 'SUR-005', 2, 2, 'DIESEL',      'OPERATIVO'),
 (6, 'SUR-006', 3, 1, 'GASOLINA 95', 'OPERATIVO'),
 (7, 'SUR-007', 4, 1, 'GASOLINA 98', 'OPERATIVO'),
 (8, 'SUR-008', 5, 1, 'DIESEL',      'OPERATIVO');

-- ---------------------------------------------------------------------
-- 2. PERSONAL
-- EMP-001: sin supervisor, supervisa a 002, 003 y 004, y tiene dos
--          funciones activas a la vez (jefe y cajero).
-- EMP-002: con supervisor.
-- EMP-003: recupera EXPENDEDOR tras un periodo anterior finalizado.
-- EMP-004: con supervisor y responsable de tres incidencias.
-- EMP-005: con supervisor, sin funciones y sin incidencias.
-- EMP-006: sin supervisor, con una funcion.
-- ---------------------------------------------------------------------

INSERT INTO empleado (id, numero_interno, documento_identidad, nombre, telefono, email, fecha_incorporacion, estacion_id, supervisor_id) VALUES
 (1, 'EMP-001', '11111111A', 'Laura Jefe',       '600000001', 'laura@gasolinera.es',   '2020-01-01', 1, NULL),
 (2, 'EMP-002', '22222222B', 'Marcos Cajero',    '600000002', 'marcos@gasolinera.es',  '2022-03-01', 1, 1),
 (3, 'EMP-003', '33333333C', 'Sara Expendedora', '600000003', 'sara@gasolinera.es',    '2021-01-01', 1, 1),
 (4, 'EMP-004', '44444444D', 'Diego Responsable','600000004', 'diego@gasolinera.es',   '2023-06-01', 2, 1),
 (5, 'EMP-005', '55555555E', 'Elena SinFuncion', '600000005', 'elena@gasolinera.es',   '2026-02-01', 2, 4),
 (6, 'EMP-006', '66666666F', 'Pablo Cajero',     NULL,        NULL,                    '2024-05-01', 3, NULL);

INSERT INTO historial_funcion
 (id, empleado_id, tipo_funcion, fecha_inicio, fecha_fin,
  limite_devoluciones, numero_acreditacion, caducidad_acreditacion, presupuesto_compras, nivel_autorizacion) VALUES
 (1, 1, 'JEFE_ESTACION',     '2020-01-01', NULL,         NULL,   NULL,    NULL,        NULL,   'ALTO'),
 (2, 1, 'CAJERO',            '2024-01-01', NULL,         300.00, NULL,    NULL,        NULL,   NULL),
 (3, 2, 'CAJERO',            '2022-03-01', NULL,         150.00, NULL,    NULL,        NULL,   NULL),
 (4, 3, 'EXPENDEDOR',        '2021-01-01', '2023-12-31', NULL,   'AC-100','2024-12-31',NULL,   NULL),
 (5, 3, 'EXPENDEDOR',        '2025-01-01', NULL,         NULL,   'AC-200','2027-12-31',NULL,   NULL),
 (6, 4, 'RESPONSABLE_TIENDA','2023-06-01', NULL,         NULL,   NULL,    NULL,        2000.00,NULL),
 (7, 6, 'CAJERO',            '2024-05-01', NULL,         100.00, NULL,    NULL,        NULL,   NULL);

-- ---------------------------------------------------------------------
-- 3. CLIENTES Y TARJETAS
-- CLI-001 a CLI-003 tienen tarjeta; CLI-004, CLI-005 y CLI-006 no.
-- TAR-004 a TAR-006 estan registradas pero sin entregar (sin titular).
-- CLI-006 solo tiene los campos obligatorios (codigo y nombre): documenta
-- el criterio de contactos opcionales.
-- ---------------------------------------------------------------------

INSERT INTO cliente (id, codigo_cliente, nombre, telefono, email, direccion) VALUES
 (1, 'CLI-001', 'Jorge Perez',  '655000001', 'jorge@example.es', 'Calle Uno 1, Madrid'),
 (2, 'CLI-002', 'Emma Ruiz',    NULL,        'emma@example.es',  'Calle Dos 2, Toledo'),
 (3, 'CLI-003', 'Luna Torres',  '655000003', 'luna@example.es',  NULL),
 (4, 'CLI-004', 'Maria Soto',   '655000004', NULL,               NULL),
 (5, 'CLI-005', 'Bermu Diaz',   NULL,        'bermu@example.es', NULL),
 (6, 'CLI-006', 'Sin Medios',   NULL,        NULL,               NULL);

INSERT INTO tarjeta_fidelizacion (id, numero_tarjeta, fecha_emision, cliente_id, fecha_asignacion) VALUES
 (1, 'TAR-001', '2024-01-10', 1, '2024-01-15'),
 (2, 'TAR-002', '2024-02-01', 2, '2024-02-05'),
 (3, 'TAR-003', '2024-03-01', 3, '2024-03-02'),
 (4, 'TAR-004', '2024-04-01', NULL, NULL),
 (5, 'TAR-005', '2024-04-01', NULL, NULL),
 (6, 'TAR-006', '2024-04-01', NULL, NULL);

-- ---------------------------------------------------------------------
-- 4. CATALOGO, HISTORIAL DE PRECIOS Y PROVEEDORES
-- PROD-001, 002, 006 y 007 son ENVASADO; PROD-003, 004 y 005 GRANEL.
-- PROD-006 esta RETIRADO pero sigue presente en la venta 5.
-- Las dos filas del historial corresponden a cambios reales anteriores;
-- el trigger de 02 las registrará con los cambios de 04.
-- ---------------------------------------------------------------------

INSERT INTO producto
 (id, referencia_comercial, descripcion, precio_venta, estado_comercializacion, modalidad, formato_envase, precision_pesada) VALUES
 (1, 'PROD-001', 'Refresco cola 330 ml',  1.50, 'ACTIVO',   'ENVASADO', 'Botella 330 ml', NULL),
 (2, 'PROD-002', 'Agua mineral 1,5 L',    0.90, 'ACTIVO',   'ENVASADO', 'Botella 1,5 L',  NULL),
 (3, 'PROD-003', 'Cafe en grano',        12.50, 'ACTIVO',   'GRANEL',   NULL,            0.100),
 (4, 'PROD-004', 'Frutos secos variados', 9.80, 'ACTIVO',   'GRANEL',   NULL,            0.050),
 (5, 'PROD-005', 'Chucharros',           14.00, 'ACTIVO',   'GRANEL',   NULL,            0.010),
 (6, 'PROD-006', 'Pan de molde 500 g',    2.10, 'RETIRADO', 'ENVASADO', 'Paquete 500 g', NULL),
 (7, 'PROD-007', 'Zumo natural 1 L',      2.40, 'ACTIVO',   'ENVASADO', 'Brik 1 L',      NULL);

INSERT INTO producto_precio_historial (id, producto_id, precio_anterior, precio_nuevo, fecha_cambio) VALUES
 (1, 1, 1.40,  1.50, '2026-01-10 09:30:00'),
 (2, 3, 12.00, 12.50,'2026-02-01 11:00:00');

INSERT INTO proveedor (id, codigo_proveedor, identificacion_fiscal, razon_social, telefono, email, direccion) VALUES
 (1, 'PROV-001', 'A11111111', 'Distribuidora Norte', '910111111', 'pedidos@dnorte.es', 'Calle Norte 1, Madrid'),
 (2, 'PROV-002', 'B22222222', 'Bebidas Sur',        '910222222', 'pedidos@bsur.es',   'Calle Sur 2, Sevilla'),
 (3, 'PROV-003', 'C33333333', 'Granos Este',        '910333333', 'pedidos@geste.es',  'Calle Este 3, Valencia'),
 (4, 'PROV-004', 'D44444444', 'Frutos Secos SL',    '910444444', 'pedidos@fs_sl.es',  'Calle Oeste 4, Malaga'),
 (5, 'PROV-005', 'E55555555', 'Panaderia Central',  '910555555', 'pedidos@pc.es',     ' Plaza Central 5, Toledo');

-- PROV-001 suministra tres productos; PROD-001 y PROD-005 tienen dos
-- proveedores. Ningun proveedor ni producto queda sin acuerdo.
INSERT INTO proveedor_producto
 (id, proveedor_id, producto_id, referencia_proveedor, precio_compra, plazo_entrega_dias) VALUES
 (1, 1, 1, 'DN-001', 1.05, 3),
 (2, 1, 2, 'DN-002', 0.60, 2),
 (3, 1, 7, 'DN-007', 1.70, 4),
 (4, 2, 1, 'BS-001', 1.10, 2),
 (5, 3, 3, 'GE-003', 9.00, 5),
 (6, 3, 5, 'GE-005',11.00, 6),
 (7, 4, 4, 'FS-004', 7.20, 4),
 (8, 4, 5, 'FS-005',10.50, 5),
 (9, 5, 6, 'PC-006', 1.40, 1),
 (10,5, 7, 'PC-007', 1.75, 3);

-- ---------------------------------------------------------------------
-- 5. CAMPAÑAS E INSCRIPCIONES
-- CAM-003 no tiene inscritos; CLI-006 no esta en ninguna campaña;
-- CLI-001 esta en tres campañas y CAM-001 tiene tres clientes.
-- ---------------------------------------------------------------------

INSERT INTO campana (id, codigo_campana, nombre, descripcion, fecha_inicio, fecha_fin) VALUES
 (1, 'CAM-001', 'Verano fresco',    'Descuentos en bebidas frias', '2026-06-01', '2026-08-31'),
 (2, 'CAM-002', 'Cafe a granel',    'Precio especial en cafe',     '2026-07-01', '2026-09-30'),
 (3, 'CAM-003', 'Sin inscritos',    'Campaña de prueba sin alta',  '2026-09-01', '2026-10-31'),
 (4, 'CAM-004', 'Clientes fieles',  'Puntos por compra',           '2026-01-01', '2026-12-31'),
 (5, 'CAM-005', 'Navidad',          'Promocion de temporada',      '2026-11-01', '2026-12-31');

INSERT INTO campana_cliente (id, campana_id, cliente_id, fecha_inscripcion) VALUES
 (1, 1, 1, '2026-06-05'),
 (2, 1, 2, '2026-06-07'),
 (3, 1, 3, '2026-06-10'),
 (4, 2, 1, '2026-07-02'),
 (5, 2, 4, '2026-07-03'),
 (6, 4, 2, '2026-02-01'),
 (7, 4, 3, '2026-02-02'),
 (8, 4, 5, '2026-02-03'),
 (9, 5, 1, '2026-11-05'),
 (10,5, 5, '2026-11-06');

-- ---------------------------------------------------------------------
-- 6. VENTAS, LINEAS Y PAGOS
-- Seis tickets: 1, 3, 4 y 5 anonimos o identificados segun se indica;
-- doce lineas en total. Venta 1 y 3 tienen varias lineas y la venta 3 se
-- cobra con tres pagos. Venta 5 queda PARCIAL y venta 6 PENDIENTE para
-- demostrar los estados de cobro.
-- El precio de cada linea es el aplicado entonces, no el del catalogo.
-- ---------------------------------------------------------------------

INSERT INTO venta (id, referencia_ticket, estacion_id, empleado_id, cliente_id, fecha_hora, importe_total, estado_pago) VALUES
 (1, 'VTA-2026-0001', 1, 1, 1,    '2026-09-25 10:15:00', 13.45, 'PAGADA'),
 (2, 'VTA-2026-0002', 1, 2, NULL, '2026-09-25 11:30:00',  3.95, 'PAGADA'),
 (3, 'VTA-2026-0003', 2, 4, 2,    '2026-09-26 09:00:00',  9.00, 'PAGADA'),
 (4, 'VTA-2026-0004', 2, 5, NULL, '2026-10-01 12:00:00', 10.30, 'PAGADA'),
 (5, 'VTA-2026-0005', 3, 6, 3,    '2026-10-02 15:45:00', 19.20, 'PARCIAL'),
 (6, 'VTA-2026-0006', 4, 1, 4,    '2026-10-03 08:30:00',  2.40, 'PENDIENTE');

INSERT INTO linea_venta
 (id, venta_id, producto_id, numero_orden, cantidad, precio_unitario_aplicado, importe) VALUES
 (1,  1, 1, 1, 3.000, 1.50,  4.50),
 (2,  1, 3, 2, 0.500,12.50,  6.25),
 (3,  1, 2, 3, 3.000, 0.90,  2.70),
 (4,  2, 4, 1, 0.250, 9.80,  2.45),
 (5,  2, 1, 2, 1.000, 1.50,  1.50),
 (6,  3, 5, 1, 0.300,14.00,  4.20),
 (7,  3, 7, 2, 2.000, 2.40,  4.80),
 (8,  4, 2, 1, 6.000, 0.90,  5.40),
 (9,  4, 4, 2, 0.500, 9.80,  4.90),
 (10, 5, 3, 1, 1.200,12.50, 15.00),
 (11, 5, 6, 2, 2.000, 2.10,  4.20),
 (12, 6, 7, 1, 1.000, 2.40,  2.40);

INSERT INTO pago_venta (id, venta_id, importe, fecha_hora, medio_pago, referencia_externa) VALUES
 (1, 1, 10.00, '2026-09-25 10:16:00', 'EFECTIVO',      NULL),
 (2, 1,  3.45, '2026-09-25 10:16:30', 'TARJETA',       'TKT-0001'),
 (3, 2,  3.95, '2026-09-25 11:31:00', 'EFECTIVO',      NULL),
 (4, 3,  4.00, '2026-09-26 09:01:00', 'EFECTIVO',      NULL),
 (5, 3,  3.00, '2026-09-26 09:01:20', 'TARJETA',       'TKT-0003'),
 (6, 3,  2.00, '2026-09-26 09:02:00', 'TRANSFERENCIA', 'TRF-0003'),
 (7, 4, 10.30, '2026-10-01 12:01:00', 'TARJETA',       'TKT-0004'),
 (8, 5,  5.00, '2026-10-02 15:46:00', 'EFECTIVO',      NULL);

-- Dev 1 y Dev 2 son devoluciones parciales sucesivas de la MISMA linea 1
-- (3 unidades compradas, 1 + 1 devueltas: no se supera la cantidad).
-- Dev 3 es una devolucion granel de 0,100 kg de los 0,250 kg comprados en
-- la linea 4. Ninguna mercancia se considera apta para volver al dispensador.
INSERT INTO devolucion
 (id, linea_venta_id, empleado_id, cantidad, motivo, fecha_hora, importe_reintegrado, apta_para_venta) VALUES
 (1, 1, 2, 1.000, 'Unidad defectuosa',        '2026-09-26 10:00:00', 1.50, FALSE),
 (2, 1, 2, 1.000, 'Error al cobrar',          '2026-09-27 10:00:00', 1.50, FALSE),
 (3, 4, 1, 0.100, 'Producto no solicitado',   '2026-09-28 10:00:00', 0.98, FALSE);

-- ---------------------------------------------------------------------
-- 7. INCIDENCIAS
-- INC-004 sin asignar; EMP-001 responsable de tres; EMP-002 y EMP-005 no
-- tienen ninguna incidencia asignada.
-- ---------------------------------------------------------------------

INSERT INTO incidencia
 (id, referencia_incidencia, estacion_id, descripcion, fecha_apertura, estado, empleado_responsable_id) VALUES
 (1, 'INC-001', 1, 'Surtidor 3 averiado',       '2026-09-20 08:00:00', 'ABIERTA',  1),
 (2, 'INC-002', 1, 'Impresora de tickets',      '2026-09-21 09:30:00', 'EN_CURSO', 1),
 (3, 'INC-003', 1, 'Luz de la marquesina',      '2026-09-22 12:00:00', 'RESUELTA', 1),
 (4, 'INC-004', 2, 'Averia pendiente de asignar','2026-10-01 07:45:00','ABIERTA',  NULL),
 (5, 'INC-005', 3, 'Puerta de almacen',         '2026-10-02 16:00:00', 'CERRADA',  3),
 (6, 'INC-006', 4, 'Aseo fuera de servicio',    '2026-10-04 10:00:00', 'ABIERTA',  4);

-- ---------------------------------------------------------------------
-- 8. COMPATIBILIDAD CON LA APLICACION ANTERIOR
-- Campos exactos de la clase Pagos: String fecha (dd/MM/yyyy) e importe
-- y litros como enteros. No se reinterpreta id_cliente como FK.
-- ---------------------------------------------------------------------

INSERT INTO pago_combustible_legado
 (id, id_cliente_original, fecha, importe, litros, combustible) VALUES
 (1, 1, '25/09/2026', 40, 14, 'GASOLINA 95'),
 (2, 2, '25/09/2026', 50, 25, 'GASOLINA 98'),
 (3, 3, '26/09/2026', 30, 12, 'DIESEL'),
 (4, 4, '01/10/2026', 60, 30, 'GASOLINA 95'),
 (5, 5, '02/10/2026', 45, 20, 'DIESEL');
