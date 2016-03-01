 
package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Record service implementation unit test.
 *
 * @author Roy Wetherall
 */
public class CreateRecordActionTest extends BaseRMTestCase
{
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

    public void testCreateRecordAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));

                Action action = actionService.createAction(CreateRecordAction.NAME);
                action.setParameterValue(CreateRecordAction.PARAM_HIDE_RECORD, false);
                action.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                actionService.executeAction(action, dmDocument);

                return null;
            }

            public void test(Void result) throws Exception
            {
                assertTrue(recordService.isRecord(dmDocument));

                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
            };
        },
        dmCollaborator);
    }
}
