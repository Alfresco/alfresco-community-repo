--
-- Title:      Upgrade to V3.4 - AVM rename duplicates (if any)
-- Database:   MySQL
-- Since:      V3.4 schema 4209
-- Author:     dward
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
create table t_avm_child_entries (
    parent_id bigint not null,
    lc_name varchar(160) not null,
    name varchar(160) not null,
    child_id bigint not null,
    primary key (parent_id, lc_name)
) ENGINE=InnoDB;

--FOREACH avm_child_entries.child_id system.upgrade.t_avm_child_entries.batchsize
INSERT INTO t_avm_child_entries (parent_id, lc_name, name, child_id)
SELECT parent_id, LOWER(name), name, child_id
FROM avm_child_entries
WHERE child_id >= ${LOWERBOUND} AND child_id <= ${UPPERBOUND};

DROP TABLE avm_child_entries;
ALTER TABLE t_avm_child_entries RENAME TO avm_child_entries;

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

CREATE INDEX idx_avm_ce_lc_name ON avm_child_entries (lc_name, parent_id);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-AVM-index-child-entries-lower';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-AVM-index-child-entries-lower', 'Manually executed script upgrade V3.4',
     0, 6002, -1, 6003, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
