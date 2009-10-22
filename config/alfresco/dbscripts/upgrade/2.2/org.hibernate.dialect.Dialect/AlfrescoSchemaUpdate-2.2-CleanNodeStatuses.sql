--
-- Title:      Clean duplicate alf_node_status entries
-- Database:   Generic
-- Since:      V3.1 schema 1011
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Does nothing.  The script is only meaningful for DBs supported by Alfresco on V2.1
-- This script does not need to run if the server has already been upgraded to schema 90 or later

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-CleanNodeStatuses';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-CleanNodeStatuses', 'Manually executed script upgrade V2.2: Clean alf_node_status table',
     0, 89, -1, 90, null, 'UNKOWN', ${true}, ${true}, 'Script completed'
   );
