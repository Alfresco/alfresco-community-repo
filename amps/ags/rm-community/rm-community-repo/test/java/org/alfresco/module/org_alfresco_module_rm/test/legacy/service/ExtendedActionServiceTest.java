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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestActionPropertySubs;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;

/**
 * Extended action service test.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedActionServiceTest extends BaseRMTestCase
{
    /** Action names */
    public static final String TEST_ACTION = "testAction";
    public static final String TEST_ACTION_2 = "testAction2";
    public static final String TEST_DM_ACTION = "testDMAction";
    public static final String RECORD_ONLY_ACTION = "recordOnlyAction";
    public static final String RECORD_AND_FOLDER_ONLY_ACTION = "recordandFolderOnlyAction";
    public static final String DELEGATE_ACTION = "rmDelegateAction";

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    // NOTE:  temporarily disabled test ... now that RM actions are no longer registered with the action service, aplicability
    //        may no longer be relevant ... possibly something to back out??
    public void xtestAvailableActions()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                List<ActionDefinition> result = actionService.getActionDefinitions(recordOne);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertTrue(containsAction(result, TEST_ACTION_2));
                assertFalse(containsAction(result, TEST_DM_ACTION));
                assertTrue(containsAction(result, RECORD_ONLY_ACTION));
                assertTrue(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertTrue(containsAction(result, DELEGATE_ACTION));

                result = actionService.getActionDefinitions(rmFolder);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertTrue(containsAction(result, TEST_ACTION_2));
                assertFalse(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertTrue(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                result = actionService.getActionDefinitions(rmContainer);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertTrue(containsAction(result, TEST_ACTION_2));
                assertFalse(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertFalse(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                result = actionService.getActionDefinitions(dmDocument);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertFalse(containsAction(result, TEST_ACTION_2));
                assertTrue(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertFalse(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                result = actionService.getActionDefinitions(dmFolder);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertFalse(containsAction(result, TEST_ACTION_2));
                assertTrue(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertFalse(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                return null;
            }
        });
    }

    private boolean containsAction(List<ActionDefinition> list, String actionName)
    {
        boolean result = false;

        for (ActionDefinition actionDefinition : list)
        {
            if (actionDefinition.getName().equals(actionName))
            {
                result = true;
                break;
            }
        }

        return result;
    }

    public void testActionPropertySubstitution() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Action action = actionService.createAction(TestActionPropertySubs.NAME);

                action.setParameterValue("dayShort", "${date.day.short}");
                action.setParameterValue("dayShort2", "${date.day}");
                action.setParameterValue("dayLong", "${date.day.long}");
                action.setParameterValue("dayNumber", "${date.day.number}");
                action.setParameterValue("dayYear", "${date.day.year}");

                action.setParameterValue("monthShort", "${date.month.short}");
                action.setParameterValue("monthShort2", "${date.month}");
                action.setParameterValue("monthLong", "${date.month.long}");
                action.setParameterValue("monthNumber", "${date.month.number}");

                action.setParameterValue("yearShort", "${date.year.short}");
                action.setParameterValue("yearShort2", "${date.year}");
                action.setParameterValue("yearLong", "${date.year.long}");
                action.setParameterValue("yearWeek", "${date.year.week}");

                action.setParameterValue("name", "${node.cm:name}");

                action.setParameterValue("company", "${message.test.company}");

                action.setParameterValue("combo", "${date.year.long}/${date.month.short}/${node.cm:name}-${message.test.company}.txt");

                actionService.executeAction(action, rmFolder);

                return null;
            }
        });
    }

    /**
     * RM-3000 
     * Tests if the actions extending DelegateAction inherit the parameter definitions from their delegate action 
     */
    public void testDelegateActions()
    {
        /*
         * set-property-value is the delegate action for setPropertyValue.
         */
        assertTrue(inheritsAllParameterDefinitions("setPropertyValue", "set-property-value"));

        /*
         * rmscript is the delegate action for executeScript.
         */
        assertTrue(inheritsAllParameterDefinitions("executeScript", "rmscript"));

        /*
         * mail is the delegate action for sendEmail.
         */
        assertTrue(inheritsAllParameterDefinitions("sendEmail", "mail"));
    }
    
    /**
     * Checks if the action definition rmAction inherits all the parameter definitions from delegateAction.
     * @param rmAction The name of the action definition extending DelegateAction.
     * @param delegateAction The name of the delegate action.
     * @return true if rmAction inherits all the parameter definitions from delegateAction. false otherwise.
     */
    private boolean inheritsAllParameterDefinitions(String rmAction, String delegateAction)
    {
        /*
         * Get the parameter definition list for rmAction
         */
        ActionDefinition rmActionDefinition = actionService.getActionDefinition(rmAction);
        assertNotNull(rmActionDefinition);
        List<ParameterDefinition> rmParameterDefinitions = rmActionDefinition.getParameterDefinitions();

        /*
         * Get the parameter definition list for the delegate action
         */
        ActionDefinition delegateActionDefinition = actionService.getActionDefinition(delegateAction);
        assertNotNull(delegateActionDefinition);
        List<ParameterDefinition> delegateParameterDefinitions = delegateActionDefinition.getParameterDefinitions();

        /*
         * Check if rmActionDefinition contains all the elements in  rmActionDefinition
         */
        return rmParameterDefinitions.containsAll(delegateParameterDefinitions);
    }
}
