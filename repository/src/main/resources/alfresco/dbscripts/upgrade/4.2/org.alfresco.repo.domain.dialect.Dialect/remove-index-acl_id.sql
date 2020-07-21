--
-- Title:      Update ALF_ACL_MEMBER_member table. Remove index acl_id
-- Database:   Generic
-- Since:      V4.2 Schema 6025
-- Author:     Alex Malinovsky
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-12284 : Index aclm_acl_id has the wrong name after upgrading 2.2 to 3.4 to 4.0.

--
-- Record script finish
--


ALTER TABLE alf_acl_member DROP INDEX acl_id; --(optional)

CREATE UNIQUE INDEX aclm_acl_id ON alf_acl_member (acl_id, ace_id, pos); --(optional)

INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-remove-index-acl_id', 'Manually executed script upgrade V4.2: Update ALF_ACL_MEMBER_member table. Remove index acl_id',
    0, 6024, -1, 6025, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );