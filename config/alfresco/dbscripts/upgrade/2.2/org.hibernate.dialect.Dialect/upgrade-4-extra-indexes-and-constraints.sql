--
-- Title:      Create additional indexes and constraints
-- Database:   Generic
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE INDEX idx_alf_adtd_woy ON alf_audit_date (week_of_year);

CREATE INDEX idx_alf_adtd_q ON alf_audit_date (quarter);

CREATE INDEX idx_alf_adtd_wom ON alf_audit_date (week_of_month);

CREATE INDEX idx_alf_adtd_dom ON alf_audit_date (day_of_month);

CREATE INDEX idx_alf_adtd_doy ON alf_audit_date (day_of_year);

CREATE INDEX idx_alf_adtd_dow ON alf_audit_date (day_of_week);

CREATE INDEX idx_alf_adtd_m ON alf_audit_date (month);

CREATE INDEX idx_alf_adtd_hy ON alf_audit_date (half_year);

CREATE INDEX idx_alf_adtd_fy ON alf_audit_date (full_year);

CREATE INDEX idx_alf_adtd_dat ON alf_audit_date (date_only);

CREATE INDEX idx_alf_adtf_ref ON alf_audit_fact (store_protocol, store_id, node_uuid);

CREATE INDEX idx_alf_adtf_usr ON alf_audit_fact (user_id);

CREATE INDEX idx_alf_adts_met ON alf_audit_source (method);

CREATE INDEX idx_alf_adts_ser ON alf_audit_source (service);

CREATE INDEX idx_alf_adts_app ON alf_audit_source (application);

CREATE INDEX idx_alf_ca_qn_ln ON alf_child_assoc (qname_localname);

CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms);

 -- The was 'idx_avm_lyr_indn'.  Rename it if you have the old name.
CREATE INDEX idx_avm_n_pi on avm_nodes (primary_indirection);

CREATE INDEX idx_avm_np_name ON avm_node_properties (qname);

CREATE INDEX idx_avm_vr_version ON avm_version_roots (version_id);

--
-- Explicit indexes and constraints not declared in the mappings
--

CREATE INDEX idx_alf_acl_inh ON alf_access_control_list (inherits, inherits_from);

CREATE INDEX fk_alf_na_qn ON alf_node_aspects (qname_id);
ALTER TABLE alf_node_aspects ADD CONSTRAINT fk_alf_na_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_np_qn ON alf_node_properties (qname_id);
ALTER TABLE alf_node_properties ADD CONSTRAINT fk_alf_np_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_avm_na_qn ON avm_aspects_new (qname_id);
ALTER TABLE avm_aspects_new ADD CONSTRAINT fk_avm_na_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_avm_np_qn ON avm_node_properties_new (qname_id);
ALTER TABLE avm_node_properties_new ADD CONSTRAINT fk_avm_np_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX idx_avm_hl_revpk ON avm_history_links (descendent, ancestor);

CREATE INDEX idx_avm_vr_revuq ON avm_version_roots (avm_store_id, version_id); 

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-4-ExtraIndexesAndConstraints';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-4-ExtraIndexesAndConstraints', 'Manually executed script upgrade V2.2: Created additional indexes and constraints',
    0, 120, -1, 121, null, 'UNKOWN', 1, 1, 'Script completed'
  );
