package org.alfresco.module.org_alfresco_module_rm.audit.event;

import java.util.Map;

import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Copy audit event.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class CopyToAuditEvent extends AuditEvent implements OnCopyCompletePolicy
{
    /**
     * Audit copy of file plan components
     *
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy#onCopyComplete(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean, java.util.Map)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:filePlanComponent"
    )
    public void onCopyComplete(QName classRef,
                               NodeRef sourceNodeRef,
                               NodeRef targetNodeRef,
                               boolean copyToNewNode,
                               Map<NodeRef, NodeRef> copyMap)
    {
        if (copyToNewNode)
        {
            recordsManagementAuditService.auditEvent(targetNodeRef, getName());
        }
    }
}
