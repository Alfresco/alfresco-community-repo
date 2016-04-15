package org.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Registers and contains the behaviour specific to the
 * {@link org.alfresco.model.ContentModel#ASPECT_TEMPORARY temporary aspect}.
 * 
 * @author gavinc
 */
public class TemporaryAspect implements CopyServicePolicies.OnCopyNodePolicy
{
    // Dependencies
    private PolicyComponent policyComponent;

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Initialise the Temporary Aspect
     * <p>
     * Ensures that the {@link ContentModel#ASPECT_TEMPORARY temporary aspect}
     * copy behaviour is disabled when update copies are performed.
     */
    public void init()
    {
        // disable copy for referencable aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_TEMPORARY,
                new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * The {@link ContentModel#ASPECT_TEMPORARY <b>sys:temporary</b>} aspect is only copied
     * if the copy is clean i.e. not to an existing node.
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        if (copyDetails.getTargetNodeRef() != null)
        {
            return DoNothingCopyBehaviourCallback.getInstance();
        }
        else
        {
            return DefaultCopyBehaviourCallback.getInstance();
        }
    }

    /**
     * Does nothing 
     */
    public void onCopyNode(
            QName classRef,
            NodeRef sourceNodeRef,
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
            PolicyScope copyDetails)
    {
        if (copyToNewNode)
        {
           copyDetails.addAspect(ContentModel.ASPECT_TEMPORARY);
        }
        else
        {
           // don't copy if this is an update operation
        }
    }
}
