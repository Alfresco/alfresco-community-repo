/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestActionPropertySubs;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Extended action service test.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedActionServiceTest extends BaseRMTestCase
{
    /** Services */
    protected ActionService dmActionService;
    protected PermissionService dmPermissionService;

    /** Action names */
    public static final String TEST_ACTION = "testAction";
    public static final String TEST_ACTION_2 = "testAction2";
    public static final String TEST_DM_ACTION = "testDMAction";
    public static final String RECORD_ONLY_ACTION = "recordOnlyAction";
    public static final String RECORD_AND_FOLDER_ONLY_ACTION = "recordandFolderOnlyAction";
    public static final String DELEGATE_ACTION = "rmDelegateAction";


    @Override
    protected void initServices()
    {
        super.initServices();
        dmActionService = (ActionService) applicationContext.getBean("ActionService");
    }

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
                List<ActionDefinition> result = dmActionService.getActionDefinitions(recordOne);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertTrue(containsAction(result, TEST_ACTION_2));
                assertFalse(containsAction(result, TEST_DM_ACTION));
                assertTrue(containsAction(result, RECORD_ONLY_ACTION));
                assertTrue(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertTrue(containsAction(result, DELEGATE_ACTION));

                result = dmActionService.getActionDefinitions(rmFolder);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertTrue(containsAction(result, TEST_ACTION_2));
                assertFalse(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertTrue(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                result = dmActionService.getActionDefinitions(rmContainer);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertTrue(containsAction(result, TEST_ACTION_2));
                assertFalse(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertFalse(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                result = dmActionService.getActionDefinitions(dmDocument);
                assertNotNull(result);
                assertFalse(containsAction(result, TEST_ACTION));
                assertFalse(containsAction(result, TEST_ACTION_2));
                assertTrue(containsAction(result, TEST_DM_ACTION));
                assertFalse(containsAction(result, RECORD_ONLY_ACTION));
                assertFalse(containsAction(result, RECORD_AND_FOLDER_ONLY_ACTION));
                assertFalse(containsAction(result, DELEGATE_ACTION));

                result = dmActionService.getActionDefinitions(dmFolder);
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
            if (actionDefinition.getName().equals(actionName) == true)
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
                Action action = dmActionService.createAction(TestActionPropertySubs.NAME);

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

                dmActionService.executeAction(action, rmFolder);

                return null;
            }
        });
    }
}
