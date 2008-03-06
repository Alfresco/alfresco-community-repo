--
-- Title:      Post-Create Indexes
-- Database:   Generic
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Hibernate only generates indexes on foreign key columns for MySQL.
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX fk_alf_acl_entr ON alf_access_control_entry (acl_id);
CREATE INDEX fk_alf_ace_perm ON alf_access_control_entry (permission_id);
CREATE INDEX fk_alf_ace_auth ON alf_access_control_entry (authority_id);

CREATE INDEX fk_alf_attr_acl ON alf_attributes (acl_id);

CREATE INDEX fk_alf_adtf_src ON alf_audit_fact (audit_source_id);
CREATE INDEX fk_alf_adtf_date ON alf_audit_fact (audit_date_id);
CREATE INDEX fk_alf_adtf_conf ON alf_audit_fact (audit_conf_id);

CREATE INDEX fk_alf_auth_ext ON alf_auth_ext_keys (id);

CREATE INDEX fk_alf_ca_pnode ON alf_child_assoc (parent_node_id);
CREATE INDEX fk_alf_ca_tqn ON alf_child_assoc (type_qname_id);
CREATE INDEX fk_alf_ca_qn_ns ON alf_child_assoc (qname_ns_id);
CREATE INDEX fk_alf_ca_cnode ON alf_child_assoc (child_node_id);

CREATE INDEX fk_alf_gatt_att ON alf_global_attributes (attribute);

CREATE INDEX fk_alf_lent_att ON alf_list_attribute_entries (attribute_id);

CREATE INDEX fk_alf_lent_latt ON alf_list_attribute_entries (list_id);

CREATE INDEX fk_alf_matt_matt ON alf_map_attribute_entries (map_id);
CREATE INDEX fk_alf_matt_att ON alf_map_attribute_entries (attribute_id);

CREATE INDEX fk_alf_n_acl ON alf_node (acl_id);
CREATE INDEX fk_alf_n_tqname ON alf_node (type_qname_id);
CREATE INDEX fk_alf_n_store ON alf_node (protocol, identifier);

CREATE INDEX fk_alf_n_asp ON alf_node_aspects (node_id);
CREATE INDEX fk_alf_na_snode ON alf_node_assoc (source_node_id);

CREATE INDEX fk_alf_na_tqn ON alf_node_assoc (type_qname_id);
CREATE INDEX fk_alf_na_tnode ON alf_node_assoc (target_node_id);

CREATE INDEX fk_alf_n_prop ON alf_node_properties (node_id);
CREATE INDEX fk_alf_np_attr ON alf_node_properties (attribute_value);

CREATE INDEX fk_alf_ns_node ON alf_node_status (node_id);
CREATE INDEX fk_alf_ns_trans ON alf_node_status (transaction_id);

CREATE INDEX fk_alf_qname_ns ON alf_qname (ns_id);

CREATE INDEX fk_alf_store_rn ON alf_store (root_node_id);

CREATE INDEX fk_alf_txn_svr ON alf_transaction (server_id);

CREATE INDEX fk_avm_asp_node ON avm_aspects (node_id);
CREATE INDEX fk_avm_asp_n ON avm_aspects_new (id);

CREATE INDEX fk_avm_ce_child ON avm_child_entries (child_id);
CREATE INDEX fk_avm_ce_parent ON avm_child_entries (parent_id);

CREATE INDEX fk_avm_hl_desc ON avm_history_links (descendent);
CREATE INDEX fk_avm_hl_ancestor ON avm_history_links (ancestor);

CREATE INDEX fk_avm_ml_to ON avm_merge_links (mto);
CREATE INDEX fk_avm_ml_from ON avm_merge_links (mfrom);

CREATE INDEX fk_avm_np_node ON avm_node_properties (node_id);
CREATE INDEX fk_avm_np_n ON avm_node_properties_new (node_id);

CREATE INDEX fk_avm_n_acl ON avm_nodes (acl_id);
CREATE INDEX fk_avm_n_store ON avm_nodes (store_new_id);

CREATE INDEX fk_avm_sp_qname ON avm_store_properties (qname_id);
CREATE INDEX fk_avm_sp_store ON avm_store_properties (avm_store_id);

CREATE INDEX fk_avm_s_root ON avm_stores (current_root_id);

CREATE INDEX fk_avm_vlne_vr ON avm_version_layered_node_entry (version_root_id);

CREATE INDEX fk_avm_vr_root ON avm_version_roots (root_id);
CREATE INDEX fk_avm_vr_store ON avm_version_roots (avm_store_id);
