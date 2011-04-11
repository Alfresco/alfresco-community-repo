--
-- Title:      Upgrade V2.2 SP1 or SP2 
-- Database:   MySQL
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
      n.id, 1, s.id, n.uuid, nstat.transaction_id, false, n.type_qname_id, n.acl_id,
      null, null, null, null, null
   FROM
      alf_node n
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
      ca.type_qname_id,
      ca.child_node_name_crc, ca.child_node_name,
      ca.child_node_id,
      ca.qname_ns_id, ca.qname_localname,
      ca.is_primary, ca.assoc_index
   FROM
      alf_child_assoc ca
   WHERE
      ca.id >= ${LOWERBOUND} AND ca.id <= ${UPPERBOUND}
;

-- Clean up
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
      na.type_qname_id
   FROM
      alf_node_assoc na
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
      qname_id
   FROM
      alf_node_aspects na
      JOIN alf_qname qn ON (na.qname_id = qn.id)
      JOIN alf_namespace ns ON (qn.ns_id = ns.id)
   WHERE
      (ns.uri != 'http://www.alfresco.org/model/system/1.0' OR
      qn.local_name != 'referenceable')
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

--FOREACH avm_aspects_new.id system.upgrade.t_avm_aspects.batchsize
INSERT INTO t_avm_aspects
   (
      node_id, qname_id
   )
   SELECT
      anew.id,
      anew.qname_id
   FROM
      avm_aspects_new anew
   WHERE
      anew.id >= ${LOWERBOUND} AND anew.id <= ${UPPERBOUND}
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
      p.qname_id,
      p.actual_type_n, p.persisted_type_n,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_store_properties p
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
      p.qname_id,
      p.actual_type_n, p.persisted_type_n,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_node_properties_new p
   WHERE
      p.node_id >= ${LOWERBOUND} AND p.node_id <= ${UPPERBOUND}   
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
   list_index INTEGER NOT NULL,
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
      np.node_id, np.qname_id, 1, -1,
      np.actual_type_n, np.persisted_type_n,
      np.boolean_value, np.long_value, np.float_value, np.double_value,
      np.string_value,
      np.serializable_value
   FROM
      alf_node_properties np
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'modified'
)
WHERE n.id >= ${LOWERBOUND} AND n.id <= ${UPPERBOUND};
-- Remove the unused cm:auditable properties
--FOREACH t_alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
DELETE t_alf_node_properties
   FROM t_alf_node_properties
   JOIN alf_qname ON (t_alf_node_properties.qname_id = alf_qname.id)
   JOIN alf_namespace ON (alf_qname.ns_id = alf_namespace.id)
   WHERE
      alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0' AND
      alf_qname.local_name IN ('creator', 'created', 'modifier', 'modified') AND
      t_alf_node_properties.node_id >= ${LOWERBOUND} AND t_alf_node_properties.node_id <= ${UPPERBOUND}
;

-- Copy all MLText values over
--FOREACH alf_node_properties.node_id system.upgrade.t_alf_node_properties.batchsize
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
      FALSE, 0, 0, 0,
      a2.string_value,
      a2.serializable_value
   FROM
      alf_node_properties np
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

-- ------------------
-- Final clean up --
-- ------------------
DROP TABLE alf_node_status;
ALTER TABLE alf_store DROP FOREIGN KEY fk_alf_store_rn;
DROP TABLE alf_node;
ALTER TABLE t_alf_node RENAME TO alf_node;
DROP TABLE alf_store;
ALTER TABLE t_alf_store RENAME TO alf_store;
CREATE INDEX idx_alf_auth_aut ON alf_authority (authority);  -- (optional)

-- ----------------
-- JBPM Differences
-- ----------------
ALTER TABLE jbpm_processinstance DROP INDEX key_;  -- (optional)
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
    86, 90, -1, 91, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
