--
-- Title:      Create AVM tables
-- Database:   MySQL InnoDB
-- Since:      V3.2.0 Schema 3002
-- Author:     janv
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

    create table avm_aspects (
        node_id bigint not null,
        qname_id bigint not null,
        primary key (node_id, qname_id)
    ) ENGINE=InnoDB;

    create table avm_child_entries (
        parent_id bigint not null,
        lc_name varchar(160) not null,
        name varchar(160) not null,
        child_id bigint not null,
        primary key (parent_id, lc_name)
    ) ENGINE=InnoDB;

    create table avm_history_links (
        ancestor bigint not null,
        descendent bigint not null,
        primary key (ancestor, descendent)
    ) ENGINE=InnoDB;

    create table avm_merge_links (
        mfrom bigint not null,
        mto bigint not null,
        primary key (mfrom, mto)
    ) ENGINE=InnoDB;

    create table avm_node_properties (
        node_id bigint not null,
        actual_type_n integer not null,
        persisted_type_n integer not null,
        multi_valued bit not null,
        boolean_value bit,
        long_value bigint,
        float_value float,
        double_value double precision,
        string_value text,
        serializable_value blob,
        qname_id bigint not null,
        primary key (node_id, qname_id)
    ) ENGINE=InnoDB;

    create table avm_nodes (
        id bigint not null auto_increment,
        class_type varchar(20) not null,
        vers bigint not null,
        version_id integer not null,
        guid varchar(36),
        creator varchar(255) not null,
        owner varchar(255) not null,
        lastModifier varchar(255) not null,
        createDate bigint not null,
        modDate bigint not null,
        accessDate bigint not null,
        is_root bit,
        store_new_id bigint,
        acl_id bigint,
        deletedType integer,
        layer_id bigint,
        indirection text,
        indirection_version integer,
        primary_indirection bit,
        opacity bit,
        content_url varchar(128),
        mime_type varchar(100),
        encoding varchar(16),
        length bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table avm_store_properties (
        id bigint not null auto_increment,
        avm_store_id bigint,
        qname_id bigint not null,
        actual_type_n integer not null,
        persisted_type_n integer not null,
        multi_valued bit not null,
        boolean_value bit,
        long_value bigint,
        float_value float,
        double_value double precision,
        string_value text,
        serializable_value blob,
        primary key (id)
    ) ENGINE=InnoDB;

    create table avm_stores (
        id bigint not null auto_increment,
        vers bigint not null,
        name varchar(255) unique,
        next_version_id integer not null,
        current_root_id bigint,
        acl_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table avm_version_layered_node_entry (
        version_root_id bigint not null,
        md5sum varchar(32) not null,
        path text,
        primary key (version_root_id, md5sum)
    ) ENGINE=InnoDB;

    create table avm_version_roots (
        id bigint not null auto_increment,
        version_id integer not null,
        avm_store_id bigint not null,
        create_date bigint not null,
        creator varchar(255) not null,
        root_id bigint not null,
        tag varchar(255),
        description text,
        primary key (id),
        constraint unique index idx_avm_vr_uq (avm_store_id, version_id)
    ) ENGINE=InnoDB;

    alter table avm_aspects
        add index fk_avm_nasp_n (node_id),
        add constraint fk_avm_nasp_n
        foreign key (node_id)
        references avm_nodes (id);

    alter table avm_child_entries
        add index fk_avm_ce_child (child_id),
        add constraint fk_avm_ce_child
        foreign key (child_id)
        references avm_nodes (id);

    alter table avm_child_entries
        add index fk_avm_ce_parent (parent_id),
        add constraint fk_avm_ce_parent
        foreign key (parent_id)
        references avm_nodes (id);

    alter table avm_history_links
        add index fk_avm_hl_desc (descendent),
        add constraint fk_avm_hl_desc
        foreign key (descendent)
        references avm_nodes (id);

    alter table avm_history_links
        add index fk_avm_hl_ancestor (ancestor),
        add constraint fk_avm_hl_ancestor
        foreign key (ancestor)
        references avm_nodes (id);

    alter table avm_merge_links
        add index fk_avm_ml_from (mfrom),
        add constraint fk_avm_ml_from
        foreign key (mfrom)
        references avm_nodes (id);

    alter table avm_merge_links
        add index fk_avm_ml_to (mto),
        add constraint fk_avm_ml_to
        foreign key (mto)
        references avm_nodes (id);

    alter table avm_node_properties
        add index fk_avm_nprop_n (node_id),
        add constraint fk_avm_nprop_n
        foreign key (node_id)
        references avm_nodes (id);

    create index idx_avm_n_pi on avm_nodes (primary_indirection);

    alter table avm_nodes
        add index fk_avm_n_acl (acl_id),
        add constraint fk_avm_n_acl
        foreign key (acl_id)
        references alf_access_control_list (id);

    alter table avm_nodes
        add index fk_avm_n_store (store_new_id),
        add constraint fk_avm_n_store
        foreign key (store_new_id)
        references avm_stores (id);

    alter table avm_store_properties
        add index fk_avm_sprop_store (avm_store_id),
        add constraint fk_avm_sprop_store
        foreign key (avm_store_id)
        references avm_stores (id);

    alter table avm_stores
        add index fk_avm_s_root (current_root_id),
        add constraint fk_avm_s_root
        foreign key (current_root_id)
        references avm_nodes (id);

    alter table avm_stores
        add index fk_avm_s_acl (acl_id),
        add constraint fk_avm_s_acl
        foreign key (acl_id)
        references alf_access_control_list (id);

    alter table avm_version_layered_node_entry
        add index fk_avm_vlne_vr (version_root_id),
        add constraint fk_avm_vlne_vr
        foreign key (version_root_id)
        references avm_version_roots (id);

    create index idx_avm_vr_version on avm_version_roots (version_id);

    alter table avm_version_roots
        add index fk_avm_vr_store (avm_store_id),
        add constraint fk_avm_vr_store
        foreign key (avm_store_id)
        references avm_stores (id);

    alter table avm_version_roots
        add index fk_avm_vr_root (root_id),
        add constraint fk_avm_vr_root
        foreign key (root_id)
        references avm_nodes (id);
        
CREATE INDEX fk_avm_nasp_qn ON avm_aspects (qname_id);
ALTER TABLE avm_aspects ADD CONSTRAINT fk_avm_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_avm_nprop_qn ON avm_node_properties (qname_id);
ALTER TABLE avm_node_properties ADD CONSTRAINT fk_avm_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_avm_sprop_qname ON avm_store_properties (qname_id);
ALTER TABLE avm_store_properties ADD CONSTRAINT fk_avm_sprop_qname FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX idx_avm_hl_revpk ON avm_history_links (descendent, ancestor);

CREATE INDEX idx_avm_ce_lc_name ON avm_child_entries (lc_name, parent_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-AvmTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-AvmTables', 'Manually executed script upgrade V3.2: AVM Tables',
    0, 3001, -1, 3002, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );