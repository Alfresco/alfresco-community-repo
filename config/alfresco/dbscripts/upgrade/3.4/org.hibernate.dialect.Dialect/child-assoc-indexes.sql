--
-- Title:      Upgrade to V3.4 - Add alf_child_assoc.idx_alf_cass_pri index
-- Database:   Generic
-- Since:      V3.4 schema 4105
-- Author:     unknown
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX idx_alf_cass_pri ON alf_child_assoc (parent_node_id, is_primary, child_node_id); --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-child-assoc-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-child-assoc-indexes', 'Manually executed script upgrade V3.4',
     0, 4104, -1, 4105, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
