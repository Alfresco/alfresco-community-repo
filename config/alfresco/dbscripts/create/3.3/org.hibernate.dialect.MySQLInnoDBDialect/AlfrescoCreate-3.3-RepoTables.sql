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

CREATE TABLE alf_namespace
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    uri VARCHAR(100) NOT NULL,
    UNIQUE (uri),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_qname
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL,
    ns_id BIGINT NOT NULL,
    local_name VARCHAR(200) NOT NULL,
    CONSTRAINT FOREIGN KEY fk_alf_qname_ns (ns_id) REFERENCES alf_namespace (id),
    UNIQUE (ns_id, local_name),
    PRIMARY KEY (id)
) ENGINE=InnoDB;
