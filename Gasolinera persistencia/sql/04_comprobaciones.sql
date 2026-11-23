-- =====================================================================
-- Gasolinera con persistencia - Fase 1 (comprobaciones)
-- 04_comprobaciones.sql : verificaciones, pruebas del trigger y negativas
-- Ejecutar CUARTO y ultimo. No se desactivan claves foráneas.
-- Estructura:
--   A. SELECT de verificacion de todos los casos del enunciado
--   B. Pruebas del trigger de precios (si MODIFICAN datos: precio de
--      PROD-001 queda cambiado; el cambio de PROD-002 se deshace)
--   C. Intentos que DEBEN FALLAR (todos comentados: se pueden ejecutar
--      uno a uno quitando el "--" para ver el error sin abortar el script)
--   D. Sentencias que HOY SE ACEPTAN y que Java debera rechazar en Fase 2
-- =====================================================================

USE gasolinera_mysql;

-- =====================================================================
-- A. VERIFICACION DE LOS CASOS PEDIDOS
-- =====================================================================

-- A1. Estaciones con su ficha y numero de surtidores.
-- Esperado: 5 filas, una ficha por estacion; estacion 1 con 3 surtidores,
-- estacion 2 con 2 y estaciones 3, 4 y 5 con 1.
SELECT e.codigo_estacion,
       e.nombre_comercial,
       f.codigo_ficha,
       f.serie_tickets,
       COUNT(s.id) AS surtidores
FROM estacion e
JOIN ficha_facturacion f ON f.estacion_id = e.id
LEFT JOIN surtidor s ON s.estacion_id = e.id
GROUP BY e.id, e.codigo_estacion, e.nombre_comercial, f.codigo_ficha, f.serie_tickets
ORDER BY e.id;

-- Esperado: 0 filas (ninguna estacion sin ficha; la alta conjunta completa
-- se controlara en Java en Fase 2, pero los datos de ejemplo la cumplen).
SELECT e.id, e.codigo_estacion
FROM estacion e
LEFT JOIN ficha_facturacion f ON f.estacion_id = e.id
WHERE f.id IS NULL;

-- A2. Unico numero visible por estacion (no puede repetirse dentro de una
-- estacion, pero si entre estaciones distintas). Esperado: 0 filas.
SELECT estacion_id, numero_visible, COUNT(*) AS repetidos
FROM surtidor
GROUP BY estacion_id, numero_visible
HAVING COUNT(*) > 1;

SELECT s.codigo_equipo, e.codigo_estacion, s.numero_visible, s.estado_servicio
FROM surtidor s
JOIN estacion e ON e.id = s.estacion_id
ORDER BY s.estacion_id, s.numero_visible;

-- A3. Clientes con y sin tarjeta.
-- Esperado: CLI-001 a CLI-003 con tarjeta asignada; CLI-004 a CLI-006
-- sin tarjeta (CLI-006 ademas no tiene ningun medio de contacto).
SELECT c.codigo_cliente, c.nombre, t.numero_tarjeta, t.fecha_asignacion
FROM cliente c
LEFT JOIN tarjeta_fidelizacion t ON t.cliente_id = c.id
ORDER BY c.id;

-- Esperado: 3 filas (TAR-004, TAR-005 y TAR-006 sin titular).
SELECT numero_tarjeta, fecha_emision
FROM tarjeta_fidelizacion
WHERE cliente_id IS NULL
ORDER BY numero_tarjeta;

-- Esperado: 0 filas (un cliente nunca tiene dos tarjetas a la vez).
SELECT cliente_id, COUNT(*) AS tarjetas
FROM tarjeta_fidelizacion
WHERE cliente_id IS NOT NULL
GROUP BY cliente_id
HAVING COUNT(*) > 1;

-- A4. Supervision: EMP-001 y EMP-006 sin supervisor; el resto con uno.
SELECT e.numero_interno, e.nombre, s.nombre AS supervisor
FROM empleado e
LEFT JOIN empleado s ON s.id = e.supervisor_id
ORDER BY e.id;

-- Esperado: 1 fila (Laura supervisa a Marcos, Sara y Diego).
SELECT s.numero_interno, s.nombre AS supervisor, COUNT(*) AS subordinados
FROM empleado e
JOIN empleado s ON s.id = e.supervisor_id
GROUP BY s.id, s.numero_interno, s.nombre
HAVING COUNT(*) > 1;

-- Esperado: 0 filas, pero SOLO por los datos de ejemplo, no por una
-- restriccion SQL: MariaDB no permite un CHECK sobre la columna FK
-- supervisor_id (error 1901), asi que no hay chk_empleado_no_self_supervisor.
-- Esta consulta es la comprobacion de partida antes de ejecutar D10, que
-- demuestra que MySQL/MariaDB ACEPTA la autosupervision directa.
-- La autosupervision y los ciclos indirectos se validaran en Java (Fase 2).
SELECT id, numero_interno, nombre
FROM empleado
WHERE supervisor_id = id;

-- A5. Funciones: historial completo.
SELECT e.numero_interno, e.nombre, h.tipo_funcion,
       h.fecha_inicio, h.fecha_fin,
       h.limite_devoluciones, h.numero_acreditacion,
       h.caducidad_acreditacion, h.presupuesto_compras, h.nivel_autorizacion
FROM historial_funcion h
JOIN empleado e ON e.id = h.empleado_id
ORDER BY h.empleado_id, h.tipo_funcion, h.fecha_inicio;

-- Funciones simultaneas (periodos abiertos). Esperado: 1 fila, Laura con
-- JEFE_ESTACION y CAJERO. Se agrupa por EMPLEADO, no por fila de historial:
-- agrupar por h.id daria siempre 1 funcion por grupo y no detectaria el caso.
SELECT e.numero_interno, e.nombre,
       COUNT(*) AS funciones_activas,
       GROUP_CONCAT(h.tipo_funcion ORDER BY h.tipo_funcion) AS funciones
FROM historial_funcion h
JOIN empleado e ON e.id = h.empleado_id
WHERE h.fecha_fin IS NULL
GROUP BY e.id, e.numero_interno, e.nombre
HAVING COUNT(*) > 1;

-- Esperado: 1 fila (Elena no tiene ninguna funcion asignada).
SELECT e.numero_interno, e.nombre
FROM empleado e
WHERE NOT EXISTS (SELECT 1 FROM historial_funcion h WHERE h.empleado_id = e.id);

-- Recuperacion de una funcion tras un periodo finalizado.
-- Esperado: 1 fila (Sara, EXPENDEDOR, fin 2023-12-31, nuevo inicio 2025-01-01).
-- Sin el NOT EXISTS la propia join produce dos filas (h1 anterior / h2 nuevo
-- y al reves); la condicion elige solo el periodo cerrado mas reciente.
SELECT e.nombre, h1.tipo_funcion, h1.fecha_fin AS fin_anterior,
       h2.fecha_inicio AS nuevo_inicio
FROM historial_funcion h1
JOIN historial_funcion h2
  ON h2.empleado_id = h1.empleado_id
 AND h2.tipo_funcion = h1.tipo_funcion
 AND h2.fecha_inicio > h1.fecha_fin
JOIN empleado e ON e.id = h1.empleado_id
WHERE h1.fecha_fin IS NOT NULL
  AND NOT EXISTS (
        SELECT 1
        FROM historial_funcion h3
        WHERE h3.empleado_id = h1.empleado_id
          AND h3.tipo_funcion = h1.tipo_funcion
          AND h3.fecha_fin IS NOT NULL
          AND h3.fecha_fin > h1.fecha_fin
          AND h3.fecha_fin < h2.fecha_inicio
  );

-- Esperado: 0 filas (no hay dos periodos ABIERTOS de la misma funcion:
-- uq_funcion_activa lo garantiza; el solapamiento de periodos cerrados
-- queda pendiente de Java en Fase 2).
SELECT empleado_id, tipo_funcion, COUNT(*) AS periodos_abiertos
FROM historial_funcion
WHERE fecha_fin IS NULL
GROUP BY empleado_id, tipo_funcion
HAVING COUNT(*) > 1;

-- A6. Productos de ambas modalidades y atributos propios.
SELECT id, referencia_comercial, modalidad, precio_venta,
       estado_comercializacion, formato_envase, precision_pesada
FROM producto
ORDER BY id;

-- Acuerdos de suministro: esperado, PROV-001 con 3 productos y
-- PROD-001 y PROD-005 con 2 proveedores; ningun aislamiento.
SELECT pr.codigo_proveedor, pr.razon_social,
       COUNT(pp.producto_id) AS productos
FROM proveedor pr
LEFT JOIN proveedor_producto pp ON pp.proveedor_id = pr.id
GROUP BY pr.id, pr.codigo_proveedor, pr.razon_social
ORDER BY pr.id;

SELECT p.referencia_comercial, p.estado_comercializacion,
       COUNT(pp.proveedor_id) AS proveedores
FROM producto p
LEFT JOIN proveedor_producto pp ON pp.producto_id = p.id
GROUP BY p.id, p.referencia_comercial, p.estado_comercializacion
ORDER BY p.id;

SELECT pr.codigo_proveedor, p.referencia_comercial,
       pp.referencia_proveedor, pp.precio_compra, pp.plazo_entrega_dias
FROM proveedor_producto pp
JOIN proveedor pr ON pr.id = pp.proveedor_id
JOIN producto p ON p.id = pp.producto_id
ORDER BY pp.proveedor_id, pp.producto_id;

-- Esperado: 0 filas (ningun proveedor sin producto).
SELECT pr.codigo_proveedor
FROM proveedor pr
LEFT JOIN proveedor_producto pp ON pp.proveedor_id = pr.id
WHERE pp.id IS NULL;

-- Esperado: 0 filas (ningun producto sin proveedor, incluido el retirado).
SELECT p.referencia_comercial
FROM producto p
LEFT JOIN proveedor_producto pp ON pp.producto_id = p.id
WHERE pp.id IS NULL;

-- A7. Campañas e inscripciones.
-- Esperado: CAM-003 con 0 inscritos; CAM-001 con 3.
SELECT c.codigo_campana, c.nombre, COUNT(cc.id) AS inscritos
FROM campana c
LEFT JOIN campana_cliente cc ON cc.campana_id = c.id
GROUP BY c.id, c.codigo_campana, c.nombre
ORDER BY c.id;

-- Esperado: CLI-006 con 0 campañas; CLI-001 con 3.
SELECT cl.codigo_cliente, cl.nombre, COUNT(cc.id) AS campanas
FROM cliente cl
LEFT JOIN campana_cliente cc ON cc.cliente_id = cl.id
GROUP BY cl.id, cl.codigo_cliente, cl.nombre
ORDER BY cl.id;

-- Esperado: 0 filas (no se puede inscribir dos veces al mismo cliente).
SELECT campana_id, cliente_id, COUNT(*)
FROM campana_cliente
GROUP BY campana_id, cliente_id
HAVING COUNT(*) > 1;

SELECT c.codigo_campana, cl.codigo_cliente, cc.fecha_inscripcion
FROM campana_cliente cc
JOIN campana c ON c.id = cc.campana_id
JOIN cliente cl ON cl.id = cc.cliente_id
ORDER BY cc.campana_id, cc.cliente_id;

-- A8. Incidencias.
-- Esperado: INC-004 sin responsable; Laura con 3, Elena y Marcos con 0.
SELECT i.referencia_incidencia, i.estado,
       e.numero_interno AS responsable, i.fecha_apertura
FROM incidencia i
LEFT JOIN empleado e ON e.id = i.empleado_responsable_id
ORDER BY i.id;

SELECT e.numero_interno, e.nombre, COUNT(i.id) AS incidencias
FROM empleado e
LEFT JOIN incidencia i ON i.empleado_responsable_id = e.id
GROUP BY e.id, e.numero_interno, e.nombre
ORDER BY e.id;

-- A9. Ventas: anonimas e identificadas, estados de cobro y multiple pago.
SELECT id, referencia_ticket,
       CASE WHEN cliente_id IS NULL THEN 'ANONIMA' ELSE 'IDENTIFICADA' END AS tipo,
       estado_pago, importe_total
FROM venta
ORDER BY id;

-- Esperado: 6 filas; la suma de lineas coincide con importe_total.
SELECT v.referencia_ticket, COUNT(l.id) AS lineas,
       SUM(l.importe) AS suma_lineas, v.importe_total
FROM venta v
JOIN linea_venta l ON l.venta_id = v.id
GROUP BY v.id, v.referencia_ticket, v.importe_total
ORDER BY v.id;

-- Esperado: ventas 1, 2, 3 y 4 con cobrado = importe_total y estado PAGADA;
-- venta 5 PARCIAL (5.00 de 19.20) y venta 6 PENDIENTE (sin pagos).
-- El control de que solo se marque PAGADA con el importe completo se
-- implementara en Java en Fase 2.
SELECT v.referencia_ticket, v.importe_total,
       COALESCE(SUM(p.importe), 0) AS cobrado, v.estado_pago
FROM venta v
LEFT JOIN pago_venta p ON p.venta_id = v.id
GROUP BY v.id, v.referencia_ticket, v.importe_total, v.estado_pago
ORDER BY v.id;

-- Esperado: 2 filas (ventas 1 y 3 pagadas con mas de un pago).
SELECT v.referencia_ticket, COUNT(p.id) AS num_pagos,
       SUM(p.importe) AS total_cobrado
FROM venta v
JOIN pago_venta p ON p.venta_id = v.id
GROUP BY v.id, v.referencia_ticket
HAVING COUNT(p.id) > 1;

-- Doce lineas repartidas en seis tickets; numero de orden unico por venta.
SELECT v.referencia_ticket, l.numero_orden, p.referencia_comercial,
       l.cantidad, l.precio_unitario_aplicado, l.importe
FROM linea_venta l
JOIN venta v ON v.id = l.venta_id
JOIN producto p ON p.id = l.producto_id
ORDER BY l.venta_id, l.numero_orden;

SELECT venta_id, numero_orden, COUNT(*)
FROM linea_venta
GROUP BY venta_id, numero_orden
HAVING COUNT(*) > 1;   -- Esperado: 0 filas

-- A10. Devoluciones parciales sucesivas de una misma linea.
-- Esperado: linea 1 -> 3 unidades compradas, 2 devueltas (1 + 1),
-- 4.50 de importe y 3.00 reintegrado; linea 4 -> 0.250 kg comprados,
-- 0.100 kg devueltos y 0.98 reintegrados (0.100 x 9.80, el precio cobrado
-- en la linea, no el catalogo actual). Nunca se supera lo comprado ni su
-- importe.
SELECT lv.id AS linea, lv.cantidad AS comprada,
       SUM(d.cantidad) AS devuelta,
       lv.importe AS importe_linea,
       SUM(d.importe_reintegrado) AS reintegrado
FROM linea_venta lv
JOIN devolucion d ON d.linea_venta_id = lv.id
GROUP BY lv.id, lv.cantidad, lv.importe
ORDER BY lv.id;

SELECT d.id, lv.id AS linea, d.cantidad, d.importe_reintegrado,
       d.motivo, d.apta_para_venta, d.fecha_hora
FROM devolucion d
JOIN linea_venta lv ON lv.id = d.linea_venta_id
ORDER BY d.id;

-- Esperado: 0 filas (todas las devoluciones son de ventas PAGADA).
SELECT d.id
FROM devolucion d
JOIN linea_venta lv ON lv.id = d.linea_venta_id
JOIN venta v ON v.id = lv.venta_id
WHERE v.estado_pago <> 'PAGADA';

-- A11. Compatibilidad con la aplicacion anterior.
-- Esperado: 5 filas con fecha dd/MM/yyyy e importe/litros enteros.
SELECT id, id_cliente_original, fecha, importe, litros, combustible
FROM pago_combustible_legado
ORDER BY id;

-- =====================================================================
-- B. PRUEBAS DEL TRIGGER DE PRECIOS (MODIFICAN DATOS)
-- =====================================================================

-- B0. Numero de entradas del historial antes de las pruebas (2 al cargar 03).
SET @n0 = (SELECT COUNT(*) FROM producto_precio_historial);
SELECT @n0 AS entradas_iniciales;

-- B1. Cambio REAL de precio: debe añadir exactamente UNA entrada.
-- Se calcula un precio nuevo distinto para que la prueba sea repetible.
SET @precio1 = (SELECT precio_venta FROM producto WHERE id = 1);
SET @nuevo1  = @precio1 + 0.25;
UPDATE producto SET precio_venta = @nuevo1 WHERE id = 1;

SELECT @n0 AS antes, COUNT(*) AS despues,
       COUNT(*) - @n0 AS entradas_nuevas
FROM producto_precio_historial;

SELECT h.producto_id, p.referencia_comercial,
       h.precio_anterior, h.precio_nuevo, h.fecha_cambio
FROM producto_precio_historial h
JOIN producto p ON p.id = h.producto_id
WHERE h.producto_id = 1
ORDER BY h.fecha_cambio DESC
LIMIT 3;
-- Esperado: la ultima entrada tiene precio_anterior = @precio1 y
-- precio_nuevo = @nuevo1, con fecha/hora del cambio.

-- B2. Guardar el MISMO precio: no debe añadir nada.
UPDATE producto SET precio_venta = @nuevo1 WHERE id = 1;
SELECT COUNT(*) AS historial_tras_mismo_precio,
       COUNT(*) - @n0 - 1 AS entradas_inesperadas
FROM producto_precio_historial;
-- Esperado: historial_tras_mismo_precio = @n0 + 1 y entradas_inesperadas = 0.

-- B3. Cambiar solo la descripcion: no debe añadir nada.
UPDATE producto
SET descripcion = CONCAT(descripcion, ' - descripcion actualizada')
WHERE id = 1;
SELECT COUNT(*) AS historial_tras_descripcion,
       COUNT(*) - @n0 - 1 AS entradas_inesperadas
FROM producto_precio_historial;
-- Esperado: sin cambios en el historial.

-- B4. Cambio dentro de una transaccion que se deshace.
-- El precio y su entrada de historial deben desaparecer juntos (InnoDB).
SET @precio2 = (SELECT precio_venta FROM producto WHERE id = 2);
SET @n1 = (SELECT COUNT(*) FROM producto_precio_historial);

START TRANSACTION;
UPDATE producto SET precio_venta = @precio2 + 1.00 WHERE id = 2;
SELECT 'DENTRO DE LA TRANSACCION' AS momento,
       (SELECT precio_venta FROM producto WHERE id = 2) AS precio,
       (SELECT COUNT(*) FROM producto_precio_historial) AS entradas;
-- Esperado aqui: precio = @precio2 + 1.00 y entradas = @n1 + 1.

ROLLBACK;
SELECT 'TRAS ROLLBACK' AS momento,
       (SELECT precio_venta FROM producto WHERE id = 2) AS precio,
       @precio2 AS precio_original,
       (SELECT COUNT(*) FROM producto_precio_historial) AS entradas,
       @n1 AS entradas_antes;
-- Esperado: precio = precio_original y entradas = entradas_antes.

-- B5. El precio cobrado en un ticket no se recalcula aunque cambie el catalogo.
SELECT v.referencia_ticket, pr.referencia_comercial,
       lv.precio_unitario_aplicado AS precio_cobrado,
       pr.precio_venta AS precio_catalogo_actual
FROM linea_venta lv
JOIN venta v ON v.id = lv.venta_id
JOIN producto pr ON pr.id = lv.producto_id
WHERE lv.id = 1;
-- Esperado: precio_cobrado = 1.50 y precio_catalogo_actual = 1.75
-- (= 1.50 + 0.25 tras B1). El ticket no se recalcula.

-- B6. La creacion de un producto no genera historial (se comprueba sobre
-- los datos ya cargados: 7 productos y solo entradas asociadas a updates).
SELECT p.id, p.referencia_comercial, COUNT(h.id) AS entradas
FROM producto p
LEFT JOIN producto_precio_historial h ON h.producto_id = p.id
GROUP BY p.id, p.referencia_comercial
ORDER BY p.id;

-- =====================================================================
-- C. INTENTOS QUE DEBEN FALLAR (COMENTADOS; quitar el "--" para probar)
-- =====================================================================

-- C1. UNIQUE: codigo de estacion repetido.
-- Error esperado: Duplicate entry 'EST-001' for key 'estacion.uq_estacion_codigo'
-- INSERT INTO estacion (codigo_estacion, nombre_comercial, direccion, fecha_apertura)
-- VALUES ('EST-001', 'Estacion duplicada', 'Calle X 1', '2026-01-01');

-- C2. UNIQUE: dos fichas para la misma estacion (1:1).
-- Error esperado: Duplicate entry '1' for key 'ficha_facturacion.uq_ficha_estacion'
-- INSERT INTO ficha_facturacion (codigo_ficha, estacion_id, serie_tickets)
-- VALUES ('FIC-999', 1, 'Z');

-- C3. UNIQUE: mismo numero visible en una estacion.
-- Error esperado: Duplicate entry '1-1' for key 'surtidor.uq_surtidor_numero'
-- INSERT INTO surtidor (codigo_equipo, estacion_id, numero_visible, tipo_combustible)
-- VALUES ('SUR-999', 1, 1, 'DIESEL');

