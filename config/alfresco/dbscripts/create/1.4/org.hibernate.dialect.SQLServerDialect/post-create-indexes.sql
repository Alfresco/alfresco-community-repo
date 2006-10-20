--
-- Add post-creation indexes. (SQL Server Schema 1.4)
--
CREATE INDEX FKFFF41F9960601995 ON alf_access_control_entry (permission_id);
CREATE INDEX FKFFF41F99B25A50BF ON alf_access_control_entry (authority_id);
CREATE INDEX FKFFF41F99B9553F6C ON alf_access_control_entry (acl_id);
CREATE INDEX FK8A749A657B7FDE43 ON alf_auth_ext_keys (id);
CREATE INDEX FKFFC5468E74173FF4 ON alf_child_assoc (child_node_id);
CREATE INDEX FKFFC5468E8E50E582 ON alf_child_assoc (parent_node_id);
CREATE INDEX FK60EFB626B9553F6C ON alf_node (acl_id);
CREATE INDEX FK60EFB626D24ADD25 ON alf_node (protocol, identifier);
CREATE INDEX FK7D4CF8EC7F2C8017 ON alf_node_properties (node_id);
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
-- New audit tables
--
CREATE INDEX FKEAD1817484342E39 ON alf_audit_fact (audit_date_id);
CREATE INDEX FKEAD18174A0F9B8D9 ON alf_audit_fact (audit_source_id);
CREATE INDEX FKEAD18174F524CFD7 ON alf_audit_fact (audit_conf_id);
