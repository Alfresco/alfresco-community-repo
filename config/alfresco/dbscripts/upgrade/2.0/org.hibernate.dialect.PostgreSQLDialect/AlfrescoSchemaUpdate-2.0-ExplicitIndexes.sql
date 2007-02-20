--
-- Title:      Explicit indexes
-- Database:   PostgreSQL
-- Since:      V2.0 Schema 38
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
-- 
-- PostgreSQL cannot handle optional statements (statements that may fail) in the
-- transaction.
--

-- JBPM tables
DROP INDEX IF EXISTS IDX_PLDACTR_ACTID;
CREATE INDEX IDX_PLDACTR_ACTID ON JBPM_POOLEDACTOR (ACTORID_);
DROP INDEX IF EXISTS IDX_TASK_ACTORID;
CREATE INDEX IDX_TASK_ACTORID ON JBPM_TASKINSTANCE (ACTORID_);

-- Audit tables
DROP INDEX IF EXISTS adt_q_idx;
CREATE INDEX adt_q_idx ON alf_audit_date (quarter);
DROP INDEX IF EXISTS adt_dow_idx;
CREATE INDEX adt_dow_idx ON alf_audit_date (day_of_week);
DROP INDEX IF EXISTS adt_date_idx;
CREATE INDEX adt_date_idx ON alf_audit_date (date_only);
DROP INDEX IF EXISTS adt_y_idx;
CREATE INDEX adt_y_idx ON alf_audit_date (year);
DROP INDEX IF EXISTS adt_hy_idx;
CREATE INDEX adt_hy_idx ON alf_audit_date (halfYear);
DROP INDEX IF EXISTS adt_wom_idx;
CREATE INDEX adt_wom_idx ON alf_audit_date (week_of_month);
DROP INDEX IF EXISTS adt_dom_idx;
CREATE INDEX adt_dom_idx ON alf_audit_date (day_of_month);
DROP INDEX IF EXISTS adt_m_idx;
CREATE INDEX adt_m_idx ON alf_audit_date (month);
DROP INDEX IF EXISTS adt_doy_idx;
CREATE INDEX adt_doy_idx ON alf_audit_date (day_of_year);
DROP INDEX IF EXISTS adt_woy_idx;
CREATE INDEX adt_woy_idx ON alf_audit_date (week_of_year);
DROP INDEX IF EXISTS adt_user_idx;
CREATE INDEX adt_user_idx ON alf_audit_fact (user_id);
DROP INDEX IF EXISTS adt_store_idx;
CREATE INDEX adt_store_idx ON alf_audit_fact (store_protocol, store_id, node_uuid);
DROP INDEX IF EXISTS app_source_met_idx;
CREATE INDEX app_source_met_idx ON alf_audit_source (method);
DROP INDEX IF EXISTS app_source_app_idx;
CREATE INDEX app_source_app_idx ON alf_audit_source (application);
DROP INDEX IF EXISTS app_source_ser_idx;
CREATE INDEX app_source_ser_idx ON alf_audit_source (service);

-- AVM tables: These are new so are not optional
CREATE INDEX idx_avm_np_name ON avm_node_properties (qname);
CREATE INDEX idx_avm_sp_name ON avm_store_properties (qname);
CREATE INDEX idx_avm_vr_version ON avm_version_roots (version_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.0-ExplicitIndexes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.0-ExplicitIndexes', 'Manually executed script upgrade V2.0: Explicit Indexes',
    0, 37, -1, 38, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );