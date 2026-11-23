# 02. Diseño Java y correspondencia con la base de datos

Paquete del modelo: `org.example.model` (34 clases/enums/abstractas). Paquete de la aplicación
existente y de la demo: `org.example`. **No hay JDBC ni DAO en el modelo**: no hay `Connection`,
`PreparedStatement`, `ResultSet` ni sentencias `INSERT/SELECT/UPDATE/DELETE`. `AlmacenamientoSQLite`
se conserva intacto para la Fase 2.

## 1. Decisiones de diseño

### 1.1 Herencia en `Producto`

`Producto` es una clase abstracta que concentra los datos comunes (`referenciaComercial`,
`descripcion`, `precioVenta`, `estadoComercializacion`, acuerdos) y declara dos operaciones
polimórficas:

- `validarCantidad(BigDecimal)`: cada subclase decide qué cantidad es válida.
- `calcularImporte(BigDecimal)`: delega en `importeRedondeado`, común a ambas.

| Subclase | Atributo propio | Regla de cantidad | Equivalente en SQL |
| --- | --- | --- | --- |
| `ProductoEnvasado` | `formatoEnvase` | Entero > 0; `setScale(0, UNNECESSARY)` rechaza `2.5` | `producto.modalidad='ENVASADO'` y `formato_envase NOT NULL` |
| `ProductoGranel` | `precisionPesada` | Decimal > 0 y `remainder(precisionPesada) == 0` | `producto.precision_pesada` con `0 < p <= 1` |

`ModalidadProducto` es un `enum` inmutable para que la modalidad no cambie durante la
comercialización. En SQL, `CHECK` garantiza que los atributos propios existan y que los ajenos sean
`NULL`, pero **no puede comparar la cantidad de una línea con la modalidad del producto**: por eso la
validación se hace en Java y se trasladará a JDBC en Fase 2.

`crearRegistroCambio(precioAnterior)` construye en memoria un `CambioPrecioHistorial`, pero no lo
inserta: en la BD lo genera el trigger `trg_producto_precio_cambiado`. Es solo la representación del
hecho para el modelo.

### 1.2 Composición + herencia en `Empleado` / `AsignacionFuncion`

Se descartó una clase `Empleado` con campos `esCajero`, `esExpendedor`, etc., porque un empleado puede
tener **varias funciones a la vez** y **recuperar una función tras un periodo cerrado**.

- `Empleado` contiene `List<AsignacionFuncion> asignaciones` (**composición**).
- `AsignacionFuncion` es abstracta y aporta `empleado`, `tipoFuncion`, `fechaInicio`, `fechaFin`,
  `estaActivaEn(LocalDate)`, `cerrar(...)` y el método polimórfico `getDetalle()`.
- Subclases: `AsignacionCajero` (`limiteDevoluciones`), `AsignacionExpendedor`
  (`numeroAcreditacion`, `caducidadAcreditacion`), `AsignacionResponsableTienda`
  (`presupuestoCompras`) y `AsignacionJefeEstacion` (`nivelAutorizacion`).
- `getFuncionesActivasEn(fecha)` devuelve todas las vigentes en una fecha, no solo una.

Esto corresponde a `historial_funcion`: una fila por periodo, `UNIQUE (empleado_id, tipo_funcion,
periodo_activo)` para evitar dos periodos abiertos de la misma función y `CHECK` para los atributos
propios. La supervisión es una referencia aparte (`Empleado.supervisor` / `empleado.supervisor_id`):
no se modela como una función, porque **supervisor no equivale a jefe de estación**.

### 1.3 `Integer id` nullable antes de persistir

Todas las entidades heredan de `Entidad`:

```java
private Integer id;
public boolean esNueva() { return id == null; }
```

- Se usa `Integer` (no `int`) porque un objeto recién creado aún no tiene identificador en la BD.
- `setId(Integer)` permite asignar el valor generado por `AUTO_INCREMENT` en Fase 2, típicamente con
  `getGeneratedKeys()`.
- Ningún código de negocio (`codigoEstacion`, `numeroInterno`, `referenciaComercial`, ...) se usa como
  PK: estos son `UNIQUE NOT NULL` en SQL.
- `esNueva()` facilitará decidir entre inserción y actualización sin consultar primero.

### 1.4 Importes: `BigDecimal` y `RoundingMode.HALF_UP`

`Producto.importeRedondeado(cantidad)` hace:

```java
validarCantidad(cantidad)
    .multiply(precioVenta)
    .setScale(2, RoundingMode.HALF_UP);
```

Razones:

- `double` introduciría errores binarios en importes monetarios; las columnas SQL son `DECIMAL(12,2)`.
- `HALF_UP` es el criterio comercial esperado (0,005 → 0,01), distinto del `HALF_EVEN` por defecto.
- El redondeo se aplica **una sola vez**, al importe final de cada línea, no al precio unitario.
- `Venta.getTotal()`, `getTotalPagado()` y `getPendiente()` vuelven a fijar escala 2 para evitar
  arrastres al sumar.
- `Devolucion.getImporte()` usa el precio de la línea (`precioUnitario`), nunca el precio actual del
  catálogo; así un cambio de precio no altera tickets antiguos (comprobado en `04_comprobaciones.sql`,
  bloque B5).

### 1.5 Convivencia de `Pagos` legado con `PagoVenta`

| Aspecto | `org.example.Pagos` (legado) | `org.example.model.PagoVenta` (Fase 1) |
| --- | --- | --- |
| Origen | Clase original usada por CSV/JSON y SQLite | Nuevo modelo de dominio |
| PK | `int id` | `Integer id` heredado de `Entidad` |
| Cliente | `int id_cliente` como valor plano | Referencia a `Venta`, que a su vez referencia a `Empleado`/`Estacion`/`Cliente` |
| Importe | `int` | `BigDecimal` |
| Fecha | `String dd/MM/yyyy` | `LocalDateTime` |
| Tabla SQL | `pago_combustible_legado` | `pago_venta` |
| FK | Ninguna (`id_cliente_original` sin reinterpretar) | `venta_id → venta.id` |

La clase `PagoCombustibleLegado` del modelo es el puente: guarda el `org.example.Cliente` original,
la estación nueva, la fecha como `LocalDate`, `importe` como `BigDecimal` y `litros` como `int`.
No sustituye ni modifica `Pagos`.

**CSV y JSON siguen funcionando igual.** `AlmacenamientoCSV`, `AlmacenamientoJson`,
`MigraCSVToJson` y `Datos/*` no se han tocado. `DemoModelo` pertenece a `org.example` para poder
redirigir los campos package-private `clientes`, `pagos` y `directorio` a un directorio temporal y
verificar lectura/escritura sin sobrescribir los datos reales. El paquete `org.example.model` tiene su
propia clase `Cliente` (con `Integer id` y referencias), que no se usa en esa verificación: allí se
emplea explícitamente `org.example.Cliente`.

## 2. Correspondencia modelo Java ↔ tablas SQL

| Clase Java | Tabla SQL | Observación |
| --- | --- | --- |
| `Entidad` | — | Base con `Integer id`; no se persiste directamente |
| `Validaciones` | — | Utilidad de dominio |
| `Estacion` | `estacion` | Agrega surtidores, empleados, ventas e incidencias |
| `FichaFacturacion` | `ficha_facturacion` | `setFicha` mantiene la relación 1:1 en memoria |
| `Surtidor` | `surtidor` | `numeroVisible` único por estación |
| `Empleado` | `empleado` | `supervisor` es otra referencia a `Empleado` |
| `AsignacionFuncion` + 4 subclases | `historial_funcion` | Herencia + composición |
| `TipoFuncion` | `historial_funcion.tipo_funcion` | `enum` |
| `Cliente` | `cliente` | Clase del modelo, distinta de `org.example.Cliente` |
| `TarjetaFidelizacion` | `tarjeta_fidelizacion` | `asignar` respeta fecha de emisión |
| `Producto` (abstracta) | `producto` | `modalidad` estable |
| `ProductoEnvasado` | `producto` (`ENVASADO`) | `formatoEnvase` |
| `ProductoGranel` | `producto` (`GRANEL`) | `precisionPesada` |
| `ModalidadProducto`, `EstadoComercializacion` | `producto.modalidad`, `producto.estado_comercializacion` | `enum` |
| `CambioPrecioHistorial` | `producto_precio_historial` | Solo se crea en memoria; lo inserta el trigger |
| `Proveedor` | `proveedor` | Lista de acuerdos |
| `AcuerdoSuministro` | `proveedor_producto` | Referencias a proveedor y producto |
| `Campana` | `campana` | Valida periodo y límite de inscripciones |
| `InscripcionCampana` | `campana_cliente` | Referencias a campaña y cliente |
| `Venta` | `venta` | Total calculado desde líneas |
| `LineaVenta` | `linea_venta` | Fija cantidad, precio unitario e importe |
| `PagoVenta` | `pago_venta` | `MedioPago` como `enum` |
| `Devolucion` | `devolucion` | Valida cantidad contra la línea |
| `Incidencia` | `incidencia` | `EstadoIncidencia` como `enum` |
| `PagoCombustibleLegado` | `pago_combustible_legado` | Puente con `org.example.Pagos` |
| `EstadoPago`, `MedioPago`, `EstadoIncidencia`, `EstadoServicioSurtidor` | columnas `ENUM` | `enum` de dominio |

## 3. Verificación realizada

- Compilación de todo `src/main/java` con `javac -encoding UTF-8 -cp <sqlite-jdbc.jar> -d target/classes`.
- Ejecución de `java -cp "target/classes;<sqlite-jdbc.jar>" org.example.DemoModelo` sin errores.
- Revisión del paquete `org.example.model`: 0 sentencias SQL y 0 llamadas JDBC (solo comentarios
  explicativos que describen el trigger de MySQL).
- `DemoModelo` verifica round-trip CSV y JSON en carpeta temporal, sin tocar `Datos/`.
