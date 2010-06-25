--
-- Title:      Increase the ipAddress field length to allow IPv6 adresses
-- Database:   MySQL
-- Since:      V3.1 schema 1009
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

ALTER TABLE alf_server MODIFY ip_address varchar(39) NOT NULL;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.1-Allow-IPv6';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.1-Allow-IPv6', 'Manually executed script upgrade V3.1: Increase the ipAddress field length',
     0, 2019, -1, 2020, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
