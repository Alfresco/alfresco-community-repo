drop table alf_test_bundle2; --(optional)

create table alf_test_bundle2
(
    message VARCHAR(255)
);

insert into alf_test_bundle2 (message) values ('script_a message 1');
insert into alf_test_bundle2 (message) values ('script_a message 2');
