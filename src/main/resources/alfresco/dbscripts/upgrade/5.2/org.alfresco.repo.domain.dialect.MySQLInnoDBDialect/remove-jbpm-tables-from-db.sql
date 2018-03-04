--
-- Title:      Remove all JBPM related data from the database
-- Database:   MySQL InnoDB
-- Since:      V5.2
-- Author:     Stefan Kopf
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Drop all tables
SET foreign_key_checks = 0;
DROP TABLE IF EXISTS JBPM_ACTION, JBPM_BYTEARRAY, JBPM_BYTEBLOCK, JBPM_COMMENT, JBPM_DECISIONCONDITIONS, JBPM_DELEGATION,
                     JBPM_EVENT, JBPM_EXCEPTIONHANDLER, JBPM_JOB, JBPM_LOG, JBPM_MODULEDEFINITION, JBPM_MODULEINSTANCE,
                     JBPM_NODE, JBPM_POOLEDACTOR, JBPM_PROCESSDEFINITION, JBPM_PROCESSINSTANCE, JBPM_RUNTIMEACTION, JBPM_SWIMLANE,
                     JBPM_SWIMLANEINSTANCE, JBPM_TASK, JBPM_TASKACTORPOOL, JBPM_TASKCONTROLLER, JBPM_TASKINSTANCE, JBPM_TOKEN,
                     JBPM_TOKENVARIABLEMAP, JBPM_TRANSITION, JBPM_VARIABLEACCESS, JBPM_VARIABLEINSTANCE;
SET foreign_key_checks = 1;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.2-remove-jbpm-tables-from-db';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.2-remove-jbpm-tables-from-db', 'Removes all JBPM related tables from the database.',
    0, 10051, -1, 10052, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );