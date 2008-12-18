--
-- Title:      Create missing 2.1 tables
-- Database:   SQL Server
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgrade paths that bypass V2.1 will need to have a some tables added in order
-- to simplify subsequent upgrade scripts.
--

-- Fix alf_audit_date column names

EXEC sp_rename 'alf_audit_date.halfYear', 'half_year', 'COLUMN';
EXEC sp_rename 'alf_audit_date.year', 'full_year', 'COLUMN';

-- create other new tables

    create table avm_aspects (
        id numeric(19,0) identity not null,
        node_id numeric(19,0) null,
        qname nvarchar(200) null,
        primary key (id)
    );                                    -- (optional)

    create table avm_aspects_new (
        id numeric(19,0) not null,
        name nvarchar(200) not null,
        primary key (id, name)
    );                                    -- (optional)

    create table avm_node_properties (
        id numeric(19,0) identity not null,
        node_id numeric(19,0) null,
        qname nvarchar(200) null,
        actual_type nvarchar(15) not null,
        multi_valued tinyint not null,
        persisted_type nvarchar(15) not null,
        boolean_value tinyint null,
        long_value numeric(19,0) null,
        float_value float null,
        double_value double precision null,
        string_value text null,
        serializable_value image null,
        primary key (id)
    );                                    -- (optional)

    create table avm_node_properties_new (
        node_id numeric(19,0) not null,
        actual_type nvarchar(15) not null,
        multi_valued tinyint not null,
        persisted_type nvarchar(15) not null,
        boolean_value tinyint null,
        long_value numeric(19,0) null,
        float_value float null,
        double_value double precision null,
        string_value text null,
        serializable_value image null,
        qname nvarchar(200) not null,
        primary key (node_id, qname)
    );                                    -- (optional)

    create table avm_store_properties (
        id numeric(19,0) identity not null,
        avm_store_id numeric(19,0) null,
        qname nvarchar(200) null,
        actual_type nvarchar(15) not null,
        multi_valued tinyint not null,
        persisted_type nvarchar(15) not null,
        boolean_value tinyint null,
        long_value numeric(19,0) null,
        float_value float null,
        double_value double precision null,
        string_value text null,
        serializable_value image null,
        primary key (id)
    );                                    -- (optional)

-- Add ACL column for AVM tables
ALTER TABLE avm_stores
   ADD acl_id numeric(19,0) null;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-0-CreateMissingTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-0-CreateMissingTables', 'Manually executed script upgrade V2.2: Created missing tables',
    0, 120, -1, 121, null, 'UNKOWN', 1, 1, 'Script completed'
  );
