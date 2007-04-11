--
-- Foreign Key Indexes for PostgreSQL databases. (Generic Schema 1.4)
--

--
-- Record script finish
--
delete from alf_applied_patch where id = 'patch.db-V1.4-PostgresFKIndexes';
insert into alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  values
  (
    'patch.db-V1.4-PostgresFKIndexes', 'Manually execute script AlfrescoSchemaUpdate-1.4-PostgresFKIndexes.sql',
    21, 24, -1, 25, null, 'UNKOWN', 1, 1, 'Script completed'
  );