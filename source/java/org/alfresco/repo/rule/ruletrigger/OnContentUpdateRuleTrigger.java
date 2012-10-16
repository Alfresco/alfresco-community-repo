/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class OnContentUpdateRuleTrigger extends RuleTriggerAbstractBase 
                                        implements ContentServicePolicies.OnContentPropertyUpdatePolicy
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(OnContentUpdateRuleTrigger.class);
    
    /** True trigger on new content, false otherwise */
    private boolean onNewContent = false;
    
    /** True trigger parent rules, false otherwier */
    private boolean triggerParentRules = true;
    
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
                ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, 
                this, 
                new JavaBehaviour(this, "onContentPropertyUpdate"));
    }

    
    
    /**
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentPropertyUpdatePolicy#onContentPropertyUpdate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.ContentData, org.alfresco.service.cmr.repository.ContentData)
     */
    @Override
    public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue,
            ContentData afterValue)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
    	
    	// Check the new content and make sure that we do indeed want to trigger the rule
        if (propertyQName.equals(ContentModel.PROP_PREFERENCE_VALUES))
        {
            return;
        }
    	    
        // Check for new content
        boolean newContent = beforeValue == null && afterValue != null;

    	// Check the new content and make sure that we do indeed want to trigger the rule
        if (newContent)
        {
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
            {
                return;
            }

            // Note: Don't use the ContentService.getReader() because we don't need access to the content
            if (!ContentData.hasContent(afterValue))
            {
                return;
            }
    	}
        // An update, but double check for content created in this transaction
        else
        {
            Set<NodeRef> newNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_NEW_NODES);
            if (newNodeRefSet.contains(nodeRef))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Receiving content property update for node created in transaction: " + nodeRef);
                }
                return;
            }
        }
        
        // Trigger the rules in the appropriate way
        if (newContent == this.onNewContent)
        {
            if (triggerParentRules == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("OnContentUpdate rule triggered fired for content; nodeId=" + nodeRef.getId() + "; newContent=" + newContent);
                }
                
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
