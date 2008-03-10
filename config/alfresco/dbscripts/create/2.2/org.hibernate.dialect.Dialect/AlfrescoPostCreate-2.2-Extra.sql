--
-- Title:      Post-Create Constraints
-- Database:   Generic
-- Since:      V2.2 Schema 86
-- Author:     Derek Hulley
--
-- Certain Hibernate mappings don't allow constraints to be declared and are therefore
-- explicitly required.  All other constraints are automatically added by the
-- Hibernate-generated script.
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--
-- Explicit indexes and constraints not declared in the mappings
--

CREATE INDEX fk_alf_na_qn ON alf_node_aspects (qname_id);
ALTER TABLE alf_node_aspects ADD CONSTRAINT fk_alf_na_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_np_qn ON alf_node_properties (qname_id);
ALTER TABLE alf_node_properties ADD CONSTRAINT fk_alf_np_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_avm_na_qn ON avm_aspects_new (qname_id);
ALTER TABLE avm_aspects_new ADD CONSTRAINT fk_avm_na_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_avm_np_qn ON avm_node_properties_new (qname_id);
ALTER TABLE avm_node_properties_new ADD CONSTRAINT fk_avm_np_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX idx_avm_hl_revpk ON avm_history_links (descendent, ancestor);

CREATE INDEX idx_avm_vr_revuq ON avm_version_roots (avm_store_id, version_id); 
