package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Workflow Definition type behaviour.
 * 
 * @author JanV
 */
public class WorkflowDefinitionType implements ContentServicePolicies.OnContentUpdatePolicy,
                                            NodeServicePolicies.OnUpdatePropertiesPolicy,
                                            NodeServicePolicies.BeforeDeleteNodePolicy
{
    /** The policy component */
    private PolicyComponent policyComponent;

    /** The workflow deployer / undeployer */
    private WorkflowDeployer workflowDeployer;
    
    /**
     * Set the policy component
     * 
     * @param policyComponent   the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the workflow deployer / undeployer
     *
     * @param workflowDeployer   the workflow deployer / undeployer
     */
    public void setWorkflowDeployer(WorkflowDeployer workflowDeployer)
    {
        this.workflowDeployer = workflowDeployer;
    }
    
    /**
     * The initialise method     
     */
    public void init()
    {
        // Register interest in the onContentUpdate policy for the workflow definition type
        policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME, 
                WorkflowModel.TYPE_WORKFLOW_DEF, 
                new JavaBehaviour(this, "onContentUpdate"));
        
        // Register interest in the onPropertyUpdate policy for the workflow definition type
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                WorkflowModel.TYPE_WORKFLOW_DEF, 
                new JavaBehaviour(this, "onUpdateProperties"));
        
        // Register interest in the node delete policy
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), 
                WorkflowModel.TYPE_WORKFLOW_DEF, 
                new JavaBehaviour(this, "beforeDeleteNode"));
    }
    
    /**
     * On content update behaviour implementation
     * 
     * @param nodeRef   the node reference whose content has been updated
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        workflowDeployer.deploy(nodeRef, true);
    }
    
    /**
     * On update properties behaviour implementation
     * 
     * @param nodeRef   the node reference
     * @param before    the values of the properties before update
     * @param after     the values of the properties after the update
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        Boolean beforeValue = (Boolean)before.get(WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
        Boolean afterValue = (Boolean)after.get(WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
        
        // If the afterValue exists and is different from the beforeValue...
        if (afterValue != null && (afterValue.equals(beforeValue) == false))
        {
            if (afterValue.booleanValue() == true)
            {
                workflowDeployer.deploy(nodeRef, true);
            }
            else
            {
                workflowDeployer.undeploy(nodeRef);
            }
        }
        else if (afterValue == null && beforeValue != null)
        {
            // Undeploy the definition since the value has been cleared
            workflowDeployer.undeploy(nodeRef);
        }

    }
    
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        workflowDeployer.undeploy(nodeRef);
    }
}
