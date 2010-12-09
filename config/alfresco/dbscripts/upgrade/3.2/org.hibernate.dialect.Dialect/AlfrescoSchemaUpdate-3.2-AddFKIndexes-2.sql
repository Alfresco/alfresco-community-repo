--
-- Title:      Upgrade to V3.2 - Add extra FK indexes 
-- Database:   Generic
-- Since:      V3.2 schema 3503
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-5396: Missing FK indexes on non-MySQL databases
-- All statements are made optional to cater for varying introductions of tables
-- using create scripts that have been fixed up.

-- ==========
-- V3.2 diffs
-- ==========

-- alf_audit_* tables
CREATE INDEX fk_alf_aud_mod_cd ON alf_audit_model(content_data_id);         -- (optional)
CREATE INDEX fk_alf_aud_app_mod ON alf_audit_app(audit_model_id);           -- (optional)
CREATE INDEX fk_alf_aud_app_dis ON alf_audit_app(disabled_paths_id);        -- (optional)
CREATE INDEX fk_alf_aud_ent_app ON alf_audit_entry(audit_app_id);           -- (optional)
CREATE INDEX fk_alf_aud_ent_use ON alf_audit_entry(audit_user_id);          -- (optional)
CREATE INDEX fk_alf_aud_ent_pro ON alf_audit_entry(audit_values_id);        -- (optional)

-- Only missing on Oracle
ALTER TABLE avm_stores        
    ADD CONSTRAINT fk_avm_s_acl
    FOREIGN KEY (acl_id)
    REFERENCES alf_access_control_list (id);                                -- (optional)
CREATE INDEX fk_avm_s_acl ON avm_stores (acl_id);                           -- (optional)

-- alf_content_* tables
CREATE INDEX fk_alf_cont_url ON alf_content_data (content_url_id);          -- (optional)
CREATE INDEX fk_alf_cont_mim ON alf_content_data (content_mimetype_id);     -- (optional)
CREATE INDEX fk_alf_cont_enc ON alf_content_data (content_encoding_id);     -- (optional)
CREATE INDEX fk_alf_cont_loc ON alf_content_data (content_locale_id);       -- (optional)

-- alf_lock_* tables
CREATE INDEX fk_alf_lock_excl ON alf_lock (excl_resource_id);               -- (optional)

-- alf_prop_* tables
CREATE INDEX fk_alf_propln_key ON alf_prop_link(key_prop_id);               -- (optional)
CREATE INDEX fk_alf_propln_val ON alf_prop_link(value_prop_id);             -- (optional)
CREATE INDEX fk_alf_propuctx_v2 ON alf_prop_unique_ctx(value2_prop_id);     -- (optional)
CREATE INDEX fk_alf_propuctx_v3 ON alf_prop_unique_ctx(value3_prop_id);     -- (optional)

-- ==========
-- V3.4 diffs
-- ==========

CREATE INDEX fk_alf_propuctx_p1 ON alf_prop_unique_ctx(prop1_id);           -- (optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-AddFKIndexes-2';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-AddFKIndexes-2', 'Script fix for ALF-5396: Missing FK indexes on non-MySQL databases',
     0, 4111, -1, 4112, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
