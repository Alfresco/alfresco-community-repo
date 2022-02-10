--
-- Title:      Remove alf_server table
-- Database:   PostgreSQL
-- Since:      V6.3
-- Author:     Bruno Bossola
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

ALTER TABLE alf_transaction
    DROP CONSTRAINT IF EXISTS fk_alf_txn_svr,
    DROP COLUMN IF EXISTS server_id;

DROP TABLE IF EXISTS alf_server;
DROP SEQUENCE IF EXISTS alf_server_seq;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V7.1.0-remove-alf_server-table';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V7.1.0-remove-alf_server-table', 'Removes alf_server table and constraints',
    0, 15000, -1, 15001, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'alf_server table and constraints removed'
  );
