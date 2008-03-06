--
-- Title:      Post-Create Constraints
-- Database:   Generic
-- Since:      V2.2 Schema 82
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

ALTER TABLE alf_node_aspects ADD CONSTRAINT fk_alf_na_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);
CREATE INDEX fk_alf_na_qn ON alf_node_aspects (qname_id);

ALTER TABLE alf_node_properties ADD CONSTRAINT fk_alf_np_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);
CREATE INDEX fk_alf_np_qn ON alf_node_properties (qname_id);

ALTER TABLE avm_aspects_new ADD CONSTRAINT fk_avm_na_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);
CREATE INDEX fk_avm_na_qn ON avm_aspects_new (qname_id);

ALTER TABLE avm_node_properties_new ADD CONSTRAINT fk_avm_np_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);
CREATE INDEX fk_avm_np_qn ON avm_node_properties_new (qname_id);
