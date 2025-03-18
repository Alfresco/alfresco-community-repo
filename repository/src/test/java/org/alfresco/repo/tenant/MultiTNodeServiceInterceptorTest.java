/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.tenant;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;

/**
 * @see MultiTNodeServiceInterceptor
 * 
 * @since 3.0
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
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
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                RetryingTransactionCallback<Object> createTenantCallback = new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable
                    {
                        tenantAdminService.createTenant(tenant1, tenant1Pwd.toCharArray());
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(createTenantCallback, false, true);
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
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
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                RetryingTransactionCallback<Object> deleteTenantCallback = new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable
                    {
                        tenantAdminService.deleteTenant(tenant1);
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(deleteTenantCallback, false, true);
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    /**
     * Control case.
     */
    public void testSetUp()
    {}
}
