--
-- Title:      Change Oracle LONG RAW columns to BLOB
-- Database:   Generic
-- Since:      V2.2 Schema 92
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.

-- TODO: This needs to be replaced with a creation of new tables, copying values over with TO_LOB and then
--       renaming the values back.

ALTER TABLE alf_attributes MODIFY (serializable_value BLOB NULL);
ALTER TABLE avm_node_properties MODIFY (serializable_value BLOB NULL);
ALTER TABLE avm_node_properties_new MODIFY (serializable_value BLOB NULL);
ALTER TABLE avm_store_properties MODIFY (serializable_value BLOB NULL);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-OrclBLOB';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-OrclBLOB', 'Modified serializable_value columns from LONG RAW to BLOB.',
    0, 91, -1, 92, null, 'UNKOWN', 1, 1, 'Script completed'
  );