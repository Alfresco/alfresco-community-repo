--
-- Title:      DROP unused IDX_AVM_VR_REVUQ index
-- Database:   PostgreSQL
-- Since:      V4.2 Schema 6031
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-19487 : ORACLE: AVM: Schema difference is present after upgrade from 3.4.13/4.0.2/4.1.5

DROP INDEX idx_avm_vr_revuq;
ALTER TABLE avm_version_roots DROP CONSTRAINT avm_version_roots_version_id_avm_store_id_key;    --(optional)
ALTER TABLE avm_version_roots DROP CONSTRAINT avm_version_roots_version_id_key;                 --(optional)
ALTER TABLE avm_version_roots ADD CONSTRAINT idx_avm_vr_uq UNIQUE (avm_store_id, version_id);

--
-- Record script finish
--

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-drop-AVM-index';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-drop-AVM-index', 'Manually executed script to drop unnecessary index',
    0, 6031, -1, 6032, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );