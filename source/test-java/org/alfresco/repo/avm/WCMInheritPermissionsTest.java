/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.avm;

import java.io.IOException;

import org.alfresco.config.JNDIConstants;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

public class WCMInheritPermissionsTest extends AVMServiceTestBase
{
    private static final String FILE_NAME = "fileForExport";
    private static final String STORE_NAME = "TestStore1";
    private static final String ROOT = "ROOT";
    
    protected PermissionService permissionService;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        permissionService = (PermissionService)fContext.getBean("permissionService");
    }
    
    private void createStagingWithSnapshots(String storeName) throws IOException
    {
        if (fService.getStore(storeName) != null)
        {
            fService.purgeStore(storeName);
        }
        
        fService.createStore(storeName);
        assertNotNull(fService.getStore(storeName));
        
        fService.createDirectory(storeName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
        fService.createSnapshot(storeName, "first", "first");
        assertNotNull(fService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW));
        fService.createDirectory(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW, JNDIConstants.DIR_DEFAULT_APPBASE);
        fService.createSnapshot(storeName, "second", "second");
        assertNotNull(fService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE));
        fService.createDirectory(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE, ROOT);
        fService.createSnapshot(storeName, "third", "third");
        assertNotNull(fService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT));
        fService.createFile(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT, FILE_NAME).close();
        fService.createSnapshot(storeName, "fourth", "fourth");
        assertNotNull(fService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT + "/" + FILE_NAME));
    }
    
    private void removeStore(String storeName)
    {
        fService.purgeStore(storeName);
        assertNull(fService.getStore(storeName));
    }
    
    public void testSetInheritParentPermissions() throws IOException
    {
        createStagingWithSnapshots(STORE_NAME);
        
        AVMNodeDescriptor nodeDescriptor = fService.lookup(-1, STORE_NAME + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT + "/"
                + FILE_NAME);
        assertNotNull(nodeDescriptor);
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, nodeDescriptor.getPath());
        assertNotNull(nodeRef);
        
        permissionService.setInheritParentPermissions(nodeRef, false);
        assertFalse(permissionService.getInheritParentPermissions(nodeRef));
        permissionService.setInheritParentPermissions(nodeRef, true);
        assertTrue(permissionService.getInheritParentPermissions(nodeRef));
        
        removeStore(STORE_NAME);
    }
}
