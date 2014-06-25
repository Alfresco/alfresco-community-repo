--SET-DELIMITER:    / 
drop table alf_test_custom_delimiter/ --(optional)

create table alf_test_custom_delimiter
(
    message VARCHAR(255)
)/

insert into alf_test_custom_delimiter (message) values ('custom delimter success')/

--SET-DELIMITER:    ; 
insert into alf_test_custom_delimiter (message) values ('custom delimter success again');
