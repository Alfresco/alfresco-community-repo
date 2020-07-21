--
-- Title:      Add new index to alf_node
-- Database:   MySQL
-- Since:      V4.1 Schema 5124
-- Author:     Viachaslau Tsikhanovich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-9516 : BM-0002: Slow query detected during site creation action

ALTER TABLE alf_node DROP FOREIGN KEY fk_alf_node_tqn;
DROP INDEX fk_alf_node_tqn ON alf_node;
CREATE INDEX idx_alf_node_tqn ON alf_node (type_qname_id, store_id, id);
ALTER TABLE alf_node ADD CONSTRAINT fk_alf_node_tqn FOREIGN KEY (`type_qname_id`) REFERENCES `alf_qname` (`id`);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-createIdxAlfNodeTQN';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-createIdxAlfNodeTQN', 'MNT-9516 : BM-0002: Slow query detected during site creation action',
    0, 5124, -1, 5125, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );