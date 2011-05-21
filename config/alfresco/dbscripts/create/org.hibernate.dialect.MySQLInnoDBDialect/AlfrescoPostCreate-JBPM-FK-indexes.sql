--
-- Title:      Upgrade to V3.4 - Add indexes for jbpm foreign keys
-- Database:   MySQL
-- Since:      V3.4 schema 4204
-- Author:     pavelyur
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- do nothing for mysql

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-JBPM-FK-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-JBPM-FK-indexes', 'Manually executed script upgrade to add FK indexes for JBPM',
     0, 4305, -1, 4306, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
