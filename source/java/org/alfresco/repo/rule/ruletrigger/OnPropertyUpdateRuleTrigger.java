/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.rule.ruletrigger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
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
    
    /** True trigger on new content, false otherwise */
    private boolean onNewContent = false;
    
    /** Should we consider zero byte content to be the same as no content? */
    private boolean ignoreEmptyContent = true;

    /** True trigger parent rules, false otherwise */
    private boolean triggerParentRules = true;
    
    /** Runtime rule service */
    private RuntimeRuleService runtimeRuleService;

   /**
    * If set to true the trigger will fire on new content, otherwise it will fire on content update
    * 
    * @param onNewContent  indicates whether to fire on content create or update
    */
   public void setOnNewContent(boolean onNewContent)
   {
       this.onNewContent = onNewContent;
   }

    /**
     * If set to true, then we consider zero byte content to be equivalent to no content.
     * 
     * @param ignoreEmptyContent boolean
     */
    public void setIgnoreEmptyContent(boolean ignoreEmptyContent)
    {
        this.ignoreEmptyContent = ignoreEmptyContent;
    }

   /**
     * Indicates whether the parent rules should be triggered or the rules on the node itself
     * 
     * @param triggerParentRules    true trigger parent rules, false otherwise
     */
    public void setTriggerParentRules(boolean triggerParentRules)
    {
        this.triggerParentRules = triggerParentRules;
    }
    
    /**
     * Set the rule service
     */
    public void setRuntimeRuleService(RuntimeRuleService runtimeRuleService)
    {
        this.runtimeRuleService = runtimeRuleService;
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

    private boolean havePropertiesBeenModified(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after, boolean newNode, boolean newContentOnly)
    {
        if (newContentOnly && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
        {
            return false;
        }        

        Set<QName> keys = new HashSet<QName>(after.keySet());
        keys.addAll(before.keySet());

        // Compare all properties, ignoring protected properties and giving special treatment to content properties
        boolean nonNullContentProperties = false;
        boolean newContentProperties = false;
        boolean nonNewModifiedContentProperties = false;
        boolean modifiedNonContentProperties = false;
        for (QName name : keys)
        {
            // Skip rule firing on this content property for performance reasons
            if (name.equals(ContentModel.PROP_PREFERENCE_VALUES))
            {
                continue;
            }
            Serializable beforeValue = before.get(name);
            Serializable afterValue = after.get(name);
            PropertyDefinition propertyDefinition = this.dictionaryService.getProperty(name);
            if (propertyDefinition == null)
            {
                if (!EqualsHelper.nullSafeEquals(beforeValue, afterValue))
                {
                    modifiedNonContentProperties = true;
                }
            }
            // Ignore protected properties
            else if (!propertyDefinition.isProtected())
            {
                if (propertyDefinition.getDataType().getName().equals(DataTypeDefinition.CONTENT)
                        && !propertyDefinition.isMultiValued())
                {
                    // Remember whether the property was populated, regardless of the ignore setting
                    if (afterValue != null)
                    {
                        nonNullContentProperties = true;
                    }                    
                    if (this.ignoreEmptyContent)
                    {
                        ContentData beforeContent = toContentData(before.get(name));
                        ContentData afterContent = toContentData(after.get(name));
                        if (!ContentData.hasContent(beforeContent) || beforeContent.getSize() == 0)
                        {
                            beforeValue = null;
                        }
                        if (!ContentData.hasContent(afterContent) || afterContent.getSize() == 0)
                        {
                            afterValue = null;
                        }
                    }
                    if (newNode)
                    {
                        if (afterValue != null)
                        {
                            newContentProperties = true;
                        }
                    }
                    else if (!EqualsHelper.nullSafeEquals(beforeValue, afterValue))
                    {
                        if (beforeValue == null)
                        {
                            newContentProperties = true;                            
                        }
                        else
                        {
                            nonNewModifiedContentProperties = true;                            
                        }
                    }
                }
                else if (!EqualsHelper.nullSafeEquals(beforeValue, afterValue))
                {
                    modifiedNonContentProperties = true;
                }
            }
        }

        if (newContentOnly)
        {
            return (newNode && !nonNullContentProperties ) || newContentProperties;
        }
        else
        {
            return modifiedNonContentProperties || nonNewModifiedContentProperties;
        }
    }
    
    /**
     * ALF-17483: It's possible that even for a single-valued contentdata property, its definition may have been changed
     * and the previous persisted value is multi-valued, so let's be careful about converting to ContentData.
     * 
     * @param object
     *            property value to convert
     * @return a ContentData if one can be extracted
     */
    private static ContentData toContentData(Object object)
    {
        if (object == null)
        {
            return null;
        }
        if (object instanceof ContentData)
        {
            return (ContentData) object;
        }
        if (object instanceof Collection<?> && !((Collection<?>) object).isEmpty())
        {
            return toContentData(((Collection<?>) object).iterator().next());
        }
        return null;
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
        if (wasCreatedInTxn)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Receiving property update for node created in transaction: " + nodeRef);
            }
            

            // A rule has already been fired for this new node, but now that we are aware of content properties, we may
            // want to withhold it until later
            if (this.onNewContent)
            {
                if (havePropertiesBeenModified(nodeRef, before, after, true, true))
                {
                    // Possibly undo a previous cancellation in this transaction
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("New node " + nodeRef.toString()
                                + " confirmed to have no content properties or to have new content so firing inbound rules.");
                    }
                    triggerRules(nodeRef);
                }
                else
                {
                    // Removes any rules that have already been triggered for that node
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Removing the pending rules for the new node " + nodeRef.toString()
                                + " since there are no non-empty content properties.");
                    }
                    runtimeRuleService.removeRulePendingExecution(nodeRef);
                }
            }
        }
        else
        {
            // Only try and trigger the rules if a non protected property has been modified
            if (!wasCreatedInTxn &&
            	before.size() != 0 &&  // ALF-4846: Do not trigger for newly created nodes	
            	havePropertiesBeenModified(nodeRef, before, after, false, this.onNewContent))
            {
                // Keep track of name changes explicitly.  This prevents the later association change from
                // triggering 'inbound' rules
                if (!EqualsHelper.nullSafeEquals(before.get(ContentModel.PROP_NAME), after.get(ContentModel.PROP_NAME)))
                {
                    // Name has changed
                    Set<NodeRef> renamedNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_RENAMED_NODES);
                    renamedNodeRefSet.add(nodeRef);
                }
                
                triggerRules(nodeRef);
            }
        }
    }

    /**
     * @param nodeRef NodeRef
     */
    private void triggerRules(NodeRef nodeRef)
    {
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
