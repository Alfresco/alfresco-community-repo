--
-- Title:      Upgrade V2.2 SP1 or SP2 
-- Database:   Oracle
-- Since:      V2.2 Schema 91
-- Author:     Derek Hulley
--
-- MLText values must be pulled back from attributes into localizable properties.
-- NodeStatus has been moved to alf_node.
-- Auditable properties have been moved to alf_node.
-- alf_node contains the old alf_node_status information.
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- -------------------
-- Build new Store --
-- -------------------

CREATE TABLE t_alf_store
(
   id number(19,0) NOT NULL,
   version number(19,0) NOT NULL,
   protocol varchar2(50 char) NOT NULL,
   identifier varchar2(100 char) NOT NULL,
   root_node_id number(19,0),
   PRIMARY KEY (id),
   UNIQUE (protocol, identifier)
);

-- --------------------------
-- Populate the ADM nodes --
-- --------------------------

CREATE TABLE t_alf_node (
   id number(19,0) NOT NULL,
   version number(19,0) NOT NULL,
   store_id number(19,0) NOT NULL,
   uuid varchar2(36 char) NOT NULL,
   transaction_id number(19,0) NOT NULL,
   node_deleted NUMBER(1) NOT NULL,
   type_qname_id number(19,0) NOT NULL,
   acl_id number(19,0),
   audit_creator varchar2(255 char),
   audit_created varchar2(30 char),
   audit_modifier varchar2(255 char),
   audit_modified varchar2(30 char),
   audit_accessed varchar2(30 char),
   PRIMARY KEY (id),
   UNIQUE (store_id, uuid)
);

CREATE INDEX idx_alf_node_del on t_alf_node (node_deleted);
CREATE INDEX fk_alf_node_acl on t_alf_node (acl_id);
CREATE INDEX fk_alf_node_tqn on t_alf_node (type_qname_id);
CREATE INDEX fk_alf_node_txn on t_alf_node (transaction_id);
CREATE INDEX fk_alf_node_store on t_alf_node (store_id);
ALTER TABLE t_alf_node
   ADD CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
   ADD CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
   ADD CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id)
   ADD CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES t_alf_store (id)
;

-- Fill the store table
INSERT INTO t_alf_store (id, version, protocol, identifier, root_node_id)
   SELECT hibernate_sequence.nextval, 1, protocol, identifier, root_node_id FROM alf_store
;

-- Copy data over
INSERT INTO t_alf_node
   (
      id, version, store_id, uuid, transaction_id, node_deleted, type_qname_id, acl_id,
      audit_creator, audit_created, audit_modifier, audit_modified
   )
   SELECT
      n.id, 1, s.id, n.uuid, nstat.transaction_id, 0, n.type_qname_id, n.acl_id,
      null, null, null, null
   FROM
      alf_node n
      JOIN alf_node_status nstat ON (nstat.node_id = n.id)
      JOIN t_alf_store s ON (s.protocol = nstat.protocol AND s.identifier = nstat.identifier)
;

-- Hook the store up to the root node
CREATE INDEX fk_alf_store_root ON t_alf_store (root_node_id); 
ALTER TABLE t_alf_store 
   ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES t_alf_node (id)
;

-- -----------------------------
-- Populate Version Counter  --
-- -----------------------------

CREATE TABLE t_alf_version_count
(
   id number(19,0) NOT NULL,
   version number(19,0) NOT NULL,
   store_id number(19,0) NOT NULL UNIQUE,
   version_count number(10,0) NOT NULL,
   PRIMARY KEY (id)
);
ALTER TABLE t_alf_version_count 
   ADD CONSTRAINT fk_alf_vc_store FOREIGN KEY (store_id) REFERENCES t_alf_store (id)
;

INSERT INTO t_alf_version_count
   (
      id, version, store_id, version_count
   )
   SELECT
      hibernate_sequence.nextval, 1, s.id, vc.version_count
   FROM
      alf_version_count vc
      JOIN t_alf_store s ON (s.protocol = vc.protocol AND s.identifier = vc.identifier)
;

DROP TABLE alf_version_count;
ALTER TABLE t_alf_version_count RENAME TO alf_version_count;

-- -----------------------------
-- Populate the Child Assocs --
-- -----------------------------

CREATE TABLE t_alf_child_assoc
(
   id number(19,0) NOT NULL,
   version number(19,0) NOT NULL,
   parent_node_id number(19,0) NOT NULL,
   type_qname_id number(19,0) NOT NULL,
   child_node_name varchar2(50 char) NOT NULL,
   child_node_name_crc number(19,0) NOT NULL,
   child_node_id number(19,0) NOT NULL,
   qname_ns_id number(19,0) NOT NULL,
   qname_localname varchar2(100 char) NOT NULL,
   is_primary number(1),
   assoc_index number(10,0),
   PRIMARY KEY (id),
   UNIQUE (parent_node_id, type_qname_id, child_node_name, child_node_name_crc)
);
CREATE INDEX idx_alf_cass_qnln on t_alf_child_assoc (qname_localname);
CREATE INDEX fk_alf_cass_pnode on t_alf_child_assoc (parent_node_id);
CREATE INDEX fk_alf_cass_cnode on t_alf_child_assoc (child_node_id);
CREATE INDEX fk_alf_cass_tqn on t_alf_child_assoc (type_qname_id);
CREATE INDEX fk_alf_cass_qnns on t_alf_child_assoc (qname_ns_id);
ALTER TABLE t_alf_child_assoc
   ADD CONSTRAINT fk_alf_cass_pnode FOREIGN KEY (parent_node_id) REFERENCES t_alf_node (id)
   ADD CONSTRAINT fk_alf_cass_cnode FOREIGN KEY (child_node_id) REFERENCES t_alf_node (id)
   ADD CONSTRAINT fk_alf_cass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
   ADD CONSTRAINT fk_alf_cass_qnns FOREIGN KEY (qname_ns_id) REFERENCES alf_namespace (id)
;

INSERT INTO t_alf_child_assoc
   (
      id, version,
      parent_node_id, child_node_id,
      child_node_name, child_node_name_crc,
      type_qname_id,
      qname_ns_id, qname_localname,
      is_primary, assoc_index
   )
   SELECT
      ca.id, 1,
      ca.parent_node_id, ca.child_node_id,
      ca.child_node_name, child_node_name_crc,
      ca.type_qname_id,
      ca.qname_ns_id, ca.qname_localname,
      ca.is_primary, ca.assoc_index
   FROM
      alf_child_assoc ca
;

-- Clean up
DROP TABLE alf_child_assoc;
ALTER TABLE t_alf_child_assoc RENAME TO alf_child_assoc;

-- ----------------------------
-- Populate the Node Assocs --
-- ----------------------------

CREATE TABLE t_alf_node_assoc
(
   id number(19,0) NOT NULL,
   version number(19,0) NOT NULL, 
   source_node_id number(19,0) NOT NULL,
   target_node_id number(19,0) NOT NULL,
   type_qname_id number(19,0) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (source_node_id, target_node_id, type_qname_id)
);
CREATE INDEX fk_alf_nass_snode on t_alf_node_assoc (source_node_id);
CREATE INDEX fk_alf_nass_tnode on t_alf_node_assoc (target_node_id);
CREATE INDEX fk_alf_nass_tqn on t_alf_node_assoc (type_qname_id);
ALTER TABLE t_alf_node_assoc
   ADD CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES t_alf_node (id)
   ADD CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES t_alf_node (id)
   ADD CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
;

INSERT INTO t_alf_node_assoc
   (
      id, version,
      source_node_id, target_node_id,
      type_qname_id
   )
   SELECT
      na.id, 1,
      na.source_node_id, na.source_node_id,
      na.type_qname_id
   FROM
      alf_node_assoc na
;

-- Clean up
DROP TABLE alf_node_assoc;
ALTER TABLE t_alf_node_assoc RENAME TO alf_node_assoc;

-- ----------------------------
-- Populate the Usage Deltas --
-- ----------------------------

CREATE TABLE t_alf_usage_delta
(
   id number(19,0) NOT NULL,
   version number(19,0) NOT NULL, 
   node_id number(19,0) NOT NULL,
   delta_size number(19,0) NOT NULL,
   PRIMARY KEY (id)
);
CREATE INDEX fk_alf_usaged_n on t_alf_usage_delta (node_id);
ALTER TABLE t_alf_usage_delta 
   ADD CONSTRAINT fk_alf_usaged_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id)
;

INSERT INTO t_alf_usage_delta
   (
      id, version,
      node_id,
      delta_size
   )
   SELECT
      ud.id, 1,
      ud.node_id,
      ud.delta_size
   FROM
      alf_usage_delta ud
;                                                          -- (optional)

-- Clean up
DROP TABLE alf_usage_delta;                                -- (optional)
ALTER TABLE t_alf_usage_delta RENAME TO alf_usage_delta;

-- -----------------------------
-- Populate the Node Aspects --
-- -----------------------------

CREATE TABLE t_alf_node_aspects
(
   node_id number(19,0) NOT NULL,
   qname_id number(19,0) NOT NULL,
   PRIMARY KEY (node_id, qname_id)
);
CREATE INDEX fk_alf_nasp_n on t_alf_node_aspects (node_id);
CREATE INDEX fk_alf_nasp_qn on t_alf_node_aspects (qname_id);
ALTER TABLE t_alf_node_aspects
   ADD CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id)
   ADD CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
;

-- Note the omission of sys:referencable.  This is implicit.
INSERT INTO t_alf_node_aspects
   (
      node_id, qname_id
   )
   SELECT
      na.node_id,
      qname_id
   FROM
      alf_node_aspects na
      JOIN alf_qname qn ON (na.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      ns.uri != 'http://www.alfresco.org/model/system/1.0' OR
      qn.local_name != 'referenceable'
;

-- Clean up
DROP TABLE alf_node_aspects;
ALTER TABLE t_alf_node_aspects RENAME TO alf_node_aspects;

-- ---------------------------------
-- Populate the AVM Node Aspects --
-- ---------------------------------

CREATE TABLE t_avm_aspects
(
   node_id number(19,0) NOT NULL,
   qname_id number(19,0) NOT NULL,
   PRIMARY KEY (node_id, qname_id)
);
CREATE INDEX fk_avm_nasp_n on t_avm_aspects (node_id);
CREATE INDEX fk_avm_nasp_qn on t_avm_aspects (qname_id);
ALTER TABLE t_avm_aspects
   ADD CONSTRAINT fk_avm_nasp_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id)
   ADD CONSTRAINT fk_avm_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
;

INSERT INTO t_avm_aspects
   (
      node_id, qname_id
   )
   SELECT
      anew.id,
      anew.qname_id
   FROM
      avm_aspects_new anew
;

-- Clean up
DROP TABLE avm_aspects;
DROP TABLE avm_aspects_new;
ALTER TABLE t_avm_aspects RENAME TO avm_aspects;

-- ----------------------------------
-- Migrate Sundry Property Tables --
-- ----------------------------------

-- Modify the avm_store_properties table
CREATE TABLE t_avm_store_properties
(
   id number(19,0) NOT NULL,
   avm_store_id number(19,0),
   qname_id number(19,0) NOT NULL,
   actual_type_n number(10,0) NOT NULL,
   persisted_type_n number(10,0) NOT NULL,
   multi_valued number(1) NOT NULL,
   boolean_value number(1),
   long_value number(19,0),
   float_value float,
   double_value DOUBLE PRECISION,
   string_value varchar2(1024 char),
   serializable_value blob,
   PRIMARY KEY (id)
);
CREATE INDEX fk_avm_sprop_store on t_avm_store_properties (avm_store_id);
CREATE INDEX fk_avm_sprop_qname on t_avm_store_properties (qname_id);
ALTER TABLE t_avm_store_properties
   ADD CONSTRAINT fk_avm_sprop_store FOREIGN KEY (avm_store_id) REFERENCES avm_stores (id)
   ADD CONSTRAINT fk_avm_sprop_qname FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
;

INSERT INTO t_avm_store_properties
   (
      id,
      avm_store_id,
      qname_id,
      actual_type_n, persisted_type_n,
      multi_valued, boolean_value, long_value, float_value, double_value, string_value, serializable_value
   )
   SELECT
      hibernate_sequence.nextval,
      p.avm_store_id,
      p.qname_id,
      p.actual_type_n, p.persisted_type_n,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, TO_LOB(p.serializable_value)
   FROM
      avm_store_properties p
;
DROP TABLE avm_store_properties;
ALTER TABLE t_avm_store_properties RENAME TO avm_store_properties;

-- Modify the avm_node_properties_new table
CREATE TABLE t_avm_node_properties
(
   node_id number(19,0) NOT NULL,
   actual_type_n number(10,0) NOT NULL,
   persisted_type_n number(10,0) NOT NULL,
   multi_valued number(1) NOT NULL,
   boolean_value number(1),
   long_value number(19,0),
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value varchar2(1024 char),
   serializable_value BLOB,
   qname_id number(19,0) NOT NULL,
   PRIMARY KEY (node_id, qname_id)
);
CREATE INDEX fk_avm_nprop_n on t_avm_node_properties (node_id);
CREATE INDEX fk_avm_nprop_qn on t_avm_node_properties (qname_id);
ALTER TABLE t_avm_node_properties
   ADD CONSTRAINT fk_avm_nprop_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id)
   ADD CONSTRAINT fk_avm_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
;

INSERT INTO t_avm_node_properties
   (
      node_id,
      qname_id,
      actual_type_n, persisted_type_n,
      multi_valued, boolean_value, long_value, float_value, double_value, string_value, serializable_value
   )
   SELECT
      p.node_id,
      p.qname_id,
      p.actual_type_n, p.persisted_type_n,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, TO_LOB(p.serializable_value)
   FROM
      avm_node_properties_new p
;

DROP TABLE avm_node_properties_new;
DROP TABLE avm_node_properties;
ALTER TABLE t_avm_node_properties RENAME TO avm_node_properties;


-- -----------------
-- Build Locales --
-- -----------------

CREATE TABLE alf_locale
(
   id number(19,0) NOT NULL,
   version number(19,0) DEFAULT 1 NOT NULL,
   locale_str varchar2(20 char) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (locale_str)
);

INSERT INTO alf_locale (id, locale_str) VALUES (1, '.default');

-- Locales come from the attribute table which was used to support MLText persistence
INSERT INTO alf_locale (id, locale_str)
   SELECT hibernate_sequence.nextval, mkey
   FROM (
     SELECT DISTINCT(ma.mkey)
        FROM alf_node_properties np
        JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
        JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
   )
;

-- -------------------------------
-- Migrate ADM Property Tables --
-- -------------------------------

CREATE TABLE t_alf_node_properties
(
   node_id number(19,0) NOT NULL,
   qname_id number(19,0) NOT NULL,
   locale_id number(19,0) NOT NULL,
   list_index number(10,0) NOT NULL,
   actual_type_n number(10,0) NOT NULL,
   persisted_type_n number(10,0) NOT NULL,
   boolean_value number(1),
   long_value number(19,0),
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value varchar2(1024 char),
   serializable_value BLOB,
   PRIMARY KEY (node_id, qname_id, list_index, locale_id)
);
CREATE INDEX fk_alf_nprop_n on t_alf_node_properties (node_id);
CREATE INDEX fk_alf_nprop_qn on t_alf_node_properties (qname_id);
CREATE INDEX fk_alf_nprop_loc on t_alf_node_properties (locale_id);
ALTER TABLE t_alf_node_properties
   ADD CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id)
   ADD CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
   ADD CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id)
;

-- Copy values over
INSERT INTO t_alf_node_properties
   (
      node_id, qname_id, list_index, locale_id,
      actual_type_n, persisted_type_n,
      boolean_value, long_value, float_value, double_value,
      string_value,
      serializable_value
   )
   SELECT
      np.node_id, np.qname_id, -1, 1,
      np.actual_type_n, np.persisted_type_n,
      np.boolean_value, np.long_value, np.float_value, np.double_value,
      np.string_value,
      TO_LOB(np.serializable_value)
   FROM
      alf_node_properties np
   WHERE
      np.attribute_value IS NULL
;
-- Update cm:auditable properties on the nodes
UPDATE t_alf_node n SET audit_creator =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = n.id AND
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'creator'
);
UPDATE t_alf_node n SET audit_created =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = n.id AND
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'created'
);
UPDATE t_alf_node n SET audit_modifier =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = n.id AND
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'modifier'
);
UPDATE t_alf_node n SET audit_modified =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = n.id AND
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'modified'
);
-- Remove the unused cm:auditable properties
DELETE
   FROM t_alf_node_properties
   WHERE EXISTS
   (
      SELECT 1
      FROM alf_qname, alf_namespace
      WHERE t_alf_node_properties.qname_id = alf_qname.id
      AND alf_qname.ns_id = alf_namespace.id
      AND alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0'
      AND alf_qname.local_name IN ('creator', 'created', 'modifier', 'modified')
   )
;

-- Copy all MLText values over
INSERT INTO t_alf_node_properties
   (
      node_id, qname_id, list_index, locale_id,
      actual_type_n, persisted_type_n,
      boolean_value, long_value, float_value, double_value,
      string_value,
      serializable_value
   )
   SELECT
      np.node_id, np.qname_id, -1, loc.id,
      -1, 0,
      0, 0, 0, 0,
      a2.string_value,
      TO_LOB(a2.serializable_value)
   FROM
      alf_node_properties np
      JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
      JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
      JOIN alf_locale loc ON (ma.mkey = loc.locale_str)
      JOIN alf_attributes a2 ON (ma.attribute_id = a2.id)
;  -- (OPTIONAL)
UPDATE t_alf_node_properties
   SET actual_type_n = 6, persisted_type_n = 6, serializable_value = NULL
   WHERE actual_type_n = -1 AND string_value IS NOT NULL
;
UPDATE t_alf_node_properties
   SET actual_type_n = 9, persisted_type_n = 9
   WHERE actual_type_n = -1 AND serializable_value IS NOT NULL
;

-- Delete the node properties and move the fixed values over
DROP TABLE alf_node_properties;
ALTER TABLE t_alf_node_properties RENAME TO alf_node_properties;

CREATE TABLE t_del_attributes
(
   id number(19,0) NOT NULL,
   PRIMARY KEY (id)
);
INSERT INTO t_del_attributes
   SELECT id FROM alf_attributes WHERE type = 'M'
;
DELETE
   FROM t_del_attributes
   WHERE EXISTS
   (
      SELECT 1 FROM alf_map_attribute_entries ma WHERE ma.attribute_id = t_del_attributes.id
   )
;
DELETE
   FROM t_del_attributes
   WHERE EXISTS
   (
      SELECT 1 FROM alf_list_attribute_entries la WHERE la.attribute_id = t_del_attributes.id
   )
;
DELETE
   FROM t_del_attributes
   WHERE EXISTS
   (
      SELECT 1 FROM alf_global_attributes ga WHERE ga.attribute = t_del_attributes.id
   )
;
INSERT INTO t_del_attributes
   SELECT a.id FROM t_del_attributes t
   JOIN alf_map_attribute_entries ma ON (ma.map_id = t.id)
   JOIN alf_attributes a ON (ma.attribute_id = a.id)
;
DELETE
   FROM alf_map_attribute_entries
   WHERE EXISTS
   (
      SELECT 1 FROM t_del_attributes t WHERE alf_map_attribute_entries.map_id = t.id
   )
;
DELETE
   FROM alf_attributes
   WHERE EXISTS
   (
      SELECT 1 FROM t_del_attributes t WHERE alf_attributes.id = t.id
   )
;
DROP TABLE t_del_attributes;

-- ------------------
-- Final clean up --
-- ------------------
DROP TABLE alf_node_status;
ALTER TABLE alf_store DROP CONSTRAINT fk_alf_store_rn;
DROP TABLE alf_node;
ALTER TABLE t_alf_node RENAME TO alf_node;
DROP TABLE alf_store;
ALTER TABLE t_alf_store RENAME TO alf_store;

-- 2.2.0 ACL fix ups
CREATE INDEX idx_alf_auth_aut ON alf_authority (authority);  -- (optional)
ALTER TABLE alf_authority MODIFY (authority VARCHAR(100 char) NULL);  -- (optional)

-- ----------------------------------
-- Convert alf_attributes to use BLOB
-- ----------------------------------
-- Changing a column type to a blob disturbs a table's indexes, so we have to rebuild them
ALTER TABLE alf_map_attribute_entries DROP CONSTRAINT fk_alf_matt_att;
ALTER TABLE alf_map_attribute_entries DROP CONSTRAINT fk_alf_matt_matt;
ALTER TABLE alf_global_attributes DROP CONSTRAINT fk_alf_gatt_att;
ALTER TABLE alf_list_attribute_entries DROP CONSTRAINT fk_alf_lent_att;
ALTER TABLE alf_list_attribute_entries DROP CONSTRAINT fk_alf_lent_latt;
ALTER TABLE alf_attributes DROP PRIMARY KEY DROP INDEX;
ALTER TABLE alf_attributes MODIFY (serializable_value BLOB NULL); -- (optional)
ALTER TABLE alf_attributes ADD PRIMARY KEY (id);
ALTER TABLE alf_map_attribute_entries
   ADD CONSTRAINT fk_alf_matt_matt FOREIGN KEY (map_id) REFERENCES alf_attributes (id)
   ADD CONSTRAINT fk_alf_matt_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id)
;
ALTER TABLE alf_global_attributes
   ADD CONSTRAINT fk_alf_gatt_att FOREIGN KEY (attribute) REFERENCES alf_attributes (id)
;
ALTER TABLE alf_list_attribute_entries
   ADD CONSTRAINT fk_alf_lent_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id)
   ADD CONSTRAINT fk_alf_lent_latt FOREIGN KEY (list_id) REFERENCES alf_attributes (id)
;
ALTER INDEX fk_alf_attr_acl REBUILD;

-- ----------------
-- JBPM Differences
-- ----------------
ALTER TABLE jbpm_processinstance DROP UNIQUE (key_, processdefinition_);  -- (optional)
CREATE INDEX idx_procin_key ON jbpm_processinstance (key_);  -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-Upgrade-From-2.2SP1';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-Upgrade-From-2.2SP1', 'Manually executed script upgrade V2.2: Upgraded V2.2 SP1 or SP2',
    86, 90, -1, 91, null, 'UNKOWN', 1, 1, 'Script completed'
  );
