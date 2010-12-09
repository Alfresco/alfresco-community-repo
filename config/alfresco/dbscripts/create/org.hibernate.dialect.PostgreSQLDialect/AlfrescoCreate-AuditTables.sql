--
-- Title:      Audit tables
-- Database:   PostgreSql
-- Since:      V3.2 Schema 3002
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE SEQUENCE alf_audit_model_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_audit_model
(
   id INT8 NOT NULL,
   content_data_id INT8 NOT NULL,
   content_crc INT8 NOT NULL,   
   CONSTRAINT fk_alf_aud_mod_cd FOREIGN KEY (content_data_id) REFERENCES alf_content_data (id),
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_aud_mod_cr ON alf_audit_model(content_crc);
CREATE INDEX fk_alf_aud_mod_cd ON alf_audit_model(content_data_id);

CREATE SEQUENCE alf_audit_app_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_audit_app
(
   id INT8 NOT NULL,
   version INT4 NOT NULL,
   app_name_id INT8 NOT NULL CONSTRAINT idx_alf_aud_app_an UNIQUE,
   audit_model_id INT8 NOT NULL,
   disabled_paths_id INT8 NOT NULL,
   CONSTRAINT fk_alf_aud_app_an FOREIGN KEY (app_name_id) REFERENCES alf_prop_value (id),   
   CONSTRAINT fk_alf_aud_app_mod FOREIGN KEY (audit_model_id) REFERENCES alf_audit_model (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_aud_app_dis FOREIGN KEY (disabled_paths_id) REFERENCES alf_prop_root (id),
   PRIMARY KEY (id)
);
CREATE INDEX fk_alf_aud_app_mod ON alf_audit_app(audit_model_id);
CREATE INDEX fk_alf_aud_app_dis ON alf_audit_app(disabled_paths_id);

CREATE SEQUENCE alf_audit_entry_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_audit_entry
(
   id INT8 NOT NULL,
   audit_app_id INT8 NOT NULL,
   audit_time INT8 NOT NULL,
   audit_user_id INT8 NULL,
   audit_values_id INT8 NULL,
   CONSTRAINT fk_alf_aud_ent_app FOREIGN KEY (audit_app_id) REFERENCES alf_audit_app (id) ON DELETE CASCADE,   
   CONSTRAINT fk_alf_aud_ent_use FOREIGN KEY (audit_user_id) REFERENCES alf_prop_value (id),
   CONSTRAINT fk_alf_aud_ent_pro FOREIGN KEY (audit_values_id) REFERENCES alf_prop_root (id),
   PRIMARY KEY (id)
);
CREATE INDEX idx_alf_aud_ent_tm ON alf_audit_entry(audit_time);
CREATE INDEX fk_alf_aud_ent_app ON alf_audit_entry(audit_app_id);
CREATE INDEX fk_alf_aud_ent_use ON alf_audit_entry(audit_user_id);
CREATE INDEX fk_alf_aud_ent_pro ON alf_audit_entry(audit_values_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-AuditTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-AuditTables', 'Manually executed script upgrade V3.2: Audit Tables',
    0, 3001, -1, 3002, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );