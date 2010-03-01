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
package org.alfresco.repo.model.ml.tools;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

        // Create a folder to work in
        RetryingTransactionCallback<NodeRef> createFolderCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
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
        folderNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createFolderCallback);
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
