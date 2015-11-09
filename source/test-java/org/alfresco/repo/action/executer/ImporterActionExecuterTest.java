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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.action.executer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
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
import org.junit.Test;

/**
 * This class contains tests for {@link ImporterActionExecuter}.
 * 
 * @author abalmus
 */
public class ImporterActionExecuterTest
{
    @Test
    public void testImportArchiveWithSuspiciousPaths() throws IOException
    {
        final ApplicationContextInit applicationContextInit = new ApplicationContextInit();
        final ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContextInit.getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
        final NodeService nodeService = serviceRegistry.getNodeService();
        final ContentService contentService = serviceRegistry.getContentService();
        final RetryingTransactionHelper retryingTransactionHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        
        final File file = new File("./source/test-resources/import-archive-test/SuspiciousPathsArchive.zip");

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                AuthenticationUtil.setRunAsUserSystem();

                StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());

                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

                NodeRef zipFileNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/test/ImporterActionExecuterTest", "testAssocQName1"),
                        ContentModel.TYPE_CONTENT).getChildRef();

                NodeRef targetFolderNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/test/ImporterActionExecuterTest", "testAssocQName2"),
                        ContentModel.TYPE_FOLDER).getChildRef();

                contentService.getWriter(zipFileNodeRef, ContentModel.PROP_CONTENT, true).putContent(file);

                ContentData contentData = (ContentData) nodeService.getProperty(zipFileNodeRef, ContentModel.PROP_CONTENT);
                ContentData newContentData = ContentData.setMimetype(contentData, MimetypeMap.MIMETYPE_ZIP);

                nodeService.setProperty(zipFileNodeRef, ContentModel.PROP_CONTENT, newContentData);

                Action action = new ActionImpl(zipFileNodeRef, GUID.generate(), "ImporterActionExecuterTestActionDefinition");
                action.setParameterValue(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, targetFolderNodeRef);
                action.setParameterValue(ImporterActionExecuter.PARAM_ENCODING, "UTF-8");

                ImporterActionExecuter executer = new ImporterActionExecuter();
                executer.setNodeService(nodeService);
                executer.setContentService(contentService);

                try
                {
                    executer.execute(action, zipFileNodeRef);
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
                    nodeService.deleteStore(storeRef);
                    
                    AuthenticationUtil.clearCurrentSecurityContext();
                }

                return null;
            }
        });
    }
}
