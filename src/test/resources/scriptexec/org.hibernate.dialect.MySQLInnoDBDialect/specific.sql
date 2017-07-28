drop table alf_test_script_exec_specific; --(optional)

create table alf_test_script_exec_specific
(
    message VARCHAR(255)
);

insert into alf_test_script_exec_specific (message) values ('mysql');
