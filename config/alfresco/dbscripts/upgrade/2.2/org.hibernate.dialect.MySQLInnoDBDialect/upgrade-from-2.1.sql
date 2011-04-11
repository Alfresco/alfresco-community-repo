--
-- Title:      Apply schema modifications to upgrade from 2.1 
-- Database:   MySQL
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

CREATE TABLE alf_namespace
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   uri VARCHAR(100) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (uri)
) ENGINE=InnoDB;

CREATE TABLE alf_qname
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   ns_id BIGINT NOT NULL,
   local_name VARCHAR(200) NOT NULL,
   INDEX fk_alf_qname_ns (ns_id),
   CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id),
   PRIMARY KEY (id),
   UNIQUE (ns_id, local_name)
) ENGINE=InnoDB;

-- Create temporary table to hold static QNames
CREATE TABLE t_qnames
(
   qname VARCHAR(255) NOT NULL,
   namespace VARCHAR(100),
   localname VARCHAR(200),
   qname_id BIGINT,
   INDEX tidx_tqn_qn (qname),
   INDEX tidx_tqn_ns (namespace),
   INDEX tidx_tqn_ln (localname)
) ENGINE=InnoDB;

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
UPDATE t_qnames SET namespace = CONCAT('FILLER-', SUBSTR(SUBSTRING_INDEX(qname, '}', 1), 2));
UPDATE t_qnames SET localname = SUBSTRING(qname, INSTR(qname, '}')+1);

-- Move the Namespaces to their new home
INSERT INTO alf_namespace (uri, version)
(
   SELECT
      distinct(x.namespace), 1
   FROM
   (
      SELECT t.namespace, n.uri FROM t_qnames t LEFT OUTER JOIN alf_namespace n ON (n.uri = t.namespace)
   ) x
   WHERE
      x.uri IS NULL
);

-- Move the Localnames to their new home
INSERT INTO alf_qname (ns_id, local_name, version)
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
);

-- Record the new qname IDs
UPDATE t_qnames t SET t.qname_id =
(
   SELECT q.id FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE ns.uri = t.namespace AND q.local_name = t.localname
);

-- ----------------------------
-- SHORTCUT:
-- Up to this point, we have been extracting static data.  The data can be dumped and loaded
-- to do faster testing of the ugprades:
--   mysqldump derek1 alf_qname alf_namespace t_qnames > extracted-qnames.sql
-- Load the dump file and continue from this point
-- ----------------------------

-- Create temporary table for dynamic (child) QNames
CREATE TABLE t_qnames_dyn
(
   qname VARCHAR(255) NOT NULL,
   namespace VARCHAR(100),
   namespace_id BIGINT,
   local_name VARCHAR(255),
   INDEX tidx_qnd_qn (qname),
   INDEX tidx_qnd_ns (namespace)
) ENGINE=InnoDB;

-- Populate the table with the child association paths
-- Query OK, 415312 rows affected (1 min 11.91 sec)
INSERT INTO t_qnames_dyn (qname)
(
   SELECT distinct(qname) FROM alf_child_assoc
);

-- Extract the Namespace
-- Query OK, 415312 rows affected (20.03 sec)
UPDATE t_qnames_dyn SET namespace = CONCAT('FILLER-', SUBSTR(SUBSTRING_INDEX(qname, '}', 1), 2));

-- Extract the Localname
-- Query OK, 415312 rows affected (16.22 sec)
UPDATE t_qnames_dyn SET local_name = SUBSTRING(qname, INSTR(qname, '}')+1);

-- Move the namespaces to the their new home
-- Query OK, 4 rows affected (34.59 sec)
INSERT INTO alf_namespace (uri, version)
(
   SELECT
      distinct(x.namespace), 1
   FROM
   (
      SELECT t.namespace, n.uri FROM t_qnames_dyn t LEFT OUTER JOIN alf_namespace n ON (n.uri = t.namespace)
   ) x
   WHERE
      x.uri IS NULL
);

-- Record the new namespace IDs
-- Query OK, 415312 rows affected (10.41 sec)
UPDATE t_qnames_dyn t SET t.namespace_id = (SELECT ns.id FROM alf_namespace ns WHERE ns.uri = t.namespace);

-- Recoup some storage
ALTER TABLE t_qnames_dyn DROP COLUMN namespace;
OPTIMIZE TABLE t_qnames_dyn;

-- ----------------------------
-- Populate the Permissions --
-- ----------------------------

-- This is a small table so we change it in place
ALTER TABLE alf_permission
   DROP INDEX type_qname,
   ADD COLUMN type_qname_id BIGINT NULL AFTER id
;
UPDATE alf_permission p SET p.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', SUBSTR(ns.uri, 8), '}', q.local_name) = p.type_qname
);
ALTER TABLE alf_permission
   DROP COLUMN type_qname,
   MODIFY COLUMN type_qname_id BIGINT NOT NULL AFTER id,
   ADD UNIQUE (type_qname_id, name),
   ADD INDEX fk_alf_perm_tqn (type_qname_id),
   ADD CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
;

-- -------------------
-- Build new Store --
-- -------------------

CREATE TABLE t_alf_store
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   protocol VARCHAR(50) NOT NULL,
   identifier VARCHAR(100) NOT NULL,
   root_node_id BIGINT,
   PRIMARY KEY (id),
   UNIQUE (protocol, identifier)
) ENGINE=InnoDB;

-- --------------------------
-- Populate the ADM nodes --
-- --------------------------

CREATE TABLE t_alf_node (
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
   INDEX idx_alf_node_del (node_deleted),
   INDEX fk_alf_node_acl (acl_id),
   INDEX fk_alf_node_tqn (type_qname_id),
   INDEX fk_alf_node_txn (transaction_id),
   INDEX fk_alf_node_store (store_id),
   CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id), 
   CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id), 
   CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id), 
   CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES t_alf_store (id), 
   PRIMARY KEY (id),
   UNIQUE (store_id, uuid)
) ENGINE=InnoDB;

-- Fill the store table
INSERT INTO t_alf_store (version, protocol, identifier, root_node_id)
   SELECT 1, protocol, identifier, root_node_id FROM alf_store
;

-- Summarize the alf_node_status table
CREATE TABLE t_summary_nstat
(
   node_id BIGINT(20) NOT NULL,
   transaction_id BIGINT(20) DEFAULT NULL,
   PRIMARY KEY (node_id)
) ENGINE=InnoDB;
--FOREACH alf_node_status.node_id system.upgrade.t_summary_nstat.batchsize
INSERT INTO t_summary_nstat (node_id, transaction_id) 
  SELECT node_id, transaction_id
  FROM alf_node_status
  WHERE node_id IS NOT NULL
  AND node_id >= ${LOWERBOUND} AND node_id <= ${UPPERBOUND};

-- Copy data over
--FOREACH alf_node.id system.upgrade.t_alf_node.batchsize
INSERT INTO t_alf_node
   (
      id, version, store_id, uuid, transaction_id, node_deleted, type_qname_id, acl_id,
      audit_creator, audit_created, audit_modifier, audit_modified, audit_accessed
   )
   SELECT STRAIGHT_JOIN
      n.id, 1, s.id, n.uuid, nstat.transaction_id, false, q.qname_id, n.acl_id,
      null, null, null, null, null
   FROM
      alf_node n
      JOIN t_qnames q ON (q.qname = n.type_qname)
      JOIN t_summary_nstat nstat ON (nstat.node_id = n.id)
      JOIN t_alf_store s ON (s.protocol = n.protocol AND s.identifier = n.identifier)
   WHERE
      n.id >= ${LOWERBOUND} AND n.id <= ${UPPERBOUND}
;
DROP TABLE t_summary_nstat;

-- Hook the store up to the root node
ALTER TABLE t_alf_store 
   ADD INDEX fk_alf_store_root (root_node_id), 
   ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES t_alf_node (id)
;

-- -----------------------------
-- Populate Version Counter  --
-- -----------------------------

CREATE TABLE t_alf_version_count
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   store_id BIGINT NOT NULL UNIQUE,
   version_count INTEGER NOT NULL,
   INDEX fk_alf_vc_store (store_id),
   CONSTRAINT fk_alf_vc_store FOREIGN KEY (store_id) REFERENCES t_alf_store (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO t_alf_version_count
   (
      version, store_id, version_count
   )
   SELECT
      1, s.id, vc.version_count
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
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   parent_node_id BIGINT NOT NULL,
   type_qname_id BIGINT NOT NULL,
   child_node_name_crc BIGINT NOT NULL,
   child_node_name VARCHAR(50) NOT NULL,
   child_node_id BIGINT NOT NULL,
   qname_ns_id BIGINT NOT NULL,
   qname_localname VARCHAR(255) NOT NULL,
   is_primary BIT,
   assoc_index INTEGER,
   INDEX idx_alf_cass_qnln (qname_localname),
   INDEX fk_alf_cass_pnode (parent_node_id),
   INDEX fk_alf_cass_cnode (child_node_id),
   INDEX fk_alf_cass_tqn (type_qname_id),
   INDEX fk_alf_cass_qnns (qname_ns_id),
   INDEX idx_alf_cass_pri (parent_node_id, is_primary, child_node_id),
   CONSTRAINT fk_alf_cass_pnode foreign key (parent_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_cass_cnode foreign key (child_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_cass_tqn foreign key (type_qname_id) REFERENCES alf_qname (id),
   CONSTRAINT fk_alf_cass_qnns foreign key (qname_ns_id) REFERENCES alf_namespace (id),
   PRIMARY KEY (id),
   UNIQUE (parent_node_id, type_qname_id, child_node_name_crc, child_node_name)
) ENGINE=InnoDB;

--FOREACH alf_child_assoc.id system.upgrade.t_alf_child_assoc.batchsize
INSERT INTO t_alf_child_assoc
   (
      id, version,
      parent_node_id,
      type_qname_id,
      child_node_name_crc, child_node_name,
      child_node_id,
      qname_ns_id, qname_localname,
      is_primary, assoc_index
   )
   SELECT STRAIGHT_JOIN
      ca.id, 1,
      ca.parent_node_id,
      tqn.qname_id,
      ca.child_node_name_crc, ca.child_node_name,
      ca.child_node_id,
      tqndyn.namespace_id, tqndyn.local_name,
      ca.is_primary, ca.assoc_index
   FROM
      alf_child_assoc ca
      JOIN t_qnames_dyn tqndyn ON (ca.qname = tqndyn.qname)
      JOIN t_qnames tqn ON (ca.type_qname = tqn.qname)
   WHERE
      ca.id >= ${LOWERBOUND} AND ca.id <= ${UPPERBOUND}
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
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL, 
   source_node_id BIGINT NOT NULL,
   target_node_id BIGINT NOT NULL,
   type_qname_id BIGINT NOT NULL,
   INDEX fk_alf_nass_snode (source_node_id),
   INDEX fk_alf_nass_tnode (target_node_id),
   INDEX fk_alf_nass_tqn (type_qname_id),
   CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (id),
   UNIQUE (source_node_id, target_node_id, type_qname_id)
) ENGINE=InnoDB;

--FOREACH alf_node_assoc.id system.upgrade.t_alf_node_assoc.batchsize
INSERT INTO t_alf_node_assoc
   (
      id, version,
      source_node_id, target_node_id,
      type_qname_id
   )
   SELECT STRAIGHT_JOIN
      na.id, 1,
      na.source_node_id, na.target_node_id,
      tqn.qname_id
   FROM
      alf_node_assoc na
      JOIN t_qnames tqn ON (na.type_qname = tqn.qname)
   WHERE
      na.id >= ${LOWERBOUND} AND na.id <= ${UPPERBOUND}
;

-- Clean up
DROP TABLE alf_node_assoc;
ALTER TABLE t_alf_node_assoc RENAME TO alf_node_assoc;

-- -----------------------------
-- Populate the Node Aspects --
-- -----------------------------

CREATE TABLE t_alf_node_aspects
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL,
   INDEX fk_alf_nasp_n (node_id),
   INDEX fk_alf_nasp_qn (qname_id),
   CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
) ENGINE=InnoDB;

--FOREACH alf_node_aspects.node_id system.upgrade.t_alf_node_aspects.batchsize
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
      tqn.qname != '{http://www.alfresco.org/model/system/1.0}referenceable'
      AND na.node_id >= ${LOWERBOUND} AND na.node_id <= ${UPPERBOUND}
;

-- Clean up
DROP TABLE alf_node_aspects;
ALTER TABLE t_alf_node_aspects RENAME TO alf_node_aspects;

-- ---------------------------------
-- Populate the AVM Node Aspects --
-- ---------------------------------

CREATE TABLE t_avm_aspects
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL,
   INDEX fk_avm_nasp_n (node_id),
   INDEX fk_avm_nasp_qn (qname_id),
   CONSTRAINT fk_avm_nasp_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
) ENGINE=InnoDB;

--FOREACH avm_aspects.node_id system.upgrade.t_avm_aspects.batchsize
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
   WHERE
      aspects_old.node_id >= ${LOWERBOUND} AND aspects_old.node_id <= ${UPPERBOUND}
;
--FOREACH avm_aspects_new.id system.upgrade.t_avm_aspects.batchsize
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
      AND anew.id >= ${LOWERBOUND} AND anew.id <= ${UPPERBOUND}
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
   type_name VARCHAR(15) NOT NULL,
   type_id INTEGER NOT NULL,
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
   id BIGINT NOT NULL AUTO_INCREMENT,
   avm_store_id BIGINT,
   qname_id BIGINT NOT NULL,
   actual_type_n integer NOT NULL,
   persisted_type_n integer NOT NULL,
   multi_valued bit NOT NULL,
   boolean_value bit,
   long_value BIGINT,
   float_value float,
   double_value DOUBLE PRECISION,
   string_value TEXT,
   serializable_value blob,
   INDEX fk_avm_sprop_store (avm_store_id),
   INDEX fk_avm_sprop_qname (qname_id),
   CONSTRAINT fk_avm_sprop_store FOREIGN KEY (avm_store_id) REFERENCES avm_stores (id),
   CONSTRAINT fk_avm_sprop_qname FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;
--FOREACH avm_store_properties.avm_store_id system.upgrade.t_avm_store_properties.batchsize
INSERT INTO t_avm_store_properties
   (
      avm_store_id,
      qname_id,
      actual_type_n, persisted_type_n,
      multi_valued, boolean_value, long_value, float_value, double_value, string_value, serializable_value
   )
   SELECT
      p.avm_store_id,
      tqn.qname_id,
      ptypes_actual.type_id, ptypes_persisted.type_id,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_store_properties p
      JOIN t_qnames tqn ON (p.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = p.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = p.persisted_type)
   WHERE
      p.avm_store_id >= ${LOWERBOUND} AND p.avm_store_id <= ${UPPERBOUND}
;
DROP TABLE avm_store_properties;
ALTER TABLE t_avm_store_properties RENAME TO avm_store_properties;

-- Modify the avm_node_properties_new table
CREATE TABLE t_avm_node_properties
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL,
   actual_type_n INTEGER NOT NULL,
   persisted_type_n INTEGER NOT NULL,
   multi_valued BIT NOT NULL,
   boolean_value BIT,
   long_value BIGINT,
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value TEXT,
   serializable_value BLOB,
   INDEX fk_avm_nprop_n (node_id),
   INDEX fk_avm_nprop_qn (qname_id),
   CONSTRAINT fk_avm_nprop_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
) ENGINE=InnoDB;
--FOREACH avm_node_properties_new.node_id system.upgrade.t_avm_node_properties.batchsize
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
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_node_properties_new p
      JOIN t_qnames tqn ON (p.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = p.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = p.persisted_type)
   WHERE
      p.node_id >= ${LOWERBOUND} AND p.node_id <= ${UPPERBOUND}   
;
--FOREACH avm_node_properties.node_id system.upgrade.t_avm_node_properties.batchsize
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
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_node_properties p
      JOIN t_qnames tqn ON (p.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = p.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = p.persisted_type)
      LEFT OUTER JOIN t_avm_node_properties tanp ON (tqn.qname_id = tanp.qname_id)
   WHERE
      tanp.qname_id IS NULL
      AND p.node_id >= ${LOWERBOUND} AND p.node_id <= ${UPPERBOUND}   
;

DROP TABLE avm_node_properties_new;
DROP TABLE avm_node_properties;
ALTER TABLE t_avm_node_properties RENAME TO avm_node_properties;


-- -----------------
-- Build Locales --
-- -----------------

CREATE TABLE alf_locale
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL DEFAULT 1,
   locale_str VARCHAR(20) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (locale_str)
) ENGINE=InnoDB;

INSERT INTO alf_locale (id, locale_str) VALUES (1, '.default');

-- Locales come from the attribute table which was used to support MLText persistence
-- Query OK, 0 rows affected (17.22 sec)
--FOREACH alf_attributes.id system.upgrade.alf_attributes.batchsize
INSERT INTO alf_locale (locale_str)
   SELECT DISTINCT(ma.mkey)
      FROM alf_node_properties np
      JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
      JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
      LEFT OUTER JOIN alf_locale l ON (ma.mkey = l.locale_str)
      WHERE l.locale_str IS NULL
      AND a1.id >= ${LOWERBOUND} AND a1.id <= ${UPPERBOUND}   
;

-- -------------------------------
-- Migrate ADM Property Tables --
-- -------------------------------

CREATE TABLE t_alf_node_properties
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL,
   locale_id BIGINT NOT NULL,
   list_index smallint NOT NULL,
   actual_type_n INTEGER NOT NULL,
   persisted_type_n INTEGER NOT NULL,
   boolean_value BIT,
   long_value BIGINT,
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value TEXT,
   serializable_value BLOB,
   INDEX fk_alf_nprop_n (node_id),
   INDEX fk_alf_nprop_qn (qname_id),
   INDEX fk_alf_nprop_loc (locale_id),
   CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id),
   PRIMARY KEY (node_id, qname_id, list_index, locale_id)
) ENGINE=InnoDB;

-- Copy values over
--FOREACH alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
INSERT INTO t_alf_node_properties
   (
      node_id, qname_id, locale_id, list_index,
      actual_type_n, persisted_type_n,
      boolean_value, long_value, float_value, double_value,
      string_value,
      serializable_value
   )
   SELECT
      np.node_id, tqn.qname_id, 1, -1,
      ptypes_actual.type_id, ptypes_persisted.type_id,
      np.boolean_value, np.long_value, np.float_value, np.double_value,
      np.string_value,
      np.serializable_value
   FROM
      alf_node_properties np
      JOIN t_qnames tqn ON (np.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = np.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = np.persisted_type)
   WHERE
      np.attribute_value IS NULL
      AND np.node_id >= ${LOWERBOUND} AND np.node_id <= ${UPPERBOUND}
;

-- Update cm:auditable properties on the nodes
--FOREACH t_alf_node.id system.upgrade.t_alf_node.batchsize
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
)
WHERE n.id >= ${LOWERBOUND} AND n.id <= ${UPPERBOUND};

--FOREACH t_alf_node.id system.upgrade.t_alf_node.batchsize
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
)
WHERE n.id >= ${LOWERBOUND} AND n.id <= ${UPPERBOUND};

--FOREACH t_alf_node.id system.upgrade.t_alf_node.batchsize
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
)
WHERE n.id >= ${LOWERBOUND} AND n.id <= ${UPPERBOUND};

--FOREACH t_alf_node.id system.upgrade.t_alf_node.batchsize
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
)
WHERE n.id >= ${LOWERBOUND} AND n.id <= ${UPPERBOUND};
-- Remove the unused cm:auditable properties
-- SHORTCUT:
-- The qname_id values can be determined up front
-- SELECT * FROM
--    alf_qname
--    JOIN alf_namespace ON (alf_qname.ns_id = alf_namespace.id)
--    WHERE
--       alf_namespace.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
--       alf_qname.local_name IN ('creator', 'created', 'modifier', 'modified')
-- ;
-- DELETE t_alf_node_properties
--    FROM t_alf_node_properties
--    WHERE
--       qname_id IN (13, 14, 23, 24);
--FOREACH t_alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
DELETE t_alf_node_properties
   FROM t_alf_node_properties
   JOIN alf_qname ON (t_alf_node_properties.qname_id = alf_qname.id)
   JOIN alf_namespace ON (alf_qname.ns_id = alf_namespace.id)
   WHERE
      alf_namespace.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
      alf_qname.local_name IN ('creator', 'created', 'modifier', 'modified') AND
      t_alf_node_properties.node_id >= ${LOWERBOUND} AND t_alf_node_properties.node_id <= ${UPPERBOUND}
;

-- Copy all MLText values over
--FOREACH alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
INSERT INTO t_alf_node_properties
   (
      node_id, qname_id, locale_id, list_index,
      actual_type_n, persisted_type_n,
      boolean_value, long_value, float_value, double_value,
      string_value,
      serializable_value
   )
   SELECT
      np.node_id, tqn.qname_id, loc.id, -1,
      -1, 0,
      FALSE, 0, 0, 0,
      a2.string_value,
      a2.serializable_value
   FROM
      alf_node_properties np
      JOIN t_qnames tqn ON (np.qname = tqn.qname)
      JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
      JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
      JOIN alf_locale loc ON (ma.mkey = loc.locale_str)
      JOIN alf_attributes a2 ON (ma.attribute_id = a2.id)
   WHERE
      np.node_id >= ${LOWERBOUND} AND np.node_id <= ${UPPERBOUND}
;  -- (OPTIONAL)
--FOREACH t_alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
UPDATE t_alf_node_properties
   SET actual_type_n = 6, persisted_type_n = 6, serializable_value = NULL
   WHERE actual_type_n = -1 AND string_value IS NOT NULL
   AND t_alf_node_properties.node_id >= ${LOWERBOUND} AND t_alf_node_properties.node_id <= ${UPPERBOUND}
