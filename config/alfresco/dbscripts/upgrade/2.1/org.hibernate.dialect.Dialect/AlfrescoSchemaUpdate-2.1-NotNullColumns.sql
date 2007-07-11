--
-- Title:      Add text columns that allow null
-- Database:   Generic
-- Since:      V2.1 Schema 64
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This is a Sybase issue, so nothing is required here. 

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-NotNullColumns';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-NotNullColumns', 'Manually executed script upgrade V2.1: Add nullable columns',
    0, 63, -1, 64, null, 'UNKOWN', 1, 1, 'Script completed'
  );