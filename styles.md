# styles.md — Guía de estilo y nivel de conocimientos

> Documento de referencia para que los agentes y el propio alumno entiendan **qué sabe hacer**, **cómo suele programar** y **cómo van avanzando los proyectos** en este repositorio.  
> Base principal observada: `Tarea1/` y `RA2-JDBC/`.  
> También se tiene en cuenta la evolución visible en `Gasolinera persistencia/`, pero este archivo debe leerse como una foto del nivel y los hábitos reales, no como una lista de cosas “que habría que usar”.

## 1. Objetivo

- Servir de contexto antes de proponer cambios o ejercicios.
- Explicar el código con el nivel adecuado: claro, directo y sin asumir conocimientos no vistos.
- Evitar que una IA meta patrones avanzados que el alumno todavía no domina, salvo que el alumno lo pida expresamente.
- Registrar la evolución del proyecto cuando aparezca una técnica nueva ya entendida o trabajada.

## 2. Contexto del repositorio

| Carpeta | Qué es | Qué aporta a este documento |
| --- | --- | --- |
| `Tarea1/` | Práctica de gasolinera con persistencia en CSV/JSON. | Base principal del estilo Java inicial: POJOs, interfaz, listas, archivos, consola. |
| `RA2-JDBC/` | Material docente de JDBC (Jardín Botánico, SQLite/MySQL). | Base de conocimientos JDBC/SQL vistos en clase: conexión, `Statement`, `PreparedStatement` básico, transacciones simples, metadatos, errores SQL. |
| `Gasolinera persistencia/` | Evolución de la gasolinera: app anterior + Fase 1 de modelo de dominio y scripts MySQL. | Señales de avance: modelo más rico, `BigDecimal`, herencia/composición, validaciones, documentación más formal. |

## 3. Conocimientos observados en `Tarea1`

### 3.1 Java básico y POO

- Usa **clases públicas simples**, una por archivo, en paquete `org.example`.
- Sabe crear **POJOs** con campos `private`, constructor, getters y `toString()`.
- Usa **encapsulación básica**: atributos privados y acceso por getters.
- No usa Lombok, records, builders, streams complejos ni frameworks.
- Los nombres suelen ser claros, en español o mezclados de forma práctica (`clientesList`, `pagosList`, `listaClientes`, `darDeAlta`).

Ejemplos vistos:
- `Tarea1/src/main/java/org/example/Cliente.java`
- `Tarea1/src/main/java/org/example/Pagos.java`

### 3.2 Interfaces y polimorfismo simple

- Entiende el contrato con interfaz: `I_Almacenamiento`.
- Implementa varias clases con la misma interfaz: `AlmacenamientoCSV`, `AlmacenamientoJson`.
- Usa la interfaz desde `Main` para trabajar con una implementación concreta.

Ejemplo visto:
- `Tarea1/src/main/java/org/example/I_Almacenamiento.java`

### 3.3 Colecciones y control de flujo

- Usa `List`, `ArrayList`, bucles `for` y `for-each`.
- Usa `switch` para menús de consola.
- Usa `do/while` para mantener el menú activo hasta salir.
- Sabe buscar recorriendo listas y comparando strings con `toLowerCase()` y `contains()`.

Ejemplo visto:
- `Tarea1/src/main/java/org/example/Main.java`
- `Tarea1/src/main/java/org/example/Gestor.java`

### 3.4 Entrada/salida por consola

- Usa `Scanner` con `System.in`.
- Muestra menús con `System.out.println`.
- El flujo es procedural y directo: pedir datos, crear objeto, añadir a lista, devolver lista.

Observación: en `Gestor` se crean varios `Scanner`; es un detalle heredado del nivel/práctica, no un patrón a “arreglar por iniciativa propia”.

### 3.5 Ficheros y persistencia simple

- Usa `java.nio.file.Path`, `Files`, `BufferedReader`, `StandardCharsets`.
- Sabe crear directorios y archivos.
- Sabe leer y escribir CSV manualmente con `split(";")` y concatenación de strings.
- Sabe leer y escribir JSON de forma casera, sin librerías externas.

Ejemplos vistos:
- `Tarea1/src/main/java/org/example/AlmacenamientoCSV.java`
- `Tarea1/src/main/java/org/example/AlmacenamientoJson.java`

### 3.6 Manejo de errores

- Conoce `throws IOException`.
- Usa `try/catch` en `Main` para capturar errores y mostrarlos por consola.
- El manejo de errores es básico y funcional, no centralizado ni con logging avanzado.

## 4. Conocimientos observados en `RA2-JDBC`

### 4.1 Conexión JDBC

- Sabe abrir conexiones con `DriverManager.getConnection(...)`.
- Usa `try-with-resources` para cerrar `Connection`, `Statement`, `PreparedStatement` y `ResultSet`.
- Conoce la diferencia práctica entre SQLite y MySQL a nivel de URL/conector.
- Sabe activar `PRAGMA foreign_keys = ON` en SQLite.

Ejemplos vistos:
- `RA2-JDBC/src/main/java/es/iesluisbraille/accesodatos/jardin/bloque02/PrimeraConexionSqlite.java`
- `RA2-JDBC/src/main/java/es/iesluisbraille/accesodatos/jardin/bloque02/PrimeraConexionMysql.java`

### 4.2 SQL desde Java

- Sabe ejecutar DDL/DML básico con `Statement`.
- Conoce `execute`, `executeUpdate` y consultas con `ResultSet`.
- Conoce `PreparedStatement` y placeholders `?`.
- Sabe leer metadatos con `DatabaseMetaData`.
- Entiende errores SQL básicos y los muestra con `SQLException`.

Ejemplos vistos:
- `RA2-JDBC/src/main/java/bloque03/CrearTabla.java`
- `RA2-JDBC/src/main/java/bloque03/VariasSentencias.java`
- `RA2-JDBC/src/main/java/bloque03/ErrorSql.java`

### 4.3 Transacciones y buenas prácticas iniciales

- Ha visto `setAutoCommit(false)`, `commit()` y `rollback()`.
- Ha visto `try-with-resources` como forma segura de cerrar recursos.
- Ha visto la separación entre preparar SQL, ejecutar y procesar resultados.

### 4.4 Herramientas y entorno

- Trabaja con **JDK 21**.
- Usa **IntelliJ IDEA**.
- En RA2-JDBC aparece **Gradle**; en Tarea1/Gasolinera aparece **Maven**.
- Entiende scripts SQL ejecutados fuera de Java.
- Conoce SQLite como base embebida y MySQL/MariaDB como servidor.

## 5. Estilo de programación habitual

### 5.1 Cómo suele escribirse el código

- Código **directo y legible**, priorizando que funcione y se entienda.
- Métodos cortos o medianos, con responsabilidad clara.
- Comentarios puntuales en español cuando hace falta explicar algo.
- Mensajes de consola en español.
- Prefiere soluciones explícitas antes que abstracciones complejas.

### 5.2 Convenciones que se ven repetidas

- Paquete principal: `org.example`.
- Clases en `PascalCase`.
- Métodos y variables en `camelCase`.
- Interfaz con prefijo `I_`: `I_Almacenamiento`.
- Constantes/rutas como campos de clase cuando son fijas (`Path.of("Datos", ...)`).
- `toString()` manual en modelos simples.
- Bloques o comentarios tipo `//CONSTRUCTOR`, `//GETTERS` en clases sencillas.

### 5.3 Decisiones técnicas típicas

- Listas en memoria como fuente de trabajo durante la ejecución.
- Persistencia al entrar/salir o mediante métodos explícitos de lectura/escritura.
- Validación sencilla, muchas veces por consola o con comprobaciones básicas.
- Reutilización por interfaz en almacenamiento.
- En JDBC, preferencia por SQL claro y legible antes que capas complejas.

## 6. Cosas que conviene no asumir todavía

Estas son pautas para agentes: no significa que el alumno no pueda aprenderlas, solo que **no deben aparecer por defecto** sin explicación o petición.

- No asumir dominio de patrones avanzados (DAO completo, Repository, MVC estricto, Spring, ORM, etc.).
- No introducir librerías externas innecesarias.
- No reescribir código simple a estilo “enterprise”.
- No sustituir CSV/JSON casero por Jackson/Gson sin pedirlo.
- No usar streams/lambdas complejas donde un bucle claro sea más didáctico.
- No cambiar nombres, formatos o comportamiento heredado solo por “limpieza”.
- No ocultar SQL importante detrás de abstracciones difíciles de seguir.

## 7. Señales de evolución ya visibles en `Gasolinera persistencia`

Estas señales sirven para entender el avance, pero hay que usarlas con cuidado y como siguiente paso natural.

- Modelo de dominio más grande en `org.example.model`.
- Uso de `Integer` nullable para IDs antes de persistir.
- Uso de `BigDecimal` y `RoundingMode.HALF_UP` para importes.
- Uso de herencia abstracta (`Entidad`, `Producto`) y polimorfismo real.
- Uso de composición (`Empleado` con `AsignacionFuncion`).
- Validaciones con `IllegalArgumentException`.
- `LocalDate` / `LocalDateTime` en el modelo nuevo.
- Scripts SQL separados por responsabilidad: esquema, triggers, datos, comprobaciones.
- Documentación técnica más detallada en `README.md` y `docs/`.

## 8. Cómo debe ayudar una IA en este repo

- Explicar el **porqué** de cada cambio, no solo el código.
- Proponer antes de hacer cambios no pedidos.
- Mantener compatibilidad con lo ya entregado, salvo indicación contraria.
- Si introduce una técnica nueva, justificarla y relacionarla con algo ya visto.
- Cuando haya varias opciones, preferir la más simple y coherente con Tarea1/RA2-JDBC.
- Si el proyecto ya evolucionó (como en Gasolinera Fase 1), respetar esa evolución y no retroceder.

## 9. Resumen rápido

**Nivel base cómodo:** Java básico, POO simple, listas, consola, archivos CSV/JSON, interfaz, JDBC inicial con SQLite/MySQL.  
**Estilo base:** claro, manual, directo, en español, sin frameworks.  
**Evolución en curso:** mejor modelado de dominio, validaciones, tipos más adecuados (`BigDecimal`, fechas), SQL más formal y documentación.
