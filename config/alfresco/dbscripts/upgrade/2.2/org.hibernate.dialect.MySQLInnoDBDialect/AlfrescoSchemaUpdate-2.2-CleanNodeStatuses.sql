--
-- Title:      Clean duplicate alf_node_status entries
-- Database:   MySQL
-- Since:      V3.1 schema 1011
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Cleans out duplicate alf_node_status entries for V2.1 installations.
-- This script does not need to run if the server has already been upgraded to schema 90 or later

CREATE TABLE t_node_status
(
   node_id INTEGER NOT NULL,
   transaction_id BIGINT(20) NOT NULL,
   PRIMARY KEY (node_id)
);
INSERT INTO t_node_status
(
   SELECT c.node_id, c.transaction_id FROM
   (
      SELECT COUNT(node_id) x, ns.node_id, ns.transaction_id FROM alf_node_status ns GROUP BY node_id
   ) c
   WHERE c.x > 1
);
DELETE FROM alf_node_status WHERE node_id IN (SELECT node_id FROM t_node_status);
INSERT INTO alf_node_status (protocol, identifier, guid, node_id, transaction_id, version)
(
  SELECT n.protocol, n.identifier, n.uuid, n.id, tns.transaction_id, 0 FROM t_node_status tns JOIN alf_node n ON (n.id = tns.node_id)
);
DROP TABLE t_node_status;

DELETE FROM alf_node_status WHERE node_id IS NULL;

UPDATE alf_node_status ns SET ns.protocol =
  (
    SELECT n.protocol FROM alf_node n WHERE n.id = ns.node_id
  );

DELETE 
      alf_transaction 
   FROM 
      alf_transaction 
      LEFT JOIN alf_node_status ON (alf_node_status.transaction_id = alf_transaction.id) 
   WHERE 
      alf_node_status.node_id is null; 

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-CleanNodeStatuses';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-CleanNodeStatuses', 'Manually executed script upgrade V2.2: Clean alf_node_status table',
     0, 89, -1, 90, null, 'UNKOWN', ${true}, ${true}, 'Script completed'
   );
