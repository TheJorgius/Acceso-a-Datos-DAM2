# 04. Operaciones futuras JDBC (Fase 2)

**Estado actual:** no implementadas. Este documento describe el contrato que se programará cuando se
permita persistir en MySQL/MariaDB. No se añaden sentencias SQL a las clases del modelo ni se modifica
`AlmacenamientoSQLite`.

Patrón común para las tres operaciones:

- `Connection` obtenida de la configuración por variables de entorno (`DB_URL`, `DB_USER`,
  `DB_PASSWORD`), sin credenciales en el código.
- `setAutoCommit(false)`, una sola transacción por operación, `commit()` al final y `rollback()` en
  cualquier excepción.
- `PreparedStatement` para todos los valores y `getGeneratedKeys()` para recuperar los `id`
  autoincrementales y asignarlos al `Integer id` del modelo Java.
- `try-with-resources` para cerrar `Connection`, `PreparedStatement` y `ResultSet`.
- No se desactivan claves foráneas (`SET FOREIGN_KEY_CHECKS = 0` está prohibido).
- Los importes se calculan en Java con `BigDecimal` y `setScale(2, RoundingMode.HALF_UP)` y se envían
  a columnas `DECIMAL(12,2)`.

---

## Operación 1. Alta transaccional conjunta de Estación + Ficha + primer Surtidor

**Objetivo.** Evitar el caso D1 de `04_comprobaciones.sql`: una estación creada sin ficha ni surtidor.
Las tres filas deben existir juntas o no existir ninguna.

### Datos de entrada

| Origen | Campos |
| --- | --- |
| `Estacion` | `codigoEstacion`, `nombreComercial`, `direccion`, `telefono` opcional, `fechaApertura` |
| `FichaFacturacion` | `codigoFicha`, `serieTickets`, `siguienteNumero` (por defecto 1), `textoPie` opcional |
| Primer `Surtidor` | `codigoEquipo`, `numeroVisible`, `tipoCombustible`, `estadoServicio` (por defecto `OPERATIVO`) |

### Pasos transaccionales

1. Validar en Java los códigos obligatorios, `numeroVisible > 0`, `siguienteNumero > 0` y fechas.
   Los objetos llegan con `id == null` (`esNueva() == true`).
2. Abrir conexión y desactivar `autoCommit`.
3. Insertar la estación y recuperar su `id` con `getGeneratedKeys()`; asignarlo al objeto.
4. Insertar la ficha con `estacion_id = id` de estación; recuperar su `id`.
5. Insertar el primer surtidor con el mismo `estacion_id`; recuperar su `id`.
6. `commit()`, reactivar `autoCommit` y devolver los tres objetos con sus identificadores.
7. Si falla cualquier paso (por ejemplo, código duplicado o número visible repetido), `rollback()`:
   no queda estación a medias.

### Reglas que debe respetar

- `uq_estacion_codigo`, `uq_ficha_codigo` y `uq_surtidor_codigo` no se pueden repetir.
- `uq_ficha_estacion` impide una segunda ficha para la misma estación.
- `uq_surtidor_numero (estacion_id, numero_visible)` y `chk_surtidor_numero`.
- `fk_ficha_estacion` y `fk_surtidor_estacion` con `ON DELETE RESTRICT ON UPDATE RESTRICT`.
- Toda estación creada por la aplicación tiene exactamente una ficha y al menos un surtidor (R68).
- `DEFAULT CURRENT_TIMESTAMP` de otras tablas no se aplica aquí: la fecha de apertura es obligatoria.

---

## Operación 2. Registro transaccional de Venta completa con líneas, total y cobros múltiples

**Objetivo.** Evitar D6 (venta sin líneas) y D7 (marcar `PAGADA` sin cobrar el total). El total no se
envía calculado por el usuario: se deriva de las líneas.

### Datos de entrada

| Origen | Campos |
| --- | --- |
| Cabecera | `referenciaTicket`, `estacionId`, `empleadoId`, `clienteId` opcional, `fechaHora` |
| Líneas (1..n) | `productoId`, `numeroOrden`, `cantidad` |
| Cobros (0..n) | `importe`, `medioPago`, `referenciaExterna` opcional |

### Pasos transaccionales

1. Validar en Java que hay al menos una línea, que los números de orden son únicos y que la fecha es
   válida. `Venta`, `LineaVenta` y `PagoVenta` llegan con `id == null`.
2. Abrir conexión y desactivar `autoCommit`.
3. Insertar la cabecera de venta con `importe_total = 0.00` y `estado_pago = PENDIENTE`; recuperar
   `venta.id`.
4. Para cada línea:
   1. Leer la modalidad, `precisionPesada` y `precioVenta` vigentes del producto.
   2. Aplicar `producto.validarCantidad(cantidad)` (entero para envasado, múltiplo exacto para granel).
   3. Calcular `importe = cantidad × precioUnitario` con `HALF_UP` a 2 decimales.
   4. Insertar la línea guardando `precio_unitario_aplicado` como copia del precio cobrado.
   5. Acumular el importe en `totalCalculado`.
5. Actualizar `venta.importe_total = totalCalculado`.
6. Para cada cobro, validar `importe > 0` y que la suma acumulada no supere `totalCalculado`; insertar
   el cobro con su `venta_id`.
7. Determinar el estado: `PAGADA` si la suma de cobros iguala el total, `PARCIAL` si es mayor que 0
   pero menor, `PENDIENTE` si no hay cobros. Actualizar `estado_pago`.
8. `commit()` y devolver la venta con sus `id` de cabecera, líneas y cobros. Ante cualquier error,
   `rollback()` completo.

### Reglas que debe respetar

- `uq_venta_ticket` y `uq_linea_orden (venta_id, numero_orden)`.
- `chk_linea_cantidad`, `chk_linea_precio`, `chk_linea_importe`, `chk_venta_importe`,
  `chk_pago_venta_importe`.
- FKs `RESTRICT` a estación, empleado, cliente opcional, producto y venta.
- Precio congelado en la línea: cambios posteriores del catálogo no recalculan tickets (B5).
- Cantidad según modalidad (R75) y al menos una línea (R76), validados en Java.
- Suma de cobros = total y estado coherente (R77/R78), validados en Java.
- `cliente_id = NULL` representa compra anónima; no se inventa un cliente genérico.
- El trigger de precios no interviene: solo reacciona a `UPDATE` de `producto`.

---

## Operación 3. Registro de Devolución parcial con acumulado previo y control de concurrencia

**Objetivo.** Evitar D8: devolver más cantidad o más importe de lo comprado, especialmente cuando dos
empleados registran devoluciones simultáneas sobre la misma línea.

### Datos de entrada

| Origen | Campos |
| --- | --- |
| `Devolucion` | `lineaVentaId`, `empleadoId`, `cantidad`, `motivo`, `aptaParaVenta` (por defecto `false`) |
| Control adicional | Versión/token esperado de la línea o del agregado, o confirmación de reintento tras conflicto |

### Pasos transaccionales

1. Abrir conexión, desactivar `autoCommit` y **bloquear la fila de la línea de venta** con una lectura
   para actualizar (bloqueo pesimista). Alternativamente, usar un token de versión optimista: si la
   versión leída no coincide con la esperada, abortar y pedir reintento.
2. Cargar la venta asociada y comprobar que `estado_pago = 'PAGADA'`.
3. Cargar la línea: `cantidad` comprada, `precio_unitario_aplicado`, `importe` y la modalidad del
   producto para validar la cantidad devuelta (entera o múltiplo de la precisión).
4. Sumar las devoluciones ya registradas para esa línea: `devueltaAcumulada` e
   `importeReintegradoAcumulado`.
5. Validar en Java:
   - `cantidad > 0` y válida según modalidad;
   - `devueltaAcumulada + cantidad <= linea.cantidad`;
   - `importeReintegrado = HALF_UP(cantidad × precio_unitario_aplicado)`;
   - `importeReintegradoAcumulado + importeReintegrado <= linea.importe`;
   - si aplica, `importeReintegrado` no supera el `limiteDevoluciones` de la función `CAJERO`
     vigente del empleado.
6. Insertar la devolución con `importe_reintegrado` calculado y `apta_para_venta` recibido.
7. `commit()`; si hay validación fallida o conflicto de versión/bloqueo, `rollback()` y devolver un
   error claro sin insertar nada.

### Reglas que debe respetar

- `chk_devolucion_cantidad`, `chk_devolucion_importe` y `DEFAULT FALSE` de `apta_para_venta`.
- FKs `RESTRICT` a `linea_venta` y `empleado`.
- Varias devoluciones parciales sucesivas de una misma línea están permitidas; el límite es el
  **acumulado**, no una única fila (A10 y D8 de `04`).
- El importe se calcula con el precio cobrado en la línea, nunca con `producto.precio_venta` actual.
- Redondeo `HALF_UP` a 2 decimales.
- Solo sobre ventas `PAGADA`.
- Concurrencia: dos operaciones sobre la misma línea no pueden superar juntas el stock devuelto; el
  bloqueo o el token optimista evita la condición de carrera.
- No se modifica `venta.importe_total` ni `estado_pago` por una devolución.

## 4. Orden recomendado de implementación en Fase 2

1. Utilidad de configuración JDBC y transacciones (común a las tres).
2. Operación 1 (alta conjunta) porque crea la base coherente de estaciones.
3. Operación 2 (venta completa) porque alimenta líneas y cobros.
4. Operación 3 (devolución) porque depende de ventas pagadas y de control de concurrencia.

Las reglas pendientes R68–R86 de `docs/03_reglas_negocio_y_evidencias.md` se irán tachando a medida
que estas operaciones y sus validaciones se implementen.
