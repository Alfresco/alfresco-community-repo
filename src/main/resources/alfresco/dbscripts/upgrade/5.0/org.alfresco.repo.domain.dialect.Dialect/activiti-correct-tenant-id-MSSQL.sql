--
-- Title:      Correct Tenant Id after Patch 8004
-- Database:   SQLServer
-- Since:      V5.0 Schema 8045
-- Author:     Mark Rogers
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Changes the value of TENANT_ID on MS SqlServer after patch 8004

-- No Op on platforms other than SQLServer
    
--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.0-activiti-correct-tenant-id-MSSQL';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.0-activiti-correct-tenant-id-MSSQL', 'Manually executed script upgrade V5.0: patch.db-V5.0-activiti-correct-tenant-id-MSSQL',
    0, 8045, -1, 8046, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );