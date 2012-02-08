--
-- Title:      Altering CLOB columns in the jBPM 3.3.1 tables to introduce Unicode characters support for jBPM 3.3.1
-- Database:   Generic
-- Since:      V3.4.8 schema 4208
-- Author:     Dmitry Velichkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-12411: Description of workflow in 'My Tasks To Do' dashboard losing utf-8 encoding after upgrade.

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-alter-jBPM331-CLOB-columns-to-nvarchar';
INSERT INTO
    alf_applied_patch
    (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
VALUES (
    'patch.db-V3.4-alter-jBPM331-CLOB-columns-to-nvarchar', 'Altering CLOB columns in the jBPM 3.3.1 tables to introduce Unicode characters support for jBPM 3.3.1',
    2018, 6000, -1, 6001, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
);
