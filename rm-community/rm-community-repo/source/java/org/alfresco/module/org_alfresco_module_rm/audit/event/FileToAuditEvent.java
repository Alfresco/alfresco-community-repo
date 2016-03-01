 
package org.alfresco.module.org_alfresco_module_rm.audit.event;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * File audit event.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class FileToAuditEvent extends AuditEvent implements OnUpdatePropertiesPolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:record"
    )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (before.get(PROP_DATE_FILED) == null && after.get(PROP_DATE_FILED) != null)
        {
            // then we can assume that the record has just been filed
            recordsManagementAuditService.auditEvent(nodeRef, getName());
        }
    }
}
