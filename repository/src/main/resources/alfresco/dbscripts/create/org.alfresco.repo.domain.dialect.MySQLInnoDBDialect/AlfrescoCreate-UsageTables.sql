--
-- Title:      User usage tables
-- Database:   MySQL InnoDB
-- Since:      V3.4 Schema 4110
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_usage_delta
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    node_id BIGINT NOT NULL,
    delta_size BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY fk_alf_usaged_n (node_id),
    CONSTRAINT fk_alf_usaged_n FOREIGN KEY (node_id) REFERENCES alf_node (id)
) ENGINE=InnoDB; -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-UsageTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-UsageTables', 'Manually executed script upgrade V3.4: Usage Tables',
    0, 113, -1, 114, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );