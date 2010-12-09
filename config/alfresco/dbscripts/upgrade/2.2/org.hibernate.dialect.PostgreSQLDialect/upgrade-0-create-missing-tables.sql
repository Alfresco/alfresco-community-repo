--
-- Title:      Create missing 2.1 tables
-- Database:   PostgreSQL
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
        id int8 not null,
        node_id int8,
        qname varchar(200),
        primary key (id)
    );                                    -- (optional)

    create table avm_aspects_new (
        id int8 not null,
        name varchar(200) not null,
        primary key (id, name)
    );                                    -- (optional)

    create table avm_node_properties (
        id int8 not null,
        node_id int8,
        qname varchar(200),
        actual_type varchar(15) not null,
        multi_valued bool not null,
        persisted_type varchar(15) not null,
        boolean_value bool,
        long_value int8,
        float_value float4,
        double_value float8,
        string_value varchar(1024),
        serializable_value bytea,
        primary key (id)
    );                                    -- (optional)

    create table avm_node_properties_new (
        node_id int8 not null,
        actual_type varchar(15) not null,
        multi_valued bool not null,
        persisted_type varchar(15) not null,
        boolean_value bool,
        long_value int8,
        float_value float4,
        double_value float8,
        string_value varchar(1024),
        serializable_value bytea,
        qname varchar(200) not null,
        primary key (node_id, qname)
    );                                    -- (optional)

    create table avm_store_properties (
        id int8 not null,
        avm_store_id int8,
        qname varchar(200),
        actual_type varchar(15) not null,
        multi_valued bool not null,
        persisted_type varchar(15) not null,
        boolean_value bool,
        long_value int8,
        float_value float4,
        double_value float8,
        string_value varchar(1024),
        serializable_value bytea,
        primary key (id)
    );                                    -- (optional)

-- Add ACL column for AVM tables
ALTER TABLE avm_stores
   ADD COLUMN acl_id int8;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-0-CreateMissingTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-0-CreateMissingTables', 'Manually executed script upgrade V2.2: Created missing tables',
    0, 120, -1, 121, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );
