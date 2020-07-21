--
-- Title:      Create Authorization Status
-- Database:   PostgreSQL
-- Since:      V4.1.11 Schema 5156
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE SEQUENCE alf_auth_status_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE alf_auth_status
(
    id INT8 NOT NULL,
    username VARCHAR(100) NOT NULL,
    deleted BOOL NOT NULL,
    authorized BOOL NOT NULL,
    checksum BYTEA NOT NULL,
    authaction VARCHAR(10) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_auth_usr_stat ON alf_auth_status (username, authorized);
CREATE INDEX idx_alf_auth_deleted ON alf_auth_status (deleted);
CREATE INDEX idx_alf_auth_action ON alf_auth_status (authaction);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-AuthorizationTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-AuthorizationTables', 'Manually executed script upgrade V4.1: Authorization status tables',
    0, 6075, -1, 6076, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
