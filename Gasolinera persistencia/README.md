# Gasolinera con persistencia — Fase 1

Entrega de la práctica **Acceso a Datos**. Este README está en la raíz de la práctica
(`Gasolinera persistencia/`); todas las rutas de esta documentación son relativas a esa carpeta.
Se mantienen dos mundos que conviven:

- **Aplicación anterior (sin cambios)**: `Main`, `Gestor`, `Cliente`, `Pagos`, `I_Almacenamiento`,
  `AlmacenamientoCSV`, `AlmacenamientoJson`, `AlmacenamientoSQLite` y los ficheros de `Datos/`.
- **Fase 1 (nueva)**: scripts SQL en `sql/`, modelo de dominio Java en `src/main/java/org/example/model/`
  y la demostración ejecutable `org.example.DemoModelo`.

> En esta fase **no hay DAO ni consultas JDBC nuevas**. La clase `AlmacenamientoSQLite` se conserva
> tal cual como conexión preparada para la Fase 2. El modelo Java solo trabaja en memoria.

## Requisitos

| Herramienta | Versión / detalle |
| --- | --- |
| JDK | 21 (indicado en `pom.xml`); también verificado con JDK 26 |
| Motor SQL | MySQL 8.4+ o MariaDB 10.4+ (XAMPP). InnoDB y `utf8mb4` |
| SQLite | Driver `org.xerial:sqlite-jdbc:3.49.1.0` (necesario para compilar la aplicación anterior) |
| Cliente SQL | `mysql` de línea de comandos, phpMyAdmin o IntelliJ Data Sources |
| Maven | Opcional: no está garantizado en el equipo; se aporta alternativa con `javac` |

No se necesita ninguna librería JSON externa: `AlmacenamientoJson` usa su parser casero actual.

## Configuración de la conexión (sin secretos ni rutas personales)

La configuración se lee de variables de entorno. **No escribir contraseñas reales ni rutas de usuario
en el repositorio.** Ejemplo de valores (la contraseña es un marcador):

```dotenv
DB_URL=jdbc:mysql://localhost:3306/gasolinera_mysql
DB_USER=gasolinera_app
DB_PASSWORD=<valor_generado_por_cada_usuario>

# Solo para compilar la aplicación anterior; ajustar a la ubicación local del JAR.
SQLITE_JAR=<ruta_local_al_jar_sqlite-jdbc-3.49.1.0>
```

También se puede usar `DB_HOST`, `DB_PORT` y `DB_NAME` si se prefiere construir la URL en el código
de la Fase 2. El nombre de la base de datos debe ser `gasolinera_mysql`, porque los scripts lo usan.

En PowerShell, por ejemplo:

```powershell
$env:DB_URL    = 'jdbc:mysql://localhost:3306/gasolinera_mysql'
$env:DB_USER   = 'gasolinera_app'
$env:DB_PASSWORD = '<valor_generado_por_cada_usuario>'
```

Para el cliente `mysql` se pueden usar las mismas variables sin exponer la contraseña en el historial:

```powershell
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/01_esquema.sql
```

## Orden exacto de ejecución de los scripts SQL

Desde esta carpeta (`Gasolinera persistencia/`):

```powershell
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/01_esquema.sql
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/02_triggers.sql
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/03_datos.sql
mysql --default-character-set=utf8mb4 -u "$env:DB_USER" -p < sql/04_comprobaciones.sql
```

| Orden | Script | Qué hace | Precaución |
| --- | --- | --- | --- |
| 1 | `sql/01_esquema.sql` | Crea la BD y las 19 tablas con PK autoincremental, UNIQUE, FK y CHECK | Empieza con `DROP DATABASE IF EXISTS gasolinera_mysql`: **borra cualquier carga anterior** |
| 2 | `sql/02_triggers.sql` | Crea `trg_producto_precio_cambiado` para el historial de precios | Debe ejecutarse después de crear `producto` |
| 3 | `sql/03_datos.sql` | Carga datos de ejemplo coherentes (19 tablas, 6 ventas, 12 líneas) | No desactiva claves foráneas |
| 4 | `sql/04_comprobaciones.sql` | Verificaciones A, pruebas del trigger B, errores esperados C y reglas pendientes D | Las sentencias C están comentadas; D documenta lo que hoy se acepta y Java deberá rechazar |

Tras el paso 4, las comprobaciones A deben devolver los resultados esperados indicados en el propio
script; el bloque B modifica el precio de `PROD-001` y deshace el cambio de `PROD-002` con `ROLLBACK`.

## Compilar y ejecutar `org.example.DemoModelo`

### Opción A: `javac` (la usada en esta entrega)

```powershell
$jar = $env:SQLITE_JAR
$fuentes = Get-ChildItem -Recurse -Filter *.java 'src\main\java' | ForEach-Object FullName
javac -encoding UTF-8 -cp $jar -d target/classes $fuentes
java -cp "target/classes;$jar" org.example.DemoModelo
```

En bash/Linux:

```bash
javac -encoding UTF-8 -cp "$SQLITE_JAR" -d target/classes $(find src/main/java -name '*.java')
java -cp "target/classes:$SQLITE_JAR" org.example.DemoModelo
```

### Opción B: Maven

```powershell
mvn -q -DskipTests compile
mvn -q exec:java -Dexec.mainClass=org.example.DemoModelo
```

La demo:

1. Construye un grafo de objetos interconectados (estación, ficha, surtidores, empleados con varias
   funciones, cliente, tarjeta, campaña, proveedor, acuerdos, productos, venta, cobros, devolución,
   incidencia y pago legado).
2. Calcula importes con `BigDecimal` y `RoundingMode.HALF_UP` a 2 decimales y captura cantidades
   inválidas de productos envasados y a granel.
3. Comprueba lectura/escritura CSV y JSON en un **directorio temporal**. No modifica `Datos/`.

Salida esperada (extracto):

```text
Envasado 2 x 13.50 = 27.00
Granel 1.750 kg x 23.456 = 41.05
REF-ENV-1 / 2.5 -> rechazada: Un producto envasado solo admite unidades enteras: 2.5
Venta A-1: 2 lineas, total 44.59, pagado 44.59, pendiente 0.00, estado PAGADA
OK: CSV clientes / OK: CSV pagos / OK: JSON clientes / OK: JSON pagos
```

## Documentación de la entrega

- [Modelo relacional y diccionario de datos](docs/01_modelo_relacional_y_diccionario.md)
- [Diseño Java y correspondencia](docs/02_diseno_java_y_correspondencia.md)
- [Reglas de negocio y evidencias](docs/03_reglas_negocio_y_evidencias.md)
- [Operaciones futuras JDBC](docs/04_operaciones_futuras_jdbc.md)
- [Borrador de auditoría de IA](docs/borrador_auditoria_ia.md)

La entrega de capturas de la fase anterior es `Gasolinera con persistencia.pdf`; en el repositorio
está en la carpeta superior (`../Gasolinera con persistencia.pdf`).
