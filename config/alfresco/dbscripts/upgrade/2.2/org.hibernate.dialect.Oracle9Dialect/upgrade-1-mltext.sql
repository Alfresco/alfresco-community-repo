--
-- Title:      Pull MLText Values into Node Properties 
-- Database:   Oracle
-- Since:      V2.2 Schema 90
-- Author:     Derek Hulley
--
-- MLText values must be pulled back from attributes into localizable properties.
-- Several statements are not relevant to upgrades from below 77.  These are optional.
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_locale
(
   id NUMBER(19,0) DEFAULT 0 NOT NULL,
   version NUMBER(19,0) DEFAULT 1 NOT NULL,
   locale_str VARCHAR2(20 CHAR) NOT NULL,
   UNIQUE (locale_str)
);

INSERT INTO alf_locale (id, locale_str) VALUES (1, '.default');

INSERT INTO alf_locale (locale_str)
(
   SELECT DISTINCT(ma.mkey)
      FROM alf_node_properties np
      JOIN alf_attributes a1 ON (np.attribute_value = a1.id)
      JOIN alf_map_attribute_entries ma ON (ma.map_id = a1.id)
);  -- (OPTIONAL)
UPDATE alf_locale SET id = hibernate_sequence.nextval WHERE id != 1;
ALTER TABLE alf_locale ADD PRIMARY KEY (id);

-- Create a temporary table to hold the attribute_value information that needs replacing
CREATE TABLE t_alf_node_properties
(
   node_id NUMBER(19,0) NOT NULL,
   qname_id NUMBER(19,0) NOT NULL,
   list_index NUMBER(10,0) NOT NULL,
   locale_id NUMBER(19,0) NOT NULL,
   actual_type_n NUMBER(10,0) NOT NULL,
   persisted_type_n NUMBER(10,0) NOT NULL,
   boolean_value NUMBER(1,0),
   long_value NUMBER(19,0),
   float_value FLOAT,
   double_value DOUBLE PRECISION,
   string_value VARCHAR2(1024 char),
   serializable_value BLOB,
   CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES alf_node,
   CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname,
   CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale,
   PRIMARY KEY (node_id, qname_id, list_index, locale_id)
);
CREATE INDEX fk_alf_nprop_n ON t_alf_node_properties (node_id);
CREATE INDEX fk_alf_nprop_qn ON t_alf_node_properties (qname_id);
CREATE INDEX fk_alf_nprop_loc ON t_alf_node_properties (locale_id);

-- Copy all simple values over
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
   FROM alf_node_properties np
   WHERE
      np.attribute_value is null
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
   FROM alf_node_properties np
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

-- Clean up unused attribute values

CREATE TABLE t_del_attributes
(
   id NUMBER(19, 0) NOT NULL,
   PRIMARY KEY (id)
);
INSERT INTO t_del_attributes
(
   SELECT id FROM alf_attributes WHERE type = 'M'
)
;
DELETE
   FROM t_del_attributes t
   WHERE t.id =
   (
      SELECT ma.attribute_id FROM alf_map_attribute_entries ma WHERE ma.attribute_id = t.id
   )
;
DELETE
   FROM t_del_attributes t
   WHERE t.id =
   (
      SELECT la.attribute_id FROM alf_list_attribute_entries la WHERE la.attribute_id = t.id
   )
;
DELETE
   FROM t_del_attributes t
   WHERE t.id =
   (
      SELECT ga.attribute FROM alf_global_attributes ga WHERE ga.attribute = t.id
   )
;
INSERT INTO t_del_attributes
(
   SELECT a.id FROM t_del_attributes t
   JOIN alf_map_attribute_entries ma ON (ma.map_id = t.id)
   JOIN alf_attributes a ON (ma.attribute_id = a.id)
);
DELETE
   FROM alf_map_attribute_entries ma
   WHERE ma.map_id =
   (
	SELECT t.id FROM t_del_attributes t WHERE t.id = ma.map_id
   )
;
DELETE
   FROM alf_attributes a
   WHERE a.id =
   (
      SELECT t.id FROM t_del_attributes t WHERE t.id = a.id
   )
;
DROP TABLE t_del_attributes;


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-1-MLText';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-1-MLText', 'Manually executed script upgrade V2.2: Moved MLText values',
    0, 90, -1, 91, null, 'UNKOWN', 1, 1, 'Script completed'
  );