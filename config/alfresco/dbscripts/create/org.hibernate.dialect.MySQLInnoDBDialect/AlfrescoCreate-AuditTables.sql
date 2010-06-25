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
   UNIQUE INDEX idx_alf_aud_mod_cr (content_crc),
   CONSTRAINT fk_alf_aud_mod_cd FOREIGN KEY (content_data_id) REFERENCES alf_content_data (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_app
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version SMALLINT NOT NULL,
   app_name_id BIGINT NOT NULL,
   audit_model_id BIGINT NOT NULL,
   disabled_paths_id BIGINT NOT NULL,
   CONSTRAINT fk_alf_aud_app_an FOREIGN KEY (app_name_id) REFERENCES alf_prop_value (id),
   CONSTRAINT UNIQUE idx_alf_aud_app_an (app_name_id),
   CONSTRAINT fk_alf_aud_app_mod FOREIGN KEY (audit_model_id) REFERENCES alf_audit_model (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_aud_app_dis FOREIGN KEY (disabled_paths_id) REFERENCES alf_prop_root (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_audit_entry
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   audit_app_id BIGINT NOT NULL,
   audit_time BIGINT NOT NULL,
   audit_user_id BIGINT NULL,
   audit_values_id BIGINT NULL,
   CONSTRAINT fk_alf_aud_ent_app FOREIGN KEY (audit_app_id) REFERENCES alf_audit_app (id) ON DELETE CASCADE,
   INDEX idx_alf_aud_ent_tm (audit_time),
   CONSTRAINT fk_alf_aud_ent_use FOREIGN KEY (audit_user_id) REFERENCES alf_prop_value (id),
   CONSTRAINT fk_alf_aud_ent_pro FOREIGN KEY (audit_values_id) REFERENCES alf_prop_root (id),
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
    0, 3001, -1, 3002, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );