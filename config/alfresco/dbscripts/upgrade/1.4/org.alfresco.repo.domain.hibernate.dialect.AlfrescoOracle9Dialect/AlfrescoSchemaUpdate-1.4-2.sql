-- ------------------------------------------------------
-- Alfresco Schema conversion V1.3 to V1.4 Part 2 (Oracle)
--
-- Adds the alf_transaction and alf_server tables to keep track of the sources
-- of transactions.
--
-- Author: Derek Hulley
-- ------------------------------------------------------

--
-- Create server and transaction tables
--

create table alf_server
(
  id number(19,0) not null,
  ip_address varchar2(15 char) not null,
  primary key (id),
  unique (ip_address)
);
insert into alf_server (id, ip_address) values (0, '0.0.0.0');

create table alf_transaction
(
  id number(19,0) not null,
  server_id number(19,0),
  change_txn_id varchar2(56 char) not null,
  primary key (id)
);
alter table alf_transaction add constraint FKB8761A3A9AE340B7 foreign key (server_id) references alf_server;
create index FKB8761A3A9AE340B7 on alf_transaction (server_id);

insert into alf_transaction
  (
    id, server_id, change_txn_id
  )
  select
    hibernate_sequence.nextval,
    (select max(id) from alf_server),
    change_txn_id
  from alf_node_status;

-- Alter node status
alter table alf_node_status add
  (
    transaction_id number(19,0) DEFAULT 0 NOT NULL
  );
-- Update FK column
update alf_node_status ns SET ns.transaction_id =
  (
    select t.id from alf_transaction t
    where t.change_txn_id = ns.change_txn_id and rownum = 1
  );
alter table alf_node_status DROP COLUMN change_txn_id;
alter table alf_node_status ADD CONSTRAINT FK71C2002B9E57C13D FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id);
create index FK71C2002B9E57C13D on alf_node_status (transaction_id);
alter table alf_node_status DROP COLUMN deleted;(optional)

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.schemaUpdateScript-V1.4-2';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.schemaUpdateScript-V1.4-2', 'Manually execute script upgrade V1.4 part 2',
    0, 20, -1, 21, sysdate, 'UNKOWN', 1, 1, 'Script completed'
  );
