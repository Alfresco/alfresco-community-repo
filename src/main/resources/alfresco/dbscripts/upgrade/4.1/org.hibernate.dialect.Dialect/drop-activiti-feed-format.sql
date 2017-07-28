--
-- Title:      Update ALF_ACTIVITY_FEED table. Delete all records with activity_format != "json". Remove column ACTIVITY_FORMAT
-- Database:   Generic
-- Since:      V4.1 Schema 5119
-- Author:     Alex Malinovsky
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-8983: 'Could not load activities list' in My/Site Activities dashlets after upgrade if activities were generated on 3.4.x
-- ALF-17493 : Remove alf_activity_feed.activity_format.
--
-- Record script finish
--

DELETE FROM alf_activity_feed WHERE activity_format <> 'json';

ALTER TABLE alf_activity_feed DROP COLUMN activity_format;

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-drop-activiti-feed-format';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-drop-activiti-feed-format', 'Manually executed script upgrade V4.1: Update ALF_ACTIVITY_FEED table. Remove column ACTIVITY_FORMAT',
    0, 6025, -1, 6026, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );