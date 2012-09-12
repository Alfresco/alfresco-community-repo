--
-- Title:      Upgrade to V3.4 - Add indexes for jbpm foreign keys
-- Database:   Generic
-- Since:      V3.4 schema 4206
-- Author:     dward
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX IDX_VARINST_STRVAL ON JBPM_VARIABLEINSTANCE (NAME_, CLASS_, STRINGVALUE_, TOKENVARIABLEMAP_); --(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-JBPM-varinst-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-JBPM-varinst-indexes', 'Manually executed script upgrade to add FK indexes for JBPM',
     0, 6016, -1, 6017, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
