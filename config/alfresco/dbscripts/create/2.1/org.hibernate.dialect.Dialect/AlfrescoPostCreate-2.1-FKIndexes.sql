--
-- Title:      Post-Create Foreign Key indexes
-- Database:   Generic
-- Since:      V2.0 Schema 63
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- JBPM
--   JBPM tables have explicit indexes where required.  Adding FK indexes here
--   leads to duplication, and on MySQL, this is exactly what happens.

-- Repository
CREATE INDEX FKFFF41F9960601995 ON alf_access_control_entry (permission_id);
CREATE INDEX FKFFF41F99B25A50BF ON alf_access_control_entry (authority_id);
CREATE INDEX FKFFF41F99B9553F6C ON alf_access_control_entry (acl_id);
CREATE INDEX FK8A749A657B7FDE43 ON alf_auth_ext_keys (id);
CREATE INDEX FKFFC5468E74173FF4 ON alf_child_assoc (child_node_id);
CREATE INDEX FKFFC5468E8E50E582 ON alf_child_assoc (parent_node_id);
CREATE INDEX FK60EFB626B9553F6C ON alf_node (acl_id);
CREATE INDEX FK60EFB626D24ADD25 ON alf_node (protocol, identifier);
CREATE INDEX FK7D4CF8EC7F2C8017 ON alf_node_properties (node_id);
CREATE INDEX FK7D4CF8EC40E780DC ON alf_node_properties (attribute_value);
CREATE INDEX FKD654E027F2C8017  ON alf_node_aspects (node_id);
CREATE INDEX FKE1A550BCB69C43F3 ON alf_node_assoc (source_node_id);
CREATE INDEX FKE1A550BCA8FC7769 ON alf_node_assoc (target_node_id);
CREATE INDEX FK71C2002B7F2C8017 ON alf_node_status (node_id);
CREATE INDEX FKBD4FF53D22DBA5BA ON alf_store (root_node_id);

--
-- Transaction tables
--
CREATE INDEX FK71C2002B9E57C13D ON alf_node_status (transaction_id);
CREATE INDEX FKB8761A3A9AE340B7 ON alf_transaction (server_id);

--
-- Audit tables
--
CREATE INDEX FKEAD1817484342E39 ON alf_audit_fact (audit_date_id);
CREATE INDEX FKEAD18174A0F9B8D9 ON alf_audit_fact (audit_source_id);
CREATE INDEX FKEAD18174F524CFD7 ON alf_audit_fact (audit_conf_id);

--
-- Attribute tables
--
CREATE INDEX fk_attr_n_acl      ON alf_attributes (acl_id);
--CREATE INDEX FK64D0B9CF69B9F16A ON alf_global_attributes (attribute);  This is auto-indexed
CREATE INDEX FKC7D52FB0ACD8822C ON alf_list_attribute_entries (list_id);
CREATE INDEX FKC7D52FB02C5AB86C ON alf_list_attribute_entries (attribute_id);
CREATE INDEX FK335CAE262C5AB86C ON alf_map_attribute_entries (attribute_id);
CREATE INDEX FK335CAE26AEAC208C ON alf_map_attribute_entries (map_id);

-- AVM
CREATE INDEX fk_avm_asp_node    ON avm_aspects (node_id);
CREATE INDEX FKD3FD9F95EDCD4A96 ON avm_aspects_new (id);
CREATE INDEX fk_avm_ce_child    ON avm_child_entries (child_id);
CREATE INDEX fk_avm_ce_parent   ON avm_child_entries (parent_id);
CREATE INDEX fk_avm_hl_desc     ON avm_history_links (descendent);
CREATE INDEX fk_avm_hl_ancestor ON avm_history_links (ancestor);
CREATE INDEX fk_avm_ml_from     ON avm_merge_links (mfrom);
CREATE INDEX fk_avm_ml_to       ON avm_merge_links (mto);
CREATE INDEX FK44A37C8A6BD529F3 ON avm_node_properties_new (node_id);
CREATE INDEX fk_avm_np_node     ON avm_node_properties (node_id);
CREATE INDEX fk_avm_n_acl       ON avm_nodes (acl_id);
CREATE INDEX fk_avm_n_store     ON avm_nodes (store_new_id);
CREATE INDEX fk_avm_sp_store    ON avm_store_properties (avm_store_id);
CREATE INDEX fk_avm_s_root      ON avm_stores (current_root_id);
CREATE INDEX fk_avm_vr_store    ON avm_version_roots (avm_store_id);
CREATE INDEX fk_avm_vr_root     ON avm_version_roots (root_id);
