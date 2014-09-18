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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the {@link PutMethod} class.
 *
 * @author alex.mukha
 */
public class PutMethodTest
{
    private static ApplicationContext ctx;

    private static final String USER1_NAME = "user1-" + PutMethodTest.class.getName();
    private static final String USER2_NAME = "user2-" + PutMethodTest.class.getName();
    private static final String TEST_DATA_FILE_NAME = "filewithdata.txt";
    private static final String DAV_LOCK_INFO_ADMIN = "davLockInfoAdmin.xml";
    private static final String DAV_LOCK_INFO_USER2 = "davLockInfoUser2.xml";
    private byte[] testDataFile;
    private byte[] davLockInfoAdminFile;
    private byte[] davLockInfoUser2File;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private WebDAVMethod method;
    private UserTransaction txn = null;

    private SearchService searchService;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private WebDAVHelper webDAVHelper;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private LockService lockService;
    private ContentService contentService;
    private CheckOutCheckInService checkOutCheckInService;
    private PermissionService permissionService;

    private NodeRef companyHomeNodeRef;
    private NodeRef versionableDoc;
    private String versionableDocName;
    private StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

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
        authenticationService = ctx.getBean("authenticationService", MutableAuthenticationService.class);
        personService = ctx.getBean("PersonService", PersonService.class);
        lockService = ctx.getBean("LockService", LockService.class);
        contentService = ctx.getBean("contentService", ContentService.class);
        checkOutCheckInService = ctx.getBean("CheckOutCheckInService", CheckOutCheckInService.class);
        permissionService = ctx.getBean("PermissionService", PermissionService.class);

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
        txn = transactionService.getUserTransaction();
        txn.begin();
        createUser(USER1_NAME);
        createUser(USER2_NAME);

        InputStream testDataIS = getClass().getClassLoader().getResourceAsStream(TEST_DATA_FILE_NAME);
        InputStream davLockInfoAdminIS = getClass().getClassLoader().getResourceAsStream(DAV_LOCK_INFO_ADMIN);
        InputStream davLockInfoUser2IS = getClass().getClassLoader().getResourceAsStream(DAV_LOCK_INFO_USER2);
        
        testDataFile = IOUtils.toByteArray(testDataIS);
        davLockInfoAdminFile = IOUtils.toByteArray(davLockInfoAdminIS);
        davLockInfoUser2File = IOUtils.toByteArray(davLockInfoUser2IS);

        testDataIS.close();
        davLockInfoAdminIS.close();
        davLockInfoUser2IS.close();

        // Create a test file with versionable aspect and content
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        versionableDocName = "doc-" + GUID.generate();
        properties.put(ContentModel.PROP_NAME, versionableDocName);

        versionableDoc = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, versionableDocName),
                ContentModel.TYPE_CONTENT, properties).getChildRef();
        contentService.getWriter(versionableDoc, ContentModel.PROP_CONTENT, true).putContent("WebDAVTestContent");
        nodeService.addAspect(versionableDoc, ContentModel.ASPECT_VERSIONABLE, null);

        txn.commit();

        txn = transactionService.getUserTransaction();
        txn.begin();
    }

    private void createUser(String userName)
    {
        if (!authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }

        if (!personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap();
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);
        }
    }

    @After
    public void tearDown() throws Exception
    {
        method = null;
        request = null;
        response = null;
        testDataFile = null;
        davLockInfoAdminFile = null;

        if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
        {
            txn.rollback();
        }
        else
        {
            txn.commit();
        }
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        deleteUser(USER1_NAME);
        deleteUser(USER2_NAME);

        nodeService.deleteNode(versionableDoc);

        // As per MNT-10037 try to create a node and delete it in the next txn
        txn = transactionService.getUserTransaction();
        txn.begin();

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        String nodeName = "leak-session-doc-" + GUID.generate();
        properties.put(ContentModel.PROP_NAME, nodeName);

        NodeRef nodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, nodeName),
                ContentModel.TYPE_CONTENT, properties).getChildRef();
        contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).putContent("WebDAVTestContent");

        txn.commit();

        txn = transactionService.getUserTransaction();
        txn.begin();

        nodeService.deleteNode(nodeRef);

        txn.commit();

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private void deleteUser(String userName)
    {
        // Delete the user, as admin
        if (this.personService.personExists(userName))
        {
            this.personService.deletePerson(userName);
        }
        if (this.authenticationService.authenticationExists(userName))
        {
            this.authenticationService.deleteAuthentication(userName);
        }
    }

    @Test
    public void testPutContentToNonExistingFile() throws Exception
    {
        String fileName = "file-" + GUID.generate();
        NodeRef fileNoderef = null;
        try
        {
            executeMethod(WebDAV.METHOD_PUT, fileName, testDataFile, null);

            ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home//cm:" + fileName + "\"");
            fileNoderef = resultSet.getNodeRef(0);
            resultSet.close();

            assertTrue("File does not exist.", nodeService.exists(fileNoderef));
            assertEquals("Filename is not correct", fileName, nodeService.getProperty(fileNoderef, ContentModel.PROP_NAME));
            assertTrue("Expected return status is " + HttpServletResponse.SC_CREATED + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_CREATED == response.getStatus());
            InputStream updatedFileIS = fileFolderService.getReader(fileNoderef).getContentInputStream();
            byte[] updatedFile = IOUtils.toByteArray(updatedFileIS);
            updatedFileIS.close();
            assertTrue("The content has to be equal", ArrayUtils.isEquals(testDataFile, updatedFile));
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
        finally
        {
            if (fileNoderef != null)
            {
                nodeService.deleteNode(fileNoderef);
            }
        }
    }

    @Test
    public void testPutContentToAnExistingFile() throws Exception
    {
        FileInfo testFileInfo = fileFolderService.create(companyHomeNodeRef, "file-" + GUID.generate(), ContentModel.TYPE_CONTENT);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, testFileInfo.getName(), testDataFile, null);

            assertTrue("File does not exist.", nodeService.exists(testFileInfo.getNodeRef()));
            assertEquals("Filename is not correct.", testFileInfo.getName(), nodeService.getProperty(testFileInfo.getNodeRef(), ContentModel.PROP_NAME));
            assertTrue("Expected return status is " + HttpServletResponse.SC_NO_CONTENT + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_NO_CONTENT == response.getStatus());
            InputStream updatedFileIS = fileFolderService.getReader(testFileInfo.getNodeRef()).getContentInputStream();
            byte[] updatedFile = IOUtils.toByteArray(updatedFileIS);
            updatedFileIS.close();
            assertTrue("The content has to be equal", ArrayUtils.isEquals(testDataFile, updatedFile));
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
        finally
        {
            nodeService.deleteNode(testFileInfo.getNodeRef());
        }
    }

    /**
     * Updating a file in an non-existent path
     */
    @Test
    public void testPutContentBadPath() throws Exception
    {
        String fileName = "file-" + GUID.generate();
        NodeRef fileNoderef = null;
        try
        {
            // Add non-existent path
            executeMethod(WebDAV.METHOD_PUT, "non/existent/path" + fileName, testDataFile, null);

            fail("The PUT execution should fail with a 400 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue(wse.getHttpStatusCode() == HttpServletResponse.SC_CONFLICT);
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
        finally
        {
            if (fileNoderef != null)
            {
                nodeService.deleteNode(fileNoderef);
            }
        }
    }

    /**
     * Creating a folder and trying to update it with a file
     */
    @Test
    public void testPutContentToFolder() throws Exception
    {
        FileInfo testFileInfo = fileFolderService.create(companyHomeNodeRef, "folder-" + GUID.generate(), ContentModel.TYPE_FOLDER);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, testFileInfo.getName(), testDataFile, null);

            fail("The PUT execution should fail with a 400 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue(wse.getHttpStatusCode() == HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
        finally
        {
            nodeService.deleteNode(testFileInfo.getNodeRef());
        }
    }

    /**
     * Putting a content to a locked file
     * <p>
     * Create and lock a file by admin
     * <p>
     * Try to put the content by user
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testPutContentToLockedFIle() throws Exception
    {
        FileInfo testFileInfo = fileFolderService.create(companyHomeNodeRef, "file-" + GUID.generate(), ContentModel.TYPE_CONTENT);
        lockService.lock(testFileInfo.getNodeRef(), LockType.WRITE_LOCK);
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(USER1_NAME);
            executeMethod(WebDAV.METHOD_PUT, testFileInfo.getName(), testDataFile, null);

            fail("The PUT execution should fail with a 423 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue(wse.getHttpStatusCode() == WebDAV.WEBDAV_SC_LOCKED);
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
        finally
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            nodeService.deleteNode(testFileInfo.getNodeRef());
        }
    }

    /**
     * Putting a content to a working copy file
     * <p>
     * Create and check out a file by user1
     * <p>
     * Try to put the content to the working copy by user2
     * 
     * See MNT-8614.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testPutContentToWorkingCopy() throws Exception
    {
        FileInfo folder = fileFolderService.create(companyHomeNodeRef, "folder-" + GUID.generate(), ContentModel.TYPE_FOLDER);
        permissionService.setInheritParentPermissions(folder.getNodeRef(), false);
        permissionService.setPermission(folder.getNodeRef(), USER1_NAME, permissionService.getAllPermission(), true);

        AuthenticationUtil.setFullyAuthenticatedUser(USER1_NAME);
        FileInfo testFileInfo = fileFolderService.create(folder.getNodeRef(), "file-" + GUID.generate(), ContentModel.TYPE_CONTENT);
        NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(testFileInfo.getNodeRef());
        String workingCopyName = fileFolderService.getFileInfo(workingCopyNodeRef).getName();
        String pathToWC = "/" + folder.getName() + "/" + workingCopyName;
        String pathToOriginal = "/" + folder.getName() + "/" + testFileInfo.getName();

        // Negative test, try to edit the WC without permissions.
        AuthenticationUtil.setFullyAuthenticatedUser(USER2_NAME);
        try
        {
            lockService.lock(workingCopyNodeRef, LockType.WRITE_LOCK);
        }
        catch (AccessDeniedException ade)
        {
            // expected
        }

        try
        {
            executeMethod(WebDAV.METHOD_LOCK, pathToWC, davLockInfoUser2File, null);

            fail("The LOCK execution should fail with a 401 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue("The status code was " + wse.getHttpStatusCode() + ", but should be " + HttpServletResponse.SC_UNAUTHORIZED,
                    wse.getHttpStatusCode() == HttpServletResponse.SC_UNAUTHORIZED);
        }
        catch (Exception e)
        {
            fail("Unexpected exception occurred: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        // Construct IF HEADER
        String lockToken = workingCopyNodeRef.getId() + WebDAV.LOCK_TOKEN_SEPERATOR + USER2_NAME;
        String lockHeaderValue = "(<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">)";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_IF, lockHeaderValue);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, pathToWC, testDataFile, headers);
            fail("The PUT execution should fail with a 423 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue("The status code was " + wse.getHttpStatusCode() + ", but should be " + HttpServletResponse.SC_UNAUTHORIZED,
                    wse.getHttpStatusCode() == HttpServletResponse.SC_UNAUTHORIZED);
        }
        catch (Exception e)
        {
            fail("Unexpected exception occurred: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        // Positive test
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        permissionService.setPermission(folder.getNodeRef(), USER2_NAME, permissionService.getAllPermission(), true);

        AuthenticationUtil.setFullyAuthenticatedUser(USER2_NAME);
        try
        {
            executeMethod(WebDAV.METHOD_LOCK, pathToWC, davLockInfoUser2File, null);

            assertEquals("File should be locked", LockStatus.LOCK_OWNER, lockService.getLockStatus(workingCopyNodeRef));
        }
        catch (Exception e)
        {
            fail("Failed to lock a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_IF, lockHeaderValue);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, pathToWC, testDataFile, headers);

            assertTrue("File does not exist.", nodeService.exists(workingCopyNodeRef));
            assertEquals("Filename is not correct", workingCopyName, nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_NAME));
            assertTrue("Expected return status is " + HttpServletResponse.SC_NO_CONTENT + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_NO_CONTENT == response.getStatus());

            assertTrue("File should have NO_CONTENT aspect", nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_NO_CONTENT));
            InputStream updatedFileIS = fileFolderService.getReader(workingCopyNodeRef).getContentInputStream();
            byte[] updatedFile = IOUtils.toByteArray(updatedFileIS);
            updatedFileIS.close();
            assertTrue("The content has to be equal", ArrayUtils.isEquals(testDataFile, updatedFile));
        }
        catch (Exception e)
        {
            fail("Failed to upload a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_LOCK_TOKEN, "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">");
        try
        {
            executeMethod(WebDAV.METHOD_UNLOCK, pathToWC, null, headers);

            assertTrue("Expected return status is " + HttpServletResponse.SC_NO_CONTENT + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_NO_CONTENT == response.getStatus());
            assertFalse("File should not have NO_CONTENT aspect", nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_NO_CONTENT));
            assertEquals("File should be unlocked", LockStatus.NO_LOCK, lockService.getLockStatus(workingCopyNodeRef));
        }
        catch (Exception e)
        {
            fail("Failed to unlock a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        // Negative test try to lock or edit the original file
        AuthenticationUtil.setFullyAuthenticatedUser(USER2_NAME);
        try
        {
            lockService.lock(testFileInfo.getNodeRef(), LockType.WRITE_LOCK);
        }
        catch (UnableToAquireLockException uale)
        {
            // expected
        }

        try
        {
            executeMethod(WebDAV.METHOD_LOCK, pathToOriginal, davLockInfoUser2File, null);

            fail("The LOCK execution should fail with a 423 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue("The status code was " + wse.getHttpStatusCode() + ", but should be " + WebDAV.WEBDAV_SC_LOCKED, wse.getHttpStatusCode() == WebDAV.WEBDAV_SC_LOCKED);
        }
        catch (Exception e)
        {
            fail("Unexpected exception occurred: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        // Construct IF HEADER
        lockToken = testFileInfo.getNodeRef().getId() + WebDAV.LOCK_TOKEN_SEPERATOR + USER2_NAME;
        lockHeaderValue = "(<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">)";
        headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_IF, lockHeaderValue);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, pathToOriginal, testDataFile, headers);
            fail("The PUT execution should fail with a 423 error");
        }
        catch (WebDAVServerException wse)
        {
            // The execution failed and it is expected
            assertTrue("The status code was " + wse.getHttpStatusCode() + ", but should be " + WebDAV.WEBDAV_SC_LOCKED, wse.getHttpStatusCode() == WebDAV.WEBDAV_SC_LOCKED);
        }
        catch (Exception e)
        {
            fail("Unexpected exception occurred: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        AuthenticationUtil.setFullyAuthenticatedUser(USER1_NAME);
        checkOutCheckInService.checkin(workingCopyNodeRef, null);

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        nodeService.deleteNode(folder.getNodeRef());
    }
    
    /**
     * Putting a content and check versioning
     * <p>
     * The node will be deleted in after test.
     */
    @Test
    public void testPutContentCheckVersions() throws Exception
    {
        assertEquals("The version should be 1.0 as no file modifications were done yet.", "1.0", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));

        // Lock the node
        try
        {
            executeMethod(WebDAV.METHOD_LOCK, versionableDocName, davLockInfoAdminFile, null);

            assertEquals("The version should not advance", "1.0", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));
        }
        catch (Exception e)
        {
            fail("Failed to lock a file: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        // Split to transactions to check the commit
        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();
        assertEquals("The version should not advance", "1.0", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));

        // Put non-zero content
        // Construct IF HEADER
        String lockToken = versionableDoc.getId() + WebDAV.LOCK_TOKEN_SEPERATOR + AuthenticationUtil.getAdminUserName();
        String lockHeaderValue = "(<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">)";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_IF, lockHeaderValue);
        try
        {
            executeMethod(WebDAV.METHOD_PUT, versionableDocName, testDataFile, headers);

            assertEquals("The version should not advance", "1.0", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to upload a file", e);
        }

        // Split to transactions to check the commit
        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();
        assertEquals("The version should advance", "1.1", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));

        // Unlock
        headers = new HashMap<String, String>();
        headers.put(WebDAV.HEADER_LOCK_TOKEN, "<" + WebDAV.OPAQUE_LOCK_TOKEN + lockToken + ">");
        try
        {
            executeMethod(WebDAV.METHOD_UNLOCK, versionableDocName, null, headers);

            assertEquals("The version should not advance from 1.1", "1.1", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to unlock a file", e);
        }

        // Split to transactions to check the commit
        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();
        assertEquals("The version should not advance from 1.1", "1.1", nodeService.getProperty(versionableDoc, ContentModel.PROP_VERSION_LABEL));
    }

    /**
     * Putting a zero content file and update it
     * <p>
     * Put an empty file
     * <p>
     * Lock the file
     * <p>
     * Put the contents
     * <p>
     * Unlock the node
     */
    @Test
    public void testPutNoContentFileAndUpdate() throws Exception
    {
        String fileName = "file-" + GUID.generate();
        NodeRef fileNoderef = null;
        try
        {
            executeMethod(WebDAV.METHOD_PUT, fileName, new byte[0], null);

            ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home//cm:" + fileName + "\"");
            fileNoderef = resultSet.getNodeRef(0);
            resultSet.close();

            assertTrue("File should exist.", nodeService.exists(fileNoderef));
            assertEquals("Filename is not correct", fileName, nodeService.getProperty(fileNoderef, ContentModel.PROP_NAME));

            assertTrue("Expected return status is " + HttpServletResponse.SC_CREATED + ", but returned is " + response.getStatus(),
                    HttpServletResponse.SC_CREATED == response.getStatus());
            byte[] updatedFile = IOUtils.toByteArray(fileFolderService.getReader(fileNoderef).getContentInputStream());
            assertTrue("The content should be empty", updatedFile.length == 0);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to upload a file", e);
        }

        try
        {
            executeMethod(WebDAV.METHOD_LOCK, fileName, davLockInfoAdminFile, null);

            assertEquals("File should be locked", LockStatus.LOCK_OWNER, lockService.getLockStatus(fileNoderef));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to lock a file", e);
        }

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
            throw new RuntimeException("Failed to upload a file", e);
        }

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
            throw new RuntimeException("Failed to unlock a file", e);
        }

        if (fileNoderef != null)
        {
            nodeService.deleteNode(fileNoderef);
        }
    }

    /**
     * Negative test to check that a temp file will be deleted from the repo if writing the content fails
     */
    @Test
    public void testPutNullContent() throws Exception
    {
        String fileName = "file-" + GUID.generate();
        NodeRef fileNoderef = null;

        // Lock the node
        try
        {
            executeMethod(WebDAV.METHOD_LOCK, fileName, davLockInfoAdminFile, null);
            ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home//cm:" + fileName + "\"");
            fileNoderef = resultSet.getNodeRef(0);
            resultSet.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to lock a file", e);
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
            // setting a null content
            executeMethod(WebDAV.METHOD_PUT, fileName, null, headers);

            fail("The execution should fail.");
        }
        catch (WebDAVServerException wse)
        {
            if (nodeService.exists(fileNoderef))
            {
                nodeService.deleteNode(fileNoderef);
                fail("File exist, but should not.");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to upload a file", e);
        }

        if (fileNoderef != null && nodeService.exists(fileNoderef))
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