;
--FOREACH t_alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
UPDATE t_alf_node_properties
   SET actual_type_n = 9, persisted_type_n = 9
   WHERE actual_type_n = -1 AND serializable_value IS NOT NULL
   AND t_alf_node_properties.node_id >= ${LOWERBOUND} AND t_alf_node_properties.node_id <= ${UPPERBOUND}
;
--FOREACH t_alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
DELETE FROM t_alf_node_properties
   WHERE actual_type_n = -1
   AND t_alf_node_properties.node_id >= ${LOWERBOUND} AND t_alf_node_properties.node_id <= ${UPPERBOUND}
;

-- Delete the node properties and move the fixed values over
DROP TABLE alf_node_properties;
ALTER TABLE t_alf_node_properties RENAME TO alf_node_properties;

CREATE TABLE t_del_attributes
(
   id BIGINT NOT NULL,
   PRIMARY KEY (id)
);

--FOREACH alf_attributes.id system.upgrade.t_del_attributes.batchsize
INSERT INTO t_del_attributes
   SELECT id FROM alf_attributes WHERE type = 'M'
   AND alf_attributes.id >= ${LOWERBOUND} AND alf_attributes.id <= ${UPPERBOUND}
;

--FOREACH t_del_attributes.id system.upgrade.t_del_attributes.batchsize
DELETE t_del_attributes
   FROM t_del_attributes
   JOIN alf_map_attribute_entries ma ON (ma.attribute_id = t_del_attributes.id)
   WHERE t_del_attributes.id >= ${LOWERBOUND} AND t_del_attributes.id <= ${UPPERBOUND}
;

--FOREACH t_del_attributes.id system.upgrade.t_del_attributes.batchsize
DELETE t_del_attributes
   FROM t_del_attributes
   JOIN alf_list_attribute_entries la ON (la.attribute_id = t_del_attributes.id)
   WHERE t_del_attributes.id >= ${LOWERBOUND} AND t_del_attributes.id <= ${UPPERBOUND}
;

--FOREACH t_del_attributes.id system.upgrade.t_del_attributes.batchsize
DELETE t_del_attributes
   FROM t_del_attributes
   JOIN alf_global_attributes ga ON (ga.attribute = t_del_attributes.id)
   WHERE t_del_attributes.id >= ${LOWERBOUND} AND t_del_attributes.id <= ${UPPERBOUND}
;

--FOREACH t_del_attributes.id system.upgrade.t_del_attributes.batchsize
INSERT INTO t_del_attributes
   SELECT a.id FROM t_del_attributes t
   JOIN alf_map_attribute_entries ma ON (ma.map_id = t.id)
   JOIN alf_attributes a ON (ma.attribute_id = a.id)
   WHERE t.id >= ${LOWERBOUND} AND t.id <= ${UPPERBOUND}
;

--FOREACH alf_map_attribute_entries.map_id system.upgrade.alf_map_attribute_entries.batchsize
DELETE alf_map_attribute_entries
   FROM alf_map_attribute_entries
   JOIN t_del_attributes t ON (alf_map_attribute_entries.map_id = t.id)
   WHERE alf_map_attribute_entries.map_id >= ${LOWERBOUND} AND alf_map_attribute_entries.map_id <= ${UPPERBOUND}
;

--FOREACH alf_list_attribute_entries.list_id system.upgrade.alf_list_attribute_entries.batchsize
DELETE alf_list_attribute_entries
   FROM alf_list_attribute_entries
   JOIN t_del_attributes t ON (alf_list_attribute_entries.list_id = t.id)
   WHERE alf_list_attribute_entries.list_id >= ${LOWERBOUND} AND alf_list_attribute_entries.list_id <= ${UPPERBOUND}
;

--FOREACH alf_attributes.id system.upgrade.alf_attributes.batchsize
DELETE alf_attributes
   FROM alf_attributes
   JOIN t_del_attributes t ON (alf_attributes.id = t.id)
   WHERE alf_attributes.id >= ${LOWERBOUND} AND alf_attributes.id <= ${UPPERBOUND}
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
ALTER TABLE alf_store DROP INDEX FKBD4FF53D22DBA5BA, DROP FOREIGN KEY FKBD4FF53D22DBA5BA;  -- (OPTIONAL)
ALTER TABLE alf_store DROP FOREIGN KEY alf_store_root;  -- (OPTIONAL)
DROP TABLE alf_node;
ALTER TABLE t_alf_node RENAME TO alf_node;
DROP TABLE alf_store;
ALTER TABLE t_alf_store RENAME TO alf_store;


