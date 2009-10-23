--
-- Title:      Ensure the audit table path column is indexed
-- Database:   PostgreSQL
-- Since:      V2.1 Schema 82
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Path was previously unused and unindex - new we use it the index is required.

CREATE INDEX idx_alf_adtf_pth ON alf_audit_fact (path);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-AuditPathIndex';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-AuditPathIndex', 'Manually executed script upgrade V2.1: Ensure existence of audit path index',
    0, 81, -1, 82, null, 'UNKNOWN', TRUE, TRUE, 'Script completed'
  );