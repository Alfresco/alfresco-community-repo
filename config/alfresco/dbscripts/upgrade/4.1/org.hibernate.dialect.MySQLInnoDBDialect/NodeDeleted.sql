--
-- Title:      Update alf_txn table and alf_node indexes to support SOLR tracking
-- Database:   MySQL
-- Since:      V4.1 Schema 5113
-- Author:     Derek Hulley
-- Author:     Dmitry Velichkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--



--ASSIGN:SYSTEM_NS_ID=id
select id from alf_namespace where uri = 'http://www.alfresco.org/model/system/1.0';


insert into alf_qname (version, ns_id, local_name) values (0, ${SYSTEM_NS_ID}, 'deleted');         -- (optional)


--ASSIGN:DELETED_TYPE_ID=id
select id from alf_qname where ns_id = ${SYSTEM_NS_ID} and local_name = 'deleted';


alter table alf_node
    drop foreign key fk_alf_node_acl,
    drop foreign key fk_alf_node_store,
    drop foreign key fk_alf_node_tqn,
    drop foreign key fk_alf_node_txn,
    drop foreign key fk_alf_node_loc,
    drop index store_id,
    drop index idx_alf_node_txn_del,
    drop index fk_alf_node_acl,
    drop index fk_alf_node_txn,
    drop index fk_alf_node_store,
    drop index fk_alf_node_tqn,
    drop index fk_alf_node_loc;

alter table alf_child_assoc
    drop foreign key fk_alf_cass_cnode,
    drop foreign key fk_alf_cass_pnode;

alter table alf_node_assoc
    drop foreign key fk_alf_nass_snode,
    drop foreign key fk_alf_nass_tnode;

alter table alf_subscriptions
    drop foreign key fk_alf_sub_user,
    drop foreign key fk_alf_sub_node;

alter table alf_store
    drop foreign key fk_alf_store_root,
    drop index fk_alf_store_root;

alter table alf_node_properties drop foreign key fk_alf_nprop_n;

alter table alf_usage_delta drop foreign key fk_alf_usaged_n;

alter table alf_node_aspects drop foreign key fk_alf_nasp_n;


alter table alf_node rename to t_alf_node;


create table alf_node
(
    id BIGINT not NULL AUTO_INCREMENT,
    version BIGINT not NULL,
    store_id BIGINT not NULL,
    uuid VARCHAR(36) not NULL,
    transaction_id BIGINT not NULL,
    type_qname_id BIGINT not NULL,
    locale_id BIGINT not NULL,
    acl_id BIGINT,
    audit_creator VARCHAR(255),
    audit_created VARCHAR(30),
    audit_modifier VARCHAR(255),
    audit_modified VARCHAR(30),
    audit_accessed VARCHAR(30),
    primary key (id),
    unique key store_id(store_id, uuid),
    key idx_alf_node_txn_type(transaction_id, type_qname_id),
    key fk_alf_node_acl(acl_id),
    key fk_alf_node_store(store_id),
    key fk_alf_node_tqn(type_qname_id),
    key fk_alf_node_loc(locale_id),
    constraint fk_alf_node_acl foreign key (acl_id) references alf_access_control_list(id),
    constraint fk_alf_node_store foreign key (store_id) references alf_store(id),
    constraint fk_alf_node_tqn foreign key (type_qname_id) references alf_qname(id),
    constraint fk_alf_node_txn foreign key (transaction_id) references alf_transaction(id),
    constraint fk_alf_node_loc foreign key (locale_id) references alf_locale(id)
) ENGINE=InnoDB;


--FOREACH t_alf_node.id system.upgrade.alf_node_deleted_type.batchsize
insert into alf_node
(id, version, store_id, uuid, transaction_id, type_qname_id, locale_id, acl_id, audit_creator, audit_created, audit_modifier, audit_modified, audit_accessed)
(
    select
       id, version, store_id, uuid, transaction_id, (case when 1 = node_deleted then ${DELETED_TYPE_ID} else type_qname_id end), locale_id, acl_id,
       audit_creator, audit_created, audit_modifier, audit_modified, audit_accessed
    from
       t_alf_node
    where
       id >= ${LOWERBOUND} AND id <= ${UPPERBOUND}
);

drop table t_alf_node;


alter table alf_store
    add index fk_alf_store_root(root_node_id),
    add constraint fk_alf_store_root foreign key (root_node_id) references alf_node(id);

alter table alf_child_assoc
    add constraint fk_alf_cass_cnode foreign key (child_node_id) references alf_node(id),
    add constraint fk_alf_cass_pnode foreign key (parent_node_id) references alf_node(id);

alter table alf_node_assoc
    add constraint fk_alf_nass_snode foreign key (source_node_id) references alf_node(id),
    add constraint fk_alf_nass_tnode foreign key (target_node_id) references alf_node(id);

alter table alf_subscriptions
    add constraint fk_alf_sub_user foreign key (user_node_id) references alf_node(id) on delete cascade,
    add constraint fk_alf_sub_node foreign key (node_id) references alf_node(id) on delete cascade;

alter table alf_node_properties add constraint fk_alf_nprop_n foreign key (node_id) references alf_node(id);

alter table alf_usage_delta add constraint fk_alf_usaged_n foreign key (node_id) references alf_node(id);

alter table alf_node_aspects add constraint fk_alf_nasp_n foreign key (node_id) references alf_node(id);



--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-NodeDeleted';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-NodeDeleted', 'Manually executed script upgrade V4.1: Remove node_deleted',
    0, 6014, -1, 6015, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );