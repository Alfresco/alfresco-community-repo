/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class VirtualStoreImplTest extends VirtualizationIntegrationTest
{
    private static Log logger = LogFactory.getLog(VirtualStoreImplTest.class);

    private VirtualStoreImpl virtualStore;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        virtualStore = ctx.getBean("virtualStore",
                                   VirtualStoreImpl.class);

    }

    @Test
    public void testNonVirtualizable() throws Exception
    {
        NodeRef aNodeRef = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                   "TestVirtualStoreImpl_createVirtualizedFolder",
                                                   null);
        assertFalse(virtualStore.canVirtualize(aNodeRef));

        try
        {
            virtualStore.virtualize(aNodeRef);
            fail("Should not be able to virtualize non-virtualizable nodes.");
        }
        catch (VirtualizationException e)
        {
            logger.info(e);
        }
    }

    @Test
    public void testCanVirtualize() throws Exception
    {
        NodeRef solrFacetsNodeRef = new NodeRef("workspace://SpacesStore/solr_facets_root_space");
        boolean canVirtualize = virtualStore.canVirtualize(solrFacetsNodeRef);
        assertEquals(false,
                     canVirtualize);
    }
    
    private String asTypedPermission(String perm)
    {
        return virtualStore.getUserPermissions().getPermissionTypeQName()+"."+perm;
    }
    
    private void assertHasQueryNodePermission(AccessStatus accessStatus,String perm)
    {
        VirtualUserPermissions virtualUserPermissions = virtualStore.getUserPermissions();
        
        assertEquals(AccessStatus.DENIED,virtualUserPermissions.hasQueryNodePermission(perm));
        assertEquals(AccessStatus.DENIED,virtualUserPermissions.hasQueryNodePermission(asTypedPermission(perm)));
    }
    
    private void assertHasVirtualNodePermission(AccessStatus accessStatus,String perm,boolean readonly)
    {
        VirtualUserPermissions virtualUserPermissions = virtualStore.getUserPermissions();
        
        assertEquals(AccessStatus.DENIED,virtualUserPermissions.hasVirtualNodePermission(perm,readonly));
        assertEquals(AccessStatus.DENIED,virtualUserPermissions.hasVirtualNodePermission(asTypedPermission(perm),readonly));
    }
    
    @Test
    public void testConfiguredUserPermissions() throws Exception
    {
        assertHasQueryNodePermission(AccessStatus.DENIED,PermissionService.DELETE);
        assertHasQueryNodePermission(AccessStatus.DENIED,PermissionService.DELETE_NODE);
        assertHasQueryNodePermission(AccessStatus.DENIED,PermissionService.CHANGE_PERMISSIONS);

        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.CREATE_ASSOCIATIONS,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.UNLOCK,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.CANCEL_CHECK_OUT,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.DELETE,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.DELETE_NODE,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.CHANGE_PERMISSIONS,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.WRITE_CONTENT,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.WRITE,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.WRITE_PROPERTIES,true);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.WRITE,false);
        assertHasVirtualNodePermission(AccessStatus.DENIED,PermissionService.WRITE_PROPERTIES,false);

    }
}
