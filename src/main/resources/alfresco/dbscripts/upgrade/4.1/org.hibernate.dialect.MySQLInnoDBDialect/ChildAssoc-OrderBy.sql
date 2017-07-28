--
-- Title:      Add new index to alf_child_assoc
-- Database:   MySQL
-- Since:      V4.1 Schema 5123
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-9053 : Alfresco site listings cause high CPU usage on database server

DROP INDEX fk_alf_cass_pnode ON alf_child_assoc;
CREATE INDEX idx_alf_cass_pnode ON alf_child_assoc (parent_node_id, assoc_index, id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-ChildAssoc-OrderBy';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-ChildAssoc-OrderBy', 'MNT-9053 : Alfresco site listings cause high CPU usage on database server',
    0, 6032, -1, 6033, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );