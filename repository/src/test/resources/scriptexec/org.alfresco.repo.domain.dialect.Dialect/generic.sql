drop table alf_test_script_exec_generic; --(optional)

create table alf_test_script_exec_generic
(
    message VARCHAR(255)
);

insert into alf_test_script_exec_generic (message) values ('generic');
