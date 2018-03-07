drop table alf_test_bundle; --(optional)

create table alf_test_bundle
(
    message VARCHAR(255)
);

insert into alf_test_bundle (message) values ('script_a message 1');
insert into alf_test_bundle (message) values ('script_a message 2');
