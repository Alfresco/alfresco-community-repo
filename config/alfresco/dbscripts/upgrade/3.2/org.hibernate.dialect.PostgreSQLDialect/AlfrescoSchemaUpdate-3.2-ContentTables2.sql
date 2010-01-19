--
-- Title:      Update Content tables (pre 3.2 Enterprise Final)
-- Database:   PostgreSQLDialect
-- Since:      V3.2 Schema 3009
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This update is required for installations that have run any of the early 3.2
-- codelines i.e. anything installed or upgraded to pre-3.2 Enterprise Final.

-- This is to (a) fix the naming convention and (b) to ensure that the index is UNIQUE
DROP INDEX idx_alf_cont_url_crc;                    --(optional)
DROP INDEX idx_alf_conturl_cr;                      --(optional)
CREATE UNIQUE INDEX idx_alf_conturl_cr ON alf_content_url (content_url_short, content_url_crc);

-- If this statement fails, it will be because the table already contains the orphan column
ALTER TABLE alf_content_url
   DROP COLUMN version,
   ADD COLUMN orphan_time INT8 NULL
;                                                   --(optional)
CREATE INDEX idx_alf_conturl_ot ON alf_content_url (orphan_time)
;                                                   --(optional)

-- This table will not exist for upgrades from pre 3.2 to 3.2 Enterprise Final
DROP TABLE alf_content_clean;                       --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-ContentTables2';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-ContentTables2', 'Manually executed script upgrade V3.2: Content Tables 2 (pre 3.2 Enterprise Final)',
    0, 3008, -1, 3009, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );