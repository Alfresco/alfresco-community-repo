--
-- Title:      Upgrade to V3.3 - Remove Version Count
-- Database:   Generic
-- Since:      V3.3 schema 4003
-- Author:     janv
--
-- remove (obsolete) version count table
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

drop table alf_version_count;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-Remove-VersionCount';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-Remove-VersionCount', 'Manually executed script upgrade V3.3 to remove Version Count',
     0, 4002, -1, 4003, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
