--
-- Title:      Create Content tables
-- Database:   PostgreSQL
-- Since:      V3.2 Schema 2012
-- Author:     
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE SEQUENCE alf_mimetype_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_mimetype
(
   id INT8 NOT NULL,
   version INT8 NOT NULL,
   mimetype_str VARCHAR(100) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (mimetype_str)
);

CREATE SEQUENCE alf_encoding_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_encoding
(
   id INT8 NOT NULL,
   version INT8 NOT NULL,
   encoding_str VARCHAR(100) NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (encoding_str)
);

-- This table may exist during upgrades, but must be removed.
-- The drop statement is therefore optional.
DROP TABLE alf_content_url;                     --(optional)
CREATE SEQUENCE alf_content_url_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_content_url
(
   id INT8 NOT NULL,
   content_url VARCHAR(255) NOT NULL,
   content_url_short VARCHAR(12) NOT NULL,
   content_url_crc INT8 NOT NULL,
   content_size INT8 NOT NULL,
   orphan_time INT8 NULL,
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_conturl_cr ON alf_content_url (content_url_short, content_url_crc);
CREATE INDEX idx_alf_conturl_ot ON alf_content_url (orphan_time);
CREATE INDEX idx_alf_conturl_sz ON alf_content_url (content_size, id);

CREATE SEQUENCE alf_content_data_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_content_data
(
   id INT8 NOT NULL,
   version INT8 NOT NULL,
   content_url_id INT8 NULL,
   content_mimetype_id INT8 NULL,
   content_encoding_id INT8 NULL,
   content_locale_id INT8 NULL,
   CONSTRAINT fk_alf_cont_url FOREIGN KEY (content_url_id) REFERENCES alf_content_url (id),
   CONSTRAINT fk_alf_cont_mim FOREIGN KEY (content_mimetype_id) REFERENCES alf_mimetype (id),
   CONSTRAINT fk_alf_cont_enc FOREIGN KEY (content_encoding_id) REFERENCES alf_encoding (id),
   CONSTRAINT fk_alf_cont_loc FOREIGN KEY (content_locale_id) REFERENCES alf_locale (id),
   PRIMARY KEY (id)
);
CREATE INDEX fk_alf_cont_url ON alf_content_data (content_url_id);
CREATE INDEX fk_alf_cont_mim ON alf_content_data (content_mimetype_id);
CREATE INDEX fk_alf_cont_enc ON alf_content_data (content_encoding_id);
CREATE INDEX fk_alf_cont_loc ON alf_content_data (content_locale_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-ContentTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-ContentTables', 'Manually executed script upgrade V3.2: Content Tables',
    0, 2011, -1, 2012, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );
