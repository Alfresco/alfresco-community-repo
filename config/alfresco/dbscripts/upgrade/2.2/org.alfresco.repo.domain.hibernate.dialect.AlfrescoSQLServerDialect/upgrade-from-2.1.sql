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
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL,
   uri nvarchar(100) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (uri)
);

CREATE TABLE alf_qname
(
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL,
   ns_id numeric(19,0) NOT NULL,
   local_name nvarchar(200) NOT NULL,
   CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id),
   PRIMARY KEY (id),
   UNIQUE (ns_id, local_name)
);
CREATE INDEX fk_alf_qname_ns ON alf_qname (ns_id);

-- Create temporary table for dynamic (child) QNames
CREATE TABLE t_qnames_dyn
(
   qname nvarchar(255) NOT NULL,
   namespace nvarchar(100) null,
   namespace_id numeric(19,0) null,
   local_name nvarchar(200) null
);
CREATE INDEX tidx_qnd_ns ON t_qnames_dyn (namespace);
CREATE INDEX tidx_qnd_qn ON t_qnames_dyn (qname);

-- Populate the table with the child association paths
INSERT INTO t_qnames_dyn (qname)
(
   SELECT distinct(qname) FROM alf_child_assoc
);

-- Extract the Namespace
UPDATE t_qnames_dyn SET namespace = 'FILLER-' + SUBSTRING(qname,2,CHARINDEX('}',qname,1)-2);
-- Extract the Localname
UPDATE t_qnames_dyn SET local_name = SUBSTRING(qname,LEN(qname)+2-CHARINDEX('}',REVERSE(qname),1),LEN(qname));

-- Move the namespaces to the their new home
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
UPDATE t_qnames_dyn SET namespace_id = (SELECT ns.id FROM alf_namespace ns WHERE ns.uri = t_qnames_dyn.namespace);

-- Recoup some storage
DROP INDEX t_qnames_dyn.tidx_qnd_ns;
ALTER TABLE t_qnames_dyn DROP COLUMN namespace;

-- Create temporary table to hold static QNames
CREATE TABLE t_qnames
(
   qname nvarchar(200) NOT NULL,
   namespace nvarchar(100) null,
   localname nvarchar(100) null,
   qname_id numeric(19,0) null
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
UPDATE t_qnames SET namespace = 'FILLER-' + SUBSTRING(qname,2,CHARINDEX('}',qname,1)-2);
UPDATE t_qnames SET localname = SUBSTRING(qname,LEN(qname)+2-CHARINDEX('}',REVERSE(qname),1),LEN(qname));

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
UPDATE t_qnames SET qname_id =
(
   SELECT q.id FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE ns.uri = t_qnames.namespace AND q.local_name = t_qnames.localname
);

-- ----------------------------
-- Populate the Permissions --
-- ----------------------------

-- Rebuild the alf_permission table
CREATE TABLE t_alf_permission
(
   id numeric(19,0) identity not null,
   type_qname_id numeric(19,0) not null,
   version numeric(19,0) not null,
   name nvarchar(100) not null,
   UNIQUE (type_qname_id, name),
   CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),   
   PRIMARY KEY (id),
);
CREATE INDEX fk_alf_perm_tqn ON t_alf_permission (type_qname_id);

INSERT INTO t_alf_permission (type_qname_id, version, name)
(
   SELECT q.id, p.version, p.name
   FROM alf_qname q, alf_permission p, alf_namespace ns
   WHERE '{' + SUBSTRING(ns.uri, 8, LEN(ns.uri)) + '}' + q.local_name = p.type_qname
   AND q.ns_id = ns.id
);

ALTER TABLE alf_access_control_entry DROP CONSTRAINT fk_alf_ace_perm;  -- (optional)
DROP TABLE alf_permission;
EXEC sp_rename 't_alf_permission', 'alf_permission';
ALTER TABLE alf_access_control_entry
  ADD CONSTRAINT fk_alf_ace_perm FOREIGN KEY (permission_id) REFERENCES alf_permission;
  
-- -------------------
-- Build new Store --
-- -------------------

CREATE TABLE t_alf_store
(
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL,
   protocol nvarchar(50) NOT NULL,
   identifier nvarchar(100) NOT NULL,
   root_node_id numeric(19,0) null,
   PRIMARY KEY (id),
   UNIQUE (protocol, identifier)
);

-- --------------------------
-- Populate the ADM nodes --
-- --------------------------

CREATE TABLE t_alf_node (
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL,
   store_id numeric(19,0) NOT NULL,
   uuid nvarchar(36) NOT NULL,
   transaction_id numeric(19,0) NOT NULL,
   node_deleted tinyint NOT NULL,
   type_qname_id numeric(19,0) NOT NULL,
   acl_id numeric(19,0) null,
   audit_creator nvarchar(255) null,
   audit_created nvarchar(30) null,
   audit_modifier nvarchar(255) null,
   audit_modified nvarchar(30) null,
   audit_accessed nvarchar(30) null,
   CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id), 
   CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id), 
   CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id), 
   CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES t_alf_store (id), 
   PRIMARY KEY (id),
   UNIQUE (store_id, uuid)
);
CREATE INDEX idx_alf_node_del ON t_alf_node (node_deleted);
CREATE INDEX fk_alf_node_acl ON t_alf_node (acl_id);
CREATE INDEX fk_alf_node_tqn ON t_alf_node (type_qname_id);
CREATE INDEX fk_alf_node_txn ON t_alf_node (transaction_id);
CREATE INDEX fk_alf_node_store ON t_alf_node (store_id);

-- Fill the store table
INSERT INTO t_alf_store (version, protocol, identifier, root_node_id)
   SELECT 1, protocol, identifier, root_node_id FROM alf_store
;

-- Add type_qname index for nodes
CREATE INDEX tidx_node_tqn ON alf_node (type_qname);

-- Copy data over
SET IDENTITY_INSERT t_alf_node ON;
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
SET IDENTITY_INSERT t_alf_node OFF;

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
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL,
   store_id numeric(19,0) NOT NULL UNIQUE,
   version_count int NOT NULL,
   CONSTRAINT fk_alf_vc_store FOREIGN KEY (store_id) REFERENCES t_alf_store (id),
   PRIMARY KEY (id)
);

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
EXEC sp_rename 't_alf_version_count', 'alf_version_count';

-- -----------------------------
-- Populate the Child Assocs --
-- -----------------------------

CREATE TABLE t_alf_child_assoc
(
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL,
   parent_node_id numeric(19,0) NOT NULL,
   type_qname_id numeric(19,0) NOT NULL,
   child_node_name nvarchar(50) NOT NULL,
   child_node_name_crc numeric(19,0) NOT NULL,
   child_node_id numeric(19,0) NOT NULL,
   qname_ns_id numeric(19,0) NOT NULL,
   qname_localname nvarchar(100) NOT NULL,
   is_primary tinyint null,
   assoc_index int null,
   CONSTRAINT fk_alf_cass_pnode foreign key (parent_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_cass_cnode foreign key (child_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_cass_tqn foreign key (type_qname_id) REFERENCES alf_qname (id),
   CONSTRAINT fk_alf_cass_qnns foreign key (qname_ns_id) REFERENCES alf_namespace (id),
   PRIMARY KEY (id),
   UNIQUE (parent_node_id, type_qname_id, child_node_name, child_node_name_crc)
);
CREATE INDEX idx_alf_cass_qnln ON t_alf_child_assoc (qname_localname);
CREATE INDEX fk_alf_cass_pnode ON t_alf_child_assoc (parent_node_id);
CREATE INDEX fk_alf_cass_cnode ON t_alf_child_assoc (child_node_id);
CREATE INDEX fk_alf_cass_tqn ON t_alf_child_assoc (type_qname_id);
CREATE INDEX fk_alf_cass_qnns ON t_alf_child_assoc (qname_ns_id);

SET IDENTITY_INSERT t_alf_child_assoc ON;
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
SET IDENTITY_INSERT t_alf_child_assoc OFF;

-- Clean up
DROP TABLE t_qnames_dyn;
DROP TABLE alf_child_assoc;
EXEC sp_rename 't_alf_child_assoc', 'alf_child_assoc';

-- ----------------------------
-- Populate the Node Assocs --
-- ----------------------------

CREATE TABLE t_alf_node_assoc
(
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL, 
   source_node_id numeric(19,0) NOT NULL,
   target_node_id numeric(19,0) NOT NULL,
   type_qname_id numeric(19,0) NOT NULL,
   CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (id),
   UNIQUE (source_node_id, target_node_id, type_qname_id)
);
CREATE INDEX fk_alf_nass_snode ON t_alf_node_assoc (source_node_id);
CREATE INDEX fk_alf_nass_tnode ON t_alf_node_assoc (target_node_id);
CREATE INDEX fk_alf_nass_tqn ON t_alf_node_assoc (type_qname_id);

SET IDENTITY_INSERT t_alf_node_assoc ON;
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
SET IDENTITY_INSERT t_alf_node_assoc OFF;

-- Clean up
DROP TABLE alf_node_assoc;
EXEC sp_rename 't_alf_node_assoc', 'alf_node_assoc';

-- ----------------------------
-- Populate the Usage Deltas --
-- ----------------------------

CREATE TABLE t_alf_usage_delta
(
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL, 
   node_id numeric(19,0) NOT NULL,
   delta_size numeric(19,0) NOT NULL,
   CONSTRAINT fk_alf_usaged_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id),
   PRIMARY KEY (id)
);
CREATE INDEX fk_alf_usaged_n ON t_alf_usage_delta (node_id);

SET IDENTITY_INSERT t_alf_usage_delta ON;
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
SET IDENTITY_INSERT t_alf_usage_delta OFF;

-- Clean up
DROP TABLE alf_usage_delta;                                -- (optional)
EXEC sp_rename 't_alf_usage_delta', 'alf_usage_delta';

-- -----------------------------
-- Populate the Node Aspects --
-- -----------------------------

CREATE TABLE t_alf_node_aspects
(
   node_id numeric(19,0) NOT NULL,
   qname_id numeric(19,0) NOT NULL,
   CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
);
CREATE INDEX fk_alf_nasp_n ON t_alf_node_aspects (node_id);
CREATE INDEX fk_alf_nasp_qn ON t_alf_node_aspects (qname_id);

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
EXEC sp_rename 't_alf_node_aspects', 'alf_node_aspects';

-- ---------------------------------
-- Populate the AVM Node Aspects --
-- ---------------------------------

CREATE TABLE t_avm_aspects
(
   node_id numeric(19,0) NOT NULL,
   qname_id numeric(19,0) NOT NULL,
   CONSTRAINT fk_avm_nasp_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
);
CREATE INDEX fk_avm_nasp_n ON t_avm_aspects (node_id);
CREATE INDEX fk_avm_nasp_qn ON t_avm_aspects (qname_id);

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
EXEC sp_rename 't_avm_aspects', 'avm_aspects';

-- ----------------------------------
-- Migrate Sundry Property Tables --
-- ----------------------------------

-- Create temporary mapping for property types
CREATE TABLE t_prop_types
(
   type_name nvarchar(15) NOT NULL,
   type_id int NOT NULL,
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
   id numeric(19,0) identity not null,
   avm_store_id numeric(19,0),
   qname_id numeric(19,0) NOT NULL,
   actual_type_n int NOT NULL,
   persisted_type_n int NOT NULL,
   multi_valued tinyint NOT NULL,
   boolean_value tinyint null,
   long_value numeric(19,0) null,
   float_value float null,
   double_value DOUBLE PRECISION null,
   string_value nvarchar(1024) null,
   serializable_value image null,
   CONSTRAINT fk_avm_sprop_store FOREIGN KEY (avm_store_id) REFERENCES avm_stores (id),
   CONSTRAINT fk_avm_sprop_qname FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (id)
);
CREATE INDEX fk_avm_sprop_store ON t_avm_store_properties (avm_store_id);
CREATE INDEX fk_avm_sprop_qname ON t_avm_store_properties (qname_id);

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
EXEC sp_rename 't_avm_store_properties', 'avm_store_properties';

-- Modify the avm_node_properties_new table
CREATE TABLE t_avm_node_properties
(
   node_id numeric(19,0) NOT NULL,
   actual_type_n int NOT NULL,
   persisted_type_n int NOT NULL,
   multi_valued tinyint NOT NULL,
   boolean_value tinyint null,
   long_value numeric(19,0) null,
   float_value FLOAT null,
   double_value DOUBLE PRECISION null,
   string_value nvarchar(1024) null,
   serializable_value image null,
   qname_id numeric(19,0) NOT NULL,
   CONSTRAINT fk_avm_nprop_n FOREIGN KEY (node_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   PRIMARY KEY (node_id, qname_id)
);
CREATE INDEX fk_avm_nprop_n ON t_avm_node_properties (node_id);
CREATE INDEX fk_avm_nprop_qn ON t_avm_node_properties (qname_id);
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
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
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
EXEC sp_rename 't_avm_node_properties', 'avm_node_properties';


-- -----------------
-- Build Locales --
-- -----------------

CREATE TABLE alf_locale
(
   id numeric(19,0) identity not null,
   version numeric(19,0) NOT NULL DEFAULT 1,
   locale_str nvarchar(20) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (locale_str)
);

SET IDENTITY_INSERT alf_locale ON;
INSERT INTO alf_locale (id, locale_str) VALUES (1, '.default');
SET IDENTITY_INSERT alf_locale OFF;

-- Locales come from the attribute table which was used to support MLText persistence
INSERT INTO alf_locale (locale_str)
   SELECT DISTINCT(ma.mkey)
      FROM alf_node_properties np
      JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
      JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
;

-- -------------------------------
-- Migrate ADM Property Tables --
-- -------------------------------

CREATE TABLE t_alf_node_properties
(
   node_id numeric(19,0) NOT NULL,
   qname_id numeric(19,0) NOT NULL,
   locale_id numeric(19,0) NOT NULL,
   list_index int NOT NULL,
   actual_type_n int NOT NULL,
   persisted_type_n int NOT NULL,
   boolean_value tinyint null,
   long_value numeric(19,0) null,
   float_value FLOAT null,
   double_value DOUBLE PRECISION null,
   string_value nvarchar(1024) null,
   serializable_value image null,
   CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES t_alf_node (id),
   CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id),
   CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id),
   PRIMARY KEY (node_id, qname_id, list_index, locale_id)
);
CREATE INDEX fk_alf_nprop_n ON t_alf_node_properties (node_id);
CREATE INDEX fk_alf_nprop_qn ON t_alf_node_properties (qname_id);
CREATE INDEX fk_alf_nprop_loc ON t_alf_node_properties (locale_id);

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
      np.serializable_value
   FROM
      alf_node_properties np
      JOIN t_qnames tqn ON (np.qname = tqn.qname)
      JOIN t_prop_types ptypes_actual ON (ptypes_actual.type_name = np.actual_type)
      JOIN t_prop_types ptypes_persisted ON (ptypes_persisted.type_name = np.persisted_type)
   WHERE
      np.attribute_value IS NULL
;
-- Update cm:auditable properties on the nodes
UPDATE t_alf_node SET audit_creator =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = t_alf_node.id AND
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'creator'
);
UPDATE t_alf_node SET audit_created =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = t_alf_node.id AND
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'created'
);
UPDATE t_alf_node SET audit_modifier =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = t_alf_node.id AND
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'modifier'
);
UPDATE t_alf_node SET audit_modified =
(
   SELECT
      string_value
   FROM
      t_alf_node_properties np
      JOIN alf_qname qn ON (np.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      np.node_id = t_alf_node.id AND
      ns.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'modified'
);
-- Remove the unused cm:auditable properties
DELETE t_alf_node_properties
   FROM t_alf_node_properties
   JOIN alf_qname ON (t_alf_node_properties.qname_id = alf_qname.id)
   JOIN alf_namespace ON (alf_qname.ns_id = alf_namespace.id)
   WHERE
      alf_namespace.uri = 'FILLER-http://www.alfresco.org/model/content/1.0' AND
      alf_qname.local_name IN ('creator', 'created', 'modifier', 'modified')
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
EXEC sp_rename 't_alf_node_properties', 'alf_node_properties';

CREATE TABLE t_del_attributes
(
   id numeric(19,0) NOT NULL,
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

-- ---------------------------------------------------
-- Remove the FILLER- values from the namespace uri --
-- ---------------------------------------------------
UPDATE alf_namespace SET uri = '.empty' WHERE uri = 'FILLER-';
UPDATE alf_namespace SET uri = SUBSTRING(uri, 8, LEN(uri)) WHERE uri LIKE 'FILLER-%';

-- ------------------
-- Final clean up --
-- ------------------
DROP TABLE t_qnames;
DROP TABLE t_prop_types;
DROP TABLE alf_node_status;
DROP INDEX alf_store.FKBD4FF53D22DBA5BA;  -- (OPTIONAL)
ALTER TABLE alf_store DROP CONSTRAINT FKBD4FF53D22DBA5BA;  -- (OPTIONAL)
ALTER TABLE alf_store DROP CONSTRAINT alf_store_root;  -- (OPTIONAL)
DROP TABLE alf_node;
EXEC sp_rename 't_alf_node', 'alf_node';
DROP TABLE alf_store;
EXEC sp_rename 't_alf_store', 'alf_store';


-- -------------------------------------
-- Modify index and constraint names --
-- -------------------------------------
DROP INDEX alf_attributes.fk_attributes_n_acl;  -- (optional)
DROP INDEX alf_attributes.fk_attr_n_acl;  -- (optional)
ALTER TABLE alf_attributes DROP CONSTRAINT fk_attributes_n_acl;  -- (optional)
CREATE INDEX fk_alf_attr_acl ON alf_attributes (acl_id);

DROP INDEX alf_audit_date.adt_woy_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_date_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_y_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_q_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_m_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_dow_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_doy_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_dom_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_hy_idx;  -- (optional)
DROP INDEX alf_audit_date.adt_wom_idx;  -- (optional)
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

DROP INDEX alf_audit_fact.adt_user_idx;  -- (optional)
DROP INDEX alf_audit_fact.adt_store_idx;  -- (optional)
DROP INDEX alf_audit_fact.FKEAD18174A0F9B8D9;
DROP INDEX alf_audit_fact.FKEAD1817484342E39
DROP INDEX alf_audit_fact.FKEAD18174F524CFD7
ALTER TABLE alf_audit_fact DROP
   CONSTRAINT FKEAD18174A0F9B8D9,
   CONSTRAINT FKEAD1817484342E39,
   CONSTRAINT FKEAD18174F524CFD7;

CREATE INDEX idx_alf_adtf_ref ON alf_audit_fact (store_protocol, store_id, node_uuid);
CREATE INDEX idx_alf_adtf_usr ON alf_audit_fact (user_id);
CREATE INDEX fk_alf_adtf_src ON alf_audit_fact (audit_source_id);
CREATE INDEX fk_alf_adtf_date ON alf_audit_fact (audit_date_id);
CREATE INDEX fk_alf_adtf_conf ON alf_audit_fact (audit_conf_id);
ALTER TABLE alf_audit_fact ADD
   CONSTRAINT fk_alf_adtf_src FOREIGN KEY (audit_source_id) REFERENCES alf_audit_source (id),
   CONSTRAINT fk_alf_adtf_date FOREIGN KEY (audit_date_id) REFERENCES alf_audit_date (id),
   CONSTRAINT fk_alf_adtf_conf FOREIGN KEY (audit_conf_id) REFERENCES alf_audit_config (id)
;

DROP INDEX alf_audit_source.app_source_app_idx;  -- (optional)
DROP INDEX alf_audit_source.app_source_ser_idx;  -- (optional)
DROP INDEX alf_audit_source.app_source_met_idx;  -- (optional)
CREATE INDEX idx_alf_adts_met ON alf_audit_source (method);
CREATE INDEX idx_alf_adts_ser ON alf_audit_source (service);
CREATE INDEX idx_alf_adts_app ON alf_audit_source (application);

ALTER TABLE alf_global_attributes DROP CONSTRAINT FK64D0B9CF69B9F16A; -- (optional)
DROP INDEX alf_global_attributes.FK64D0B9CF69B9F16A; -- (optional)
-- alf_global_attributes.attribute is declared UNIQUE.  Indexes may automatically have been created.
CREATE INDEX fk_alf_gatt_att ON alf_global_attributes (attribute);  -- (optional)
ALTER TABLE alf_global_attributes
   ADD CONSTRAINT fk_alf_gatt_att FOREIGN KEY (attribute) REFERENCES alf_attributes (id)
;

DROP INDEX alf_list_attribute_entries.FKC7D52FB02C5AB86C; -- (optional)
DROP INDEX alf_list_attribute_entries.FKC7D52FB0ACD8822C; -- (optional)
ALTER TABLE alf_list_attribute_entries DROP CONSTRAINT FKC7D52FB02C5AB86C; -- (optional)
ALTER TABLE alf_list_attribute_entries DROP CONSTRAINT FKC7D52FB0ACD8822C; -- (optional)
CREATE INDEX fk_alf_lent_att ON alf_list_attribute_entries (attribute_id);
CREATE INDEX fk_alf_lent_latt ON alf_list_attribute_entries (list_id);
ALTER TABLE alf_list_attribute_entries ADD
   CONSTRAINT fk_alf_lent_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id),
   CONSTRAINT fk_alf_lent_latt FOREIGN KEY (list_id) REFERENCES alf_attributes (id)
;

DROP INDEX alf_map_attribute_entries.FK335CAE26AEAC208C; -- (optional)
ALTER TABLE alf_map_attribute_entries DROP CONSTRAINT FK335CAE26AEAC208C; -- (optional)
DROP INDEX alf_map_attribute_entries.FK335CAE262C5AB86C; -- (optional)
ALTER TABLE alf_map_attribute_entries DROP CONSTRAINT FK335CAE262C5AB86C; -- (optional)
CREATE INDEX fk_alf_matt_matt ON alf_map_attribute_entries (map_id);
CREATE INDEX fk_alf_matt_att ON alf_map_attribute_entries (attribute_id);
ALTER TABLE alf_map_attribute_entries ADD
   CONSTRAINT fk_alf_matt_matt FOREIGN KEY (map_id) REFERENCES alf_attributes (id),
   CONSTRAINT fk_alf_matt_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id)
;

DROP INDEX alf_transaction.idx_commit_time_ms; -- (optional)
DROP INDEX alf_transaction.FKB8761A3A9AE340B7;
ALTER TABLE alf_transaction DROP CONSTRAINT FKB8761A3A9AE340B7;
CREATE INDEX fk_alf_txn_svr ON alf_transaction (server_id);
ALTER TABLE alf_transaction ADD CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES alf_server (id);
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms);

DROP INDEX avm_child_entries.fk_avm_ce_child; -- (optional)
ALTER TABLE avm_child_entries DROP CONSTRAINT fk_avm_ce_child; -- (optional)
DROP INDEX avm_child_entries.fk_avm_ce_parent; -- (optional)
ALTER TABLE avm_child_entries DROP CONSTRAINT fk_avm_ce_parent; -- (optional)
CREATE INDEX fk_avm_ce_child ON avm_child_entries (child_id);
CREATE INDEX fk_avm_ce_parent ON avm_child_entries (parent_id);
ALTER TABLE avm_child_entries ADD
   CONSTRAINT fk_avm_ce_child FOREIGN KEY (child_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_ce_parent FOREIGN KEY (parent_id) REFERENCES avm_nodes (id)
;

DROP INDEX avm_history_links.fk_avm_hl_desc; -- (optional)
ALTER TABLE avm_history_links DROP CONSTRAINT fk_avm_hl_desc; -- (optional)
DROP INDEX avm_history_links.fk_avm_hl_ancestor; -- (optional)
ALTER TABLE avm_history_links DROP CONSTRAINT fk_avm_hl_ancestor; -- (optional)
DROP INDEX avm_history_links.idx_avm_hl_revpk; -- (optional)
CREATE INDEX fk_avm_hl_desc ON avm_history_links (descendent);
CREATE INDEX fk_avm_hl_ancestor ON avm_history_links (ancestor);
CREATE INDEX idx_avm_hl_revpk ON avm_history_links (descendent, ancestor);
ALTER TABLE avm_history_links ADD
   CONSTRAINT fk_avm_hl_desc FOREIGN KEY (descendent) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_hl_ancestor FOREIGN KEY (ancestor) REFERENCES avm_nodes (id)
;

DROP INDEX avm_merge_links.fk_avm_ml_to; -- (optional)
ALTER TABLE avm_merge_links DROP CONSTRAINT fk_avm_ml_to; -- (optional)
DROP INDEX avm_merge_links.fk_avm_ml_from; -- (optional)
ALTER TABLE avm_merge_links DROP CONSTRAINT fk_avm_ml_from; -- (optional)
CREATE INDEX fk_avm_ml_to ON avm_merge_links (mto);
CREATE INDEX fk_avm_ml_from ON avm_merge_links (mfrom);
ALTER TABLE avm_merge_links ADD
   CONSTRAINT fk_avm_ml_to FOREIGN KEY (mto) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_ml_from FOREIGN KEY (mfrom) REFERENCES avm_nodes (id)
;

DROP INDEX avm_nodes.fk_avm_n_acl; -- (optional)
ALTER TABLE avm_nodes DROP CONSTRAINT fk_avm_n_acl; -- (optional)
DROP INDEX avm_nodes.fk_avm_n_store; -- (optional)
ALTER TABLE avm_nodes DROP CONSTRAINT fk_avm_n_store; -- (optional)
DROP INDEX avm_nodes.idx_avm_n_pi; -- (optional)
CREATE INDEX fk_avm_n_acl ON avm_nodes (acl_id);
CREATE INDEX fk_avm_n_store ON avm_nodes (store_new_id);
CREATE INDEX idx_avm_n_pi ON avm_nodes (primary_indirection);
ALTER TABLE avm_nodes ADD
   CONSTRAINT fk_avm_n_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
   CONSTRAINT fk_avm_n_store FOREIGN KEY (store_new_id) REFERENCES avm_stores (id)
;

DROP INDEX avm_stores.fk_avm_s_root; -- (optional)
ALTER TABLE avm_stores DROP CONSTRAINT fk_avm_s_root; -- (optional)
CREATE INDEX fk_avm_s_acl ON avm_stores (acl_id);
CREATE INDEX fk_avm_s_root ON avm_stores (current_root_id);
ALTER TABLE avm_stores ADD
   CONSTRAINT fk_avm_s_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
   CONSTRAINT fk_avm_s_root FOREIGN KEY (current_root_id) REFERENCES avm_nodes (id)
;

DROP INDEX avm_version_layered_node_entry.FK182E672DEB9D70C; -- (optional)
ALTER TABLE avm_version_layered_node_entry DROP CONSTRAINT FK182E672DEB9D70C; -- (optional)
CREATE INDEX fk_avm_vlne_vr ON avm_version_layered_node_entry (version_root_id);
ALTER TABLE avm_version_layered_node_entry
   ADD CONSTRAINT fk_avm_vlne_vr FOREIGN KEY (version_root_id) REFERENCES avm_version_roots (id)
;

DROP INDEX avm_version_roots.idx_avm_vr_version; -- (optional)
DROP INDEX avm_version_roots.idx_avm_vr_revuq; -- (optional)
DROP INDEX avm_version_roots.fk_avm_vr_root; -- (optional)
ALTER TABLE avm_version_roots DROP CONSTRAINT fk_avm_vr_root; -- (optional)
DROP INDEX avm_version_roots.fk_avm_vr_store; -- (optional)
ALTER TABLE avm_version_roots DROP CONSTRAINT fk_avm_vr_store; -- (optional)
CREATE INDEX idx_avm_vr_version ON avm_version_roots (version_id);
CREATE INDEX idx_avm_vr_revuq ON avm_version_roots (avm_store_id, version_id);
CREATE INDEX fk_avm_vr_root ON avm_version_roots (root_id);
CREATE INDEX fk_avm_vr_store ON avm_version_roots (avm_store_id);
ALTER TABLE avm_version_roots ADD
   CONSTRAINT fk_avm_vr_root FOREIGN KEY (root_id) REFERENCES avm_nodes (id),
   CONSTRAINT fk_avm_vr_store FOREIGN KEY (avm_store_id) REFERENCES avm_stores (id)
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
