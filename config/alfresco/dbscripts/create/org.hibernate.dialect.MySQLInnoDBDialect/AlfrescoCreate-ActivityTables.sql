--
-- Title:      Activity tables
-- Database:   MySQL InnoDB
-- Since:      V3.0 Schema 126
-- Author:     janv
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_activity_feed
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT,
    post_date DATETIME NOT NULL,
    activity_summary TEXT,
    feed_user_id VARCHAR(255),
    activity_type VARCHAR(255) NOT NULL,
    site_network VARCHAR(255),
    app_tool VARCHAR(36),
    post_user_id VARCHAR(255) NOT NULL,
    feed_date DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY feed_postdate_idx (post_date),
    KEY feed_postuserid_idx (post_user_id),
    KEY feed_feeduserid_idx (feed_user_id),
    KEY feed_sitenetwork_idx (site_network)
) ENGINE=InnoDB;

CREATE TABLE alf_activity_feed_control
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    feed_user_id VARCHAR(255) NOT NULL,
    site_network VARCHAR(255),
    app_tool VARCHAR(36),
    last_modified DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY feedctrl_feeduserid_idx (feed_user_id)
) ENGINE=InnoDB;

CREATE TABLE alf_activity_post
(
    sequence_id BIGINT NOT NULL AUTO_INCREMENT,
    post_date DATETIME NOT NULL,
    status VARCHAR(10) NOT NULL,
    activity_data TEXT NOT NULL,
    post_user_id VARCHAR(255) NOT NULL,
    job_task_node INTEGER NOT NULL,
    site_network VARCHAR(255),
    app_tool VARCHAR(36),
    activity_type VARCHAR(255) NOT NULL,
    last_modified DATETIME NOT NULL,
    PRIMARY KEY (sequence_id),
    KEY post_jobtasknode_idx (job_task_node),
    KEY post_status_idx (status)
) ENGINE=InnoDB;


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.0-ActivityTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.0-ActivityTables', 'Manually executed script upgrade V3.0: Activity Tables',
    0, 125, -1, 126, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );