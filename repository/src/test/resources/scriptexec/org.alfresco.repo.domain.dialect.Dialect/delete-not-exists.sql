drop table temp_tst_tbl_1; --(optional)
drop table temp_tst_tbl_2; --(optional)
drop table temp_tst_tbl_3; --(optional)
drop table temp_tst_tbl_4; --(optional)

create table temp_tst_tbl_1
(
    id INT8 NOT NULL,
    PRIMARY KEY (id)
);

create table temp_tst_tbl_2
(
    tbl_2_id INT8 NOT NULL,
    PRIMARY KEY (tbl_2_id)
);

create table temp_tst_tbl_3
(
    tbl_3_id INT8 NOT NULL,
    PRIMARY KEY (tbl_3_id)
);

create table temp_tst_tbl_4
(
    tbl_4_id INT8 NOT NULL,
    PRIMARY KEY (tbl_4_id)
);

insert into temp_tst_tbl_1 (id) values (1);
insert into temp_tst_tbl_1 (id) values (2);
insert into temp_tst_tbl_1 (id) values (3);
insert into temp_tst_tbl_1 (id) values (4);
insert into temp_tst_tbl_1 (id) values (5);
insert into temp_tst_tbl_1 (id) values (10);
insert into temp_tst_tbl_1 (id) values (11);

insert into temp_tst_tbl_2 (tbl_2_id) values (1);
insert into temp_tst_tbl_2 (tbl_2_id) values (10);
insert into temp_tst_tbl_2 (tbl_2_id) values (11);

insert into temp_tst_tbl_3 (tbl_3_id) values (2);
insert into temp_tst_tbl_3 (tbl_3_id) values (4);

--DELETE_NOT_EXISTS temp_tst_tbl_1.id,temp_tst_tbl_2.tbl_2_id,temp_tst_tbl_3.tbl_3_id,temp_tst_tbl_4.tbl_4_id system.upgrade.clean_alf_prop_tables.batchsize

