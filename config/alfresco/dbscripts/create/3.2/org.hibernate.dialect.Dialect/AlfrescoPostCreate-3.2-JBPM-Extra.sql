--
-- Title:      Fix jbpm tables
-- Database:   Generic
-- Since:      V3.2 schema 2013
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This patch is only required to fix the 'configuration_' column in JBPM on DB2.     
--

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-JBPM-Extra';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-JBPM-Extra', 'Manually executed script upgrade V3.2 fix problems in jbpm tables.',
     0, 2012, -1, 2013, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );