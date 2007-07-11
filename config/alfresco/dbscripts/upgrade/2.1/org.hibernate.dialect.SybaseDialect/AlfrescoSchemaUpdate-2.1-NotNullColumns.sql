--
-- Title:      ADD text columns that allow null
-- Database:   Sybase
-- Since:      V2.1 Schema 64
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Sybase won't allow ALTER TABLE commands where the text columns are not null.
-- Where the nullability is not explicitly set, Sybase assumes NOT NULL.

-- JBPM
ALTER TABLE JBPM_NODE ADD DESCRIPTION_ text NULL;
ALTER TABLE JBPM_NODE ADD ISASYNCEXCL_ tinyint DEFAULT 0;
ALTER TABLE JBPM_NODE ADD SUBPROCNAME_ text NULL;
ALTER TABLE JBPM_NODE ADD SCRIPT_ numeric(19,0) NULL;
ALTER TABLE JBPM_PROCESSDEFINITION ADD CLASS_ char(1) NOT NULL DEFAULT 'P';
ALTER TABLE JBPM_PROCESSDEFINITION ADD DESCRIPTION_ text NULL;
ALTER TABLE JBPM_PROCESSINSTANCE ADD KEY_ text NULL;
ALTER TABLE JBPM_TASK ADD CONDITION_ text NULL;
ALTER TABLE JBPM_TASK ADD PRIORITY_ int DEFAULT 2;
ALTER TABLE JBPM_TASKINSTANCE ADD PROCINST_ numeric(19,0) NULL;
ALTER TABLE JBPM_TOKEN ADD LOCK_ text NULL;
ALTER TABLE JBPM_TRANSITION ADD DESCRIPTION_ text NULL;
ALTER TABLE JBPM_TRANSITION ADD CONDITION_ text NULL;

-- ALFRESCO
ALTER TABLE alf_access_control_entry ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_access_control_list ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_authority ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_child_assoc ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_node ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_node_assoc ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_node_properties ADD attribute_value numeric(19,0) NULL;
ALTER TABLE alf_node_status ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_permission ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_server ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_store ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_transaction ADD version numeric(19,0) DEFAULT 0;
ALTER TABLE alf_version_count ADD version numeric(19,0) DEFAULT 0;


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-NotNullColumns';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-NotNullColumns', 'Manually executed script upgrade V2.1: ADD nullable text columns for Sybase',
    0, 63, -1, 64, null, 'UNKOWN', 1, 1, 'Script completed'
  );