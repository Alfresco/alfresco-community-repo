--
-- Title:      AVM Foreign Key indexes
-- Database:   PostgreSQL
-- Since:      V2.0 Schema 38
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX fk_avm_asp_node ON avm_aspects (node_id);
CREATE INDEX fk_avm_ce_child ON avm_child_entries (child_id);
CREATE INDEX fk_avm_ce_parent ON avm_child_entries (parent_id);
CREATE INDEX fk_avm_hl_desc ON avm_history_links (descendent);
CREATE INDEX fk_avm_hl_ancestor ON avm_history_links (ancestor);
CREATE INDEX fk_avm_ml_from ON avm_merge_links (mfrom);
CREATE INDEX fk_avm_ml_to ON avm_merge_links (mto);
CREATE INDEX fk_avm_np_node ON avm_node_properties (node_id);
CREATE INDEX fk_avm_n_acl ON avm_nodes (acl_id);
CREATE INDEX fk_avm_n_store ON avm_nodes (store_new_id);
CREATE INDEX fk_avm_sp_store ON avm_store_properties (avm_store_id);
CREATE INDEX fk_avm_s_root ON avm_stores (current_root_id);
CREATE INDEX fk_avm_vr_store ON avm_version_roots (avm_store_id);
CREATE INDEX fk_avm_vr_root ON avm_version_roots (root_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.0-AVMFKIndexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.0-AVMFKIndexes', 'Manually executed script upgrade V2.0: AVM Foreign Key Indexes',
    0, 37, -1, 38, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );