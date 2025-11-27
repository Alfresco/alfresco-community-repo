--
-- Title:      Merge alf_prop_string_value_seq and alf_prop_serializable_value_seq
-- Database:   PostgreSQL
-- Since:      V7.2.0
-- Author:     Your Name
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Create new merged sequence with initial value
CREATE SEQUENCE alf_prop_string_value_seq_unified START WITH 1 INCREMENT BY 1;

-- Set sequence to start from maximum of both old sequences
SELECT setval('alf_prop_string_value_seq_unified', 
  GREATEST(
    COALESCE((SELECT last_value FROM alf_prop_string_value_seq), 0),
    COALESCE((SELECT last_value FROM alf_prop_serializable_value_seq), 0)
  ) + 1,
  false
);

-- Drop old sequences
DROP SEQUENCE IF EXISTS alf_prop_string_value_seq;
DROP SEQUENCE IF EXISTS alf_prop_serializable_value_seq;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-v25.3-merge-alf_prop_string-sequences';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-v25.3-merge-alf_prop_string-sequences', 'Merge alf_prop_string_value_seq and alf_prop_serializable_value_seq into single sequence',
    0, 21000, -1, 21001, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Property value sequences merged successfully'
  );