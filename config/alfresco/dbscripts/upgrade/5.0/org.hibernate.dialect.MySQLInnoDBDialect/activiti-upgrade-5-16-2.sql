--
-- Title:      Upgraded Activiti tables to 5.16.2 version
-- Database:   MySQL
-- Since:      V5.0 Schema 8002
-- Author:     Mark Rogers
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgraded Activiti tables from 5.14 to 5.16.2 version

# MySQL < 5.6.4 does not support timestamps/dates with millisecond precision.
# This upgrade file lacks the necessary upgrades that are done on a 5.6.4+ installation
# to get millisecond precision.

alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);
    
alter table ACT_RU_EXECUTION drop foreign key ACT_FK_EXE_PROCDEF;   

alter table ACT_RU_EXECUTION drop index ACT_UNIQ_RU_BUS_KEY;  

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);
    
#alter table ACT_RE_DEPLOYMENT 
#    add TENANT_ID_ varchar(255) default ''; 
    
#alter table ACT_RE_PROCDEF 
#    add TENANT_ID_ varchar(255) default '';     
    
alter table ACT_RU_EXECUTION
    add TENANT_ID_ varchar(255) default '';    
    
alter table ACT_RU_TASK
    add TENANT_ID_ varchar(255) default '';  
    
alter table ACT_RU_JOB
    add TENANT_ID_ varchar(255) default '';   
    
alter table ACT_RE_MODEL
    add TENANT_ID_ varchar(255) default '';  
    
alter table ACT_RU_EVENT_SUBSCR
   add TENANT_ID_ varchar(255) default ''; 
   
alter table ACT_RU_EVENT_SUBSCR
   add PROC_DEF_ID_ varchar(64);           
    
alter table ACT_RE_PROCDEF
    drop index ACT_UNIQ_PROCDEF;
    
alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);      

update ACT_GE_PROPERTY set VALUE_ = '5.15' where NAME_ = 'schema.version';

#  
# ACT-1867: MySQL DATETIME and TIMESTAMP precision
# The way this is done, is by creating a new column, pumping over all data
# and then removing the old column.
#

# DEPLOY_TIME_ in ACT_RE_DEPLOYMENT

ALTER TABLE ACT_RE_DEPLOYMENT ADD DEPLOY_TIME_TEMP_ timestamp;
UPDATE ACT_RE_DEPLOYMENT SET DEPLOY_TIME_TEMP_ = DEPLOY_TIME_;
ALTER TABLE ACT_RE_DEPLOYMENT DROP COLUMN DEPLOY_TIME_;
ALTER TABLE ACT_RE_DEPLOYMENT CHANGE DEPLOY_TIME_TEMP_ DEPLOY_TIME_ timestamp;


# CREATE_TIME_ in ACT_RE_MODEL

ALTER TABLE ACT_RE_MODEL ADD CREATE_TIME_TEMP_ timestamp null;
UPDATE ACT_RE_MODEL SET CREATE_TIME_TEMP_ = CREATE_TIME_; 
ALTER TABLE ACT_RE_MODEL DROP COLUMN CREATE_TIME_;
ALTER TABLE ACT_RE_MODEL CHANGE CREATE_TIME_TEMP_ CREATE_TIME_ timestamp null;


# LAST_UPDATE_TIME_ in ACT_RE_MODEL

ALTER TABLE ACT_RE_MODEL ADD LAST_UPDATE_TIME_TEMP_ timestamp null;
UPDATE ACT_RE_MODEL SET LAST_UPDATE_TIME_TEMP_ = LAST_UPDATE_TIME_; 
ALTER TABLE ACT_RE_MODEL DROP COLUMN LAST_UPDATE_TIME_;
ALTER TABLE ACT_RE_MODEL CHANGE LAST_UPDATE_TIME_TEMP_ LAST_UPDATE_TIME_ timestamp null;


# LOCK_EXP_TIME_ in ACT_RU_JOB

ALTER TABLE ACT_RU_JOB ADD LOCK_EXP_TIME_TEMP_ timestamp null;
UPDATE ACT_RU_JOB SET LOCK_EXP_TIME_TEMP_ = LOCK_EXP_TIME_; 
ALTER TABLE ACT_RU_JOB DROP COLUMN LOCK_EXP_TIME_;
ALTER TABLE ACT_RU_JOB CHANGE LOCK_EXP_TIME_TEMP_ LOCK_EXP_TIME_ timestamp null;


# DUEDATE_ in ACT_RU_JOB

ALTER TABLE ACT_RU_JOB ADD DUEDATE_TEMP_ timestamp null;
UPDATE ACT_RU_JOB SET DUEDATE_TEMP_ = DUEDATE_; 
ALTER TABLE ACT_RU_JOB DROP COLUMN DUEDATE_;
ALTER TABLE ACT_RU_JOB CHANGE DUEDATE_TEMP_ DUEDATE_ timestamp null;


# CREATE_TIME_ in ACT_RU_TASK

ALTER TABLE ACT_RU_TASK ADD CREATE_TIME_TEMP_ timestamp;
UPDATE ACT_RU_TASK SET CREATE_TIME_TEMP_ = CREATE_TIME_; 
ALTER TABLE ACT_RU_TASK DROP COLUMN CREATE_TIME_;
ALTER TABLE ACT_RU_TASK CHANGE CREATE_TIME_TEMP_ CREATE_TIME_ timestamp;


# DUE_DATE_ in ACT_RU_TASK

ALTER TABLE ACT_RU_TASK ADD DUE_DATE_TEMP_ datetime;
UPDATE ACT_RU_TASK SET DUE_DATE_TEMP_ = DUE_DATE_; 
ALTER TABLE ACT_RU_TASK DROP COLUMN DUE_DATE_;
ALTER TABLE ACT_RU_TASK CHANGE DUE_DATE_TEMP_ DUE_DATE_ datetime;


# CREATED_ in ACT_RU_EVENT_SUBSCR

ALTER TABLE ACT_RU_EVENT_SUBSCR ADD CREATED_TEMP_ timestamp not null;
UPDATE ACT_RU_EVENT_SUBSCR SET CREATED_TEMP_ = CREATED_; 
ALTER TABLE ACT_RU_EVENT_SUBSCR DROP COLUMN CREATED_;
ALTER TABLE ACT_RU_EVENT_SUBSCR CHANGE CREATED_TEMP_ CREATED_ timestamp not null DEFAULT CURRENT_TIMESTAMP;


alter table ACT_RU_TASK
    add FORM_KEY_ varchar(255);
    
alter table ACT_RU_EXECUTION
    add NAME_ varchar(255);
    
create table ACT_EVT_LOG (
    LOG_NR_ bigint auto_increment,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ LONGBLOB,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp null,
    IS_PROCESSED_ tinyint default 0,
    primary key (LOG_NR_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;  

update ACT_GE_PROPERTY set VALUE_ = '5.16' where NAME_ = 'schema.version';
update ACT_GE_PROPERTY set VALUE_ = '5.16.1' where NAME_ = 'schema.version';

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-Activiti-5.16.2';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-Activiti-5.16.2', 'Manually executed script upgrade V5.0: Upgraded Activiti tables to 5.16.2 version',
    0, 8001, -1, 8002, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );