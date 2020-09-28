-- Simple test of the script executor functionality.


drop table alf_test_script_exec; --(optional)

create table alf_test_script_exec
(
    textfield VARCHAR(255)
);

insert into alf_test_script_exec (textfield) values ('hello');
insert into alf_test_script_exec (textfield) values ('world');
