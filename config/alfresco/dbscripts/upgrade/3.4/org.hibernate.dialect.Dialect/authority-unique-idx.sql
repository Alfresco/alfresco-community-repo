--
-- Title:      Upgrade to V3.4 - Ensure existence of unique index on alf_authority (DB2)
-- Database:   Generic
-- Since:      V3.4 schema 4100
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-authority-unique-idx';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-authority-unique-idx', 'Manually executed script upgrade V3.4',
     0, 4099, -1, 4100, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
