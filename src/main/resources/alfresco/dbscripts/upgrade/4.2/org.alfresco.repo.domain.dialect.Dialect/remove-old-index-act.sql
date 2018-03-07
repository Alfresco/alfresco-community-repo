--
-- Title:      Delete unnecessary indexes add with older version of Activiti in 4.0 branch
-- Database:   Generic
-- Since:      V4.2 Schema 6053
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-10646 : DB2 & MSSQL: Unexpected index found in database after upgrade 4.1.7.3 to 4.2.1

-- No-op

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