--
-- Title:      Remove wcmwf:submitted Aspect
-- Database:   Generic
-- Since:      V2.1 Schema 73
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- This removes the wcmwf:submitted aspect along with the wcmwf:workflowInstanceId property
-- from the AVM nodes

DELETE FROM avm_aspects WHERE EXISTS
(
   SELECT q.id FROM alf_qname q
   JOIN alf_namespace n ON (q.ns_id = n.id)
   WHERE n.uri = 'http://www.alfresco.org/model/wcmworkflow/1.0' and q.local_name = 'submitted' AND q.id = avm_aspects.qname_id
);
DELETE FROM avm_node_properties WHERE EXISTS
(
   SELECT q.id FROM alf_qname q
   JOIN alf_namespace n ON (q.ns_id = n.id)
   WHERE n.uri = 'http://www.alfresco.org/model/wcmworkflow/1.0' and q.local_name = 'workflowInstanceId' AND q.id = avm_node_properties.qname_id
);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V2.1-RemoveWcmSubmittedAspect';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V2.1-RemoveWcmSubmittedAspect', 'Manually executed script upgrade V2.1: Remove wcmwf:submitted aspect',
    0, 72, -1, 73, null, 'UNKOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );