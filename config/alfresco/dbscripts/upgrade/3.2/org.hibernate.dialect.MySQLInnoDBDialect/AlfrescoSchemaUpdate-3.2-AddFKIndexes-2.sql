--
-- Title:      Upgrade to V3.2 - Add extra FK indexes 
-- Database:   MySQL
-- Since:      V3.2 schema 3503
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-5396: Missing FK indexes on non-MySQL databases
-- Not relevant to MySQL


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-AddFKIndexes-2';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-AddFKIndexes-2', 'Script fix for ALF-5396: Missing FK indexes on non-MySQL databases',
     0, 4111, -1, 4112, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
