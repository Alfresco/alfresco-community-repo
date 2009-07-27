--
-- Title:      Property Value tables
-- Database:   MySQL InnoDB
-- Since:      V3.3 Schema 3001
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_prop_class
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   java_class_name VARCHAR(255) NOT NULL,
   java_class_name_short VARCHAR(32) NOT NULL,
   java_class_name_crc BIGINT NOT NULL,
   UNIQUE INDEX idx_prop_class_crc (java_class_name_crc, java_class_name_short),
   INDEX idx_prop_class_class (java_class_name),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_string_value
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   string_value text NOT NULL,
   INDEX idx_prop_str_val (string_value(64)),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

--CREATE TABLE alf_prop_string_value
--(
--   id BIGINT NOT NULL AUTO_INCREMENT,
--   string_value text NOT NULL,
--   prop_class_id BIGINT NOT NULL,
--   INDEX idx_prop_str_val (string_value(64)),
--   CONSTRAINT fk_prop_str_classid FOREIGN KEY (prop_class_id) REFERENCES alf_prop_class (id),
--   PRIMARY KEY (id)
--) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-PropertyValueTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-PropertyValueTables', 'Manually executed script upgrade V3.3: PropertyValue Tables',
    0, 3000, -1, 3001, null, 'UNKOWN', 1, 1, 'Script completed'
  );