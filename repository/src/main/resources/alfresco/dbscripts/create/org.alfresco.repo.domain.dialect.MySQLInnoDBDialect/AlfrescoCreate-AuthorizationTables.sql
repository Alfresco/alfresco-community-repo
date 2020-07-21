--
-- Title:      Create Authorization Status
-- Database:   MySQL InnoDB
-- Since:      V4.1.11 Schema 5156
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_auth_status
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   username VARCHAR(100) NOT NULL,
   deleted BIT NOT NULL,
   authorized BIT NOT NULL,
   checksum BLOB NOT NULL,
   authaction VARCHAR(10) NOT NULL,
   UNIQUE INDEX idx_alf_auth_usr_stat (username, authorized),
   INDEX idx_alf_auth_action (authaction),
   INDEX idx_alf_auth_deleted (deleted),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

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
