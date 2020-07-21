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

import static org.junit.Assert.*;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStoreCaps;
import org.alfresco.service.cmr.repository.ContentReader;
import org.junit.Before;
import org.junit.Test;

public class MultiTAdminServiceImplTest
{
    private MultiTAdminServiceImpl tenantAdmin;
    
    @Before
    public void setUp() throws Exception
    {
        tenantAdmin = new MultiTAdminServiceImpl();
    }

    @Test
    public void testTenantDeployer()
    {
        ContentStore contentStore = new ConcreteTenantDeployer();
        TenantDeployer tenantDeployer = tenantAdmin.tenantDeployer(contentStore);
        assertNotNull(tenantDeployer);
    }
    
    @Test
    public void testTenantDeployerRetrievedByContentStoreCaps()
    {
        ContentStore contentStore = new FakeSubsystemProxy(false);
        TenantDeployer tenantDeployer = tenantAdmin.tenantDeployer(contentStore);
        assertNotNull(tenantDeployer);
    }
    
    @Test
    public void testTenantDeployerMayBeNullWhenInterfaceNotImplemented()
    {
        ContentStore contentStore = new BaseStore();
        TenantDeployer tenantDeployer = tenantAdmin.tenantDeployer(contentStore);
        assertNull(tenantDeployer);
    }
    
    @Test
    public void testTenantDeployerMayBeNullWhenProxyingAndInterfaceNotImplemented()
    {    
        // Represents proxy in front of non-TenantDeployer ContentStore
        ContentStore contentStore = new FakeSubsystemProxy(true);
        TenantDeployer tenantDeployer = tenantAdmin.tenantDeployer(contentStore);
        assertNull(tenantDeployer);
    }

    @Test
    public void testTenantRoutingContentStore()
    {
        ContentStore contentStore = new ConcreteTenantRoutingContentStore();
        TenantRoutingContentStore router = tenantAdmin.tenantRoutingContentStore(contentStore);
        assertNotNull(router);
    }
    
    @Test
    public void testTenantRoutingContentStoreRetrievedByContentStoreCaps()
    {
        ContentStore contentStore = new FakeSubsystemProxy(false);
        TenantRoutingContentStore router = tenantAdmin.tenantRoutingContentStore(contentStore);
        assertNotNull(router);
    }
    
    @Test
    public void testTenantRoutingContentStoreMayBeNullWhenInterfaceNotImplemented()
    {
        ContentStore contentStore = new BaseStore();
        TenantRoutingContentStore router = tenantAdmin.tenantRoutingContentStore(contentStore);
        assertNull(router);
    }
    
    @Test
    public void testTenantRoutingContentStoreMayBeNullWhenProxyingAndInterfaceNotImplemented()
    {    
        // Represents proxy in front of non-TenantRoutingContentStore ContentStore
        ContentStore contentStore = new FakeSubsystemProxy(true);
        TenantRoutingContentStore router = tenantAdmin.tenantRoutingContentStore(contentStore);
        assertNull(router);
    }
    

    
    
    

    // This is implemented by the CryptodocSubsystemProxyFactory in real life.
    private static class FakeSubsystemProxy extends BaseStore implements ContentStoreCaps
    {
        private boolean returnNull;
        
        FakeSubsystemProxy(boolean returnNull)
        {
            this.returnNull = returnNull;
        }
        
        @Override
        public TenantDeployer getTenantRoutingContentStore()
        {
            // would return the underlying ContentStore bean in real life (or null).
            return returnNull ? null : new ConcreteTenantRoutingContentStore();
        }

        @Override
        public TenantDeployer getTenantDeployer()
        {
            // would return the underlying ContentStore bean in real life (or null).
            return returnNull ? null : new ConcreteTenantDeployer();
        }
    }
    
    private static class ConcreteTenantDeployer extends BaseStore implements TenantDeployer
    {
        @Override
        public void onEnableTenant()
        {
        }

        @Override
        public void onDisableTenant()
        {
        }

        @Override
        public void init()
        {
        }

        @Override
        public void destroy()
        {
        }
    }
    
    private static class ConcreteTenantRoutingContentStore extends BaseStore implements TenantRoutingContentStore
    {
        @Override
        public void onEnableTenant()
        {
        }

        @Override
        public void onDisableTenant()
        {
        }

        @Override
        public void init()
        {
        }

        @Override
        public void destroy()
        {
        }   
    }
    
    private static class BaseStore extends AbstractContentStore
    {

        @Override
        public boolean isWriteSupported()
        {
            return false;
        }

        @Override
        public ContentReader getReader(String contentUrl)
        {
            return null;
        }
    }
    
}
