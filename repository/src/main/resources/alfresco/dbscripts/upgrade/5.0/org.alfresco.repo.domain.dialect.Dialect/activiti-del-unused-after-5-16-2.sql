--
-- Title:      Update ACT_HI_PROCINST and ACT_RU_EXECUTION tables. Remove unused columns UNI_BUSINESS_KEY and UNI_PROC_DEF_ID
--             in both tables. Columns are unused after upgrade to Activiti 5.16.2
-- Database:   Generic
-- Since:      V5.0 Schema 8022
-- Author:     Alexander Malinovsky
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.0-remove-columns-after-upgrade-to-activiti-5.16.2';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.0-remove-columns-after-upgrade-to-activiti-5.16.2', 'Manually executed script upgrade V5.0: Upgraded Activiti tables to 5.16.2 version',
    0, 9002, -1, 9003, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );