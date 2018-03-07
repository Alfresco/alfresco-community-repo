--
-- Title:      Update alf_txn table and alf_node indexes to support SOLR tracking
-- Database:   Generic
-- Since:      V4.0 Schema 5023
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

DROP INDEX idx_alf_txn_ctms;
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms, id);

CREATE INDEX idx_alf_node_txn_del ON alf_node (transaction_id, node_deleted);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.0-SolrTracking';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.0-SolrTracking', 'Manually executed script upgrade V4.0: Updates for SOLR tracking',
    0, 5022, -1, 5023, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );