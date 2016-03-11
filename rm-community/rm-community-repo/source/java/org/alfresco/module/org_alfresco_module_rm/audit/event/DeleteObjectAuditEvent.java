package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Audits file plan component delete
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class DeleteObjectAuditEvent extends AuditEvent implements BeforeDeleteNodePolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:filePlanComponent"
    )
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        recordsManagementAuditService.auditEvent(nodeRef, getName(), null, null, true, false);
    }
}
