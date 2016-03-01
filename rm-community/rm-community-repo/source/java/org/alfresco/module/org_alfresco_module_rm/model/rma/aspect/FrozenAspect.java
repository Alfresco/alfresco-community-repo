 
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * rma:frozen behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:frozen"
)
public class FrozenAspect extends    BaseBehaviourBean
                          implements NodeServicePolicies.BeforeDeleteNodePolicy,
                                     NodeServicePolicies.OnAddAspectPolicy,
                                     NodeServicePolicies.OnRemoveAspectPolicy
{
    /** file plan service */
    protected FilePlanService filePlanService;

    /** freeze service */
    protected FreezeService freezeService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param freezeService freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    /**
     * Ensure that no frozen node is deleted.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(nodeRef) &&
                    filePlanService.isFilePlanComponent(nodeRef))
                {
                    if (freezeService.isFrozen(nodeRef))
                    {
                        // never allowed to delete a frozen node
                        throw new AccessDeniedException("Frozen nodes can not be deleted.");
                    }

                    // check children
                    checkChildren(nodeService.getChildAssocs(nodeRef));
                }
                return null;
            }
        });
    }

    /**
     * Checks the children for frozen nodes. Throws security error if any are
     * found.
     *
     * @param assocs
     */
    private void checkChildren(List<ChildAssociationRef> assocs)
    {
        for (ChildAssociationRef assoc : assocs)
        {
            // we only care about primary children
            if (assoc.isPrimary())
            {
                NodeRef nodeRef = assoc.getChildRef();
                if (freezeService.isFrozen(nodeRef))
                {
                    // never allowed to delete a node with a frozen child
                    throw new AccessDeniedException("Can not delete node, because it contains a frozen child node.");
                }

                // check children
                checkChildren(nodeService.getChildAssocs(nodeRef));
            }
        }
    }
    
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onAddAspect(final NodeRef record, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(record) &&
                    isRecord(record))
                {
                    // get the owning record folder
                    NodeRef recordFolder = nodeService.getPrimaryParent(record).getParentRef();
                    // check that the aspect has been added
                    if (nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN))
                    {
                        // increment current count
                        int currentCount = (Integer)nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT);
                        currentCount = currentCount + 1;
                        nodeService.setProperty(recordFolder, PROP_HELD_CHILDREN_COUNT, currentCount);
                    }
                }
                return null;
            }
        });        
    }

    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onRemoveAspect(final NodeRef record, QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(record) &&
                    isRecord(record))
                {
                    // get the owning record folder
                    NodeRef recordFolder = nodeService.getPrimaryParent(record).getParentRef();
        
                    // check that the aspect has been added
                    if (nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN))
                    {
                        // decrement current count
                        int currentCount = (Integer)nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT);
                        if (currentCount > 0)
                        {
                            currentCount = currentCount - 1;
                            nodeService.setProperty(recordFolder, PROP_HELD_CHILDREN_COUNT, currentCount);
                        }
                    }                   
                }
                return null;
            }
        }); 
        
    }

}
