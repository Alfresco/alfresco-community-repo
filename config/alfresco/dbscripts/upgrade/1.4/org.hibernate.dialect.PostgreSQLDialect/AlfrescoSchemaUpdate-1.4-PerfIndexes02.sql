--
-- Some explicit indexes to improve performance for various use-cases (PostgreSQL 1.4)
--

-- Association QNames
CREATE INDEX idx_ca_type_qname ON alf_child_assoc (type_qname);
CREATE INDEX idx_ca_qname ON alf_child_assoc (qname);
CREATE INDEX idx_na_type_qname ON alf_node_assoc (type_qname);

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.db-V1.4-PerfIndexes02';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.db-V1.4-PerfIndexes02', 'Executed script AlfrescoSchemaUpdate-1.4-PerfIndexes02.sql',
    0, 75, -1, 76, null, 'UNKOWN', TRUE, TRUE, 'Script completed'
  );