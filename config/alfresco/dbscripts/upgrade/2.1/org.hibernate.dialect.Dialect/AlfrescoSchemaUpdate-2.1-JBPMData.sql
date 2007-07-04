--
-- Title:      Jbpm 3.1.2 -> 3.2 Data Migration
-- Database:   Generic
-- Since:      V2.1 Schema 52
-- Author:     David Caruana
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

UPDATE JBPM_TASK SET PRIORITY_ = 2;
UPDATE JBPM_NODE SET ISASYNCEXCL__ = 0;
UPDATE JBPM_MODULEINSTANCE SET VERSION_ = 0;
UPDATE JBPM_POOLEDACTOR SET VERSION_ = 0;
UPDATE JBPM_SWIMLANEINSTANCE SET VERSION_ = 0;
UPDATE JBPM_TASKINSTANCE SET VERSION_ = 0;
UPDATE JBPM_TOKENVARIABLEMAP SET VERSION_ = 0;
UPDATE JBPM_VARIABLEINSTANCE SET VERSION_ = 0;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-JBPMUpdate';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-JBPMUpdate', 'Manually executed script upgrade V2.1: JBPM 3.1.2 to 3.2 Data Upgrade',
    0, 51, -1, 52, null, 'UNKOWN', 1, 1, 'Script completed'
  );