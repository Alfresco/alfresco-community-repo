--
-- Title:      Apply all DM schema modifications 
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

---------------------------------
-- Build Namespaces and QNames --
---------------------------------

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

-- Create temporary table for dynamic (child) QNames
CREATE TABLE t_qnames_dyn
(
   qname VARCHAR(100) NOT NULL,
   namespace VARCHAR(100),
   namespace_id BIGINT,
   local_name VARCHAR(100),
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
UPDATE t_qnames_dyn SET local_name = SUBSTRING_INDEX(qname, '}', -1);

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

-- Create temporary table to hold static QNames
CREATE TABLE t_qnames
(
   qname VARCHAR(200) NOT NULL,
   namespace VARCHAR(100),
   localname VARCHAR(100),
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
UPDATE t_qnames SET localname = SUBSTRING_INDEX(qname, '}', -1);

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

------------------------------
-- Populate the Permissions --
------------------------------

-- This is a small table so we change it in place
ALTER TABLE alf_permission DROP INDEX type_qname;
ALTER TABLE alf_permission ADD COLUMN type_qname_id BIGINT NULL AFTER id;
UPDATE alf_permission p SET p.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', SUBSTR(ns.uri, 8), '}', q.local_name) = p.type_qname
);
ALTER TABLE alf_permission DROP COLUMN type_qname;
ALTER TABLE alf_permission MODIFY COLUMN type_qname_id BIGINT NOT NULL AFTER id;
ALTER TABLE alf_permission ADD UNIQUE (type_qname_id, name);

---------------------
-- Build new Store --
---------------------

CREATE TABLE t_alf_store
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   protocol VARCHAR(50) NOT NULL,
   identifier VARCHAR(100) NOT NULL,
   root_node_id BIGINT,
   PRIMARY KEY (id),
   UNIQUE (protocol, identifier)
) TYPE=InnoDB;

CREATE TABLE t_alf_node (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   store_id BIGINT NOT NULL,
   uuid VARCHAR(36) NOT NULL,
   transaction_id BIGINT NOT NULL,
   node_deleted bit NOT NULL,
   type_qname_id BIGINT NOT NULL,
   acl_id BIGINT,
   audit_creator VARCHAR(255) NOT NULL,
   audit_created VARCHAR(30) NOT NULL,
   audit_modifier VARCHAR(255) NOT NULL,
   audit_modified VARCHAR(30) NOT NULL,
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
) TYPE=InnoDB;

-- Fill the store table
INSERT INTO t_alf_store (version, protocol, identifier, root_node_id)
   SELECT 1, protocol, identifier, root_node_id FROM alf_store
;

----------------------------
-- Populate the new nodes --
----------------------------

-- Query OK, 830222 rows affected (2 min 18.96 sec)	
INSERT INTO t_alf_node
   (
      id, version, store_id, uuid, transaction_id, node_deleted, type_qname_id,
      audit_creator, audit_created, audit_modifier, audit_modified
   )
   SELECT
      n.id, 1, s.id, n.uuid, nstat.transaction_id, false, q.qname_id,
      'unknown', '2008-09-17T02:23:37.212+01:00', 'unkown', '2008-09-17T02:23:37.212+01:00'
   FROM
      t_qnames q
      JOIN alf_node n ON (q.qname = n.type_qname)
      JOIN alf_node_status nstat ON (nstat.node_id = n.id)
      JOIN t_alf_store s ON (s.protocol = nstat.protocol AND s.identifier = nstat.identifier)
;

-- Hook the store up to the root node
ALTER TABLE t_alf_store 
   ADD INDEX fk_alf_store_root (root_node_id), 
   ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES t_alf_node (id)
;

-------------------------------
-- Populate the Child Assocs --
-------------------------------

CREATE TABLE t_alf_child_assoc
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   parent_node_id BIGINT NOT NULL,
   type_qname_id BIGINT NOT NULL,
   child_node_name VARCHAR(50) NOT NULL,
   child_node_name_crc BIGINT NOT NULL,
   child_node_id BIGINT NOT NULL,
   qname_ns_id BIGINT NOT NULL,
   qname_localname VARCHAR(100) NOT NULL,
   is_primary BIT,
   assoc_index INTEGER,
   INDEX idx_alf_cass_qnln (qname_localname),
   INDEX fk_alf_cass_pnode (parent_node_id),
   INDEX fk_alf_cass_cnode (child_node_id),
   INDEX fk_alf_cass_tqn (type_qname_id),
   INDEX fk_alf_cass_qnns (qname_ns_id),
   CONSTRAINT fk_alf_cass_pnode foreign key (parent_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_cass_cnode foreign key (child_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_cass_tqn foreign key (type_qname_id) REFERENCES alf_qname (id),
   CONSTRAINT fk_alf_cass_qnns foreign key (qname_ns_id) REFERENCES alf_namespace (id),
   PRIMARY KEY (id),
   UNIQUE (parent_node_id, type_qname_id, child_node_name, child_node_name_crc)
) TYPE=InnoDB;

-- Query OK, 830217 rows affected (11 min 59.10 sec)
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

------------------------------
-- Populate the Node Assocs --
------------------------------

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
) TYPE=InnoDB;

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

-------------------------------
-- Populate the Node Aspects --
-------------------------------

CREATE TABLE t_alf_node_aspects
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL,
   INDEX fk_alf_nasp_n (node_id),
   INDEX fk_alf_nasp_qn (qname_id),
   CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
) TYPE=InnoDB;

-- Note the omission of sys:referencable and cm:auditable.  These are implicit.
-- Query OK, 415051 rows affected (17.59 sec)
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
         '{http://www.alfresco.org/model/system/1.0}referenceable',
         '{http://www.alfresco.org/model/content/1.0}auditable'
      )
;

-- Clean up
DROP TABLE alf_node_aspects;
ALTER TABLE t_alf_node_aspects RENAME TO alf_node_aspects;

-----------------------------------
-- Populate the AVM Node Aspects --
-----------------------------------

CREATE TABLE t_avm_aspects
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL,
   INDEX fk_avm_nasp_n (node_id),
   INDEX fk_avm_nasp_qn (qname_id),
   CONSTRAINT fk_avm_nasp_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
) TYPE=InnoDB;

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

------------------------------------
-- Migrate Sundry Property Tables --
------------------------------------

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

-- Modify the avm_node_properties_new table
CREATE TABLE t_avm_node_properties_new
(
   node_id BIGINT NOT NULL,
   actual_type_n INTEGER NOT NULL,
   persisted_type_n INTEGER NOT NULL,
   multi_valued BIT NOT NULL,
   boolean_value BIT,
   long_value BIGINT,
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value TEXT,
   serializable_value BLOB,
   qname_id BIGINT NOT NULL,
   INDEX fk_avm_nprop_n (node_id),
   INDEX fk_avm_nprop_qn (qname_id),
   CONSTRAINT fk_avm_nprop_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
) TYPE=InnoDB;
INSERT INTO t_avm_node_properties_new
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
;
DROP TABLE avm_node_properties_new;
ALTER TABLE t_avm_node_properties_new RENAME TO avm_node_properties_new;

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
) TYPE=InnoDB;
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
;
DROP TABLE avm_store_properties;
ALTER TABLE t_avm_store_properties RENAME TO avm_store_properties;

-- Modify the avm_node_properties table
-- This table is old, so the data will be extracte and it will be replaced
CREATE TABLE t_avm_node_properties
(
   node_id BIGINT NOT NULL,
   actual_type_n INTEGER NOT NULL,
   persisted_type_n INTEGER NOT NULL,
   multi_valued BIT NOT NULL,
   boolean_value BIT,
   long_value BIGINT,
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value TEXT,
   serializable_value BLOB,
   qname_id BIGINT NOT NULL,
   PRIMARY KEY (node_id, qname_id)
) TYPE=InnoDB;
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
;
-- Copy values to new table.  Duplicates are avoided just in case.
INSERT INTO avm_node_properties_new
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
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      t_avm_node_properties p
      LEFT OUTER JOIN avm_node_properties_new pnew ON (pnew.node_id = p.node_id AND pnew.qname_id = p.qname_id)
   WHERE
      pnew.qname_id is null
;
DROP TABLE t_avm_node_properties;
DROP TABLE avm_node_properties;
ALTER TABLE avm_node_properties_new RENAME TO avm_node_properties;


-------------------
-- Build Locales --
-------------------

CREATE TABLE alf_locale
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL DEFAULT 1,
   locale_str VARCHAR(20) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (locale_str)
) TYPE=InnoDB;

INSERT INTO alf_locale (id, locale_str) VALUES (1, '.default');

-- Locales come from the attribute table which was used to support MLText persistence
-- Query OK, 0 rows affected (17.22 sec)
INSERT INTO alf_locale (locale_str)
   SELECT DISTINCT(ma.mkey)
      FROM alf_node_properties np
      JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
      JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
;

---------------------------------
-- Migrate ADM Property Tables --
---------------------------------

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
) TYPE=InnoDB;

-- Copy all simple values over
-- Query OK, 2905008 rows affected (7 min 11.49 sec)
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
      np.serializable_value
   FROM
      alf_node_properties np
      JOIN t_qnames tqn ON (np.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = np.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = np.persisted_type)
   WHERE
      np.attribute_value is null AND
      tqn.qname NOT IN
      (
         '{http://www.alfresco.org/model/content/1.0}created',
         '{http://www.alfresco.org/model/content/1.0}creator',
         '{http://www.alfresco.org/model/content/1.0}modified',
         '{http://www.alfresco.org/model/content/1.0}modifier'
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
   id BIGINT NOT NULL,
   PRIMARY KEY (id)
);
INSERT INTO t_del_attributes
   SELECT id FROM alf_attributes WHERE type = 'M'
;
DELETE t_del_attributes
   FROM t_del_attributes
   JOIN alf_map_attribute_entries ma ON (ma.attribute_id = t_del_attributes.id)
;
DELETE t_del_attributes
   FROM t_del_attributes
   JOIN alf_list_attribute_entries la ON (la.attribute_id = t_del_attributes.id)
;
DELETE t_del_attributes
   FROM t_del_attributes
   JOIN alf_global_attributes ga ON (ga.attribute = t_del_attributes.id)
;
INSERT INTO t_del_attributes
   SELECT a.id FROM t_del_attributes t
   JOIN alf_map_attribute_entries ma ON (ma.map_id = t.id)
   JOIN alf_attributes a ON (ma.attribute_id = a.id)
;
DELETE alf_map_attribute_entries
   FROM alf_map_attribute_entries
   JOIN t_del_attributes t ON (alf_map_attribute_entries.map_id = t.id)
;
DELETE alf_attributes
   FROM alf_attributes
   JOIN t_del_attributes t ON (alf_attributes.id = t.id)
;
DROP TABLE t_del_attributes;

--------------------
-- Final clean up --
--------------------
DROP TABLE t_qnames;
DROP TABLE t_prop_types;
DROP TABLE alf_node_status;
ALTER TABLE alf_store DROP INDEX FKBD4FF53D22DBA5BA, DROP FOREIGN KEY FKBD4FF53D22DBA5BA;  -- (OPTIONAL)
ALTER TABLE alf_store DROP FOREIGN KEY alf_store_root;  -- (OPTIONAL)
DROP TABLE alf_node;
ALTER TABLE t_alf_node RENAME TO alf_node;
DROP TABLE alf_store;
ALTER TABLE t_alf_store RENAME TO alf_store;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-1-FullDmUpgrade';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-1-FullDmUpgrade', 'Manually executed script upgrade V2.2: ADM ',
    0, 85, -1, 91, null, 'UNKOWN', 1, 1, 'Script completed'
  );
