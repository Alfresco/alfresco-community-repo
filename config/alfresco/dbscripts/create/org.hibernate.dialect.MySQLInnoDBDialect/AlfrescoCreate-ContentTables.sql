--
-- Title:      Create Content tables
-- Database:   MySQL InnoDB
-- Since:      V3.2 Schema 2012
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_mimetype
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   mimetype_str VARCHAR(100) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (mimetype_str)
) ENGINE=InnoDB;

CREATE TABLE alf_encoding
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   encoding_str VARCHAR(100) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (encoding_str)
) ENGINE=InnoDB;

-- This table may exist during upgrades, but must be removed.
-- The drop statement is therefore optional.
DROP TABLE alf_content_url;                     --(optional)
CREATE TABLE alf_content_url
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   content_url VARCHAR(255) NOT NULL,
   content_url_short VARCHAR(12) NOT NULL,
   content_url_crc BIGINT NOT NULL,
   content_size BIGINT NOT NULL,
   orphan_time BIGINT NULL,
   UNIQUE INDEX idx_alf_conturl_cr (content_url_short, content_url_crc),
   INDEX idx_alf_conturl_ot (orphan_time),
   INDEX idx_alf_conturl_sz (content_size),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_content_data
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   content_url_id BIGINT NULL,
   content_mimetype_id BIGINT NULL,
   content_encoding_id BIGINT NULL,
   content_locale_id BIGINT NULL,
   CONSTRAINT fk_alf_cont_url FOREIGN KEY (content_url_id) REFERENCES alf_content_url (id),
   CONSTRAINT fk_alf_cont_mim FOREIGN KEY (content_mimetype_id) REFERENCES alf_mimetype (id),
   CONSTRAINT fk_alf_cont_enc FOREIGN KEY (content_encoding_id) REFERENCES alf_encoding (id),
   CONSTRAINT fk_alf_cont_loc FOREIGN KEY (content_locale_id) REFERENCES alf_locale (id),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-ContentTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-ContentTables', 'Manually executed script upgrade V3.2: Content Tables',
    0, 2011, -1, 2012, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );