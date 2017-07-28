--
-- Title:      DROP Indexes
-- Database:   Generic
-- Since:      V4.1 Schema 6030
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-9275: When upgrading on Oracle RAC from version 3.2.2 to version 3.3 or higher, values returned by sequences are not ordered.

-- Used only for Oracle

--
-- Record script finish
--

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-fix-Repo-seqs-order';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-fix-Repo-seqs-order', 'Manually executed script to set ORDER bit for sequences',
    0, 6030, -1, 6031, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );