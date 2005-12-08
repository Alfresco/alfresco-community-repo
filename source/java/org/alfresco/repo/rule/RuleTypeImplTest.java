/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
		public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef)
		{
			this.rulesTriggered = true;
		}
    	
    }
}
