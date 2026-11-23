-- =====================================================================
-- Gasolinera con persistencia - Fase 1 (diseno relacional)
-- 02_triggers.sql : registro automatico de cambios de precio en MySQL
-- Ejecutar DESPUES de 01_esquema.sql.
-- El historial lo escribe MySQL, nunca el programa Java.
-- =====================================================================

USE gasolinera_mysql;

DROP TRIGGER IF EXISTS trg_producto_precio_cambiado;

DELIMITER $$

CREATE TRIGGER trg_producto_precio_cambiado
AFTER UPDATE ON producto
FOR EACH ROW
BEGIN
    -- Solo se registra un cambio REAL de precio:
    -- guardar el mismo precio, cambiar la descripcion o el estado y el
    -- INSERT inicial no generan ninguna entrada.
    IF OLD.precio_venta <> NEW.precio_venta THEN
        INSERT INTO producto_precio_historial
            (producto_id, precio_anterior, precio_nuevo, fecha_cambio)
        VALUES
            (NEW.id, OLD.precio_venta, NEW.precio_venta, NOW());
    END IF;
END$$

DELIMITER ;

-- Comportamiento esperado:
--   UPDATE producto SET precio_venta = 2.10 WHERE id = 1;  -> 1 fila nueva
--   UPDATE producto SET precio_venta = 2.10 WHERE id = 1;  -> 0 filas nuevas
--   UPDATE producto SET descripcion = '...' WHERE id = 1;  -> 0 filas nuevas
--   INSERT INTO producto (...) VALUES (...);               -> 0 filas nuevas
-- Todas las tablas son InnoDB: si el UPDATE se hace dentro de una
-- transaccion y se ejecuta ROLLBACK, desaparecen juntos el precio nuevo y
-- la entrada de historial. No se necesita ningun mecanismo externo.
