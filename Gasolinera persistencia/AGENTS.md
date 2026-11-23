# AGENTS.md — Gasolinera con persistencia

> Guía para agentes y para quien continúe esta práctica. **No existe una clase `Gasolinera`**:
> «Gasolinera» es el nombre del proyecto; el modelo de datos son las clases `Cliente` y `Pagos`.

## 1. Alcance del repositorio

| Ruta | Qué es | Regla |
| --- | --- | --- |
| `.` (esta carpeta) | **Programa de esta práctica** (proyecto Maven, paquete `org.example`). | Aquí se trabajan las entregas de la gasolinera. |
| `../Tarea1/` | Entrega anterior en CSV/JSON, sin la implementación SQLite. | Referencia histórica; no modificar. |
| `../RA2-JDBC/` | Material docente de JDBC (Jardín Botánico, SQLite y MySQL). | Referencia de sintaxis/versiones; **no modificar**. |

Todas las rutas de esta guía son relativas a esta carpeta (`Gasolinera persistencia/`). Los ficheros
se abren en IntelliJ IDEA como proyecto Maven desde esta misma carpeta.

## 2. Arquitectura actual

```
Main  ──> Gestor (lógica de negocio, consola)
  │
  └──> I_Almacenamiento (contrato)
          ├── AlmacenamientoCSV     (Datos/clientes.csv, Datos/pagos.csv)
          ├── AlmacenamientoJson    (Datos/clientes.json, Datos/pagos.json)
          └── AlmacenamientoSQLite  (impl. actualmente en uso por Main)
```

- **`Main`** — punto de entrada. Mantiene en memoria `List<Cliente>` y `List<Pagos>`; instancia `Gestor` y `AlmacenamientoSQLite` (vía `I_Almacenamiento`); al arrancar llama a `crearDirectorioYArchivos()`, lee ambas listas y muestra el menú; al salir persiste con `guardarDatos(...)`.
- **`Gestor`** — altas, listados, búsqueda y pagos por consola (`Scanner`). **No accede a ficheros ni a BD**: solo recibe y devuelve listas.
- **`I_Almacenamiento`** — contrato común: `crearDirectorioYArchivos()`, `comprobarRuta(Path)`, `leerClientes()`, `leerPagos()`, `escribirClientes(List<Cliente>)`, `escribirPagos(List<Pagos>)`. Todos declaran `throws IOException` (ni un `SQLException` en la interfaz).
- **`AlmacenamientoCSV`** — UTF-8, carpeta `Datos/`, separador `;`, sin cabecera, un registro por línea. `Files.readAllLines` + `split(";")` para leer; `Files.write` (sobrescribe todo) para escribir.
- **`AlmacenamientoJson`** — UTF-8, `Datos/clientes.json` y `Datos/pagos.json`. Parser casero línea a línea (sin librerías JSON): descarta líneas sin `"id"`, recorta entre `{` y `}`, divide por comas. Tiene un campo `json` sin usar en `AlmacenamientoCSV`.
- **`MigraCSVToJson`** — solo lee CSV devolviendo listas; la opción 7 del menú la usa para recargar desde CSV. No es un convertidor de ficheros.
- **`AlmacenamientoSQLite`** — implementación añadida en la entrega anterior: conexión `jdbc:sqlite:<ruta absoluta>`, `PRAGMA foreign_keys = ON`, creación de tablas y escrituras con `PreparedStatement`. Es código existente, **no ampliar en esta fase**.

### Modelo de dominio de Fase 1 (`org.example.model`)

- 34 clases/enums/abstractas en `src/main/java/org/example/model/`. Ninguna abre conexiones ni contiene SQL/DAO: son entidades en memoria con `Integer id` heredado de `Entidad` (nullable antes de persistir) y referencias a objetos.
- `Producto` es abstracta; `ProductoEnvasado` exige unidades enteras y `ProductoGranel` exige decimal positivo múltiplo exacto de `precisionPesada`. Ambos lanzan `IllegalArgumentException` y calculan con `BigDecimal` + `RoundingMode.HALF_UP` a 2 decimales.
- Las funciones de `Empleado` son composición: `AsignacionFuncion` abstracta con `AsignacionCajero`, `AsignacionExpendedor`, `AsignacionResponsableTienda` y `AsignacionJefeEstacion`. Pueden coexistir y conservar historial.
- `DemoModelo` (`org.example`) construye el grafo completo, muestra cálculos válidos/inválidos y comprueba CSV/JSON en un directorio temporal; **no escribe en `Datos/`**. No modificar `Main` ni el comportamiento de CSV/JSON.

