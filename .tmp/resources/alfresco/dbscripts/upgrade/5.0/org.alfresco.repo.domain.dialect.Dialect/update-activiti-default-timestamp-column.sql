--
-- Title:      Update Activiti tables ACT_RE_DEPLOYMENT and ACT_RU_TASK with default timestamp value with NULL
-- Database:   Generic
-- Since:      V5.0 Schema 8034
-- Author:     Ramona Popa
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.0-update-activiti-default-timestamp-column';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.0-update-activiti-default-timestamp-column', 'Manually executed script upgrade V5.0: Upgraded Activiti tables ACT_RE_DEPLOYMENT and ACT_RU_TASK for default timestamp value with NULL',
    0, 8033, -1, 8034, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );