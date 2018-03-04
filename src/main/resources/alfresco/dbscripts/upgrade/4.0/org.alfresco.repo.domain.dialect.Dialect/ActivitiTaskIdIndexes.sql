--
-- Title:      Add missing Activiti indexes on task-id for runtime and history variables.
-- Database:   Generic
-- Since:      V4.0 Schema 5029
-- Author:     Frederik Heremans
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Add index to runtime variable table
create index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE(TASK_ID_); --(optional)

-- Add index to history variable table
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_); --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.0-Activiti-task-id-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.0-Activiti-task-id-indexes', 'Manually executed script upgrade V4.0: Add missing Activiti indexes on task-id',
     0, 6003, -1, 6004, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );