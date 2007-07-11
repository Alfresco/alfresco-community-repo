--
-- Title:      Post-Create Foreign Key indexes
-- Database:   MySQL
-- Since:      V2.0 Schema 63
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

-- Remove pointless duplicated FK indexes
ALTER TABLE alf_global_attributes DROP INDEX FK64D0B9CF69B9F16A;


-- The MySQL dialects apply the FK indexes by default
