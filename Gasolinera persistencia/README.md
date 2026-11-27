# Gasolinera con persistencia — Contexto unificado (Fase 1)

> Guía única para agentes y continuidad de la práctica. **No existe una clase `Gasolinera`**: es el nombre del proyecto; el modelo de datos legacy son `Cliente` y `Pagos`.  
> Rutas relativas a esta carpeta (`Gasolinera persistencia/`), salvo que se indique lo contrario.

## 1. Alcance y estado

| Ruta | Qué es | Regla |
| --- | --- | --- |
| `.` | Práctica Maven (`org.example`) | Aquí se trabaja la entrega. |
| `../Tarea1/` | Entrega anterior CSV/JSON, sin SQLite | Referencia histórica; no modificar. |
| `../RA2-JDBC/` | Material docente JDBC (Jardín Botánico, SQLite/MySQL) | Referencia de sintaxis/versiones; no modificar. |

**Conviven dos mundos:**
- **App anterior (intacta):** `Main`, `Gestor`, `Cliente`, `Pagos`, `I_Almacenamiento`, `AlmacenamientoCSV`, `AlmacenamientoJson`, `AlmacenamientoSQLite` y `Datos/`.
- **Fase 1 (nueva):** scripts SQL en `sql/`, modelo de dominio en `src/main/java/org/example/model/` y demo `org.example.DemoModelo`.

> **Fase 1 = diseño/preparación.** No hay DAO ni consultas JDBC nuevas. `AlmacenamientoSQLite` se conserva tal cual para Fase 2. El modelo Java solo trabaja en memoria.

## 2. Requisitos y configuración

| Herramienta | Versión / detalle |
| --- | --- |
| JDK | 21 (`pom.xml`); verificado también con JDK 26 |
| SQL | MySQL 8.4+ o MariaDB 10.4+ (XAMPP), InnoDB, `utf8mb4` |
| SQLite | `org.xerial:sqlite-jdbc:3.49.1.0` (necesario para compilar la app anterior) |
| Cliente SQL | `mysql` CLI, phpMyAdmin o IntelliJ Data Sources |
| Maven | Opcional; se aporta alternativa con `javac` |

**Configuración por variables de entorno (sin secretos ni rutas personales):**

```dotenv
DB_URL=jdbc:mysql://localhost:3306/gasolinera_mysql
DB_USER=gasolinera_app
DB_PASSWORD=<valor_generado_por_cada_usuario>
SQLITE_JAR=<ruta_local_al_jar_sqlite-jdbc-3.49.1.0>  # solo para compilar app anterior
```

También se puede usar `DB_HOST`, `DB_PORT`, `DB_NAME`; la BD debe llamarse `gasolinera_mysql` porque los scripts la usan.

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/gasolinera_mysql'
$env:DB_USER = 'gasolinera_app'
$env:DB_PASSWORD = '<valor_generado_por_cada_usuario>'
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/01_esquema.sql
```

## 3. Scripts SQL (ejecutar fuera de Java)

Orden desde `Gasolinera persistencia/`:

```powershell
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/01_esquema.sql
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/02_triggers.sql
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/03_datos.sql
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/04_comprobaciones.sql
```

| Orden | Script | Qué hace | Precaución |
| --- | --- | --- | --- |
| 1 | `01_esquema.sql` | Crea BD y 19 tablas con PK AI, UNIQUE, FK, CHECK | Empieza con `DROP DATABASE IF EXISTS gasolinera_mysql`: **borra carga anterior** |
| 2 | `02_triggers.sql` | Crea `trg_producto_precio_cambiado` para historial de precios | Después de crear `producto` |
| 3 | `03_datos.sql` | Datos de ejemplo coherentes (19 tablas, 6 ventas, 12 líneas) | No desactiva claves foráneas |
| 4 | `04_comprobaciones.sql` | A: verificaciones; B: pruebas trigger; C: errores esperados comentados; D: reglas pendientes para Java Fase 2 | C comentado; B modifica `PROD-001` y deshace `PROD-002` con `ROLLBACK` |

Detalles: `04` tiene 46 `SELECT`, 4 pruebas de trigger (incluido `ROLLBACK`), C1–C15 errores esperados comentados (UNIQUE/CHECK/FK/granel con envase) y D1–D10 reglas hoy aceptadas que Java deberá rechazar en Fase 2. El trigger registra cambios reales de `producto.precio_venta` (`OLD.precio_venta <> NEW.precio_venta`) en `producto_precio_historial`; funciona también desde herramientas SQL y con InnoDB se deshace con la transacción.

## 4. Arquitectura actual

```text
Main  ──> Gestor (lógica de negocio, consola)
  │
  └──> I_Almacenamiento (contrato)
          ├── AlmacenamientoCSV     (Datos/clientes.csv, Datos/pagos.csv)
          ├── AlmacenamientoJson    (Datos/clientes.json, Datos/pagos.json)
          └── AlmacenamientoSQLite  (impl. actual usada por Main)
```

- **`Main`**: entrada; mantiene `List<Cliente>` y `List<Pagos>`; instancia `Gestor` y `AlmacenamientoSQLite`; al arrancar `crearDirectorioYArchivos()`, lee listas y muestra menú; al salir persiste con `guardarDatos(...)`.
- **`Gestor`**: altas, listados, búsqueda y pagos por consola (`Scanner`). **No accede a ficheros ni BD**.
- **`I_Almacenamiento`**: `crearDirectorioYArchivos()`, `comprobarRuta(Path)`, `leerClientes()`, `leerPagos()`, `escribirClientes(List<Cliente>)`, `escribirPagos(List<Pagos>)`; todos con `throws IOException` (sin `SQLException`).
- **`AlmacenamientoCSV`**: UTF-8, `Datos/`, separador `;`, sin cabecera, un registro por línea; `Files.readAllLines` + `split(";")`; `Files.write` sobrescribe.
- **`AlmacenamientoJson`**: UTF-8, `clientes.json`/`pagos.json`; parser casero línea a línea (sin librerías JSON).
- **`MigraCSVToJson`**: solo lee CSV devolviendo listas; opción 7 del menú recarga desde CSV. No convierte ficheros.
- **`AlmacenamientoSQLite`**: entrega anterior; `jdbc:sqlite:<ruta absoluta>`, `PRAGMA foreign_keys = ON`, tablas y escrituras con `PreparedStatement`. **No ampliar en esta fase**.

### Modelo de datos legacy

| Clase | Campos | Observaciones |
| --- | --- | --- |
| `Cliente` | `int id`, `String nombre`, `String tlfn`, `String matricula` | Constructor 4 campos; getters y `toString()`. |
| `Pagos` | `int id`, `int id_cliente`, `String fecha`, `int importe`, `int litros`, `String combustible` | `fecha` como `String` `dd/MM/yyyy`; importa `Date` sin usar; getter `getId_cliente()`. |

## 5. Modelo de dominio Fase 1 (`org.example.model`)

- 34 clases/enums/abstractas en `src/main/java/org/example/model/`. **Sin conexiones, SQL ni DAO**: entidades en memoria con `Integer id` heredado de `Entidad` (nullable antes de persistir) y referencias a objetos.
- `Producto` abstracta; `ProductoEnvasado` exige unidades enteras; `ProductoGranel` exige decimal positivo múltiplo exacto de `precisionPesada`. Ambos lanzan `IllegalArgumentException` y calculan con `BigDecimal` + `RoundingMode.HALF_UP` a 2 decimales.
- Funciones de `Empleado` por composición: `AsignacionFuncion` abstracta + `AsignacionCajero`, `AsignacionExpendedor`, `AsignacionResponsableTienda`, `AsignacionJefeEstacion`; pueden coexistir y conservar historial.
- `DemoModelo` (`org.example`) construye grafo completo, muestra cálculos válidos/inválidos y comprueba CSV/JSON en **directorio temporal**; **no escribe en `Datos/`**. No modificar `Main` ni comportamiento CSV/JSON.
- Convivencia: campos antiguos de `Pagos` se conservan sin reinterpretar (`pago_combustible_legado`).

## 6. Compilar y ejecutar `DemoModelo`

### Opción A: `javac` (usada en la entrega)

```powershell
$jar = $env:SQLITE_JAR
$fuentes = Get-ChildItem -Recurse -Filter *.java 'src\main\java' | ForEach-Object FullName
javac -encoding UTF-8 -cp $jar -d target/classes $fuentes
java -cp "target/classes;$jar" org.example.DemoModelo
```

```bash
javac -encoding UTF-8 -cp "$SQLITE_JAR" -d target/classes $(find src/main/java -name '*.java')
java -cp "target/classes:$SQLITE_JAR" org.example.DemoModelo
```

### Opción B: Maven

```powershell
mvn -q -DskipTests compile
mvn -q exec:java -Dexec.mainClass=org.example.DemoModelo
```

La demo: 1) construye grafo de objetos (estación, ficha, surtidores, empleados con funciones, cliente, tarjeta, campaña, proveedor, acuerdos, productos, venta, cobros, devolución, incidencia, pago legado); 2) calcula importes con `BigDecimal`/`HALF_UP` a 2 decimales y captura cantidades inválidas; 3) comprueba lectura/escritura CSV y JSON en directorio temporal, sin tocar `Datos/`.

Salida esperada (extracto):

```text
Envasado 2 x 13.50 = 27.00
Granel 1.750 kg x 23.456 = 41.05
REF-ENV-1 / 2.5 -> rechazada: Un producto envasado solo admite unidades enteras: 2.5
Venta A-1: 2 lineas, total 44.59, pagado 44.59, pendiente 0.00, estado PAGADA
OK: CSV clientes / OK: CSV pagos / OK: JSON clientes / OK: JSON pagos
```

## 7. Convenciones y versiones

- Paquetes: `org.example` (app original y `DemoModelo`) y `org.example.model` (Fase 1). Una clase pública por archivo.
- Clases `PascalCase`; interfaz con prefijo `I_`. Métodos/variables `camelCase`. Mantener mezcla heredada `id_cliente`/`idCliente` donde ya existe para no romper serialización.
- Listas: conviven `clientesList`/`pagosList` en `Main` y `listaClientes`/`listaPagos` en interfaz/implementaciones; respetar el nombre usado por cada método.
- Campos privados, getters explícitos (sin Lombok/records). Comentarios y mensajes en español; bloques `//CONSTRUCTOR`, `//GETTERS`.
- Datos en `Datos/` con nombres fijos `clientes.csv|json`, `pagos.csv|json`; UTF-8.
- En MySQL preferir `<entidad>_id` para FK (`cliente_id`); en Java/CSV/JSON se usa `id_cliente`. Documentar mapeo; no mezclar estilos en la misma capa.
- **Versiones**: JDK 21; MySQL 8.4+ InnoDB/`utf8mb4`, validado en MariaDB 10.4.32; driver IntelliJ `mysql.8`, `com.mysql.cj.jdbc.Driver`, conector `com.mysql:mysql-connector-j:26.7.0`; SQLite gasolinera `sqlite-jdbc:3.49.1.0` (`RA2-JDBC` usa `3.53.4.0`).

## 8. Reglas estrictas Fase 1

1. **No implementar JDBC/DAO MySQL todavía.** Solo modelo relacional, scripts SQL y modelo de dominio Java. No añadir `SELECT/INSERT/UPDATE/DELETE`, DAOs ni lógica MySQL. Los únicos ejemplos JDBC: `../RA2-JDBC/` y `AlmacenamientoSQLite.java` (no modificar salvo indicación expresa).
2. **Mantener CSV/JSON intactos.** No tocar `AlmacenamientoCSV`, `AlmacenamientoJson`, `MigraCSVToJson` ni `Datos/*`. No cambiar separadores (`;`), claves JSON, orden de campos ni formato de `fecha`. Persistir por `I_Almacenamiento`.
3. **IDs técnicos autoincrementales en BD.** Todas las tablas, incluidas asociaciones/líneas/históricos: `id INT NOT NULL AUTO_INCREMENT PRIMARY KEY`. Nunca usar matrícula/teléfono/nombre/código de negocio como PK; van como `UNIQUE NOT NULL`. FK referencian `id` técnico (`cliente_id -> cliente.id`). En MySQL: InnoDB, `utf8mb4`, `CHECK` (8.0.16+); evitar `CHECK` que mencione columna `AUTO_INCREMENT` o con `FOREIGN KEY` (MariaDB error 1901; por eso no existe `chk_empleado_no_self_supervisor`). No usar `DEFAULT (CURRENT_DATE)` con paréntesis: en MariaDB `DEFAULT CURRENT_TIMESTAMP` (`campana_cliente.fecha_inscripcion` es `DATETIME`). `01_esquema.sql` empieza con `DROP DATABASE IF EXISTS gasolinera_mysql;` y es compatible con MySQL 8 y MariaDB 10.4+/XAMPP.

## 9. Alcance Fase 1 y pendiente Fase 2

- **Hecho en Fase 1:** modelo relacional, scripts SQL, modelo de dominio Java (>15 clases, sin vacías ni DAO), trigger en MySQL (nunca en Java), validación/cálculo por modalidad con `BigDecimal` `HALF_UP` a 2 decimales, demo sin MySQL, convivencia CSV/JSON/SQLite.
- **SQL**: todas las tablas con `id` AI PK; códigos de negocio `UNIQUE NOT NULL`; InnoDB `utf8mb4`.
- **Pendiente Fase 2 (no presentar como rechazo automático):** altas conjuntas estación+ficha y proveedor+producto; baja del último surtidor o último acuerdo; venta con al menos una línea; autosupervisión directa y ciclos de supervisión; periodos solapados de una misma función; devoluciones acumuladas; suma de pagos = importe venta; cambio de modalidad de producto. Autosupervisión no se puede prohibir con `CHECK` en MariaDB/XAMPP (error 1901 por columna con FK en CHECK): se validará en Java junto con ciclos indirectos.

## 10. Deuda conocida (no corregir en esta fase)

- `Pagos.fecha` es `String`, `importe` y `litros` son `int`: mapeo a SQL (`DATE`, `DECIMAL`) se decidirá cuando se permitan consultas.
- `AlmacenamientoJson.escribirPagos()` genera formato no estrictamente JSON (fecha sin comillas, claves con espacios, coma final, sin array contenedor); se conserva por compatibilidad.
- `Gestor` usa varios `new Scanner(System.in)` y `Pagos` importa `Date` sin usarlo: se deja como está para no alterar comportamiento.

## 11. Documentación y entregables

- [Modelo relacional y diccionario de datos](docs/01_modelo_relacional_y_diccionario.md)
- [Diseño Java y correspondencia](docs/02_diseno_java_y_correspondencia.md)
- [Reglas de negocio y evidencias](docs/03_reglas_negocio_y_evidencias.md)
- [Operaciones futuras JDBC](docs/04_operaciones_futuras_jdbc.md)
- [Borrador de auditoría de IA](docs/borrador_auditoria_ia.md)
- Capturas fase anterior: `Gasolinera con persistencia.pdf` (en repo, ubicado en `RA2-JDBC/Entregables/` tras reorganización de carpetas).
