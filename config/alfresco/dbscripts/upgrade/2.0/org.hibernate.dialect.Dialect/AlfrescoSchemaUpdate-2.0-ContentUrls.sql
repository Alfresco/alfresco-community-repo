--
-- Title:      Indexes for alf_content_url table
-- Database:   Generic
-- Since:      V2.0 Schema 44
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
-- 

-- Content URLs
SELECT COUNT(*) FROM alf_content_url;
CREATE INDEX idx_alf_con_urls ON alf_content_url (content_url);(optional)

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.0-ContentUrls';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.0-ContentUrls', 'Manually executed script upgrade V2.0: Indexes for alf_content_url table',
    0, 123, -1, 124, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );