--
-- Title:      Set ACL change set timestamps to sensible values after previous incorrect update
-- Database:   Generic
-- Since:      V4.0 Schema 5033
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Migrate data

--ASSIGN:min_tx_ms=min_tx_ms
SELECT min(commit_time_ms) as min_tx_ms from alf_transaction;

--FOREACH alf_acl_change_set.id system.upgrade.alf_acl_change_set.batchsize
UPDATE alf_acl_change_set
   SET
      commit_time_ms = ${min_tx_ms} + id
   WHERE
      id >= ${LOWERBOUND} AND id <= ${UPPERBOUND}
      AND  commit_time_ms < ${min_tx_ms} 
;


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.0-AclChangeSet2';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.0-AclChangeSet2', 'Manually executed script upgrade V4.0:  Set ACL change set timestamps to sensible values after previous incorrect update',
    0, 6008, -1, 6009, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );