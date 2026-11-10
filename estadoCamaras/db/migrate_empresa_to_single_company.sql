-- Run against the application's MySQL database before starting the updated version.
-- This removes the camera-to-company relationship while preserving camera/history
-- records and the existing empresa table/data.

SET @database_name = DATABASE();
SET @foreign_key_name = (
    SELECT CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @database_name
      AND TABLE_NAME = 'camara'
      AND COLUMN_NAME = 'empresa_id'
      AND REFERENCED_TABLE_NAME = 'empresa'
    LIMIT 1
);

SET @sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE `camara` DROP FOREIGN KEY `', REPLACE(@foreign_key_name, '`', '``'), '`')
);
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @has_company_column = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @database_name
      AND TABLE_NAME = 'camara'
      AND COLUMN_NAME = 'empresa_id'
);

SET @sql = IF(
    @has_company_column = 0,
    'SELECT 1',
    'ALTER TABLE `camara` DROP COLUMN `empresa_id`'
);
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
