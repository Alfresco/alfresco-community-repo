package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Link to audit event.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class LinkToAuditEvent extends AuditEvent implements OnCreateChildAssociationPolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.ASSOCIATION,
            type = "rma:filePlanComponent"
    )
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // only care about linking child associations
        if (!childAssocRef.isPrimary())
        {
            // TODO
            // add some dummy properties to indicate the details of the link?
            recordsManagementAuditService.auditEvent(childAssocRef.getChildRef(), getName());
        }
    }

}
