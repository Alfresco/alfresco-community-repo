/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.email.server;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.DuplicateAttributeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that supports functionality of email aliasable aspect.
 * 
 * @author mrogers
 * @since 2.2
 */
public class AliasableAspect implements NodeServicePolicies.OnAddAspectPolicy,
    NodeServicePolicies.BeforeRemoveAspectPolicy,
    NodeServicePolicies.OnUpdatePropertiesPolicy,
    NodeServicePolicies.BeforeDeleteNodePolicy,
    CopyServicePolicies.OnCopyNodePolicy
{
    private PolicyComponent policyComponent;

    private NodeService nodeService;
    
    private AttributeService attributeService;
    
    private static Log logger = LogFactory.getLog(AliasableAspect.class);

    /**
     * The first "key" into the attribute table - identifies that the attribute is for this class
     */
    public final static String ALIASABLE_ATTRIBUTE_KEY_1 = "AliasableAspect";
    
    /**
     * The second "key" into the attribute table - identifies that the attribute is an alias
     */
    public final static String ALIASABLE_ATTRIBUTE_KEY_2 = "Alias";
    
    private final static String ERROR_MSG_DUPLICATE_ALIAS="email.server.err.duplicate_alias";
    /**
     * @param nodeService  Alfresco Node Service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param policyComponent  Alfresco Policy Component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Spring initilaise method used to register the policy behaviours
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "attributeService", attributeService);
        
        // Register the policy behaviours
        policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, 
           EmailServerModel.ASPECT_ALIASABLE, 
            new JavaBehaviour(this, "onAddAspect", NotificationFrequency.FIRST_EVENT));
 
        policyComponent.bindClassBehaviour(BeforeRemoveAspectPolicy.QNAME,
           EmailServerModel.ASPECT_ALIASABLE, 
           new JavaBehaviour(this, "beforeRemoveAspect", NotificationFrequency.FIRST_EVENT));

        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, 
            EmailServerModel.ASPECT_ALIASABLE, 
            new JavaBehaviour(this, "onUpdateProperties"));
        
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, 
                EmailServerModel.ASPECT_ALIASABLE, 
                new JavaBehaviour(this, "beforeDeleteNode"));
        
        policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, 
            EmailServerModel.ASPECT_ALIASABLE, 
            new JavaBehaviour(this, "getCopyCallback"));
    }
       
    
    /**
     * method to normalise an email alias.   
     * 
     * Currently this involves trimmimg and lower casing, but it may change in future
     * 
     * @param value
     * @return the normalised value.
     */
    public static String normaliseAlias(String value)
    {
        if(value != null)
        {
            return value.toLowerCase();
        }
        return value;
    }

    /**
     * Set the email alias for the specified node. 
     * 
     * If the rule is broken, AlfrescoRuntimeException will be thrown.
     * 
     * @param nodeRef Reference to target node
     * @param alias Alias that we want to set to the target node
     * @exception AlfrescoRuntimeException if the <b>alias</b> property is duplicated by another node.
     */
    public void addAlias(NodeRef nodeRef, String alias)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("add email alias nodeRef:" + nodeRef + ", alias:" + alias);
        }
        // first try to see if the new alias is in use elsewhere?   
        try
        {
            attributeService.createAttribute(nodeRef, ALIASABLE_ATTRIBUTE_KEY_1, ALIASABLE_ATTRIBUTE_KEY_2, normaliseAlias(alias));
        }
        catch (DuplicateAttributeException de)
        {
            throw AlfrescoRuntimeException.create(ERROR_MSG_DUPLICATE_ALIAS, normaliseAlias(alias));
        }
        
    }
    
    /**
     * remove the specified alias
     * @param alias to remove
     */
    public void removeAlias(String alias)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("remove email alias alias:" + alias);
        }
        attributeService.removeAttribute(ALIASABLE_ATTRIBUTE_KEY_1, ALIASABLE_ATTRIBUTE_KEY_2, normaliseAlias(alias));
    }    
    
    /**
     * Get a node ref by its email alias
     * @return the node ref, or null if there is no node for that alias
     */
    public NodeRef getByAlias(String alias)
    {
        Serializable value = attributeService.getAttribute(ALIASABLE_ATTRIBUTE_KEY_1, ALIASABLE_ATTRIBUTE_KEY_2, normaliseAlias(alias));
        
        if(value instanceof NodeRef)
        {
            return (NodeRef)value;
        }
        
        return null;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     * @exception AlfrescoRuntimeException Throws if the <b>alias</b> property is duplicated.
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        Object alias = nodeService.getProperty(nodeRef, EmailServerModel.PROP_ALIAS);
        if (alias != null)
        {
            addAlias(nodeRef, alias.toString());
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     * @exception AlfrescoRuntimeException Throws if the <b>alias</b> property is duplicated.
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        String oldAlias = (String)before.get(EmailServerModel.PROP_ALIAS);
        String newAlias = (String)after.get(EmailServerModel.PROP_ALIAS);
        
        if(oldAlias != null && newAlias != null && (normaliseAlias(oldAlias)).equals(normaliseAlias(newAlias)))
        {
            // alias has not changed
            return;
        }
        
        if (newAlias != null)
        {
            addAlias(nodeRef, newAlias);
        }
        
        if(oldAlias != null)
        {
            removeAlias(oldAlias);
        }

    }

    @Override
    public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        String alias = (String)nodeService.getProperty(nodeRef, EmailServerModel.PROP_ALIAS);
        if(alias != null)
        {
            removeAlias(alias);
        }
    }
    
    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        String alias = (String)nodeService.getProperty(nodeRef, EmailServerModel.PROP_ALIAS);
        if(alias != null)
        {
            removeAlias(alias);
        }
    }

    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef,
            CopyDetails copyDetails)
    {
        return AliasableAspectCopyBehaviourCallback.INSTANCE;
    }

    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public AttributeService getAttributeService()
    {
        return attributeService;
    }
}
