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

import java.util.Random;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule trigger test
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class RuleTriggerTest extends BaseSpringTest
{
    private static final String ON_CREATE_NODE_TRIGGER = "on-create-node-trigger";
    private static final String ON_UPDATE_NODE_TRIGGER = "on-update-node-trigger";
    private static final String ON_MOVE_NODE_TRIGGER = "on-move-node-trigger";
    private static final String ON_CREATE_CHILD_ASSOCIATION_TRIGGER = "on-create-child-association-trigger";
    private static final String ON_DELETE_CHILD_ASSOCIATION_TRIGGER = "on-delete-child-association-trigger";
    private static final String ON_CREATE_ASSOCIATION_TRIGGER = "on-create-association-trigger";
    private static final String ON_DELETE_ASSOCIATION_TRIGGER = "on-delete-association-trigger";
    private static final String ON_PROPERTY_UPDATE_TRIGGER = "on-property-update-trigger";
    private static final String ON_CONTENT_CREATE_TRIGGER = "on-content-create-trigger";

    private NodeService nodeService;
    private ContentService contentService;
    
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;

    @Before
    public void before() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
        
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        
        this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
    }
    
    @After
    public void after()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void testOnCreateNodeTrigger()
    {
        TestRuleType ruleType = createTestRuleType(ON_CREATE_NODE_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER);
        
        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);
    }

    @Test
    public void testOnCreateIgnoredTypesTrigger()
    {
        TestRuleType ruleType = createTestRuleType(ON_CREATE_NODE_TRIGGER);
        assertFalse(ruleType.rulesTriggered);

        //Try and trigger the type
        this.nodeService.createNode(
                this.rootNodeRef, 
                ForumModel.ASSOC_DISCUSSION,
                ForumModel.ASSOC_DISCUSSION, 
                ForumModel.TYPE_POST);
        
        //Check to see if the rule type has been triggered
        assertFalse(ruleType.rulesTriggered);
    }
 
    @Test
    public void testOnUpdateNodeTrigger()
    {
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        TestRuleType ruleType = createTestRuleType(ON_UPDATE_NODE_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, "nameChanged");
        
        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);        
    }
    
//    public void testOnDeleteNodeTrigger()
//    {
//        NodeRef nodeRef = this.nodeService.createNode(
//                this.rootNodeRef,
//                ContentModel.ASSOC_CHILDREN,
//                ContentModel.ASSOC_CHILDREN,
//                ContentModel.TYPE_CONTAINER).getChildRef();
//        
//        TestRuleType ruleType = createTestRuleType(ON_DELETE_NODE_TRIGGER);
//        assertFalse(ruleType.rulesTriggered);
//        
//        // Try and trigger the type
//        this.nodeService.deleteNode(nodeRef);
//        
//        // Check to see if the rule type has been triggered
//        assertTrue(ruleType.rulesTriggered);        
//    }
    
    @Test
    public void testOnCreateChildAssociationTrigger()
    {
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        TestRuleType ruleType = createTestRuleType(ON_CREATE_CHILD_ASSOCIATION_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.addChild(
                nodeRef, 
                nodeRef2,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN);
        
        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);        
    }
    
    @Test
    public void testOnDeleteChildAssociationTrigger()
    {
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        this.nodeService.addChild(
                nodeRef, 
                nodeRef2,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN);
        
        TestRuleType ruleType = createTestRuleType(ON_DELETE_CHILD_ASSOCIATION_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.removeChild(nodeRef, nodeRef2);
        
        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);        
    }
    
    @Test
    public void testOnCreateAssociationTrigger()
    {
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        TestRuleType ruleType = createTestRuleType(ON_CREATE_ASSOCIATION_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.createAssociation(nodeRef, nodeRef2, ContentModel.ASSOC_CHILDREN);
        
        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);
    }
    
    @Test
    public void testOnCreateOriginalAssociationTrigger()
    {
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        TestRuleType ruleType = createTestRuleType(ON_CREATE_ASSOCIATION_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.createAssociation(nodeRef, nodeRef2, ContentModel.ASSOC_ORIGINAL);
        
        // Check to see if the rule type has been triggered
        assertFalse(ruleType.rulesTriggered);
    }
    
    @Test
    public void testOnDeleteAssociationTrigger()
    {
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
        this.nodeService.createAssociation(nodeRef, nodeRef2, ContentModel.ASSOC_CHILDREN);
        
        TestRuleType ruleType = createTestRuleType(ON_DELETE_ASSOCIATION_TRIGGER);
        assertFalse(ruleType.rulesTriggered);
        
        // Try and trigger the type
        this.nodeService.removeAssociation(nodeRef, nodeRef2, ContentModel.ASSOC_CHILDREN);
        
        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);        
    }
    
    @Test
    public void testOnContentCreateTrigger()
    {
        TestRuleType nodeCreate = createTestRuleType(ON_CREATE_NODE_TRIGGER);
        assertFalse(nodeCreate.rulesTriggered);

        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTENT).getChildRef();
        
        assertTrue(nodeCreate.rulesTriggered);

        // Terminate the transaction
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        TestRuleType contentCreate = createTestRuleType(ON_CONTENT_CREATE_TRIGGER);
        assertFalse(contentCreate.rulesTriggered);
        
        // Try and trigger the type
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent("some content");
        
        // Check to see if the rule type has been triggered
        assertTrue(contentCreate.rulesTriggered);
        
        // Try and trigger the type (again)
        contentCreate.rulesTriggered = false;
        assertFalse(contentCreate.rulesTriggered);
        ContentWriter contentWriter2 = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter2.setEncoding("UTF-8");
        contentWriter2.putContent("some content");
        
        // Check to see if the rule type has been triggered
        assertFalse(contentCreate.rulesTriggered);
    }
    
    @Test
    public void testOnContentUpdateTrigger()
    {
        TestRuleType nodeCreate = createTestRuleType(ON_CREATE_NODE_TRIGGER);
        assertFalse(nodeCreate.rulesTriggered);

        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTENT).getChildRef();

        assertTrue(nodeCreate.rulesTriggered);
        
        TestRuleType contentCreate = createTestRuleType(ON_CONTENT_CREATE_TRIGGER);
        TestRuleType contentUpdate = createTestRuleType(ON_PROPERTY_UPDATE_TRIGGER);
        assertFalse(contentCreate.rulesTriggered);
        assertFalse(contentUpdate.rulesTriggered);
        
        // Try and trigger the type
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent("some content");
        
        // Check to see if the rule type has been triggered
        assertTrue(contentCreate.rulesTriggered);
        assertFalse(contentUpdate.rulesTriggered);
        
        // Try and trigger the type (again)
        contentCreate.rulesTriggered = false;
        assertFalse(contentCreate.rulesTriggered);
        ContentWriter contentWriter2 = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter2.setEncoding("UTF-8");
        contentWriter2.putContent("more content some content");
        
        // Check to see if the rule type has been triggered
        assertTrue(contentCreate.rulesTriggered);
        assertFalse(
                "Content update must not fire if the content was created in the same txn.",
                contentUpdate.rulesTriggered);
        
        // Terminate the transaction
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        contentCreate.rulesTriggered = false;
        
        // Try and trigger the type (again)
        ContentWriter contentWriter3 = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter3.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter3.setEncoding("UTF-8");
        contentWriter3.putContent("Yet content some content");
        
        // Check to see if the rule type has been triggered
        assertFalse(
                "Content create should not be fired on an update in a new txn",
                contentCreate.rulesTriggered);
        assertTrue(
                "Content update must not fire if the content was created in the same txn.",
                contentUpdate.rulesTriggered);
    }
    
    @Test
    public void testOnMoveNodeTrigger()
    {
        NodeRef nodeRef1 = this.nodeService.createNode(this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();

        NodeRef nodeRef2 = this.nodeService.createNode(this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();

        this.nodeService.createNode(nodeRef2,
                ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();

        this.nodeService.createNode(nodeRef2,
                ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();

        
        TestRuleType ruleType = createTestRuleType(ON_MOVE_NODE_TRIGGER);
        assertFalse(ruleType.rulesTriggered);

        // Try and trigger the type
        this.nodeService.moveNode(nodeRef2, nodeRef1, ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN);

        // Check to see if the rule type has been triggered
        assertTrue(ruleType.rulesTriggered);
        assertEquals(3, ruleType.triggerCount);
    }
    @Test
    public void testOnPropertyUpdateRuleTrigger()
    {
        NodeRef nodeRef1 = this.nodeService.createNode(this.rootNodeRef,
                    ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN,
                    ContentModel.TYPE_CONTAINER).getChildRef();

        ContentWriter contentWriter = this.contentService.getWriter(nodeRef1, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent("some content");

        Random rand=new Random();
        this.nodeService.setProperty(nodeRef1, ContentModel.PROP_CASCADE_CRC, rand.nextLong());
        // Terminate the transaction
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        TestRuleType contentUpdate = createTestRuleType(ON_PROPERTY_UPDATE_TRIGGER);
        this.nodeService.setProperty(nodeRef1, ContentModel.PROP_CASCADE_CRC, rand.nextLong());

        assertFalse(contentUpdate.rulesTriggered);
        assertEquals("trigger count not matching",0,contentUpdate.triggerCount);

    }

    private TestRuleType createTestRuleType(String ruleTriggerName)
    {
        RuleTrigger ruleTrigger = (RuleTrigger)this.applicationContext.getBean(ruleTriggerName);
        assertNotNull(ruleTrigger);
        TestRuleType ruleType = new TestRuleType();
        ruleTrigger.registerRuleType(ruleType);
        return ruleType;
    }
    
    private class TestRuleType implements RuleType
    {
        public boolean rulesTriggered = false;
        public int triggerCount = 0;

        public String getName()
        {
            return "testRuleType";
        }

        public String getDisplayLabel()
        {
            return "displayLabel";
        }

        public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately)
        {
            // Indicate that the rules have been triggered
            this.rulesTriggered = true;
            triggerCount++;
        }
    }
}
