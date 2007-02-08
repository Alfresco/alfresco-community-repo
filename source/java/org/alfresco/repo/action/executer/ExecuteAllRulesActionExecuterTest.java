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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Execute all rules action execution test
 * 
 * @author Roy Wetherall
 */
public class ExecuteAllRulesActionExecuterTest extends BaseSpringTest
{
    /** The node service */
    private NodeService nodeService;
    
    /** The rule service */
    private RuleService ruleService;
    
    /** The action service */
    private ActionService actionService;
    
    /** The store reference */
    private StoreRef testStoreRef;
    
    /** The root node reference */
    private NodeRef rootNodeRef;
    
    /** The add features action executer */
    private ExecuteAllRulesActionExecuter executer;
    
    /** Id used to identify the test action created */
    private final static String ID = GUID.generate();
    
    /**
     * Called at the begining of all tests
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.ruleService = (RuleService)this.applicationContext.getBean("ruleService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        
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
     * Test execution
     */
    public void testExecution()
    {      
        // Create a folder and put a couple of documents in it
        NodeRef folder = this.nodeService.createNode(
            this.rootNodeRef,
            ContentModel.ASSOC_CHILDREN,
            QName.createQName("{test}folderOne"),
            ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef doc1 = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}docOne"),
                ContentModel.TYPE_CONTENT).getChildRef();
        NodeRef doc2 = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}docTwo"),
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
        
        // Check the the docs don't have the aspects yet
        assertFalse(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_VERSIONABLE));
        assertFalse(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_CLASSIFIABLE));
        assertFalse(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_VERSIONABLE));
        assertFalse(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_CLASSIFIABLE));
        
        assertTrue(this.nodeService.exists(folder));
        
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, ExecuteAllRulesActionExecuter.NAME, null);
        this.executer.execute(action, folder);
        
        assertTrue(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_VERSIONABLE));
        assertTrue(this.nodeService.hasAspect(doc1, ContentModel.ASPECT_CLASSIFIABLE));
        assertTrue(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_VERSIONABLE));
        assertTrue(this.nodeService.hasAspect(doc2, ContentModel.ASPECT_CLASSIFIABLE));
    }
}
