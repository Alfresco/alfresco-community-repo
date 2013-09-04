--
-- Title:      Core Repository Tables
-- Database:   MySQL InnoDB
-- Since:      V3.3 Schema 4000
-- Author:     Derek Hulley, janv
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_applied_patch
(
    id VARCHAR(64) NOT NULL,
    description TEXT,
    fixes_from_schema INTEGER,
    fixes_to_schema INTEGER,
    applied_to_schema INTEGER,
    target_schema INTEGER,
    applied_on_date DATETIME,
    applied_to_server VARCHAR(64),
    was_executed BIT,
    succeeded BIT,
    report TEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_locale
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    locale_str VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY locale_str (locale_str)
) ENGINE=InnoDB;

CREATE TABLE alf_namespace
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    uri VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uri (uri)
) ENGINE=InnoDB;

CREATE TABLE alf_qname
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    ns_id BIGINT NOT NULL,
    local_name VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY ns_id (ns_id, local_name),
    CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id)
) ENGINE=InnoDB;

CREATE TABLE alf_permission
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    type_qname_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY type_qname_id (type_qname_id, name),
    KEY fk_alf_perm_tqn (type_qname_id),
    CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;

CREATE TABLE alf_ace_context
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    class_context TEXT,
    property_context TEXT,
    kvp_context TEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_authority
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    authority VARCHAR(100),
    crc BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY authority (authority, crc),
    KEY idx_alf_auth_aut (authority)
) ENGINE=InnoDB;

CREATE TABLE alf_access_control_entry
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    allowed BIT NOT NULL,
    applies INTEGER NOT NULL,
    context_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY permission_id (permission_id, authority_id, allowed, applies),
    KEY fk_alf_ace_ctx (context_id),
    KEY fk_alf_ace_perm (permission_id),
    KEY fk_alf_ace_auth (authority_id),
    CONSTRAINT fk_alf_ace_auth FOREIGN KEY (authority_id) REFERENCES alf_authority (id),
    CONSTRAINT fk_alf_ace_ctx FOREIGN KEY (context_id) REFERENCES alf_ace_context (id),
    CONSTRAINT fk_alf_ace_perm FOREIGN KEY (permission_id) REFERENCES alf_permission (id)
) ENGINE=InnoDB;

CREATE TABLE alf_acl_change_set
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    commit_time_ms BIGINT,
	KEY idx_alf_acs_ctms (commit_time_ms),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_access_control_list
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    acl_id VARCHAR(36)  NOT NULL,
    latest BIT NOT NULL,
    acl_version BIGINT NOT NULL,
    inherits BIT NOT NULL,
    inherits_from BIGINT,
    type INTEGER NOT NULL,
    inherited_acl BIGINT,
    is_versioned BIT NOT NULL,
    requires_version BIT NOT NULL,
    acl_change_set BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY acl_id (acl_id, latest, acl_version),
    KEY idx_alf_acl_inh (inherits, inherits_from),
    KEY fk_alf_acl_acs (acl_change_set),
    CONSTRAINT fk_alf_acl_acs FOREIGN KEY (acl_change_set) REFERENCES alf_acl_change_set (id)
) ENGINE=InnoDB;

CREATE TABLE alf_acl_member
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    acl_id BIGINT NOT NULL,
    ace_id BIGINT NOT NULL,
    pos INTEGER NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY aclm_acl_id (acl_id, ace_id, pos),
    KEY fk_alf_aclm_acl (acl_id),
    KEY fk_alf_aclm_ace (ace_id),
    CONSTRAINT fk_alf_aclm_ace FOREIGN KEY (ace_id) REFERENCES alf_access_control_entry (id),
    CONSTRAINT fk_alf_aclm_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
) ENGINE=InnoDB;

CREATE TABLE alf_authority_alias
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    auth_id BIGINT NOT NULL,
    alias_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY auth_id (auth_id, alias_id),
    KEY fk_alf_autha_ali (alias_id),
    KEY fk_alf_autha_aut (auth_id),
    CONSTRAINT fk_alf_autha_aut FOREIGN KEY (auth_id) REFERENCES alf_authority (id),
    CONSTRAINT fk_alf_autha_ali FOREIGN KEY (alias_id) REFERENCES alf_authority (id)
) ENGINE=InnoDB;

CREATE TABLE alf_server
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    ip_address VARCHAR(39) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY ip_address (ip_address)
) ENGINE=InnoDB;

CREATE TABLE alf_transaction
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    server_id BIGINT,
    change_txn_id VARCHAR(56) NOT NULL,
    commit_time_ms BIGINT,
    PRIMARY KEY (id),
    KEY idx_alf_txn_ctms (commit_time_ms),
    KEY fk_alf_txn_svr (server_id),
    CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES alf_server (id)
) ENGINE=InnoDB;

CREATE TABLE alf_store
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    identifier VARCHAR(100) NOT NULL,
    root_node_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY protocol (protocol, identifier)
) ENGINE=InnoDB;

CREATE TABLE alf_node
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    transaction_id BIGINT NOT NULL,
    type_qname_id BIGINT NOT NULL,
    locale_id BIGINT NOT NULL,
    acl_id BIGINT,
    audit_creator VARCHAR(255),
    audit_created VARCHAR(30),
    audit_modifier VARCHAR(255),
    audit_modified VARCHAR(30),
    audit_accessed VARCHAR(30),
    PRIMARY KEY (id),
    UNIQUE KEY store_id (store_id, uuid),
    KEY idx_alf_node_txn_type (transaction_id, type_qname_id),
    KEY fk_alf_node_acl (acl_id),
    KEY fk_alf_node_store (store_id),
    KEY fk_alf_node_tqn (type_qname_id),
    KEY fk_alf_node_loc (locale_id),
    KEY idx_alf_node_mdq (store_id, type_qname_id),
    KEY idx_alf_node_cor (audit_creator, store_id, type_qname_id),
    KEY idx_alf_node_crd (audit_created, store_id, type_qname_id),
    KEY idx_alf_node_mor (audit_modifier, store_id, type_qname_id),
    KEY idx_alf_node_mod (audit_modified, store_id, type_qname_id),
    CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
    CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES alf_store (id),
    CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),
    CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id),
    CONSTRAINT fk_alf_node_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id)
) ENGINE=InnoDB;

ALTER TABLE alf_store ADD INDEX fk_alf_store_root (root_node_id), ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES alf_node (id);

CREATE TABLE alf_child_assoc
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    parent_node_id BIGINT NOT NULL,
    type_qname_id BIGINT NOT NULL,
    child_node_name_crc BIGINT NOT NULL,
    child_node_name VARCHAR(50) NOT NULL,
    child_node_id BIGINT NOT NULL,
    qname_ns_id BIGINT NOT NULL,
    qname_localname VARCHAR(255) NOT NULL,
    qname_crc BIGINT NOT NULL,
    is_primary bit,
    assoc_index INTEGER,
    PRIMARY KEY (id),
    UNIQUE KEY parent_node_id (parent_node_id, type_qname_id, child_node_name_crc, child_node_name),
    KEY idx_alf_cass_pnode (parent_node_id, assoc_index, id),
    KEY fk_alf_cass_cnode (child_node_id),
    KEY fk_alf_cass_tqn (type_qname_id),
    KEY fk_alf_cass_qnns (qname_ns_id),
    KEY idx_alf_cass_qncrc (qname_crc, type_qname_id, parent_node_id),
    KEY idx_alf_cass_pri (parent_node_id, is_primary, child_node_id),
    CONSTRAINT fk_alf_cass_cnode FOREIGN KEY (child_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_cass_pnode FOREIGN KEY (parent_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_cass_qnns FOREIGN KEY (qname_ns_id) REFERENCES alf_namespace (id),
    CONSTRAINT fk_alf_cass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;

CREATE TABLE alf_node_aspects
(
    node_id BIGINT NOT NULL,
    qname_id BIGINT NOT NULL,
    PRIMARY KEY (node_id, qname_id),
    KEY fk_alf_nasp_n (node_id),
    KEY fk_alf_nasp_qn (qname_id),
    CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;

CREATE TABLE alf_node_assoc
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    type_qname_id BIGINT NOT NULL,
    assoc_index BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY source_node_id (source_node_id, target_node_id, type_qname_id),
    KEY fk_alf_nass_snode (source_node_id, type_qname_id, assoc_index),
    KEY fk_alf_nass_tnode (target_node_id, type_qname_id),
    KEY fk_alf_nass_tqn (type_qname_id),
    CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;

CREATE TABLE alf_node_properties
(
    node_id BIGINT NOT NULL,
    actual_type_n INTEGER NOT NULL,
    persisted_type_n INTEGER NOT NULL,
    boolean_value bit,
    long_value BIGINT,
    float_value float,
    double_value double precision,
    string_value text,
    serializable_value blob,
    qname_id BIGINT NOT NULL,
    list_index INTEGER NOT NULL,
    locale_id BIGINT NOT NULL,
    PRIMARY KEY (node_id, qname_id, list_index, locale_id),
    KEY fk_alf_nprop_n (node_id),
    KEY fk_alf_nprop_qn (qname_id),
    KEY fk_alf_nprop_loc (locale_id),
    KEY idx_alf_nprop_s (qname_id, string_value(42)),
    KEY idx_alf_nprop_l (qname_id, long_value),
    CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id),
    CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;
