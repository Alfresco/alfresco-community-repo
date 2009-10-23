--
-- Title:      Upgrade to V3.2 - upgrade jbpm tables to jbpm 3.3.1 
-- Database:   PostgreSQL
-- Since:      V3.2 schema 2013
-- Author:     
--
-- upgrade jbpm tables to jbpm 3.3.1
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- we mark next statement as optional to not fail the upgrade from 2.1.a  (as it doesn't contain jbpm)
alter table JBPM_ACTION alter column EXPRESSION_ type text; -- (optional)
alter table JBPM_COMMENT alter column MESSAGE_ type text; -- (optional)
alter table JBPM_DELEGATION alter column CLASSNAME_ type text; -- (optional)
alter table JBPM_DELEGATION alter column CONFIGURATION_ type text; -- (optional)
alter table JBPM_EXCEPTIONHANDLER alter column EXCEPTIONCLASSNAME_ type text; -- (optional)
alter table JBPM_JOB alter column EXCEPTION_ type text; -- (optional)
alter table JBPM_LOG alter column MESSAGE_ type text,
   alter column EXCEPTION_ type text,
   alter column OLDSTRINGVALUE_ type text,
   alter column NEWSTRINGVALUE_ type text; -- (optional)
alter table JBPM_MODULEDEFINITION alter column NAME_ type varchar(255); -- (optional)
alter table JBPM_NODE alter column DESCRIPTION_ type text; -- (optional)
alter table JBPM_PROCESSDEFINITION alter column DESCRIPTION_ type text; -- (optional)
alter table JBPM_TASK alter column DESCRIPTION_ type text; -- (optional)
alter table JBPM_TASKINSTANCE alter column DESCRIPTION_ type text; -- (optional)
alter table JBPM_TRANSITION alter column DESCRIPTION_ type text; -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-Upgrade-JBPM';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-Upgrade-JBPM', 'Manually executed script upgrade V3.2 to jbpm version 3.3.1 usage',
     0, 2017, -1, 2018, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
   );