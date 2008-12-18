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

-- Copy data over
SET IDENTITY_INSERT t_alf_node ON;
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
      ca.type_qname_id,
      ca.qname_ns_id, ca.qname_localname,
      ca.is_primary, ca.assoc_index
   FROM
      alf_child_assoc ca
;
SET IDENTITY_INSERT t_alf_child_assoc OFF;

-- Clean up
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
      na.type_qname_id
   FROM
      alf_node_assoc na
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
      anew.id,
      anew.qname_id
   FROM
      avm_aspects_new anew
;

-- Clean up
DROP TABLE avm_aspects;
DROP TABLE avm_aspects_new;
EXEC sp_rename 't_avm_aspects', 'avm_aspects';

-- ----------------------------------
-- Migrate Sundry Property Tables --
-- ----------------------------------

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
      p.qname_id,
      p.actual_type_n, p.persisted_type_n,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_store_properties p
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
      p.qname_id,
      p.actual_type_n, p.persisted_type_n,
      p.multi_valued, p.boolean_value, p.long_value, p.float_value, p.double_value, p.string_value, p.serializable_value
   FROM
      avm_node_properties_new p
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
      np.node_id, np.qname_id, -1, 1,
      np.actual_type_n, np.persisted_type_n,
      np.boolean_value, np.long_value, np.float_value, np.double_value,
      np.string_value,
      np.serializable_value
   FROM
      alf_node_properties np
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      ns.uri = 'http://www.alfresco.org/model/content/1.0' AND
      qn.local_name = 'modified'
);
-- Remove the unused cm:auditable properties
DELETE t_alf_node_properties
   FROM t_alf_node_properties
   JOIN alf_qname ON (t_alf_node_properties.qname_id = alf_qname.id)
   JOIN alf_namespace ON (alf_qname.ns_id = alf_namespace.id)
   WHERE
      alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0' AND
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
      np.node_id, np.qname_id, -1, loc.id,
      -1, 0,
      0, 0, 0, 0,
      a2.string_value,
      a2.serializable_value
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

-- ------------------
-- Final clean up --
-- ------------------
DROP TABLE alf_node_status;
ALTER TABLE alf_store DROP CONSTRAINT fk_alf_store_rn;
DROP TABLE alf_node;
EXEC sp_rename 't_alf_node', 'alf_node';
DROP TABLE alf_store;
EXEC sp_rename 't_alf_store', 'alf_store';

-- ----------------
-- JBPM Differences
-- ----------------

-- We need to remove a unique index and unfortunately we don't know its name so let's just rebuild the table!
create table T_JBPM_PROCESSINSTANCE (
    ID_ numeric(19,0) identity not null,
    VERSION_ int not null,
    KEY_ nvarchar(255) null,
    START_ datetime null,
    END_ datetime null,
    ISSUSPENDED_ tinyint null,
    PROCESSDEFINITION_ numeric(19,0) null,
    ROOTTOKEN_ numeric(19,0) null,
    SUPERPROCESSTOKEN_ numeric(19,0) null,
    primary key (ID_),
);
SET IDENTITY_INSERT T_JBPM_PROCESSINSTANCE ON;
INSERT INTO T_JBPM_PROCESSINSTANCE (ID_, VERSION_, KEY_, START_, END_, ISSUSPENDED_, PROCESSDEFINITION_, ROOTTOKEN_, SUPERPROCESSTOKEN_)
   SELECT ID_, VERSION_, KEY_, START_, END_, ISSUSPENDED_, PROCESSDEFINITION_, ROOTTOKEN_, SUPERPROCESSTOKEN_
   FROM JBPM_PROCESSINSTANCE
;
SET IDENTITY_INSERT T_JBPM_PROCESSINSTANCE OFF;

alter table JBPM_JOB drop constraint FK_JOB_PRINST;
alter table JBPM_JOB
    add constraint FK_JOB_PRINST foreign key (PROCESSINSTANCE_) references T_JBPM_PROCESSINSTANCE
;
alter table JBPM_MODULEINSTANCE drop constraint FK_MODINST_PRCINST;
alter table JBPM_MODULEINSTANCE
   add constraint FK_MODINST_PRCINST foreign key (PROCESSINSTANCE_) references T_JBPM_PROCESSINSTANCE
;
alter table JBPM_RUNTIMEACTION drop constraint FK_RTACTN_PROCINST;
alter table JBPM_RUNTIMEACTION
   add constraint FK_RTACTN_PROCINST foreign key (PROCESSINSTANCE_) references T_JBPM_PROCESSINSTANCE;
alter table JBPM_TASKINSTANCE drop constraint FK_TSKINS_PRCINS;
alter table JBPM_TASKINSTANCE
   add constraint FK_TSKINS_PRCINS foreign key (PROCINST_) references T_JBPM_PROCESSINSTANCE;
alter table JBPM_TOKEN drop constraint FK_TOKEN_PROCINST;
alter table JBPM_TOKEN
   add constraint FK_TOKEN_PROCINST foreign key (PROCESSINSTANCE_) references T_JBPM_PROCESSINSTANCE;
alter table JBPM_VARIABLEINSTANCE drop constraint FK_VARINST_PRCINST;
alter table JBPM_VARIABLEINSTANCE
   add constraint FK_VARINST_PRCINST foreign key (PROCESSINSTANCE_) references T_JBPM_PROCESSINSTANCE;
alter table JBPM_TOKEN drop constraint FK_TOKEN_SUBPI;
alter table JBPM_TOKEN
   add constraint FK_TOKEN_SUBPI foreign key (SUBPROCESSINSTANCE_) references T_JBPM_PROCESSINSTANCE;

DROP TABLE JBPM_PROCESSINSTANCE;
EXEC sp_rename 'T_JBPM_PROCESSINSTANCE', 'JBPM_PROCESSINSTANCE';

ALTER TABLE JBPM_PROCESSINSTANCE ADD
    constraint FK_PROCIN_PROCDEF foreign key (PROCESSDEFINITION_) references JBPM_PROCESSDEFINITION,
    constraint FK_PROCIN_ROOTTKN foreign key (ROOTTOKEN_) references JBPM_TOKEN,
    constraint FK_PROCIN_SPROCTKN foreign key (SUPERPROCESSTOKEN_) references JBPM_TOKEN;
create index IDX_PROCIN_ROOTTK on JBPM_PROCESSINSTANCE (ROOTTOKEN_);
create index IDX_PROCIN_SPROCTK on JBPM_PROCESSINSTANCE (SUPERPROCESSTOKEN_);
create index IDX_PROCIN_KEY on JBPM_PROCESSINSTANCE (KEY_);
create index IDX_PROCIN_PROCDEF on JBPM_PROCESSINSTANCE (PROCESSDEFINITION_);

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
