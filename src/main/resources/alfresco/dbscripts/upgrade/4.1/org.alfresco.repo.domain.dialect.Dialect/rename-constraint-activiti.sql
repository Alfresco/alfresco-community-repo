--
-- Title:      Update ACT_HI_PROCINST table. Create normal name for unique constraint on PROC_INST_ID_
-- Database:   Generic
-- Since:      V4.1 Schema 5116
-- Author:     Dmitry Vaserin
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-15828 : DB2: unexpected index found in database.

-- Patch is applied only for DB2, see ALF-15828

--
-- Record script finish
--


DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-rename-constraint-activiti';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-rename-constraint-activiti', 'Manually executed script upgrade V4.1: Rename PROC_INST_ID_ constraint',
    0, 6020, -1, 6021, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );