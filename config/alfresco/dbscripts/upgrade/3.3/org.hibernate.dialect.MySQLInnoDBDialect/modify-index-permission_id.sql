--
-- Title:      Upgrade to V3.3 - Remove context_id from the permission_id index on alf_access_control_list_entry 
-- Database:   MySQL
-- Since:      V3.3 schema 4011
-- Author:     andyh
--
-- Remove context_id from the permission_id unique index (as it alwaays contains null and therefore has no effect)
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--



-- The remainder of this script is adapted from 
-- Repository/config/alfresco/dbscripts/upgrade/2.2/org.hibernate.dialect.MySQLInnoDBDialect/AlfrescoSchemaUpdate-2.2-ACL.sql
-- Ports should do the same and reflect the DB specific improvements

CREATE TABLE alf_tmp_min_ace (
  min BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  authority_id BIGINT NOT NULL,
  allowed BIT(1) NOT NULL,
  applies INT NOT NULL,
  UNIQUE (permission_id, authority_id, allowed, applies)
) ENGINE=InnoDB;

INSERT INTO alf_tmp_min_ace (min, permission_id, authority_id, allowed, applies)
    SELECT
       min(ace1.id),
       ace1.permission_id,
       ace1.authority_id,
       ace1.allowed,
       ace1.applies
    FROM
       alf_access_control_entry ace1
    GROUP BY
       ace1.permission_id, ace1.authority_id, ace1.allowed, ace1.applies
;


CREATE TABLE alf_tmp_acl_members (
    id BIGINT NOT NULL,
    min BIGINT NOT NULL,
    acl_id BIGINT NOT NULL,
    pos BIGINT,
    ace_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (min, acl_id, pos, ace_id)
) ENGINE=InnoDB;


INSERT INTO
    alf_tmp_acl_members
    SELECT
        mem.id, help.min, mem.acl_id, mem.pos, mem.ace_id
    FROM
        alf_acl_member mem
        JOIN
            alf_access_control_entry ace
        ON
            mem.ace_id = ace.id
        JOIN
            alf_tmp_min_ace help
        ON
            help.permission_id = ace.permission_id AND
            help.authority_id = ace.authority_id AND
            help.allowed = ace.allowed AND
            help.applies = ace.applies;


CREATE TABLE alf_tmp_acl_groups (
    min BIGINT NOT NULL,
    acl_id BIGINT NOT NULL,
    pos BIGINT,
    group_min BIGINT NOT NULL,
    UNIQUE (min, acl_id, pos)
) ENGINE=InnoDB;


INSERT INTO
    alf_tmp_acl_groups
    SELECT
        mems.min, mems.acl_id, mems.pos, min(mems.ace_id)
    FROM
        alf_tmp_acl_members mems
    GROUP BY
        mems.min, mems.acl_id, mems.pos
    HAVING
        count(*) > 1;


DELETE FROM
    alf_acl_member
WHERE
    id IN (
        SELECT
            mems.id
        FROM
            alf_tmp_acl_members mems
            JOIN
                alf_tmp_acl_groups groups
            ON
                mems.min = groups.min AND
                mems.acl_id = groups.acl_id AND
                mems.pos = groups.pos
        WHERE
            mems.ace_id <> groups.group_min
    );


DROP TABLE
    alf_tmp_acl_members;

DROP TABLE
    alf_tmp_acl_groups;


-- Update members to point to the first use of an access control entry
UPDATE alf_acl_member mem
   SET ace_id = (SELECT help.min FROM alf_access_control_entry ace 
                     JOIN alf_tmp_min_ace help
                     ON		help.permission_id = ace.permission_id AND
                                help.authority_id = ace.authority_id AND 
                                help.allowed = ace.allowed AND 
                                help.applies = ace.applies 
                     WHERE ace.id = mem.ace_id  );

DROP TABLE alf_tmp_min_ace;

-- Remove duplicate aces the mysql way (as you can not use the deleted table in the where clause ...)

CREATE TABLE tmp_to_delete SELECT ace.id FROM alf_acl_member mem RIGHT OUTER JOIN alf_access_control_entry ace ON mem.ace_id = ace.id WHERE mem.ace_id IS NULL;
DELETE FROM ace USING alf_access_control_entry ace JOIN tmp_to_delete t ON ace.id = t.id;
DROP TABLE tmp_to_delete;

-- Add constraint for duplicate acls (this no longer includes the context)


ALTER TABLE alf_access_control_entry DROP INDEX permission_id;
ALTER TABLE alf_access_control_entry
   ADD UNIQUE permission_id (permission_id, authority_id, allowed, applies);


--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-modify-index-permission_id';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-modify-index-permission_id', 'Remove context_id from the permission_id unique index (as it always contains null and therefore has no effect)',
     0, 4102, -1, 4103, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
