 
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
 * Audits file plan component property updates
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class UpdateObjectAuditEvent extends AuditEvent implements OnUpdatePropertiesPolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:filePlanComponent"
    )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        recordsManagementAuditService.auditEvent(nodeRef, getName(), before, after, false, true);
    }

}
