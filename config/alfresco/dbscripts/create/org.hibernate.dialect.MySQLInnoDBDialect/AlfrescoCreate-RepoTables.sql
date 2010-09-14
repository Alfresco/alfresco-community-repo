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
    KEY fk_alf_qname_ns (ns_id),
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
    version BIGINT NOT NULL,
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

CREATE TABLE alf_audit_config
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_url TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_date
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    date_only date NOT NULL,
    day_of_year INTEGER NOT NULL,
    day_of_month INTEGER NOT NULL,
    day_of_week INTEGER NOT NULL,
    week_of_year INTEGER NOT NULL,
    week_of_month INTEGER NOT NULL,
    month INTEGER NOT NULL,
    quarter INTEGER NOT NULL,
    half_year INTEGER NOT NULL,
    full_year INTEGER NOT NULL,
    PRIMARY KEY (id),
    KEY idx_alf_adtd_woy (week_of_year),
    KEY idx_alf_adtd_fy (full_year),
    KEY idx_alf_adtd_q (quarter),
    KEY idx_alf_adtd_wom (week_of_month),
    KEY idx_alf_adtd_dom (day_of_month),
    KEY idx_alf_adtd_doy (day_of_year),
    KEY idx_alf_adtd_dow (day_of_week),
    KEY idx_alf_adtd_m (month),
    KEY idx_alf_adtd_hy (half_year),
    KEY idx_alf_adtd_dat (date_only)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_source
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    application VARCHAR(255) NOT NULL,
    service VARCHAR(255),
    method VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_alf_adts_met (method),
    KEY idx_alf_adts_ser (service),
    KEY idx_alf_adts_app (application)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_fact
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(255) NOT NULL,
    timestamp DATETIME NOT NULL,
    transaction_id VARCHAR(56) NOT NULL,
    session_id VARCHAR(56),
    store_protocol VARCHAR(50),
    store_id VARCHAR(100),
    node_uuid VARCHAR(36),
    path TEXT,
    filtered BIT NOT NULL,
    return_val TEXT,
    arg_1 TEXT,
    arg_2 TEXT,
    arg_3 TEXT,
    arg_4 TEXT,
    arg_5 TEXT,
    fail BIT NOT NULL,
    serialized_url TEXT,
    exception_message TEXT,
    host_address TEXT,
    client_address TEXT,
    message_text TEXT,
    audit_date_id BIGINT NOT NULL,
    audit_conf_id BIGINT NOT NULL,
    audit_source_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_alf_adtf_ref (store_protocol, store_id, node_uuid),
    KEY idx_alf_adtf_usr (user_id),
    KEY fk_alf_adtf_src (audit_source_id),
    KEY fk_alf_adtf_date (audit_date_id),
    KEY fk_alf_adtf_conf (audit_conf_id),
    KEY idx_alf_adtf_pth (path(128)),
    CONSTRAINT fk_alf_adtf_conf FOREIGN KEY (audit_conf_id) REFERENCES alf_audit_config (id),
    CONSTRAINT fk_alf_adtf_date FOREIGN KEY (audit_date_id) REFERENCES alf_audit_date (id),
    CONSTRAINT fk_alf_adtf_src FOREIGN KEY (audit_source_id) REFERENCES alf_audit_source (id)
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
    node_deleted bit NOT NULL,
    type_qname_id BIGINT NOT NULL,
    acl_id BIGINT,
    audit_creator VARCHAR(255),
    audit_created VARCHAR(30),
    audit_modifier VARCHAR(255),
    audit_modified VARCHAR(30),
    audit_accessed VARCHAR(30),
    PRIMARY KEY (id),
    UNIQUE KEY store_id (store_id, uuid),
    KEY idx_alf_node_del (node_deleted),
    KEY fk_alf_node_acl (acl_id),
    KEY fk_alf_node_txn (transaction_id),
    KEY fk_alf_node_store (store_id),
    KEY fk_alf_node_tqn (type_qname_id),
    CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
    CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES alf_store (id),
    CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),
    CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id)
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
    KEY fk_alf_cass_pnode (parent_node_id),
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

CREATE TABLE alf_locale
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    locale_str VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY locale_str (locale_str)
) ENGINE=InnoDB;

CREATE TABLE alf_attributes
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(1) NOT NULL,
    version BIGINT NOT NULL,
    acl_id BIGINT,
    bool_value BIT,
    byte_value TINYINT,
    short_value SMALLINT,
    int_value INTEGER,
    long_value BIGINT,
    float_value FLOAT,
    double_value DOUBLE PRECISION,
    string_value TEXT,
    serializable_value BLOB,
    PRIMARY KEY (id),
    KEY fk_alf_attr_acl (acl_id),
    CONSTRAINT fk_alf_attr_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
) ENGINE=InnoDB;

CREATE TABLE alf_global_attributes
(
    name VARCHAR(160) NOT NULL,
    attribute BIGINT,
    PRIMARY KEY (name),
    UNIQUE KEY attribute (attribute),
    KEY fk_alf_gatt_att (attribute),
    CONSTRAINT fk_alf_gatt_att FOREIGN KEY (attribute) REFERENCES alf_attributes (id)
) ENGINE=InnoDB;

CREATE TABLE alf_list_attribute_entries
(
    list_id BIGINT NOT NULL,
    mindex INTEGER NOT NULL,
    attribute_id BIGINT,
    PRIMARY KEY (list_id, mindex),
    KEY fk_alf_lent_att (attribute_id),
    KEY fk_alf_lent_latt (list_id),
    CONSTRAINT fk_alf_lent_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id),
    CONSTRAINT fk_alf_lent_latt FOREIGN KEY (list_id) REFERENCES alf_attributes (id)
) ENGINE=InnoDB;

CREATE TABLE alf_map_attribute_entries
(
    map_id BIGINT NOT NULL,
    mkey VARCHAR(160) NOT NULL,
    attribute_id BIGINT,
    PRIMARY KEY (map_id, mkey),
    KEY fk_alf_matt_matt (map_id),
    KEY fk_alf_matt_att (attribute_id),
    CONSTRAINT fk_alf_matt_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id),
    CONSTRAINT fk_alf_matt_matt FOREIGN KEY (map_id) REFERENCES alf_attributes (id)
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
    PRIMARY KEY (id),
    UNIQUE KEY source_node_id (source_node_id, target_node_id, type_qname_id),
    KEY fk_alf_nass_snode (source_node_id),
    KEY fk_alf_nass_tnode (target_node_id),
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
    CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id),
    CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;
