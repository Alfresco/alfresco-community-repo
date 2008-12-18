--
-- Title:      Apply schema modifications to upgrade from 2.1 
-- Database:   Oracle
-- Since:      V2.2 Schema 91
-- Author:     Derek Hulley
--
-- In order to streamline the upgrade, all modifications to large tables need to
-- be handled in as few steps as possible.  This usually involves as few ALTER TABLE
-- statements as possible.  The general approach is:
--   Create a table with the correct structure, including indexes and CONSTRAINTs
--   Copy pristine data into the new table
--   Drop the old table
--   Rename the new table
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- -------------------------------
-- Build Namespaces and QNames --
-- -------------------------------

-- Create static namespace and qname tables
-- The Primary Key is not added as it's easier to add in afterwards
CREATE TABLE alf_namespace
(
   id NUMBER(19,0) DEFAULT 0 NOT NULL,
   version number(19,0) NOT NULL,
   uri VARCHAR2(100 CHAR) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (uri)
);

CREATE TABLE alf_qname
(
   id NUMBER(19,0) DEFAULT 0 NOT NULL,
   version NUMBER(19,0) NOT NULL,
   ns_id NUMBER(19,0) NOT NULL,
   local_name VARCHAR2(200 char) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (ns_id, local_name)
);
CREATE INDEX fk_alf_qn_ns on alf_qname (ns_id);
ALTER TABLE alf_qname ADD CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id);

-- Create temporary table for dynamic (child) QNames
CREATE TABLE t_qnames_dyn
(
   qname VARCHAR2(255) NOT NULL,
   namespace VARCHAR2(255),
   namespace_id NUMBER(19,0),
   local_name VARCHAR2(200 char)
);
CREATE INDEX tidx_qnd_qn ON t_qnames_dyn (qname);
CREATE INDEX tidx_qnd_ns ON t_qnames_dyn (namespace);

-- Populate the table with the child association paths
INSERT INTO t_qnames_dyn (qname)
(
   SELECT distinct qname FROM alf_child_assoc
);
-- Extract the Namespace
UPDATE t_qnames_dyn SET namespace = CONCAT('FILLER-', SUBSTRC(qname,2,INSTRC(qname,'}',1)-2));
-- Extract the Localname
UPDATE t_qnames_dyn SET local_name = SUBSTRC(qname,INSTRC(qname, '}',-1)+1);
-- Move the namespaces to the their new home
INSERT INTO alf_namespace (id, uri, version)
(
   SELECT hibernate_sequence.nextval, y.* FROM
   (
      SELECT
         DISTINCT(x.namespace), 1
      FROM
      (
         SELECT t.namespace, n.uri FROM t_qnames_dyn t LEFT OUTER JOIN alf_namespace n ON (n.uri = t.namespace)
      ) x
      WHERE
         x.uri IS NULL
   ) y
);
-- Record the new namespace IDs
UPDATE t_qnames_dyn t SET t.namespace_id = (SELECT ns.id FROM alf_namespace ns WHERE ns.uri = t.namespace);

-- Recoup some storage
ALTER TABLE t_qnames_dyn DROP COLUMN namespace;

-- Create temporary table to hold static QNames
CREATE TABLE t_qnames
(
   qname VARCHAR2(255) NOT NULL,
   namespace VARCHAR2(255),
   localname VARCHAR2(255),
   qname_id NUMBER(19,0)
);
CREATE INDEX tidx_tqn_qn ON t_qnames (qname);
CREATE INDEX tidx_tqn_ns ON t_qnames (namespace);
CREATE INDEX tidx_tqn_ln ON t_qnames (localname);

-- Populate the table with all known static QNames
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.type_qname FROM alf_node s LEFT OUTER JOIN t_qnames t ON (s.type_qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.qname FROM alf_node_aspects s LEFT OUTER JOIN t_qnames t ON (s.qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.qname FROM alf_node_properties s LEFT OUTER JOIN t_qnames t ON (s.qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.qname FROM avm_aspects s LEFT OUTER JOIN t_qnames t ON (s.qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.name FROM avm_aspects_new s LEFT OUTER JOIN t_qnames t ON (s.name = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.qname FROM avm_node_properties s LEFT OUTER JOIN t_qnames t ON (s.qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.qname FROM avm_node_properties_new s LEFT OUTER JOIN t_qnames t ON (s.qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.qname FROM avm_store_properties s LEFT OUTER JOIN t_qnames t ON (s.qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.type_qname FROM alf_node_assoc s LEFT OUTER JOIN t_qnames t ON (s.type_qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.type_qname FROM alf_child_assoc s LEFT OUTER JOIN t_qnames t ON (s.type_qname = t.qname) WHERE t.qname IS NULL
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT s.type_qname FROM alf_permission s LEFT OUTER JOIN t_qnames t ON (s.type_qname = t.qname) WHERE t.qname IS NULL
);

-- Extract the namespace and localnames from the QNames
UPDATE t_qnames SET namespace = CONCAT('FILLER-', SUBSTR(qname,2,INSTRC(qname,'}',1)-2));
UPDATE t_qnames SET localname = SUBSTR(qname,INSTRC(qname,'}',1)+1);

-- Move the Namespaces to their new home
INSERT INTO alf_namespace (id, uri, version)
(
   SELECT hibernate_sequence.nextval, y.* FROM
   (
      SELECT
         DISTINCT(x.namespace), 1
      FROM
      (
         SELECT t.namespace, n.uri FROM t_qnames t LEFT OUTER JOIN alf_namespace n ON (n.uri = t.namespace)
      ) x
      WHERE
         x.uri IS NULL
   ) y
);

-- Move the Localnames to their new home
INSERT INTO alf_qname (id, ns_id, local_name, version)
(
   SELECT hibernate_sequence.nextval, y.*
   FROM
   (
      SELECT
         x.ns_id, x.t_localname, 1
      FROM
      (
         SELECT n.id AS ns_id, t.localname AS t_localname, q.local_name AS q_localname
         FROM t_qnames t
         JOIN alf_namespace n ON (n.uri = t.namespace)
          LEFT OUTER JOIN alf_qname q ON (q.local_name = t.localname)
       ) x
      WHERE
         q_localname IS NULL
      GROUP BY x.ns_id, x.t_localname
   ) y
);

-- Record the new qname IDs
UPDATE t_qnames t SET t.qname_id =
(
   SELECT q.id FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE ns.uri = t.namespace AND q.local_name = t.localname
);

-- ----------------------------
-- Populate the Permissions --
-- ----------------------------

-- This is a small table so we change it in place
ALTER TABLE alf_permission DROP UNIQUE (type_qname, name);
ALTER TABLE alf_permission ADD ( type_qname_id NUMBER(19,0) NULL );
UPDATE alf_permission p SET p.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = p.type_qname
);
ALTER TABLE alf_permission DROP COLUMN type_qname;
ALTER TABLE alf_permission MODIFY ( type_qname_id NUMBER(19,0) NOT NULL);
ALTER TABLE alf_permission ADD UNIQUE (type_qname_id, name);
CREATE INDEX fk_alf_perm_tqn ON alf_permission (type_qname_id);
ALTER TABLE alf_permission ADD CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id);

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

-- Add type_qname index for nodes
CREATE INDEX tidx_node_tqn ON alf_node (type_qname);

-- Copy data over
INSERT INTO t_alf_node
   (
      id, version, store_id, uuid, transaction_id, node_deleted, type_qname_id, acl_id,
      audit_creator, audit_created, audit_modifier, audit_modified
   )
   SELECT
      n.id, 1, s.id, n.uuid, nstat.transaction_id, 0, q.qname_id, n.acl_id,
      null, null, null, null
   FROM
      alf_node n
      JOIN t_qnames q ON (q.qname = n.type_qname)
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
      tqn.qname_id,
      tqndyn.namespace_id, tqndyn.local_name,
      ca.is_primary, ca.assoc_index
   FROM
      alf_child_assoc ca
      JOIN t_qnames_dyn tqndyn ON (ca.qname = tqndyn.qname)
      JOIN t_qnames tqn ON (ca.type_qname = tqn.qname)
;

-- Clean up
DROP TABLE t_qnames_dyn;
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
      tqn.qname_id
   FROM
      alf_node_assoc na
      JOIN t_qnames tqn ON (na.type_qname = tqn.qname)
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
      tqn.qname_id
   FROM
      alf_node_aspects na
      JOIN t_qnames tqn ON (na.qname = tqn.qname)
   WHERE
      tqn.qname NOT IN
      (
         '{http://www.alfresco.org/model/system/1.0}referenceable'
      )
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
      aspects_old.node_id,
      tqn.qname_id
   FROM
      avm_aspects aspects_old
      JOIN t_qnames tqn ON (aspects_old.qname = tqn.qname)
;
INSERT INTO t_avm_aspects
   (
      node_id, qname_id
   )
   SELECT
      anew.id,
      tqn.qname_id
   FROM
      avm_aspects_new anew
      JOIN t_qnames tqn ON (anew.name = tqn.qname)
      LEFT JOIN avm_aspects aold ON (anew.id = aold.node_id AND anew.name = aold.qname)
   WHERE
      aold.id IS NULL
;

-- Clean up
DROP TABLE avm_aspects;
DROP TABLE avm_aspects_new;
ALTER TABLE t_avm_aspects RENAME TO avm_aspects;

-- ----------------------------------
-- Migrate Sundry Property Tables --
-- ----------------------------------

-- Create temporary mapping for property types
CREATE TABLE t_prop_types
(
   type_name varchar2(15 char) NOT NULL,
   type_id number(10,0) NOT NULL,
   PRIMARY KEY (type_name)
);
INSERT INTO t_prop_types values ('NULL', 0);
INSERT INTO t_prop_types values ('BOOLEAN', 1);
INSERT INTO t_prop_types values ('INTEGER', 2);
INSERT INTO t_prop_types values ('LONG', 3);
INSERT INTO t_prop_types values ('FLOAT', 4);
INSERT INTO t_prop_types values ('DOUBLE', 5);
INSERT INTO t_prop_types values ('STRING', 6);
INSERT INTO t_prop_types values ('DATE', 7);
INSERT INTO t_prop_types values ('DB_ATTRIBUTE', 8);
INSERT INTO t_prop_types values ('SERIALIZABLE', 9);
INSERT INTO t_prop_types values ('MLTEXT', 10);
INSERT INTO t_prop_types values ('CONTENT', 11);
INSERT INTO t_prop_types values ('NODEREF', 12);
INSERT INTO t_prop_types values ('CHILD_ASSOC_REF', 13);
INSERT INTO t_prop_types values ('ASSOC_REF', 14);
INSERT INTO t_prop_types values ('QNAME', 15);
INSERT INTO t_prop_types values ('PATH', 16);
INSERT INTO t_prop_types values ('LOCALE', 17);
INSERT INTO t_prop_types values ('VERSION_NUMBER', 18);

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
      tqn.qname_id,
      ptypes_actual.type_id, ptypes_persisted.type_id,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, TO_LOB(p.serializable_value)
   FROM
      avm_store_properties p
      JOIN t_qnames tqn ON (p.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = p.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = p.persisted_type)
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
      tqn.qname_id,
      ptypes_actual.type_id, ptypes_persisted.type_id,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, TO_LOB(p.serializable_value)
   FROM
      avm_node_properties_new p
      JOIN t_qnames tqn ON (p.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = p.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = p.persisted_type)
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
      tqn.qname_id,
      ptypes_actual.type_id, ptypes_persisted.type_id,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, TO_LOB(p.serializable_value)
   FROM
      avm_node_properties p
      JOIN t_qnames tqn ON (p.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = p.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = p.persisted_type)
      LEFT OUTER JOIN t_avm_node_properties tanp ON (tqn.qname_id = tanp.qname_id)
   WHERE
      tanp.qname_id IS NULL
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
      np.node_id, tqn.qname_id, -1, 1,
      ptypes_actual.type_id, ptypes_persisted.type_id,
      np.boolean_value, np.long_value, np.float_value, np.double_value,
      np.string_value,
      TO_LOB(np.serializable_value)
   FROM
      alf_node_properties np
      JOIN t_qnames tqn ON (np.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = np.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = np.persisted_type)
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
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
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
      AND alf_namespace.uri = 'FILLER-http://www.alfresco.org/model/content/1.0'
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
      np.node_id, tqn.qname_id, -1, loc.id,
      -1, 0,
      0, 0, 0, 0,
      a2.string_value,
      TO_LOB(a2.serializable_value)
   FROM
      alf_node_properties np
      JOIN t_qnames tqn ON (np.qname = tqn.qname)
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

-- ---------------------------------------------------
-- Remove the FILLER- values from the namespace uri --
-- ---------------------------------------------------
UPDATE alf_namespace SET uri = '.empty' WHERE uri = 'FILLER-';
UPDATE alf_namespace SET uri = SUBSTR(uri, 8) WHERE uri LIKE 'FILLER-%';

-- ------------------
-- Final clean up --
-- ------------------
DROP TABLE t_qnames;
DROP TABLE t_prop_types;
DROP TABLE alf_node_status;
DROP INDEX FKBD4FF53D22DBA5BA;  -- (OPTIONAL)
ALTER TABLE alf_store DROP CONSTRAINT FKBD4FF53D22DBA5BA;  -- (OPTIONAL)
ALTER TABLE alf_store DROP CONSTRAINT alf_store_root;  -- (OPTIONAL)
DROP TABLE alf_node;
ALTER TABLE t_alf_node RENAME TO alf_node;
DROP TABLE alf_store;
ALTER TABLE t_alf_store RENAME TO alf_store;


-- -------------------------------------
-- Modify index and constraint names --
-- -------------------------------------

-- Since one has to rebuild indexes after changing a column to a BLOB, this
-- is also a good time to remap the BLOB column!
DROP INDEX fk_attributes_n_acl;  -- (optional)
DROP INDEX fk_attr_n_acl;  -- (optional)
ALTER TABLE alf_attributes DROP CONSTRAINT fk_attributes_n_acl;  -- (optional)
DROP INDEX FK64D0B9CF69B9F16A; -- (optional)
ALTER TABLE alf_global_attributes DROP CONSTRAINT FK64D0B9CF69B9F16A; -- (optional)
DROP INDEX FKC7D52FB02C5AB86C; -- (optional)
ALTER TABLE alf_list_attribute_entries DROP CONSTRAINT FKC7D52FB02C5AB86C; -- (optional)
DROP INDEX FKC7D52FB0ACD8822C; -- (optional)
ALTER TABLE alf_list_attribute_entries DROP CONSTRAINT FKC7D52FB0ACD8822C; -- (optional)
DROP INDEX FK335CAE26AEAC208C; -- (optional)
ALTER TABLE alf_map_attribute_entries DROP CONSTRAINT FK335CAE26AEAC208C; -- (optional)
DROP INDEX FK335CAE262C5AB86C; -- (optional)
ALTER TABLE alf_map_attribute_entries DROP CONSTRAINT FK335CAE262C5AB86C; -- (optional)
ALTER TABLE alf_attributes DROP PRIMARY KEY DROP INDEX;
ALTER TABLE alf_attributes MODIFY (serializable_value BLOB NULL); -- (optional)
ALTER TABLE alf_attributes ADD PRIMARY KEY (id);
CREATE INDEX fk_alf_attr_acl ON alf_attributes (acl_id);

DROP INDEX adt_woy_idx;  -- (optional)
DROP INDEX adt_date_idx;  -- (optional)
DROP INDEX adt_y_idx;  -- (optional)
DROP INDEX adt_q_idx;  -- (optional)
DROP INDEX adt_m_idx;  -- (optional)
DROP INDEX adt_dow_idx;  -- (optional)
DROP INDEX adt_doy_idx;  -- (optional)
DROP INDEX adt_dom_idx;  -- (optional)
DROP INDEX adt_hy_idx;  -- (optional)
DROP INDEX adt_wom_idx;  -- (optional)

CREATE INDEX idx_alf_adtd_woy ON alf_audit_date (week_of_year);
CREATE INDEX idx_alf_adtd_q ON alf_audit_date (quarter);
CREATE INDEX idx_alf_adtd_wom ON alf_audit_date (week_of_month);
CREATE INDEX idx_alf_adtd_dom ON alf_audit_date (day_of_month);
CREATE INDEX idx_alf_adtd_doy ON alf_audit_date (day_of_year);
CREATE INDEX idx_alf_adtd_dow ON alf_audit_date (day_of_week);
CREATE INDEX idx_alf_adtd_m ON alf_audit_date (month);
CREATE INDEX idx_alf_adtd_hy ON alf_audit_date (half_year);
CREATE INDEX idx_alf_adtd_fy ON alf_audit_date (full_year);
CREATE INDEX idx_alf_adtd_dat ON alf_audit_date (date_only);

DROP INDEX adt_user_idx;  -- (optional)
DROP INDEX adt_store_idx;  -- (optional)

DROP INDEX FKEAD18174A0F9B8D9;
DROP INDEX FKEAD1817484342E39;
DROP INDEX FKEAD18174F524CFD7;

ALTER TABLE alf_audit_fact
   DROP CONSTRAINT FKEAD18174A0F9B8D9
   DROP CONSTRAINT FKEAD1817484342E39
   DROP CONSTRAINT FKEAD18174F524CFD7
;

CREATE INDEX idx_alf_adtf_ref ON alf_audit_fact (store_protocol, store_id, node_uuid);
CREATE INDEX idx_alf_adtf_usr ON alf_audit_fact (user_id);
CREATE INDEX fk_alf_adtf_src ON alf_audit_fact (audit_source_id);
CREATE INDEX fk_alf_adtf_date ON alf_audit_fact (audit_date_id);
CREATE INDEX fk_alf_adtf_conf ON alf_audit_fact (audit_conf_id);

ALTER TABLE alf_audit_fact
   ADD CONSTRAINT fk_alf_adtf_src FOREIGN KEY (audit_source_id) REFERENCES alf_audit_source (id)
   ADD CONSTRAINT fk_alf_adtf_date FOREIGN KEY (audit_date_id) REFERENCES alf_audit_date (id)
   ADD CONSTRAINT fk_alf_adtf_conf FOREIGN KEY (audit_conf_id) REFERENCES alf_audit_config (id)
;

DROP INDEX app_source_app_idx;  -- (optional)
DROP INDEX app_source_ser_idx;  -- (optional)
DROP INDEX app_source_met_idx;  -- (optional)

CREATE INDEX idx_alf_adts_met ON alf_audit_source (method);
CREATE INDEX idx_alf_adts_ser ON alf_audit_source (service);
CREATE INDEX idx_alf_adts_app ON alf_audit_source (application);

-- alf_global_attributes.attribute is declared unique.  Indexes may automatically have been created.
CREATE INDEX fk_alf_gatt_att ON alf_global_attributes (attribute);  -- (optional)
ALTER TABLE alf_global_attributes
   ADD CONSTRAINT fk_alf_gatt_att FOREIGN KEY (attribute) REFERENCES alf_attributes (id)
;

CREATE INDEX fk_alf_lent_att ON alf_list_attribute_entries (attribute_id);
CREATE INDEX fk_alf_lent_latt ON alf_list_attribute_entries (list_id);
ALTER TABLE alf_list_attribute_entries
   ADD CONSTRAINT fk_alf_lent_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id)
   ADD CONSTRAINT fk_alf_lent_latt FOREIGN KEY (list_id) REFERENCES alf_attributes (id)
;

CREATE INDEX fk_alf_matt_matt ON alf_map_attribute_entries (map_id);
CREATE INDEX fk_alf_matt_att ON alf_map_attribute_entries (attribute_id);
ALTER TABLE alf_map_attribute_entries
   ADD CONSTRAINT fk_alf_matt_matt FOREIGN KEY (map_id) REFERENCES alf_attributes (id)
   ADD CONSTRAINT fk_alf_matt_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id)
;

DROP INDEX idx_commit_time_ms; -- (optional)
DROP INDEX FKB8761A3A9AE340B7;
CREATE INDEX fk_alf_txn_svr ON alf_transaction (server_id);
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms);
ALTER TABLE alf_transaction DROP CONSTRAINT FKB8761A3A9AE340B7;
ALTER TABLE alf_transaction
   ADD CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES alf_server (id)
;
DROP INDEX fk_avm_ce_child; -- (optional)
DROP INDEX fk_avm_ce_parent; -- (optional)
ALTER TABLE avm_child_entries DROP CONSTRAINT fk_avm_ce_child; -- (optional)
ALTER TABLE avm_child_entries DROP CONSTRAINT fk_avm_ce_parent; -- (optional)
CREATE INDEX fk_avm_ce_child ON avm_child_entries (child_id);
CREATE INDEX fk_avm_ce_parent ON avm_child_entries (parent_id);
ALTER TABLE avm_child_entries
   ADD CONSTRAINT fk_avm_ce_child FOREIGN KEY (child_id) REFERENCES avm_nodes (id)
   ADD CONSTRAINT fk_avm_ce_parent FOREIGN KEY (parent_id) REFERENCES avm_nodes (id)
;

DROP INDEX fk_avm_hl_desc; -- (optional)
DROP INDEX fk_avm_hl_ancestor; -- (optional)
DROP INDEX idx_avm_hl_revpk; -- (optional)
ALTER TABLE avm_history_links DROP CONSTRAINT fk_avm_hl_desc; -- (optional)
ALTER TABLE avm_history_links DROP CONSTRAINT fk_avm_hl_ancestor; -- (optional)
DROP INDEX idx_avm_hl_revpk; -- (optional)
CREATE INDEX fk_avm_hl_desc ON avm_history_links (descendent);
CREATE INDEX fk_avm_hl_ancestor ON avm_history_links (ancestor);
CREATE INDEX idx_avm_hl_revpk ON avm_history_links (descendent, ancestor);
ALTER TABLE avm_history_links
   ADD CONSTRAINT fk_avm_hl_desc FOREIGN KEY (descendent) REFERENCES avm_nodes (id)
   ADD CONSTRAINT fk_avm_hl_ancestor FOREIGN KEY (ancestor) REFERENCES avm_nodes (id)
;

DROP INDEX fk_avm_ml_to; -- (optional)
DROP INDEX fk_avm_ml_from; -- (optional)
ALTER TABLE avm_merge_links DROP CONSTRAINT fk_avm_ml_to; -- (optional)
ALTER TABLE avm_merge_links DROP CONSTRAINT fk_avm_ml_from; -- (optional)

CREATE INDEX fk_avm_ml_to ON avm_merge_links (mto);
CREATE INDEX fk_avm_ml_from ON avm_merge_links (mfrom);
ALTER TABLE avm_merge_links
   ADD CONSTRAINT fk_avm_ml_to FOREIGN KEY (mto) REFERENCES avm_nodes (id)
   ADD CONSTRAINT fk_avm_ml_from FOREIGN KEY (mfrom) REFERENCES avm_nodes (id)
;
DROP INDEX fk_avm_n_acl; -- (optional)
DROP INDEX fk_avm_n_store; -- (optional)
DROP INDEX idx_avm_n_pi; -- (optional)
ALTER TABLE avm_nodes DROP CONSTRAINT fk_avm_n_acl; -- (optional)
ALTER TABLE avm_nodes DROP CONSTRAINT fk_avm_n_store; -- (optional)

CREATE INDEX fk_avm_n_acl ON avm_nodes (acl_id);
CREATE INDEX fk_avm_n_store ON avm_nodes (store_new_id);
CREATE INDEX idx_avm_n_pi ON avm_nodes (primary_indirection);
ALTER TABLE avm_nodes
   ADD CONSTRAINT fk_avm_n_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
   ADD CONSTRAINT fk_avm_n_store FOREIGN KEY (store_new_id) REFERENCES avm_stores (id)
;

DROP INDEX fk_avm_s_root; -- (optional)
ALTER TABLE avm_stores DROP CONSTRAINT fk_avm_s_root; -- (optional)
CREATE INDEX fk_avm_s_acl ON avm_stores (acl_id);
CREATE INDEX fk_avm_s_root ON avm_stores (current_root_id);
ALTER TABLE avm_stores
   ADD CONSTRAINT fk_avm_s_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
   ADD CONSTRAINT fk_avm_s_root FOREIGN KEY (current_root_id) REFERENCES avm_nodes (id)
;

DROP INDEX FK182E672DEB9D70C; -- (optional)
ALTER TABLE avm_version_layered_node_entry DROP CONSTRAINT FK182E672DEB9D70C; -- (optional)
CREATE INDEX fk_avm_vlne_vr ON avm_version_layered_node_entry (version_root_id);
ALTER TABLE avm_version_layered_node_entry
   ADD CONSTRAINT fk_avm_vlne_vr FOREIGN KEY (version_root_id) REFERENCES avm_version_roots (id)
;

DROP INDEX idx_avm_vr_version; -- (optional)
DROP INDEX idx_avm_vr_revuq; -- (optional)
DROP INDEX fk_avm_vr_root; -- (optional)
DROP INDEX fk_avm_vr_store; -- (optional)
ALTER TABLE avm_version_roots DROP CONSTRAINT fk_avm_vr_root; -- (optional)
ALTER TABLE avm_version_roots DROP CONSTRAINT fk_avm_vr_store; -- (optional)
CREATE INDEX idx_avm_vr_version ON avm_version_roots (version_id);
CREATE INDEX idx_avm_vr_revuq ON avm_version_roots (avm_store_id, version_id);
CREATE INDEX fk_avm_vr_root ON avm_version_roots (root_id);
CREATE INDEX fk_avm_vr_store ON avm_version_roots (avm_store_id);
ALTER TABLE avm_version_roots
   ADD CONSTRAINT fk_avm_vr_root FOREIGN KEY (root_id) REFERENCES avm_nodes (id)
   ADD CONSTRAINT fk_avm_vr_store FOREIGN KEY (avm_store_id) REFERENCES avm_stores (id)
; 

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-Upgrade-From-2.1';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-Upgrade-From-2.1', 'Manually executed script upgrade V2.2: Upgrade from 2.1',
    0, 85, -1, 91, null, 'UNKOWN', 1, 1, 'Script completed'
  );
