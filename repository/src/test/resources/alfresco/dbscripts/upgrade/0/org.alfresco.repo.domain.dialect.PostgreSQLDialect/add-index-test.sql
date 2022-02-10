
CREATE INDEX idx_alf_node_test ON alf_node (acl_id, audit_creator);  --(optional)


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V0-add-index-test';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V0-add-index-test', 'Manually executed script upgrade V0: Added new index test',
    0, 15000, -1, 15001, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );