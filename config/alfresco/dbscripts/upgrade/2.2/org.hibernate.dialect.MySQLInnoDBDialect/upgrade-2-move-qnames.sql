--
-- Title:      Move static QNames and Namsespaces into a separate table
-- Database:   MySQL
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This script replaces the various static QName and Namespace entries
-- with a more efficient FK relationship to the static values.
--

-- Create static namespace and qname tables
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
   PRIMARY KEY (id),
   UNIQUE (ns_id, local_name)
) ENGINE=InnoDB;

-- Create temporary indexes and constraints
CREATE INDEX t_fk_alf_qn_ns on alf_qname (ns_id);
ALTER TABLE alf_qname ADD CONSTRAINT t_fk_alf_qn_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id);

-- Create temporary table for dynamic (child) QNames
CREATE TABLE t_qnames_dyn
(
   qname varchar(255) NOT NULL,
   namespace varchar(255)
) ENGINE=InnoDB;
CREATE INDEX tidx_qnd_qn ON t_qnames_dyn (qname);
CREATE INDEX tidx_qnd_ns ON t_qnames_dyn (namespace);

-- Populate the table with the child association paths
INSERT INTO t_qnames_dyn (qname)
(
   SELECT qname FROM alf_CHILD_ASSOC
);
-- Extract the Namespace
UPDATE t_qnames_dyn SET namespace = SUBSTR(SUBSTRING_INDEX(qname, '}', 1), 2);
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
-- We can get trash the temp table
DROP TABLE t_qnames_dyn;

-- Create temporary table to hold static QNames
CREATE TABLE t_qnames
(
   qname varchar(255) NOT NULL,
   namespace varchar(255),
   localname varchar(255)
) ENGINE=InnoDB;
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
-- Extract the namespace and localnames from the QNames
UPDATE t_qnames SET namespace = SUBSTR(SUBSTRING_INDEX(qname, '}', 1), 2);
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
-- We can get trash the temp table
DROP TABLE t_qnames;

--
-- DATA REPLACEMENT: alf_node.type_qname
--
ALTER TABLE alf_node ADD COLUMN type_qname_id BIGINT NULL AFTER uuid;
UPDATE alf_node n set n.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = n.type_qname
);
ALTER TABLE alf_node DROP COLUMN type_qname;
ALTER TABLE alf_node MODIFY COLUMN type_qname_id BIGINT NOT NULL AFTER uuid;

--
-- DATA REPLACEMENT: alf_node_aspects.qname
-- Due to the the potentially-missing primary key on the original table, it is
-- possible to have duplicates.  These are removed.
--
ALTER TABLE alf_node_aspects DROP PRIMARY KEY; -- (optional)
ALTER TABLE alf_node_aspects ADD COLUMN qname_id BIGINT NULL AFTER node_id;
UPDATE alf_node_aspects na set na.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = na.qname
);
ALTER TABLE alf_node_aspects DROP COLUMN qname;
ALTER TABLE alf_node_aspects MODIFY COLUMN qname_id BIGINT NOT NULL AFTER node_id;
CREATE TABLE t_dup_aspects
(
   node_id BIGINT NOT NULL,
   qname_id BIGINT NOT NULL
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
DELETE FROM alf_node_aspects na
   USING alf_node_aspects na 
   JOIN t_dup_aspects t ON (t.node_id = na.node_id AND t.qname_id = na.qname_id);
INSERT INTO alf_node_aspects (node_id, qname_id)
(
   SELECT
      node_id, qname_id
   FROM
      t_dup_aspects
);
DROP TABLE t_dup_aspects;
ALTER TABLE alf_node_aspects ADD PRIMARY KEY (node_id, qname_id);

--
-- DATA REPLACEMENT: alf_node_properties.qname
--
ALTER TABLE alf_node_properties DROP PRIMARY KEY;
ALTER TABLE alf_node_properties ADD COLUMN qname_id BIGINT NULL AFTER node_id;
UPDATE alf_node_properties np set np.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = np.qname
);
ALTER TABLE alf_node_properties DROP COLUMN qname;
ALTER TABLE alf_node_properties MODIFY COLUMN qname_id BIGINT NOT NULL AFTER node_id;
ALTER TABLE alf_node_properties ADD PRIMARY KEY (node_id, qname_id);

