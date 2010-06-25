--
-- Title:      Upgrade to V3.3
-- Database:   DB2
-- Since:      V3.3 schema 4106
-- Author:     janv
--
-- This patch is only required to fix column on DB2.

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-Node-Prop-Serializable';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-Node-Prop-Serializable', 'Manually executed script upgrade V3.3',
     0, 4105, -1, 4106, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
