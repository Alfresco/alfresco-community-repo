--
-- Title:      Additional index for activiti on historic activity (PROC_INST_ID_ and ACTIVITY_ID_)
-- Database:   Generic
-- Since:      V4.2 Schema 6022
-- Author:     Frederik Heremans
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Add index to historic activity table
create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_); --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-add-activti-index-historic-activity';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-add-activti-index-historic-activity', 'Additional index created on ACT_HI_ACTINST',
     0, 6021, -1, 6022, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );