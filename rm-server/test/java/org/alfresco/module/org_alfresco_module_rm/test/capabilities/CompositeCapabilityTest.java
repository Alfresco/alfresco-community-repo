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
package org.alfresco.module.org_alfresco_module_rm.test.capabilities;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Declarative capability unit test
 * 
 * @author Roy Wetherall
 */
public class CompositeCapabilityTest extends BaseRMTestCase
{
    private NodeRef record;
    private NodeRef declaredRecord;
    
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();
        
        // Pre-filed content
        record = utils.createRecord(rmFolder, "record.txt");
        declaredRecord = utils.createRecord(rmFolder, "declaredRecord.txt");      
    }
    
    @Override
    protected void setupTestData()
    {
        super.setupTestData();
        
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
                utils.declareRecord(declaredRecord);
                
                return null;
            }
        });
    }
    
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();
    }
    
    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);
        
        // Give all the users file permission objects
        for (String user : testUsers)
        {
            filePlanPermissionService.setPermission(rmContainer, user, RMPermissionModel.FILING);
        }                
    }
    
    public void testUpdate()
    {
        final Capability capability = capabilityService.getCapability("Update");
        assertNotNull(capability);
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(record));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(declaredRecord));
                
                return null;
            }
        }, recordsManagerName);
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                
                return null;
            }
        }, userName);
        
      
    }
    
    public void testUpdateProperties()
    {
        final Capability capability = capabilityService.getCapability("UpdateProperties");
        assertNotNull(capability);
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(record));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(declaredRecord));
                
                return null;
            }
        }, recordsManagerName);
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                
                return null;
            }
        }, userName);
        
      
    }
}
