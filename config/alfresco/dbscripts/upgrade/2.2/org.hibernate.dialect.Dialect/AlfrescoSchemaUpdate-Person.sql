--
-- Title:      Move user name to be part of the association QNAME
-- Database:   Generic
-- Since:      V2.2 Schema 91
-- Author:     Andy Hind
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Path was previously unused and unindex - new we use it the index is required.

--FOREACH alf_child_assoc.id system.upgrade.alf_child_assoc.batchsize
UPDATE
      alf_child_assoc
   SET 
      qname_ns_id =
      (
         SELECT
            id
         FROM
            alf_namespace n
         WHERE
            n.uri = 'http://www.alfresco.org/model/content/1.0'
      ),
      qname_localname =
      (
         SELECT
            LOWER(p.string_value)
         FROM
            alf_node_properties p
            JOIN alf_qname q on p.qname_id = q.id
            JOIN alf_namespace n on q.ns_id = n.id
         WHERE
            p.node_id = alf_child_assoc.child_node_id AND
            q.local_name ='userName' AND
            n.uri = 'http://www.alfresco.org/model/content/1.0'
      )
   WHERE exists
   (
      SELECT
            0
         FROM alf_node_properties pp
         JOIN alf_qname qq on pp.qname_id = qq.id
         JOIN alf_namespace nn on qq.ns_id = nn.id
      WHERE
         pp.node_id = alf_child_assoc.child_node_id AND
         qq.local_name ='userName' AND
         nn.uri = 'http://www.alfresco.org/model/content/1.0'
    )
    AND alf_child_assoc.id >= ${LOWERBOUND} AND alf_child_assoc.id <= ${UPPERBOUND}
;

-- Validation query
-- select count(*) from alf_child_assoc c
-- JOIN alf_node_properties pp ON c.child_node_id = pp.node_id AND c.qname_localname = pp.string_value
-- JOIN alf_qname qq on pp.qname_id = qq.id 
-- JOIN alf_namespace nn on qq.ns_id = nn.id AND c.qname_ns_id = nn.id
-- WHERE qq.local_name ='userName' AND nn.uri = 'http://www.alfresco.org/model/content/1.0'

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.2-Person-3';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.2-Person-3', 'Manually executed script upgrade V2.2: Person user name also in the association qname',
    0, 3002, -1, 3003, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );
