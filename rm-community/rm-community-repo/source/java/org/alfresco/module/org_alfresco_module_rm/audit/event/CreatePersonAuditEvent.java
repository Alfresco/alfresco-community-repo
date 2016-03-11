package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Audits person creation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class CreatePersonAuditEvent extends AuditEvent implements OnCreateNodePolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "cm:person"
    )
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        recordsManagementAuditService.auditEvent(childAssocRef.getChildRef(), getName());
    }
}
