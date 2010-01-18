--
-- Title:      Core Repository Tables
-- Database:   MySQL InnoDB
-- Since:      V3.3 Schema 4000
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_applied_patch
(
    id VARCHAR(64) NOT NULL,
    description TEXT,
    fixes_from_schema INTEGER,
    fixes_to_schema INTEGER,
    applied_to_schema INTEGER,
    target_schema INTEGER,
    applied_on_date DATETIME,
    applied_to_server VARCHAR(64),
    was_executed BIT,
    succeeded BIT,
    report TEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
