--
-- Title:      Remote all pre-2.2 indexes and constraints
-- Database:   MySQL
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

ALTER TABLE alf_access_control_entry DROP INDEX fk_alf_ace_ctx, DROP FOREIGN KEY fk_alf_ace_ctx;

ALTER TABLE alf_access_control_entry DROP INDEX fk_alf_ace_perm, DROP FOREIGN KEY fk_alf_ace_perm;

ALTER TABLE alf_access_control_entry DROP INDEX fk_alf_ace_auth, DROP FOREIGN KEY fk_alf_ace_auth;

ALTER TABLE alf_access_control_list DROP INDEX fk_alf_acl_acs, DROP FOREIGN KEY fk_alf_acl_acs;

ALTER TABLE alf_access_control_list DROP INDEX idx_alf_acl_inh;

ALTER TABLE alf_acl_member DROP INDEX fk_alf_aclm_acl, DROP FOREIGN KEY fk_alf_aclm_acl;

ALTER TABLE alf_acl_member DROP INDEX fk_alf_aclm_ace, DROP FOREIGN KEY fk_alf_aclm_ace;

-- Table might not exist
ALTER TABLE alf_attributes DROP INDEX fk_attributes_n_acl, DROP FOREIGN KEY fk_attributes_n_acl;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_woy_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_date_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_y_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_q_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_m_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_dow_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_doy_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_dom_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_hy_idx;  -- (optional)

ALTER TABLE alf_audit_date DROP INDEX adt_wom_idx;  -- (optional)

ALTER TABLE alf_audit_fact DROP INDEX adt_user_idx;  -- (optional)

ALTER TABLE alf_audit_fact DROP INDEX adt_store_idx;  -- (optional)

ALTER TABLE alf_audit_fact DROP INDEX FKEAD18174A0F9B8D9, DROP FOREIGN KEY FKEAD18174A0F9B8D9;

ALTER TABLE alf_audit_fact DROP INDEX FKEAD1817484342E39, DROP FOREIGN KEY FKEAD1817484342E39;

ALTER TABLE alf_audit_fact DROP INDEX FKEAD18174F524CFD7, DROP FOREIGN KEY FKEAD18174F524CFD7;

ALTER TABLE alf_audit_source DROP INDEX app_source_app_idx;  -- (optional)

ALTER TABLE alf_audit_source DROP INDEX app_source_ser_idx;  -- (optional)

ALTER TABLE alf_audit_source DROP INDEX app_source_met_idx;  -- (optional)

ALTER TABLE alf_authority DROP INDEX idx_alf_auth_aut;

ALTER TABLE alf_authority_alias DROP INDEX fk_alf_autha_ali, DROP FOREIGN KEY fk_alf_autha_ali;

ALTER TABLE alf_authority_alias DROP INDEX fk_alf_autha_aut, DROP FOREIGN KEY fk_alf_autha_aut;

ALTER TABLE alf_child_assoc DROP INDEX FKFFC5468E8E50E582, DROP FOREIGN KEY FKFFC5468E8E50E582;

ALTER TABLE alf_child_assoc DROP INDEX FKFFC5468E74173FF4, DROP FOREIGN KEY FKFFC5468E74173FF4;

ALTER TABLE alf_global_attributes DROP FOREIGN KEY FK64D0B9CF69B9F16A; -- (optional)
ALTER TABLE alf_global_attributes DROP INDEX FK64D0B9CF69B9F16A; --(optional)

ALTER TABLE alf_list_attribute_entries DROP INDEX FKC7D52FB02C5AB86C, DROP FOREIGN KEY FKC7D52FB02C5AB86C; -- (optional)

ALTER TABLE alf_list_attribute_entries DROP INDEX FKC7D52FB0ACD8822C, DROP FOREIGN KEY FKC7D52FB0ACD8822C; -- (optional)

ALTER TABLE alf_map_attribute_entries DROP INDEX FK335CAE26AEAC208C, DROP FOREIGN KEY FK335CAE26AEAC208C; -- (optional)

ALTER TABLE alf_map_attribute_entries DROP INDEX FK335CAE262C5AB86C, DROP FOREIGN KEY FK335CAE262C5AB86C; -- (optional)

ALTER TABLE alf_node DROP INDEX FK60EFB626B9553F6C, DROP FOREIGN KEY FK60EFB626B9553F6C;

ALTER TABLE alf_node DROP INDEX FK60EFB626D24ADD25, DROP FOREIGN KEY FK60EFB626D24ADD25;

ALTER TABLE alf_node_aspects DROP INDEX FKD654E027F2C8017, DROP FOREIGN KEY FKD654E027F2C8017;

ALTER TABLE alf_node_assoc DROP INDEX FKE1A550BCB69C43F3, DROP FOREIGN KEY FKE1A550BCB69C43F3;

ALTER TABLE alf_node_assoc DROP INDEX FKE1A550BCA8FC7769, DROP FOREIGN KEY FKE1A550BCA8FC7769;

ALTER TABLE alf_node_properties DROP INDEX FK7D4CF8EC7F2C8017, DROP FOREIGN KEY FK7D4CF8EC7F2C8017;

ALTER TABLE alf_node_properties DROP INDEX FK7D4CF8EC40E780DC, DROP FOREIGN KEY FK7D4CF8EC40E780DC; -- (optional)

ALTER TABLE alf_node_status DROP INDEX FK71C2002B7F2C8017, DROP FOREIGN KEY FK71C2002B7F2C8017;

ALTER TABLE alf_node_status DROP INDEX FK71C2002B9E57C13D, DROP FOREIGN KEY FK71C2002B9E57C13D;

ALTER TABLE alf_store DROP INDEX FKBD4FF53D22DBA5BA, DROP FOREIGN KEY FKBD4FF53D22DBA5BA;

ALTER TABLE alf_transaction DROP INDEX idx_commit_time_ms; -- (optional)

ALTER TABLE alf_transaction DROP INDEX FKB8761A3A9AE340B7, DROP FOREIGN KEY FKB8761A3A9AE340B7;

ALTER TABLE avm_aspects DROP INDEX fk_avm_asp_node, DROP FOREIGN KEY fk_avm_asp_node; --(optional)

ALTER TABLE avm_aspects_new DROP INDEX FKD3FD9F95EDCD4A96, DROP FOREIGN KEY FKD3FD9F95EDCD4A96; -- (optional)

ALTER TABLE avm_child_entries DROP INDEX fk_avm_ce_child, DROP FOREIGN KEY fk_avm_ce_child; --(optional)

ALTER TABLE avm_child_entries DROP INDEX fk_avm_ce_parent, DROP FOREIGN KEY fk_avm_ce_parent; --(optional)

ALTER TABLE avm_history_links DROP INDEX fk_avm_hl_desc, DROP FOREIGN KEY fk_avm_hl_desc; --(optional)

ALTER TABLE avm_history_links DROP INDEX fk_avm_hl_ancestor, DROP FOREIGN KEY fk_avm_hl_ancestor; --(optional)

ALTER TABLE avm_history_links DROP INDEX idx_avm_hl_revpk; --(optional)

ALTER TABLE avm_merge_links DROP INDEX fk_avm_ml_to, DROP FOREIGN KEY fk_avm_ml_to; --(optional)

ALTER TABLE avm_merge_links DROP INDEX fk_avm_ml_from, DROP FOREIGN KEY fk_avm_ml_from; --(optional)

ALTER TABLE avm_node_properties DROP INDEX idx_avm_np_name; --(optional)

ALTER TABLE avm_node_properties DROP INDEX fk_avm_np_node, DROP FOREIGN KEY fk_avm_np_node; --(optional)

ALTER TABLE avm_node_properties_new DROP INDEX FK44A37C8A6BD529F3, DROP FOREIGN KEY FK44A37C8A6BD529F3; -- (optional)

ALTER TABLE avm_nodes DROP INDEX fk_avm_n_acl, DROP FOREIGN KEY fk_avm_n_acl; --(optional)

ALTER TABLE avm_nodes DROP INDEX fk_avm_n_store, DROP FOREIGN KEY fk_avm_n_store; --(optional)

ALTER TABLE avm_nodes DROP INDEX idx_avm_n_pi; --(optional)

ALTER TABLE avm_store_properties DROP INDEX idx_avm_sp_name; --(optional)

ALTER TABLE avm_store_properties DROP INDEX fk_avm_sp_store, DROP FOREIGN KEY fk_avm_sp_store; --(optional)

ALTER TABLE avm_stores DROP INDEX fk_avm_s_root, DROP FOREIGN KEY fk_avm_s_root; --(optional)

ALTER TABLE avm_version_layered_node_entry DROP INDEX FK182E672DEB9D70C, DROP FOREIGN KEY FK182E672DEB9D70C; -- (optional)

ALTER TABLE avm_version_roots DROP INDEX idx_avm_vr_version; --(optional)

ALTER TABLE avm_version_roots DROP INDEX idx_avm_vr_revuq; --(optional)

ALTER TABLE avm_version_roots DROP INDEX fk_avm_vr_root, DROP FOREIGN KEY fk_avm_vr_root; --(optional)

ALTER TABLE avm_version_roots DROP INDEX fk_avm_vr_store, DROP FOREIGN KEY fk_avm_vr_store; --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-1-DropIndexesAndConstraints';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-1-DropIndexesAndConstraints', 'Manually executed script upgrade V2.2: Remove pre-2.2 indexes and constraints',
    0, 120, -1, 121, null, 'UNKOWN', 1, 1, 'Script completed'
  );
