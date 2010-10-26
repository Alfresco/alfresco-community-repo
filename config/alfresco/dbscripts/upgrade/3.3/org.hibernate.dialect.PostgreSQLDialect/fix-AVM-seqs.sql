--
-- Title:      Upgrade to V3.4 - Create AVM sequences
-- Database:   PostgreSQL
-- Since:      V3.4 schema 4105
-- Author:     unknown
--
-- creates sequences for AVM tables
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--ASSIGN:hibernate_seq_next_value=value
SELECT NEXTVAL('hibernate_sequence') AS value;

CREATE SEQUENCE avm_stores_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;               -- (optional)

CREATE SEQUENCE avm_store_properties_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;     -- (optional)

CREATE SEQUENCE avm_nodes_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;                -- (optional)

CREATE SEQUENCE avm_version_roots_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;        -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-Fix-AVM-Seqs';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-Fix-AVM-Seqs', 'Manually executed script upgrade V3.3 to create AVM sequences',
     0, 4104, -1, 4105, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
