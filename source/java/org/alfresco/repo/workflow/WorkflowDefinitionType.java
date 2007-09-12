/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Workflow Definition type behaviour.
 * 
 * @author JanV
 */
public class WorkflowDefinitionType implements ContentServicePolicies.OnContentUpdatePolicy,
                                            NodeServicePolicies.OnUpdatePropertiesPolicy,
                                            NodeServicePolicies.BeforeDeleteNodePolicy
{
    // logger
    private static Log logger = LogFactory.getLog(WorkflowDefinitionType.class);
    
    /** The node service */
    private NodeService nodeService;
    
    /** The policy component */
    private PolicyComponent policyComponent;
    
    /** The workflow service */
    private WorkflowService workflowService;


    
    /**
     * Set the node service
     * 
     * @param nodeService       the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
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
     * Set the workflow service
     *
     * @param workflowService   the workflow service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    
    /**
     * The initialise method     
     */
    public void init()
    {
        // Register interest in the onContentUpdate policy for the workflow definition type
        policyComponent.bindClassBehaviour(
                ContentServicePolicies.ON_CONTENT_UPDATE, 
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
        deploy(nodeRef);
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
        
        if (afterValue != null && 
            (beforeValue == null || (beforeValue != null && afterValue != null && beforeValue.equals(afterValue) == false)))
        {
            if (afterValue.booleanValue() == true)
            {
                deploy(nodeRef);
            }
            else
            {
                undeploy(nodeRef);
            }
        }
        else if (afterValue == null && beforeValue != null)
        {
            // Undeploy the definition since the value has been cleared
            undeploy(nodeRef);
        }

    }
    
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // Ignore if the node is a working copy 
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            undeploy(nodeRef);
        }
    }
    
    private void deploy(NodeRef nodeRef)
    {
        // deploy / re-deploy
        WorkflowDeployment deployment = workflowService.deployDefinition(nodeRef);
        
        if (deployment != null)
        {
            WorkflowDefinition def = deployment.definition;
            
            // Update the meta data for the model
            Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
            
            props.put(WorkflowModel.PROP_WORKFLOW_DEF_NAME, def.getName());
            
            // TODO - ability to return and handle deployment problems / warnings
            if (deployment.problems.length > 0)
            {
                for (String problem : deployment.problems)
                {
                    logger.warn(problem);
                }
            }

            nodeService.setProperties(nodeRef, props);
        }
    }
    
    private void undeploy(NodeRef nodeRef)
    {
        String defName = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_NAME);
        if (defName != null)
        {
            // Undeploy the workflow definition - all versions in JBPM
            List<WorkflowDefinition> defs = workflowService.getAllDefinitionsByName(defName);
            for (WorkflowDefinition def: defs)
            {
                logger.info("Undeploying workflow '" + defName + "' ...");
                workflowService.undeployDefinition(def.getId());
                logger.info("... undeployed '" + def.getId() + "' v" + def.getVersion());
            }
        }
    }
}
