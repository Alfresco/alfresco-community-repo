 
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.integration.hold.DeleteHoldTest;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Unit test for https://issues.alfresco.com/jira/browse/RM-1463
 *
 * @author Tuna Aksoy
 * @since 2.2
 * @version 1.0
 */
public class RM1463Test extends DeleteHoldTest
{
    public void testAddRecordFolderToHoldWithoutFilingPermissionOnRecordFolder()
    {
        // Create hold
        final NodeRef hold = createAndCheckHold();

        doTestInTransaction(new Test<Void>()
        {
           @Override
           public Void run()
           {
               // Add the user to the RM Manager role
               filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, userName);

               // Give the user filing permissions on the hold
               permissionService.setPermission(hold, userName, RMPermissionModel.FILING, true);

               // Give the user only read permissions on the record folder
               permissionService.setPermission(rmFolder, userName, RMPermissionModel.READ_RECORDS, true);

               return null;
           }
        });

        doTestInTransaction(new FailureTest(AlfrescoRuntimeException.class)
        {
            @Override
            public void run()
            {
                holdService.addToHold(hold, rmFolder);
            }
        }, userName);
    }
}
