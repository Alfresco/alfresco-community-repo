package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour associated with the scheduled aspect
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
        defaultType = "rma:scheduled"
)
public class ScheduledAspect extends    BaseBehaviourBean
                             implements NodeServicePolicies.OnAddAspectPolicy
{
    /** disposition service */
    private DispositionService dispositionService;

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (nodeService.exists(nodeRef) &&
            dispositionService.getAssociatedDispositionSchedule(nodeRef) == null)
        {
           dispositionService.createDispositionSchedule(nodeRef, null);
        }
    }
}
