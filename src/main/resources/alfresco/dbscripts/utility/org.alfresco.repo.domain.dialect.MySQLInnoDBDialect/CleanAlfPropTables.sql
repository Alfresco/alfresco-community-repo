-- The script intended to clean obsolete properties from alf_prop_xxx tables
-- see MNT-10067
-- 
-- All the useful properties in alf_prop_root are gathered in temp_prop_root_ref.
-- These can be found in alf_audit_app.disabled_paths_id, alf_audit_entry.audit_values_id, alf_prop_unique_ctx.prop1_id
-- Then the obsolete ones are put to temp_prop_root_obs and deleted.
--
-- Afterwards, all the usefull properties in alf_prop_value are gathered in temp_prop_val_ref.
-- These can be found in alf_audit_app.app_name_id, alf_audit_entry.audit_user_id, alf_prop_link.key_prop_id, alf_prop_link.key_prop_id,
-- alf_prop_unique_ctx.value1_prop_id, alf_prop_unique_ctx.value2_prop_id, alf_prop_unique_ctx.value3_prop_id.
-- All of these tables are participating in recording audit. Afterwards the obsolete values in alf_prop_value are deleted.
-- Knowing all the ID's gathered in temp_prop_val_obs.long_value with a combination of the properties type in temp_prop_val_obs.persisted_type,
-- the rest of the values used in audit can be deleted from alf_prop_string_value, alf_prop_serializable_value, alf_prop_double_value.

-- create temp tables
create table temp_prop_root_ref
(
    id BIGINT NOT NULL,
    index idx_temp_prop_root_ref_id (id)
) ENGINE=MyISAM;
create table temp_prop_root_obs
(
    id BIGINT NOT NULL,
    index idx_temp_prop_root_obs_id (id)
) ENGINE=MyISAM;
create table temp_prop_val_ref
(
    id BIGINT NOT NULL,
    index idx_temp_prop_val_ref_id (id)
) ENGINE=MyISAM;
create table temp_prop_val_obs
(
    id BIGINT NOT NULL,
    persisted_type TINYINT NOT NULL,
    long_value BIGINT NOT NULL,
    index idx_temp_prop_val_obs_id (id),
    index idx_temp_prop_val_obs_per (persisted_type, id, long_value)
) ENGINE=MyISAM;

create table temp_del_str1
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
create table temp_del_str2
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
create table temp_del_ser1
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
create table temp_del_ser2
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
create table temp_del_double1
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;
create table temp_del_double2
(
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MyISAM;

-- Determine the maximum IDs in order to constrain deletion ranges and avoid deleting new data
--ASSIGN:PROP_ROOT_MAX_ID=idmax!-1
select max(id) as idmax from alf_prop_root;
--ASSIGN:PROP_VAL_MAX_ID=idmax!-1
select max(id) as idmax from alf_prop_value;
--ASSIGN:PROP_STRING_MAX_ID=idmax!-1
select max(id) as idmax from alf_prop_string_value;
--ASSIGN:PROP_SERIALIZABLE_MAX_ID=idmax!-1
select max(id) as idmax from alf_prop_serializable_value;
--ASSIGN:PROP_DOUBLE_MAX_ID=idmax!-1
select max(id) as idmax from alf_prop_double_value;

-- get all active references to alf_prop_root
--FOREACH alf_audit_app.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_root_ref select disabled_paths_id as id from alf_audit_app where id >= ${LOWERBOUND} and id <= ${UPPERBOUND};
--FOREACH alf_audit_entry.audit_values_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_root_ref select audit_values_id from alf_audit_entry where audit_values_id >= ${LOWERBOUND} and audit_values_id <= ${UPPERBOUND};
--FOREACH alf_prop_unique_ctx.prop1_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_root_ref select prop1_id from alf_prop_unique_ctx where prop1_id is not null and prop1_id >= ${LOWERBOUND} and prop1_id <= ${UPPERBOUND};

-- determine the obsolete entries from alf_prop_root
--FOREACH alf_prop_root.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_root_obs select alf_prop_root.id from alf_prop_root left join temp_prop_root_ref on temp_prop_root_ref.id = alf_prop_root.id where temp_prop_root_ref.id is null and alf_prop_root.id >= ${LOWERBOUND} and alf_prop_root.id <= ${UPPERBOUND} and alf_prop_root.id <= ${PROP_ROOT_MAX_ID};

-- clear alf_prop_root which cascades DELETE to alf_prop_link
--FOREACH temp_prop_root_obs.id system.upgrade.clean_alf_prop_tables.batchsize
delete apr from alf_prop_root apr inner join temp_prop_root_obs tpra on apr.id = tpra.id and tpra.id >= ${LOWERBOUND} and tpra.id <= ${UPPERBOUND};

-- get all active references to alf_prop_value
--FOREACH alf_prop_value.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select id from alf_prop_value where id in (select app_name_id from alf_audit_app) and id >= ${LOWERBOUND} and id <= ${UPPERBOUND};
--FOREACH alf_audit_entry.audit_user_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select audit_user_id from alf_audit_entry where audit_user_id >= ${LOWERBOUND} and audit_user_id <= ${UPPERBOUND};
--FOREACH alf_prop_link.key_prop_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select key_prop_id from alf_prop_link where key_prop_id >= ${LOWERBOUND} and key_prop_id <= ${UPPERBOUND};
--FOREACH alf_prop_link.value_prop_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select value_prop_id from alf_prop_link where value_prop_id >= ${LOWERBOUND} and value_prop_id <= ${UPPERBOUND};
--FOREACH alf_prop_unique_ctx.value1_prop_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select value1_prop_id from alf_prop_unique_ctx where value1_prop_id >= ${LOWERBOUND} and value1_prop_id <= ${UPPERBOUND};
--FOREACH alf_prop_unique_ctx.value2_prop_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select value2_prop_id from alf_prop_unique_ctx where value2_prop_id >= ${LOWERBOUND} and value2_prop_id <= ${UPPERBOUND};
--FOREACH alf_prop_unique_ctx.value3_prop_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_ref select value3_prop_id from alf_prop_unique_ctx where value3_prop_id >= ${LOWERBOUND} and value3_prop_id <= ${UPPERBOUND};

-- determine the obsolete entries from alf_prop_value
--FOREACH alf_prop_value.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_val_obs select apv.id, apv.persisted_type, apv.long_value from alf_prop_value apv left join temp_prop_val_ref on (apv.id = temp_prop_val_ref.id) where temp_prop_val_ref.id is null and apv.id >= ${LOWERBOUND} and apv.id <= ${UPPERBOUND} and apv.id <= ${PROP_VAL_MAX_ID};

-- clear the obsolete entries
--FOREACH temp_prop_val_obs.id system.upgrade.clean_alf_prop_tables.batchsize
delete apv from alf_prop_value apv inner join temp_prop_val_obs tpva on apv.id = tpva.id and tpva.id >= ${LOWERBOUND} and tpva.id <= ${UPPERBOUND};

-- find and clear obsoleted string values
-- find the strings already deleted
--FOREACH temp_prop_val_obs.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_del_str1 select distinct distinct pva.long_value from temp_prop_val_obs pva where pva.persisted_type in (3, 5, 6) and pva.id >= ${LOWERBOUND} and pva.id <= ${UPPERBOUND};
--FOREACH temp_del_str1.id system.upgrade.clean_alf_prop_tables.batchsize
delete aps from alf_prop_string_value aps inner join temp_del_str1 tds on aps.id = tds.id and tds.id >= ${LOWERBOUND} and tds.id <= ${UPPERBOUND};

-- or added only to the alf_prop_string_value
-- disabled, as it is an edge case and the query is rather slow, see MNT-10067
-- FOREACH alf_prop_string_value.id system.upgrade.clean_alf_prop_tables.batchsize
-- insert into temp_del_str2 select aps.id from alf_prop_string_value aps left join alf_prop_value apv on apv.long_value = aps.id and apv.persisted_type in (3, 5, 6) where apv.id is null and aps.id >= ${LOWERBOUND} and aps.id <= ${UPPERBOUND};
-- FOREACH temp_del_str2.id system.upgrade.clean_alf_prop_tables.batchsize
-- delete aps from alf_prop_string_value aps inner join temp_del_str2 tds on aps.id = tds.id and tds.id >= ${LOWERBOUND} and tds.id <= ${UPPERBOUND};

-- find and clear obsoleted serialized values
-- find the serialized values already deleted
--FOREACH temp_prop_val_obs.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_del_ser1 select distinct pva.long_value from temp_prop_val_obs pva where pva.persisted_type = 4 and pva.id >= ${LOWERBOUND} and pva.id <= ${UPPERBOUND};
--FOREACH temp_del_ser1.id system.upgrade.clean_alf_prop_tables.batchsize
delete aps from alf_prop_serializable_value aps inner join temp_del_ser1 tds on aps.id = tds.id and tds.id >= ${LOWERBOUND} and tds.id <= ${UPPERBOUND};

-- disabled, as it is an edge case and the query is rather slow, see MNT-10067
-- FOREACH alf_prop_serializable_value.id system.upgrade.clean_alf_prop_tables.batchsize
-- insert into temp_del_ser2 select aps.id from alf_prop_serializable_value aps left join alf_prop_value apv on apv.long_value = aps.id and apv.persisted_type = 4 where apv.id is null and aps.id >= ${LOWERBOUND} and aps.id <= ${UPPERBOUND};
-- FOREACH temp_del_ser2.id system.upgrade.clean_alf_prop_tables.batchsize
-- delete aps from alf_prop_serializable_value aps inner join temp_del_ser2 tds on aps.id = tds.id and tds.id >= ${LOWERBOUND} and tds.id <= ${UPPERBOUND};

-- find and clear obsoleted double values
-- find the double values already deleted
--FOREACH temp_prop_val_obs.id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_del_double1 select distinct pva.long_value from temp_prop_val_obs pva where pva.persisted_type = 2 and pva.id >= ${LOWERBOUND} and pva.id <= ${UPPERBOUND};
--FOREACH temp_del_double1.id system.upgrade.clean_alf_prop_tables.batchsize
delete apd from alf_prop_double_value apd inner join temp_del_double1 tdd on apd.id = tdd.id and tdd.id >= ${LOWERBOUND} and tdd.id <= ${UPPERBOUND};

-- disabled, as it is an edge case and the query is rather slow, see MNT-10067
-- FOREACH alf_prop_double_value.id system.upgrade.clean_alf_prop_tables.batchsize
-- insert into temp_del_double2 select apd.id from alf_prop_double_value apd left join alf_prop_value apv on apv.long_value = apd.id and apv.persisted_type = 2 where apv.id is null and apd.id >= ${LOWERBOUND} and apd.id <= ${UPPERBOUND};
-- FOREACH temp_del_double2.id system.upgrade.clean_alf_prop_tables.batchsize
-- delete apd from alf_prop_double_value apd inner join temp_del_double2 tdd on apd.id = tdd.id and tdd.id >= ${LOWERBOUND} and tdd.id <= ${UPPERBOUND};
