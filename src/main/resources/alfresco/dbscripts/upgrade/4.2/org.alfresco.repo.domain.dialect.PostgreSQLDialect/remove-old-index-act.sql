--
-- Title:      Delete unnecessary indexes add with older version of Activiti in 4.0 branch
-- Database:   PostgreSQL
-- Since:      V4.2 Schema 6053
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-10646 : DB2 & MSSQL: Unexpected index found in database after upgrade 4.1.7.3 to 4.2.1

DROP index ACT_IDX_VARIABLE_BA; --(optional)
DROP index ACT_IDX_VARIABLE_EXEC; --(optional)
DROP index ACT_IDX_VARIABLE_PROCINST; --(optional)
DROP index ACT_IDX_TASK_PROC_DEF_ID; --(optional)
DROP index ACT_IDX_JOB_EXCEPTION_STACK_ID; --(optional)
DROP index ACT_IDX_IDENT_LNK_TASK; --(optional)
DROP index ACT_IDX_EXECUTION_PARENT; --(optional)
DROP index ACT_IDX_EXECUTION_SUPER; --(optional)
DROP index ACT_IDX_MEMBERSHIP_GROUP ; --(optional)
DROP index ACT_IDX_MEMBERSHIP_USER; --(optional)
DROP index ACT_IDX_BYTEARRAY_; --(optional)

--
-- Record script finish
--
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-remove-old-index-act', 'Manually executed script upgrade V4.2: Delete unnecessary indexes add with older version of Activiti in 4.0 branch',
    0, 6027, -1, 6028, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );

