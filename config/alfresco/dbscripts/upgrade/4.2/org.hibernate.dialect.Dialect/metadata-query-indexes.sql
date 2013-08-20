--
-- Title:      Update alf_node, alf_node_properties and alf_content_url to support in-transaction metadata queries
-- Database:   Generic
-- Since:      V4.2 Schema 6024
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX idx_alf_node_mdq ON alf_node (store_id, type_qname_id, id);  --(optional)
CREATE INDEX idx_alf_node_cor ON alf_node (audit_creator, store_id, type_qname_id, id);  --(optional)
CREATE INDEX idx_alf_node_crd ON alf_node (audit_created, store_id, type_qname_id, id);  --(optional)
CREATE INDEX idx_alf_node_mor ON alf_node (audit_modifier, store_id, type_qname_id, id);  --(optional)
CREATE INDEX idx_alf_node_mod ON alf_node (audit_modified, store_id, type_qname_id, id);  --(optional)

CREATE INDEX idx_alf_nprop_s ON alf_node_properties (qname_id, string_value, node_id);  --(optional)
CREATE INDEX idx_alf_nprop_l ON alf_node_properties (qname_id, long_value, node_id);  --(optional)

CREATE INDEX idx_alf_conturl_sz ON alf_content_url (content_size, id);  --(optional)


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-metadata-query-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-metadata-query-indexes', 'Manually executed script upgrade V4.2: Updates for metadata query',
    0, 6023, -1, 6024, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );