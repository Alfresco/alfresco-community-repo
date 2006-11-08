-- ------------------------------------------------------
-- Alfresco Schema conversion V1.3 to V1.4 Part 2 (MySQL)
--
-- Adds the alf_transaction and alf_server tables to keep track of the sources
-- of transactions.
--
-- Author: Derek Hulley
-- ------------------------------------------------------

--
-- Create server and transaction tables
--

CREATE TABLE alf_server (
  id bigint(20) NOT NULL auto_increment,
  ip_address varchar(15) NOT NULL,
  PRIMARY KEY  (id),
  UNIQUE KEY ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into alf_server (id, ip_address) values (0, '0.0.0.0');

CREATE TABLE alf_transaction (
  id bigint(20) NOT NULL auto_increment,
  server_id bigint(20) default NULL,
  change_txn_id varchar(56) NOT NULL,
  PRIMARY KEY  (id),
  KEY FKB8761A3A9AE340B7 (server_id),
  CONSTRAINT FKB8761A3A9AE340B7 FOREIGN KEY (server_id) REFERENCES alf_server (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into alf_transaction
  (
    server_id, change_txn_id
  )
  select (select max(id) from alf_server), change_txn_id from alf_node_status group by change_txn_id;

-- Alter node status
ALTER TABLE alf_node_status
  ADD COLUMN transaction_id bigint(20) NOT NULL DEFAULT 0 AFTER node_id;
-- Update FK column
UPDATE alf_node_status ns SET ns.transaction_id =
  (
    select t.id from alf_transaction t where t.change_txn_id = ns.change_txn_id
  );
ALTER TABLE alf_node_status
  DROP COLUMN change_txn_id,
  ADD CONSTRAINT FK71C2002B9E57C13D FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id),
  ADD INDEX FK71C2002B9E57C13D (transaction_id);
ALTER TABLE alf_node_status
  DROP COLUMN deleted
  ;(optional)

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.schemaUpdateScript-V1.4-2';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.schemaUpdateScript-V1.4-2', 'Manually execute script upgrade V1.4 part 2',
    0, 20, -1, 21, now(), 'UNKOWN', 1, 1, 'Script completed'
  );