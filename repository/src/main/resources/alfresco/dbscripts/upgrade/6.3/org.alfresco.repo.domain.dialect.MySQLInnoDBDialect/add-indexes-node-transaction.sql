--
-- Title:      Update alf_node and alf_transaction indexes for more performance
-- Database:   MySQL
-- Since:      V6.3
-- Author:     Eva Vasques
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

DROP INDEX idx_alf_node_ver ON alf_node;  --(optional)
CREATE INDEX idx_alf_node_ver ON alf_node (version);

DROP INDEX idx_alf_node_txn ON alf_node;  --(optional)
CREATE INDEX idx_alf_node_txn ON alf_node (transaction_id);

DROP INDEX idx_alf_txn_ctms ON alf_transaction;  --(optional)
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms, id);

DROP INDEX idx_alf_txn_ctms_sc ON alf_transaction;  --(optional)
CREATE INDEX idx_alf_txn_ctms_sc ON alf_transaction (commit_time_ms);

DROP INDEX idx_alf_txn_id_ctms ON alf_transaction;  --(optional)
CREATE INDEX idx_alf_txn_id_ctms ON alf_transaction (id, commit_time_ms);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V6.3-add-indexes-node-transaction';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V6.3-add-indexes-node-transaction', 'Create aditional indexes on alf_node and alf_transaction',
    0, 14001, -1, 14002, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
