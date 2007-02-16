--
-- Title:      AVM Foreign Key indexes
-- Database:   MySQL
-- Since:      V2.0 Schema 38
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- The MySQL dialects apply the FK indexes by default

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.0-AVMFKIndexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.0-AVMFKIndexes', 'Manually executed script upgrade V2.0: AVM Foreign Key Indexes',
    0, 37, -1, 38, now(), 'UNKOWN', 1, 1, 'Script completed'
  );