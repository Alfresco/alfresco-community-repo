drop table alf_test_script_exec_override; --(optional)

create table alf_test_script_exec_override
(
    message VARCHAR(255)
);

insert into alf_test_script_exec_override (message) values ('FAILURE! script should not have been run.');
