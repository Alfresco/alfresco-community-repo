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

--
-- ALF-3546: patch.db-V3.2-AddFKIndexes is failing on MSSQL Server 2008 
--           Made the 'alf_access_control_entry' indexes optional
CREATE INDEX fk_alf_ace_auth ON alf_access_control_entry (authority_id);   -- (optional)
CREATE INDEX fk_alf_ace_perm ON alf_access_control_entry (permission_id);  -- (optional)
CREATE INDEX fk_alf_ace_ctx ON alf_access_control_entry (context_id);      -- (optional)

CREATE INDEX fk_alf_acl_acs ON alf_access_control_list (acl_change_set);

CREATE INDEX fk_alf_aclm_acl ON alf_acl_member (acl_id);
CREATE INDEX fk_alf_aclm_ace ON alf_acl_member (ace_id);

CREATE INDEX fk_alf_attr_acl ON alf_attributes (acl_id);

CREATE INDEX fk_alf_autha_ali ON alf_authority_alias (alias_id);
CREATE INDEX fk_alf_autha_aut ON alf_authority_alias (auth_id);

CREATE INDEX fk_alf_cass_pnode ON alf_child_assoc (parent_node_id);
CREATE INDEX fk_alf_cass_cnode ON alf_child_assoc (child_node_id);

-- alf_global_attributes.attribute is declared unique.  Indexes may automatically have been created.
CREATE INDEX fk_alf_gatt_att ON alf_global_attributes (attribute);  -- (optional)

CREATE INDEX fk_alf_lent_att ON alf_list_attribute_entries (attribute_id);
CREATE INDEX fk_alf_lent_latt ON alf_list_attribute_entries (list_id);

CREATE INDEX fk_alf_matt_matt ON alf_map_attribute_entries (map_id);
CREATE INDEX fk_alf_matt_att ON alf_map_attribute_entries (attribute_id);

CREATE INDEX fk_alf_node_acl ON alf_node (acl_id);
CREATE INDEX fk_alf_node_txn ON alf_node (transaction_id);
CREATE INDEX fk_alf_node_store ON alf_node (store_id);

CREATE INDEX fk_alf_nasp_n ON alf_node_aspects (node_id);

CREATE INDEX fk_alf_nass_snode ON alf_node_assoc (source_node_id);
CREATE INDEX fk_alf_nass_tnode ON alf_node_assoc (target_node_id);

CREATE INDEX fk_alf_nprop_n ON alf_node_properties (node_id);

-- Optional: Present in AlfrescoCreate-3.3-RepoTables.sql for create but required during upgrade
CREATE INDEX fk_alf_qname_ns ON alf_qname (ns_id);    -- (optional)

CREATE INDEX fk_alf_store_root ON alf_store (root_node_id);

CREATE INDEX fk_alf_txn_svr ON alf_transaction (server_id);

-- Optional: Present in various other patches
CREATE INDEX fk_alf_usaged_n ON alf_usage_delta (node_id);    -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-AddFKIndexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-AddFKIndexes', 'Fixes ALF-3189: Added missing FK indexes. Note: The script is empty for MySQL.',
     3007, 4012, -1, 4013, null, 'UNKOWN', ${true}, ${true}, 'Script completed'
   );
