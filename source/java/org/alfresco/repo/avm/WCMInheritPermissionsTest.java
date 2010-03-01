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

import org.alfresco.config.JNDIConstants;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;

public class WCMInheritPermissionsTest extends AbstractSpringContextTest
{
    private static final String FILE_NAME = "fileForExport";
    private static final String STORE_NAME = "TestStore1";
    private static final String ROOT = "ROOT";

    private void createStaggingWithSnapshots(String storeName)
    {
        if (avmService.getStore(storeName) != null)
        {
            avmService.purgeStore(storeName);
        }

        avmService.createStore(storeName);
        assertNotNull(avmService.getStore(storeName));

        avmService.createDirectory(storeName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
        avmService.createSnapshot(storeName, "first", "first");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW));
        avmService.createDirectory(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW, JNDIConstants.DIR_DEFAULT_APPBASE);
        avmService.createSnapshot(storeName, "second", "second");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE));
        avmService.createDirectory(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE, ROOT);
        avmService.createSnapshot(storeName, "third", "third");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT));
        avmService.createFile(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT, FILE_NAME);
        avmService.createSnapshot(storeName, "fourth", "fourth");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT + "/" + FILE_NAME));

    }
    
    private void removeStore(String storeName)
    {
        avmService.purgeStore(storeName);
        assertNull(avmService.getStore(storeName));
    }

    public void testSetInheritParentPermissions()
    {
        createStaggingWithSnapshots(STORE_NAME);

        AVMNodeDescriptor nodeDescriptor = avmService.lookup(-1, STORE_NAME + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT + "/"
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