-- -------------------------------------
-- Modify index and constraint names --
-- -------------------------------------
ALTER TABLE alf_attributes DROP INDEX fk_attributes_n_acl, DROP FOREIGN KEY fk_attributes_n_acl;  -- (optional)
ALTER TABLE alf_attributes DROP INDEX fk_attr_n_acl, DROP FOREIGN KEY fk_attr_n_acl;  -- (optional)
ALTER TABLE alf_attributes
   ADD INDEX fk_alf_attr_acl (acl_id)
;

ALTER TABLE alf_global_attributes DROP FOREIGN KEY FK64D0B9CF69B9F16A; -- (optional)
ALTER TABLE alf_global_attributes DROP INDEX FK64D0B9CF69B9F16A; -- (optional)
-- alf_global_attributes.attribute is declared unique.  Indexes may automatically have been created.
ALTER TABLE alf_global_attributes
   ADD INDEX fk_alf_gatt_att (attribute);  -- (optional)
ALTER TABLE alf_global_attributes
   ADD CONSTRAINT fk_alf_gatt_att FOREIGN KEY (attribute) REFERENCES alf_attributes (id)
;
   
ALTER TABLE alf_list_attribute_entries DROP INDEX FKC7D52FB02C5AB86C, DROP FOREIGN KEY FKC7D52FB02C5AB86C; -- (optional)
ALTER TABLE alf_list_attribute_entries DROP INDEX FKC7D52FB0ACD8822C, DROP FOREIGN KEY FKC7D52FB0ACD8822C; -- (optional)
ALTER TABLE alf_list_attribute_entries
   ADD INDEX fk_alf_lent_att (attribute_id),
   ADD CONSTRAINT fk_alf_lent_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id),
   ADD INDEX fk_alf_lent_latt (list_id),
   ADD CONSTRAINT fk_alf_lent_latt FOREIGN KEY (list_id) REFERENCES alf_attributes (id)
;

ALTER TABLE alf_map_attribute_entries DROP INDEX FK335CAE26AEAC208C, DROP FOREIGN KEY FK335CAE26AEAC208C; -- (optional)
ALTER TABLE alf_map_attribute_entries DROP INDEX FK335CAE262C5AB86C, DROP FOREIGN KEY FK335CAE262C5AB86C; -- (optional)
ALTER TABLE alf_map_attribute_entries
   ADD INDEX fk_alf_matt_matt (map_id),
   ADD CONSTRAINT fk_alf_matt_matt FOREIGN KEY (map_id) REFERENCES alf_attributes (id),
   ADD INDEX fk_alf_matt_att (attribute_id),
   ADD CONSTRAINT fk_alf_matt_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id)
;

ALTER TABLE alf_transaction DROP INDEX idx_commit_time_ms; -- (optional)
ALTER TABLE alf_transaction
   ADD COLUMN commit_time_ms BIGINT NULL
; -- (optional)
ALTER TABLE alf_transaction
   DROP INDEX FKB8761A3A9AE340B7, DROP FOREIGN KEY FKB8761A3A9AE340B7,
   ADD INDEX fk_alf_txn_svr (server_id),
   ADD CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES alf_server (id),
   ADD INDEX idx_alf_txn_ctms (commit_time_ms)
;

--FOREACH alf_transaction.id system.upgrade.alf_transaction.batchsize
UPDATE alf_transaction SET commit_time_ms = id WHERE commit_time_ms IS NULL
AND alf_transaction.id >= ${LOWERBOUND} AND alf_transaction.id <= ${UPPERBOUND};

ALTER TABLE avm_child_entries DROP INDEX fk_avm_ce_child, DROP FOREIGN KEY fk_avm_ce_child; -- (optional)
ALTER TABLE avm_child_entries DROP INDEX fk_avm_ce_parent, DROP FOREIGN KEY fk_avm_ce_parent; -- (optional)
ALTER TABLE avm_child_entries
   ADD INDEX fk_avm_ce_child (child_id),
   ADD CONSTRAINT fk_avm_ce_child FOREIGN KEY (child_id) REFERENCES avm_nodes (id),
   ADD INDEX fk_avm_ce_parent (parent_id),
   ADD CONSTRAINT fk_avm_ce_parent FOREIGN KEY (parent_id) REFERENCES avm_nodes (id)
;

ALTER TABLE avm_history_links DROP INDEX fk_avm_hl_desc, DROP FOREIGN KEY fk_avm_hl_desc; -- (optional)
ALTER TABLE avm_history_links DROP INDEX fk_avm_hl_ancestor, DROP FOREIGN KEY fk_avm_hl_ancestor; -- (optional)
ALTER TABLE avm_history_links DROP INDEX idx_avm_hl_revpk; -- (optional)
ALTER TABLE avm_history_links
   ADD INDEX fk_avm_hl_desc (descendent),
   ADD CONSTRAINT fk_avm_hl_desc FOREIGN KEY (descendent) REFERENCES avm_nodes (id),
   ADD INDEX fk_avm_hl_ancestor (ancestor),
   ADD CONSTRAINT fk_avm_hl_ancestor FOREIGN KEY (ancestor) REFERENCES avm_nodes (id),
   ADD INDEX idx_avm_hl_revpk (descendent, ancestor)
;

ALTER TABLE avm_merge_links DROP INDEX fk_avm_ml_to, DROP FOREIGN KEY fk_avm_ml_to; -- (optional)
ALTER TABLE avm_merge_links DROP INDEX fk_avm_ml_from, DROP FOREIGN KEY fk_avm_ml_from; -- (optional)
ALTER TABLE avm_merge_links
   ADD INDEX fk_avm_ml_to (mto),
   ADD CONSTRAINT fk_avm_ml_to FOREIGN KEY (mto) REFERENCES avm_nodes (id),
   ADD INDEX fk_avm_ml_from (mfrom),
   ADD CONSTRAINT fk_avm_ml_from FOREIGN KEY (mfrom) REFERENCES avm_nodes (id)
;

ALTER TABLE avm_nodes DROP INDEX fk_avm_n_acl, DROP FOREIGN KEY fk_avm_n_acl; -- (optional)
ALTER TABLE avm_nodes DROP INDEX fk_avm_n_store, DROP FOREIGN KEY fk_avm_n_store; -- (optional)
ALTER TABLE avm_nodes DROP INDEX idx_avm_n_pi; -- (optional)
ALTER TABLE avm_nodes
   ADD INDEX fk_avm_n_acl (acl_id),
   ADD CONSTRAINT fk_avm_n_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
   ADD INDEX fk_avm_n_store (store_new_id),
   ADD CONSTRAINT fk_avm_n_store FOREIGN KEY (store_new_id) REFERENCES avm_stores (id),
   ADD INDEX idx_avm_n_pi (primary_indirection)
;

ALTER TABLE avm_stores DROP INDEX fk_avm_s_root, DROP FOREIGN KEY fk_avm_s_root; -- (optional)
ALTER TABLE avm_stores
   ADD INDEX fk_avm_s_acl (acl_id),
   ADD CONSTRAINT fk_avm_s_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
   ADD INDEX fk_avm_s_root (current_root_id),
   ADD CONSTRAINT fk_avm_s_root FOREIGN KEY (current_root_id) REFERENCES avm_nodes (id)
;

ALTER TABLE avm_version_layered_node_entry DROP INDEX FK182E672DEB9D70C, DROP FOREIGN KEY FK182E672DEB9D70C; -- (optional)
ALTER TABLE avm_version_layered_node_entry
   ADD INDEX fk_avm_vlne_vr (version_root_id),
   ADD CONSTRAINT fk_avm_vlne_vr FOREIGN KEY (version_root_id) REFERENCES avm_version_roots (id)
;

ALTER TABLE avm_version_roots DROP INDEX idx_avm_vr_version; -- (optional)
ALTER TABLE avm_version_roots DROP INDEX idx_avm_vr_revuq; -- (optional)
ALTER TABLE avm_version_roots DROP INDEX fk_avm_vr_root, DROP FOREIGN KEY fk_avm_vr_root; -- (optional)
ALTER TABLE avm_version_roots DROP INDEX fk_avm_vr_store, DROP FOREIGN KEY fk_avm_vr_store; -- (optional)
ALTER TABLE avm_version_roots
   ADD INDEX idx_avm_vr_version (version_id),
   ADD INDEX idx_avm_vr_revuq (avm_store_id, version_id),
   ADD INDEX fk_avm_vr_root (root_id),
   ADD CONSTRAINT fk_avm_vr_root FOREIGN KEY (root_id) REFERENCES avm_nodes (id),
   ADD INDEX fk_avm_vr_store (avm_store_id),
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
    0, 85, -1, 91, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
