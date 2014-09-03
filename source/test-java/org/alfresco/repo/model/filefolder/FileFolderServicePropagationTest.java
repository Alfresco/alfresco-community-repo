/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.model.filefolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * @see FileFolderService
 * @author Dmitry Velichkevich
 */
public class FileFolderServicePropagationTest extends TestCase
{
    private static final String TEST_USER_NAME = "userx";

    private static final String TEST_USER_PASSWORD = TEST_USER_NAME;

    private static final String ADMIN_USER_NAME = "admin";


    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    private Boolean defaultPreservationValue;


    private MutableAuthenticationService authenticationService;

    private TransactionService transactionService;

    private FileFolderServiceImpl fileFolderService;

    private PermissionService permissionService;

    private SearchService searchService;

    private NodeService nodeService;


    private FileInfo testFile;

    private FileInfo testFolder;

    private FileInfo testRootFolder;

    private FileInfo testEmptyFolder;


    @Before
    public void setUp() throws Exception
    {
        fileFolderService = (FileFolderServiceImpl) applicationContext.getBean("fileFolderService");

        if (null == defaultPreservationValue)
        {
            defaultPreservationValue = fileFolderService.isPreserveAuditableData();
        }

        ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);

        authenticationService = serviceRegistry.getAuthenticationService();
        transactionService = serviceRegistry.getTransactionService();
        permissionService = serviceRegistry.getPermissionService();
        searchService = serviceRegistry.getSearchService();
        nodeService = serviceRegistry.getNodeService();

