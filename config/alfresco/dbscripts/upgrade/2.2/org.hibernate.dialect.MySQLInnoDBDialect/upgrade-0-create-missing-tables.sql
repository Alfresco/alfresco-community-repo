--
-- Title:      Create missing 2.1 tables
-- Database:   MySQL
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgrade paths that bypass V2.1 will need to have a some tables added in order
-- to simplify subsequent upgrade scripts.
--

-- create other new tables

    create table avm_aspects (
        id bigint not null auto_increment,
        node_id bigint,
        qname varchar(200),
        primary key (id)
    ) ENGINE=InnoDB;                                    -- (optional)

    create table avm_aspects_new (
        id bigint not null,
        name varchar(200) not null,
        primary key (id, name)
    ) ENGINE=InnoDB;                                    -- (optional)

    create table avm_node_properties (
        id bigint not null auto_increment,
        node_id bigint,
        qname varchar(200),
        actual_type varchar(15) not null,
        multi_valued bit not null,
        persisted_type varchar(15) not null,
        boolean_value bit,
        long_value bigint,
        float_value float,
        double_value double precision,
        string_value text,
        serializable_value blob,
        primary key (id)
    ) ENGINE=InnoDB;                                    -- (optional)

    create table avm_node_properties_new (
        node_id bigint not null,
        actual_type varchar(15) not null,
        multi_valued bit not null,
        persisted_type varchar(15) not null,
        boolean_value bit,
        long_value bigint,
        float_value float,
        double_value double precision,
        string_value text,
        serializable_value blob,
        qname varchar(200) not null,
        primary key (node_id, qname)
    ) ENGINE=InnoDB;                                    -- (optional)

    create table avm_store_properties (
        id bigint not null auto_increment,
        avm_store_id bigint,
        qname varchar(200),
        actual_type varchar(15) not null,
        multi_valued bit not null,
        persisted_type varchar(15) not null,
        boolean_value bit,
        long_value bigint,
        float_value float,
        double_value double precision,
        string_value text,
        serializable_value blob,
        primary key (id)
    ) ENGINE=InnoDB;                                    -- (optional)

-- Add ACL column for AVM tables
ALTER TABLE avm_stores
   ADD COLUMN acl_id BIGINT;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-0-CreateMissingTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-0-CreateMissingTables', 'Manually executed script upgrade V2.2: Created missing tables',
    0, 120, -1, 121, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
