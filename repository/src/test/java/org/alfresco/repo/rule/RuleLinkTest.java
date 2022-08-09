/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parameter definition implementation unit test.
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
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
    private NodeRef folderThree;

    @Before
    public void before() throws Exception
    {
        // Get the services
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        ruleService = (RuleService) applicationContext.getBean("ruleService");
        actionService = (ActionService) applicationContext.getBean("actionService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        fileFolderService = (FileFolderService) applicationContext.getBean("fileFolderService");

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
        folderThree = fileFolderService.create(folder, "folderThree", ContentModel.TYPE_FOLDER).getNodeRef();
    }

    @Test
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
        unlink(folderTwo);
        
        assertTrue(this.ruleService.hasRules(folderOne));
        assertEquals(2, ruleService.getRules(folderOne, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderOne));
        
        assertFalse(this.ruleService.hasRules(folderTwo));
        assertEquals(0, ruleService.getRules(folderTwo, false).size());
        assertFalse(ruleService.isLinkedToRuleNode(folderTwo));
        
    }
    
    private void link(NodeRef folderFrom, NodeRef folderTo)
    {
        Action linkAction = actionService.createAction(LinkRules.NAME);
        linkAction.setParameterValue(LinkRules.PARAM_LINK_FROM_NODE, folderFrom);        
        actionService.executeAction(linkAction, folderTo);
    }
    
    private void unlink(NodeRef folder)
    {
        Action unlinkAction = actionService.createAction(UnlinkRules.NAME);
        actionService.executeAction(unlinkAction, folder);
    }   
    
    @Test
    public void testRelink()
    {
        // Setup test data
        Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        rule = createTestRule(false, "chewy");
        this.ruleService.saveRule(folderTwo, rule);
        rule = createTestRule(false, "han");
        this.ruleService.saveRule(folderTwo, rule);
        
        List<Rule> rules = ruleService.getRules(folderThree);
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
        
        link(folderOne, folderThree);
        
        rules = ruleService.getRules(folderThree);
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
        assertEquals(1, rules.size());
        
        link(folderTwo, folderThree);
        
        rules = ruleService.getRules(folderThree);
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
        assertEquals(2, rules.size());
        
        try
        {
            rules = ruleService.getRules(folderTwo);
            assertNotNull(rules);
            assertFalse(rules.isEmpty());
            assertEquals(2, rules.size());
            
            link(folderTwo, folderOne);
            fail("Shouldn't be able to link a folder that already has rules that it owns.");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // excepted
        }
        
        unlink(folderThree);
        
        rules = ruleService.getRules(folderThree);
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
        
        link(folderTwo, folderThree);
        
        rules = ruleService.getRules(folderThree);
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
        assertEquals(2, rules.size());
    }
    
    /**
     * ALF-11923
     * @since 4.0.2
     * @author Neil Mc Erlean.
     */
    @Test
    public void testDeleteFolderWithPrimaryRules()
    {
        // Setup test data
        Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        
        link(folderOne, folderTwo);
        link(folderOne, folderThree);
        
        List<Rule> rules1 = ruleService.getRules(folderOne);
        assertNotNull(rules1);
        assertFalse(rules1.isEmpty());
        assertEquals(1, rules1.size());
        
        List<Rule> rules2 = ruleService.getRules(folderTwo);
        assertEquals(rules1, rules2);
        
        List<Rule> rules3 = ruleService.getRules(folderThree);
        assertEquals(rules1, rules3);
        
        // Now delete folder 1.
        nodeService.deleteNode(folderOne);
        rules2 = ruleService.getRules(folderTwo);
        rules3 = ruleService.getRules(folderThree);
        
        assertTrue(rules2.isEmpty());
        assertFalse(nodeService.hasAspect(folderTwo, RuleModel.ASPECT_RULES));
        
        assertTrue(rules3.isEmpty());
        assertFalse(nodeService.hasAspect(folderThree, RuleModel.ASPECT_RULES));
    }
    
    /**
     * ALF-11923
     * @since 4.1.1
     * @author Neil Mc Erlean.
     */
    @Test
    public void testDeleteFolderWithSecondaryRules()
    {
        // Setup test data
        Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        
        link(folderOne, folderTwo);
        link(folderOne, folderThree);
        
        List<Rule> rules1 = ruleService.getRules(folderOne);
        assertNotNull(rules1);
        assertFalse(rules1.isEmpty());
        assertEquals(1, rules1.size());
        
        List<Rule> rules2 = ruleService.getRules(folderTwo);
        assertEquals(rules1, rules2);
        
        List<Rule> rules3 = ruleService.getRules(folderThree);
        assertEquals(rules1, rules3);
        
        // Now delete folder 2.
        nodeService.deleteNode(folderTwo);
        rules1 = ruleService.getRules(folderOne);
        rules3 = ruleService.getRules(folderThree);
        
        assertFalse(rules1.isEmpty());
        assertTrue(nodeService.hasAspect(folderOne, RuleModel.ASPECT_RULES));
        
        assertFalse(rules3.isEmpty());
        assertTrue(nodeService.hasAspect(folderThree, RuleModel.ASPECT_RULES));
        assertEquals(rules1, rules3);
    }

    @Test
    public void testGetRuleSetNode() {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        final NodeRef expectedRuleSetNodeRef = getRuleSetNode(folderOne);

        final NodeRef ruleSetNodeRef = ruleService.getRuleSetNode(folderOne);

        assertNotNull(ruleSetNodeRef);
        assertEquals(expectedRuleSetNodeRef, ruleSetNodeRef);
    }

    @Test
    public void testGetRuleSetNodeForLinkedFolder() {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        link(folderOne, folderTwo);
        final NodeRef expectedRuleSetNodeRef = getRuleSetNode(folderOne);

        final NodeRef ruleSetNodeRef = ruleService.getRuleSetNode(folderTwo);

        assertNotNull(ruleSetNodeRef);
        assertEquals(expectedRuleSetNodeRef, ruleSetNodeRef);
    }

    @Test
    public void testIsRuleSetAssociatedWithFolder()
    {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        final NodeRef ruleSetNodeRef = getRuleSetNode(folderOne);

        // when
        final boolean associated = ruleService.isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderOne);

        assertTrue(associated);
    }

    @Test
    public void testIsRuleSetNotAssociatedWithFolder()
    {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderTwo , rule);
        final NodeRef ruleSetNodeRef = getRuleSetNode(folderTwo);

        // when
        final boolean associated = ruleService.isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderOne);

        assertFalse(associated);
    }

    @Test
    public void testIsRuleAssociatedWithRuleSet()
    {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        final NodeRef ruleSetNodeRef = getRuleSetNode(folderOne);

        List<ChildAssociationRef> folderChildes = nodeService.getChildAssocs(folderOne);
        List<ChildAssociationRef> ruleSetChildes = nodeService.getChildAssocs(ruleSetNodeRef);
        List<ChildAssociationRef> ruleParents = nodeService.getParentAssocs(rule.getNodeRef());

        // when
        final boolean associated = ruleService.isRuleAssociatedWithRuleSet(rule.getNodeRef(), ruleSetNodeRef);

        assertNotNull(folderChildes);
        assertNotNull(ruleSetChildes);
        assertNotNull(ruleParents);
        assertTrue(associated);
    }

    @Test
    public void testIsRuleNotAssociatedWithRuleSet()
    {
        final Rule rule = createTestRule(false, "luke");
        final Rule otherRule = createTestRule(false, "bobs rule");
        this.ruleService.saveRule(folderOne, rule);
        this.ruleService.saveRule(folderTwo, otherRule);
        final NodeRef ruleSetNodeRef = getRuleSetNode(folderOne);

        // when
        final boolean associated = ruleService.isRuleAssociatedWithRuleSet(otherRule.getNodeRef(), ruleSetNodeRef);

        assertFalse(associated);
    }

    @Test
    public void testIsRuleSetShared()
    {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        link(folderOne, folderTwo);
        final NodeRef ruleSetNodeRef = ruleService.getRuleSetNode(folderOne);

        // when
        final boolean shared = ruleService.isRuleSetShared(ruleSetNodeRef);

        assertTrue(shared);
    }

    @Test
    public void testIsRuleSetNotShared()
    {
        final Rule rule = createTestRule(false, "luke");
        this.ruleService.saveRule(folderOne, rule);
        final NodeRef ruleSetNodeRef = ruleService.getRuleSetNode(folderOne);

        // when
        final boolean shared = ruleService.isRuleSetShared(ruleSetNodeRef);

        assertFalse(shared);
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

    private NodeRef getRuleSetNode(final NodeRef folderNodeRef) {
        return nodeService.getChildAssocs(folderNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER).stream()
            .filter(ChildAssociationRef::isPrimary)
            .map(ChildAssociationRef::getChildRef)
            .findFirst()
            .orElse(null);
    }
}
