--
-- Title:      Add txid index
-- Database:   PostgreSQL
-- Since:      V7.1.0
-- Author:     Bruno Bossola
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

DROP INDEX idx_alf_node_txn;
CREATE INDEX idx_alf_node_txn on alf_node (transaction_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V7.1.0-add-node-txid-index';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V7.1.0-add-node-txid-index', 'Creates additional index on alf_node',
    0, 15001, -1, 15002, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Transaction ID index added to alf_node'
  );
