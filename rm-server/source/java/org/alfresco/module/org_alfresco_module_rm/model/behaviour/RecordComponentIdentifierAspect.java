/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.model.behaviour;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * Record component identifier aspect behaviour
 * 
 * @author Roy Wetherall
 */
public class RecordComponentIdentifierAspect
        implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                   NodeServicePolicies.BeforeDeleteNodePolicy,
                   RecordsManagementModel
{
    private static final String CONTEXT_VALUE = "rma:identifier";
    
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private AttributeService attributeService;
    private FilePlanService filePlanService;

    /**
     * @param policyComponent the policyComponent to set
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the component to manage the unique properties
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }
    
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "attributeService", attributeService);
        
        policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ASPECT_RECORD_COMPONENT_ID, 
                new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME, 
                ASPECT_RECORD_COMPONENT_ID, 
                new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.EVERY_EVENT));
    }

    /**
     * Ensures that the {@link RecordsManagementModel#PROP_IDENTIFIER rma:identifier} property remains
     * unique within the context of the parent node.
     */
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                // Check whether the identifier property has changed
                String beforeId = (String)before.get(PROP_IDENTIFIER);
                String afterId = (String)after.get(PROP_IDENTIFIER);
                updateUniqueness(nodeRef, beforeId, afterId);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName()); 
    }

    /**
     * Cleans up the {@link RecordsManagementModel#PROP_IDENTIFIER rma:identifier} property unique triplet.
     */
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                String beforeId = (String) nodeService.getProperty(nodeRef, PROP_IDENTIFIER);
                updateUniqueness(nodeRef, beforeId, null);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName()); 
    }
    
    /**
     * Updates the uniqueness check using the values provided.  If the after value is <tt>null</tt>
     * then this is considered to be a removal.
     */
    private void updateUniqueness(NodeRef nodeRef, String beforeId, String afterId)
    {
        NodeRef contextNodeRef = filePlanService.getFilePlan(nodeRef);
        
        if (beforeId == null)
        {
            if (afterId != null)
            {
                // Just create it
                attributeService.createAttribute(null, CONTEXT_VALUE, contextNodeRef, afterId);
            }
        }
        else if (afterId == null)
        {
            if (beforeId != null)
            {
                // The before value was not null, so remove it
                attributeService.removeAttribute(CONTEXT_VALUE, contextNodeRef, beforeId);
            }
            // Do a blanket removal in case this is a contextual nodes
            attributeService.removeAttributes(CONTEXT_VALUE, nodeRef);
        }
        else
        {
            // This is a full update
            attributeService.updateOrCreateAttribute(
                    CONTEXT_VALUE, contextNodeRef, beforeId,
                    CONTEXT_VALUE, contextNodeRef, afterId);
        }
    }
}
