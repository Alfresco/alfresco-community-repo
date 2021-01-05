/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.TestWithUserUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class UnlockMethodTest
{
    private UnlockMethod unlockMethod;
    private LockMethod lockMethod;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private @Mock WebDAVHelper davHelper;    
    ApplicationContext appContext;
    
    /**
     * Services used by the tests
     */
    private NodeService nodeService;
    private CheckOutCheckInService cociService;
    private ContentService contentService;
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private PermissionService permissionService;

    /**
     * Data used by the tests
     */
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private String userNodeRef;
    private NodeRef folderNodeRef;
    private NodeRef fileNodeRef;
    private NodeRef fileWorkingCopyNodeRef;

    /**
     * Types and properties used by the tests
     */
    private static final String CONTENT_1 = "This is some content";
    private static final String TEST_STORE_IDENTIFIER = "test_store-" + System.currentTimeMillis();
    private static final String TEST_FILE_NAME = "file";

    /**
     * User details 
     */
    private String userName;
    private static final String PWD = "password";
    
    @Before
    public void setUp() throws Exception
    {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        unlockMethod = new UnlockMethod();
        unlockMethod.setDetails(request, response, davHelper, null);
        lockMethod = new LockMethod();
        lockMethod.setDetails(request, null, davHelper, null);
    }

    /**
     * Set up preconditions for unlock a checked out node
     */
    protected void setUpPreconditionForCheckedOutTest() throws Exception
    {
        appContext = ApplicationContextHelper.getApplicationContext(new String[]
        {
            "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
            "classpath:alfresco/remote-api-context.xml"
        });

        // Set the services
        this.cociService = (CheckOutCheckInService) appContext.getBean("checkOutCheckInService");
        this.contentService = (ContentService) appContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) appContext.getBean("authenticationService");
        this.permissionService = (PermissionService) appContext.getBean("permissionService");
        this.transactionService = (TransactionService) appContext.getBean("TransactionService");
        this.nodeService = (NodeService) appContext.getBean("NodeService");
        // Authenticate as system to create initial test data set
        this.authenticationComponent = (AuthenticationComponent) appContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();

        RetryingTransactionCallback<Void> createTestFileCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Create the store and get the root node reference
                storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, TEST_STORE_IDENTIFIER);
                if (!nodeService.exists(storeRef))
                {
                    storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, TEST_STORE_IDENTIFIER);
                }
                rootNodeRef = nodeService.getRootNode(storeRef);

                // Create and authenticate the user
                userName = "webdavUnlockTest" + GUID.generate();
                TestWithUserUtils.createUser(userName, PWD, rootNodeRef, nodeService, authenticationService);
                permissionService.setPermission(rootNodeRef, userName, PermissionService.ALL_PERMISSIONS, true);
                TestWithUserUtils.authenticateUser(userName, PWD, rootNodeRef, authenticationService);
                userNodeRef = TestWithUserUtils.getCurrentUser(authenticationService);

                // create test file in test folder
                folderNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("test"), ContentModel.TYPE_FOLDER,
                        Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, "folder")).getChildRef();
                fileNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("test"), ContentModel.TYPE_CONTENT,
                        Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, TEST_FILE_NAME)).getChildRef();
                ContentWriter contentWriter = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.setMimetype("text/plain");
                contentWriter.setEncoding("UTF-8");
                contentWriter.putContent(CONTENT_1);

                // Check out test file
                fileWorkingCopyNodeRef = cociService.checkout(fileNodeRef);
                assertNotNull(fileWorkingCopyNodeRef);
                assertEquals(userNodeRef, nodeService.getProperty(fileNodeRef, ContentModel.PROP_LOCK_OWNER));

                return null;
            }

        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(createTestFileCallback);
    }

    @Test
    public void parseValidLockTokenHeader() throws WebDAVServerException
    {
        String lockToken = "976e2f82-40ab-4852-a867-986e9ce11f82:admin";
        String lockHeaderValue = "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">";
        request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
        unlockMethod.parseRequestHeaders();
        
        assertEquals(lockToken, unlockMethod.getLockToken());
    }
    
    @Test
    public void parseInvalidLockTokenHeader()
    {        
        String lockToken = "976e2f82-40ab-4852-a867-986e9ce11f82:admin";
        String lockHeaderValue = "<wrongprefix:" + lockToken + ">";
        request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
        try
        {
            unlockMethod.parseRequestHeaders();
            fail("Exception should have been thrown, but wasn't.");
        }
        catch (WebDAVServerException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatusCode());
        }
    }
    
    @Test
    public void parseMissingLockTokenHeader()
    {        
        // Note: we're not adding the lock token header
        try
        {
            unlockMethod.parseRequestHeaders();
            fail("Exception should have been thrown, but wasn't.");
        }
        catch (WebDAVServerException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatusCode());
        }
    }
    
    /**
     * Test MNT-9680: Working copies are open in read-only mode when using Webdav online edit
     *
     * @throws Exception
     */
    @Test
    public void unlockWorkingCopy() throws Exception
    {
        setUpPreconditionForCheckedOutTest();
        try
        {
            String workingCopyName = nodeService.getProperty(fileWorkingCopyNodeRef, ContentModel.PROP_NAME).toString();
            String lockToken = fileWorkingCopyNodeRef.getId() + WebDAV.LOCK_TOKEN_SEPERATOR + this.userName;
            String lockHeaderValue = "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">";
            final WebDAVHelper davHelper = (WebDAVHelper) appContext.getBean("webDAVHelper");

            request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
            request.setRequestURI("/" + workingCopyName);
            String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<d:lockinfo xmlns:d=\"DAV:\">"
                + "<d:lockscope><d:exclusive/></d:lockscope>"
                + "</d:lockinfo>";

            request.setContent(content.getBytes("UTF-8"));

            lockMethod.setDetails(request, new MockHttpServletResponse(), davHelper, folderNodeRef);
            lockMethod.parseRequestHeaders();
            lockMethod.parseRequestBody();

            RetryingTransactionCallback<Void> lockExecuteImplCallBack = new RetryingTransactionCallback<Void>()
            {

                @Override
                public Void execute() throws Throwable
                {
                    lockMethod.executeImpl();
                    return null;
                }

            };
            this.transactionService.getRetryingTransactionHelper().doInTransaction(lockExecuteImplCallBack);

            unlockMethod.setDetails(request, new MockHttpServletResponse(), davHelper, folderNodeRef);
            unlockMethod.parseRequestHeaders();

            RetryingTransactionCallback<Void> unlockExecuteImplCallBack = new RetryingTransactionCallback<Void>()
            {

                @Override
                public Void execute() throws Throwable
                {
                    unlockMethod.executeImpl();
                    return null;
                }

            };
            this.transactionService.getRetryingTransactionHelper().doInTransaction(unlockExecuteImplCallBack);

            assertNull("lockType property should be deleted on unlock", nodeService.getProperty(fileWorkingCopyNodeRef, ContentModel.PROP_LOCK_TYPE));
            assertNull("lockOwner property should be deleted on unlock", nodeService.getProperty(fileWorkingCopyNodeRef, ContentModel.PROP_LOCK_OWNER));

        }
        finally
        {
            // clear context for current user
            this.authenticationComponent.clearCurrentSecurityContext();

            // delete test store as system user
            this.authenticationComponent.setSystemUserAsCurrentUser();
            RetryingTransactionCallback<Void> deleteStoreCallback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (nodeService.exists(storeRef))
                    {
                        nodeService.deleteStore(storeRef);
                    }
                    return null;
                }
            };
            this.transactionService.getRetryingTransactionHelper().doInTransaction(deleteStoreCallback);
        }
    }

    /**
     * Test that it is impossible to unlock a checked out node
     * 
     * @throws Exception
     */
    @Test
    public void unlockCheckedOutNode() throws Exception
    {
        setUpPreconditionForCheckedOutTest();
        try
        {
            String lockToken = fileNodeRef.getId() + WebDAV.LOCK_TOKEN_SEPERATOR + this.userName;
            String lockHeaderValue = "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">";
            request.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockHeaderValue);
            request.setRequestURI("/" + TEST_FILE_NAME);

            WebDAVHelper davHelper = (WebDAVHelper) appContext.getBean("webDAVHelper");
            unlockMethod.setDetails(request, new MockHttpServletResponse(), davHelper, folderNodeRef);
            unlockMethod.parseRequestHeaders();
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    unlockMethod.executeImpl();
                    return null;
                }
            });

            fail("Exception should have been thrown, but wasn't.");
        }
        catch (AlfrescoRuntimeException e)
        {
            if (e.getCause() instanceof WebDAVServerException)
            {
                WebDAVServerException ee = (WebDAVServerException) e.getCause();
                assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, ee.getHttpStatusCode());
            }
            else
            {
                fail("Incorrect exception thrown.");
            }
        }
        catch (WebDAVServerException e)
        {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, e.getHttpStatusCode());
        }
        finally
        {
            // clear context for current user
            this.authenticationComponent.clearCurrentSecurityContext();

            // delete test store as system user
            this.authenticationComponent.setSystemUserAsCurrentUser();
            RetryingTransactionCallback<Void> deleteStoreCallback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (nodeService.exists(storeRef))
                    {
                        nodeService.deleteStore(storeRef);
                    }
                    return null;
                }
            };
            this.transactionService.getRetryingTransactionHelper().doInTransaction(deleteStoreCallback);
        }
    }

}
