package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Move to audit event.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class MoveToAuditEvent extends AuditEvent implements OnMoveNodePolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:filePlanComponent"
    )
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        recordsManagementAuditService.auditEvent(newChildAssocRef.getChildRef(), getName());
    }

}
