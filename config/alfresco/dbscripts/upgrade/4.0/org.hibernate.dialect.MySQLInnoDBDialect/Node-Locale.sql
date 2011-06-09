--
-- Title:      Add 'locale_id' column to 'alf_node'
-- Database:   MySQL
-- Since:      V4.0 Schema 5010
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--ASSIGN:def_locale_id=id
SELECT id FROM alf_locale WHERE locale_str = '.default';

-- Add the column, using a default to fill
ALTER TABLE alf_node
    ADD COLUMN locale_id INT8 NOT NULL DEFAULT ${def_locale_id} AFTER type_qname_id,
    ADD KEY fk_alf_node_loc (locale_id),
    ADD CONSTRAINT fk_alf_node_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id)
;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.0-Node-Locale';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.0-Node-Locale', 'Manually executed script upgrade V4.0: Add locale_id column to alf_node',
    0, 5009, -1, 5010, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );