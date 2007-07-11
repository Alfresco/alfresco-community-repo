--
-- Title:      Ensure that all expicit indexes are present
-- Database:   Generic
-- Since:      V2.1 Schema 64
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- During an upgrade, explicit indexes are not added.  This script ensures that
-- all explicit indexes that would be available after a clean boot are also
-- available for an upgrade.

-- JBPM
SELECT COUNT(*) FROM JBPM_ACTION;
CREATE INDEX IDX_ACTION_EVENT ON JBPM_ACTION (EVENT_);(optional)
CREATE INDEX IDX_ACTION_ACTNDL ON JBPM_ACTION (ACTIONDELEGATION_);(optional)
CREATE INDEX IDX_ACTION_PROCDF ON JBPM_ACTION (PROCESSDEFINITION_);(optional)
SELECT COUNT(*) FROM JBPM_COMMENT;
CREATE INDEX IDX_COMMENT_TOKEN ON JBPM_COMMENT (TOKEN_);(optional)
CREATE INDEX IDX_COMMENT_TSK ON JBPM_COMMENT (TASKINSTANCE_);(optional)
SELECT COUNT(*) FROM JBPM_DELEGATION;
CREATE INDEX IDX_DELEG_PRCD ON JBPM_DELEGATION (PROCESSDEFINITION_);(optional)
SELECT COUNT(*) FROM JBPM_JOB;
CREATE INDEX IDX_JOB_TSKINST ON JBPM_JOB (TASKINSTANCE_);(optional)
CREATE INDEX IDX_JOB_PRINST ON JBPM_JOB (PROCESSINSTANCE_);(optional)
CREATE INDEX IDX_JOB_TOKEN ON JBPM_JOB (TOKEN_);(optional)
SELECT COUNT(*) FROM JBPM_MODULEDEFINITION;
CREATE INDEX IDX_MODDEF_PROCDF ON JBPM_MODULEDEFINITION (PROCESSDEFINITION_);(optional)
SELECT COUNT(*) FROM JBPM_MODULEINSTANCE;
CREATE INDEX IDX_MODINST_PRINST ON JBPM_MODULEINSTANCE (PROCESSINSTANCE_);(optional)
SELECT COUNT(*) FROM JBPM_NODE;
CREATE INDEX IDX_PSTATE_SBPRCDEF ON JBPM_NODE (SUBPROCESSDEFINITION_);(optional)
CREATE INDEX IDX_NODE_SUPRSTATE ON JBPM_NODE (SUPERSTATE_);(optional)
CREATE INDEX IDX_NODE_PROCDEF ON JBPM_NODE (PROCESSDEFINITION_);(optional)
CREATE INDEX IDX_NODE_ACTION ON JBPM_NODE (ACTION_);(optional)
SELECT COUNT(*) FROM JBPM_POOLEDACTOR;
CREATE INDEX IDX_PLDACTR_ACTID ON JBPM_POOLEDACTOR (ACTORID_);(optional)
CREATE INDEX IDX_TSKINST_SWLANE ON JBPM_POOLEDACTOR (SWIMLANEINSTANCE_);(optional)
SELECT COUNT(*) FROM JBPM_PROCESSDEFINITION;
CREATE INDEX IDX_PROCDEF_STRTST ON JBPM_PROCESSDEFINITION (STARTSTATE_);(optional)
SELECT COUNT(*) FROM JBPM_PROCESSINSTANCE;
CREATE INDEX IDX_PROCIN_ROOTTK ON JBPM_PROCESSINSTANCE (ROOTTOKEN_);(optional)
CREATE INDEX IDX_PROCIN_SPROCTK ON JBPM_PROCESSINSTANCE (SUPERPROCESSTOKEN_);(optional)
CREATE INDEX IDX_PROCIN_PROCDEF ON JBPM_PROCESSINSTANCE (PROCESSDEFINITION_);(optional)
SELECT COUNT(*) FROM JBPM_RUNTIMEACTION;
CREATE INDEX IDX_RTACTN_PRCINST ON JBPM_RUNTIMEACTION (PROCESSINSTANCE_);(optional)
CREATE INDEX IDX_RTACTN_ACTION ON JBPM_RUNTIMEACTION (ACTION_);(optional)
SELECT COUNT(*) FROM JBPM_SWIMLANEINSTANCE;
CREATE INDEX IDX_SWIMLINST_SL ON JBPM_SWIMLANEINSTANCE (SWIMLANE_);(optional)
SELECT COUNT(*) FROM JBPM_TASK;
CREATE INDEX IDX_TASK_TSKNODE ON JBPM_TASK (TASKNODE_);(optional)
CREATE INDEX IDX_TASK_PROCDEF ON JBPM_TASK (PROCESSDEFINITION_);(optional)
CREATE INDEX IDX_TASK_TASKMGTDF ON JBPM_TASK (TASKMGMTDEFINITION_);(optional)
SELECT COUNT(*) FROM JBPM_TASKINSTANCE;
CREATE INDEX IDX_TASKINST_TOKN ON JBPM_TASKINSTANCE (TOKEN_);(optional)
CREATE INDEX IDX_TASKINST_TSK ON JBPM_TASKINSTANCE (TASK_, PROCINST_);(optional)
CREATE INDEX IDX_TSKINST_TMINST ON JBPM_TASKINSTANCE (TASKMGMTINSTANCE_);(optional)
CREATE INDEX IDX_TSKINST_SLINST ON JBPM_TASKINSTANCE (SWIMLANINSTANCE_);(optional)
CREATE INDEX IDX_TASK_ACTORID ON JBPM_TASKINSTANCE (ACTORID_);(optional)
SELECT COUNT(*) FROM JBPM_TOKEN;
CREATE INDEX IDX_TOKEN_PROCIN ON JBPM_TOKEN (PROCESSINSTANCE_);(optional)
CREATE INDEX IDX_TOKEN_SUBPI ON JBPM_TOKEN (SUBPROCESSINSTANCE_);(optional)
CREATE INDEX IDX_TOKEN_NODE ON JBPM_TOKEN (NODE_);(optional)
CREATE INDEX IDX_TOKEN_PARENT ON JBPM_TOKEN (PARENT_);(optional)
SELECT COUNT(*) FROM JBPM_TOKENVARIABLEMAP;
CREATE INDEX IDX_TKVARMAP_CTXT ON JBPM_TOKENVARIABLEMAP (CONTEXTINSTANCE_);(optional)
CREATE INDEX IDX_TKVVARMP_TOKEN ON JBPM_TOKENVARIABLEMAP (TOKEN_);(optional)
SELECT COUNT(*) FROM JBPM_TRANSITION;
CREATE INDEX IDX_TRANSIT_TO ON JBPM_TRANSITION (TO_);(optional)
CREATE INDEX IDX_TRANSIT_FROM ON JBPM_TRANSITION (FROM_);(optional)
CREATE INDEX IDX_TRANS_PROCDEF ON JBPM_TRANSITION (PROCESSDEFINITION_);(optional)
SELECT COUNT(*) FROM JBPM_VARIABLEINSTANCE;
CREATE INDEX IDX_VARINST_TKVARMP ON JBPM_VARIABLEINSTANCE (TOKENVARIABLEMAP_);(optional)
CREATE INDEX IDX_VARINST_PRCINS ON JBPM_VARIABLEINSTANCE (PROCESSINSTANCE_);(optional)
CREATE INDEX IDX_VARINST_TK ON JBPM_VARIABLEINSTANCE (TOKEN_);(optional)

SELECT COUNT(*) FROM alf_audit_date;
CREATE INDEX adt_q_idx ON alf_audit_date (quarter);(optional)
CREATE INDEX adt_dow_idx ON alf_audit_date (day_of_week);(optional)
CREATE INDEX adt_date_idx ON alf_audit_date (date_only);(optional)
CREATE INDEX adt_y_idx ON alf_audit_date (year);(optional)
CREATE INDEX adt_hy_idx ON alf_audit_date (halfYear);(optional)
CREATE INDEX adt_wom_idx ON alf_audit_date (week_of_month);(optional)
CREATE INDEX adt_dom_idx ON alf_audit_date (day_of_month);(optional)
CREATE INDEX adt_m_idx ON alf_audit_date (month);(optional)
CREATE INDEX adt_doy_idx ON alf_audit_date (day_of_year);(optional)
CREATE INDEX adt_woy_idx ON alf_audit_date (week_of_year);(optional)
SELECT COUNT(*) FROM alf_audit_fact;
CREATE INDEX adt_user_idx ON alf_audit_fact (user_id);(optional)
CREATE INDEX adt_store_idx ON alf_audit_fact (store_protocol, store_id, node_uuid);(optional)
SELECT COUNT(*) FROM alf_audit_source;
CREATE INDEX app_source_met_idx ON alf_audit_source (method);(optional)
CREATE INDEX app_source_app_idx ON alf_audit_source (application);(optional)
CREATE INDEX app_source_ser_idx ON alf_audit_source (service);(optional)
 
SELECT COUNT(*) FROM avm_node_properties;
CREATE INDEX idx_avm_np_name ON avm_node_properties (qname);(optional)
SELECT COUNT(*) FROM avm_store_properties;
CREATE INDEX idx_avm_sp_name ON avm_store_properties (qname);(optional)
SELECT COUNT(*) FROM avm_version_roots;
CREATE INDEX idx_avm_vr_version ON avm_version_roots (version_id);(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-ExplicitIndexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-ExplicitIndexes', 'Manually executed script upgrade V2.1: Ensure existence of V2.1 Explicit indexes',
    0, 63, -1, 64, null, 'UNKOWN', 1, 1, 'Script completed'
  );