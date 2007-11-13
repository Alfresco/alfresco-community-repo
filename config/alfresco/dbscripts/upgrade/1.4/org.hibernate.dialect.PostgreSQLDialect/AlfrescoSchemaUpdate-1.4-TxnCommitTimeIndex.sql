--
-- Explicit index for alf_transaction.commit_time_ms (PostgreSQL 1.4)
--

CREATE INDEX idx_commit_time_ms ON alf_transaction (commit_time_ms);
UPDATE alf_transaction SET commit_time_ms = id WHERE commit_time_ms IS NULL;

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.db-V1.4-TxnCommitTimeIndex';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.db-V1.4-TxnCommitTimeIndex', 'Executed script AlfrescoSchemaUpdate-1.4-TxnCommitTimeIndex.sql',
    0, 75, -1, 76, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );