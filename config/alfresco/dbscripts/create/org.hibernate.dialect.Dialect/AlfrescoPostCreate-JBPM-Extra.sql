--
-- Title:      Fix jbpm tables
-- Database:   Generic
-- Since:      V3.3 schema 4013
-- Author:     janv
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This patch is only required to fix JBPM columns on DB2.
--

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-JBPM-Extra';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-JBPM-Extra', 'Manually executed script upgrade V3.3 fix problems in JBPM tables',
     0, 4105, -1, 4106, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );