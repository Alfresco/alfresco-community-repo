/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
        assertEquals(4, handles.length);
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
        assertEquals(4, handles.length);
        for (RepositoryExportHandle handle : handles)
        {
            assertTrue(nodeService.exists(handle.exportFile));
        }
        
        setComplete();
    }

}
