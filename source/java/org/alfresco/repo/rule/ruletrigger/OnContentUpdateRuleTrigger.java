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

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class OnContentUpdateRuleTrigger extends RuleTriggerAbstractBase 
                                        implements ContentServicePolicies.OnContentUpdatePolicy
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
                ContentServicePolicies.OnContentUpdatePolicy.QNAME, 
                this, 
                new JavaBehaviour(this, "onContentUpdate"));
    }

    /**
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
    	
    	// Check the new content and make sure that we do indeed want to trigger the rule
    	boolean fail = false;
    	if (newContent == true)
    	{
    	    Boolean value = (Boolean)nodeService.getProperty(nodeRef, QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "editInline"));
            if (value != null)
            {
                boolean editInline = value.booleanValue();
                if (editInline == true)
                {
                    fail = true;
                }
            }
    	    
            if (fail == false)
            {
        		ContentReader contentReader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        		if (contentReader == null || 
        			contentReader.exists() == false || 
        			isZeroLengthOfficeDoc(contentReader) == true)
        		{
    				fail = true;
        		}
            }
    	}
        
        // Double check for content created in this transaction
        if (fail == false && !newContent)
        {
            Set<NodeRef> newNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_NEW_NODES);
            boolean wasCreatedInTxn = newNodeRefSet.contains(nodeRef);
            if (logger.isDebugEnabled() && wasCreatedInTxn)
            {
                logger.debug("Receiving content property update for node created in transaction: " + nodeRef);
            }
            fail = wasCreatedInTxn;
        }
    	
    	// Trigger the rules in the appropriate way
        if (fail == false && newContent == this.onNewContent)
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
    
    /**
     * Indicates whether we are dealing with a zero length office document or not
     * 
     * @param contentReader		the content reader
     * @return boolean			true if zero length office document, false otherwise					
     */
    private boolean isZeroLengthOfficeDoc(ContentReader contentReader)
    {
    	boolean result = false;
    	if (contentReader.getSize() == 0 &&
    		(MimetypeMap.MIMETYPE_WORD.equals(contentReader.getMimetype()) == true ||
    		 MimetypeMap.MIMETYPE_EXCEL.equals(contentReader.getMimetype()) == true ||
    		 MimetypeMap.MIMETYPE_PPT.equals(contentReader.getMimetype()) == true))
    	{
    		result = true;
    	}
    	return result;
    }

}