        testFile = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
        {
            @Override
            public FileInfo execute() throws Throwable
            {
                FileInfo result = AuthenticationUtil.runAs(new RunAsWork<FileInfo>()
                {
                    @Override
                    public FileInfo doWork() throws Exception
                    {
                        ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
                        NodeRef companyHome = resultSet.getNodeRef(0);
                        resultSet.close();

                        StringBuilder name = new StringBuilder("TestRootFolder-").append(System.currentTimeMillis());
                        testRootFolder = fileFolderService.create(companyHome, name.toString(), ContentModel.TYPE_FOLDER);

                        name = new StringBuilder("TestDocument-").append(System.currentTimeMillis()).append(".txt");
                        FileInfo result = fileFolderService.create(testRootFolder.getNodeRef(), name.toString(), ContentModel.TYPE_CONTENT);
                        ContentWriter writer = fileFolderService.getWriter(result.getNodeRef());
                        writer.setEncoding("UTF-8");
                        writer.setMimetype("text/plain");
                        writer.putContent("Test content named " + result.getName());

                        name = new StringBuilder("TestEmptyFolder-").append(System.currentTimeMillis());
                        testEmptyFolder = fileFolderService.create(testRootFolder.getNodeRef(), name.toString(), ContentModel.TYPE_FOLDER);

                        name = new StringBuilder("TestFolder-").append(System.currentTimeMillis());
                        testFolder = fileFolderService.create(testRootFolder.getNodeRef(), name.toString(), ContentModel.TYPE_FOLDER);

                        return result;
                    }
                }, ADMIN_USER_NAME);

                AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        authenticationService.createAuthentication(TEST_USER_NAME, TEST_USER_PASSWORD.toCharArray());
                        permissionService.setPermission(testRootFolder.getNodeRef(), TEST_USER_NAME, PermissionService.FULL_CONTROL, true);
                        return null;
                    }
                });

                return result;
            }
        });
    }

    @After
    public void tearDown() throws Exception
    {
        // Resetting to default value...
        fileFolderService.setPreserveAuditableData(defaultPreservationValue);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                return AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        authenticationService.deleteAuthentication(TEST_USER_NAME);
                        fileFolderService.delete(testRootFolder.getNodeRef());
                        return null;
                    }
                });
            }
        });
    }


    @Test
    public void testPreservingPropertiesOfDocumentMnt8109() throws Exception
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            // Just stop to wait for the end of...
        }

        // Enabling preservation of modification properties data...
        fileFolderService.setPreserveAuditableData(true);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_NAME);
                moveObjectAndAssert(testFile);
                return null;
            }
        });
    }

    @Test
    public void testPreservingPropertiesOfFolderMnt8109() throws Exception
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            // Just stop to wait for the end of...
        }

        // Enabling preservation of modification properties data...
        fileFolderService.setPreserveAuditableData(true);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_NAME);
                moveObjectAndAssert(testFolder);
                return null;
            }
        });
    }

    @Test
    public void testPreservingPropertiesOfParentFolderMnt8109() throws Exception
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            // Just stop to wait for the end of...
        }

        // Enabling preservation of modification properties data...
        fileFolderService.setPreserveAuditableData(true);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_NAME);
                moveObjectAndAssert(testFile);
                return null;
            }
        });

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                FileInfo actualParent = fileFolderService.getFileInfo(getAndAssertSingleParent(testFile));
                assertEquals(testEmptyFolder.getModifiedDate(), actualParent.getModifiedDate());
                assertEquals(testEmptyFolder.getProperties().get(ContentModel.PROP_MODIFIER), actualParent.getProperties().get(ContentModel.PROP_MODIFIER));
                return null;
            }
        });
    }


    private void moveObjectAndAssert(FileInfo object) throws FileNotFoundException
    {
        FileInfo moved = fileFolderService.move(object.getNodeRef(), testEmptyFolder.getNodeRef(), object.getName());
        assertParent(moved, testEmptyFolder);
        assertEquals(object.getModifiedDate(), moved.getModifiedDate());
        assertEquals(object.getProperties().get(ContentModel.PROP_MODIFIER), moved.getProperties().get(ContentModel.PROP_MODIFIER));
    }


    @Test
    public void testNotPreservingPropertiesOfDocumentMnt8109() throws Exception
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            // Just stop to wait for the end of...
        }

        // Disabling preservation of modification properties data... 
        fileFolderService.setPreserveAuditableData(false);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {

                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_NAME);
                moveObjectAndAssertAbsenceOfPropertiesPreserving(testFile);
                return null;

            }
        });
    }

    @Test
    public void testNotPreservingPropertiesOfFolderMnt8109() throws Exception
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            // Just stop to wait for the end of...
        }

        // Disabling preservation of modification properties data...
        fileFolderService.setPreserveAuditableData(false);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {

                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_NAME);
                moveObjectAndAssertAbsenceOfPropertiesPreserving(testFolder);
                return null;

            }
        });
    }

    @Test
    public void testNotPreservingPropertiesOfParentFolderMnt8109() throws Exception
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            // Just stop to wait for the end of...
        }

        // Disabling preservation of modification properties data...
        fileFolderService.setPreserveAuditableData(false);
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_NAME);
                moveObjectAndAssertAbsenceOfPropertiesPreserving(testFile);
                return null;
            }
        });

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                FileInfo actualParent = fileFolderService.getFileInfo(getAndAssertSingleParent(testFile));
                assertTrue("Modification time difference MUST BE greater or equal than 1 000 milliseconds!", (actualParent.getModifiedDate().getTime() - testEmptyFolder
                        .getModifiedDate().getTime()) >= 1000);
                assertEquals(TEST_USER_NAME, actualParent.getProperties().get(ContentModel.PROP_MODIFIER));
                return null;
            }
        });
    }


    private void moveObjectAndAssertAbsenceOfPropertiesPreserving(FileInfo object) throws FileNotFoundException
    {
        FileInfo moved = fileFolderService.move(object.getNodeRef(), testEmptyFolder.getNodeRef(), object.getName());
        assertParent(moved, testEmptyFolder);
        assertTrue("Modification time difference MUST BE greater or equal than 1 000 milliseconds!", (moved.getModifiedDate().getTime() - object.getModifiedDate().getTime()) >= 1000);
        assertEquals(TEST_USER_NAME, moved.getProperties().get(ContentModel.PROP_MODIFIER));
    }

    private void assertParent(FileInfo child, FileInfo expectedParent)
    {
        assertEquals(expectedParent.getNodeRef(), getAndAssertSingleParent(child));
    }

    private NodeRef getAndAssertSingleParent(FileInfo child)
    {
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(child.getNodeRef());
        assertNotNull(("No one parent has been found for " + child.toString()), parentAssocs);
        assertEquals(1, parentAssocs.size());
        return parentAssocs.iterator().next().getParentRef();
    }
}
