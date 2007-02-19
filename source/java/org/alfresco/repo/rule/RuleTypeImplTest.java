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
package org.alfresco.repo.rule;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rule.ruletrigger.RuleTrigger;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.BaseSpringTest;

/**
 * Parameter definition implementation unit test.
 * 
 * @author Roy Wetherall
 */
public class RuleTypeImplTest extends BaseSpringTest
{
    private static final String NAME = "name";
    
    private NodeService nodeService;
	private ContentService contentService;
	
	private StoreRef testStoreRef;
	private NodeRef rootNodeRef;
	
	@Override
	protected void onSetUpInTransaction() throws Exception
	{
		this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
		this.contentService = (ContentService)this.applicationContext.getBean("contentService");
		
		this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
	}
    
    public void testConstructor()
    {
        create();
    }
   
    private RuleTypeImpl create()
    {    	 
        RuleTypeImpl temp = new RuleTypeImpl(null);
        temp.setBeanName(NAME);
        assertNotNull(temp);
        return temp;
    }
    
    public void testGetName()
    {
        RuleTypeImpl temp = create();
        assertEquals(NAME, temp.getName());
    }   
    
    // TODO Test the display label, ensuring that the label is retrieved from the resource
    
    // TODO Test setRuleTriggers
    
    // TODO Test triggerRuleType
    
    public void testMockInboundRuleType()
    {
    	NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef, 
    			ContentModel.ASSOC_CHILDREN,
    			ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTENT).getChildRef();
		NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef, 
    			ContentModel.ASSOC_CHILDREN,
    			ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
    	
    	List<RuleTrigger> triggers = new ArrayList<RuleTrigger>(2);
        triggers.add((RuleTrigger)this.applicationContext.getBean("on-content-create-trigger"));
    	triggers.add((RuleTrigger)this.applicationContext.getBean("on-content-update-trigger"));
    	triggers.add((RuleTrigger)this.applicationContext.getBean("on-create-child-association-trigger"));
    	
    	ExtendedRuleType ruleType = new ExtendedRuleType(triggers);
    	assertFalse(ruleType.rulesTriggered);
    	
    	// Update some content in order to trigger the rule type
    	ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    	contentWriter.putContent("any old content");        
    	assertTrue(ruleType.rulesTriggered);
    	
    	// Reset
    	ruleType.rulesTriggered = false;
    	assertFalse(ruleType.rulesTriggered);
    	
    	// Create a child association in order to trigger the rule type
    	this.nodeService.addChild(
    			nodeRef2, 
    			nodeRef, 
    			ContentModel.ASSOC_CHILDREN,
    			ContentModel.ASSOC_CHILDREN);
    	assertTrue(ruleType.rulesTriggered);
    }
    
    private class ExtendedRuleType extends RuleTypeImpl
    {
    	public boolean rulesTriggered = false;
    	
		public ExtendedRuleType(List<RuleTrigger> ruleTriggers)
		{
			super(ruleTriggers);
		}
		
		@Override
		public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately)
		{
			this.rulesTriggered = true;
		}
    	
    }
}