-- C4. CHECK: precio de venta no positivo.
-- Error esperado: Check constraint 'chk_producto_precio' is violated.
-- INSERT INTO producto
--  (referencia_comercial, descripcion, precio_venta, modalidad, formato_envase)
-- VALUES ('PROD-999', 'Precio cero', 0, 'ENVASADO', 'Caja');

-- C5. CHECK: producto a granel sin precision de pesada.
-- Error esperado: Check constraint 'chk_producto_granel' is violated.
-- INSERT INTO producto
--  (referencia_comercial, descripcion, precio_venta, modalidad)
-- VALUES ('PROD-998', 'Granel sin precision', 5.00, 'GRANEL');

-- C6. CHECK: fin de campaña anterior al inicio.
-- Error esperado: Check constraint 'chk_campana_fechas' is violated.
-- INSERT INTO campana (codigo_campana, nombre, fecha_inicio, fecha_fin)
-- VALUES ('CAM-999', 'Fechas invertidas', '2026-12-01', '2026-11-01');

-- C7. CHECK: cantidad de linea no positiva. El importe se deja valido (1.50)
-- para que el UNICO motivo del error sea la cantidad y no chk_linea_importe.
-- Error esperado: Check constraint 'chk_linea_cantidad' is violated.
-- INSERT INTO linea_venta
--  (venta_id, producto_id, numero_orden, cantidad, precio_unitario_aplicado, importe)
-- VALUES (1, 1, 9, 0, 1.50, 1.50);

-- C8. UNIQUE: ficha de facturacion con codigo repetido.
-- Error esperado: Duplicate entry 'FIC-001' for key 'ficha_facturacion.uq_ficha_codigo'
-- INSERT INTO ficha_facturacion (codigo_ficha, estacion_id, serie_tickets)
-- VALUES ('FIC-001', 2, 'Y');

-- C9. UNIQUE: acuerdo proveedor-producto repetido.
-- Error esperado: Duplicate entry '1-1' for key 'proveedor_producto.uq_proveedor_producto'
-- INSERT INTO proveedor_producto
--  (proveedor_id, producto_id, referencia_proveedor, precio_compra, plazo_entrega_dias)
-- VALUES (1, 1, 'DN-001BIS', 1.20, 3);

-- C10. UNIQUE: doble inscripcion en la misma campaña.
-- Error esperado: Duplicate entry '1-1' for key 'campana_cliente.uq_campana_cliente'
-- INSERT INTO campana_cliente (campana_id, cliente_id, fecha_inscripcion)
-- VALUES (1, 1, '2026-06-05');

-- C11. UNIQUE: dos tarjetas para un mismo cliente.
-- Error esperado: Duplicate entry '1' for key 'tarjeta_fidelizacion.uq_tarjeta_cliente'
-- INSERT INTO tarjeta_fidelizacion (numero_tarjeta, fecha_emision, cliente_id, fecha_asignacion)
-- VALUES ('TAR-999', '2026-01-01', 1, '2026-01-02');

-- C12. UNIQUE: mismo numero de orden dentro de una venta.
-- Error esperado: Duplicate entry '1-1' for key 'linea_venta.uq_linea_orden'
-- INSERT INTO linea_venta
--  (venta_id, producto_id, numero_orden, cantidad, precio_unitario_aplicado, importe)
-- VALUES (1, 2, 1, 1, 0.90, 0.90);

-- C13. FK: referencia a una estacion inexistente.
-- Error esperado: Cannot add or update a child row: a foreign key constraint fails
-- INSERT INTO venta (referencia_ticket, estacion_id, empleado_id, fecha_hora)
-- VALUES ('VTA-999', 999, 1, NOW());

-- C14. FK RESTRICT: no se puede borrar una estacion con ficha y surtidores.
-- Error esperado: Cannot delete or update a parent row: a foreign key constraint fails
-- DELETE FROM estacion WHERE id = 1;

-- C15. CHECK: producto a granel CON formato de envase.
-- Tiene precision de pesada, pero chk_producto_granel exige formato_envase
-- NULL para GRANEL. Es el caso inverso de C5 (granel sin precision).
-- Error esperado: Check constraint 'chk_producto_granel' is violated.
-- INSERT INTO producto
--  (referencia_comercial, descripcion, precio_venta, modalidad, formato_envase, precision_pesada)
-- VALUES ('PROD-997', 'Granel con envase', 5.00, 'GRANEL', 'Bolsa 500 g', 0.050);

