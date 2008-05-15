--
-- Title:      Activities Schema
-- Database:   Oracle
-- Since:      V3.0.0 Schema
--
-- Note: The Activities schema is NOT managed by Hibernate
--


CREATE TABLE alf_activity_post (
  sequence_id number(19,0) NOT NULL,
  post_date timestamp NOT NULL,
  status varchar2(10) NOT NULL,
  activity_data varchar2(4000) NOT NULL,
  post_user_id varchar2(255) NOT NULL,
  job_task_node number(19,0) NOT NULL,
  site_network varchar2(255) default NULL,
  app_tool varchar2(36) default NULL,
  activity_type varchar2(255) NOT NULL,
  last_modified timestamp NOT NULL,
  primary key (sequence_id)
);

CREATE SEQUENCE alf_activity_post_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX jobtasknode_idx on alf_activity_post(job_task_node);
CREATE INDEX status_idx on alf_activity_post(status);


CREATE TABLE alf_activity_feed (
  id number(19,0) NOT NULL,
  post_id number(19,0) default NULL,
  post_date timestamp NOT NULL,
  activity_summary varchar2(4000) default NULL,
  feed_user_id varchar2(255) default NULL,
  activity_type varchar2(255) NOT NULL,
  activity_format varchar2(10) default NULL,
  site_network varchar2(255) default NULL,
  app_tool varchar2(36) default NULL,
  post_user_id varchar2(255) NOT NULL,
  feed_date timestamp NOT NULL,
  primary key (id)
);

CREATE SEQUENCE alf_activity_feed_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX postdate_idx ON alf_activity_feed(post_date);
CREATE INDEX feeduserid_idx ON alf_activity_feed(feed_user_id);
CREATE INDEX postuserid_idx ON alf_activity_feed(post_user_id);
CREATE INDEX sitenetwork_idx ON alf_activity_feed(site_network);
CREATE INDEX activityformat_idx ON alf_activity_feed(activity_format);


CREATE TABLE alf_activity_feed_control (
  id number(19,0) NOT NULL,
  feed_user_id varchar2(255) NOT NULL,
  site_network varchar2(255) NOT NULL,
  app_tool varchar2(36) NOT NULL,
  last_modified timestamp NOT NULL,
  primary key (id)
);

CREATE SEQUENCE alf_activity_feed_control_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX feedcontroluserid_idx ON alf_activity_feed_control(feed_user_id);
