# 01. Modelo relacional y diccionario de datos

Base de datos: `gasolinera_mysql` (`utf8mb4` / `utf8mb4_unicode_ci`, motor **InnoDB** en las 19 tablas).
Fuente: `sql/01_esquema.sql`.

**Este documento no incluye ningún diagrama entidad-relación.** La estructura se describe en notación
textual y en diccionarios por tabla.

## 1. Notación textual

- `A.id → B.id [N:1]` significa que varias filas de `A` pueden referenciar una fila de `B`.
- `RESTRICT` impide borrar/actualizar el padre si existen hijos.
- `SET NULL` deja la FK del hijo a `NULL` si desaparece el padre (solo en columnas `NULL`).
- `UQ(...)` = restricción `UNIQUE`; `CK(...)` = `CHECK`; `PK` = clave primaria técnica autoincremental.
- Todas las PK son `INT NOT NULL AUTO_INCREMENT`. Ningún código de negocio es PK.
- Los códigos de negocio son `VARCHAR ... NOT NULL` con `UNIQUE`.

### Asociaciones textuales

1. `ficha_facturacion.estacion_id → estacion.id [1:1, UQ, RESTRICT/RESTRICT]`.
2. `surtidor.estacion_id → estacion.id [N:1, RESTRICT/RESTRICT]`.
3. `empleado.estacion_id → estacion.id [N:1, RESTRICT/RESTRICT]`.
4. `empleado.supervisor_id → empleado.id [N:1 opcional, SET NULL/RESTRICT]` (self-FK).
5. `historial_funcion.empleado_id → empleado.id [N:1, RESTRICT/RESTRICT]`.
6. `tarjeta_fidelizacion.cliente_id → cliente.id [1:1 opcional, SET NULL/RESTRICT]`.
7. `producto_precio_historial.producto_id → producto.id [N:1, RESTRICT/RESTRICT]`.
8. `proveedor_producto.proveedor_id → proveedor.id [N:1, RESTRICT/RESTRICT]`.
9. `proveedor_producto.producto_id → producto.id [N:1, RESTRICT/RESTRICT]`.
10. `campana_cliente.campana_id → campana.id [N:1, RESTRICT/RESTRICT]`.
11. `campana_cliente.cliente_id → cliente.id [N:1, RESTRICT/RESTRICT]`.
12. `venta.estacion_id → estacion.id [N:1, RESTRICT/RESTRICT]`.
13. `venta.empleado_id → empleado.id [N:1, RESTRICT/RESTRICT]`.
14. `venta.cliente_id → cliente.id [N:1 opcional, RESTRICT/RESTRICT]`.
15. `linea_venta.venta_id → venta.id [N:1, RESTRICT/RESTRICT]`.
16. `linea_venta.producto_id → producto.id [N:1, RESTRICT/RESTRICT]`.
17. `pago_venta.venta_id → venta.id [N:1, RESTRICT/RESTRICT]`.
18. `devolucion.linea_venta_id → linea_venta.id [N:1, RESTRICT/RESTRICT]`.
19. `devolucion.empleado_id → empleado.id [N:1, RESTRICT/RESTRICT]`.
20. `incidencia.estacion_id → estacion.id [N:1, RESTRICT/RESTRICT]`.
21. `incidencia.empleado_responsable_id → empleado.id [N:1 opcional, SET NULL/RESTRICT]`.
22. `pago_combustible_legado` **no tiene FK**: `id_cliente_original` conserva el valor de la clase
    `Pagos` sin reinterpretarlo.

`ON UPDATE RESTRICT` aparece en todas las FK (excepto las de `SET NULL`, que también usan
`ON UPDATE RESTRICT`). La PK nunca se actualiza, pero la política queda declarada explícitamente.

## 2. Diccionario de datos

### 2.1 `estacion`

**Finalidad.** Registrar las estaciones de servicio y sus datos de identidad.

**PK:** `id`. **UNIQUE:** `uq_estacion_codigo (codigo_estacion)`. **FK:** ninguna. **CHECK:** ninguno.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `codigo_estacion` | `VARCHAR(20)` | NO | — | `uq_estacion_codigo`; código interno de la empresa |
| `nombre_comercial` | `VARCHAR(120)` | NO | — | Nombre público |
| `direccion` | `VARCHAR(200)` | NO | — | Dirección postal |
| `telefono` | `VARCHAR(30)` | SÍ | `NULL` | Contacto opcional |
| `fecha_apertura` | `DATE` | NO | — | Fecha de apertura |

### 2.2 `ficha_facturacion`

**Finalidad.** Configuración de facturación de una estación (relación 1:1 estricta).

**PK:** `id`. **UNIQUE:** `uq_ficha_codigo (codigo_ficha)`, `uq_ficha_estacion (estacion_id)`.
**FK:** `fk_ficha_estacion → estacion.id ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_ficha_siguiente_numero (siguiente_numero > 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `codigo_ficha` | `VARCHAR(30)` | NO | — | Código que usa soporte |
| `estacion_id` | `INT` | NO | — | FK; `UNIQUE` garantiza una sola ficha por estación |
| `serie_tickets` | `VARCHAR(10)` | NO | — | Serie de numeración |
| `siguiente_numero` | `INT` | NO | `1` | Debe ser > 0 |
| `texto_pie` | `TEXT` | SÍ | `NULL` | Pie de ticket opcional |

### 2.3 `surtidor`

**Finalidad.** Equipos de repostaje de cada estación.

**PK:** `id`. **UNIQUE:** `uq_surtidor_codigo (codigo_equipo)`,
`uq_surtidor_numero (estacion_id, numero_visible)`.
**FK:** `fk_surtidor_estacion → estacion.id ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_surtidor_numero (numero_visible > 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `codigo_equipo` | `VARCHAR(30)` | NO | — | Código único en toda la empresa |
| `estacion_id` | `INT` | NO | — | FK a la estación |
| `numero_visible` | `INT` | NO | — | Único dentro de la estación; > 0 |
| `tipo_combustible` | `VARCHAR(30)` | NO | — | Tipo atendido |
| `estado_servicio` | `ENUM('OPERATIVO','AVERIADO','FUERA_SERVICIO')` | NO | `'OPERATIVO'` | Estado de servicio |

### 2.4 `empleado`

**Finalidad.** Personal de las estaciones, con supervisor actual opcional.

**PK:** `id`. **UNIQUE:** `uq_empleado_numero (numero_interno)`,
`uq_empleado_documento (documento_identidad)`.
**FK:** `fk_empleado_estacion → estacion.id ON DELETE RESTRICT ON UPDATE RESTRICT`;
`fk_empleado_supervisor → empleado.id ON DELETE SET NULL ON UPDATE RESTRICT`.
**CHECK:** ninguno (ver nota de compatibilidad).

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `numero_interno` | `VARCHAR(20)` | NO | — | Código de negocio, `UNIQUE` |
| `documento_identidad` | `VARCHAR(30)` | NO | — | `UNIQUE` |
| `nombre` | `VARCHAR(120)` | NO | — | Nombre completo |
| `telefono` | `VARCHAR(30)` | SÍ | `NULL` | Contacto opcional |
| `email` | `VARCHAR(120)` | SÍ | `NULL` | Contacto opcional |
| `fecha_incorporacion` | `DATE` | NO | — | Alta en la empresa |
| `estacion_id` | `INT` | NO | — | FK; adscripción actual |
| `supervisor_id` | `INT` | SÍ | `NULL` | Self-FK; **no equivale a jefe de estación** |

> MariaDB/XAMPP rechaza con **error 1901** un `CHECK` que mencione una columna con `FOREIGN KEY`,
> por lo que no existe `chk_empleado_no_self_supervisor`. La autosupervisión y los ciclos se validarán
> en Java en Fase 2. Tampoco se usa `DEFAULT (CURRENT_DATE)` con paréntesis.

### 2.5 `historial_funcion`

**Finalidad.** Periodos de las funciones de cada empleado; permite simultaneidad de funciones
distintas e historial de una misma función.

**PK:** `id`. **UNIQUE:** `uq_funcion_activa (empleado_id, tipo_funcion, periodo_activo)`.
**FK:** `fk_funcion_empleado → empleado.id ON DELETE RESTRICT ON UPDATE RESTRICT`.

**CHECK:**
`chk_funcion_fechas`, `chk_funcion_cajero`, `chk_funcion_expendedor`, `chk_funcion_responsable`,
`chk_funcion_jefe`, `chk_funcion_solo_atributos_propios`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `empleado_id` | `INT` | NO | — | FK al empleado |
| `tipo_funcion` | `ENUM('CAJERO','EXPENDEDOR','RESPONSABLE_TIENDA','JEFE_ESTACION')` | NO | — | Tipo de función |
| `fecha_inicio` | `DATE` | NO | — | Inicio del periodo |
| `fecha_fin` | `DATE` | SÍ | `NULL` | `NULL` = periodo abierto; debe ser ≥ inicio |
| `limite_devoluciones` | `DECIMAL(12,2)` | SÍ | `NULL` | Solo `CAJERO`: obligatorio y ≥ 0 |
| `numero_acreditacion` | `VARCHAR(30)` | SÍ | `NULL` | Solo `EXPENDEDOR`: obligatorio |
| `caducidad_acreditacion` | `DATE` | SÍ | `NULL` | Solo `EXPENDEDOR`: obligatoria |
| `presupuesto_compras` | `DECIMAL(12,2)` | SÍ | `NULL` | Solo `RESPONSABLE_TIENDA`: obligatorio y ≥ 0 |
| `nivel_autorizacion` | `VARCHAR(20)` | SÍ | `NULL` | Solo `JEFE_ESTACION`: obligatorio |
| `periodo_activo` | `INT GENERATED ALWAYS AS (IF(fecha_fin IS NULL, 1, NULL)) STORED` | SÍ | calculada | Columna generada: evita dos periodos abiertos de la misma función |

Los atributos de otras funciones deben ser `NULL` (`chk_funcion_solo_atributos_propios`).
El solapamiento de **periodos cerrados** queda pendiente de Java en Fase 2.

### 2.6 `cliente`

**Finalidad.** Clientes registrados.

**PK:** `id`. **UNIQUE:** `uq_cliente_codigo (codigo_cliente)`. **FK:** ninguna. **CHECK:** ninguno.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `codigo_cliente` | `VARCHAR(20)` | NO | — | Código de negocio, `UNIQUE` |
| `nombre` | `VARCHAR(120)` | NO | — | Nombre |
| `telefono` | `VARCHAR(30)` | SÍ | `NULL` | Contacto opcional |
| `email` | `VARCHAR(120)` | SÍ | `NULL` | Contacto opcional |
| `direccion` | `VARCHAR(200)` | SÍ | `NULL` | Contacto opcional |

Solo código y nombre son obligatorios. Exigir al menos un medio de contacto está previsto para Fase 2.

### 2.7 `tarjeta_fidelizacion`

**Finalidad.** Tarjetas emitidas, con o sin titular asignado.

**PK:** `id`. **UNIQUE:** `uq_tarjeta_numero (numero_tarjeta)`, `uq_tarjeta_cliente (cliente_id)`.
**FK:** `fk_tarjeta_cliente → cliente.id ON DELETE SET NULL ON UPDATE RESTRICT`.
**CHECK:** `chk_tarjeta_fechas (fecha_asignacion IS NULL OR fecha_asignacion >= fecha_emision)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `numero_tarjeta` | `VARCHAR(30)` | NO | — | `UNIQUE` |
| `fecha_emision` | `DATE` | NO | — | Emisión |
| `cliente_id` | `INT` | SÍ | `NULL` | Titular; `UNIQUE` permite tarjetas sin entregar |
| `fecha_asignacion` | `DATE` | SÍ | `NULL` | ≥ `fecha_emision` si existe |

### 2.8 `producto`

**Finalidad.** Catálogo de productos, con modalidad estable.

**PK:** `id`. **UNIQUE:** `uq_producto_referencia (referencia_comercial)`.
**FK:** ninguna. **CHECK:** `chk_producto_precio`, `chk_producto_envase`, `chk_producto_granel`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `referencia_comercial` | `VARCHAR(50)` | NO | — | Referencia de negocio, `UNIQUE` |
| `descripcion` | `VARCHAR(200)` | NO | — | Descripción |
| `precio_venta` | `DECIMAL(12,2)` | NO | — | > 0; redondeo Java `HALF_UP` |
| `estado_comercializacion` | `ENUM('ACTIVO','RETIRADO')` | NO | `'ACTIVO'` | Retirado no se borra |
| `modalidad` | `ENUM('ENVASADO','GRANEL')` | NO | — | Estable en Fase 2 |
| `formato_envase` | `VARCHAR(50)` | SÍ | `NULL` | Obligatorio solo si `ENVASADO`; prohibido en `GRANEL` |
| `precision_pesada` | `DECIMAL(6,3)` | SÍ | `NULL` | Solo `GRANEL`: `0 < p <= 1` kg; prohibida en `ENVASADO` |

### 2.9 `producto_precio_historial`

**Finalidad.** Registro automático de cambios reales de precio (lo escribe el trigger).

**PK:** `id`. **UNIQUE:** ninguna. **FK:**
`fk_historial_precio_producto → producto.id ON DELETE RESTRICT ON UPDATE RESTRICT`. **CHECK:** ninguno.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `producto_id` | `INT` | NO | — | FK al producto |
| `precio_anterior` | `DECIMAL(12,2)` | NO | — | Valor de `OLD.precio_venta` |
| `precio_nuevo` | `DECIMAL(12,2)` | NO | — | Valor de `NEW.precio_venta` |
| `fecha_cambio` | `DATETIME` | NO | `CURRENT_TIMESTAMP` | El trigger usa `NOW()` explícitamente |

No se edita desde la aplicación: en Fase 2 se impedirá escribir esta tabla desde Java.

### 2.10 `proveedor`

**Finalidad.** Proveedores del catálogo.

**PK:** `id`. **UNIQUE:** `uq_proveedor_codigo (codigo_proveedor)`,
`uq_proveedor_fiscal (identificacion_fiscal)`. **FK:** ninguna. **CHECK:** ninguno.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `codigo_proveedor` | `VARCHAR(20)` | NO | — | Código de negocio |
| `identificacion_fiscal` | `VARCHAR(30)` | NO | — | CIF/NIF, `UNIQUE` |
| `razon_social` | `VARCHAR(150)` | NO | — | Razón social |
| `telefono` | `VARCHAR(30)` | SÍ | `NULL` | Contacto opcional |
| `email` | `VARCHAR(120)` | SÍ | `NULL` | Contacto opcional |
| `direccion` | `VARCHAR(200)` | SÍ | `NULL` | Contacto opcional |

### 2.11 `proveedor_producto`

**Finalidad.** Acuerdos de suministro (asociación N:M proveedor-producto).

**PK:** `id`. **UNIQUE:** `uq_proveedor_producto (proveedor_id, producto_id)`.
**FK:** `fk_acuerdo_proveedor → proveedor.id ON DELETE RESTRICT ON UPDATE RESTRICT`;
`fk_acuerdo_producto → producto.id ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_acuerdo_precio (precio_compra > 0)`, `chk_acuerdo_plazo (plazo_entrega_dias >= 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `proveedor_id` | `INT` | NO | — | FK al proveedor |
| `producto_id` | `INT` | NO | — | FK al producto |
| `referencia_proveedor` | `VARCHAR(50)` | NO | — | Referencia usada por el proveedor |
| `precio_compra` | `DECIMAL(12,2)` | NO | — | > 0 |
| `plazo_entrega_dias` | `INT` | NO | — | ≥ 0 |

No se guarda historial de condiciones: se actualiza la misma fila. Evitar aislamientos se traslada a Fase 2.

### 2.12 `campana`

**Finalidad.** Campañas promocionales.

**PK:** `id`. **UNIQUE:** `uq_campana_codigo (codigo_campana)`. **FK:** ninguna.
**CHECK:** `chk_campana_fechas (fecha_fin >= fecha_inicio)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `codigo_campana` | `VARCHAR(20)` | NO | — | Código de negocio |
| `nombre` | `VARCHAR(120)` | NO | — | Nombre de campaña |
| `descripcion` | `TEXT` | SÍ | `NULL` | Descripción opcional |
| `fecha_inicio` | `DATE` | NO | — | Inicio |
| `fecha_fin` | `DATE` | NO | — | ≥ inicio |

### 2.13 `campana_cliente`

**Finalidad.** Inscripciones de clientes en campañas.

**PK:** `id`. **UNIQUE:** `uq_campana_cliente (campana_id, cliente_id)`.
**FK:** `fk_inscripcion_campana → campana.id ON DELETE RESTRICT ON UPDATE RESTRICT`;
`fk_inscripcion_cliente → cliente.id ON DELETE RESTRICT ON UPDATE RESTRICT`. **CHECK:** ninguno.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `campana_id` | `INT` | NO | — | FK a campaña |
| `cliente_id` | `INT` | NO | — | FK a cliente |
| `fecha_inscripcion` | `DATETIME` | NO | `CURRENT_TIMESTAMP` | Sin paréntesis (compatibilidad MariaDB) |

Que la inscripción caiga dentro del periodo de la campaña no está garantizado por SQL: se valida en el
modelo Java y se trasladará a la capa JDBC en Fase 2.

### 2.14 `venta`

**Finalidad.** Tickets de tienda; guarda la estación del momento para no depender de cambios posteriores.

**PK:** `id`. **UNIQUE:** `uq_venta_ticket (referencia_ticket)`.
**FK:** `fk_venta_estacion → estacion.id`, `fk_venta_empleado → empleado.id`,
`fk_venta_cliente → cliente.id`, todas `ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_venta_importe (importe_total >= 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `referencia_ticket` | `VARCHAR(40)` | NO | — | Número de ticket, `UNIQUE` |
| `estacion_id` | `INT` | NO | — | Estación que emite el ticket |
| `empleado_id` | `INT` | NO | — | Cajero que registra |
| `cliente_id` | `INT` | SÍ | `NULL` | `NULL` = compra anónima |
| `fecha_hora` | `DATETIME` | NO | `CURRENT_TIMESTAMP` | Fecha/hora de la venta |
| `importe_total` | `DECIMAL(12,2)` | NO | `0.00` | ≥ 0; se recalcula desde líneas en Fase 2 |
| `estado_pago` | `ENUM('PENDIENTE','PARCIAL','PAGADA')` | NO | `'PENDIENTE'` | Coherencia con pagos: pendiente Java |

### 2.15 `linea_venta`

**Finalidad.** Líneas de cada ticket, con precio y importe congelados en el momento de la venta.

**PK:** `id`. **UNIQUE:** `uq_linea_orden (venta_id, numero_orden)`.
**FK:** `fk_linea_venta → venta.id`, `fk_linea_producto → producto.id`, ambas
`ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_linea_cantidad (cantidad > 0)`, `chk_linea_precio (precio_unitario_aplicado > 0)`,
`chk_linea_importe (importe >= 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `venta_id` | `INT` | NO | — | FK a la venta |
| `producto_id` | `INT` | NO | — | FK al producto |
| `numero_orden` | `INT` | NO | — | Único dentro de la venta |
| `cantidad` | `DECIMAL(10,3)` | NO | — | > 0; modalidad la valida Java en Fase 2 |
| `precio_unitario_aplicado` | `DECIMAL(12,2)` | NO | — | Copia del precio cobrado |
| `importe` | `DECIMAL(12,2)` | NO | — | ≥ 0; `cantidad × precio`, `HALF_UP` en Java |

### 2.16 `pago_venta`

**Finalidad.** Cobros de una venta de tienda. **No es** el pago de repostaje heredado.

**PK:** `id`. **UNIQUE:** ninguna. **FK:**
`fk_pago_venta → venta.id ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_pago_venta_importe (importe > 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `venta_id` | `INT` | NO | — | FK a la venta |
| `importe` | `DECIMAL(12,2)` | NO | — | > 0 |
| `fecha_hora` | `DATETIME` | NO | `CURRENT_TIMESTAMP` | Momento del cobro |
| `medio_pago` | `ENUM('EFECTIVO','TARJETA','TRANSFERENCIA','OTRO')` | NO | — | Medio de cobro |
| `referencia_externa` | `VARCHAR(50)` | SÍ | `NULL` | Referencia de tarjeta/transferencia |

La suma de cobros frente a `importe_total` se validará en Java en Fase 2.

### 2.17 `devolucion`

**Finalidad.** Devoluciones parciales de una línea concreta, con importe reintegrado.

**PK:** `id`. **UNIQUE:** ninguna (se permiten varias devoluciones por línea).
**FK:** `fk_devolucion_linea → linea_venta.id`, `fk_devolucion_empleado → empleado.id`, ambas
`ON DELETE RESTRICT ON UPDATE RESTRICT`.
**CHECK:** `chk_devolucion_cantidad (cantidad > 0)`, `chk_devolucion_importe (importe_reintegrado >= 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `linea_venta_id` | `INT` | NO | — | FK a la línea devuelta |
| `empleado_id` | `INT` | NO | — | Empleado que autoriza |
| `cantidad` | `DECIMAL(10,3)` | NO | — | > 0; acumulado ≤ comprado: Java Fase 2 |
| `motivo` | `VARCHAR(255)` | NO | — | Motivo obligatorio |
| `fecha_hora` | `DATETIME` | NO | `CURRENT_TIMESTAMP` | Momento de la devolución |
| `importe_reintegrado` | `DECIMAL(12,2)` | NO | — | ≥ 0; calculado con el precio de la línea |
| `apta_para_venta` | `BOOLEAN` | NO | `FALSE` | Indica si puede volver a la venta |

### 2.18 `incidencia`

**Finalidad.** Incidencias de trabajo detectadas en una estación.

**PK:** `id`. **UNIQUE:** `uq_incidencia_referencia (referencia_incidencia)`.
**FK:** `fk_incidencia_estacion → estacion.id ON DELETE RESTRICT ON UPDATE RESTRICT`;
`fk_incidencia_responsable → empleado.id ON DELETE SET NULL ON UPDATE RESTRICT`. **CHECK:** ninguno.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `referencia_incidencia` | `VARCHAR(30)` | NO | — | Código de negocio |
| `estacion_id` | `INT` | NO | — | Estación donde se detecta |
| `descripcion` | `TEXT` | NO | — | Descripción obligatoria |
| `fecha_apertura` | `DATETIME` | NO | `CURRENT_TIMESTAMP` | Apertura |
| `estado` | `ENUM('ABIERTA','EN_CURSO','RESUELTA','CERRADA')` | NO | `'ABIERTA'` | Estado del seguimiento |
| `empleado_responsable_id` | `INT` | SÍ | `NULL` | Opcional al abrir; sin `UNIQUE` (varios permitidos) |

### 2.19 `pago_combustible_legado`

**Finalidad.** Conservar sin reinterpretar los pagos de repostaje de la aplicación CSV/JSON anterior.

**PK:** `id`. **UNIQUE:** ninguna. **FK:** **ninguna**.
**CHECK:** `chk_legado_importe (importe >= 0)`, `chk_legado_litros (litros >= 0)`.

| Columna | Tipo | Nulo | Default | Restricciones / observaciones |
| --- | --- | --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | NO | — | PK técnica |
| `id_cliente_original` | `INT` | SÍ | `NULL` | Valor original de `Pagos.id_cliente`; **no es FK** |
| `fecha` | `VARCHAR(10)` | SÍ | `NULL` | Igual que `Pagos.fecha`: `dd/MM/yyyy` |
| `importe` | `INT` | NO | — | Entero, como en la clase antigua |
| `litros` | `INT` | NO | — | Entero, como en la clase antigua |
| `combustible` | `VARCHAR(30)` | NO | — | Texto del legado |

Se mantiene separado de `pago_venta`: este último usa `DECIMAL(12,2)`, FK a `venta` y `medio_pago`.
