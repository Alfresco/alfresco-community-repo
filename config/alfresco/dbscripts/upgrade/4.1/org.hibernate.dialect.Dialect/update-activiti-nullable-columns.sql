--
-- Title:      Update ALF_ACTIVITY_FEED and ALF_ACTIVITY_FEED_CONTROL tables by setting special @@NULL@@ value for nullable columns feed_user_id and site_network.
-- Database:   Generic
-- Since:      V4.1 Schema 5149
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-9532: SQL performance issue - WHERE ... IS NULL statements
--
-- Record script finish
--
-- Since oracle treats empty strings as NULLs, we have to use following format in where clause
-- ((feed_user_id IS NULL AND '' IS NULL) OR feed_user_id = '')
-- (feed_user_id IS NULL AND '' IS NULL) is Oracle specific part

--FOREACH alf_activity_feed.id system.upgrade.alf_activity_feed.batchsize
UPDATE alf_activity_feed af
   SET feed_user_id = '@@NULL@@'
   WHERE
      ((feed_user_id IS NULL AND '' IS NULL) OR feed_user_id = '') AND af.id >= ${LOWERBOUND} AND af.id <= ${UPPERBOUND};

--FOREACH alf_activity_feed.id system.upgrade.alf_activity_feed.batchsize
UPDATE alf_activity_feed af
   SET site_network = '@@NULL@@'
   WHERE
      ((site_network IS NULL AND '' IS NULL) OR site_network = '') AND af.id >= ${LOWERBOUND} AND af.id <= ${UPPERBOUND};

--FOREACH alf_activity_feed_control.id system.upgrade.alf_activity_feed_control.batchsize
UPDATE alf_activity_feed_control afc
   SET feed_user_id = '@@NULL@@'
   WHERE
      ((feed_user_id IS NULL AND '' IS NULL) OR feed_user_id = '') AND afc.id >= ${LOWERBOUND} AND afc.id <= ${UPPERBOUND};

--FOREACH alf_activity_feed_control.id system.upgrade.alf_activity_feed_control.batchsize
UPDATE alf_activity_feed_control afc
   SET site_network = '@@NULL@@'
   WHERE
      ((site_network IS NULL AND '' IS NULL) OR site_network = '') AND afc.id >= ${LOWERBOUND} AND afc.id <= ${UPPERBOUND};

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-update-activiti-nullable-columns';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-update-activiti-nullable-columns', 'Manually executed script upgrade V4.1: ALF_ACTIVITY_FEED and ALF_ACTIVITY_FEED_CONTROL tables. Updates feed_user_id and site_network columns with @@NULL@@ value',
    0, 5149, -1, 5150, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );