package org.alfresco.repo.node;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;

/**
 * Undeletable aspect behaviour bean.
 * 
 * Deletions of nodes with the {@link ContentModel#ASPECT_UNDELETABLE} are not allowed by default.
 * This class registers the behaviour that prevents the deletion.
 * <p/>
 * This aspect/behaviour combination allows for detailed application control of when node deletion is allowed
 * or disallowed for particular nodes. It is not related to the normal permissions controls, which of course apply.
 * <p/>
 * An example of its usage is in the {@link SiteService}, where {@link org.alfresco.repo.site.SiteModel#TYPE_SITE} nodes are given the
 * {@link ContentModel#ASPECT_UNDELETABLE} as a mandatory aspect. Therefore any attempt to delete such a node will
 * result in an exception. However, this behaviour is disabled within the {@link SiteService} in order to allow
 * site node deletion from within that service but from no other code.
 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 */
public class UndeletableAspect implements NodeServicePolicies.BeforeDeleteNodePolicy
{
   private PolicyComponent policyComponent;
   private NodeService nodeService;
   
   /**
    * Set the policy component
    * 
    * @param policyComponent   policy component
    */
   public void setPolicyComponent(PolicyComponent policyComponent)
   {
       this.policyComponent = policyComponent;
   }
   
   /**
    * Set the node service
    * 
    * @param nodeService   node service
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }
   
   /**
    * Initialise method
    */
   public void init()
   {
       this.policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME,
               ContentModel.ASPECT_UNDELETABLE,
               new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));
   }

   /**
    * Ensures that undeletable nodes cannot be deleted by default.
    */
    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        QName nodeType = nodeService.getType(nodeRef);
        throw new AlfrescoRuntimeException(nodeType.toPrefixString() + " deletion is not allowed. Attempted to delete " + nodeRef);
    }
}
