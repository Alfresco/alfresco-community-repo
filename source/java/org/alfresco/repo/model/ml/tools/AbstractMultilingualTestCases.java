/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.model.ml.tools;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Base multilingual test cases
 *
 * @author yanipig
 */
public abstract class AbstractMultilingualTestCases extends TestCase
{

    protected static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    protected ServiceRegistry serviceRegistry;
    protected AuthenticationComponent authenticationComponent;
    protected TransactionService transactionService;
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected VersionService versionService;
    protected MultilingualContentService multilingualContentService;
    protected NodeRef folderNodeRef;
    protected ContentFilterLanguagesService contentFilterLanguagesService;
    protected NodeArchiveService nodeArchiveService;
    protected EditionService editionService;

    @Override
    protected void setUp() throws Exception
    {
        nodeArchiveService = (NodeArchiveService) ctx.getBean("nodeArchiveService");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        versionService = serviceRegistry.getVersionService();
        multilingualContentService = (MultilingualContentService) ctx.getBean("MultilingualContentService");
        contentFilterLanguagesService = (ContentFilterLanguagesService) ctx.getBean("ContentFilterLanguagesService");
        editionService = (EditionService) ctx.getBean("EditionService");

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

    protected NodeRef createContent()
    {
        String name = "" + System.currentTimeMillis();
        return createContent(name);
    }

    protected NodeRef createContent(String name)
    {
        NodeRef contentNodeRef = fileFolderService.create(
                folderNodeRef,
                name,
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

}
