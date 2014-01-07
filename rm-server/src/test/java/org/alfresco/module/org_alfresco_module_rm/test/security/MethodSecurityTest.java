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
package org.alfresco.module.org_alfresco_module_rm.test.security;

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
