-- =====================================================================
-- Gasolinera con persistencia - Fase 1 (diseno relacional)
-- 01_esquema.sql : base de datos, tablas, claves y restricciones
-- MySQL 8.4+ y MariaDB 10.4+ (XAMPP) / InnoDB / utf8mb4
-- Ejecutar ANTES de 02_triggers.sql. Se puede relanzar desde cero: empieza
-- con DROP DATABASE, asi que borra cualquier carga anterior de esta BD.
-- Todas las tablas usan un unico id INT AUTO_INCREMENT como PK tecnica;
-- los codigos de negocio se mantienen como UNIQUE NOT NULL.
-- Compatibilidad MariaDB: ningun CHECK referencia una columna con FOREIGN
-- KEY (MariaDB lo rechaza con error 1901) ni una columna AUTO_INCREMENT.
-- Por eso la autosupervision y los ciclos se validaran en Java (Fase 2).
-- Tampoco se usan valores por defecto entre parentesis, como (CURRENT_DATE).
-- =====================================================================

DROP DATABASE IF EXISTS gasolinera_mysql;

CREATE DATABASE IF NOT EXISTS gasolinera_mysql
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gasolinera_mysql;

-- ---------------------------------------------------------------------
-- 1. ESTACIONES Y CONFIGURACION DE FACTURACION (1:1)
-- MySQL no tiene DEFERRABLE constraints: la FK garantiza que toda ficha
-- pertenezca a una estacion y el UNIQUE que no se comparta ni se repita.
-- Que TODA estacion tenga exactamente una ficha se completa con el alta
-- conjunta transaccional que se implementara en Java en la Fase 2.
-- ---------------------------------------------------------------------

CREATE TABLE estacion (
    id                 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_estacion    VARCHAR(20)  NOT NULL COMMENT 'Codigo interno de la empresa',
    nombre_comercial   VARCHAR(120) NOT NULL,
    direccion          VARCHAR(200) NOT NULL,
    telefono           VARCHAR(30)  NULL,
    fecha_apertura     DATE         NOT NULL,
    CONSTRAINT uq_estacion_codigo UNIQUE (codigo_estacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ficha_facturacion (
    id                 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_ficha       VARCHAR(30) NOT NULL COMMENT 'Codigo que usa soporte para identificar la ficha',
    estacion_id        INT         NOT NULL,
    serie_tickets      VARCHAR(10) NOT NULL,
    siguiente_numero   INT         NOT NULL DEFAULT 1,
    texto_pie          TEXT        NULL,
    CONSTRAINT uq_ficha_codigo   UNIQUE (codigo_ficha),
    CONSTRAINT uq_ficha_estacion UNIQUE (estacion_id),
    CONSTRAINT chk_ficha_siguiente_numero CHECK (siguiente_numero > 0),
    CONSTRAINT fk_ficha_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cada estacion tiene al menos un surtidor; impedir borrar el ultimo se
-- controlara en Java en la Fase 2 (no hay CHECK entre tablas en MySQL).
CREATE TABLE surtidor (
    id                INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_equipo     VARCHAR(30) NOT NULL COMMENT 'Codigo unico en toda la empresa',
    estacion_id       INT         NOT NULL,
    numero_visible    INT         NOT NULL COMMENT 'Numero impreso en el equipo; unico dentro de la estacion',
    tipo_combustible  VARCHAR(30) NOT NULL,
    estado_servicio   ENUM('OPERATIVO','AVERIADO','FUERA_SERVICIO') NOT NULL DEFAULT 'OPERATIVO',
    CONSTRAINT uq_surtidor_codigo  UNIQUE (codigo_equipo),
    CONSTRAINT uq_surtidor_numero  UNIQUE (estacion_id, numero_visible),
    CONSTRAINT chk_surtidor_numero CHECK (numero_visible > 0),
    CONSTRAINT fk_surtidor_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. PERSONAL, FUNCIONES Y SUPERVISION
-- La supervision actual se guarda en el propio empleado (self-FK).
-- No se anade CHECK (supervisor_id <> id): MariaDB/XAMPP prohibe usar en un
-- CHECK una columna que participa en una FOREIGN KEY (error 1901), y MySQL 8
-- tampoco puede garantizar con el CHECK los ciclos indirectos. Por tanto,
-- la autosupervision directa Y los ciclos indirectos se controlaran en Java
-- en la Fase 2 (recorrido recursivo de supervisor_id antes de grabar).
-- La self-FK sigue garantizando que el supervisor sea otro empleado valido
-- y ON DELETE SET NULL evita referencias huerfanas al dar de baja uno.
-- ---------------------------------------------------------------------

CREATE TABLE empleado (
    id                   INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    numero_interno       VARCHAR(20)  NOT NULL COMMENT 'Codigo de negocio del empleado',
    documento_identidad  VARCHAR(30)  NOT NULL,
    nombre               VARCHAR(120) NOT NULL,
    telefono             VARCHAR(30)  NULL,
    email                VARCHAR(120) NULL,
    fecha_incorporacion  DATE         NOT NULL,
    estacion_id          INT          NOT NULL,
    supervisor_id        INT          NULL COMMENT 'Supervisor actual; NO equivale a jefe de estacion',
    CONSTRAINT uq_empleado_numero    UNIQUE (numero_interno),
    CONSTRAINT uq_empleado_documento UNIQUE (documento_identidad),
    -- Sin CHECK de autosupervision: MariaDB no lo acepta sobre una columna
    -- FK (error 1901). Tanto supervisor_id = id como los ciclos se validan
    -- en Java en Fase 2; ver 04_comprobaciones.sql, bloque D.
    CONSTRAINT fk_empleado_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_empleado_supervisor FOREIGN KEY (supervisor_id)
        REFERENCES empleado (id) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Una fila = un periodo de una funcion para una persona. Varias filas
-- simultaneas de funciones DISTINTAS estan permitidas. Los atributos
-- especificos son nullables porque no todas las funciones los usan, pero
-- los CHECK exigen los de la funcion correspondiente y prohiben los demas.
-- periodo_activo (columna generada) impide dos periodos abiertos de la
-- misma funcion; el solapamiento de periodos cerrados queda para Fase 2.
CREATE TABLE historial_funcion (
    id                      INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    empleado_id             INT          NOT NULL,
    tipo_funcion            ENUM('CAJERO','EXPENDEDOR','RESPONSABLE_TIENDA','JEFE_ESTACION') NOT NULL,
    fecha_inicio            DATE         NOT NULL,
    fecha_fin               DATE         NULL COMMENT 'NULL = periodo todavia activo',
    limite_devoluciones     DECIMAL(12,2) NULL COMMENT 'Solo CAJERO',
    numero_acreditacion     VARCHAR(30)  NULL COMMENT 'Solo EXPENDEDOR',
    caducidad_acreditacion  DATE         NULL COMMENT 'Solo EXPENDEDOR',
    presupuesto_compras     DECIMAL(12,2) NULL COMMENT 'Solo RESPONSABLE_TIENDA',
    nivel_autorizacion      VARCHAR(20)  NULL COMMENT 'Solo JEFE_ESTACION',
    periodo_activo          INT GENERATED ALWAYS AS (IF(fecha_fin IS NULL, 1, NULL)) STORED,
    CONSTRAINT chk_funcion_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_funcion_cajero CHECK
        (tipo_funcion <> 'CAJERO' OR (limite_devoluciones IS NOT NULL AND limite_devoluciones >= 0)),
    CONSTRAINT chk_funcion_expendedor CHECK
        (tipo_funcion <> 'EXPENDEDOR' OR (numero_acreditacion IS NOT NULL AND caducidad_acreditacion IS NOT NULL)),
    CONSTRAINT chk_funcion_responsable CHECK
        (tipo_funcion <> 'RESPONSABLE_TIENDA' OR (presupuesto_compras IS NOT NULL AND presupuesto_compras >= 0)),
    CONSTRAINT chk_funcion_jefe CHECK
        (tipo_funcion <> 'JEFE_ESTACION' OR nivel_autorizacion IS NOT NULL),
    CONSTRAINT chk_funcion_solo_atributos_propios CHECK (
           (tipo_funcion = 'CAJERO'            OR limite_devoluciones IS NULL)
       AND (tipo_funcion = 'EXPENDEDOR'        OR (numero_acreditacion IS NULL AND caducidad_acreditacion IS NULL))
       AND (tipo_funcion = 'RESPONSABLE_TIENDA' OR presupuesto_compras IS NULL)
       AND (tipo_funcion = 'JEFE_ESTACION'     OR nivel_autorizacion IS NULL)
    ),
    CONSTRAINT uq_funcion_activa UNIQUE (empleado_id, tipo_funcion, periodo_activo),
    CONSTRAINT fk_funcion_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. CLIENTES Y TARJETAS DE FIDELIZACION
-- Criterio de contacto: solo codigo y nombre son obligatorios; telefono,
-- email y direccion son opcionales (Fase 2 exigira al menos un medio).
-- Tarjeta.cliente_id unico y nullable: tarjetas sin entregar permitidas y
-- un cliente nunca tiene dos tarjetas a la vez.
-- ---------------------------------------------------------------------

CREATE TABLE cliente (
    id              INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_cliente  VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    telefono        VARCHAR(30)  NULL,
    email           VARCHAR(120) NULL,
    direccion       VARCHAR(200) NULL,
    CONSTRAINT uq_cliente_codigo UNIQUE (codigo_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tarjeta_fidelizacion (
    id                INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    numero_tarjeta    VARCHAR(30) NOT NULL,
    fecha_emision     DATE        NOT NULL,
    cliente_id        INT         NULL,
    fecha_asignacion  DATE        NULL,
    CONSTRAINT uq_tarjeta_numero UNIQUE (numero_tarjeta),
    CONSTRAINT uq_tarjeta_cliente UNIQUE (cliente_id),
    CONSTRAINT chk_tarjeta_fechas
        CHECK (fecha_asignacion IS NULL OR fecha_asignacion >= fecha_emision),
    CONSTRAINT fk_tarjeta_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (id) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4. CATALOGO, HISTORIAL DE PRECIOS Y PROVEEDORES
-- La modalidad es estable: un producto no cambia de ENVASADO a GRANEL.
-- Impedir ese cambio se controlara en Java en la Fase 2.
-- Redondeo monetario comun: 2 decimales (DECIMAL(12,2)), HALF_UP en Java.
-- ---------------------------------------------------------------------

CREATE TABLE producto (
    id                       INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    referencia_comercial     VARCHAR(50)  NOT NULL,
    descripcion              VARCHAR(200) NOT NULL,
    precio_venta             DECIMAL(12,2) NOT NULL,
    estado_comercializacion  ENUM('ACTIVO','RETIRADO') NOT NULL DEFAULT 'ACTIVO',
    modalidad                ENUM('ENVASADO','GRANEL') NOT NULL,
    formato_envase           VARCHAR(50)  NULL COMMENT 'Solo ENVASADO',
    precision_pesada         DECIMAL(6,3) NULL COMMENT 'Solo GRANEL, en kg (0 < p <= 1)',
    CONSTRAINT uq_producto_referencia UNIQUE (referencia_comercial),
    CONSTRAINT chk_producto_precio CHECK (precio_venta > 0),
    CONSTRAINT chk_producto_envase CHECK
        (modalidad <> 'ENVASADO' OR (formato_envase IS NOT NULL AND precision_pesada IS NULL)),
    CONSTRAINT chk_producto_granel CHECK
        (modalidad <> 'GRANEL' OR
         (precision_pesada IS NOT NULL AND precision_pesada > 0
          AND precision_pesada <= 1 AND formato_envase IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alimentado exclusivamente por el trigger de 02_triggers.sql.
CREATE TABLE producto_precio_historial (
    id               INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    producto_id      INT           NOT NULL,
    precio_anterior  DECIMAL(12,2) NOT NULL,
    precio_nuevo     DECIMAL(12,2) NOT NULL,
    fecha_cambio     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_precio_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE proveedor (
    id                    INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_proveedor      VARCHAR(20)  NOT NULL,
    identificacion_fiscal VARCHAR(30)  NOT NULL,
    razon_social          VARCHAR(150) NOT NULL,
    telefono              VARCHAR(30)  NULL,
    email                 VARCHAR(120) NULL,
    direccion             VARCHAR(200) NULL,
    CONSTRAINT uq_proveedor_codigo UNIQUE (codigo_proveedor),
    CONSTRAINT uq_proveedor_fiscal UNIQUE (identificacion_fiscal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- N:M proveedor-producto. No se guarda historial de condiciones: para
-- cambiar el acuerdo se actualiza la misma fila (UNIQUE del par).
-- Que ningun proveedor/producto quede aislado se garantizara en Fase 2
-- con altas conjuntas y baja del ultimo acuerdo.
CREATE TABLE proveedor_producto (
    id                    INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    proveedor_id          INT           NOT NULL,
    producto_id           INT           NOT NULL,
    referencia_proveedor  VARCHAR(50)   NOT NULL COMMENT 'Referencia que usa este proveedor',
    precio_compra         DECIMAL(12,2) NOT NULL,
    plazo_entrega_dias    INT           NOT NULL,
    CONSTRAINT uq_proveedor_producto UNIQUE (proveedor_id, producto_id),
    CONSTRAINT chk_acuerdo_precio CHECK (precio_compra > 0),
    CONSTRAINT chk_acuerdo_plazo  CHECK (plazo_entrega_dias >= 0),
    CONSTRAINT fk_acuerdo_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedor (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_acuerdo_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 5. CAMPAÑAS PROMOCIONALES (N:M con clientes)
-- ---------------------------------------------------------------------

CREATE TABLE campana (
    id              INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_campana  VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    descripcion     TEXT         NULL,
    fecha_inicio    DATE         NOT NULL,
    fecha_fin       DATE         NOT NULL,
    CONSTRAINT uq_campana_codigo UNIQUE (codigo_campana),
    CONSTRAINT chk_campana_fechas CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE campana_cliente (
    id                 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    campana_id         INT  NOT NULL,
    cliente_id         INT  NOT NULL,
    fecha_inscripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de la inscripcion',
    CONSTRAINT uq_campana_cliente UNIQUE (campana_id, cliente_id),
    CONSTRAINT fk_inscripcion_campana FOREIGN KEY (campana_id)
        REFERENCES campana (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_inscripcion_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 6. VENTAS, LINEAS, PAGOS Y DEVOLUCIONES
-- estacion_id se guarda en la venta: los cambios de adscripcion del
-- empleado no alteran la estacion que consta en sus tickets antiguos.
-- La venta admite cliente_id NULL (compra anonima).
-- estado_pago distingue PENDIENTE / PARCIAL / PAGADA; la comprobacion de
-- que la suma de pagos iguale importe_total se hara en Java (Fase 2).
-- ---------------------------------------------------------------------

CREATE TABLE venta (
    id                INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    referencia_ticket VARCHAR(40) NOT NULL,
    estacion_id       INT         NOT NULL,
    empleado_id       INT         NOT NULL,
    cliente_id        INT         NULL,
    fecha_hora        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_total     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado_pago       ENUM('PENDIENTE','PARCIAL','PAGADA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT uq_venta_ticket UNIQUE (referencia_ticket),
    CONSTRAINT chk_venta_importe CHECK (importe_total >= 0),
    CONSTRAINT fk_venta_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_venta_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- precio_unitario_aplicado e importe son copia del momento de la venta:
-- los cambios posteriores del catalogo no pueden recalcular tickets.
-- La validacion de cantidad entera para ENVASADO / decimal para GRANEL
-- requiere consultar la modalidad del producto: se hara en Java (Fase 2).
CREATE TABLE linea_venta (
    id                       INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id                 INT           NOT NULL,
    producto_id              INT           NOT NULL,
    numero_orden             INT           NOT NULL,
    cantidad                 DECIMAL(10,3) NOT NULL,
    precio_unitario_aplicado DECIMAL(12,2) NOT NULL,
    importe                  DECIMAL(12,2) NOT NULL,
    CONSTRAINT uq_linea_orden UNIQUE (venta_id, numero_orden),
    CONSTRAINT chk_linea_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_linea_precio   CHECK (precio_unitario_aplicado > 0),
    CONSTRAINT chk_linea_importe  CHECK (importe >= 0),
    CONSTRAINT fk_linea_venta FOREIGN KEY (venta_id)
        REFERENCES venta (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pago_venta (
    id                INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id          INT           NOT NULL,
    importe           DECIMAL(12,2) NOT NULL,
    fecha_hora        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    medio_pago        ENUM('EFECTIVO','TARJETA','TRANSFERENCIA','OTRO') NOT NULL,
    referencia_externa VARCHAR(50)  NULL,
    CONSTRAINT chk_pago_venta_importe CHECK (importe > 0),
    CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id)
        REFERENCES venta (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Una devolucion = una linea concreta de una venta ya pagada. Varias
-- devoluciones sucesivas de la misma linea permitidas; el limite
-- acumulado de cantidad/importe se controlara en Java en Fase 2.
CREATE TABLE devolucion (
    id                   INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    linea_venta_id       INT           NOT NULL,
    empleado_id          INT           NOT NULL,
    cantidad             DECIMAL(10,3) NOT NULL,
    motivo               VARCHAR(255) NOT NULL,
    fecha_hora           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_reintegrado  DECIMAL(12,2) NOT NULL,
    apta_para_venta      BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_devolucion_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_devolucion_importe  CHECK (importe_reintegrado >= 0),
    CONSTRAINT fk_devolucion_linea FOREIGN KEY (linea_venta_id)
        REFERENCES linea_venta (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_devolucion_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 7. INCIDENCIAS
-- Responsable opcional en el momento de abrir; un empleado puede tener
-- varias incidencias asignadas (sin UNIQUE en empleado_responsable_id).
-- ---------------------------------------------------------------------

CREATE TABLE incidencia (
    id                       INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    referencia_incidencia    VARCHAR(30) NOT NULL,
    estacion_id              INT         NOT NULL,
    descripcion              TEXT        NOT NULL,
    fecha_apertura           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado                   ENUM('ABIERTA','EN_CURSO','RESUELTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
    empleado_responsable_id  INT         NULL,
    CONSTRAINT uq_incidencia_referencia UNIQUE (referencia_incidencia),
    CONSTRAINT fk_incidencia_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_incidencia_responsable FOREIGN KEY (empleado_responsable_id)
        REFERENCES empleado (id) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 8. COMPATIBILIDAD CON LA APLICACION ANTERIOR
-- Los registros antiguos de la clase Pagos se conservan sin reinterpretar:
-- fecha se guarda como el String original (dd/MM/yyyy) e id_cliente como
-- el valor que tenia, sin convertirlo en una FK nueva. Es distinto de
-- pago_venta (cobros de la tienda, Fase 2).
-- ---------------------------------------------------------------------

CREATE TABLE pago_combustible_legado (
    id                 INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_cliente_original INT        NULL COMMENT 'Valor original de Pagos.id_cliente; sin reinterpretar',
    fecha              VARCHAR(10) NULL COMMENT 'Igual que Pagos.fecha (dd/MM/yyyy)',
    importe            INT         NOT NULL,
    litros             INT         NOT NULL,
    combustible        VARCHAR(30) NOT NULL,
    CONSTRAINT chk_legado_importe CHECK (importe >= 0),
    CONSTRAINT chk_legado_litros  CHECK (litros >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
