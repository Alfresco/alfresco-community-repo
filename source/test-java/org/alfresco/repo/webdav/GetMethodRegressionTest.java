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

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests {@link GetMethod} in real environment, using {@link Mock} HTTP request and {@link Mock} HTTP response
 * 
 * @author Dmitry Velichkevich
 */
@RunWith(MockitoJUnitRunner.class)
public class GetMethodRegressionTest extends TestCase
{
    private static final int DOCUMENTS_AMOUNT_FOR_GET_METHOD_TEST = 25;


    private static final String HTTP_METHOD_GET = "GET";

    private static final String TEST_ENCODING = "UTF-8";

    private static final String TEST_MIMETYPE = "text/plain";

    private static final String TEST_WEBDAV_URL_PREFIX = "/";

    private static final String AUDIT_REGISTRY_BEAN_NAME = "Audit";

    private static final String PROP_AUDIT_ALFRESCO_ACCESS_ENABLED = "audit.alfresco-access.enabled";

    private static final String ROOT_TEST_FOLDER_NAME = "TestFolder-" + System.currentTimeMillis();

    private static final String TEST_DOCUMENT_NAME_PATTERN = "TestDocument-%d-%d.pdf";

    private static final String TEXT_DOCUMENT_CONTENT_PATTERN = "Text content for '%s' document";


    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    private WebDAVHelper davHelper;

    private AuditService auditService;

    private FileFolderService fileFolderService;

    private AuditModelRegistryImpl auditRegistry;

    private TransactionService transactionService;


    private UserTransaction transaction;

    private GetMethod testingMethod;

    private NodeRef companyHomeNodeRef;

    private NodeRef rootTestFolder;

    private MockHttpServletResponse mockResponse;


    @Before
    public void setUp() throws Exception
    {
        ServiceRegistry registry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        davHelper = (WebDAVHelper) applicationContext.getBean(WebDAVHelper.BEAN_NAME);
        auditRegistry = (AuditModelRegistryImpl) applicationContext.getBean(AUDIT_REGISTRY_BEAN_NAME);

        auditService = registry.getAuditService();
        fileFolderService = registry.getFileFolderService();
        transactionService = registry.getTransactionService();

        testingMethod = new GetMethod();
        mockResponse = new MockHttpServletResponse();

        restartTransaction(TransactionActionEnum.ACTION_NONE);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHomeNodeRef = registry.getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
        rootTestFolder = fileFolderService.create(companyHomeNodeRef, ROOT_TEST_FOLDER_NAME, ContentModel.TYPE_FOLDER).getNodeRef();
    }

