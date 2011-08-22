--
-- Title:      Subscription tables
-- Database:   MySQL InnoDB
-- Since:      V4.0 Schema 5011
-- Author:     Florian Mueller
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_subscriptions
(
  user_node_id BIGINT NOT NULL,
  node_id BIGINT NOT NULL,
  PRIMARY KEY (user_node_id, node_id),
  CONSTRAINT fk_alf_sub_user FOREIGN KEY (user_node_id) REFERENCES alf_node(id) ON DELETE CASCADE,
  CONSTRAINT fk_alf_sub_node FOREIGN KEY (node_id) REFERENCES alf_node(id) ON DELETE CASCADE
) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.0-SubscriptionTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.0-SubscriptionTables', 'Manually executed script upgrade V4.0: Subscription Tables',
    0, 5010, -1, 5011, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );