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
package org.alfresco.repo.action.executer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class contains tests for {@link ImporterActionExecuter}.
 * 
 * @author abalmus
 */
public class ImporterActionExecuterTest
{
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit ctx = new ApplicationContextInit();

    private static final String FILE_NAME = "import-archive-test/SuspiciousPathsArchive.zip";

    private static ContentService contentService;
    private static ImporterActionExecuter importerActionExecuter;
    private static NodeService nodeService;
    private static ServiceRegistry serviceRegistry;

    private static StoreRef storeRef;

    @BeforeClass
    public static void setup() throws Exception
    {
        serviceRegistry = ctx.getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY, ServiceRegistry.class);
        contentService = serviceRegistry.getContentService();
        nodeService = serviceRegistry.getNodeService();
        importerActionExecuter = ctx.getApplicationContext().getBean(ImporterActionExecuter.NAME, ImporterActionExecuter.class);

        AuthenticationUtil.setRunAsUserSystem();

        // we need a store
        storeRef = serviceRegistry.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<StoreRef>()
        {
            public StoreRef execute()
            {
                StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());
                return storeRef;
            }
        });
    }

    @AfterClass
    public static void tearDown()
    {
        try
        {
            serviceRegistry.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute()
                {
                    if (storeRef != null)
                    {
                        nodeService.deleteStore(storeRef);
                    }
                    return null;
                }
            });
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    @Test
    public void testImportArchiveWithSuspiciousPaths() throws IOException
    {
        final RetryingTransactionHelper retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

                NodeRef zipFileNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/test/ImporterActionExecuterTest", "testAssocQName1"), ContentModel.TYPE_CONTENT).getChildRef();

                NodeRef targetFolderNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/test/ImporterActionExecuterTest", "testAssocQName2"), ContentModel.TYPE_FOLDER).getChildRef();

                putContent(zipFileNodeRef, FILE_NAME);

                Action action = createAction(zipFileNodeRef, "ImporterActionExecuterTestActionDefinition", targetFolderNodeRef);

                try
                {
                    importerActionExecuter.execute(action, zipFileNodeRef);
                    fail("An AlfrescoRuntimeException should have occured.");
                }
                catch (AlfrescoRuntimeException e)
                {
                    assertTrue(e.getMessage().contains(ImporterActionExecuter.ARCHIVE_CONTAINS_SUSPICIOUS_PATHS_ERROR));
                }
                finally
                {
                    nodeService.deleteNode(targetFolderNodeRef);
                    nodeService.deleteNode(zipFileNodeRef);
                }

                return null;
            }
        });
    }

    /**
     * MNT-16292: Unzipped files which have folders do not get the cm:titled
     * aspect applied
     * 
     * @throws IOException
     */
    @Test
    public void testImportHasTitledAspectForFolders() throws IOException
    {
        final RetryingTransactionHelper retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

                // create test data
                NodeRef zipFileNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_CONTENT).getChildRef();
                NodeRef targetFolderNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_FOLDER).getChildRef();

                putContent(zipFileNodeRef, "import-archive-test/folderCmTitledAspectArchive.zip");

                Action action = createAction(zipFileNodeRef, "ImporterActionExecuterTestActionDefinition", targetFolderNodeRef);

                try
                {
                    importerActionExecuter.execute(action, zipFileNodeRef);

                    // check if import succeeded 
                    NodeRef importedFolder = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS, "folderCmTitledAspectArchive");
                    assertNotNull("import action failed", importedFolder);

                    // check if aspect is set
                    boolean hasAspectTitled = nodeService.hasAspect(importedFolder, ContentModel.ASPECT_TITLED);
                    assertTrue("folder didn't get the cm:titled aspect applied", hasAspectTitled);

                    // MNT-17017 check ContentModel.PROP_TITLE is not set on the top level folder, just like Share
                    String title = (String)nodeService.getProperty(importedFolder, ContentModel.PROP_TITLE);
                    assertNull("The title should not have cm:title set", title);
                }
                finally
                {
                    // clean test data
                    nodeService.deleteNode(targetFolderNodeRef);
                    nodeService.deleteNode(zipFileNodeRef);
                }

                return null;
            }
        });
    }

    private void putContent(NodeRef zipFileNodeRef, String resource)
    {
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource(resource);
        final File file = new File(url.getFile());
        
        contentService.getWriter(zipFileNodeRef, ContentModel.PROP_CONTENT, true).putContent(file);

        ContentData contentData = (ContentData) nodeService.getProperty(zipFileNodeRef, ContentModel.PROP_CONTENT);
        ContentData newContentData = ContentData.setMimetype(contentData, MimetypeMap.MIMETYPE_ZIP);

        nodeService.setProperty(zipFileNodeRef, ContentModel.PROP_CONTENT, newContentData);
    }

    private Action createAction(NodeRef nodeRef, String actionDefinitionName, NodeRef targetNodeRef)
    {
        Action action = new ActionImpl(nodeRef, GUID.generate(), actionDefinitionName);
        action.setParameterValue(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, targetNodeRef);
        action.setParameterValue(ImporterActionExecuter.PARAM_ENCODING, "UTF-8");

        return action;
    }
}
