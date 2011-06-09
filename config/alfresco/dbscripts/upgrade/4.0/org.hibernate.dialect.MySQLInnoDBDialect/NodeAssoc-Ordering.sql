--
-- Title:      Add 'assoc_index' column to 'alf_node_assoc'
-- Database:   MySQL
-- Since:      V4.0 Schema 5008
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Cut the original table to just the data
ALTER TABLE alf_node_assoc
    DROP FOREIGN KEY fk_alf_nass_snode,
    DROP FOREIGN KEY fk_alf_nass_tnode,
    DROP FOREIGN KEY fk_alf_nass_tqn,
    DROP INDEX source_node_id,
    DROP INDEX fk_alf_nass_snode,
    DROP INDEX fk_alf_nass_tnode,
    DROP INDEX fk_alf_nass_tqn;
ALTER TABLE alf_node_assoc
    RENAME TO t_alf_node_assoc;

-- So now it's just raw data
-- Reconstruct the table
CREATE TABLE alf_node_assoc
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    type_qname_id BIGINT NOT NULL,
    assoc_index BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY source_node_id (source_node_id, target_node_id, type_qname_id),
    KEY fk_alf_nass_snode (source_node_id, type_qname_id, assoc_index),
    KEY fk_alf_nass_tnode (target_node_id, type_qname_id),
    KEY fk_alf_nass_tqn (type_qname_id),
    CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
) ENGINE=InnoDB;

-- Copy the data over
--FOREACH t_alf_node_assoc.id system.upgrade.alf_node_assoc.batchsize
INSERT INTO alf_node_assoc
    (id, version, source_node_id, target_node_id, type_qname_id, assoc_index)
    (
        SELECT
           id, 1, source_node_id, target_node_id, type_qname_id, 1
        FROM
           t_alf_node_assoc
        WHERE
           id >= ${LOWERBOUND} AND id <= ${UPPERBOUND}
    );

-- Drop old data
DROP TABLE t_alf_node_assoc;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.0-NodeAssoc-Ordering';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.0-NodeAssoc-Ordering', 'Manually executed script upgrade V4.0: Add assoc_index column to alf_node_assoc',
    0, 5008, -1, 5009, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );