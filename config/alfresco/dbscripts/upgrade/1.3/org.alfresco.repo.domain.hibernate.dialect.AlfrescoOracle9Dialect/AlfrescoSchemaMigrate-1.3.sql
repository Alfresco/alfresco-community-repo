-- ------------------------------------------------------
-- Alfresco Schema conversion V1.2.1 to V1.3
--
-- For Oracle.
--
-- Note: This script does not create a temporary
--       properties table.  It updates the existing
--       table as it is not possible to insert..select
--       long raw columns in Oracle.
--
-- Author: David Caruana
-- ------------------------------------------------------

--
-- Create temporary 1.3 schema
--

CREATE TABLE T_access_control_entry (
  id number(19,0) NOT NULL,
  protocol varchar2(50) default NULL,
  identifier varchar2(100) default NULL,
  uuid varchar2(36) default NULL,
  typeUri varchar2(100) default NULL,
  typeName varchar2(100) default NULL,
  name varchar2(100) default NULL,
  recipient varchar2(100) default NULL,
  acl_id number(19, 0),
  permission_id number(19, 0),
  authority_id varchar2(100),
  allowed number(1, 0) NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX IDX_ACE_REF ON T_access_control_entry (protocol, identifier, uuid);

CREATE TABLE T_access_control_list
(
  id number(19,0) not null,
  protocol varchar2(50) NOT NULL,
  identifier varchar2(100) NOT NULL,
  uuid varchar2(36) NOT NULL,
  inherits number(1,0) NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX IDX_ACL_REF ON T_access_control_list (protocol, identifier, uuid);

create table T_auth_ext_keys
(
  id varchar2(100) not null,
  externalKey varchar2(100) not null,
  primary key (id, externalKey)
);

create table T_authority
(
  recipient varchar2(100) not null,
  primary key (recipient)
);

CREATE TABLE T_child_assoc
(
  id number(19,0) NOT NULL,
  parent_node_id number(19,0) default NULL,
  parent_protocol varchar(50) default NULL,
  parent_identifier varchar(100) default NULL,
  parent_uuid varchar(36) default NULL,
  child_node_id number(19,0) default NULL,
  child_protocol varchar(50) default NULL,
  child_identifier varchar(100) default NULL,
  child_uuid varchar(36) default NULL,
  type_qname varchar(255) NOT NULL,
  qname varchar(255) NOT NULL,
  is_primary number(1,0) default NULL,
  assoc_index number(10,0) default NULL,
  PRIMARY KEY (id)
);
CREATE INDEX IDX_CA_PARENT ON T_child_assoc(parent_protocol, parent_identifier, parent_uuid);
CREATE INDEX IDX_CA_CHILD ON T_child_assoc(child_protocol, child_identifier, child_uuid);

CREATE TABLE T_node
(
  id number(19,0) NOT NULL,
  protocol varchar2(50) NOT NULL,
  identifier varchar2(100) NOT NULL,
  uuid varchar2(36) NOT NULL,
  acl_id number(19,0) default NULL,
  type_qname varchar2(255) NOT NULL,
  PRIMARY KEY  (id)
);
CREATE INDEX IDX_NODE_REF ON T_node(protocol, identifier, uuid);

CREATE TABLE T_node_aspects
(
  protocol varchar2(50) NOT NULL,
  identifier varchar2(100) NOT NULL,
  uuid varchar2(36) NOT NULL,
  node_id number(19,0),
  qname varchar2(200) default NULL
);
CREATE INDEX IDX_ASPECTS_REF ON T_node_aspects(protocol, identifier, uuid);

CREATE TABLE T_node_assoc
(
  id number(19,0) NOT NULL,
  source_node_id number(19,0) default NULL,
  source_protocol varchar2(50) default NULL,
  source_identifier varchar2(100) default NULL,
  source_uuid varchar2(36) default NULL,
  target_node_id number(19,0) default NULL,
  target_protocol varchar2(50) default NULL,
  target_identifier varchar2(100) default NULL,
  target_uuid varchar2(36) default NULL,
  type_qname varchar2(255) NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX IDX_NA_SOURCE on T_node_assoc(source_protocol, source_identifier, source_uuid);
CREATE INDEX IDX_NA_TARGET on T_node_assoc(target_protocol, target_identifier, target_uuid);

CREATE TABLE T_node_status
(
  protocol varchar2(50) NOT NULL,
  identifier varchar2(100) NOT NULL,
  guid varchar2(36) NOT NULL,
  node_id number(19,0) default NULL,
  change_txn_id varchar2(56) NOT NULL,
  deleted number(1,0) NOT NULL,
  primary key (protocol, identifier, guid)
);

CREATE TABLE T_permission
(
  id number(19,0) NOT NULL,
  type_qname varchar2(200) NOT NULL,
  name varchar2(100) NOT NULL,
  PRIMARY KEY (id),
  unique (type_qname, name)
);

CREATE TABLE T_store
(
  protocol varchar2(50) NOT NULL,
  identifier varchar2(100) NOT NULL,
  root_node_id number(19,0) default NULL,
  primary key (protocol, identifier)
);

CREATE TABLE T_version_count
(
  protocol varchar2(50) NOT NULL,
  identifier varchar2(100) NOT NULL,
  version_count number(10,0) NOT NULL,
  primary key (protocol, identifier)
);

create sequence hibernate_sequence;


--
-- Copy data from old tables to intermediate tables
--

insert into T_store (protocol, identifier)
  select protocol, identifier from store;

insert into T_node (id, protocol, identifier, uuid, type_qname)
  select hibernate_sequence.nextval, protocol, identifier, guid, type_qname from node;

update T_store tstore set root_node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tstore.protocol and
    tnode.identifier = tstore.identifier and
    tnode.uuid =
    (select ostore.root_guid from store ostore where
      ostore.protocol = tstore.protocol and
      ostore.identifier = tstore.identifier
    )
  );

insert into t_version_count (protocol, identifier, version_count)
  select protocol, identifier, version_count from version_count;

insert into t_node_status (protocol, identifier, guid, change_txn_id, deleted)
  select protocol, identifier, guid, change_txn_id, deleted from node_status;
update T_node_status tstatus set node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tstatus.protocol and
    tnode.identifier = tstatus.identifier and
    tnode.uuid = tstatus.guid
  );


insert into T_node_aspects
  (
    protocol, identifier, uuid, qname
  )
  select
      protocol, identifier, guid, qname
    from node_aspects;
update T_node_aspects taspects set node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = taspects.protocol and
    tnode.identifier = taspects.identifier and
    tnode.uuid = taspects.uuid
  );

insert into T_child_assoc
  (
    id, parent_protocol, parent_identifier, parent_uuid,
    child_protocol, child_identifier, child_uuid,
    type_qname, qname, is_primary, assoc_index
  )
  select
    hibernate_sequence.nextval, parent_protocol, parent_identifier, parent_guid,
    child_protocol, child_identifier, child_guid,
    type_qname, qname, isPrimary, assoc_index
  from
    child_assoc;
update T_child_assoc tassoc set parent_node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tassoc.parent_protocol and
    tnode.identifier = tassoc.parent_identifier and
    tnode.uuid = tassoc.parent_uuid
  );
update T_child_assoc tassoc set child_node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tassoc.child_protocol and
    tnode.identifier = tassoc.child_identifier and
    tnode.uuid = tassoc.child_uuid
  );

insert into T_node_assoc
  (
    id, source_protocol, source_identifier, source_uuid,
    target_protocol, target_identifier, target_uuid,
    type_qname
  )
  select
    hibernate_sequence.nextval, source_protocol, source_identifier, source_guid,
    target_protocol, target_identifier, target_guid,
    type_qname
  from
    node_assoc;
update T_node_assoc tassoc set source_node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tassoc.source_protocol and
    tnode.identifier = tassoc.source_identifier and
    tnode.uuid = tassoc.source_uuid
  );
update T_node_assoc tassoc set target_node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tassoc.target_protocol and
    tnode.identifier = tassoc.target_identifier and
    tnode.uuid = tassoc.target_uuid
  );

insert into T_permission
   (
     id, type_qname, name
   )
   select
     hibernate_sequence.nextval, '{' || type_uri || '}' || type_name, name
   from
     permission_ref;

insert into T_access_control_list
  (
    id, protocol, identifier, uuid, inherits
  )
  select
      hibernate_sequence.nextval, protocol, identifier, guid, inherits
    from node_permission;
update T_node tnode set acl_id =
  (select tacl.id from T_access_control_list tacl where
    tacl.protocol = tnode.protocol and
    tacl.identifier = tnode.identifier and
    tacl.uuid = tnode.uuid
  );

insert into T_auth_ext_keys
  (
    id, externalKey
  )
  select
    id, externalKey
  from
    externalkeys;

insert into T_authority
  (
    recipient
  )
  select
    recipient
  from
    recipient;

insert into T_access_control_entry
  (
    id, protocol, identifier, uuid,
    typeUri, typeName, name,
    recipient,
    allowed
  )
  select
    hibernate_sequence.nextval, e.protocol, e.identifier, e.guid,
    e.typeUri, e.typeName, e.name,
    e.recipient,
    e.allowed
  from node_perm_entry e join t_node n on e.protocol = n.protocol and e.identifier = n.identifier and e.guid = n.uuid
  ;

update T_access_control_entry tentry
  set
    acl_id =
    (
      select
        tacl.id
      from T_access_control_list tacl
      join T_node tnode on tacl.id = tnode.acl_id
      where
        tnode.protocol = tentry.protocol and
        tnode.identifier = tentry.identifier and
        tnode.uuid = tentry.uuid
    );
update T_access_control_entry tentry
  set
    tentry.permission_id =
    (
      select
        tpermission.id
      from T_permission tpermission
      where
        tpermission.type_qname = '{' || tentry.typeUri || '}' || tentry.typeName and
        tpermission.name = tentry.name
    );
update T_access_control_entry tentry
  set
    tentry.authority_id =
    (
      select
        tauthority.recipient
      from T_authority tauthority
      where
        tauthority.recipient = tentry.recipient
    );
delete from T_access_control_list where id not in (select distinct(acl_id) id from t_access_control_entry where acl_id is not null);
delete from T_access_control_entry where acl_id is null;
update T_node set acl_id = null where acl_id not in (select id from t_access_control_list);

--
-- Create New schema (Oracle)
--

DROP TABLE child_assoc cascade constraints;
DROP TABLE node_assoc cascade constraints;
DROP TABLE node_aspects cascade constraints;
DROP TABLE node cascade constraints;
DROP TABLE node_status cascade constraints;
DROP TABLE version_count cascade constraints;
DROP TABLE store cascade constraints;
DROP TABLE node_perm_entry cascade constraints;
DROP TABLE node_permission cascade constraints;
DROP TABLE permission_ref cascade constraints;
DROP TABLE recipient cascade constraints;
DROP TABLE externalKeys cascade constraints;

create table access_control_entry
(
       id number(19,0) not null, 
       acl_id number(19,0) not null, 
       permission_id number(19,0) not null, 
       authority_id varchar2(100) not null, 
       allowed number(1,0) not null, 
       primary key (id), 
       unique (acl_id, permission_id, authority_id)
);

create table access_control_list
(
       id number(19,0) not null, 
       inherits number(1,0) not null, 
       primary key (id)
);

create table auth_ext_keys
(
       id varchar2(100) not null,
       externalKey varchar2(100) not null,
       primary key (id, externalKey)
);

create table authority
(
       recipient varchar2(100) not null,
       primary key (recipient)
);

create table child_assoc
(
       id number(19,0) not null,
       parent_node_id number(19,0),
       child_node_id number(19,0),
       type_qname varchar2(255) not null,
       qname varchar2(255) not null,
       is_primary number(1,0),
       assoc_index number(10,0),
       primary key (id)
);

create table node
(
       id number(19,0) not null,
       protocol varchar2(50) not null,
       identifier varchar2(100) not null,
       uuid varchar2(36) not null,
       type_qname varchar2(255) not null,
       acl_id number(19,0),
       primary key (id),
       unique (protocol, identifier, uuid)
);

create table node_aspects
(
       node_id number(19,0) not null,
       qname varchar2(200)
);

create table node_assoc
(
       id number(19,0) not null,
       source_node_id number(19,0),
       target_node_id number(19,0),
       type_qname varchar2(255) not null,
       primary key (id)
);

create table node_status
(
       protocol varchar2(50) not null,
       identifier varchar2(100) not null,
       guid varchar2(36) not null,
       node_id number(19,0),
       change_txn_id varchar2(56) not null,
       primary key (protocol, identifier, guid)
);

create table permission
(
       id number(19,0) not null,
       type_qname varchar2(200) not null,
       name varchar2(100) not null,
       primary key (id),
       unique (type_qname, name)
);

create table store
(
       protocol varchar2(50) not null,
       identifier varchar2(100) not null,
       root_node_id number(19,0),
       primary key (protocol, identifier)
);

create table version_count
(
       protocol varchar2(100) not null,
       identifier varchar2(100) not null,
       version_count number(10,0) not null,
       primary key (protocol, identifier)
);


--
-- Copy data into new schema
--

insert into store
  (
    protocol, identifier, root_node_id
  )
  select
    protocol, identifier, root_node_id
  from
    T_store;

insert into node
  (
    id, protocol, identifier, uuid, type_qname, acl_id
  )
  select
    id, protocol, identifier, uuid, type_qname, acl_id
  from
    T_node;

insert into version_count
  (
    protocol, identifier, version_count
  )
  select
    protocol, identifier, version_count
  from
    T_version_count;

insert into node_status
  (
    protocol, identifier, guid, node_id, change_txn_id
  )
  select
    protocol, identifier, guid, node_id, change_txn_id
  from
    T_node_status;


alter table node_properties add (node_id number(19,0));

update node_properties tproperties set node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tproperties.protocol and
    tnode.identifier = tproperties.identifier and
    tnode.uuid = tproperties.guid
  );

alter table node_properties modify (node_id number(19,0) not null);
alter table node_properties drop primary key;
alter table node_properties add primary key (node_id, qname);
alter table node_properties drop column protocol;
alter table node_properties drop column identifier;
alter table node_properties drop column guid;


insert into node_aspects
  (
    node_id, qname
  )
  select
      node_id, qname
  from
    T_node_aspects;

insert into child_assoc
  (
    id, parent_node_id, child_node_id, type_qname, qname, is_primary, assoc_index
  )
  select
    id, parent_node_id, child_node_id, type_qname, qname, is_primary, assoc_index
  from
    T_child_assoc;

insert into node_assoc
  (
    id, source_node_id, target_node_id, type_qname
  )
  select
    id, source_node_id, target_node_id, type_qname
  from
    T_node_assoc;

insert into permission
   (
     id, type_qname, name
   )
   select
     id, type_qname, name
   from
     T_permission;

insert into access_control_list
  (
    id, inherits
  )
  select
    id, inherits
  from
    T_access_control_list;

insert into auth_ext_keys
  (
    id, externalKey
  )
  select
    id, externalKey
  from
    T_auth_ext_keys;

insert into authority
  (
    recipient
  )
  select
    recipient
  from
    T_authority;

insert into access_control_entry
  (
    id, acl_id, permission_id, authority_id, allowed
  )
  select
    id, acl_id, permission_id, authority_id, allowed
  from
    T_access_control_entry;


-- Enable constraints

alter table access_control_entry add constraint FKF064DF7560601995 foreign key (permission_id) references permission;
alter table access_control_entry add constraint FKF064DF75B25A50BF foreign key (authority_id) references authority;
alter table access_control_entry add constraint FKF064DF75B9553F6C foreign key (acl_id) references access_control_list;
alter table auth_ext_keys add constraint FK31D3BA097B7FDE43 foreign key (id) references authority;
alter table child_assoc add constraint FKC6EFFF3274173FF4 foreign key (child_node_id) references node;
alter table child_assoc add constraint FKC6EFFF328E50E582 foreign key (parent_node_id) references node;
alter table node add constraint FK33AE02B9553F6C foreign key (acl_id) references access_control_list;
alter table node add constraint FK33AE02D24ADD25 foreign key (protocol, identifier) references store;
alter table node_properties add constraint FKC962BF907F2C8017 foreign key  (node_id) references node;
alter table node_aspects add constraint FK2B91A9DE7F2C8017 foreign key (node_id) references node;
alter table node_assoc add constraint FK5BAEF398B69C43F3 foreign key (source_node_id) references node;
alter table node_assoc add constraint FK5BAEF398A8FC7769 foreign key (target_node_id) references node;
alter table node_status add constraint FK38ECB8CF7F2C8017 foreign key (node_id) references node;
alter table store add constraint FK68AF8E122DBA5BA foreign key (root_node_id) references node;

-- Add additional indexes
CREATE INDEX FKF064DF7560601995 ON access_control_entry (permission_id);
CREATE INDEX FKF064DF75B25A50BF ON access_control_entry (authority_id);
CREATE INDEX FKF064DF75B9553F6C ON access_control_entry (acl_id);
CREATE INDEX FK31D3BA097B7FDE43 ON auth_ext_keys (id);
CREATE INDEX FKC6EFFF3274173FF4 ON child_assoc (child_node_id);
CREATE INDEX FKC6EFFF328E50E582 ON child_assoc (parent_node_id);
CREATE INDEX FK33AE02B9553F6C ON node (acl_id);
CREATE INDEX FK33AE02D24ADD25 ON node (protocol, identifier);
CREATE INDEX FK2B91A9DE7F2C8017 ON node_aspects (node_id);
CREATE INDEX FK5BAEF398B69C43F3 ON node_assoc (source_node_id);
CREATE INDEX FK5BAEF398A8FC7769 ON node_assoc (target_node_id);
CREATE INDEX FKC962BF907F2C8017 ON node_properties (node_id);
CREATE INDEX FK38ECB8CF7F2C8017 ON node_status (node_id);
CREATE INDEX FK68AF8E122DBA5BA ON store (root_node_id);

ALTER TABLE applied_patch MODIFY id varchar(64);
