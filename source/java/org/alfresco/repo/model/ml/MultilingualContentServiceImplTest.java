/*
 * Copyright (C) 2007 Alfresco, Inc.
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
 */
package org.alfresco.repo.model.ml;

import java.util.Locale;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.ml.MultilingualContentServiceImpl
 * 
 * @author Derek Hulley
 */
public class MultilingualContentServiceImplTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private MultilingualContentService multilingualContentService;
    private NodeRef folderNodeRef;
    
    @Override
    protected void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        multilingualContentService = (MultilingualContentService) ctx.getBean("MultilingualContentService");
        
        // Run as admin
        authenticationComponent.setCurrentUser("admin");
        
        // Create a folder to work in
        TransactionWork<NodeRef> createFolderWork = new TransactionWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                // Create the folder
                NodeRef folderNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                        ContentModel.TYPE_FOLDER).getChildRef();
                // done
                return folderNodeRef;
            }
        };
        folderNodeRef = TransactionUtil.executeInUserTransaction(transactionService, createFolderWork);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        // Clear authentication
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    private NodeRef createContent()
    {
        NodeRef contentNodeRef = fileFolderService.create(
                folderNodeRef,
                "" + System.currentTimeMillis(),
                ContentModel.TYPE_CONTENT).getNodeRef();
        // add some content
        ContentWriter contentWriter = fileFolderService.getWriter(contentNodeRef);
        contentWriter.putContent("ABC");
        // done
        return contentNodeRef;
    }
    
    public void testSetup() throws Exception
    {
        // Ensure that content can be created
        createContent();
    }

    public void testMakeTranslation() throws Exception
    {
        NodeRef contentNodeRef = createContent();
        // Turn the content into a translation with the appropriate structures
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(contentNodeRef, Locale.CHINESE);
        // Check it
        assertNotNull("Container not created", mlContainerNodeRef);
    }
}
