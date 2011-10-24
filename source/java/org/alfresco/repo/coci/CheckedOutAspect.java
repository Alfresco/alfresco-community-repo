package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Policies relating to the checkedOut aspect.
 * 
 * @since 4.0
 *
 */
public class CheckedOutAspect
{
    /**
     * Policy component
     */
    protected PolicyComponent policyComponent;

    /**
     * Sets the policy component
     * 
     * @param policyComponent  the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
    }
    
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_CHECKED_OUT,
                new JavaBehaviour(this, "getCallbackForCheckedOutAspect"));

        this.policyComponent.bindClassBehaviour(VersionServicePolicies.OnCreateVersionPolicy.QNAME,
                ContentModel.ASPECT_CHECKED_OUT,
                new JavaBehaviour(this, "onCreateVersion"));
    }

    /**
     * Callback behaviour retrieval for the 'onCreateVersion' aspect.
     * 
     * @return              Returns {@link DoNothingCopyBehaviourCallback} always
     */
    public void onCreateVersion(QName classRef, NodeRef versionableNode, Map<String, Serializable> versionProperties, PolicyScope nodeDetails)
    {
        nodeDetails.getAspects().remove(ContentModel.ASPECT_CHECKED_OUT);
    }
    
    /**
     * Callback behaviour retrieval for the 'checkedOut' aspect.
     * 
     * @return              Returns {@link DoNothingCopyBehaviourCallback} always
     */
    public CopyBehaviourCallback getCallbackForCheckedOutAspect(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }
}
