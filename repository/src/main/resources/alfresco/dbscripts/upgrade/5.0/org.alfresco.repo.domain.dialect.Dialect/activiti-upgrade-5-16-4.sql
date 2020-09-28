--
-- Title:      Upgraded Activiti tables from 5.16.2 to 5.16.4 version
-- Database:   Generic
-- Since:      V5.0 Schema 8009
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

update ACT_GE_PROPERTY set VALUE_ = '5.16.4.0' where NAME_ = 'schema.version';

create index ACT_IDX_HI_PROCVAR_TASK_ID on ACT_HI_VARINST(TASK_ID_);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.0-upgrade-to-activiti-5.16.4';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.0-upgrade-to-activiti-5.16.4', 'Manually executed script upgrade V5.0: Upgraded Activiti tables to 5.16.4 version',
    0, 8008, -1, 8009, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );