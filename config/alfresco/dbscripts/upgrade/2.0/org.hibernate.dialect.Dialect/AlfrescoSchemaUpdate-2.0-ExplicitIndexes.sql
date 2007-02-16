--
-- Title:      Explicit indexes
-- Database:   Generic
-- Since:      V2.0 Schema 38
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
-- 
-- None of the Hibernate dialects will generate statements for explicit indexes
-- when creating new tables for an existing database.  We need to check that the
-- indexes for the AUDIT, JBPM and AVM tables are present.
--
-- This script must be executed after Hibernate-generated scripts have been executed.
-- Where the updates are being applied long after the tables may have been created,
-- the index creation statements are optional.  The the select statements ensure that
-- the script is only executed once the appropriate tables have been created.
--

-- JBPM tables
SELECT COUNT(*) FROM JBPM_POOLEDACTOR;
CREATE INDEX IDX_PLDACTR_ACTID ON JBPM_POOLEDACTOR (ACTORID_);(optional)
SELECT COUNT(*) FROM JBPM_TASKINSTANCE;
CREATE INDEX IDX_TASK_ACTORID ON JBPM_TASKINSTANCE (ACTORID_);(optional)

-- Audit tables
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
    0, 37, -1, 38, null, 'UNKOWN', 1, 1, 'Script completed'
  );