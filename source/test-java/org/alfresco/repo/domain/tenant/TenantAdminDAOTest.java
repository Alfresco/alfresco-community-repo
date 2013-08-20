/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.domain.tenant;

import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see TenantAdminDAO
 * 
 * @author janv
 * @since 4.0
 */
public class TenantAdminDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private TenantAdminDAO tenantAdminDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        tenantAdminDAO = (TenantAdminDAO)ctx.getBean("tenantAdminDAO");
    }
    
    private TenantEntity createTenant(final String tenantDomain, final boolean enabled) throws Exception
    {
        RetryingTransactionCallback<TenantEntity> callback = new RetryingTransactionCallback<TenantEntity>()
        {
            public TenantEntity execute() throws Throwable
            {
                TenantEntity tenantEntity = new TenantEntity();
                tenantEntity.setTenantDomain(tenantDomain);
                tenantEntity.setEnabled(enabled);
                
                return tenantAdminDAO.createTenant(tenantEntity);
            }
        };
        return txnHelper.doInTransaction(callback, false);
    }
    
    private void deleteTenant(final String tenantDomain) throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                tenantAdminDAO.deleteTenant(tenantDomain);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, false);
    }
    
    private void updateTenant(final TenantUpdateEntity tenantUpdateEntity) throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                tenantAdminDAO.updateTenant(tenantUpdateEntity);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, false);
    }
    
    private TenantEntity getTenant(final String tenantDomain) throws Exception
    {
        RetryingTransactionCallback<TenantEntity> callback = new RetryingTransactionCallback<TenantEntity>()
        {
            public TenantEntity execute() throws Throwable
            {
                return tenantAdminDAO.getTenant(tenantDomain);
            }
        };
        return txnHelper.doInTransaction(callback, true);
    }
    
    private TenantUpdateEntity getTenantForUpdate(final String tenantDomain) throws Exception
    {
        RetryingTransactionCallback<TenantUpdateEntity> callback = new RetryingTransactionCallback<TenantUpdateEntity>()
        {
            public TenantUpdateEntity execute() throws Throwable
            {
                return tenantAdminDAO.getTenantForUpdate(tenantDomain);
            }
        };
        return txnHelper.doInTransaction(callback, true);
    }
    
    private List<TenantEntity> listTenants(final boolean enabledOnly) throws Exception
    {
        RetryingTransactionCallback<List<TenantEntity>> callback = new RetryingTransactionCallback<List<TenantEntity>>()
        {
            public List<TenantEntity> execute() throws Throwable
            {
                return tenantAdminDAO.listTenants(enabledOnly);
            }
        };
        return txnHelper.doInTransaction(callback, true);
    }
    
    public void testCreateAndDeleteTenant() throws Exception
    {
        final String tenantDomain = getName() + "-" + System.currentTimeMillis();
        
        TenantEntity tenantEntity= getTenant(tenantDomain);
        assertNull(tenantEntity);
        
        TenantEntity newTenantEntity = new TenantEntity();
        newTenantEntity.setTenantDomain(tenantDomain);
        newTenantEntity.setEnabled(false);
        
        TenantEntity createTenantEntity = createTenant(tenantDomain, false);
        assertNotNull(createTenantEntity);
        
        tenantEntity= getTenant(tenantDomain);
        assertEquals(createTenantEntity, tenantEntity);
        
        deleteTenant(tenantDomain);
        
        assertNull(getTenant(tenantDomain));
    }
    
    public void testCreateTenantWithRollback() throws Exception
    {
        final String tenantDomain = getName() + "-" + System.currentTimeMillis();
        
        final TenantEntity newTenantEntity = new TenantEntity();
        newTenantEntity.setTenantDomain(tenantDomain);
        newTenantEntity.setEnabled(false);
        
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                createTenant(tenantDomain, false);
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        
        // Check that it doesn't exist
        assertNull(getTenant(tenantDomain));
    }
    
    public void testUpdateTenant() throws Exception
    {
        final String tenantDomain = getName() + "-" + System.currentTimeMillis();
        
        TenantEntity tenantEntity = getTenant(tenantDomain);
        assertNull(tenantEntity);
        
        TenantEntity createTenantEntity = createTenant(tenantDomain, false);
        assertNotNull(createTenantEntity);
        assertFalse(createTenantEntity.getEnabled());
        
        TenantUpdateEntity tenantUpdateEntity = getTenantForUpdate(tenantDomain);
        assertEquals(createTenantEntity, tenantUpdateEntity);
        assertFalse(tenantUpdateEntity.getEnabled());
        
        assertEquals(createTenantEntity.getTenantDomain(), tenantUpdateEntity.getTenantDomain());
        assertEquals(createTenantEntity.getEnabled(), tenantUpdateEntity.getEnabled());
        assertEquals(createTenantEntity.getTenantName(), tenantUpdateEntity.getTenantName());
        assertEquals(createTenantEntity.getContentRoot(), tenantUpdateEntity.getContentRoot());
        assertEquals(createTenantEntity.getDbUrl(), tenantUpdateEntity.getDbUrl());
        
        tenantUpdateEntity.setEnabled(true);
        updateTenant(tenantUpdateEntity);
        
        tenantEntity = getTenant(tenantDomain);
        assertNotNull(tenantEntity);
        assertTrue(tenantEntity.getEnabled());
        
        assertEquals(tenantEntity.getTenantDomain(), tenantUpdateEntity.getTenantDomain());
        assertEquals(tenantEntity.getEnabled(), tenantUpdateEntity.getEnabled());
        assertEquals(tenantEntity.getTenantName(), tenantUpdateEntity.getTenantName());
        assertEquals(tenantEntity.getContentRoot(), tenantUpdateEntity.getContentRoot());
        assertEquals(tenantEntity.getDbUrl(), tenantUpdateEntity.getDbUrl());
        
        deleteTenant(tenantDomain);
        
        assertNull(getTenant(tenantDomain));
    }
    
    public void testListTenants() throws Exception
    {
        final String tenantDomainPrefix = getName() + "-" + System.currentTimeMillis();
        final int cnt = 5;
        
        int beforeCnt = listTenants(false).size();
        int enabledCnt = listTenants(true).size();
        
        for (int i = 1; i <= cnt; i++)
        {
            String tenantDomain = tenantDomainPrefix + "-" + i;
            TenantEntity tenantEntity = getTenant(tenantDomain);
            assertNull(tenantEntity);
            
            tenantEntity = createTenant(tenantDomain, false);
            assertNotNull(tenantEntity);
            
            assertEquals(i+beforeCnt, listTenants(false).size());
            assertEquals("Tenant enabled/disabled count incorrect.", enabledCnt, listTenants(true).size());
            
            tenantEntity = getTenant(tenantDomain);
            assertNotNull(tenantEntity);
        }
        
        for (int i = cnt; i >= 1; i--)
        {
            String tenantDomain = tenantDomainPrefix + "-" + i;
            TenantEntity tenantEntity = getTenant(tenantDomain);
            assertNotNull(tenantEntity);
            
            deleteTenant(tenantDomain);
            
            assertEquals(i-1+beforeCnt, listTenants(false).size());
            
            tenantEntity = getTenant(tenantDomain);
            assertNull(tenantEntity);
        }
    }
}
