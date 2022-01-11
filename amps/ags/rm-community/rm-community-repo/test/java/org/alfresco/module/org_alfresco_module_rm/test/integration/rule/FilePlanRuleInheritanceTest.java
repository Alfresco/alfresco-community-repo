/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.rule;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.impl.DeclareRecordAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.springframework.extensions.webscripts.GUID;

/**
 * File plan rule inheritance test
 * 
 * @author Roy Wetherall
 * @since 2.4
 */
public class FilePlanRuleInheritanceTest extends BaseRMTestCase
{
    private RuleService ruleService;
    
    @Override
    protected void initServices()
    {
        super.initServices();
        ruleService = (RuleService)applicationContext.getBean("RuleService");
    }
    
    @Override
    protected boolean isRMSiteTest()
    {
        return false;
    }
    
    private NodeRef createFilePlan()
    {
        return filePlanService.createFilePlan(folder, "My File Plan");
    }
    
    /** 
     * Given that a single rule is set on the parent folder of the file plan root
     * And that it is configured to apply to children
     * When we ask for the rules on the file plan, including those inherited
     * Then it will not include those defined on the parent folder
     */
    public void testFilePlanDoesNotInheritRulesFromParentFolder()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef filePlan = null;
            private Rule rule = null;
            private List<Rule> rules = null;
            
            public void given()
            {
                filePlan = createFilePlan();
                
                // create a rule that applies to childre
                Action completeRecordAction = actionService.createAction(DeclareRecordAction.NAME);
                rule = new Rule();
                rule.setRuleType("inbound");
                rule.setAction(completeRecordAction);
                rule.applyToChildren(true);
            }

            public void when()
            {
                // save rule on file plan root parent folder
                ruleService.saveRule(folder, rule);
            }

            public void then()
            {
                // get rules, including those inherited
                rules = ruleService.getRules(filePlan, true);

                // rules aren't inhreited from file plan root parent folder
                assertEquals(0, rules.size());
            }
        }); 
    }
    
    /** 
     *  Given that a single rule is set on the file plan root
     *  And that it is configured to apply to children
     *  When we ask for the rules on the unfiled record container including those inherited 
     *  Then it will not include those defined on the file plan root
     *  
     *  See https://issues.alfresco.com/jira/browse/RM-3148
     */
    public void testFilePlanRulesInheritedInUnfiledContainer()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef filePlan = null;
            private List<Rule> rules = null;
            private Rule rule = null;

            public void given()
            {
                filePlan = createFilePlan();
                
                // create a rule that applies to childre
                Action completeRecordAction = actionService.createAction(DeclareRecordAction.NAME);
                rule = new Rule();
                rule.setRuleType("inbound");
                rule.setAction(completeRecordAction);
                rule.applyToChildren(true);
            }

            public void when()
            {
                // save rule on file plan root
                ruleService.saveRule(filePlan, rule);
            }

            public void then()
            {
                // get rules, including those inherited
                NodeRef unfiledRecordContainer = filePlanService.getUnfiledContainer(filePlan);
                rules = ruleService.getRules(unfiledRecordContainer, true);

                // rules aren't inhreited from file plan root
                assertEquals(0, rules.size());
            }
        }); 
    }
    
    /** 
     *  Given that a single rule is set on the file plan root
     *  And that it is configured to apply to children
     *  When we ask for the rules on the hold container including those inherited 
     *  Then it will not include those defined on the file plan root
     */
    public void testFilePlanRulesInheritedInHoldContainer()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef filePlan = null;
            private List<Rule> rules = null;
            private Rule rule = null;

            public void given()
            {
                filePlan = createFilePlan();
                
                // create a rule that applies to childre
                Action completeRecordAction = actionService.createAction(DeclareRecordAction.NAME);
                rule = new Rule();
                rule.setRuleType("inbound");
                rule.setAction(completeRecordAction);
                rule.applyToChildren(true);
            }

            public void when()
            {
                // save rule on file plan root
                ruleService.saveRule(filePlan, rule);
            }

            public void then()
            {
                // get rules, including those inherited
                NodeRef container = filePlanService.getHoldContainer(filePlan);
                rules = ruleService.getRules(container, true);

                // rules aren't inhreited from file plan root
                assertEquals(0, rules.size());
            }
        }); 
    }
    
    /** 
     *  Given that a single rule is set on the file plan root
     *  And that it is configured to apply to children
     *  When we ask for the rules on the transfer container including those inherited 
     *  Then it will not include those defined on the file plan root
     */
    public void testFilePlanRulesInheritedInTransferContainer()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef filePlan = null;
            private List<Rule> rules = null;
            private Rule rule = null;

            public void given()
            {
                filePlan = createFilePlan();
                
                // create a rule that applies to childre
                Action completeRecordAction = actionService.createAction(DeclareRecordAction.NAME);
                rule = new Rule();
                rule.setRuleType("inbound");
                rule.setAction(completeRecordAction);
                rule.applyToChildren(true);
            }

            public void when()
            {
                // save rule on file plan root
                ruleService.saveRule(filePlan, rule);
            }

            public void then()
            {
                // get rules, including those inherited
                NodeRef container = filePlanService.getTransferContainer(filePlan);
                rules = ruleService.getRules(container, true);

                // rules aren't inhreited from file plan root
                assertEquals(0, rules.size());
            }
        }); 
    }
    
    /** 
     *  Given that a single rule is set on the file plan root
     *  And that it is configured to apply to children
     *  When we ask for the rules on a record category including those inherited 
     *  Then it will include those defined on the file plan root
     */
    public void testFilePlanRulesInheritedOnRecordCategory()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef filePlan = null;
            private NodeRef recordCategory = null;
            private List<Rule> rules = null;
            private Rule rule = null;

            public void given()
            {
                filePlan = createFilePlan();
                recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create a rule that applies to childre
                Action completeRecordAction = actionService.createAction(DeclareRecordAction.NAME);
                rule = new Rule();
                rule.setRuleType("inbound");
                rule.setAction(completeRecordAction);
                rule.applyToChildren(true);
            }

            public void when()
            {
                // save rule on file plan root
                ruleService.saveRule(filePlan, rule);
            }

            public void then()
            {
                // get rules, including those inherited
                rules = ruleService.getRules(recordCategory, true);

                // rules aren't inhreited from file plan root
                assertEquals(1, rules.size());
            }
        }); 
    }
}
