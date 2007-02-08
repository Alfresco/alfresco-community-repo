/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */package org.alfresco.repo.exporter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.RepositoryExporterService;
import org.alfresco.service.cmr.view.RepositoryExporterService.FileExportHandle;
import org.alfresco.service.cmr.view.RepositoryExporterService.RepositoryExportHandle;
import org.alfresco.util.BaseSpringTest;


public class RepositoryExporterComponentTest extends BaseSpringTest
{
    private RepositoryExporterService repositoryService;
    private AuthenticationComponent authenticationComponent;
    private NodeService nodeService;
    private FileFolderService fileFolderService;

    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        this.fileFolderService = (FileFolderService)applicationContext.getBean(ServiceRegistry.FILE_FOLDER_SERVICE.getLocalName());
        this.repositoryService = (RepositoryExporterService)applicationContext.getBean("repositoryExporterComponent");
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }
    

    public void testDummy()
    {
    }
    
    public void xtestTempFileExport()
        throws Exception
    {
        FileExportHandle[] handles = repositoryService.export("test");
        assertNotNull(handles);
        assertEquals(6, handles.length);
        for (FileExportHandle tempFile : handles)
        {
            assertTrue(tempFile.exportFile.exists());
        }
    }
        
    public void xtestRepositoryExport()
        throws Exception
    {
        // Create a temp store to hold exports
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        FileInfo container = fileFolderService.create(rootNode, "export", ContentModel.TYPE_FOLDER);

        // Export stores
        RepositoryExportHandle[] handles = repositoryService.export(container.getNodeRef(), "test");
        assertNotNull(handles);
        assertEquals(6, handles.length);
        for (RepositoryExportHandle handle : handles)
        {
            assertTrue(nodeService.exists(handle.exportFile));
        }
        
        setComplete();
    }

}
