--
-- Title:      Create Content Encryption tables
-- Database:   PostgreSQL
-- Since:      V5.0 Schema 7006
-- Author:     Steve Glover
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE SEQUENCE alf_content_url_enc_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_content_url_encryption
(
   id INT8 NOT NULL,
   content_url_id INT8 NOT NULL,
   algorithm VARCHAR(10) NOT NULL,
   key_size INT4 NOT NULL,
   encrypted_key BYTEA NOT NULL,
   master_keystore_id VARCHAR(20) NOT NULL,
   master_key_alias VARCHAR(15) NOT NULL,
   unencrypted_file_size INT8 NULL,
   CONSTRAINT fk_alf_cont_enc_url FOREIGN KEY (content_url_id) REFERENCES alf_content_url (id) ON DELETE CASCADE,
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_cont_enc_url ON alf_content_url_encryption (content_url_id);
CREATE INDEX idx_alf_cont_enc_mka ON alf_content_url_encryption (master_key_alias);

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