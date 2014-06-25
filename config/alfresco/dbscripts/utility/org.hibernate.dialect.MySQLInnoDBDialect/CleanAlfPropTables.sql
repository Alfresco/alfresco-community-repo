--BEGIN TXN

-- get all active references to alf_prop_root
--FOREACH alf_audit_app.id system.upgrade.clean_alf_prop_tables.batchsize
create table temp_prop_root_ref as select disabled_paths_id as id from alf_audit_app where id >= ${LOWERBOUND} and id <= ${UPPERBOUND};
create index idx_temp_prop_root_ref_id on temp_prop_root_ref(id);
--FOREACH alf_audit_entry.audit_values_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_root_ref select audit_values_id from alf_audit_entry where audit_values_id >= ${LOWERBOUND} and audit_values_id <= ${UPPERBOUND};
--FOREACH alf_prop_unique_ctx.prop1_id system.upgrade.clean_alf_prop_tables.batchsize
insert into temp_prop_root_ref select prop1_id from alf_prop_unique_ctx where prop1_id is not null and prop1_id >= ${LOWERBOUND} and prop1_id <= ${UPPERBOUND};

-- determine the obsolete entries from alf_prop_root
--FOREACH alf_prop_root.id system.upgrade.clean_alf_prop_tables.batchsize
create table temp_prop_root_abs as select alf_prop_root.id from alf_prop_root left join temp_prop_root_ref on temp_prop_root_ref.id = alf_prop_root.id where temp_prop_root_ref.id is null and alf_prop_root.id >= ${LOWERBOUND} and alf_prop_root.id <= ${UPPERBOUND};
create index idx_temp_prop_root_abs_id on temp_prop_root_abs(id);

-- clear alf_prop_root which cascades DELETE to alf_prop_link
--FOREACH temp_prop_root_abs.id system.upgrade.clean_alf_prop_tables.batchsize
delete from alf_prop_root where id in (select id from temp_prop_root_abs where id >= ${LOWERBOUND} and id <= ${UPPERBOUND});

-- get all active references to alf_prop_value

--FOREACH alf_prop_value.id system.upgrade.clean_alf_prop_tables.batchsize
create table temp_prop_val_ref as select id from alf_prop_value where id in (select app_name_id from alf_audit_app) and id >= ${LOWERBOUND} and id <= ${UPPERBOUND};
create index idx_temp_prop_val_ref_id on temp_prop_val_ref(id);
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
create table temp_prop_val_abs as select apv.id, apv.persisted_type, apv.long_value from alf_prop_value apv left join temp_prop_val_ref on (apv.id = temp_prop_val_ref.id) where temp_prop_val_ref.id is null and apv.id >= ${LOWERBOUND} and apv.id <= ${UPPERBOUND};
create index idx_temp_prop_val_abs_id on temp_prop_val_abs(id);
create index idx_temp_prop_val_abs_per on temp_prop_val_abs(persisted_type, id, long_value);

-- clear the obsolete entries
--FOREACH temp_prop_val_abs.id system.upgrade.clean_alf_prop_tables.batchsize
delete from alf_prop_value where id in (select id from temp_prop_val_abs where id >= ${LOWERBOUND} and id <= ${UPPERBOUND});

-- find and clear obsoleted string values
create table temp_del_str as select temp_prop_val_abs.long_value as string_id from temp_prop_val_abs left join alf_prop_value apv on (apv.id = temp_prop_val_abs.id) where temp_prop_val_abs.persisted_type in (3,5,6) and apv.id is null;
--FOREACH temp_del_str.string_id system.upgrade.clean_alf_prop_tables.batchsize
delete from alf_prop_string_value where id in (select id from temp_del_str where id >= ${LOWERBOUND} and id <= ${UPPERBOUND});

-- find and clear obsoleted serialized values
create table temp_del_ser as select temp_prop_val_abs.long_value as string_id from temp_prop_val_abs left join alf_prop_value apv on (apv.id = temp_prop_val_abs.id) where temp_prop_val_abs.persisted_type = 4 and apv.id is null;
--FOREACH temp_del_ser.string_id system.upgrade.clean_alf_prop_tables.batchsize
delete from alf_prop_serializable_value where id in (select id from temp_del_ser where id >= ${LOWERBOUND} and id <= ${UPPERBOUND});

-- find and clear obsoleted double values
create table temp_del_double as select temp_prop_val_abs.long_value as string_id from temp_prop_val_abs left join alf_prop_value apv on (apv.id = temp_prop_val_abs.id) where temp_prop_val_abs.persisted_type = 2 and apv.id is null;
--FOREACH temp_del_double.string_id system.upgrade.clean_alf_prop_tables.batchsize
delete from alf_prop_double_value where id in (select id from temp_del_double where id >= ${LOWERBOUND} and id <= ${UPPERBOUND});

--END TXN