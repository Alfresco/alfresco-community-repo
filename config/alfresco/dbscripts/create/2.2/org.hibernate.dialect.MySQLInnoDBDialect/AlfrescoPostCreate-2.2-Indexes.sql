--
-- Title:      Post-Create Indexes
-- Database:   MySQL
-- Since:      V2.2 Schema 84
-- Author:     Derek Hulley
--
-- Hibernate only generates indexes on foreign key columns for MySQL.
-- There are also certain relationships that can not be declared in Hibernate but
-- still need to be maintained and need indexes.
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

--
-- Explicit indexes not declared in the mappings
--

CREATE INDEX fk_alf_na_qn ON alf_node_aspects (qname_id);

CREATE INDEX fk_alf_np_qn ON alf_node_properties (qname_id);

CREATE INDEX fk_avm_na_qn ON avm_aspects_new (qname_id);

CREATE INDEX fk_avm_np_qn ON avm_node_properties_new (qname_id);

--
-- Foreign Key indexes
-- These are auto-generated for MySQL
--
