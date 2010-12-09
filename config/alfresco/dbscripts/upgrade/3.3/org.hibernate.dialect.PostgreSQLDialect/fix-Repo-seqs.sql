--
-- Title:      Upgrade to V3.3 - Create repo sequences
-- Database:   PostgreSQL
-- Since:      V3.4 schema 4105
-- Author:     unknown
--
-- creates sequences for repo tables
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--ASSIGN:hibernate_seq_next_value=value
SELECT NEXTVAL('hibernate_sequence') AS value;

CREATE SEQUENCE alf_namespace_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;   -- (optional)

CREATE SEQUENCE alf_qname_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;       -- (optional)

CREATE SEQUENCE alf_permission_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_ace_context_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_authority_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_access_control_entry_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_acl_change_set_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_access_control_list_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_acl_member_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_authority_alias_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_server_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_transaction_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_store_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_node_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_child_assoc_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_locale_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_attributes_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_node_assoc_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1;

CREATE SEQUENCE alf_usage_delta_seq START WITH ${hibernate_seq_next_value} INCREMENT BY 1; -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-Fix-Repo-Seqs';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-Fix-Repo-Seqs', 'Manually executed script upgrade V3.3 to create repo sequences',
     0, 4104, -1, 4105, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
