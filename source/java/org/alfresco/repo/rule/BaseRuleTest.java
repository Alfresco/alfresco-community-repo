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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

/**
 * Base class for rule service test.
 * <p>
 * This file contains a number of helpers to reduce the duplication in tests.
 * 
 * @author Roy Wetherall
 */
public class BaseRuleTest extends BaseSpringTest
{
    /**
     * Data used in the tests
     */
    protected static final String RULE_TYPE_NAME = RuleType.INBOUND;

    /**
     * Action used in tests
     */
    protected static final String ACTION_DEF_NAME = AddFeaturesActionExecuter.NAME;
    protected static final String ACTION_PROP_NAME_1 = AddFeaturesActionExecuter.PARAM_ASPECT_NAME;
    protected static final QName ACTION_PROP_VALUE_1 = ContentModel.ASPECT_LOCKABLE;

    /**
     * ActionCondition used in tests
     */
    protected static final String CONDITION_DEF_NAME = ComparePropertyValueEvaluator.NAME;
    protected static final String COND_PROP_NAME_1 = ComparePropertyValueEvaluator.PARAM_VALUE;
    protected static final String COND_PROP_VALUE_1 = ".doc";

    /**
     * Rule values used in tests
     */
    protected static final String TITLE = "title";
    protected static final String DESCRIPTION = "description";

    /**
     * Services
     */
    protected NodeService nodeService;
    protected ContentService contentService;
    protected RuleService ruleService;
	protected ConfigurableService configService;
    protected AuthenticationComponent authenticationComponent;

    /**
     * Rule type used in tests
     */
    protected RuleType ruleType;

    /**
     * Store and node references
     */
    protected StoreRef testStoreRef;
    protected NodeRef rootNodeRef;
    protected NodeRef nodeRef;
    protected NodeRef configFolder;
    protected ActionService actionService;
    protected TransactionService transactionService;

    /**
     * onSetUpInTransaction implementation
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {               
        // Get the services
        this.nodeService = (NodeService) this.applicationContext
                .getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext
                .getBean("contentService");
        this.ruleService = (RuleService) this.applicationContext
                .getBean("ruleService");
        this.configService = (ConfigurableService)this.applicationContext
        		.getBean("configurableService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");

        authenticationComponent.setSystemUserAsCurrentUser();
        
        // Get the rule type
        this.ruleType = this.ruleService.getRuleType(RULE_TYPE_NAME);

        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTAINER).getChildRef();
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }

    protected void addRulesAspect()
    {
    	// Make the node actionable
    	this.configService.makeConfigurable(this.nodeRef);
    	this.nodeService.addAspect(this.nodeRef, RuleModel.ASPECT_RULES, null); 
    }

    protected Rule createTestRule()
    {
        return createTestRule(false);
    }
    
    protected Rule createTestRule(boolean isAppliedToChildren)
    {
        // Rule properties
        Map<String, Serializable> conditionProps = new HashMap<String, Serializable>();
        conditionProps.put(COND_PROP_NAME_1, COND_PROP_VALUE_1);

        Map<String, Serializable> actionProps = new HashMap<String, Serializable>();
        actionProps.put(ACTION_PROP_NAME_1, ACTION_PROP_VALUE_1);
        
        List<String> ruleTypes = new ArrayList<String>(1);
        ruleTypes.add(this.ruleType.getName());
        
        // Create the action
        Action action = this.actionService.createAction(CONDITION_DEF_NAME);
        action.setParameterValues(conditionProps);
        
        ActionCondition actionCondition = this.actionService.createActionCondition(CONDITION_DEF_NAME);
        actionCondition.setParameterValues(conditionProps);
        action.addActionCondition(actionCondition);
        
        // Create the rule
        Rule rule = new Rule();
        rule.setRuleTypes(ruleTypes);
        rule.setTitle(TITLE);
        rule.setDescription(DESCRIPTION);
        rule.applyToChildren(isAppliedToChildren);        
        rule.setAction(action);

        return rule;
    }

    protected void checkRule(Rule rule)
    {
        // Check the basic details of the rule
        assertEquals(this.ruleType.getName(), rule.getRuleTypes().get(0));
        assertEquals(TITLE, rule.getTitle());
        assertEquals(DESCRIPTION, rule.getDescription());

        Action ruleAction = rule.getAction();
        assertNotNull(ruleAction);
        
        // Check conditions
        List<ActionCondition> ruleConditions = ruleAction.getActionConditions();
        assertNotNull(ruleConditions);
        assertEquals(1, ruleConditions.size());
        assertEquals(CONDITION_DEF_NAME, ruleConditions.get(0)
                .getActionConditionDefinitionName());
        Map<String, Serializable> condParams = ruleConditions.get(0)
                .getParameterValues();
        assertNotNull(condParams);
        assertEquals(1, condParams.size());
        assertTrue(condParams.containsKey(COND_PROP_NAME_1));
        assertEquals(COND_PROP_VALUE_1, condParams.get(COND_PROP_NAME_1));

        // Check the actions
        assertEquals(ACTION_DEF_NAME, ruleAction.getActionDefinitionName());
        Map<String, Serializable> actionParams = ruleAction.getParameterValues();
        assertNotNull(actionParams);
        assertEquals(1, actionParams.size());
        assertTrue(actionParams.containsKey(ACTION_PROP_NAME_1));
        assertEquals(ACTION_PROP_VALUE_1, actionParams.get(ACTION_PROP_NAME_1));
    }
}
