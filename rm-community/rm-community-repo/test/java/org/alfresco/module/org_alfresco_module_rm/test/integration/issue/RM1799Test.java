 
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.util.GUID;

/**
 * Test for RM-1799
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RM1799Test extends BaseRMTestCase
{
    private String myUser;
    private NodeRef category;

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        myUser = GUID.generate();
        createPerson(myUser);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, myUser);
    }

    public void testRM1799() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.setPermission(filePlan, myUser, RMPermissionModel.FILING);
                return null;
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                category = filePlanService.createRecordCategory(filePlan, GUID.generate());
                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(category, RMPermissionModel.FILING));
                return null;
            }
        }, myUser);
    }
}
