package org.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Andy
 * @since 5.1
 */
public class CascadeUpdateAspect implements OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy, OnMoveNodePolicy
{
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private SOLRTrackingComponent solrTrackingComponent;

  
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
 
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        // need to listen to:
        // invokeOnCreateChildAssociation(newParentAssocRef, false);
        // invokeOnDeleteChildAssociation(oldParentAssocRef);
        // invokeOnMoveNode(oldParentAssocRef, newParentAssocRef); 
        // categories affect paths via membership (not paths beneath nodes that are categories) 
        // - only changing category structure requires a cascade not changing a node's on a categories
        
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindAssociationBehaviour(OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onDeleteChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        markCascadeUpdate(oldChildAssocRef.getChildRef());
        markCascadeUpdate(newChildAssocRef.getChildRef());
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        markCascadeUpdate(childAssocRef.getChildRef());
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        if(!isNewNode)
        {
            markCascadeUpdate(childAssocRef.getChildRef());
        }
    }
    
    private void markCascadeUpdate(NodeRef nodeRef)
    {
        Status status = nodeService.getNodeStatus(nodeRef);
        nodeService.setProperty(status.getNodeRef(), ContentModel.PROP_CASCADE_CRC, solrTrackingComponent.getCRC(status.getDbId()));
        nodeService.setProperty(status.getNodeRef(), ContentModel.PROP_CASCADE_TX, status.getDbTxnId());   
    }
}
