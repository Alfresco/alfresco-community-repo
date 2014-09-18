/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the {@link LockMethod} class.
 * 
 * @author pavel.yurkevich
 */
public class LockMethodTest
{
    private LockMethod lockMethod;
    private PropFindMethod propFindMethod;
    
    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext(new String[]
    {
        "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
        "classpath:alfresco/remote-api-context.xml"
    });
    
    /**
     * Services used by the tests
     */
    private WebDAVHelper davHelper;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private NodeService nodeService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    private ContentService contentService;
    private FileFolderService fileFolderService;

    /**
     * Types and properties used by the tests
     */
    private static final String CONTENT_1 = "This is some content";
    private static final String TEST_FILE_NAME = "file" + GUID.generate();

    /**
     * Data used by the tests
     */
    private NodeRef folderNodeRef;
    private NodeRef fileNodeRef;
    
    /**
     * User details 
     */
    private String userName;
    private static final String PWD = "password";
    
    @Before
    public void setUp() throws Exception
    {
        lockMethod = new LockMethod();
        propFindMethod = new PropFindMethod();
        
        this.transactionService = (TransactionService) applicationContext.getBean("TransactionService");
        this.davHelper = (WebDAVHelper) applicationContext.getBean("webDAVHelper");
        this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");        
        this.nodeService = (NodeService) applicationContext.getBean("NodeService");
        this.authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        this.permissionService = (PermissionService) applicationContext.getBean("permissionService");
        this.contentService = (ContentService) applicationContext.getBean("contentService");
        this.fileFolderService = (FileFolderService) applicationContext.getBean("fileFolderService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        RetryingTransactionCallback<Void> createTestFileCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

                // Create and authenticate the user
                userName = "webdavLockTest" + GUID.generate();
                TestWithUserUtils.createUser(userName, PWD, rootNodeRef, nodeService, authenticationService);
                permissionService.setPermission(rootNodeRef, userName, PermissionService.ALL_PERMISSIONS, true);
                TestWithUserUtils.authenticateUser(userName, PWD, rootNodeRef, authenticationService);

                // create test file in test folder
                folderNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("test"), ContentModel.TYPE_FOLDER,
                        Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, "folder")).getChildRef();
                fileNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName("test"), ContentModel.TYPE_CONTENT,
                        Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, TEST_FILE_NAME)).getChildRef();
                ContentWriter contentWriter = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.setMimetype("text/plain");
                contentWriter.setEncoding("UTF-8");
                contentWriter.putContent(CONTENT_1);

                return null;
            }

        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(createTestFileCallback);
    }
    
    @After
    public void tearDown()
    {
        lockMethod = null;
        propFindMethod = null;
        
        // clear context for current user
        this.authenticationComponent.clearCurrentSecurityContext();

        // delete test store as system user
        this.authenticationComponent.setSystemUserAsCurrentUser();
        RetryingTransactionCallback<Void> deleteTestFolderCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                if (nodeService.exists(folderNodeRef))
                {
                    nodeService.deleteNode(folderNodeRef);
                }
                return null;
            }
        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(deleteTestFolderCallback);
    }
    
    @Test
    public void testRefreshLock() throws Exception
    {
        MockHttpServletRequest lockRequest = new MockHttpServletRequest(); 
        lockRequest.addHeader(WebDAV.HEADER_TIMEOUT, WebDAV.SECOND + 5);
        lockRequest.setRequestURI("/" + TEST_FILE_NAME);
        
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:lockinfo xmlns:D=\"DAV:\"><D:lockscope xmlns:D=\"DAV:\">" +
        		"<D:exclusive xmlns:D=\"DAV:\"/></D:lockscope><D:locktype xmlns:D=\"DAV:\"><D:write xmlns:D=\"DAV:\"/></D:locktype>" +
        		"<D:owner xmlns:D=\"DAV:\">" + userName + "</D:owner></D:lockinfo>";

        lockRequest.setContent(content.getBytes("UTF-8"));

        lockMethod.setDetails(lockRequest, new MockHttpServletResponse(), davHelper, folderNodeRef);
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
        
        // lock node for 5 seconds
        this.transactionService.getRetryingTransactionHelper().doInTransaction(lockExecuteImplCallBack);
        
        RetryingTransactionCallback<LockInfo> getNodeLockInfoCallBack = new RetryingTransactionCallback<LockInfo>()
        {
            @Override
            public LockInfo execute() throws Throwable
            {
                return lockMethod.getNodeLockInfo(fileFolderService.getFileInfo(fileNodeRef));
            }
        };
        
        // get lock info
        LockInfo lockInfo = this.transactionService.getRetryingTransactionHelper().doInTransaction(getNodeLockInfoCallBack);
        
        assertNotNull(lockInfo);
        
        // wait for 1 second
        Thread.sleep(1000);
        
        String lockToken = fileNodeRef.getId() + WebDAV.LOCK_TOKEN_SEPERATOR + this.userName;
        String lockHeaderValue = "(<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">)";
        lockRequest.addHeader(WebDAV.HEADER_IF, lockHeaderValue);
        
        lockMethod.parseRequestHeaders();
        
        // update lock for another 5 seconds
        this.transactionService.getRetryingTransactionHelper().doInTransaction(lockExecuteImplCallBack);
        
        // get updated lock info
        LockInfo updatedLockInfo = this.transactionService.getRetryingTransactionHelper().doInTransaction(getNodeLockInfoCallBack);
        
        assertNotNull(updatedLockInfo);
        
        assertEquals("Lock owner should not change after lock refresh.", lockInfo.getOwner(), updatedLockInfo.getOwner());
        assertEquals("Lock token should not change after lock refresh.", lockInfo.getExclusiveLockToken(), updatedLockInfo.getExclusiveLockToken());
        
        assertFalse("Expires was not updated.", lockInfo.getExpires().equals(updatedLockInfo.getExpires()));
        assertTrue("Expires was updated incorrectly.", lockInfo.getExpires().before(updatedLockInfo.getExpires()));
        
        // prepare propfind method
        MockHttpServletRequest propFindRequest = new MockHttpServletRequest();
        propFindRequest.setRequestURI("/" + TEST_FILE_NAME);
        propFindRequest.addHeader(WebDAV.HEADER_DEPTH, "1");
        content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/>" +
        		"<D:getcontentlength/><D:resourcetype/><D:supportedlock/><D:lockdiscovery/></D:prop></D:propfind>";

        propFindRequest.setContent(content.getBytes("UTF-8"));
        
        MockHttpServletResponse propfindResponse = new MockHttpServletResponse();
        propFindMethod.setDetails(propFindRequest, propfindResponse, davHelper, folderNodeRef);
        
        propFindMethod.parseRequestHeaders();
        propFindMethod.parseRequestBody();
        
        RetryingTransactionCallback<Void> propfindExecuteImplCallBack = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                propFindMethod.executeImpl();
                return null;
            }
        };
        
        // make propfind method call for locked resource
        this.transactionService.getRetryingTransactionHelper().doInTransaction(propfindExecuteImplCallBack);
        
        String response = propfindResponse.getContentAsString();
        
        assertFalse("Propfind response should contain lock informarion.", response.indexOf("lockdiscovery") == -1);
        
        // wait for lock expiration
        Thread.sleep(6000);
        
        propfindResponse = new MockHttpServletResponse();
        propFindMethod.setDetails(propFindRequest, propfindResponse, davHelper, folderNodeRef);
        
        // make another propfind call on resource with expired lock
        this.transactionService.getRetryingTransactionHelper().doInTransaction(propfindExecuteImplCallBack);
        response = propfindResponse.getContentAsString();
        
        // check that lock information is not there for expired lock
        assertTrue("Propfind response should not conatain information about expired lock", response.indexOf("lockdiscovery") == -1);
    }
    
    @Test
    public void testMNT_10873() throws Exception
    {
        String fileName = TEST_FILE_NAME + GUID.generate();
        final MockHttpServletRequest lockRequest = new MockHttpServletRequest();
        MockHttpServletResponse lockResponse = new MockHttpServletResponse();
        lockRequest.addHeader(WebDAV.HEADER_TIMEOUT, WebDAV.SECOND + 5);
        // set request uri to point to non-existent file
        lockRequest.setRequestURI("/" + fileName);
        
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:lockinfo xmlns:D=\"DAV:\"><D:lockscope xmlns:D=\"DAV:\">" +
                "<D:exclusive xmlns:D=\"DAV:\"/></D:lockscope><D:locktype xmlns:D=\"DAV:\"><D:write xmlns:D=\"DAV:\"/></D:locktype>" +
                "<D:owner xmlns:D=\"DAV:\">" + userName + "</D:owner></D:lockinfo>";

        lockRequest.setContent(content.getBytes("UTF-8"));

        lockMethod.setDetails(lockRequest, lockResponse, davHelper, folderNodeRef);
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
        
        // lock node for 5 seconds
        this.transactionService.getRetryingTransactionHelper().doInTransaction(lockExecuteImplCallBack);
        
        assertEquals("Unexpected response status code.", HttpServletResponse.SC_CREATED, lockResponse.getStatus());
        
        RetryingTransactionCallback<NodeRef> getNodeRefCallback = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                return lockMethod.getNodeForPath(folderNodeRef, lockRequest.getRequestURI()).getNodeRef();
            }
        };
        
        NodeRef nodeRef = this.transactionService.getRetryingTransactionHelper().doInTransaction(getNodeRefCallback);
        
        assertTrue("NodeRef should exists.", nodeService.exists(nodeRef));        
        assertTrue("sys:webdavNoContent aspect should be applied on node.", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WEBDAV_NO_CONTENT));
        
        // sleep for 6 seconds to ensure that timer was triggered
        Thread.sleep(6000);
        
        assertFalse("File should note exist in repo any more.", nodeService.exists(nodeRef));
        assertFalse("File should note exist in trashcan.", nodeService.exists(new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, nodeRef.getId())));
    }
    
    @Test
    public void testMNT_11990() throws Exception
    {
        MockHttpServletRequest lockRequest = new MockHttpServletRequest(); 
        lockRequest.addHeader(WebDAV.HEADER_TIMEOUT, WebDAV.SECOND + 3600);        
        lockRequest.addHeader(WebDAV.HEADER_IF, "(<" + WebDAV.makeLockToken(fileNodeRef, userName) + ">)");
        lockRequest.setRequestURI("/" + TEST_FILE_NAME);

        lockMethod.setDetails(lockRequest, new MockHttpServletResponse(), davHelper, folderNodeRef);
        lockMethod.parseRequestHeaders();
        lockMethod.parseRequestBody();
        
        RetryingTransactionCallback<Void> lockExecuteImplCallBack = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    lockMethod.executeImpl();
                    fail("Lock should not be refreshed for non-locked file.");
                }
                catch (WebDAVServerException e)
                {
                    assertEquals(e.getHttpStatusCode(), HttpServletResponse.SC_BAD_REQUEST);
                }
                return null;
            }
        };
        
        // try to refresh lock for non-locked node        
        this.transactionService.getRetryingTransactionHelper().doInTransaction(lockExecuteImplCallBack);
    }
}
