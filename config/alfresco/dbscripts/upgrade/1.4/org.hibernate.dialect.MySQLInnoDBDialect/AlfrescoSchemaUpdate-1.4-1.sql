-- ------------------------------------------------------
-- Alfresco Schema conversion V1.3 to V1.4 Part 1 (MySQL)
--
-- Adds the columns required to enforce the duplicate name detection
-- 
-- Author: Derek Hulley
-- ------------------------------------------------------

--
-- Delete intermediate tables from previous upgrades
--

DROP TABLE IF EXISTS T_access_control_entry;
DROP TABLE IF EXISTS T_access_control_list;
DROP TABLE IF EXISTS T_applied_patch;
DROP TABLE IF EXISTS T_auth_ext_keys;
DROP TABLE IF EXISTS T_authority;
DROP TABLE IF EXISTS T_child_assoc;
DROP TABLE IF EXISTS T_node;
DROP TABLE IF EXISTS T_node_aspects;
DROP TABLE IF EXISTS T_node_assoc;
DROP TABLE IF EXISTS T_node_properties;
DROP TABLE IF EXISTS T_node_status;
DROP TABLE IF EXISTS T_permission;
DROP TABLE IF EXISTS T_store;
DROP TABLE IF EXISTS T_version_count;

--
-- Upgrades to 1.3 of MyIsam tables could have missed the applied_patch table InnoDB
--
ALTER TABLE applied_patch        ENGINE = InnoDB;

--
-- Unique name constraint
--

-- Apply new schema changes to child assoc table
ALTER TABLE child_assoc
  ADD COLUMN child_node_name VARCHAR(50) NOT NULL DEFAULT 'V1.4 upgrade' AFTER type_qname,
  ADD COLUMN child_node_name_crc bigint(20) NOT NULL DEFAULT -1 AFTER child_node_name;

UPDATE child_assoc
  SET child_node_name_crc = id * -1;

ALTER TABLE child_assoc
  ADD UNIQUE INDEX IDX_CHILD_NAMECRC(parent_node_id, type_qname, child_node_name, child_node_name_crc);

-- Apply unique index for node associations
ALTER TABLE node_assoc
  ADD UNIQUE INDEX IDX_ASSOC(source_node_id, type_qname, target_node_id);

--
-- Rename tables to give 'alf_' prefix
--
ALTER TABLE access_control_entry  RENAME TO alf_access_control_entry;
ALTER TABLE access_control_list   RENAME TO alf_access_control_list;
ALTER TABLE applied_patch         RENAME TO alf_applied_patch;
ALTER TABLE auth_ext_keys         RENAME TO alf_auth_ext_keys;
ALTER TABLE authority             RENAME TO alf_authority;
ALTER TABLE child_assoc           RENAME TO alf_child_assoc;
ALTER TABLE node                  RENAME TO alf_node;
ALTER TABLE node_aspects          RENAME TO alf_node_aspects;
ALTER TABLE node_assoc            RENAME TO alf_node_assoc;
ALTER TABLE node_properties       RENAME TO alf_node_properties;
ALTER TABLE node_status           RENAME TO alf_node_status;
ALTER TABLE permission            RENAME TO alf_permission;
ALTER TABLE store                 RENAME TO alf_store;
ALTER TABLE version_count         RENAME TO alf_version_count;

--
-- The table renames will cause Hibernate to rehash the FK constraint names.
-- For MySQL, Hibernate will generate scripts to add the appropriate constraints
-- and indexes.
--
ALTER TABLE alf_access_control_entry
  DROP FOREIGN KEY FKF064DF7560601995,
  DROP INDEX       FKF064DF7560601995,
  DROP FOREIGN KEY FKF064DF75B25A50BF,
  DROP INDEX       FKF064DF75B25A50BF,
  DROP FOREIGN KEY FKF064DF75B9553F6C,
  DROP INDEX       FKF064DF75B9553F6C;
ALTER TABLE alf_auth_ext_keys
  DROP FOREIGN KEY FK31D3BA097B7FDE43,
  DROP INDEX       FK31D3BA097B7FDE43;
ALTER TABLE alf_child_assoc
  DROP FOREIGN KEY FKC6EFFF3274173FF4,
  DROP INDEX       FKC6EFFF3274173FF4,
  DROP FOREIGN KEY FKC6EFFF328E50E582,
  DROP INDEX       FKC6EFFF328E50E582;(optional)
ALTER TABLE alf_child_assoc
  DROP FOREIGN KEY FKFFC5468E74173FF4,
  DROP INDEX       FKFFC5468E74173FF4,
  DROP FOREIGN KEY FKFFC5468E8E50E582,
  DROP INDEX       FKFFC5468E8E50E582;(optional)
ALTER TABLE alf_node
  DROP FOREIGN KEY FK33AE02B9553F6C,
  DROP INDEX       FK33AE02B9553F6C;
ALTER TABLE alf_node
  DROP FOREIGN KEY FK33AE02D24ADD25,
  DROP INDEX       FK33AE02D24ADD25;
ALTER TABLE alf_node_properties
  DROP FOREIGN KEY FKC962BF907F2C8017,
  DROP INDEX       FKC962BF907F2C8017;
ALTER TABLE alf_node_aspects
  DROP FOREIGN KEY FK2B91A9DE7F2C8017,
  DROP INDEX       FK2B91A9DE7F2C8017;
ALTER TABLE alf_node_assoc
  DROP FOREIGN KEY FK5BAEF398B69C43F3,
  DROP INDEX       FK5BAEF398B69C43F3;
ALTER TABLE alf_node_assoc
  DROP FOREIGN KEY FK5BAEF398A8FC7769,
  DROP INDEX       FK5BAEF398A8FC7769;
ALTER TABLE alf_node_status
  DROP FOREIGN KEY FK38ECB8CF7F2C8017,
  DROP INDEX       FK38ECB8CF7F2C8017;
ALTER TABLE alf_store
  DROP FOREIGN KEY FK68AF8E122DBA5BA,
  DROP INDEX       FK68AF8E122DBA5BA;

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.schemaUpdateScript-V1.4-1';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.schemaUpdateScript-V1.4-1', 'Manually execute script upgrade V1.4 part 1',
    0, 19, -1, 20, now(), 'UNKOWN', 1, 1, 'Script completed'
  );