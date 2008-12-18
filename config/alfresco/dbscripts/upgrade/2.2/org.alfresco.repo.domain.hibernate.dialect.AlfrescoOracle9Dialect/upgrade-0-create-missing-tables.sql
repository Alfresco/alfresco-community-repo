--
-- Title:      Create missing 2.1 tables
-- Database:   Oracle
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Upgrade paths that bypass V2.1 will need to have a some tables added in order
-- to simplify subsequent upgrade scripts.
--

-- Fix alf_audit_date column names

alter table alf_audit_date RENAME COLUMN halfyear to half_year;
alter table alf_audit_date RENAME COLUMN year to full_year;

-- create other new tables

    create table avm_aspects (
        id number(19,0) not null,
        node_id number(19,0),
        qname varchar2(200 char),
        primary key (id)
    );                                    -- (optional)

    create table avm_aspects_new (
        id number(19,0) not null,
        name varchar2(200 char) not null,
        primary key (id, name)
    );                                    -- (optional)

    create table avm_node_properties (
        id number(19,0) not null,
        node_id number(19,0),
        qname varchar2(200 char),
        actual_type varchar2(15 char) not null,
        multi_valued number(1,0) not null,
        persisted_type varchar2(15 char) not null,
        boolean_value number(1,0),
        long_value number(19,0),
        float_value float,
        double_value double precision,
        string_value varchar2(1024 char),
        serializable_value long raw,
        primary key (id)
    );                                    -- (optional)

    create table avm_node_properties_new (
        node_id number(19,0) not null,
        actual_type varchar2(15 char) not null,
        multi_valued number(1,0) not null,
        persisted_type varchar2(15 char) not null,
        boolean_value number(1,0),
        long_value number(19,0),
        float_value float,
        double_value double precision,
        string_value varchar2(1024 char),
        serializable_value long raw,
        qname varchar2(200 char) not null,
        primary key (node_id, qname)
    );                                    -- (optional)

    create table avm_store_properties (
        id number(19,0) not null,
        avm_store_id number(19,0),
        qname varchar2(200 char),
        actual_type varchar2(15 char) not null,
        multi_valued number(1,0) not null,
        persisted_type varchar2(15 char) not null,
        boolean_value number(1,0),
        long_value number(19,0),
        float_value float,
        double_value double precision,
        string_value varchar2(1024 char),
        serializable_value long raw,
        primary key (id)
    );                                    -- (optional)

-- Add ACL column for AVM tables
ALTER TABLE avm_stores
   ADD (acl_id number(19,0));

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
