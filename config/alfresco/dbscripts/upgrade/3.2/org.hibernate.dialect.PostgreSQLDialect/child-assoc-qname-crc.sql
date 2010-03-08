--
-- Title:      Upgrade to V3.2 - Add qname_crc column to alf_child_assoc 
-- Database:   PostgreSQL
-- Since:      V3.2 schema 3006
-- Author:     Pavel Yurkevich
--
-- Add qname_crc column to alf_child_assoc and change indexes
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

ALTER TABLE alf_child_assoc
   ADD COLUMN qname_crc INT8 NOT NULL DEFAULT 0;

-- Enable additional patches to run by CRC-ing the descriptor nodes
UPDATE alf_child_assoc
   SET qname_crc = 147310537
   WHERE qname_ns_id = (SELECT id FROM alf_namespace WHERE uri = 'http://www.alfresco.org/model/system/1.0')
   AND qname_localname = 'descriptor';

UPDATE alf_child_assoc
   SET qname_crc = 369154895
   WHERE qname_ns_id = (SELECT id FROM alf_namespace WHERE uri = 'http://www.alfresco.org/model/system/1.0')
   AND qname_localname = 'descriptor-current';

ALTER TABLE alf_child_assoc ALTER COLUMN qname_crc DROP DEFAULT;

DROP INDEX idx_alf_cass_qnln;

CREATE INDEX idx_alf_cass_qncrc ON alf_child_assoc (qname_crc, type_qname_id, parent_node_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-Child-Assoc-QName-CRC';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-Child-Assoc-QName-CRC', 'Manually executed script upgrade V3.2 to Add qname_crc column to alf_child_assoc and change indexes',
     0, 3005, -1, 3006, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
