--
-- Title:      Audit tables
-- Database:   MySQL InnoDB
-- Since:      V3.2 Schema 3002
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_audit_cfg
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   content_data_id BIGINT NOT NULL,
   content_crc BIGINT NOT NULL,
   UNIQUE INDEX idx_alf_audit_cfg_crc (content_crc),
   CONSTRAINT fk_alf_audit_cfg_cd FOREIGN KEY (content_data_id) REFERENCES alf_content_data (id),
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