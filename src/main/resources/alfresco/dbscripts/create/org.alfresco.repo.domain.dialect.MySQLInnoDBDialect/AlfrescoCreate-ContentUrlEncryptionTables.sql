--
-- Title:      Create Content Encryption tables
-- Database:   MySQL InnoDB
-- Since:      V5.0 Schema 7006
-- Author:     Steve Glover
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_content_url_encryption
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   content_url_id BIGINT NOT NULL,
   algorithm VARCHAR(10) NOT NULL,
   key_size INTEGER NOT NULL,
   encrypted_key BLOB NOT NULL,
   master_keystore_id VARCHAR(20) NOT NULL,
   master_key_alias VARCHAR(15) NOT NULL,
   unencrypted_file_size BIGINT NULL,
   UNIQUE INDEX idx_alf_cont_enc_url (content_url_id),
   INDEX idx_alf_cont_enc_mka (master_key_alias),
   CONSTRAINT fk_alf_cont_enc_url FOREIGN KEY (content_url_id) REFERENCES alf_content_url (id) ON DELETE CASCADE,
   PRIMARY KEY (id)
) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V5.0-ContentUrlEncryptionTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V5.0-ContentUrlEncryptionTables', 'Manually executed script upgrade V5.0: Content Url Encryption Tables',
    0, 8001, -1, 8002, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );