/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;

/**
 * @see MultiTNodeServiceInterceptor 
 * 
 * @since 3.0 
 * @author Derek Hulley
 */
public class MultiTNodeServiceInterceptorTest extends TestCase
{
    public static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private String tenant1 = "tenant-" + GUID.generate();
    private String tenant1Pwd = "pwd1";
    private boolean enableTest = true;
    private TransactionService transactionService;
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    private MultiTNodeServiceInterceptor interceptor;
    
    @Override
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        interceptor = (MultiTNodeServiceInterceptor) ctx.getBean("multiTNodeServiceInterceptor");
        
        // If MT is disabled, then disable all tests
        if (!tenantAdminService.isEnabled())
        {
            enableTest = false;
            return;
        }
        
        // Create a tenant
        RetryingTransactionCallback<Object> createTenantCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                tenantAdminService.createTenant(tenant1, tenant1Pwd.toCharArray());
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createTenantCallback, false, true);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        // If MT is disabled, then disable all tests
        if (!tenantAdminService.isEnabled())
        {
            return;
        }
        
        // Delete a tenant
        RetryingTransactionCallback<Object> createTenantCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                tenantAdminService.deleteTenant(tenant1);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createTenantCallback, false, true);
    }
    
    /**
     * Control case.
     */
    public void testSetUp()
    {
    }
}
