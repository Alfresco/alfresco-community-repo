--
-- Title:      Audit tables
-- Database:   MySQL InnoDB
-- Since:      V3.2 Schema 3002
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_audit_model
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   content_data_id BIGINT NOT NULL,
   content_crc BIGINT NOT NULL,
   UNIQUE INDEX idx_alf_audit_cfg_crc (content_crc),
   CONSTRAINT fk_alf_audit_model_cd FOREIGN KEY (content_data_id) REFERENCES alf_content_data (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_session
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   audit_model_id BIGINT NOT NULL,
   app_name_id BIGINT NOT NULL,
   CONSTRAINT fk_alf_audit_sess_model FOREIGN KEY (audit_model_id) REFERENCES alf_audit_model (id),
   CONSTRAINT fk_alf_audit_sess_app FOREIGN KEY (app_name_id) REFERENCES alf_prop_value (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_entry
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   audit_session_id BIGINT NOT NULL,
   audit_time BIGINT NOT NULL,
   audit_user_id BIGINT NULL,
   audit_values_id BIGINT NULL,
   CONSTRAINT fk_alf_audit_ent_sess FOREIGN KEY (audit_session_id) REFERENCES alf_audit_session (id),
   INDEX idx_alf_audit_ent_time (audit_time),
   CONSTRAINT fk_alf_audit_ent_user FOREIGN KEY (audit_user_id) REFERENCES alf_prop_value (id),
   CONSTRAINT fk_alf_audit_ent_prop FOREIGN KEY (audit_values_id) REFERENCES alf_prop_value (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-AuditTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-AuditTables', 'Manually executed script upgrade V3.2: Audit Tables',
    0, 3001, -1, 3002, null, 'UNKOWN', 1, 1, 'Script completed'
  );