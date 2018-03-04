--
-- Title:      Increase column sizes for Activiti
-- Database:   MySQL
-- Since:      V4.1 Schema 5112
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-14983 : Upgrade scripts to increase column sizes for Activiti

ALTER TABLE ACT_RU_TASK MODIFY ASSIGNEE_ VARCHAR(255);
ALTER TABLE ACT_RU_TASK MODIFY OWNER_ VARCHAR(255);
ALTER TABLE ACT_RU_IDENTITYLINK MODIFY GROUP_ID_ VARCHAR(255);
ALTER TABLE ACT_RU_IDENTITYLINK MODIFY USER_ID_ VARCHAR(255);
ALTER TABLE ACT_HI_TASKINST MODIFY ASSIGNEE_ VARCHAR(255);
ALTER TABLE ACT_HI_TASKINST MODIFY OWNER_ VARCHAR(255);
ALTER TABLE ACT_HI_ACTINST MODIFY ASSIGNEE_ VARCHAR(255);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-increase-column-size-activiti';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-increase-column-size-activiti', 'ALF-14983 : Upgrade scripts to increase column sizes for Activiti',
    0, 6012, -1, 6013, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );