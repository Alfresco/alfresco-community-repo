package org.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Registers and contains the behaviour specific to the
 * {@link org.alfresco.model.ContentModel#ASPECT_PENDING_DELETE 'pending delete' aspect}.
 * 
 * @author Derek Hulley
 * @since 4.1.1
 */
public class PendingDeleteAspect implements CopyServicePolicies.OnCopyNodePolicy
{
    private PolicyComponent policyComponent;

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @see #getCopyCallback(QName, CopyDetails)
     */
    public void init()
    {
        // disable copy for referencable aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_PENDING_DELETE,
                new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * @return          Returns {@link DoNothingCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }
}
