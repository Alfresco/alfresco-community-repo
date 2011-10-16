/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.rule.ruletrigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * On propety update trigger
 * 
 * @author Roy Wetherall
 */
public class OnPropertyUpdateRuleTrigger extends RuleTriggerAbstractBase 
                                        implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(OnPropertyUpdateRuleTrigger.class);
    
    /** True trigger parent rules, false otherwier */
    private boolean triggerParentRules = true;
    
    /**
     * Indicates whether the parent rules should be triggered or the rules on the node itself
     * 
     * @param triggerParentRules    true trigger parent rules, false otherwise
     */
    public void setTriggerParentRules(boolean triggerParentRules)
    {
        this.triggerParentRules = triggerParentRules;
    }
    
    /*
     * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
     */
    public void registerRuleTrigger()
    {
        // Bind behaviour
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                this, 
                new JavaBehaviour(this, "onUpdateProperties"));
    }

    private boolean havePropertiesBeenModified(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        List<QName> remainder = new ArrayList<QName>(after.keySet());
        List<QName> modifiedProperties = new ArrayList<QName>();
        for (QName name : before.keySet())
        {
            if (after.containsKey(name) == true)
            {
                Serializable beforeValue = before.get(name);
                Serializable afterValue = after.get(name);
                if (EqualsHelper.nullSafeEquals(beforeValue, afterValue) != true)
                {
                    // The property has been changed
                    modifiedProperties.add(name);
                }
                
                // Remove the property from the remainder list
                remainder.remove(name);
            }
        }
        
        // Add any properties now remaining whose values have been added for the first time
        if (remainder.size() != 0)
        {
            modifiedProperties.addAll(remainder);
        }
        
        // Filter out the protected and content type properties from the list of modified properties
        for (QName propertyName : new ArrayList<QName>(modifiedProperties))
        {
            PropertyDefinition propertyDefinition = this.dictionaryService.getProperty(propertyName);
            if (propertyDefinition != null)
            {
                if (propertyDefinition.isProtected() == true || propertyDefinition.getDataType().getName().equals(DataTypeDefinition.CONTENT) == true)
                {
                    // Remove the protected property from the list
                    modifiedProperties.remove(propertyName);
                }
            }
        }        
        
        return (modifiedProperties.isEmpty() == false);
    }
    
    /**
     * Triggers rules if properties have been updated
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
        // Do not fire if the node has been created in this transaction
        Set<NodeRef> newNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_NEW_NODES);
        boolean wasCreatedInTxn = newNodeRefSet.contains(nodeRef);
        if (logger.isDebugEnabled() && wasCreatedInTxn)
        {
            logger.debug("Receiving property update for node created in transaction: " + nodeRef);
        }
        
        // Only try and trigger the rules if a non protected property has been modified
        if (!wasCreatedInTxn &&
        	before.size() != 0 &&  // ALF-4846: Do not trigger for newly created nodes	
        	havePropertiesBeenModified(nodeRef, before, after) == true)
        {
            // Keep track of name changes explicitly.  This prevents the later association change from
            // triggering 'inbound' rules
            if (!EqualsHelper.nullSafeEquals(before.get(ContentModel.PROP_NAME), after.get(ContentModel.PROP_NAME)))
            {
                // Name has changed
                Set<NodeRef> renamedNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_RENAMED_NODES);
                renamedNodeRefSet.add(nodeRef);
            }
            
            if (triggerParentRules == true)
            {            
                List<ChildAssociationRef> parentsAssocRefs = this.nodeService.getParentAssocs(nodeRef);
                for (ChildAssociationRef parentAssocRef : parentsAssocRefs)
                {
                    triggerRules(parentAssocRef.getParentRef(), nodeRef);
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(
                                "OnPropertyUpdate rule triggered (parent); " +
                        	    "nodeRef=" + parentAssocRef.getParentRef());
                    }
                }
            }
            else
            {
                triggerRules(nodeRef, nodeRef);
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("OnPropertyUpdate rule triggered; nodeRef=" + nodeRef);
                }
            }
        }
    }
}
