--
-- Title:      Upgraded Activiti tables to 5.11 version
-- Database:   MySQL
-- Since:      V4.1 Schema 6023
-- Author:     Frederik Heremans
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgraded Activiti tables to 5.11 version

alter table ACT_RE_PROCDEF 
    MODIFY KEY_ varchar(255) not null;

alter table ACT_RE_PROCDEF 
    MODIFY VERSION_ integer not null;
    
-- http://jira.codehaus.org/browse/ACT-1424    
alter table ACT_RU_JOB 
    MODIFY LOCK_EXP_TIME_ timestamp null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ varchar(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ varchar(4000);
    
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ integer;
    
update ACT_RU_TASK set SUSPENSION_STATE_ = 1;     

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);

create table ACT_RE_MODEL (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    KEY_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp null,
    LAST_UPDATE_TIME_ timestamp null,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE 
    foreign key (EDITOR_SOURCE_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE_EXTRA 
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);
    
alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_DEPLOYMENT 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);   

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';

alter table ACT_RU_JOB
    add PROC_DEF_ID_ varchar(64);

update ACT_GE_PROPERTY set VALUE_ = '5.11' where NAME_ = 'schema.version';

create table ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);

CREATE TABLE ACT_HI_ACTINST_TMP LIKE ACT_HI_ACTINST;
ALTER TABLE ACT_HI_ACTINST_TMP MODIFY ASSIGNEE_ VARCHAR(255);
alter table ACT_HI_ACTINST_TMP
   add TASK_ID_ varchar(64);
   
alter table ACT_HI_ACTINST_TMP
   add CALL_PROC_INST_ID_ varchar(64);
INSERT INTO ACT_HI_ACTINST_TMP SELECT *, NULL, NULL FROM ACT_HI_ACTINST;
DROP TABLE ACT_HI_ACTINST CASCADE; 
RENAME TABLE ACT_HI_ACTINST_TMP to ACT_HI_ACTINST;


CREATE TABLE ACT_HI_DETAIL_TMP LIKE ACT_HI_DETAIL;
alter table ACT_HI_DETAIL_TMP
   MODIFY PROC_INST_ID_ varchar(64) null;
alter table ACT_HI_DETAIL_TMP
   MODIFY EXECUTION_ID_ varchar(64) null;
INSERT INTO ACT_HI_DETAIL_TMP SELECT * FROM ACT_HI_DETAIL;
DROP TABLE ACT_HI_DETAIL CASCADE; 
RENAME TABLE ACT_HI_DETAIL_TMP to ACT_HI_DETAIL;

--
-- Update engine properties table
--
UPDATE ACT_GE_PROPERTY SET VALUE_ = '5.11' WHERE NAME_ = 'schema.version';
UPDATE ACT_GE_PROPERTY SET VALUE_ = CONCAT(VALUE_,' upgrade(5.11)') WHERE NAME_ = 'schema.history';

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-upgrade-to-activiti-5.11';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-upgrade-to-activiti-5.11', 'Manually executed script upgrade V4.2: Upgraded Activiti tables to 5.11 version',
    0, 5111, -1, 5112, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );