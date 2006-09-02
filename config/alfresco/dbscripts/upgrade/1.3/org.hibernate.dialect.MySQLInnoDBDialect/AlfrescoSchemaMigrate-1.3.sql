-- ------------------------------------------------------
-- Alfresco Schema conversion V1.2.1 to V1.3
-- 
-- Author: Derek Hulley
-- ------------------------------------------------------

--
-- Create temporary 1.3 schema
--

CREATE TABLE `T_access_control_entry` (
  `id` bigint(20) NOT NULL auto_increment,
  `protocol` varchar(50) default NULL,
  `identifier` varchar(100) default NULL,
  `uuid` varchar(36) default NULL,
  `typeUri` varchar(100) default NULL,
  `typeName` varchar(100) default NULL,
  `name` varchar(100) default NULL,
  `recipient` varchar(100) default NULL,
  `acl_id` bigint(20),
  `permission_id` bigint(20),
  `authority_id` varchar(100),
  `allowed` bit(1) NOT NULL,
  PRIMARY KEY  (`id`)
);
ALTER TABLE `T_access_control_entry` ADD INDEX `IDX_REF`(`protocol`, `identifier`, `uuid`);

CREATE TABLE `T_access_control_list` (
  `id` bigint(20) NOT NULL auto_increment,
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `inherits` bit(1) NOT NULL,
  PRIMARY KEY  (`id`)
);
ALTER TABLE `T_access_control_list` ADD INDEX `IDX_REF`(`protocol`, `identifier`, `uuid`);

CREATE TABLE `T_applied_patch` (
  `id` varchar(32) NOT NULL,
  `description` text,
  `fixes_from_schema` int(11) default NULL,
  `fixes_to_schema` int(11) default NULL,
  `applied_to_schema` int(11) default NULL,
  `target_schema` int(11) default NULL,
  `applied_on_date` datetime default NULL,
  `applied_to_server` varchar(64) default NULL,
  `was_executed` bit(1) default NULL,
  `succeeded` bit(1) default NULL,
  `report` text
);

CREATE TABLE `T_auth_ext_keys` (
  `id` varchar(100) NOT NULL,
  `externalKey` varchar(100) NOT NULL
);

CREATE TABLE `T_authority` (
  `recipient` varchar(100) NOT NULL
);

CREATE TABLE `T_child_assoc` (
  `id` bigint(20) NOT NULL auto_increment,
  `parent_node_id` bigint(20) default NULL,
  `parent_protocol` varchar(50) default NULL,
  `parent_identifier` varchar(100) default NULL,
  `parent_uuid` varchar(36) default NULL,
  `child_node_id` bigint(20) default NULL,
  `child_protocol` varchar(50) default NULL,
  `child_identifier` varchar(100) default NULL,
  `child_uuid` varchar(36) default NULL,
  `type_qname` varchar(255) NOT NULL,
  `qname` varchar(255) NOT NULL,
  `is_primary` bit(1) default NULL,
  `assoc_index` int(11) default NULL,
  PRIMARY KEY  (`id`)
);
ALTER TABLE `T_child_assoc` ADD INDEX `IDX_REF_PARENT`(`parent_protocol`, `parent_identifier`, `parent_uuid`);
ALTER TABLE `T_child_assoc` ADD INDEX `IDX_REF_CHILD`(`child_protocol`, `child_identifier`, `child_uuid`);

CREATE TABLE `T_node` (
  `id` bigint(20) NOT NULL auto_increment,
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `acl_id` bigint(20) default NULL,
  `type_qname` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
);
ALTER TABLE `T_node` ADD INDEX `IDX_REF`(`protocol`, `identifier`, `uuid`);

CREATE TABLE `T_node_aspects` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `node_id` bigint(20),
  `qname` varchar(200) default NULL
);
ALTER TABLE `T_node_aspects` ADD INDEX `IDX_REF`(`protocol`, `identifier`, `uuid`);

CREATE TABLE `T_node_assoc` (
  `id` bigint(20) NOT NULL auto_increment,
  `source_node_id` bigint(20) default NULL,
  `source_protocol` varchar(50) default NULL,
  `source_identifier` varchar(100) default NULL,
  `source_uuid` varchar(36) default NULL,
  `target_node_id` bigint(20) default NULL,
  `target_protocol` varchar(50) default NULL,
  `target_identifier` varchar(100) default NULL,
  `target_uuid` varchar(36) default NULL,
  `type_qname` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
);
ALTER TABLE `T_node_assoc` ADD INDEX `IDX_REF_SOURCE`(`source_protocol`, `source_identifier`, `source_uuid`);
ALTER TABLE `T_node_assoc` ADD INDEX `IDX_REF_TARGET`(`target_protocol`, `target_identifier`, `target_uuid`);

CREATE TABLE `T_node_properties` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `node_id` bigint(20),
  `actual_type` varchar(15) NOT NULL,
  `multi_valued` bit(1) NOT NULL,
  `persisted_type` varchar(15) NOT NULL,
  `boolean_value` bit(1) default NULL,
  `long_value` bigint(20) default NULL,
  `float_value` float default NULL,
  `double_value` double default NULL,
  `string_value` text,
  `serializable_value` blob,
  `qname` varchar(200) NOT NULL
);
ALTER TABLE `t_node_properties` ADD INDEX `IDX_REF`(`protocol`, `identifier`, `uuid`);

CREATE TABLE `T_node_status` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `guid` varchar(36) NOT NULL,
  `node_id` bigint(20) default NULL,
  `change_txn_id` varchar(56) NOT NULL,
  `deleted` bit(1) NOT NULL
);
ALTER TABLE `t_node_status` ADD INDEX `IDX_REF`(`protocol`, `identifier`, `guid`);

CREATE TABLE `T_permission` (
  `id` bigint(20) NOT NULL auto_increment,
  `type_qname` varchar(200) NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`)
);

CREATE TABLE `T_store` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `root_node_id` bigint(20) default NULL
);
ALTER TABLE `t_store` ADD INDEX `IDX_STORE_REF`(`protocol`, `identifier`);

CREATE TABLE `T_version_count` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `version_count` int(11) NOT NULL
);

--
-- Copy data from old tables to intermediate tables
--

insert into T_store (protocol, identifier)
  select protocol, identifier from store;

insert into T_node (protocol, identifier, uuid, type_qname)
  select protocol, identifier, guid, type_qname from node;

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

insert into T_node_properties
  (
    protocol, identifier, uuid, actual_type, multi_valued, persisted_type,
    boolean_value, long_value, float_value, double_value, string_value, serializable_value, qname
  )
  select
      protocol, identifier, guid, actual_type, multi_valued, persisted_type,
      boolean_value, long_value, float_value, double_value, string_value, serializable_value, qname
    from node_properties;
update T_node_properties tproperties set node_id =
  (select tnode.id from T_node tnode where
    tnode.protocol = tproperties.protocol and
    tnode.identifier = tproperties.identifier and
    tnode.uuid = tproperties.uuid
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
    parent_protocol, parent_identifier, parent_uuid,
    child_protocol, child_identifier, child_uuid,
    type_qname, qname, is_primary, assoc_index
  )
  select
    parent_protocol, parent_identifier, parent_guid,
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
    source_protocol, source_identifier, source_uuid,
    target_protocol, target_identifier, target_uuid,
    type_qname
  )
  select
    source_protocol, source_identifier, source_guid,
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
     type_qname, name
   )
   select
     CONCAT('{', type_uri, '}', type_name), name
   from
     permission_ref;

insert into T_access_control_list
  (
    protocol, identifier, uuid, inherits
  )
  select
      protocol, identifier, guid, inherits
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
    protocol, identifier, uuid,
    typeUri, typeName, name,
    recipient,
    allowed
  )
  select
    protocol, identifier, guid,
    typeUri, typeName, name,
    recipient,
    allowed
  from node_perm_entry;
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
        tpermission.type_qname = CONCAT('{', tentry.typeUri, '}', tentry.typeName) and
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
-- Create New schema (MySQL)
--

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE child_assoc;
DROP TABLE node_assoc;
DROP TABLE node_properties;
DROP TABLE node_aspects;
DROP TABLE node;
DROP TABLE node_status;
DROP TABLE version_count;
DROP TABLE store;
DROP TABLE node_perm_entry;
DROP TABLE node_permission;
DROP TABLE permission_ref;
DROP TABLE recipient;
DROP TABLE externalKeys;

CREATE TABLE `access_control_entry` (
  `id` bigint(20) NOT NULL auto_increment,
  `acl_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  `authority_id` varchar(100) NOT NULL,
  `allowed` bit(1) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `acl_id` (`acl_id`,`permission_id`,`authority_id`),
  KEY `FKF064DF7560601995` (`permission_id`),
  KEY `FKF064DF75B25A50BF` (`authority_id`),
  KEY `FKF064DF75B9553F6C` (`acl_id`),
  CONSTRAINT `FKF064DF75B9553F6C` FOREIGN KEY (`acl_id`) REFERENCES `access_control_list` (`id`),
  CONSTRAINT `FKF064DF7560601995` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`),
  CONSTRAINT `FKF064DF75B25A50BF` FOREIGN KEY (`authority_id`) REFERENCES `authority` (`recipient`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `access_control_list` (
  `id` bigint(20) NOT NULL auto_increment,
  `inherits` bit(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `auth_ext_keys` (
  `id` varchar(100) NOT NULL,
  `externalKey` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`,`externalKey`),
  KEY `FK31D3BA097B7FDE43` (`id`),
  CONSTRAINT `FK31D3BA097B7FDE43` FOREIGN KEY (`id`) REFERENCES `authority` (`recipient`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `authority` (
  `recipient` varchar(100) NOT NULL,
  PRIMARY KEY  (`recipient`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `child_assoc` (
  `id` bigint(20) NOT NULL auto_increment,
  `parent_node_id` bigint(20) default NULL,
  `child_node_id` bigint(20) default NULL,
  `type_qname` varchar(255) NOT NULL,
  `qname` varchar(255) NOT NULL,
  `is_primary` bit(1) default NULL,
  `assoc_index` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKFFC5468E74173FF4` (`child_node_id`),
  KEY `FKFFC5468E8E50E582` (`parent_node_id`),
  CONSTRAINT `FKFFC5468E8E50E582` FOREIGN KEY (`parent_node_id`) REFERENCES `node` (`id`),
  CONSTRAINT `FKFFC5468E74173FF4` FOREIGN KEY (`child_node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
FKFFC5468E74173FF4

CREATE TABLE `node` (
  `id` bigint(20) NOT NULL auto_increment,
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `type_qname` varchar(255) NOT NULL,
  `acl_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `protocol` (`protocol`,`identifier`,`uuid`),
  KEY `FK33AE02D24ADD25` (`protocol`,`identifier`),
  CONSTRAINT `FK33AE02D24ADD25` FOREIGN KEY (`protocol`, `identifier`) REFERENCES `store` (`protocol`, `identifier`),
  CONSTRAINT `FK33AE02B9553F6C` FOREIGN KEY (`acl_id`) REFERENCES `access_control_list` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `node_aspects` (
  `node_id` bigint(20) NOT NULL,
  `qname` varchar(200) default NULL,
  KEY `FK2B91A9DE7F2C8017` (`node_id`),
  CONSTRAINT `FK2B91A9DE7F2C8017` FOREIGN KEY (`node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `node_assoc` (
  `id` bigint(20) NOT NULL auto_increment,
  `source_node_id` bigint(20) default NULL,
  `target_node_id` bigint(20) default NULL,
  `type_qname` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK5BAEF398B69C43F3` (`source_node_id`),
  KEY `FK5BAEF398A8FC7769` (`target_node_id`),
  CONSTRAINT `FK5BAEF398A8FC7769` FOREIGN KEY (`target_node_id`) REFERENCES `node` (`id`),
  CONSTRAINT `FK5BAEF398B69C43F3` FOREIGN KEY (`source_node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `node_properties` (
  `node_id` bigint(20) NOT NULL,
  `actual_type` varchar(15) NOT NULL,
  `multi_valued` bit(1) NOT NULL,
  `persisted_type` varchar(15) NOT NULL,
  `boolean_value` bit(1) default NULL,
  `long_value` bigint(20) default NULL,
  `float_value` float default NULL,
  `double_value` double default NULL,
  `string_value` text,
  `serializable_value` blob,
  `qname` varchar(200) NOT NULL,
  PRIMARY KEY  (`node_id`,`qname`),
  KEY `FKC962BF907F2C8017` (`node_id`),
  CONSTRAINT `FKC962BF907F2C8017` FOREIGN KEY (`node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `node_status` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `guid` varchar(36) NOT NULL,
  `node_id` bigint(20) default NULL,
  `change_txn_id` varchar(56) NOT NULL,
  PRIMARY KEY  (`protocol`,`identifier`,`guid`),
  KEY `FK38ECB8CF7F2C8017` (`node_id`),
  CONSTRAINT `FK38ECB8CF7F2C8017` FOREIGN KEY (`node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `permission` (
  `id` bigint(20) NOT NULL auto_increment,
  `type_qname` varchar(200) NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `type_qname` (`type_qname`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `store` (
  `protocol` varchar(50) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `root_node_id` bigint(20) default NULL,
  PRIMARY KEY  (`protocol`,`identifier`),
  KEY `FK68AF8E122DBA5BA` (`root_node_id`),
  CONSTRAINT `FK68AF8E122DBA5BA` FOREIGN KEY (`root_node_id`) REFERENCES `node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `version_count` (
  `protocol` varchar(100) NOT NULL,
  `identifier` varchar(100) NOT NULL,
  `version_count` int(11) NOT NULL,
  PRIMARY KEY  (`protocol`,`identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

insert into node_properties
  (
    node_id, actual_type, multi_valued, persisted_type,
    boolean_value, long_value, float_value, double_value, string_value, serializable_value, qname
  )
  select
      node_id, actual_type, multi_valued, persisted_type,
      boolean_value, long_value, float_value, double_value, string_value, serializable_value, qname
  from
    T_node_properties;

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

SET FOREIGN_KEY_CHECKS = 1;


-- Allow longer patch identifiers

ALTER TABLE applied_patch MODIFY id varchar(64) not null;