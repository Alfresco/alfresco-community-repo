--
-- Title:      Jbpm 3.2 Process Instance Key
-- Database:   Generic
-- Since:      V2.1 Schema 63
-- Author:     David Caruana
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
-- 

UPDATE JBPM_PROCESSINSTANCE SET KEY_ = ID_ WHERE KEY_ IS NULL;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-JBPMProcessKey';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-JBPMProcessKey', 'Manually executed script upgrade V2.1: JBPM 3.2 Process Instance Key',
    0, 62, -1, 63, null, 'UNKOWN', 1, 1, 'Script completed'
  );