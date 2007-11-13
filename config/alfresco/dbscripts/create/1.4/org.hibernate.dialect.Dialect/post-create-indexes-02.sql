--
-- More post-creation indexes. (Generic Schema 1.4)
--
-- These are not declared in the Hibernate mappings.
--

-- Association QNames
CREATE INDEX idx_ca_type_qname ON alf_child_assoc (type_qname);
CREATE INDEX idx_ca_qname ON alf_child_assoc (qname);
CREATE INDEX idx_na_type_qname ON alf_node_assoc (type_qname);
