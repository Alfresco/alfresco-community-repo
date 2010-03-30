--
-- Title:      Core Repository Tables
-- Database:   PostgreSQL
-- Since:      V3.3 Schema 4000
-- Author:     unknown
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_applied_patch
(
    id VARCHAR(64) NOT NULL,
    description VARCHAR(1024),
    fixes_from_schema INT4,
    fixes_to_schema INT4,
    applied_to_schema INT4,
    target_schema INT4,
    applied_on_date TIMESTAMP,
    applied_to_server VARCHAR(64),
    was_executed BOOL,
    succeeded BOOL,
    report VARCHAR(1024),
    PRIMARY KEY (id)
);

CREATE TABLE alf_namespace
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    uri VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uri ON alf_namespace (uri);
CREATE SEQUENCE alf_namespace_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_qname
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    ns_id INT8 NOT NULL,
    local_name VARCHAR(200) NOT NULL,
    CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id),    
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ns_id ON alf_qname (ns_id, local_name);
CREATE INDEX fk_alf_qname_ns ON alf_qname (ns_id);
CREATE SEQUENCE alf_qname_seq START WITH 1 INCREMENT BY 1;