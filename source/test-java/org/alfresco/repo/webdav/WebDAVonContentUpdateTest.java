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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for MNT-10966: 4.1.7 breaks onContentUpdate policies when using webdav
 * Implemented on base of PutMethodTest which was introduced by alex.mukha.
 *
 * @author viachaslau.tsikhanovich, alex.mukha
 */
public class WebDAVonContentUpdateTest
{
    private static ApplicationContext ctx;

    private static final String TEST_DATA_FILE_NAME = "filewithdata.txt";
    private static final String DAV_LOCK_INFO_XML = "davLockInfoAdmin.xml";
    private byte[] testDataFile;
    private byte[] davLockInfoFile;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private WebDAVMethod method;
    private UserTransaction txn = null;

    private SearchService searchService;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private WebDAVHelper webDAVHelper;
    private LockService lockService;
    private PolicyComponent policyComponent;

    private NodeRef companyHomeNodeRef;
    private StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

	private boolean flag;
	private int counter = 0;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(new String[]
            {
                "classpath:alfresco/application-context.xml",
                "classpath:alfresco/web-scripts-application-context.xml",
                "classpath:alfresco/remote-api-context.xml"
            });
    }

    @Before
    public void setUp() throws Exception
    {
        searchService = ctx.getBean("SearchService", SearchService.class);
        fileFolderService = ctx.getBean("FileFolderService", FileFolderService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        transactionService = ctx.getBean("transactionService", TransactionService.class);
        webDAVHelper = ctx.getBean("webDAVHelper", WebDAVHelper.class);
        lockService = ctx.getBean("LockService", LockService.class);
        policyComponent = ctx.getBean("policyComponent", PolicyComponent.class);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHomeNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // find "Company Home"
                ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
                NodeRef result = resultSet.getNodeRef(0);
                resultSet.close();

                return result;
            }
        });

        InputStream testDataIS = getClass().getClassLoader().getResourceAsStream(TEST_DATA_FILE_NAME);
        InputStream davLockInfoIS = getClass().getClassLoader().getResourceAsStream(DAV_LOCK_INFO_XML);
        testDataFile = IOUtils.toByteArray(testDataIS);
        davLockInfoFile = IOUtils.toByteArray(davLockInfoIS);
        testDataIS.close();
        davLockInfoIS.close();

        txn = transactionService.getUserTransaction();
        txn.begin();
    }

    @After
    public void tearDown() throws Exception
    {
        method = null;
        request = null;
        response = null;
        testDataFile = null;
        davLockInfoFile = null;

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    public void doOnContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        flag = true;
        ++counter;
    }

    /**
     * Put a content file and check that onContentUpdate fired
     * <p>
     * Lock the file
     * <p>
     * Put the contents
     * <p>
     * Unlock the node
     */
    @Test
    public void testUploadNewContentFiresContentUpdatePolicies() throws Exception
    {
        flag = false;
        counter = 0;
        policyComponent.bindClassBehaviour(OnContentUpdatePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "doOnContentUpdate"));

        String fileName = "file-" + GUID.generate();
        NodeRef fileNoderef = null;

        try
        {
            executeMethod(WebDAV.METHOD_LOCK, fileName, davLockInfoFile, null);
            
            ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home//cm:" + fileName + "\"");
            fileNoderef = resultSet.getNodeRef(0);
            resultSet.close();
            
            assertEquals("File should be locked", LockStatus.LOCK_OWNER, lockService.getLockStatus(fileNoderef));
        }
        catch (Exception e)
        {
            fail("Failed to lock a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();

        // Construct IF HEADER
        String lockToken = fileNoderef.getId() + WebDAV.LOCK_TOKEN_SEPERATOR + AuthenticationUtil.getAdminUserName();
        String lockHeaderValue = "(<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">)";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_IF, lockHeaderValue);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, fileName, testDataFile, headers);
            
            assertTrue("File does not exist.", nodeService.exists(fileNoderef));
            assertEquals("Filename is not correct", fileName, nodeService.getProperty(fileNoderef, ContentModel.PROP_NAME));
            assertTrue("Expected return status is " + HttpServletResponse.SC_NO_CONTENT + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_NO_CONTENT == response.getStatus());

            assertTrue("File should have NO_CONTENT aspect", nodeService.hasAspect(fileNoderef, ContentModel.ASPECT_NO_CONTENT));
            InputStream updatedFileIS = fileFolderService.getReader(fileNoderef).getContentInputStream();
            byte[] updatedFile = IOUtils.toByteArray(updatedFileIS);
            updatedFileIS.close();
            assertTrue("The content has to be equal", ArrayUtils.isEquals(testDataFile, updatedFile));
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();

        headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_LOCK_TOKEN, "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">");
        try
        {
            executeMethod(WebDAV.METHOD_UNLOCK, fileName, null, headers);

            assertTrue("Expected return status is " + HttpServletResponse.SC_NO_CONTENT + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_NO_CONTENT == response.getStatus());
            assertFalse("File should not have NO_CONTENT aspect", nodeService.hasAspect(fileNoderef, ContentModel.ASPECT_NO_CONTENT));
            assertEquals("File should be unlocked", LockStatus.NO_LOCK, lockService.getLockStatus(fileNoderef));
        }
        catch (Exception e)
        {
            fail("Failed to unlock a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        assertTrue("onContentUpdate policies were not triggered", flag);
        assertEquals("onContentUpdate policies should be triggered only once",  counter, 1);

        if (fileNoderef != null)
        {
            nodeService.deleteNode(fileNoderef);
        }
    }

    /**
     * Executes WebDAV method for testing
     * <p>
     * Sets content to request from a test file
     * 
     * @param method Method to prepare, should be initialized (PUT, LOCK, UNLOCK are supported)
     * @param fileName the name of the file set to the context, can be used with path, i.e. "path/to/file/fileName.txt"
     * @param content If <b>not null</b> adds test content to the request
     * @param headers to set to request, can be null
     * @throws Exception
     */
    private void executeMethod(String methodName, String fileName, byte[] content, Map<String, String> headers) throws Exception
    {
        if (methodName == WebDAV.METHOD_PUT)
            method = new PutMethod();
        else if (methodName == WebDAV.METHOD_LOCK)
            method = new LockMethod();
        else if (methodName == WebDAV.METHOD_UNLOCK)
            method = new UnlockMethod();
        if (method != null)
        {
            request = new MockHttpServletRequest(methodName, "/alfresco/webdav/" + fileName);
            response = new MockHttpServletResponse();
            request.setServerPort(8080);
            request.setServletPath("/webdav");
            if (content != null)
            {
                request.setContent(content);
            }

            if (headers != null && !headers.isEmpty())
            {
                for (String key : headers.keySet())
                {
                    request.addHeader(key, headers.get(key));
                }
            }

            method.setDetails(request, response, webDAVHelper, companyHomeNodeRef);

            method.execute();
        }
    }
}
