--
-- Title:      Upgrade to V3.4 - AVM rename duplicates (if any)
-- Database:   Generic
-- Since:      V3.4 schema 4201
-- Author:     janv
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE table avm_tmp_child_entries AS
SELECT
    ce.parent_id, ce.name, ce.child_id
FROM
    avm_child_entries ce,
    (
       SELECT
          ce2.parent_id, LOWER(ce2.name) as lname, MAX(ce2.child_id) as max_child_id
       FROM
          avm_child_entries ce2
       GROUP BY
          ce2.parent_id, LOWER(ce2.name)
       HAVING
          COUNT(*) > 1
    ) entities
 WHERE
    ce.parent_id = entities.parent_id AND 
    LOWER(ce.name) = entities.lname AND
    ce.child_id != entities.max_child_id;

UPDATE avm_child_entries
    SET name = name || '-renamed.duplicate.mark-' || child_id || '.temp'
WHERE EXISTS
    (SELECT 
         1
     FROM
         avm_tmp_child_entries tmp
     WHERE
         parent_id = tmp.parent_id AND
         name = tmp.name AND
         child_id = tmp.child_id);

--ASSIGN:update_count=value
SELECT 
    COUNT(*) as value
FROM
    avm_tmp_child_entries;
    
    
DROP TABLE avm_tmp_child_entries;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.4-AVM-rename-dupes';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.4-AVM-rename-dupes', 'Manually executed script upgrade V3.4',
     0, 4200, -1, 4201, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed: rows updated = ${update_count}'
   );
