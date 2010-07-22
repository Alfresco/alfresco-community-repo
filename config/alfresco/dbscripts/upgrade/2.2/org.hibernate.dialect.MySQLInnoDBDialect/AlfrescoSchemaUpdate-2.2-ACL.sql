--
-- Title:      Update for permissions schema changes
-- Database:   MySQL InnoDB
-- Since:      V2.2 Schema 85
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_acl_change_set (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   primary key (id)
) ENGINE=InnoDB;


-- Add to ACL
ALTER TABLE alf_access_control_list
   ADD COLUMN type INT NOT NULL DEFAULT 0,
   ADD COLUMN latest BOOLEAN NOT NULL DEFAULT TRUE,
   ADD COLUMN acl_id VARCHAR(36) NOT NULL DEFAULT 'UNSET',
   ADD COLUMN acl_version BIGINT NOT NULL DEFAULT 1,
   ADD COLUMN inherited_acl BIGINT,
   ADD COLUMN is_versioned BOOLEAN NOT NULL DEFAULT FALSE,
   ADD COLUMN requires_version BOOLEAN NOT NULL DEFAULT FALSE,
   ADD COLUMN acl_change_set BIGINT,
   ADD COLUMN inherits_from BIGINT,
   ADD INDEX fk_alf_acl_acs (acl_change_set),
   ADD CONSTRAINT fk_alf_acl_acs FOREIGN KEY (acl_change_set) REFERENCES alf_acl_change_set (id),
   ADD INDEX idx_alf_acl_inh (inherits, inherits_from);

--FOREACH alf_access_control_list.id system.upgrade.alf_access_control_list.batchsize
UPDATE alf_access_control_list acl
   set acl_id = (acl.id)
   WHERE acl.id >= ${LOWERBOUND} AND acl.id <= ${UPPERBOUND};
   

ALTER TABLE alf_access_control_list
   ADD UNIQUE (acl_id, latest, acl_version);

-- Create ACL member list
CREATE TABLE alf_acl_member (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   acl_id BIGINT NOT NULL,
   ace_id BIGINT NOT NULL,
   pos INT NOT NULL,
   INDEX fk_alf_aclm_acl (acl_id),
   CONSTRAINT fk_alf_aclm_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
   INDEX fk_alf_aclm_ace (ace_id),
   CONSTRAINT fk_alf_aclm_ace FOREIGN KEY (ace_id) REFERENCES alf_access_control_entry (id),
   primary key (id),
   unique(acl_id, ace_id, pos)
) ENGINE=InnoDB;

ALTER TABLE alf_access_control_entry DROP INDEX acl_id;

-- Extend ACE
ALTER TABLE alf_access_control_entry
   ADD COLUMN auth_id BIGINT NOT NULL DEFAULT -1,
   ADD COLUMN applies INT NOT NULL DEFAULT 0,
   ADD COLUMN context_id BIGINT;

-- remove unused
DROP TABLE alf_auth_ext_keys;

-- remove authority constraint
ALTER TABLE alf_access_control_entry DROP INDEX FKFFF41F99B25A50BF, DROP FOREIGN KEY FKFFF41F99B25A50BF; -- (optional)

-- restructure authority
ALTER TABLE alf_authority
   DROP PRIMARY KEY,
   ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT,
   ADD COLUMN crc BIGINT,
   CHANGE recipient authority VARCHAR(100),
   ADD INDEX idx_alf_auth_aut (authority),
   ADD primary key (id),
   ADD UNIQUE (authority, crc);

-- migrate data - fix up FK refs to authority
--FOREACH alf_access_control_entry.id system.upgrade.alf_access_control_entry.batchsize
UPDATE alf_access_control_entry ace
   set auth_id = (select id from alf_authority a where a.authority = ace.authority_id)
   WHERE ace.id >= ${LOWERBOUND} AND ace.id <= ${UPPERBOUND};


-- migrate data - build equivalent ACL entries
--FOREACH alf_access_control_list.id system.upgrade.alf_acl_member.batchsize
INSERT INTO alf_acl_member (version, acl_id, ace_id, pos)
   select 1, ace.acl_id, ace.id, 0
   from alf_access_control_entry ace join alf_access_control_list acl on acl.id = ace.acl_id
   where acl.id >= ${LOWERBOUND} AND acl.id <= ${UPPERBOUND};

-- Create ACE context
CREATE TABLE alf_ace_context (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   class_context VARCHAR(1024),
   property_context VARCHAR(1024),
   kvp_context VARCHAR(1024),
   primary key (id)
 ) ENGINE=InnoDB;


-- Create auth aliases table
CREATE TABLE alf_authority_alias (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   auth_id BIGINT NOT NULL,
   alias_id BIGINT NOT NULL,
   INDEX fk_alf_autha_ali (alias_id),
   CONSTRAINT fk_alf_autha_ali FOREIGN KEY (alias_id) REFERENCES alf_authority (id),
   INDEX fk_alf_autha_aut (auth_id),
   CONSTRAINT fk_alf_autha_aut FOREIGN KEY (auth_id) REFERENCES alf_authority (id),
   primary key (id),
   UNIQUE (auth_id, alias_id)
)  ENGINE=InnoDB;


-- Tidy up unused cols on ace table and add the FK contstraint back
-- finish take out of ACL_ID
ALTER TABLE alf_access_control_entry
   DROP INDEX FKFFF41F99B9553F6C, DROP FOREIGN KEY FKFFF41F99B9553F6C,
   DROP INDEX FKFFF41F9960601995, DROP FOREIGN KEY FKFFF41F9960601995,
   DROP COLUMN acl_id, DROP COLUMN authority_id,
   CHANGE auth_id authority_id BIGINT NOT NULL,
   ADD INDEX fk_alf_ace_auth (authority_id),
   ADD CONSTRAINT fk_alf_ace_auth FOREIGN KEY (authority_id) REFERENCES alf_authority (id),
   ADD INDEX fk_alf_ace_perm (permission_id),
   ADD CONSTRAINT fk_alf_ace_perm FOREIGN KEY (permission_id) REFERENCES alf_permission (id),
   ADD INDEX fk_alf_ace_ctx (context_id),
   ADD CONSTRAINT fk_alf_ace_ctx FOREIGN KEY (context_id) REFERENCES alf_ace_context (id)
;
   

CREATE TABLE alf_tmp_min_ace (
  min BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  authority_id BIGINT NOT NULL,
  allowed BIT(1) NOT NULL,
  applies INT NOT NULL,
  UNIQUE (permission_id, authority_id, allowed, applies)
) ENGINE=InnoDB;

--FOREACH alf_access_control_entry.authority_id system.upgrade.alf_tmp_min_ace.batchsize
INSERT INTO alf_tmp_min_ace (min, permission_id, authority_id, allowed, applies)
    SELECT
       min(ace1.id),
       ace1.permission_id,
       ace1.authority_id,
       ace1.allowed,
       ace1.applies
    FROM
       alf_access_control_entry ace1
    WHERE
       ace1.authority_id >= ${LOWERBOUND} AND ace1.authority_id <= ${UPPERBOUND}
    GROUP BY
       ace1.permission_id, ace1.authority_id, ace1.allowed, ace1.applies
;
   

-- Update members to point to the first use of an access control entry
--FOREACH alf_acl_member.id system.upgrade.alf_acl_member.batchsize
UPDATE alf_acl_member mem
   SET ace_id = (SELECT help.min FROM alf_access_control_entry ace 
                     JOIN alf_tmp_min_ace help
                     ON		help.permission_id = ace.permission_id AND
                                help.authority_id = ace.authority_id AND 
                                help.allowed = ace.allowed AND 
                                help.applies = ace.applies 
                     WHERE ace.id = mem.ace_id  )
   WHERE mem.id >= ${LOWERBOUND} AND mem.id <= ${UPPERBOUND};
DROP TABLE alf_tmp_min_ace;

-- Remove duplicate aces the mysql way (as you can not use the deleted table in the where clause ...)

CREATE TABLE tmp_to_delete SELECT ace.id FROM alf_acl_member mem RIGHT OUTER JOIN alf_access_control_entry ace ON mem.ace_id = ace.id WHERE mem.ace_id IS NULL;
DELETE FROM ace USING alf_access_control_entry ace JOIN tmp_to_delete t ON ace.id = t.id;
DROP TABLE tmp_to_delete;

-- Add constraint for duplicate acls

ALTER TABLE alf_access_control_entry
   ADD UNIQUE (permission_id, authority_id, allowed, applies, context_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-ACL';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-ACL', 'Manually executed script upgrade V2.2: Update acl schema',
    0, 119, -1, 120, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
