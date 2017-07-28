--
-- Title:      Remove unnecessary column for Activiti
-- Database:   Generic
-- Since:      V4.1 Schema 5115
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-16038 : DB2: Upgrade script needed to remove ALFUSER.ACT_HI_ACTINST.OWNER_

-- Patch is applied only for DB2, see ALF-16038

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-remove-column-activiti';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-remove-column-activiti', 'ALF-16038 : DB2: Upgrade script to remove ALFUSER.ACT_HI_ACTINST.OWNER_',
    0, 6017, -1, 6018, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );