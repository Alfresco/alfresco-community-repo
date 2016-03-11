package org.alfresco.module.org_alfresco_module_rm.test.legacy.security;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Tests method level security of core alfresco services.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public class MethodSecurityTest extends BaseRMTestCase implements RMPermissionModel
{
    /**
     * Indicate this is a user test.
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    /**
     * Test node service security access
     */
    public void testNodeService()
    {
        doTestInTransaction(new FailureTest
        (
                "We don't have permission to access this node."
        )
        {
            @Override
            public void run()
            {
                nodeService.getProperties(rmContainer);                
            }
            
        }, rmUserName);
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.setPermission(rmContainer, rmUserName, READ_RECORDS);
                return null;
            }
            
            @Override
            public void test(Void result) throws Exception
            {
                nodeService.getProperties(rmContainer);
            }
            
        }, rmUserName);
    }
}
