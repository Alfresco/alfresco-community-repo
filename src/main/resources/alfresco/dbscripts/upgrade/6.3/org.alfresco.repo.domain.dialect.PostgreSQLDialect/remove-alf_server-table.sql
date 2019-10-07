--
-- Title:      Remove alf_server table
-- Database:   PostgreSQL
-- Since:      V6.3
-- Author:     David Edwards
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- DROP the indexes
DROP INDEX fk_alf_txn_svr;
DROP INDEX idx_alf_txn_ctms;

-- DROP the constraints alf_transaction
ALTER TABLE alf_transaction DROP CONSTRAINT fk_alf_txn_svr;

-- Rename existing alf_transaction to t_alf_transaction
ALTER TABLE alf_transaction RENAME TO t_alf_transaction;

-- Create new alf_transaction table with new schema
CREATE TABLE alf_transaction
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    change_txn_id VARCHAR(56) NOT NULL,
    commit_time_ms INT8,
    PRIMARY KEY (id)
);
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms, id);


--FOREACH t_alf_transaction.id system.upgrade.alf_server_deleted.batchsize
INSERT INTO alf_transaction
(id, version, change_txn_id, commit_time_ms)
(
    SELECT
       id, version, change_txn_id, commit_time_ms
    FROM
       t_alf_transaction
    WHERE
       id >= ${LOWERBOUND} AND id <= ${UPPERBOUND}
);

-- DROP existing fk constraint from alf_node ADD a new reference to the new alf_transaction table
ALTER TABLE alf_node 
   DROP CONSTRAINT fk_alf_node_txn, 
   ADD CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) 
   REFERENCES alf_transaction (id);

DROP TABLE t_alf_transaction;
DROP TABLE alf_server;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V6.3-remove-alf_server-table';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V6.3-remove-alf_server-table', 'Remove alf_server table',
    0, 14000, -1, 14001, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );