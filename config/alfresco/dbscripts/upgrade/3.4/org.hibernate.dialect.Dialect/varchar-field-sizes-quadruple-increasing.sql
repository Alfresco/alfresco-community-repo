--
-- Title:      Increasing 'VARCHAR' field sizes quadruply for DB2 dialect
-- Database:   Generic
-- Since:      V3.4
-- Author:     Dmitry Velichkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-4300: DB2: Review schema (eg. VARCHAR columns) with respect to multi-byte support (when using DB2 / UTF-8)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-VarcharFieldSizesQuadrupleIncreasing';
INSERT INTO
    alf_applied_patch
    (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
VALUES (
    'patch.db-V3.4-VarcharFieldSizesQuadrupleIncreasing', 'Increasing VARCHAR field sizes quadruply for DB2 dialect V3.4',
    0, 4303, -1, 4304, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
);
