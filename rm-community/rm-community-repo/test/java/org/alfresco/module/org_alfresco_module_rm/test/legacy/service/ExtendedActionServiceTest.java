 
package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestActionPropertySubs;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;

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
}
