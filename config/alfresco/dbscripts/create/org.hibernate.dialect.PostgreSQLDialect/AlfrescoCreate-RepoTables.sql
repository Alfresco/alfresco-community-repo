--
-- Title:      Core Repository Tables
-- Database:   PostgreSQL
-- Since:      V3.3 Schema 4000
-- Author:     unknown
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_applied_patch
(
    id VARCHAR(64) NOT NULL,
    description VARCHAR(1024),
    fixes_from_schema INT4,
    fixes_to_schema INT4,
    applied_to_schema INT4,
    target_schema INT4,
    applied_on_date TIMESTAMP,
    applied_to_server VARCHAR(64),
    was_executed BOOL,
    succeeded BOOL,
    report VARCHAR(1024),
    PRIMARY KEY (id)
);

CREATE SEQUENCE alf_locale_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_locale
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    locale_str VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)    
);
CREATE UNIQUE INDEX locale_str ON alf_locale (locale_str);

CREATE SEQUENCE alf_namespace_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_namespace
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    uri VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uri ON alf_namespace (uri);

CREATE SEQUENCE alf_qname_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_qname
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    ns_id INT8 NOT NULL,
    local_name VARCHAR(200) NOT NULL,
    CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id),    
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ns_id ON alf_qname (ns_id, local_name);

CREATE SEQUENCE alf_permission_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_permission
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
);
CREATE UNIQUE INDEX type_qname_id ON alf_permission (type_qname_id, name);
CREATE INDEX fk_alf_perm_tqn ON alf_permission (type_qname_id);

CREATE SEQUENCE alf_ace_context_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_ace_context
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    class_context VARCHAR(1024),
    property_context VARCHAR(1024),
    kvp_context VARCHAR(1024),
    PRIMARY KEY (id)
);

CREATE SEQUENCE alf_authority_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_authority
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    authority VARCHAR(100),
    crc INT8,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX authority ON alf_authority (authority, crc);
CREATE INDEX idx_alf_auth_aut ON alf_authority (authority);

CREATE SEQUENCE alf_access_control_entry_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_access_control_entry
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    permission_id INT8 NOT NULL,
    authority_id INT8 NOT NULL,
    allowed BOOL NOT NULL,
    applies INT4 NOT NULL,
    context_id INT8,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_ace_auth FOREIGN KEY (authority_id) REFERENCES alf_authority (id),
    CONSTRAINT fk_alf_ace_ctx FOREIGN KEY (context_id) REFERENCES alf_ace_context (id),
    CONSTRAINT fk_alf_ace_perm FOREIGN KEY (permission_id) REFERENCES alf_permission (id)
);
CREATE UNIQUE INDEX permission_id ON alf_access_control_entry (permission_id, authority_id, allowed, applies);
CREATE INDEX fk_alf_ace_ctx ON alf_access_control_entry (context_id);
CREATE INDEX fk_alf_ace_perm ON alf_access_control_entry (permission_id);
CREATE INDEX fk_alf_ace_auth ON alf_access_control_entry (authority_id);

CREATE SEQUENCE alf_acl_change_set_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_acl_change_set
(
    id INT8 NOT NULL,
    commit_time_ms INT8,
    PRIMARY KEY (id)
);
CREATE INDEX idx_alf_acs_ctms ON alf_acl_change_set (commit_time_ms);

CREATE SEQUENCE alf_access_control_list_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_access_control_list
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    acl_id VARCHAR(36)  NOT NULL,
    latest BOOL NOT NULL,
    acl_version INT8 NOT NULL,
    inherits BOOL NOT NULL,
    inherits_from INT8,
    type INT4 NOT NULL,
    inherited_acl INT8,
    is_versioned BOOL NOT NULL,
    requires_version BOOL NOT NULL,
    acl_change_set INT8,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_acl_acs FOREIGN KEY (acl_change_set) REFERENCES alf_acl_change_set (id)
);
CREATE UNIQUE INDEX acl_id ON alf_access_control_list (acl_id, latest, acl_version);
CREATE INDEX idx_alf_acl_inh ON alf_access_control_list (inherits, inherits_from);
CREATE INDEX fk_alf_acl_acs ON alf_access_control_list (acl_change_set);

CREATE SEQUENCE alf_acl_member_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_acl_member
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    acl_id INT8 NOT NULL,
    ace_id INT8 NOT NULL,
    pos INT4 NOT NULL,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_aclm_ace FOREIGN KEY (ace_id) REFERENCES alf_access_control_entry (id),
    CONSTRAINT fk_alf_aclm_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
);
CREATE UNIQUE INDEX aclm_acl_id ON alf_acl_member (acl_id, ace_id, pos);
CREATE INDEX fk_alf_aclm_acl ON alf_acl_member (acl_id);
CREATE INDEX fk_alf_aclm_ace ON alf_acl_member (ace_id);

CREATE SEQUENCE alf_authority_alias_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_authority_alias
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    auth_id INT8 NOT NULL,
    alias_id INT8 NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_autha_aut FOREIGN KEY (auth_id) REFERENCES alf_authority (id),
    CONSTRAINT fk_alf_autha_ali FOREIGN KEY (alias_id) REFERENCES alf_authority (id)
);
CREATE UNIQUE INDEX auth_id ON alf_authority_alias (auth_id, alias_id);
CREATE INDEX fk_alf_autha_ali ON alf_authority_alias (alias_id);
CREATE INDEX fk_alf_autha_aut ON alf_authority_alias (auth_id);

CREATE SEQUENCE alf_server_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_server
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    ip_address VARCHAR(39) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ip_address ON alf_server (ip_address);

CREATE SEQUENCE alf_transaction_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_transaction
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    server_id INT8,
    change_txn_id VARCHAR(56) NOT NULL,
    commit_time_ms INT8,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES alf_server (id)
);
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms, id);
CREATE INDEX fk_alf_txn_svr ON alf_transaction (server_id);

CREATE SEQUENCE alf_store_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_store
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    identifier VARCHAR(100) NOT NULL,
    root_node_id INT8,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX protocol ON alf_store (protocol, identifier);

CREATE SEQUENCE alf_node_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_node
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    store_id INT8 NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    transaction_id INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    locale_id INT8 NOT NULL,
    acl_id INT8,
    audit_creator VARCHAR(255),
    audit_created VARCHAR(30),
    audit_modifier VARCHAR(255),
    audit_modified VARCHAR(30),
    audit_accessed VARCHAR(30),
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
    CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES alf_store (id),
    CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),
    CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id),
    CONSTRAINT fk_alf_node_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id)
);
CREATE UNIQUE INDEX store_id ON alf_node (store_id, uuid);
CREATE INDEX idx_alf_node_mdq ON alf_node (store_id, type_qname_id, id);
CREATE INDEX idx_alf_node_cor ON alf_node (audit_creator, store_id, type_qname_id, id);
CREATE INDEX idx_alf_node_crd ON alf_node (audit_created, store_id, type_qname_id, id);
CREATE INDEX idx_alf_node_mor ON alf_node (audit_modifier, store_id, type_qname_id, id);
CREATE INDEX idx_alf_node_mod ON alf_node (audit_modified, store_id, type_qname_id, id);
CREATE INDEX idx_alf_node_txn_type ON alf_node (transaction_id, type_qname_id);
CREATE INDEX fk_alf_node_acl ON alf_node (acl_id);
CREATE INDEX fk_alf_node_store ON alf_node (store_id);
CREATE INDEX fk_alf_node_tqn ON alf_node (type_qname_id);
CREATE INDEX fk_alf_node_loc ON alf_node (locale_id);

CREATE INDEX fk_alf_store_root ON alf_store (root_node_id);
ALTER TABLE alf_store ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES alf_node (id);

CREATE SEQUENCE alf_child_assoc_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_child_assoc
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    parent_node_id INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    child_node_name_crc INT8 NOT NULL,
    child_node_name VARCHAR(50) NOT NULL,
    child_node_id INT8 NOT NULL,
    qname_ns_id INT8 NOT NULL,
    qname_localname VARCHAR(255) NOT NULL,
    qname_crc INT8 NOT NULL,
    is_primary BOOL,
    assoc_index INT4,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_cass_cnode FOREIGN KEY (child_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_cass_pnode FOREIGN KEY (parent_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_cass_qnns FOREIGN KEY (qname_ns_id) REFERENCES alf_namespace (id),
    CONSTRAINT fk_alf_cass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
);
CREATE UNIQUE INDEX parent_node_id ON alf_child_assoc (parent_node_id, type_qname_id, child_node_name_crc, child_node_name);
CREATE INDEX idx_alf_cass_pnode ON alf_child_assoc (parent_node_id, assoc_index, id);
CREATE INDEX fk_alf_cass_cnode ON alf_child_assoc (child_node_id);
CREATE INDEX fk_alf_cass_tqn ON alf_child_assoc (type_qname_id);
CREATE INDEX fk_alf_cass_qnns ON alf_child_assoc (qname_ns_id);
CREATE INDEX idx_alf_cass_qncrc ON alf_child_assoc (qname_crc, type_qname_id, parent_node_id);
CREATE INDEX idx_alf_cass_pri ON alf_child_assoc (parent_node_id, is_primary, child_node_id);

CREATE TABLE alf_node_aspects
(
    node_id INT8 NOT NULL,
    qname_id INT8 NOT NULL,
    PRIMARY KEY (node_id, qname_id),
	CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
);
CREATE INDEX fk_alf_nasp_n ON alf_node_aspects (node_id);
CREATE INDEX fk_alf_nasp_qn ON alf_node_aspects (qname_id);

CREATE SEQUENCE alf_node_assoc_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_node_assoc
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    source_node_id INT8 NOT NULL,
    target_node_id INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    assoc_index INT8 NOT NULL,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
);
CREATE UNIQUE INDEX source_node_id ON alf_node_assoc (source_node_id, target_node_id, type_qname_id);
CREATE INDEX fk_alf_nass_snode ON alf_node_assoc (source_node_id, type_qname_id, assoc_index);
CREATE INDEX fk_alf_nass_tnode ON alf_node_assoc (target_node_id, type_qname_id);
CREATE INDEX fk_alf_nass_tqn ON alf_node_assoc (type_qname_id);

CREATE TABLE alf_node_properties
(
    node_id INT8 NOT NULL,
    actual_type_n INT4 NOT NULL,
    persisted_type_n INT4 NOT NULL,
    boolean_value BOOL,
    long_value INT8,
    float_value FLOAT4,
    double_value FLOAT8,
    string_value VARCHAR(1024),
    serializable_value BYTEA,
    qname_id INT8 NOT NULL,
    list_index INT4 NOT NULL,
    locale_id INT8 NOT NULL,
    PRIMARY KEY (node_id, qname_id, list_index, locale_id),
    CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id),
    CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
);
CREATE INDEX fk_alf_nprop_n ON alf_node_properties (node_id);
CREATE INDEX fk_alf_nprop_qn ON alf_node_properties (qname_id);
CREATE INDEX fk_alf_nprop_loc ON alf_node_properties (locale_id);
CREATE INDEX idx_alf_nprop_s ON alf_node_properties (qname_id, string_value, node_id);
CREATE INDEX idx_alf_nprop_l ON alf_node_properties (qname_id, long_value, node_id);