    @After
    public void tearDown() throws Exception
    {
        if ((null != transaction) && (Status.STATUS_ROLLEDBACK != transaction.getStatus()) && (Status.STATUS_COMMITTED != transaction.getStatus()))
        {
            transaction.rollback();
        }

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Test for <a href="https://issues.alfresco.com/jira/browse/MNT-10820">MNT-10820</a>
     * 
     * @throws Exception
     */
    @Test
    public void testAuditRecordsAdditionAsbsence() throws Exception
    {
        String url = new StringBuilder(TEST_WEBDAV_URL_PREFIX).append(ROOT_TEST_FOLDER_NAME).toString();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HTTP_METHOD_GET, url);
        testingMethod.setDetails(mockRequest, mockResponse, davHelper, companyHomeNodeRef);

        boolean auditEnabled = auditService.isAuditEnabled();

        if (!auditEnabled)
        {
            auditService.setAuditEnabled(true);
        }

        boolean alfrescoAccessEnabled = Boolean.valueOf(auditRegistry.getProperty(PROP_AUDIT_ALFRESCO_ACCESS_ENABLED));

        if (!alfrescoAccessEnabled)
        {
            setAuditRegistryProperty(PROP_AUDIT_ALFRESCO_ACCESS_ENABLED, Boolean.TRUE.toString());
        }

        try
        {
            createTestContent(rootTestFolder, DOCUMENTS_AMOUNT_FOR_GET_METHOD_TEST);

            restartTransaction(TransactionActionEnum.ACTION_COMMIT);

            Long expectedLastTime = getLastAuditRecordTime();

            testingMethod.executeImpl();
            assertEquals(HttpURLConnection.HTTP_OK, mockResponse.getStatus());

            String contentAsString = mockResponse.getContentAsString();
            assertNotNull("WebDAV 'GET' method response is empty!", contentAsString);
            assertTrue(contentAsString.contains(ROOT_TEST_FOLDER_NAME));

            restartTransaction(TransactionActionEnum.ACTION_COMMIT);

            Long actualLastTime = getLastAuditRecordTime();

            if (null == expectedLastTime)
            {
                assertNull("Audit entry table is not empty after 'GetMethod.executeImpl()' invocation. But it is expected to be empty!", actualLastTime);
            }
            else
            {
                assertEquals(expectedLastTime, actualLastTime);
            }
        }
        finally
        {
            if (!alfrescoAccessEnabled)
            {
                setAuditRegistryProperty(PROP_AUDIT_ALFRESCO_ACCESS_ENABLED, Boolean.FALSE.toString());
            }

            if (!auditEnabled)
            {
                auditService.setAuditEnabled(false);
            }

            fileFolderService.delete(rootTestFolder);

            NodeArchiveService archiveService = (NodeArchiveService) applicationContext.getBean("nodeArchiveService");
            archiveService.purgeAllArchivedNodes(rootTestFolder.getStoreRef());
        }
    }

    private void setAuditRegistryProperty(String propertyName, String value)
    {
        assertTrue(("'" + propertyName + "' is not updatable!"), auditRegistry.isUpdateable(propertyName));

        auditRegistry.stop();
        auditRegistry.setProperty(propertyName, value);
        auditRegistry.start();
    }

    private void createTestContent(NodeRef parentNode, int documentsAmount)
    {
        for (int i = 0; i < documentsAmount; i++)
        {
            String testDocumentName = String.format(TEST_DOCUMENT_NAME_PATTERN, i, System.currentTimeMillis());
            FileInfo testDocument = fileFolderService.create(parentNode, testDocumentName, ContentModel.TYPE_CONTENT);
            ContentWriter writer = fileFolderService.getWriter(testDocument.getNodeRef());
            writer.putContent(String.format(TEXT_DOCUMENT_CONTENT_PATTERN, testDocumentName));
            writer.setMimetype(TEST_MIMETYPE);
            writer.setEncoding(TEST_ENCODING);
        }
    }

    private enum TransactionActionEnum
    {
        ACTION_NONE,

        ACTION_COMMIT,

        ACTION_ROLLBACK
    }

    /**
     * Commits or rolls back or does nothing with the current transaction and begins a new {@link UserTransaction}
     * 
     * @param transactionAction - one of the {@link TransactionActionEnum} values which specifies action to be done for the current transaction
     * @throws Exception
     */
    private void restartTransaction(TransactionActionEnum transactionAction) throws Exception
    {
        if ((null != transaction) && (Status.STATUS_ROLLEDBACK != transaction.getStatus()) && (Status.STATUS_COMMITTED != transaction.getStatus()))
        {
            if (TransactionActionEnum.ACTION_COMMIT == transactionAction)
            {
                transaction.commit();
            }
            else if (TransactionActionEnum.ACTION_ROLLBACK == transactionAction)
            {
                transaction.rollback();
            }
        }

        transaction = transactionService.getUserTransaction();
        transaction.begin();
    }

    private Long getLastAuditRecordTime()
    {
        final Holder<Long> lastTime = new Holder<Long>();

        AuditQueryParameters parameters = new AuditQueryParameters();
        parameters.setForward(false);

        auditService.auditQuery(new AuditQueryCallback()
        {
            @Override
            public boolean valuesRequired()
            {
                return false;
            }

            @Override
            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                return false;
            }

            @Override
            public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values)
            {
                lastTime.setValue(time);
                return false;
            }
        }, parameters, 1);

        return lastTime.getValue();
    }
}
