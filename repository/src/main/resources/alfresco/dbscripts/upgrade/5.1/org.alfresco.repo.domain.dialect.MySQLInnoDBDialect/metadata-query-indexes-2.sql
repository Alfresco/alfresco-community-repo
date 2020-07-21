--
-- Title:      Update alf_node_properties to support in-transaction metadata queries on remaining property types
-- Database:   Generic
-- Since:      V5.1 Schema 9004
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX idx_alf_nprop_b ON alf_node_properties (qname_id, boolean_value);  --(optional)
CREATE INDEX idx_alf_nprop_f ON alf_node_properties (qname_id, float_value);  --(optional)
CREATE INDEX idx_alf_nprop_d ON alf_node_properties (qname_id, double_value);  --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.1-metadata-query-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.1-metadata-query-indexes', 'Manually executed script upgrade V5.1: Updates for metadata query',
    0, 9003, -1, 9004, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );