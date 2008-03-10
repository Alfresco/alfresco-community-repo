--
-- Title:      Move static QNames and Namsespaces into a separate table
-- Database:   Oracle
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This script replaces the various static QName and Namespace entries
-- with a more efficient FK relationship to the static values.
--

-- Create static namespace and qname tables
-- The Primary Key is not added as it's easier to add in afterwards
CREATE TABLE alf_namespace
(
   id NUMBER(19,0) DEFAULT 0 NOT NULL,
   version number(19,0) NOT NULL,
   uri VARCHAR2(100 CHAR) NOT NULL,
   UNIQUE (uri)
);

CREATE TABLE alf_qname
(
   id NUMBER(19,0) DEFAULT 0 NOT NULL,
   version NUMBER(19,0) NOT NULL,
   ns_id NUMBER(19,0) NOT NULL,
   local_name VARCHAR2(200 char) NOT NULL,
   UNIQUE (ns_id, local_name)
);

-- Create temporary indexes and constraints
CREATE INDEX t_fk_alf_qn_ns on alf_qname (ns_id);

-- Create temporary table for dynamic (child) QNames
CREATE TABLE t_qnames_dyn
(
   qname VARCHAR2(255) NOT NULL,
   namespace VARCHAR2(255)
);
CREATE INDEX tidx_qnd_qn ON t_qnames_dyn (qname);
CREATE INDEX tidx_qnd_ns ON t_qnames_dyn (namespace);

-- Populate the table with the child association paths
INSERT INTO t_qnames_dyn (qname)
(
   SELECT qname FROM alf_child_assoc
);
-- Extract the Namespace
UPDATE t_qnames_dyn SET namespace = CONCAT('FILLER-', SUBSTR(qname,2,INSTRC(qname,'}',1)-2));
-- Move the namespaces to the their new home
INSERT INTO alf_namespace (uri, version)
(
   SELECT
      DISTINCT(x.namespace), 1
   FROM
   (
      SELECT t.namespace, n.uri FROM t_qnames_dyn t LEFT OUTER JOIN alf_namespace n ON (n.uri = t.namespace)
   ) x
   WHERE
      x.uri IS NULL
);
-- We can trash the temp table
DROP TABLE t_qnames_dyn;

-- Create temporary table to hold static QNames
CREATE TABLE t_qnames
(
   qname VARCHAR2(255) NOT NULL,
   namespace VARCHAR2(255),
   localname VARCHAR2(255)
);
CREATE INDEX tidx_tqn_qn ON t_qnames (qname);
CREATE INDEX tidx_tqn_ns ON t_qnames (namespace);
CREATE INDEX tidx_tqn_ln ON t_qnames (localname);

-- Populate the table with all known static QNames
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT type_qname FROM alf_node
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT qname FROM alf_node_aspects
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT qname FROM alf_node_properties
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT name FROM avm_aspects_new
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT qname FROM avm_node_properties_new
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT qname FROM avm_store_properties
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT type_qname FROM alf_node_assoc
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT type_qname FROM alf_child_assoc
);
INSERT INTO t_qnames (qname)
(
   SELECT DISTINCT type_qname FROM alf_permission
);
-- Extract the namespace and localnames from the QNames
UPDATE t_qnames SET namespace = CONCAT('FILLER-', SUBSTR(qname,2,INSTRC(qname,'}',1)-2));
UPDATE t_qnames SET localname = SUBSTR(qname,INSTRC(qname,'}',1)+1);
-- Move the Namespaces to their new home
INSERT INTO alf_namespace (uri, version)
(
   SELECT
      DISTINCT(x.namespace), 1
   FROM
   (
      SELECT t.namespace, n.uri FROM t_qnames t LEFT OUTER JOIN alf_namespace n ON (n.uri = t.namespace)
   ) x
   WHERE
      x.uri IS NULL
);
UPDATE alf_namespace SET id = hibernate_sequence.nextval;
ALTER TABLE alf_namespace ADD PRIMARY KEY (id);

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
UPDATE alf_qname SET id = hibernate_sequence.nextval;
ALTER TABLE alf_qname ADD PRIMARY KEY (id);

-- Check the data
ALTER TABLE alf_qname ADD CONSTRAINT t_fk_alf_qn_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id);

-- We can get trash the temp table
DROP TABLE t_qnames;

--
-- DATA REPLACEMENT: alf_node.type_qname
--
ALTER TABLE alf_node ADD ( type_qname_id NUMBER(19,0) NULL );
UPDATE alf_node n SET n.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = n.type_qname
);
ALTER TABLE alf_node DROP COLUMN type_qname;
ALTER TABLE alf_node MODIFY ( type_qname_id NUMBER(19,0) NOT NULL );

--
-- DATA REPLACEMENT: alf_node_aspects.qname
-- Due to the the potentially-missing primary key on the original table, it is
-- possible to have duplicates.  These are removed.
--
ALTER TABLE alf_node_aspects DROP PRIMARY KEY; -- (optional)
ALTER TABLE alf_node_aspects ADD ( qname_id NUMBER(19,0) NULL );
UPDATE alf_node_aspects na SET na.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = na.qname
);
ALTER TABLE alf_node_aspects DROP COLUMN qname;
ALTER TABLE alf_node_aspects MODIFY ( qname_id NUMBER(19,0) NOT NULL);
CREATE TABLE t_dup_aspects
(
   node_id NUMBER(19,0) NOT NULL,
   qname_id NUMBER(19,0) NOT NULL
);
INSERT INTO t_dup_aspects (node_id, qname_id)
(
   SELECT
      node_id, qname_id
   FROM
      alf_node_aspects
   GROUP BY
      node_id, qname_id
   HAVING
      count(*) > 1
);
DELETE alf_node_aspects na WHERE na.rowid IN (
   SELECT ina.rowid FROM alf_node_aspects ina
   JOIN t_dup_aspects t ON (ina.node_id = t.node_id AND ina.qname_id = t.qname_id)
);
INSERT INTO alf_node_aspects (node_id, qname_id)
(
   SELECT
      node_id, qname_id
   FROM
      t_dup_aspects
   GROUP BY
      node_id, qname_id
);
DROP TABLE t_dup_aspects;
ALTER TABLE alf_node_aspects ADD PRIMARY KEY (node_id, qname_id);

--
-- DATA REPLACEMENT: alf_node_properties.qname
--
ALTER TABLE alf_node_properties DROP PRIMARY KEY;
ALTER TABLE alf_node_properties ADD ( qname_id NUMBER(19,0) NULL );
UPDATE alf_node_properties np SET np.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = np.qname
);
ALTER TABLE alf_node_properties DROP COLUMN qname;
ALTER TABLE alf_node_properties MODIFY ( qname_id NUMBER(19,0) NOT NULL);
ALTER TABLE alf_node_properties ADD PRIMARY KEY (node_id, qname_id);

--
-- DATA REPLACEMENT: avm_aspects_new.name (aka qname)
--
ALTER TABLE avm_aspects_new DROP PRIMARY KEY;
ALTER TABLE avm_aspects_new ADD ( qname_id NUMBER(19,0) NULL );
UPDATE avm_aspects_new na SET na.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = na.name
);
ALTER TABLE avm_aspects_new DROP COLUMN name;
ALTER TABLE avm_aspects_new MODIFY ( qname_id NUMBER(19,0) NOT NULL);
ALTER TABLE avm_aspects_new ADD PRIMARY KEY (id, qname_id);

--
-- DATA REPLACEMENT: avm_node_properties.qname
--
-- This table is deprecated and made empty so there is no need to alter it

--
-- DATA REPLACEMENT: avm_node_properties_new.qname
--
ALTER TABLE avm_node_properties_new DROP PRIMARY KEY;
ALTER TABLE avm_node_properties_new ADD ( qname_id NUMBER(19,0) NULL );
UPDATE avm_node_properties_new np SET np.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = np.qname
);
ALTER TABLE avm_node_properties_new DROP COLUMN qname;
ALTER TABLE avm_node_properties_new MODIFY ( qname_id NUMBER(19,0) NOT NULL);
ALTER TABLE avm_node_properties_new ADD PRIMARY KEY (node_id, qname_id);

--
-- DATA REPLACEMENT: avm_store_properties.qname
--
ALTER TABLE avm_store_properties ADD ( qname_id NUMBER(19,0) NULL );
UPDATE avm_store_properties np SET np.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = np.qname
);
ALTER TABLE avm_store_properties DROP COLUMN qname;
ALTER TABLE avm_store_properties MODIFY ( qname_id NUMBER(19,0) NOT NULL);

--
-- DATA REPLACEMENT: alf_child_assoc.type_qname
--
ALTER TABLE alf_child_assoc DROP UNIQUE (parent_node_id, type_qname, child_node_name, child_node_name_crc);
ALTER TABLE alf_child_assoc ADD ( type_qname_id NUMBER(19,0) NULL );
UPDATE alf_child_assoc ca SET ca.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = ca.type_qname
);
ALTER TABLE alf_child_assoc DROP COLUMN type_qname;
ALTER TABLE alf_child_assoc MODIFY ( type_qname_id NUMBER(19,0) NOT NULL);
ALTER TABLE alf_child_assoc ADD UNIQUE (parent_node_id, type_qname_id, child_node_name, child_node_name_crc);

--
-- DATA REPLACEMENT: alf_child_assoc.qname
--
-- Namespace
ALTER TABLE alf_child_assoc ADD ( qname_ns_id NUMBER(19,0) NULL );
UPDATE alf_child_assoc ca SET ca.qname_ns_id =
(
   SELECT ns.id
   FROM alf_namespace ns
   WHERE CONCAT('...', SUBSTR(qname,2,INSTRC(qname,'}',1)-2)) = CONCAT('...', SUBSTR(ns.uri, 8))
);
ALTER TABLE alf_child_assoc MODIFY ( qname_ns_id NUMBER(19,0) NOT NULL);
-- LocalName
ALTER TABLE alf_child_assoc ADD ( qname_localname VARCHAR2(200) NULL);
UPDATE alf_child_assoc ca SET ca.qname_localname = SUBSTR(qname,INSTRC(qname,'}',1)+1);
ALTER TABLE alf_child_assoc MODIFY ( qname_localname VARCHAR2(200) NOT NULL);
-- Drop old column
ALTER TABLE alf_child_assoc DROP COLUMN qname;

--
-- DATA REPLACEMENT: alf_node_assoc.type_qname
--
ALTER TABLE alf_node_assoc DROP UNIQUE (source_node_id, target_node_id, type_qname);
ALTER TABLE alf_node_assoc ADD ( type_qname_id NUMBER(19,0) NULL );
UPDATE alf_node_assoc na SET na.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT(CONCAT('{', SUBSTR(ns.uri, 8)), CONCAT('}', q.local_name)) = na.type_qname
);
ALTER TABLE alf_node_assoc DROP COLUMN type_qname;
ALTER TABLE alf_node_assoc MODIFY ( type_qname_id NUMBER(19,0) NOT NULL);
ALTER TABLE alf_node_assoc ADD UNIQUE (source_node_id, target_node_id, type_qname_id);

--
-- DATA REPLACEMENT: alf_permission.type_qname
--
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

-- Drop the temporary indexes and constraints
DROP INDEX t_fk_alf_qn_ns;
ALTER TABLE alf_qname DROP CONSTRAINT t_fk_alf_qn_ns;

-- Remove the FILLER- values from the namespace uri
UPDATE alf_namespace SET uri = 'empty' WHERE uri = 'FILLER-';
UPDATE alf_namespace SET uri = SUBSTR(uri, 8) WHERE uri LIKE 'FILLER-%';

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-2-MoveQNames';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-2-MoveQNames', 'Manually executed script upgrade V2.2: Moved static QNames and Namespaces',
    0, 85, -1, 86, null, 'UNKOWN', 1, 1, 'Script completed'
  );