-- =====================================================================
-- D. REGLAS AUN NO GARANTIZADAS POR SQL (PENDIENTES DE JAVA EN FASE 2)
-- Las siguientes sentencias HOY SE ACEPTAN. No presentarlas como rechazo
-- automatico: estan aqui solo para documentar el control que falta.
-- =====================================================================

-- D1. Alta conjunta estacion + ficha: se podria dejar una estacion sin ficha.
-- INSERT INTO estacion (codigo_estacion, nombre_comercial, direccion, fecha_apertura)
-- VALUES ('EST-099', 'Provisional', 'Calle Y 9', '2026-05-01');

-- D2. Baja del ultimo surtidor de una estacion (EST-003 se quedaria sin ninguno).
-- DELETE FROM surtidor WHERE id = 6;

-- D3. Un proveedor o producto aislado al borrar su ultimo acuerdo.
-- DELETE FROM proveedor_producto WHERE id = 5;  -- PROV-003 y PROD-003 quedarian aislados

-- D4. Ciclo indirecto de supervision: EMP-001 pasa a depender de EMP-002,
-- que a su vez depende de EMP-001. Hoy no hay ninguna restriccion que lo
-- detecte: se acepta y debe impedirlo Java en Fase 2 (recursion).
-- UPDATE empleado SET supervisor_id = 2 WHERE id = 1;

-- D5. Periodos solapados de una UNA MISMA funcion ( uno cerrado 2022-01-01 a
-- 2022-06-30 se solapa con el periodo real de Sara que empieza en 2021 ).
-- INSERT INTO historial_funcion
--  (empleado_id, tipo_funcion, fecha_inicio, fecha_fin, numero_acreditacion, caducidad_acreditacion)
-- VALUES (3, 'EXPENDEDOR', '2022-01-01', '2022-06-30', 'AC-OLD', '2022-12-31');

-- D6. Venta registrada sin lineas.
-- INSERT INTO venta (referencia_ticket, estacion_id, empleado_id, fecha_hora)
-- VALUES ('VTA-099', 1, 1, NOW());

-- D7. Marcar como PAGADA una venta con menos pagos que su importe.
-- UPDATE venta SET estado_pago = 'PAGADA' WHERE id = 5;

-- D8. Devoluciones acumuladas por encima de la cantidad comprada
-- (la linea 1 tiene 3 unidades y ya se devolvieron 2).
-- INSERT INTO devolucion
--  (linea_venta_id, empleado_id, cantidad, motivo, importe_reintegrado)
-- VALUES (1, 2, 2.000, 'Devolucion excesiva', 3.00);

-- D9. Cambio de modalidad de un producto: hoy pasaria el CHECK si se añade
-- precision; la modalidad debe ser estable durante toda la comercializacion.
-- UPDATE producto SET modalidad = 'GRANEL', precision_pesada = 0.100,
--                     formato_envase = NULL
-- WHERE id = 1;

-- D10. Autosupervision directa: Laura pasa a ser su propia supervisora.
-- Se ACEPTA (no existe chk_empleado_no_self_supervisor: MariaDB/XAMPP
-- devuelve error 1901 si un CHECK menciona una columna con FK). Ver A4.
-- UPDATE empleado SET supervisor_id = id WHERE id = 1;
-- SELECT id, numero_interno, nombre FROM empleado WHERE supervisor_id = id;
-- Si se ejecuta D10, volver a dejarlo como estaba antes de seguir:
-- UPDATE empleado SET supervisor_id = NULL WHERE id = 1;

-- Resumen de lo que Java debera validar en Fase 2:
--   * estacion siempre con su ficha y al menos un surtidor; baja del ultimo;
--   * proveedor y producto nunca aislados (altas conjuntas);
--   * autosupervision directa y ciclos de supervision prohibidos (recursion
--     sobre supervisor_id, sin CHECK posible en MariaDB por error 1901);
--   * periodos de una misma funcion sin solapamiento;
--   * cantidad entera para ENVASADO y decimal valido para GRANEL;
--   * venta con al menos una linea y suma de pagos = importe_total;
--   * devoluciones sobre venta pagada, acumulados <= comprado y <= importe,
--     calculo con el precio cobrado y redondeo HALF_UP a 2 decimales;
--   * modalidad inmutable y historial de precios no editable desde la app.
