--
-- Title:      Upgraded Activiti tables to 5.10 version
-- Database:   MySQL
-- Since:      V4.1 Schema 5115
-- Author:     Frederik Heremans
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgraded Activiti tables to 5.10 version

--
-- Upgrade scripts for 5.8 to 5.9
--

alter table ACT_RU_EXECUTION 
add SUSPENSION_STATE_ integer;

alter table ACT_RE_PROCDEF
add SUSPENSION_STATE_ integer;

alter table ACT_RE_PROCDEF
add REV_ integer;

update ACT_RE_PROCDEF set REV_ = 1;
update ACT_RE_PROCDEF set SUSPENSION_STATE_ = 1;
update ACT_RU_EXECUTION set SUSPENSION_STATE_ = 1;

create table ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp not null,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);

alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION(ID_);
    
alter table ACT_RU_EXECUTION 
add IS_EVENT_SCOPE_ TINYINT;

update ACT_RU_EXECUTION set IS_EVENT_SCOPE_ = 0;

CREATE TABLE ACT_HI_PROCINST_TMP LIKE ACT_HI_PROCINST;
alter table ACT_HI_PROCINST_TMP
add DELETE_REASON_ varchar(4000);
INSERT INTO ACT_HI_PROCINST_TMP SELECT *, NULL FROM ACT_HI_PROCINST;
DROP TABLE ACT_HI_PROCINST CASCADE; 
RENAME TABLE ACT_HI_PROCINST_TMP to ACT_HI_PROCINST;

CREATE TABLE ACT_GE_BYTEARRAY_TMP LIKE ACT_GE_BYTEARRAY;
alter table ACT_GE_BYTEARRAY_TMP 
add GENERATED_ TINYINT;
INSERT INTO ACT_GE_BYTEARRAY_TMP SELECT *, 0 FROM ACT_GE_BYTEARRAY;

ALTER TABLE ACT_RU_VARIABLE DROP FOREIGN KEY ACT_FK_VAR_BYTEARRAY;
ALTER TABLE ACT_RU_JOB DROP FOREIGN KEY ACT_FK_JOB_EXCEPTION;
DROP TABLE ACT_GE_BYTEARRAY CASCADE; 
RENAME TABLE ACT_GE_BYTEARRAY_TMP to ACT_GE_BYTEARRAY;

alter table ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);
    
alter table ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_JOB 
    add constraint ACT_FK_JOB_EXCEPTION 
    foreign key (EXCEPTION_STACK_ID_) 
    references ACT_GE_BYTEARRAY (ID_);




--
-- Upgrade scripts for 5.9 to 5.10
--

alter table ACT_RU_IDENTITYLINK
add PROC_DEF_ID_ varchar(64);

create index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF(ID_);
    
alter table ACT_RU_EXECUTION 
	add CACHED_ENT_STATE_ integer;

update ACT_RU_EXECUTION set CACHED_ENT_STATE_ = 7;
	
alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_);
    
--
-- Update engine properties table
--
UPDATE ACT_GE_PROPERTY SET VALUE_ = '5.10' WHERE NAME_ = 'schema.version';
UPDATE ACT_GE_PROPERTY SET VALUE_ = 'create(5.7) upgrade(5.10)' WHERE NAME_ = 'schema.history';

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-upgrade-to-activiti-5.10';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-upgrade-to-activiti-5.10', 'Manually executed script upgrade V4.2: Upgraded Activiti tables to 5.10 version',
    0, 5111, -1, 5112, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );