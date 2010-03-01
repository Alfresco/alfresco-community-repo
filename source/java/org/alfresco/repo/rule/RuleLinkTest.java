/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

/**
 * Parameter definition implementation unit test.
 * 
 * @author Roy Wetherall
 */
public class RuleLinkTest extends BaseSpringTest
{
    protected static final String RULE_TYPE_NAME = RuleType.INBOUND;
    protected static final String ACTION_DEF_NAME = AddFeaturesActionExecuter.NAME;
    protected static final String ACTION_PROP_NAME_1 = AddFeaturesActionExecuter.PARAM_ASPECT_NAME;
    protected static final QName ACTION_PROP_VALUE_1 = ContentModel.ASPECT_LOCKABLE;
    protected static final String CONDITION_DEF_NAME = ComparePropertyValueEvaluator.NAME;
    protected static final String COND_PROP_NAME_1 = ComparePropertyValueEvaluator.PARAM_VALUE;
    protected static final String COND_PROP_VALUE_1 = ".doc";

    
    private NodeService nodeService;
    private RuleService ruleService;
    private ActionService actionService;
    private AuthenticationComponent authenticationComponent;
    private FileFolderService fileFolderService;
    
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef folderOne;
    private NodeRef folderTwo;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {               
        // Get the services
        nodeService = (NodeService)getApplicationContext().getBean("nodeService");
        ruleService = (RuleService)getApplicationContext().getBean("ruleService");
        actionService = (ActionService)getApplicationContext().getBean("actionService");
        authenticationComponent = (AuthenticationComponent)getApplicationContext().getBean("authenticationComponent");
        fileFolderService = (FileFolderService)getApplicationContext().getBean("fileFolderService");

        //authenticationComponent.setSystemUserAsCurrentUser();
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        
        // Create the store and get the root node
        testStoreRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(testStoreRef);

        // Create the node used for tests
        NodeRef folder = nodeService.createNode(
                  rootNodeRef,
                  ContentModel.ASSOC_CHILDREN,
                  QName.createQName("{test}testnode"),
                  ContentModel.TYPE_FOLDER).getChildRef();
        
        folderOne = fileFolderService.create(folder, "folderOne", ContentModel.TYPE_FOLDER).getNodeRef();
        folderTwo = fileFolderService.create(folder, "folderTwo", ContentModel.TYPE_FOLDER).getNodeRef();
    }

    public void testLinkRule()
    {
        // Create a rule
        Rule rule = createTestRule(false, "bobs rule");
        this.ruleService.saveRule(folderOne, rule);
        
        assertTrue(this.ruleService.hasRules(folderOne));
        assertEquals(1, ruleService.getRules(folderOne, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderOne));
        
        assertFalse(this.ruleService.hasRules(folderTwo));
        assertEquals(0, ruleService.getRules(folderTwo, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderTwo));
        
        Action linkAction = actionService.createAction(LinkRules.NAME);
        linkAction.setParameterValue(LinkRules.PARAM_LINK_FROM_NODE, folderOne);        
        actionService.executeAction(linkAction, folderTwo);
        
        assertTrue(this.ruleService.hasRules(folderOne));
        assertEquals(1, ruleService.getRules(folderOne, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderOne));
        
        assertTrue(this.ruleService.hasRules(folderTwo));
        assertEquals(1, ruleService.getRules(folderTwo, false).size());
        boolean value = ruleService.isLinkedToRuleNode(folderTwo);
        assertTrue(value);
        assertEquals(folderOne, ruleService.getLinkedToRuleNode(folderTwo));
        
        List<NodeRef> linkedFrom = ruleService.getLinkedFromRuleNodes(folderTwo);
        assertNotNull(linkedFrom);
        assertTrue(linkedFrom.isEmpty());
        
        linkedFrom = ruleService.getLinkedFromRuleNodes(folderOne);
        assertNotNull(linkedFrom);
        assertEquals(1, linkedFrom.size());
        assertEquals(folderTwo, linkedFrom.get(0));
        
        // Check that you can't modify the rules on a linked rule node        
        try
        {
            rule = createTestRule(false, "bobs rule 2");
            this.ruleService.saveRule(folderTwo, rule);
            fail("Shouldn't be able to add a new rule to a linked rule set");
        }
        catch (RuleServiceException e)
        {
            // Expected
        }
        
        // Add another rule to folder one
        rule = createTestRule(false, "bobs other rule");
        this.ruleService.saveRule(folderOne, rule);
        
        assertTrue(this.ruleService.hasRules(folderOne));
        assertEquals(2, ruleService.getRules(folderOne, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderOne));
        
        assertTrue(this.ruleService.hasRules(folderTwo));
        assertEquals(2, ruleService.getRules(folderTwo, false).size());
        value = ruleService.isLinkedToRuleNode(folderTwo);
        assertTrue(value);
        assertEquals(folderOne, ruleService.getLinkedToRuleNode(folderTwo));
        
        linkedFrom = ruleService.getLinkedFromRuleNodes(folderTwo);
        assertNotNull(linkedFrom);
        assertTrue(linkedFrom.isEmpty());
        
        linkedFrom = ruleService.getLinkedFromRuleNodes(folderOne);
        assertNotNull(linkedFrom);
        assertEquals(1, linkedFrom.size());
        assertEquals(folderTwo, linkedFrom.get(0));
        
        // Unlink
        Action unlinkAction = actionService.createAction(UnlinkRules.NAME);
        actionService.executeAction(unlinkAction, folderTwo);
        
        assertTrue(this.ruleService.hasRules(folderOne));
        assertEquals(2, ruleService.getRules(folderOne, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderOne));
        
        assertFalse(this.ruleService.hasRules(folderTwo));
        assertEquals(0, ruleService.getRules(folderTwo, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderTwo));
        
    }
    
    protected Rule createTestRule(boolean isAppliedToChildren, String title)
    {
        // Rule properties
        Map<String, Serializable> conditionProps = new HashMap<String, Serializable>();
        conditionProps.put(COND_PROP_NAME_1, COND_PROP_VALUE_1);

        Map<String, Serializable> actionProps = new HashMap<String, Serializable>();
        actionProps.put(ACTION_PROP_NAME_1, ACTION_PROP_VALUE_1);
        
        List<String> ruleTypes = new ArrayList<String>(1);
        ruleTypes.add(RULE_TYPE_NAME);
        
        // Create the action
        Action action = this.actionService.createAction(CONDITION_DEF_NAME);
        action.setParameterValues(conditionProps);
        
        ActionCondition actionCondition = this.actionService.createActionCondition(CONDITION_DEF_NAME);
        actionCondition.setParameterValues(conditionProps);
        action.addActionCondition(actionCondition);
        
        // Create the rule
        Rule rule = new Rule();
        rule.setRuleTypes(ruleTypes);
        rule.setTitle(title);
        rule.setDescription("bob");
        rule.applyToChildren(isAppliedToChildren);        
        rule.setAction(action);

        return rule;
    }
        
}
