/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.rule.ruletrigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
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
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("OnPropertyUpdate rule triggered fired; nodeRef=" + nodeRef.toString() + "; triggerParentRules=" + this.triggerParentRules);
        }
        
        Object createdNodeRef = AlfrescoTransactionSupport.getResource(nodeRef.toString());
        
        // Only try and trigger the rules if a non protected propety has been modified
        if (createdNodeRef == null && havePropertiesBeenModified(nodeRef, before, after) == true)
        {
            if (triggerParentRules == true)
            {            
                List<ChildAssociationRef> parentsAssocRefs = this.nodeService.getParentAssocs(nodeRef);
                for (ChildAssociationRef parentAssocRef : parentsAssocRefs)
                {
                    triggerRules(parentAssocRef.getParentRef(), nodeRef);
                }
            }
            else
            {
                triggerRules(nodeRef, nodeRef);
            }
        }
    }



}
