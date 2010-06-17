--
-- Title:      Activities Schema - Extras (Indexes)
-- Database:   Generic
-- Since:      V3.0.0 Schema
--

CREATE INDEX post_jobtasknode_idx ON alf_activity_post(job_task_node);
CREATE INDEX post_status_idx ON alf_activity_post(status);

CREATE INDEX feed_postdate_idx ON alf_activity_feed(post_date);
CREATE INDEX feed_postuserid_idx ON alf_activity_feed(post_user_id);
CREATE INDEX feed_feeduserid_idx ON alf_activity_feed(feed_user_id);
CREATE INDEX feed_sitenetwork_idx ON alf_activity_feed(site_network);
CREATE INDEX feed_activityformat_idx ON alf_activity_feed(activity_format);

CREATE INDEX feedctrl_feeduserid_idx ON alf_activity_feed_control(feed_user_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.0-0-CreateActivitiesExtras';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.0-0-CreateActivitiesExtras', 'Executed script create V3.0: Created activities extras',
    0, 125, -1, 126, null, 'UNKNOWN', 1, 1, 'Script completed'
  );