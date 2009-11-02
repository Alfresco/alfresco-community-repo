--
-- Title:      Additional Indexes
-- Database:   Generic
-- Since:      V3.2 schema 2023
-- Author:     davew
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Additional indexes

CREATE INDEX idx_alf_cass_qncrc on alf_child_assoc (qname_crc, type_qname_id, parent_node_id);
