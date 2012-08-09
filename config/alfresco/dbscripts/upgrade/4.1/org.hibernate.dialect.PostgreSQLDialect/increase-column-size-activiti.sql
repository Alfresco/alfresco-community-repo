--
-- Title:      Increase column sizes for Activiti
-- Database:   PostgreSQL
-- Since:      V4.1 Schema 5112
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-14983 : Upgrade scripts to increase column sizes for Activiti

ALTER TABLE ACT_RU_TASK ALTER COLUMN ASSIGNEE_ TYPE VARCHAR(255);
ALTER TABLE ACT_RU_TASK ALTER COLUMN OWNER_ TYPE VARCHAR(255);
ALTER TABLE ACT_RU_IDENTITYLINK ALTER COLUMN GROUP_ID_ TYPE VARCHAR(255);
ALTER TABLE ACT_RU_IDENTITYLINK ALTER COLUMN USER_ID_ TYPE VARCHAR(255);
ALTER TABLE ACT_HI_TASKINST ALTER COLUMN ASSIGNEE_ TYPE VARCHAR(255);
ALTER TABLE ACT_HI_TASKINST ALTER COLUMN OWNER_ TYPE VARCHAR(255);
ALTER TABLE ACT_HI_ACTINST ALTER COLUMN ASSIGNEE_ TYPE VARCHAR(255);

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