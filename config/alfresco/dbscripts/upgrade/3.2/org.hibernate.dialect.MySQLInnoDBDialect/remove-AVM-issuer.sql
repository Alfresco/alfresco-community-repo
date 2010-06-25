--
-- Title:      Upgrade to V3.2 - Remove AVM Issuer 
-- Database:   MySQL
-- Since:      V3.2 schema 2008
-- Author:     janv
--
-- remove AVM node issuer - replace with auto-increment id
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- -----------------------------
-- Enable auto-increment --
-- -----------------------------

insert into avm_nodes (id, class_type, vers, version_id, guid, creator, owner, lastModifier, createDate, modDate, accessDate, is_root, store_new_id, acl_id, deletedType, layer_id, indirection, indirection_version, primary_indirection, opacity, content_url, mime_type, encoding, length)
select 
(select max(id)+1 from avm_nodes),
class_type, vers, version_id, guid, creator, owner, lastModifier, createDate, modDate, accessDate, is_root, store_new_id, acl_id, deletedType, layer_id, indirection, indirection_version, primary_indirection, opacity, content_url, mime_type, encoding, length
from avm_nodes where id = 0;

update avm_aspects set node_id = (select max(id) from avm_nodes) where node_id = 0;

update avm_child_entries set parent_id = (select max(id) from avm_nodes) where parent_id = 0;
update avm_child_entries set child_id = (select max(id) from avm_nodes) where child_id = 0;

update avm_history_links set ancestor = (select max(id) from avm_nodes) where ancestor = 0;
update avm_history_links set descendent = (select max(id) from avm_nodes) where descendent = 0;

update avm_merge_links set mfrom = (select max(id) from avm_nodes) where mfrom = 0;
update avm_merge_links set mto = (select max(id) from avm_nodes) where mto = 0;

update avm_node_properties set node_id = (select max(id) from avm_nodes) where node_id = 0;

update avm_stores set current_root_id = (select max(id) from avm_nodes) where current_root_id = 0;

update avm_version_roots set root_id = (select max(id) from avm_nodes) where root_id = 0;

delete from avm_nodes where id = 0;

alter table avm_nodes modify column id bigint not null auto_increment;

-- drop issuer table

drop table avm_issuer_ids;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-Remove-AVM-Issuer';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-Remove-AVM-Issuer', 'Manually executed script upgrade V3.2 to remove AVM Issuer',
     0, 2007, -1, 2008, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