### Modelo de datos

| Clase | Campos | Observaciones |
| --- | --- | --- |
| `Cliente` | `int id`, `String nombre`, `String tlfn`, `String matricula` | Constructor con los 4 campos; solo getters y `toString()`. |
| `Pagos` | `int id`, `int id_cliente`, `String fecha`, `int importe`, `int litros`, `String combustible` | Importa `java.util.Date` pero no lo usa: `fecha` se maneja como `String` (`dd/MM/yyyy`). Getter `getId_cliente()` (con guion bajo). |

## 3. Convenciones de nombres y estilo

- Paquetes: `org.example` (programa original y `DemoModelo`) y `org.example.model` (modelo de Fase 1). **Una clase pública por archivo** con el mismo nombre.
- Clases en PascalCase; interfaz con prefijo `I_`: `I_Almacenamiento`, `AlmacenamientoCSV`, `AlmacenamientoSQLite`.
- Métodos y variables en camelCase (`leerClientes`, `crearDirectorioYArchivos`, `idCliente`). El código heredado mezcla `id_cliente` (modelo `Pagos`, CSV y JSON) con `idCliente` (parámetros del constructor); **mantener tal cual** para no romper la serialización.
- Listas: conviven `clientesList`/`pagosList` en `Main` y `listaClientes`/`listaPagos` en la interfaz y las implementaciones. Respetar el nombre que ya usa cada método.
- Campos privados, getters explícitos (sin Lombok ni registros). Comentarios y mensajes de consola en español; bloques `//CONSTRUCTOR`, `//GETTERS`.
- Datos en `Datos/` con nombres fijos `clientes.csv|json`, `pagos.csv|json`; codificación `UTF-8` en todas las lecturas/escrituras.
- En MySQL se prefiere `<entidad>_id` para claves foráneas (`cliente_id`), mientras que en Java/CSV/JSON se usa `id_cliente`. Documentar el mapeo; no mezclar ambos estilos en una misma capa.

## 4. Versiones

- **Java/JDK 21** — `maven.compiler.source`/`target` = 21 en `pom.xml`; `project-jdk-name="21"` en `.idea/misc.xml`; `JavaLanguageVersion.of(21)` en `../RA2-JDBC/build.gradle.kts`.
- **MySQL 8.4+** con InnoDB y `utf8mb4` (según `../RA2-JDBC/sql/mysql/01-esquema.sql`); validado también en **MariaDB 10.4.32**. Driver de IntelliJ `mysql.8`, clase `com.mysql.cj.jdbc.Driver`, URL `jdbc:mysql://localhost:3306/ra2_botanico`, conector `com.mysql:mysql-connector-j:26.7.0`.
- **SQLite (estado actual de la gasolinera)** — driver `org.xerial:sqlite-jdbc:3.49.1.0` en `pom.xml`. En `RA2-JDBC` se usa `3.53.4.0`. MySQL es el destino de la siguiente fase, todavía no el motor en uso por `Main`.

## 5. Fase 1 — Ampliación MySQL (alcance de esta entrega)

Fuente: `Caso_practico_Gasolinera_MySQL_Fase_1.md`.

- **Solo diseño y preparación**: modelo relacional, scripts SQL y modelo de dominio Java (más de 15 clases como referencia de tamaño, sin clases vacías ni DAO).
- **No programar métodos JDBC todavía**: nada de `INSERT`/`SELECT`/`UPDATE`/`DELETE`, DAOs ni persistencia MySQL desde Java. La conexión JDBC ya preparada se conserva, pero no se utiliza para consultas.
- **Los scripts se ejecutan fuera de Java**, en este orden: `sql/01_esquema.sql` → `sql/02_triggers.sql` → `sql/03_datos.sql` → `sql/04_comprobaciones.sql`. Los cuatro están creados; los tres primeros dejan una base coherente sobre un entorno vacío. No se desactivan claves foráneas para ocultar cargas incorrectas. `04` contiene 46 `SELECT`, 4 pruebas del trigger (incluido `ROLLBACK`) y dos bloques separados: C1–C15 son errores esperados comentados (UNIQUE, CHECK, FK y el caso de granel con envase) y D1–D10 son reglas hoy aceptadas que Java deberá rechazar en Fase 2. `01` empieza con `DROP DATABASE IF EXISTS` para poder relanzarlo desde cero.
- **El trigger va en MySQL, nunca en Java**: `02_triggers.sql` registra los cambios reales de `producto.precio_venta` (`OLD.precio_venta <> NEW.precio_venta`) en `producto_precio_historial`. Funciona también si el precio se modifica desde una herramienta SQL, y con InnoDB la entrada desaparece si se deshace la transacción.
- **Modelo de dominio Java**: validar cantidades y calcular importes según la modalidad (envase = unidades enteras; granel = decimal respetando `precision_pesada`), con `BigDecimal` y redondeo `HALF_UP` a 2 decimales. Demo breve sin MySQL en `DemoModelo`. Las clases de dominio no abren conexiones ni ejecutan SQL. Maven no está en el PATH: compilar con `javac -cp <sqlite-jdbc.jar> -d target/classes (Get-ChildItem -Recurse -Filter *.java src\main\java).FullName` y ejecutar con `java -cp "target/classes;<sqlite-jdbc.jar>" org.example.DemoModelo`.
- **Convivencia**: CSV, JSON y SQLite siguen funcionando igual. Los campos antiguos de `Pagos` se conservan sin reinterpretarlos (tabla `pago_combustible_legado`).
- **SQL**: todas las tablas, incluidas asociaciones, líneas e históricos, llevan `id INT NOT NULL AUTO_INCREMENT PRIMARY KEY`; los códigos de negocio son `UNIQUE NOT NULL`; tablas InnoDB en `utf8mb4`.
- **Pendiente para Fase 2 (no presentar como rechazo automático)**: altas conjuntas estación+ficha y proveedor+producto, baja del último surtidor o del último acuerdo, venta con al menos una línea, **autosupervisión directa y ciclos de supervisión**, periodos solapados de una misma función, devoluciones acumuladas, suma de pagos = importe de la venta y cambio de modalidad de un producto. La autosupervisión no se puede prohibir con `CHECK` en MariaDB/XAMPP (error 1901: columna con `FOREIGN KEY` dentro de un `CHECK`), así que se validará en Java junto con los ciclos indirectos.

## 6. Reglas estrictas de esta práctica

1. **No implementar consultas JDBC ni métodos de persistencia MySQL todavía.** En Fase 1 solo se diseña el modelo relacional, se escriben los scripts SQL y se prepara el modelo de dominio Java. No añadir `SELECT`/`INSERT`/`UPDATE`/`DELETE`, DAOs ni lógica MySQL en esta carpeta. Los únicos ejemplos JDBC son `../RA2-JDBC/` (docente) y `AlmacenamientoSQLite.java` (entrega anterior): no modificarlos salvo indicación expresa.
2. **Mantener CSV/JSON intactos.** No tocar `AlmacenamientoCSV`, `AlmacenamientoJson`, `MigraCSVToJson` ni `Datos/*`. No cambiar separadores (`;`), claves JSON, orden de campos ni el formato de `fecha`. Si hay que persistir, hacerlo a través de `I_Almacenamiento`.
3. **IDs técnicos autoincrementales en BD.** Todas las tablas de esta ampliación, incluidas asociaciones, asignaciones, líneas e históricos, llevan `id INT NOT NULL AUTO_INCREMENT PRIMARY KEY`. **Nunca** usar matrícula, teléfono, nombre ni código de negocio como clave primaria: esos códigos van como `UNIQUE NOT NULL`. Las FK referencian siempre el `id` técnico (`cliente_id` → `cliente.id`). En MySQL: InnoDB, `utf8mb4` y `CHECK` (8.0.16+); evita que un `CHECK` mencione la columna `AUTO_INCREMENT` **o una columna con `FOREIGN KEY`** (MariaDB lo rechaza con error 1901; por eso no existe `chk_empleado_no_self_supervisor`). Tampoco uses `DEFAULT (CURRENT_DATE)` con paréntesis: en MariaDB usa `DEFAULT CURRENT_TIMESTAMP` (campana_cliente.fecha_inscripcion es `DATETIME`). `01_esquema.sql` empieza con `DROP DATABASE IF EXISTS gasolinera_mysql;` y es compatible con MySQL 8 y MariaDB 10.4+/XAMPP.

## 7. Deuda conocida (no corregir en esta fase)

- `Pagos.fecha` es `String`, `importe` y `litros` son `int`: el mapeo a tipos SQL (`DATE`, `DECIMAL`) se decidirá cuando se permitan consultas.
- `AlmacenamientoJson.escribirPagos()` genera un formato no estrictamente JSON (fecha sin comillas, claves con espacios, coma final y sin array contenedor); se conserva por compatibilidad con las entregas anteriores.
- `Gestor` usa varios `new Scanner(System.in)` y `Pagos` importa `Date` sin usarlo: se deja como está para no alterar el comportamiento de la práctica.
