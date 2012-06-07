--
-- Title:      Upgrade to V3.4 - Remove redundant indexes in jbpm tables
-- Database:   Generic
-- Since:      V3.4 schema 4210
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

DROP INDEX IDX_ACTION_ACTNDL ON JBPM_ACTION;    -- (optional)
DROP INDEX IDX_ACTION_EVENT ON JBPM_ACTION;    -- (optional)
DROP INDEX IDX_ACTION_PROCDF ON JBPM_ACTION;    -- (optional)
DROP INDEX IDX_COMMENT_TOKEN ON JBPM_COMMENT;    -- (optional)
DROP INDEX IDX_COMMENT_TSK ON JBPM_COMMENT;    -- (optional)
DROP INDEX IDX_DELEG_PRCD ON JBPM_DELEGATION;    -- (optional)
DROP INDEX IDX_JOB_PRINST ON JBPM_JOB;    -- (optional)
DROP INDEX IDX_JOB_TOKEN ON JBPM_JOB;    -- (optional)
DROP INDEX IDX_JOB_TSKINST ON JBPM_JOB;    -- (optional)
DROP INDEX IDX_MODDEF_PROCDF ON JBPM_MODULEDEFINITION;    -- (optional)
DROP INDEX IDX_MODINST_PRINST ON JBPM_MODULEINSTANCE;    -- (optional)
DROP INDEX IDX_NODE_ACTION ON JBPM_NODE;    -- (optional)
DROP INDEX IDX_NODE_PROCDEF ON JBPM_NODE;    -- (optional)
DROP INDEX IDX_NODE_SUPRSTATE ON JBPM_NODE;    -- (optional)
DROP INDEX IDX_PSTATE_SBPRCDEF ON JBPM_NODE;    -- (optional)
DROP INDEX IDX_PLDACTR_ACTID ON JBPM_POOLEDACTOR;    -- (optional)
DROP INDEX IDX_TSKINST_SWLANE ON JBPM_POOLEDACTOR;    -- (optional)
DROP INDEX IDX_PROCDEF_STRTST ON JBPM_PROCESSDEFINITION;    -- (optional)
DROP INDEX IDX_PROCIN_KEY ON JBPM_PROCESSINSTANCE;    -- (optional)
DROP INDEX IDX_PROCIN_PROCDEF ON JBPM_PROCESSINSTANCE;    -- (optional)
DROP INDEX IDX_PROCIN_ROOTTK ON JBPM_PROCESSINSTANCE;    -- (optional)
DROP INDEX IDX_PROCIN_SPROCTK ON JBPM_PROCESSINSTANCE;    -- (optional)
DROP INDEX IDX_RTACTN_ACTION ON JBPM_RUNTIMEACTION;    -- (optional)
DROP INDEX IDX_RTACTN_PRCINST ON JBPM_RUNTIMEACTION;    -- (optional)
DROP INDEX IDX_SWIMLINST_SL ON JBPM_SWIMLANEINSTANCE;    -- (optional)
DROP INDEX IDX_TASK_PROCDEF ON JBPM_TASK;    -- (optional)
DROP INDEX IDX_TASK_TASKMGTDF ON JBPM_TASK;    -- (optional)
DROP INDEX IDX_TASK_TSKNODE ON JBPM_TASK;    -- (optional)
DROP INDEX IDX_TASKINST_TOKN ON JBPM_TASKINSTANCE;    -- (optional)
DROP INDEX IDX_TASKINST_TSK ON JBPM_TASKINSTANCE;    -- (optional)
DROP INDEX IDX_TASK_ACTORID ON JBPM_TASKINSTANCE;    -- (optional)
DROP INDEX IDX_TSKINST_SLINST ON JBPM_TASKINSTANCE;    -- (optional)
DROP INDEX IDX_TSKINST_TMINST ON JBPM_TASKINSTANCE;    -- (optional)
DROP INDEX IDX_TOKEN_NODE ON JBPM_TOKEN;    -- (optional)
DROP INDEX IDX_TOKEN_PARENT ON JBPM_TOKEN;    -- (optional)
DROP INDEX IDX_TOKEN_PROCIN ON JBPM_TOKEN;    -- (optional)
DROP INDEX IDX_TOKEN_SUBPI ON JBPM_TOKEN;    -- (optional)
DROP INDEX IDX_TKVARMAP_CTXT ON JBPM_TOKENVARIABLEMAP;    -- (optional)
DROP INDEX IDX_TKVVARMP_TOKEN ON JBPM_TOKENVARIABLEMAP;    -- (optional)
DROP INDEX IDX_TRANSIT_FROM ON JBPM_TRANSITION;    -- (optional)
DROP INDEX IDX_TRANSIT_TO ON JBPM_TRANSITION;    -- (optional)
DROP INDEX IDX_TRANS_PROCDEF ON JBPM_TRANSITION;    -- (optional)
DROP INDEX IDX_VARINST_PRCINS ON JBPM_VARIABLEINSTANCE;    -- (optional)
DROP INDEX IDX_VARINST_TK ON JBPM_VARIABLEINSTANCE;    -- (optional)
DROP INDEX IDX_VARINST_TKVARMP ON JBPM_VARIABLEINSTANCE;    -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-remove-redundant-jbpm-indexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-remove-redundant-jbpm-indexes', 'Manually executed script upgrade V3.4',
     0, 6010, -1, 6011, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
