--
-- Title:      Define string_value column as nonkey in IDX_ALF_NPROP_S index.
-- Database:   Generic
-- Since:      V4.2 Schema 6052
-- Author:     Viachaslau Tsikhanovich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-9764 : SQL Server: MDQ index does not handle large string values

-- Patch is applied only for MS SQL Server, see MNT-9764

--
-- Record script finish
--

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-restructure-idx_alf_nprop_s-MSSQL';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-restructure-idx_alf_nprop_s-MSSQL', 'Defines string_value column as nonkey in IDX_ALF_NPROP_S index',
    0, 6051, -1, 6052, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );