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
) type=InnoDB;


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
   ADD INDEX FK_ACL_CH_SET (acl_change_set),
   ADD CONSTRAINT FOREIGN KEY FK_ACL_CH_SET (acl_change_set) REFERENCES alf_acl_change_set (id);

UPDATE alf_access_control_list acl
   set acl_id = (acl.id);

ALTER TABLE alf_access_control_list
   ADD UNIQUE (acl_id, latest, acl_version);

-- Create ACL member list
CREATE TABLE alf_acl_member (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   acl_id BIGINT NOT NULL,
   ace_id BIGINT NOT NULL,
   pos INT NOT NULL,
   primary key (id),
   unique(acl_id, ace_id, pos),
   index FK_ACL_ID (acl_id),
   CONSTRAINT FOREIGN KEY FK_ACL_ID (acl_id) REFERENCES alf_access_control_list (id),
   index FK_ACE_ID (ace_id),
   CONSTRAINT FOREIGN KEY FK_ACE_ID (ace_id) REFERENCES alf_access_control_entry (id)
) type=InnoDB;



ALTER TABLE alf_access_control_entry DROP INDEX acl_id;

-- Extend ACE
ALTER TABLE alf_access_control_entry
   ADD COLUMN auth_id BIGINT NOT NULL DEFAULT -1,
   ADD COLUMN applies INT NOT NULL DEFAULT 0,
   ADD COLUMN context_id BIGINT;

-- remove unused
DROP TABLE alf_auth_ext_keys;

-- remove authority constraint
ALTER TABLE alf_access_control_entry
   DROP FOREIGN KEY FKFFF41F99B25A50BF;

-- restructure authority
ALTER TABLE alf_authority
   DROP PRIMARY KEY,
   ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT,
   CHANGE recipient authority VARCHAR(100),
   ADD primary key (id),
   ADD CONSTRAINT UNIQUE INDEX U_AUTH (authority);

-- migrate data - fix up FK refs to authority
UPDATE alf_access_control_entry ace
   set auth_id = (select id from alf_authority a where a.authority = ace.authority_id);


-- migrate data - build equivalent ACL entries
INSERT INTO alf_acl_member (version, acl_id, ace_id, pos)
   select 1, acl_id, id, 0 from alf_access_control_entry;

-- Create ACE context
CREATE TABLE alf_ace_context (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   class_context VARCHAR(1024),
   property_context VARCHAR(1024),
   kvp_context VARCHAR(1024),
   primary key (id)
 ) type=InnoDB;


-- Create auth aliases table
CREATE TABLE alf_authority_alias (
   id BIGINT NOT NULL AUTO_INCREMENT,
   version BIGINT NOT NULL,
   auth_id BIGINT NOT NULL,
   alias_id BIGINT NOT NULL,
   primary key (id),
   INDEX FK_ALIAS_TO_AUTH (alias_id),
   CONSTRAINT FOREIGN KEY FK_ALIAS_TO_AUTH (alias_id) REFERENCES alf_authority (id),
   INDEX FK_AUTH_TO_AUTH (auth_id),
   CONSTRAINT FOREIGN KEY FK_AUTH_TO_AUTH (auth_id) REFERENCES alf_authority (id),
   CONSTRAINT UNIQUE INDEX U_AUTH_TO_ALIAS (auth_id, alias_id)
)  type=InnoDB;


-- Tidy up unused cols on ace table and add the FK contstraint back
-- finish take out of ACL_ID
ALTER TABLE alf_access_control_entry
   DROP FOREIGN KEY FKFFF41F99B9553F6C,
   DROP COLUMN acl_id,
   DROP COLUMN authority_id,
   CHANGE auth_id authority_id BIGINT NOT NULL,
   ADD INDEX  FK_ACL_TO_AUTH (authority_id),
   ADD CONSTRAINT FOREIGN KEY FK_ACL_TO_AUTH (authority_id) REFERENCES alf_authority (id);


-- Update members to point to the first use of an access control entry
UPDATE alf_acl_member mem
   SET ace_id = (SELECT min(ace2.id) FROM alf_access_control_entry ace1 
                     JOIN alf_access_control_entry ace2 
                             ON ace1.permission_id = ace2.permission_id AND
                                ace1.authority_id = ace2.authority_id AND 
                                ace1.allowed = ace2.allowed AND 
                                ace1.applies = ace2.applies 
                     WHERE ace1.id = mem.ace_id  );

-- Remove duplicate aces the mysql way (as you can not use the deleted table in the where clause ...)

CREATE TEMPORARY TABLE tmp_to_delete SELECT ace.id FROM alf_acl_member mem RIGHT OUTER JOIN alf_access_control_entry ace ON mem.ace_id = ace.id WHERE mem.ace_id IS NULL;
DELETE FROM alf_access_control_entry ace USING alf_access_control_entry ace JOIN tmp_to_delete t ON ace.id = t.id;
DROP TEMPORARY TABLE tmp_to_delete;

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
    0, 76, -1, 77, null, 'UNKOWN', 1, 1, 'Script completed'
  );