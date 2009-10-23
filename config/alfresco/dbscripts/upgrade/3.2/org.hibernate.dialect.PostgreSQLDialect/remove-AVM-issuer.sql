--
-- Title:      Upgrade to V3.2 - Remove AVM Issuer 
-- Database:   PostgreSQL
-- Since:      V3.2 schema 2008
-- Author:     janv
--
-- remove AVM node issuer - replace with sequence
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- update sequence, if needed

SELECT SETVAL('hibernate_sequence', GREATEST((MAX(id)+1), NEXTVAL('hibernate_sequence'))) FROM avm_nodes;

-- drop issuer table

DROP TABLE avm_issuer_ids;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-Remove-AVM-Issuer';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-Remove-AVM-Issuer', 'Manually executed script upgrade V3.2 to remove AVM Issuer',
     0, 2007, -1, 2008, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
   );

