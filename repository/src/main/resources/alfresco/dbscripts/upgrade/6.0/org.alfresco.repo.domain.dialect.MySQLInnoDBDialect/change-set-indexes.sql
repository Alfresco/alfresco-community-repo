--
-- Title:      Update alf_change_set indexes for more performance index tracking
-- Database:   MySQLInnoDB
-- Since:      V6.0 Schema 10201
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Nothing required

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V6.0-change-set-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V6.0-change-set-indexes', 'Manually executed script upgrade V6.0: Updates for change set tracking',
    0, 10200, -1, 10201, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );