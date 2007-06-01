--
-- Title:      Fill 'version' columns with data
-- Database:   Generic
-- Since:      V2.1 Schema 54
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

UPDATE alf_store SET version = 1 WHERE version IS NULL;
UPDATE alf_node SET version = 1 WHERE version IS NULL;
UPDATE alf_child_assoc SET version = 1 WHERE version IS NULL;
UPDATE alf_node_assoc SET version = 1 WHERE version IS NULL;
UPDATE alf_node_status SET version = 1 WHERE version IS NULL;
UPDATE alf_transaction SET version = 1 WHERE version IS NULL;
UPDATE alf_server SET version = 1 WHERE version IS NULL;
UPDATE alf_access_control_list SET version = 1 WHERE version IS NULL;
UPDATE alf_access_control_entry SET version = 1 WHERE version IS NULL;
UPDATE alf_permission SET version = 1 WHERE version IS NULL;
UPDATE alf_authority SET version = 1 WHERE version IS NULL;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-VersionColumns';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-VersionColumns', 'Manually executed script upgrade V2.1: Created initial version number for ADM entities',
    0, 53, -1, 54, null, 'UNKOWN', 1, 1, 'Script completed'
  );