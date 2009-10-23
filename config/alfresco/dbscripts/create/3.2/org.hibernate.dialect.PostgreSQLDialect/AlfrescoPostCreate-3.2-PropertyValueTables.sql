--
-- Title:      Property Value tables
-- Database:   PostgreSql
-- Since:      V3.2 Schema 3001
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_prop_class
(
   id INT8 NOT NULL,
   java_class_name VARCHAR(255) NOT NULL,
   java_class_name_short VARCHAR(32) NOT NULL,
   java_class_name_crc INT8 NOT NULL,   
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_prop_class_crc ON alf_prop_class(java_class_name_crc, java_class_name_short);
CREATE INDEX idx_alf_prop_class_class ON alf_prop_class(java_class_name);

CREATE SEQUENCE alf_prop_class_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_prop_date_value
(
   date_value INT8 NOT NULL,
   full_year INT4 NOT NULL,
   half_of_year INT2 NOT NULL,
   quarter_of_year INT2 NOT NULL,
   month_of_year INT2 NOT NULL,
   week_of_year INT2 NOT NULL,
   week_of_month INT2 NOT NULL,
   day_of_year INT4 NOT NULL,
   day_of_month INT2 NOT NULL,
   day_of_week INT2 NOT NULL,   
   PRIMARY KEY (date_value)
);
CREATE INDEX idx_alf_prop_date_units ON alf_prop_date_value(full_year, month_of_year, day_of_month);

CREATE TABLE alf_prop_double_value
(
   id INT8 NOT NULL,
   double_value FLOAT8 NOT NULL,   
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_prop_dbl_val ON alf_prop_double_value(double_value);

CREATE SEQUENCE alf_prop_double_value_seq START WITH 1 INCREMENT BY 1;

-- Stores unique, case-sensitive string values --
CREATE TABLE alf_prop_string_value
(
   id INT8 NOT NULL,
   string_value VARCHAR(1024) NOT NULL,
   string_end_lower VARCHAR(16) NOT NULL,
   string_crc INT8 NOT NULL,   
   PRIMARY KEY (id)
);
CREATE INDEX idx_alf_prop_str ON alf_prop_string_value(string_value);
CREATE UNIQUE INDEX idx_alf_prop_crc ON alf_prop_string_value(string_end_lower, string_crc);

CREATE SEQUENCE alf_prop_string_value_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_prop_serializable_value
(
   id INT8 NOT NULL,
   serializable_value BYTEA NOT NULL,
   PRIMARY KEY (id)
);
CREATE SEQUENCE alf_prop_serializable_value_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_prop_value
(
   id INT8 NOT NULL,
   actual_type_id INT8 NOT NULL,
   persisted_type INT2 NOT NULL,
   long_value INT8 NOT NULL,   
   PRIMARY KEY (id)
);
CREATE INDEX idx_alf_prop_per ON alf_prop_value(persisted_type, long_value);
CREATE UNIQUE INDEX idx_alf_prop_act ON alf_prop_value(actual_type_id, long_value);

CREATE SEQUENCE alf_prop_value_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_prop_root
(
   id INT8 NOT NULL,
   version INT4 NOT NULL,
   PRIMARY KEY (id)
);
CREATE SEQUENCE alf_prop_root_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_prop_link
(
   root_prop_id INT8 NOT NULL,
   prop_index INT8 NOT NULL,
   contained_in INT8 NOT NULL,
   key_prop_id INT8 NOT NULL,
   value_prop_id INT8 NOT NULL,
   CONSTRAINT fk_alf_prop_link_root FOREIGN KEY (root_prop_id) REFERENCES alf_prop_root (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_prop_link_key FOREIGN KEY (key_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_prop_link_val FOREIGN KEY (value_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,   
   PRIMARY KEY (root_prop_id, contained_in, prop_index)
);
CREATE INDEX idx_alf_prop_link_for ON alf_prop_link(root_prop_id, key_prop_id, value_prop_id);

CREATE TABLE alf_prop_unique_ctx
(
   id INT8 NOT NULL,
   version INT4 NOT NULL,
   value1_prop_id INT8 NOT NULL,
   value2_prop_id INT8 NOT NULL,
   value3_prop_id INT8 NOT NULL,   
   CONSTRAINT fk_alf_prop_unique_ctx_1 FOREIGN KEY (value1_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_prop_unique_ctx_2 FOREIGN KEY (value2_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_prop_unique_ctx_3 FOREIGN KEY (value3_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_prop_unique_ctx ON alf_prop_unique_ctx(value1_prop_id, value2_prop_id, value3_prop_id);

CREATE SEQUENCE alf_prop_unique_ctx_seq START WITH 1 INCREMENT BY 1;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-PropertyValueTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-PropertyValueTables', 'Manually executed script upgrade V3.2: PropertyValue Tables',
    0, 3000, -1, 3001, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );