--
-- Title:      Removing Link Validation related metadata
-- Database:   Generic
-- Since:      V3.4
-- Author:     Dmitry Velichkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- ALF-5185: WCM: upgrade / re-index can fail on 3.4 if link validation (now removed) was previously being used

--
-- Removing Link Validation Reports
--
DELETE
FROM
    avm_store_properties
WHERE
    qname_id in (
        SELECT
            id
        FROM
            alf_qname
        WHERE
            local_name = '.link.validation.report'
    );

--
-- Removing Link Validation QName
--
DELETE
FROM
    alf_qname
WHERE
    local_name = '.link.validation.report';

--
-- Record script finish
--
DELETE
FROM
    alf_applied_patch
WHERE
    id = 'patch.db-V3.4-RemovingLinkValidationMetadata';

INSERT INTO
    alf_applied_patch
    (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
VALUES (
    'patch.db-V3.4-RemovingLinkValidationMetadata', 'Link Validation reports metadata removed V3.4',
    0, 4113, -1, 4114, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
);
