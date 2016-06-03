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
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.junit.experimental.categories.Category;

/**
 * Parameter definition implementation unit test.
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
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
    	List<RuleTrigger> triggers = new ArrayList<RuleTrigger>(2);
        triggers.add((RuleTrigger)this.applicationContext.getBean("on-content-create-trigger"));
    	triggers.add((RuleTrigger)this.applicationContext.getBean("on-create-node-trigger"));
    	triggers.add((RuleTrigger)this.applicationContext.getBean("on-create-child-association-trigger"));
    	
    	ExtendedRuleType ruleType = new ExtendedRuleType(triggers);
    	assertFalse(ruleType.rulesTriggered);
    	
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTENT).getChildRef();
        
    	// Update some content in order to trigger the rule type
    	ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    	contentWriter.putContent("any old content");        
    	assertTrue(ruleType.rulesTriggered);
    	
        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
    	
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
