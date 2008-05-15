--
-- Title:      Activities Schema
-- Database:   MySQL
-- Since:      V3.0.0 Schema
--
-- Note: The Activities schema is NOT managed by Hibernate
--


CREATE TABLE alf_activity_post (
  sequence_id bigint NOT NULL auto_increment,
  post_date timestamp NOT NULL,
  status varchar(10) NOT NULL,
  activity_data varchar(4000) NOT NULL,
  post_user_id varchar(255) NOT NULL,
  job_task_node int(11) NOT NULL,
  site_network varchar(255) default NULL,
  app_tool varchar(36) default NULL,
  activity_type varchar(255) NOT NULL,
  last_modified timestamp NOT NULL,
  PRIMARY KEY (sequence_id),
  KEY jobtasknode_idx (job_task_node),
  KEY status_idx (status)
) type=InnoDB;


CREATE TABLE alf_activity_feed (
  id bigint NOT NULL auto_increment,
  post_id bigint default NULL,
  post_date timestamp NOT NULL,
  activity_summary varchar(4000) default NULL,
  feed_user_id varchar(255) NOT NULL,
  activity_type varchar(255) NOT NULL,
  activity_format varchar(10) default NULL,
  site_network varchar(255) default NULL,
  app_tool varchar(36) default NULL,
  post_user_id varchar(255) NOT NULL,
  feed_date timestamp NOT NULL,
  PRIMARY KEY (id),
  KEY postdate_idx (post_date),
  KEY feeduserid_idx (feed_user_id),
  KEY postuserid_idx (post_user_id),
  KEY sitenetwork_idx (site_network),
  KEY activityformat_idx (activity_format)
) type=InnoDB;


CREATE TABLE alf_activity_feed_control (
  id bigint NOT NULL auto_increment,
  feed_user_id varchar(255) NOT NULL,
  site_network varchar(255) NOT NULL,
  app_tool varchar(36) NOT NULL,
  last_modified timestamp NOT NULL,
  PRIMARY KEY (id),
  KEY feedcontroluserid_idx (feed_user_id)
) type=InnoDB;
