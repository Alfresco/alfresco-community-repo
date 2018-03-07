--
-- Title:      DROP Indexes
-- Database:   MySQL
-- Since:      V4.1 Schema 5119
-- Author:     Valery Shikunets
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-16286: DROP fk_alf_qname_ns from alf_qname table

ALTER TABLE alf_qname DROP INDEX fk_alf_qname_ns;  --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-drop-alfqname-fk-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-drop-alfqname-fk-indexes', 'Manually executed script upgrade V4.1: DROP fk_alf_qname_ns on alf_qname table',
    0, 6023, -1, 6024, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );