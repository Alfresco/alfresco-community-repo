-- ------------------------------------------------------
-- Alfresco Schema conversion V1.3 to V1.4 Part 1 (Oracle)
--
-- Adds the columns required to enforce the duplicate name detection
-- 
-- Author: Derek Hulley
-- ------------------------------------------------------

--
-- Unique name constraint
--

-- Apply new schema changes to child assoc table
ALTER TABLE child_assoc ADD
  (
    child_node_name VARCHAR2(50 CHAR) DEFAULT 'V1.4 upgrade' NOT NULL,
    child_node_name_crc NUMBER(19,0) DEFAULT -1 NOT NULL
  );

UPDATE child_assoc
  SET child_node_name_crc = id * -1;

CREATE UNIQUE INDEX IDX_CHILD_NAMECRC ON child_assoc (parent_node_id, type_qname, child_node_name, child_node_name_crc);

-- Apply unique index for node associations
CREATE UNIQUE INDEX IDX_ASSOC ON node_assoc (source_node_id, type_qname, target_node_id);

--
-- Rename tables to give 'alf_' prefix
--
ALTER TABLE access_control_entry RENAME TO alf_access_control_entry;
ALTER TABLE access_control_list  RENAME TO alf_access_control_list;
ALTER TABLE applied_patch        RENAME TO alf_applied_patch;
ALTER TABLE auth_ext_keys        RENAME TO alf_auth_ext_keys;
ALTER TABLE authority            RENAME TO alf_authority;
ALTER TABLE child_assoc          RENAME TO alf_child_assoc;
ALTER TABLE node                 RENAME TO alf_node;
ALTER TABLE node_aspects         RENAME TO alf_node_aspects;
ALTER TABLE node_assoc           RENAME TO alf_node_assoc;
ALTER TABLE node_properties      RENAME TO alf_node_properties;
ALTER TABLE node_status          RENAME TO alf_node_status;
ALTER TABLE permission           RENAME TO alf_permission;
ALTER TABLE store                RENAME TO alf_store;
ALTER TABLE version_count        RENAME TO alf_version_count;

--
-- The table renames will cause Hibernate to rehash the FK constraint names
--
ALTER TABLE alf_access_control_entry RENAME CONSTRAINT FKF064DF7560601995   TO   FKFFF41F9960601995;
ALTER TABLE alf_access_control_entry RENAME CONSTRAINT FKF064DF75B25A50BF   TO   FKFFF41F99B25A50BF;
ALTER TABLE alf_access_control_entry RENAME CONSTRAINT FKF064DF75B9553F6C   TO   FKFFF41F99B9553F6C;
ALTER TABLE alf_auth_ext_keys        RENAME CONSTRAINT FK31D3BA097B7FDE43   TO   FK8A749A657B7FDE43;
ALTER TABLE alf_child_assoc          RENAME CONSTRAINT FKC6EFFF3274173FF4   TO   FKFFC5468E74173FF4;
ALTER TABLE alf_child_assoc          RENAME CONSTRAINT FKC6EFFF328E50E582   TO   FKFFC5468E8E50E582;
ALTER TABLE alf_node                 RENAME CONSTRAINT FK33AE02B9553F6C     TO   FK60EFB626B9553F6C;
ALTER TABLE alf_node                 RENAME CONSTRAINT FK33AE02D24ADD25     TO   FK60EFB626D24ADD25;
ALTER TABLE alf_node_properties      RENAME CONSTRAINT FKC962BF907F2C8017   TO   FK7D4CF8EC7F2C8017;
ALTER TABLE alf_node_aspects         RENAME CONSTRAINT FK2B91A9DE7F2C8017   TO   FKD654E027F2C8017;
ALTER TABLE alf_node_assoc           RENAME CONSTRAINT FK5BAEF398B69C43F3   TO   FKE1A550BCB69C43F3;
ALTER TABLE alf_node_assoc           RENAME CONSTRAINT FK5BAEF398A8FC7769   TO   FKE1A550BCA8FC7769;
ALTER TABLE alf_node_status          RENAME CONSTRAINT FK38ECB8CF7F2C8017   TO   FK71C2002B7F2C8017;
ALTER TABLE alf_store                RENAME CONSTRAINT FK68AF8E122DBA5BA    TO   FKBD4FF53D22DBA5BA;

--
-- Rename the indexes to keep in synch with the new table names.  For Oracle, Hibernate doesn't create or add these
--
ALTER INDEX   FKF064DF7560601995 RENAME TO FKFFF41F9960601995;
ALTER INDEX   FKF064DF75B25A50BF RENAME TO FKFFF41F99B25A50BF;
ALTER INDEX   FKF064DF75B9553F6C RENAME TO FKFFF41F99B9553F6C;
ALTER INDEX   FK31D3BA097B7FDE43 RENAME TO FK8A749A657B7FDE43;
ALTER INDEX   FKC6EFFF3274173FF4 RENAME TO FKFFC5468E74173FF4;
ALTER INDEX   FKC6EFFF328E50E582 RENAME TO FKFFC5468E8E50E582;
ALTER INDEX   FK33AE02B9553F6C   RENAME TO FK60EFB626B9553F6C;
ALTER INDEX   FK33AE02D24ADD25   RENAME TO FK60EFB626D24ADD25;
ALTER INDEX   FKC962BF907F2C8017 RENAME TO FK7D4CF8EC7F2C8017;
ALTER INDEX   FK2B91A9DE7F2C8017 RENAME TO FKD654E027F2C8017;
ALTER INDEX   FK5BAEF398B69C43F3 RENAME TO FKE1A550BCB69C43F3;
ALTER INDEX   FK5BAEF398A8FC7769 RENAME TO FKE1A550BCA8FC7769;
ALTER INDEX   FK38ECB8CF7F2C8017 RENAME TO FK71C2002B7F2C8017;
ALTER INDEX   FK68AF8E122DBA5BA  RENAME TO FKBD4FF53D22DBA5BA;

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.schemaUpdateScript-V1.4-1';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.schemaUpdateScript-V1.4-1', 'Manually execute script upgrade V1.4 part 1',
    0, 19, -1, 20, sysdate, 'UNKOWN', 1, 1, 'Script completed'
  );