--
-- DATA REPLACEMENT: avm_aspects_new.name (aka qname)
--
ALTER TABLE avm_aspects_new DROP PRIMARY KEY;
ALTER TABLE avm_aspects_new ADD COLUMN qname_id BIGINT NULL AFTER id;
UPDATE avm_aspects_new na set na.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = na.name
);
ALTER TABLE avm_aspects_new DROP COLUMN name;
ALTER TABLE avm_aspects_new MODIFY COLUMN qname_id BIGINT NOT NULL AFTER id;
ALTER TABLE avm_aspects_new ADD PRIMARY KEY (id, qname_id);

--
-- DATA REPLACEMENT: avm_node_properties.qname
--
-- This table is deprecated and made empty so there is no need to alter it

--
-- DATA REPLACEMENT: avm_node_properties_new.qname
--
ALTER TABLE avm_node_properties_new DROP PRIMARY KEY;
ALTER TABLE avm_node_properties_new ADD COLUMN qname_id BIGINT NULL AFTER node_id;
UPDATE avm_node_properties_new np set np.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = np.qname
);
ALTER TABLE avm_node_properties_new DROP COLUMN qname;
ALTER TABLE avm_node_properties_new MODIFY COLUMN qname_id BIGINT NOT NULL AFTER node_id;
ALTER TABLE avm_node_properties_new ADD PRIMARY KEY (node_id, qname_id);

--
-- DATA REPLACEMENT: avm_store_properties.qname
--
ALTER TABLE avm_store_properties ADD COLUMN qname_id BIGINT NULL AFTER avm_store_id;
UPDATE avm_store_properties np set np.qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = np.qname
);
ALTER TABLE avm_store_properties DROP COLUMN qname;
ALTER TABLE avm_store_properties MODIFY COLUMN qname_id BIGINT NOT NULL AFTER avm_store_id;

--
-- DATA REPLACEMENT: alf_child_assoc.type_qname
--
ALTER TABLE alf_child_assoc ADD COLUMN type_qname_id BIGINT NULL AFTER child_node_id;
UPDATE alf_child_assoc ca set ca.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = ca.type_qname
);
ALTER TABLE alf_child_assoc DROP COLUMN type_qname;
ALTER TABLE alf_child_assoc MODIFY COLUMN type_qname_id BIGINT NOT NULL AFTER child_node_id;

--
-- DATA REPLACEMENT: alf_child_assoc.qname
--
-- Namespace
ALTER TABLE alf_child_assoc ADD COLUMN qname_ns_id BIGINT NULL AFTER type_qname_id;
UPDATE alf_child_assoc ca set ca.qname_ns_id =
(
   SELECT ns.id
   FROM alf_namespace ns
   WHERE SUBSTR(SUBSTRING_INDEX(qname, '}', 1), 2) = ns.uri
);
ALTER TABLE alf_child_assoc MODIFY COLUMN qname_ns_id BIGINT NOT NULL AFTER type_qname_id;
-- LocalName
ALTER TABLE alf_child_assoc ADD COLUMN qname_localname VARCHAR(200) NULL AFTER qname_ns_id;
UPDATE alf_child_assoc ca SET ca.qname_localname = SUBSTRING_INDEX(qname, '}', -1);
ALTER TABLE alf_child_assoc MODIFY COLUMN qname_localname VARCHAR(200) NOT NULL AFTER qname_ns_id;
-- Drop old column
ALTER TABLE alf_child_assoc DROP COLUMN qname;

--
-- DATA REPLACEMENT: alf_node_assoc.type_qname
--
ALTER TABLE alf_node_assoc ADD COLUMN type_qname_id BIGINT NULL AFTER target_node_id;
UPDATE alf_node_assoc na set na.type_qname_id =
(
   SELECT q.id
   FROM alf_qname q
   JOIN alf_namespace ns ON (q.ns_id = ns.id)
   WHERE CONCAT('{', ns.uri, '}', q.local_name) = na.type_qname
);
ALTER TABLE alf_node_assoc DROP COLUMN type_qname;
ALTER TABLE alf_node_assoc MODIFY COLUMN type_qname_id BIGINT NOT NULL AFTER target_node_id;

-- Drop the temporary indexes and constraints
ALTER TABLE alf_qname DROP INDEX t_fk_alf_qn_ns;
ALTER TABLE alf_qname DROP FOREIGN KEY t_fk_alf_qn_ns;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-QNames-2-MoveQNames';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-QNames-2-MoveQNames', 'Manually executed script upgrade V2.2: Moved static QNames and Namespaces',
    0, 85, -1, 86, null, 'UNKOWN', 1, 1, 'Script completed'
  );
