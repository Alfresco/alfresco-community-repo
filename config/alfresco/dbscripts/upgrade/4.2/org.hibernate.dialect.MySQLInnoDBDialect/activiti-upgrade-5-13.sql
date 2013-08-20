--
-- Title:      Upgraded Activiti tables to 5.13 version
-- Database:   MySQL
-- Since:      V4.1 Schema 6029
-- Author:     Frederik Heremans
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgraded Activiti tables to 5.13 version

alter table ACT_HI_TASKINST
  add CLAIM_TIME_ datetime;

alter table ACT_HI_TASKINST
  add FORM_KEY_ varchar(255);
  
alter table ACT_RU_IDENTITYLINK
  add PROC_INST_ID_ varchar(64);
  
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_);     

create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);

create table ACT_HI_IDENTITYLINK (
    ID_ varchar(64),
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_HI_IDENT_LNK_USER on ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_);

drop index ACT_IDX_IDL_PROCINST on ACT_RU_IDENTITYLINK; -- (optional)
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_); -- (optional)

--
-- Update engine properties table
--
UPDATE ACT_GE_PROPERTY SET VALUE_ = '5.13' WHERE NAME_ = 'schema.version';
UPDATE ACT_GE_PROPERTY SET VALUE_ = CONCAT(VALUE_,' upgrade(5.13)') WHERE NAME_ = 'schema.history';

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-upgrade-to-activiti-5.13';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-upgrade-to-activiti-5.13', 'Manually executed script upgrade V4.2: Upgraded Activiti tables to 5.13 version',
    0, 6028, -1, 6029
    , null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );