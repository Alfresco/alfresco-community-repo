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
package org.alfresco.repo.rule.ruletrigger;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
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
                ContentServicePolicies.ON_CONTENT_UPDATE, 
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
    		ContentReader contentReader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    		if (contentReader == null || 
    			contentReader.exists() == false || 
    			isZeroLengthOfficeDoc(contentReader) == true)
    		{
				fail = true;
    		}
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
