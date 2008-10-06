--
-- Title:      Audit Path index
-- Database:   Generic
-- Since:      V2.1 Schema 81
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Audit path index


CREATE INDEX idx_alf_adtf_pth ON alf_audit_fact (path);
