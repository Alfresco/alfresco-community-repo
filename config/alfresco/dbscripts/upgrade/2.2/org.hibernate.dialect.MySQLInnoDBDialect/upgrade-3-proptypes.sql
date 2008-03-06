--
-- Title:      Replace property type descriptors with numerical equivalents
-- Database:   MySQL
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

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

-- Modify the alf_node_properties table
ALTER TABLE alf_node_properties ADD COLUMN actual_type_n INTEGER NULL AFTER qname_id;
ALTER TABLE alf_node_properties ADD COLUMN persisted_type_n INTEGER NULL AFTER actual_type_n;

UPDATE alf_node_properties p SET p.actual_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.actual_type);
UPDATE alf_node_properties p SET p.persisted_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.persisted_type);

ALTER TABLE alf_node_properties DROP COLUMN actual_type;
ALTER TABLE alf_node_properties DROP COLUMN persisted_type;

ALTER TABLE alf_node_properties MODIFY COLUMN actual_type_n INTEGER NOT NULL AFTER qname_id;
ALTER TABLE alf_node_properties MODIFY COLUMN persisted_type_n INTEGER NOT NULL AFTER actual_type_n;

-- Modify the avm_node_properties_new table
ALTER TABLE avm_node_properties_new ADD COLUMN actual_type_n INTEGER NULL AFTER qname_id;
ALTER TABLE avm_node_properties_new ADD COLUMN persisted_type_n INTEGER NULL AFTER actual_type_n;

UPDATE avm_node_properties_new p SET p.actual_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.actual_type);
UPDATE avm_node_properties_new p SET p.persisted_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.persisted_type);

ALTER TABLE avm_node_properties_new DROP COLUMN actual_type;
ALTER TABLE avm_node_properties_new DROP COLUMN persisted_type;

ALTER TABLE avm_node_properties_new MODIFY COLUMN actual_type_n INTEGER NOT NULL AFTER qname_id;
ALTER TABLE avm_node_properties_new MODIFY COLUMN persisted_type_n INTEGER NOT NULL AFTER actual_type_n;

-- Modify the avm_store_properties table
ALTER TABLE avm_store_properties ADD COLUMN actual_type_n INTEGER NULL AFTER qname_id;
ALTER TABLE avm_store_properties ADD COLUMN persisted_type_n INTEGER NULL AFTER actual_type_n;

UPDATE avm_store_properties p SET p.actual_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.actual_type);
UPDATE avm_store_properties p SET p.persisted_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.persisted_type);

ALTER TABLE avm_store_properties DROP COLUMN actual_type;
ALTER TABLE avm_store_properties DROP COLUMN persisted_type;

ALTER TABLE avm_store_properties MODIFY COLUMN actual_type_n INTEGER NOT NULL AFTER qname_id;
ALTER TABLE avm_store_properties MODIFY COLUMN persisted_type_n INTEGER NOT NULL AFTER actual_type_n;

-- Modify the avm_node_properties table
ALTER TABLE avm_node_properties ADD COLUMN actual_type_n INTEGER NULL AFTER qname;
ALTER TABLE avm_node_properties ADD COLUMN persisted_type_n INTEGER NULL AFTER actual_type_n;

UPDATE avm_node_properties p SET p.actual_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.actual_type);
UPDATE avm_node_properties p SET p.persisted_type_n = (SELECT t.type_id FROM t_prop_types t WHERE t.type_name = p.persisted_type);

ALTER TABLE avm_node_properties DROP COLUMN actual_type;
ALTER TABLE avm_node_properties DROP COLUMN persisted_type;

ALTER TABLE avm_node_properties MODIFY COLUMN actual_type_n INTEGER NOT NULL AFTER qname;
ALTER TABLE avm_node_properties MODIFY COLUMN persisted_type_n INTEGER NOT NULL AFTER actual_type_n;

-- Remove temporary table
DROP TABLE t_prop_types;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-QNames-3-PropTypes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-QNames-3-PropTypes', 'Manually executed script upgrade V2.2: Changed PropertyValue types',
    0, 85, -1, 86, null, 'UNKOWN', 1, 1, 'Script completed'
  );
