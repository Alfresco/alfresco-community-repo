--
-- Title:      Increase column sizes for Activiti
-- Database:   Generic
-- Since:      V4.1 Schema 5112
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-14983 : Upgrade scripts to increase column sizes for Activiti

CREATE TABLE ACT_RU_TASK_TMP LIKE ACT_RU_TASK;
ALTER TABLE ACT_RU_TASK_TMP MODIFY ASSIGNEE_ VARCHAR(255);
ALTER TABLE ACT_RU_TASK_TMP MODIFY OWNER_ VARCHAR(255);

INSERT INTO ACT_RU_TASK_TMP SELECT * FROM ACT_RU_TASK;

-- Drop referencing FK's
ALTER TABLE ACT_RU_IDENTITYLINK DROP FOREIGN KEY ACT_FK_TSKASS_TASK;
DROP TABLE ACT_RU_TASK CASCADE; 
RENAME TABLE ACT_RU_TASK_TMP to ACT_RU_TASK;

CREATE TABLE ACT_RU_IDENTITYLINK_TMP LIKE ACT_RU_IDENTITYLINK;
ALTER TABLE ACT_RU_IDENTITYLINK_TMP MODIFY GROUP_ID_ VARCHAR(255);
ALTER TABLE ACT_RU_IDENTITYLINK_TMP MODIFY USER_ID_ VARCHAR(255);
INSERT INTO ACT_RU_IDENTITYLINK_TMP SELECT * FROM ACT_RU_IDENTITYLINK;

DROP TABLE ACT_RU_IDENTITYLINK CASCADE; 
RENAME TABLE ACT_RU_IDENTITYLINK_TMP to ACT_RU_IDENTITYLINK;

-- Recreate dropped ACT_FK_TSKASS_TASK after identitylink table has been rebuilt
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);


CREATE TABLE ACT_HI_TASKINST_TMP LIKE ACT_HI_TASKINST;
ALTER TABLE ACT_HI_TASKINST_TMP MODIFY ASSIGNEE_ VARCHAR(255);
ALTER TABLE ACT_HI_TASKINST_TMP MODIFY OWNER_ VARCHAR(255);
INSERT INTO ACT_HI_TASKINST_TMP SELECT * FROM ACT_HI_TASKINST;
DROP TABLE ACT_HI_TASKINST CASCADE; 
RENAME TABLE ACT_HI_TASKINST_TMP to ACT_HI_TASKINST;

CREATE TABLE ACT_HI_ACTINST_TMP LIKE ACT_HI_ACTINST;
ALTER TABLE ACT_HI_ACTINST_TMP MODIFY ASSIGNEE_ VARCHAR(255);
INSERT INTO ACT_HI_ACTINST_TMP SELECT * FROM ACT_HI_ACTINST;
DROP TABLE ACT_HI_ACTINST CASCADE; 
RENAME TABLE ACT_HI_ACTINST_TMP to ACT_HI_ACTINST;


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