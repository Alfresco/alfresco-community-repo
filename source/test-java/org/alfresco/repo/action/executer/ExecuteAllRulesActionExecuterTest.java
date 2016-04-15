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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.experimental.categories.Category;

/**
 * Execute all rules action execution test
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
public class ExecuteAllRulesActionExecuterTest extends BaseSpringTest
{
    /** The node service */
    private NodeService nodeService;
    
    /** The rule service */
    private RuleService ruleService;
    
    /** The action service */
    private ActionService actionService;
    
    /** The CheckOut/CheckIn service */
    private CheckOutCheckInService checkOutCheckInService;
    
    private RetryingTransactionHelper transactionHelper;
    
    /** The store reference */
    private StoreRef testStoreRef;
    
    /** The root node reference */
    private NodeRef rootNodeRef;
    
    /** The add features action executer */
    private ExecuteAllRulesActionExecuter executer;
    
    /** Id used to identify the test action created */
    private final static String ID = GUID.generate();
    
    /**
     * Called at the beginning of all tests
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.checkOutCheckInService = (CheckOutCheckInService) this.applicationContext.getBean("checkOutCheckInService");
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.ruleService = (RuleService)this.applicationContext.getBean("ruleService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        transactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
        
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Get the executer instance 
        this.executer = (ExecuteAllRulesActionExecuter)this.applicationContext.getBean(ExecuteAllRulesActionExecuter.NAME);
    }
    
    /**
     * ALF-17517: if inbound "Add classifiable Aspect" rule is applied on folder
     * test that no InvalidNodeRefException is thrown during Checkout/Checkin for
     * the document in folder. Such case can happen during revert to older document version.
     */
    public void testRevertVersion()
    {
        // Create a folder
        final NodeRef folder = this.nodeService.createNode(
            this.rootNodeRef,
            ContentModel.ASSOC_CHILDREN,
            QName.createQName("{test}FolderWithInboundRule"),
            ContentModel.TYPE_FOLDER).getChildRef();

        // Create "Add classifiable Aspect" rule on folder
        Rule classifiableRule = new Rule();
        classifiableRule.setRuleType(RuleType.INBOUND);
        Action addFeaturesAction = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        addFeaturesAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
        classifiableRule.setAction(addFeaturesAction);
        this.ruleService.saveRule(folder, classifiableRule);
        
        // Put a document into folder        
        final NodeRef doc = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}TestDoc"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        // Execute rule on document upload
        ActionImpl action = new ActionImpl(null, ID, ExecuteAllRulesActionExecuter.NAME, null);
        this.executer.execute(action, folder);
        setComplete();
        endTransaction();
        assertTrue(this.nodeService.hasAspect(doc, ContentModel.ASPECT_CLASSIFIABLE));
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Checkout/Checkin the node - to store the new version in version history
                NodeRef workingCopyRef = checkOutCheckInService.checkout(doc);
                Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
                props.put(Version.PROP_DESCRIPTION, "");
                props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
                NodeRef original = checkOutCheckInService.checkin(workingCopyRef, props);
                
                // Execute the action
                ActionImpl action = new ActionImpl(null, ID, ExecuteAllRulesActionExecuter.NAME, null);
                action.setParameterValue(ExecuteAllRulesActionExecuter.PARAM_RUN_ALL_RULES_ON_CHILDREN, Boolean.TRUE);
                executer.execute(action, folder);
                
                return null;
            }                 
        });
        
        assertTrue(this.nodeService.hasAspect(doc, ContentModel.ASPECT_CLASSIFIABLE));
    }
    
    /**
     * Test execution
     */
    public void testExecution()
    {      
        // Create a folder and put a couple of documents in it
        final NodeRef folder = this.nodeService.createNode(
            this.rootNodeRef,
            ContentModel.ASSOC_CHILDREN,
            QName.createQName("{test}folderOne"),
            ContentModel.TYPE_FOLDER).getChildRef();
        final NodeRef doc1 = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}docOne"),
                ContentModel.TYPE_CONTENT).getChildRef();
        final NodeRef doc2 = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}docTwo"),
                ContentModel.TYPE_CONTENT).getChildRef();
        final NodeRef folder2 = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}folderTwo"),
                ContentModel.TYPE_FOLDER).getChildRef();
        final NodeRef doc3 = this.nodeService.createNode(
                folder2,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}docThree"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        
        // Add a couple of rules to the folder
        Rule rule1 = new Rule();
        rule1.setRuleType(RuleType.INBOUND);
        Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        action1.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
        rule1.setAction(action1);
        this.ruleService.saveRule(folder, rule1);
        
        Rule rule2 = new Rule();
        rule2.setRuleType(RuleType.INBOUND);
        Action action2 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        action2.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
        rule2.setAction(action2);
        this.ruleService.saveRule(folder, rule2);
        
        Rule rule3 = new Rule();
        rule3.setRuleType(RuleType.INBOUND);
        Action action3 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        action3.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_TITLED);
        rule3.setAction(action3);
        this.ruleService.saveRule(folder2, rule3);
        
        // Check the the docs don't have the aspects yet
        assertFalse(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_VERSIONABLE));
        assertFalse(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_CLASSIFIABLE));
        assertFalse(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_TITLED));
        assertFalse(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_VERSIONABLE));
        assertFalse(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_CLASSIFIABLE));
        assertFalse(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_TITLED));
        assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_VERSIONABLE));
        assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_CLASSIFIABLE));
        assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_TITLED));
        
        assertTrue(this.nodeService.exists(folder));
        
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, ExecuteAllRulesActionExecuter.NAME, null);
        this.executer.execute(action, folder);
        
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(doc1, ContentModel.ASPECT_VERSIONABLE));
                assertTrue(nodeService.hasAspect(doc1, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_TITLED));
                assertTrue(nodeService.hasAspect(doc2, ContentModel.ASPECT_VERSIONABLE));
                assertTrue(nodeService.hasAspect(doc2, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_TITLED));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_VERSIONABLE));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_TITLED));
                
                clearAspects(doc1);
                clearAspects(doc2);
                clearAspects(doc3);
                
                return null;
            }                 
        });   
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertFalse(nodeService.hasAspect(doc1, ContentModel.ASPECT_VERSIONABLE));
                assertFalse(nodeService.hasAspect(doc1, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc1, ContentModel.ASPECT_TITLED));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_VERSIONABLE));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_TITLED));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_VERSIONABLE));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_TITLED));
                                
                // Execute the action
                ActionImpl action = new ActionImpl(null, ID, ExecuteAllRulesActionExecuter.NAME, null);
                action.setParameterValue(ExecuteAllRulesActionExecuter.PARAM_RUN_ALL_RULES_ON_CHILDREN, Boolean.TRUE);
                executer.execute(action, folder);
                
                return null;
            }                 
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(doc1, ContentModel.ASPECT_VERSIONABLE));
                assertTrue(nodeService.hasAspect(doc1, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_TITLED));
                assertTrue(nodeService.hasAspect(doc2, ContentModel.ASPECT_VERSIONABLE));
                assertTrue(nodeService.hasAspect(doc2, ContentModel.ASPECT_CLASSIFIABLE));
                assertFalse(nodeService.hasAspect(doc2, ContentModel.ASPECT_TITLED));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_VERSIONABLE));
                assertFalse(nodeService.hasAspect(doc3, ContentModel.ASPECT_CLASSIFIABLE));
                assertTrue(nodeService.hasAspect(doc3, ContentModel.ASPECT_TITLED));
                
                clearAspects(doc1);
                clearAspects(doc2);
                clearAspects(doc3);
                
                return null;
            }                 
        }); 
    }
    
    private void clearAspects(NodeRef nodeRef)
    {
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_CLASSIFIABLE);
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TITLED);
    }
}
