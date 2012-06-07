--
-- Title:      Upgrade to V3.4 - Remove redundant indexes in jbpm tables
-- Database:   PostgreSQL
-- Since:      V3.4 schema 4210
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

DROP INDEX IDX_ACTION_ACTNDL;    -- (optional)
DROP INDEX IDX_ACTION_EVENT;    -- (optional)
DROP INDEX IDX_ACTION_PROCDF;    -- (optional)
DROP INDEX IDX_COMMENT_TOKEN;    -- (optional)
DROP INDEX IDX_COMMENT_TSK;    -- (optional)
DROP INDEX IDX_DELEG_PRCD;    -- (optional)
DROP INDEX IDX_JOB_PRINST;    -- (optional)
DROP INDEX IDX_JOB_TOKEN;    -- (optional)
DROP INDEX IDX_JOB_TSKINST;    -- (optional)
DROP INDEX IDX_MODDEF_PROCDF;    -- (optional)
DROP INDEX IDX_MODINST_PRINST;    -- (optional)
DROP INDEX IDX_NODE_ACTION;    -- (optional)
DROP INDEX IDX_NODE_PROCDEF;    -- (optional)
DROP INDEX IDX_NODE_SUPRSTATE;    -- (optional)
DROP INDEX IDX_PSTATE_SBPRCDEF;    -- (optional)
DROP INDEX IDX_PLDACTR_ACTID;    -- (optional)
DROP INDEX IDX_TSKINST_SWLANE;    -- (optional)
DROP INDEX IDX_PROCDEF_STRTST;    -- (optional)
DROP INDEX IDX_PROCIN_KEY;    -- (optional)
DROP INDEX IDX_PROCIN_PROCDEF;    -- (optional)
DROP INDEX IDX_PROCIN_ROOTTK;    -- (optional)
DROP INDEX IDX_PROCIN_SPROCTK;    -- (optional)
DROP INDEX IDX_RTACTN_ACTION;    -- (optional)
DROP INDEX IDX_RTACTN_PRCINST;    -- (optional)
DROP INDEX IDX_SWIMLINST_SL;    -- (optional)
DROP INDEX IDX_TASK_PROCDEF;    -- (optional)
DROP INDEX IDX_TASK_TASKMGTDF;    -- (optional)
DROP INDEX IDX_TASK_TSKNODE;    -- (optional)
DROP INDEX IDX_TASKINST_TOKN;    -- (optional)
DROP INDEX IDX_TASKINST_TSK;    -- (optional)
DROP INDEX IDX_TASK_ACTORID;    -- (optional)
DROP INDEX IDX_TSKINST_SLINST;    -- (optional)
DROP INDEX IDX_TSKINST_TMINST;    -- (optional)
DROP INDEX IDX_TOKEN_NODE;    -- (optional)
DROP INDEX IDX_TOKEN_PARENT;    -- (optional)
DROP INDEX IDX_TOKEN_PROCIN;    -- (optional)
DROP INDEX IDX_TOKEN_SUBPI;    -- (optional)
DROP INDEX IDX_TKVARMAP_CTXT;    -- (optional)
DROP INDEX IDX_TKVVARMP_TOKEN;    -- (optional)
DROP INDEX IDX_TRANSIT_FROM;    -- (optional)
DROP INDEX IDX_TRANSIT_TO;    -- (optional)
DROP INDEX IDX_TRANS_PROCDEF;    -- (optional)
DROP INDEX IDX_VARINST_PRCINS;    -- (optional)
DROP INDEX IDX_VARINST_TK;    -- (optional)
DROP INDEX IDX_VARINST_TKVARMP;    -- (optional)

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
