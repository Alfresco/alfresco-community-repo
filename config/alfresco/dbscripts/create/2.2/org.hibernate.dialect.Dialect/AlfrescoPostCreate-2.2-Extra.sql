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

CREATE INDEX fk_alf_cass_tqn ON alf_child_assoc (type_qname_id);
ALTER TABLE alf_child_assoc ADD CONSTRAINT fk_alf_cass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_cass_qnns ON alf_child_assoc (qname_ns_id);
ALTER TABLE alf_child_assoc ADD CONSTRAINT fk_alf_cass_qnns FOREIGN KEY (qname_ns_id) REFERENCES alf_namespace (id);

CREATE INDEX fk_alf_node_tqn ON alf_node (type_qname_id);
ALTER TABLE alf_node ADD CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_nasp_qn ON alf_node_aspects (qname_id);
ALTER TABLE alf_node_aspects ADD CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_nass_tqn ON alf_node_assoc (type_qname_id);
ALTER TABLE alf_node_assoc ADD CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_nprop_qn ON alf_node_properties (qname_id);
ALTER TABLE alf_node_properties ADD CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id);

CREATE INDEX fk_alf_nprop_loc ON alf_node_properties (locale_id);
ALTER TABLE alf_node_properties ADD CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id);

CREATE INDEX fk_alf_perm_tqn ON alf_permission (type_qname_id);
ALTER TABLE alf_permission ADD CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id);

