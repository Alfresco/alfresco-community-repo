--
-- Title:      Create lock tables
-- Database:   MySQL InnoDB
-- Since:      V3.2 Schema 2011
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_lock_resource
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   qname_ns_id BIGINT NOT NULL,
   qname_localname VARCHAR(255) NOT NULL,
   CONSTRAINT fk_alf_lockr_ns FOREIGN KEY (qname_ns_id) REFERENCES alf_namespace (id),
   PRIMARY KEY (id),
   UNIQUE INDEX idx_alf_lockr_key (qname_ns_id, qname_localname)
) ENGINE=InnoDB;

CREATE TABLE alf_lock
(
   id BIGINT NOT NULL auto_increment,
   version BIGINT NOT NULL,
   shared_resource_id BIGINT NOT NULL,
   excl_resource_id BIGINT NOT NULL,
   lock_token VARCHAR(36) NOT NULL,
   start_time BIGINT NOT NULL,
   expiry_time BIGINT NOT NULL,
   CONSTRAINT fk_alf_lock_shared FOREIGN KEY (shared_resource_id) REFERENCES alf_lock_resource (id),
   CONSTRAINT fk_alf_lock_excl FOREIGN KEY fk_alf_lock_excl (excl_resource_id) REFERENCES alf_lock_resource (id),
   PRIMARY KEY (id),
   UNIQUE INDEX idx_alf_lock_key (shared_resource_id, excl_resource_id)
) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-LockTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-LockTables', 'Manually executed script upgrade V3.2: Lock Tables',
    0, 2010, -1, 2011, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );