--
-- Title:      Upgrade to V3.3 - Create repo sequences
-- Database:   PostgreSQL
-- Since:      V3.3 schema 4005
-- Author:     unknown
--
-- creates sequences for repo tables
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE SEQUENCE alf_namespace_seq START WITH 1 INCREMENT BY 1;
SELECT SETVAL('alf_namespace_seq', NEXTVAL('hibernate_sequence'));

CREATE SEQUENCE alf_qname_seq START WITH 1 INCREMENT BY 1;
SELECT SETVAL('alf_qname_seq', CURRVAL('hibernate_sequence'));

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.3-Fix-Repo-Seqs';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.3-Fix-Repo-Seqs', 'Manually executed script upgrade V3.3 to create repo sequences',
     0, 4004, -1, 4005, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
