--
-- Title:      Ensure that all Foreign Key indexes are present
-- Database:   MySQL InnoDB
-- Since:      V2.1 Schema 64
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- The MySQL InnoDB Dialect has special support for foreign keys.

-- Remove pointless duplicated FK indexes
ALTER TABLE alf_global_attributes DROP INDEX FK64D0B9CF69B9F16A;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-FKIndexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-FKIndexes', 'Manually executed script upgrade V2.1: Ensure existence of V2.1 FK indexes',
    0, 63, -1, 64, null, 'UNKOWN', 1, 1, 'Script completed'
  );