 
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * rma:dispositionLifecycle behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:dispositionLifecycle"
)
public class DispositionLifecycleAspect extends BaseBehaviourBean
                                        implements NodeServicePolicies.OnAddAspectPolicy
{
    /** disposition service */
    protected DispositionService dispositionService;

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Copy callback for disposition lifecycle
     */
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            policy = "alf:getCopyCallback"
    )
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new DoNothingCopyBehaviourCallback();
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.EVERY_EVENT
    )
    public void onAddAspect(final NodeRef nodeRef, final QName aspect)
    {
        if (nodeService.exists(nodeRef))
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    dispositionService.refreshDispositionAction(nodeRef);
                    return null;
                }
            });
        }
    }
}
