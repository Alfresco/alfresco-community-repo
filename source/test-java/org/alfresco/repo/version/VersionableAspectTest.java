/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Dmitry Velichkevich
 */
public class VersionableAspectTest extends TestCase
{
    private static final String NAME_AND_EXT_DELIMETER = ".";

    private static final String NAME_AND_EXT_DELIMETER_REGEXP = "\\" + NAME_AND_EXT_DELIMETER;


    private static final String ADMIN_CREDENTIAL = "admin";

    private static final String ROOT_NODE_TERM = "PATH:\"/app\\:company_home\"";

    private static final String DOCUMENT_NAME = "ChildDocumentWithVersionLabel-.txt";

    private static final String PARENT_FOLDER_NAME = "ParentFolder-" + System.currentTimeMillis();

    private static final String TEST_CONTENT_01 = "Test Content version 0.1\n";
    private static final String TEST_CONTENT_10 = "Test Content version 1.0\n";


    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService = (NodeService) applicationContext.getBean("nodeService");
    private LockService lockService = (LockService) applicationContext.getBean("lockService");
    private SearchService searchService = (SearchService) applicationContext.getBean("searchService");
    private ContentService contentService = (ContentService) applicationContext.getBean("contentService");
    private TransactionService transactionService = (TransactionService) applicationContext.getBean("transactionService");
    private CheckOutCheckInService checkOutCheckInService = (CheckOutCheckInService) applicationContext.getBean("checkOutCheckInService");
    private AuthenticationService authenticationService = (AuthenticationService) applicationContext.getBean("authenticationService");

    private NodeRef document;
    private NodeRef parentFolder;

    @Override
    protected void setUp() throws Exception
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                authenticationService.authenticate(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL.toCharArray());

                ResultSet query = null;
                NodeRef rootNode = null;
                try
                {
                    query = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, ROOT_NODE_TERM);
                    rootNode = query.getNodeRef(0);
                }
                finally
                {
                    if (null != query)
                    {
                        query.close();
                    }
                }

                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, PARENT_FOLDER_NAME);
                parentFolder = nodeService.createNode(rootNode, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, PARENT_FOLDER_NAME),
                        ContentModel.TYPE_FOLDER, properties).getChildRef();

                properties.clear();
                properties.put(ContentModel.PROP_NAME, DOCUMENT_NAME);

                document = nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, DOCUMENT_NAME),
                        ContentModel.TYPE_CONTENT, properties).getChildRef();
                contentService.getWriter(document, ContentModel.PROP_CONTENT, true).putContent(TEST_CONTENT_01);

                if (!nodeService.hasAspect(document, ContentModel.ASPECT_VERSIONABLE))
                {
                    Map<QName, Serializable> versionProperties = new HashMap<QName, Serializable>();
                    versionProperties.put(ContentModel.PROP_VERSION_LABEL, "0.1");
                    versionProperties.put(ContentModel.PROP_INITIAL_VERSION, true);
                    versionProperties.put(ContentModel.PROP_VERSION_TYPE, VersionType.MINOR);
                    nodeService.addAspect(document, ContentModel.ASPECT_VERSIONABLE, versionProperties);
                }

                return null;
            }
        });
    }

    @Override
    protected void tearDown() throws Exception
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                if (null != parentFolder)
                {
                    nodeService.deleteNode(parentFolder);
                }

                authenticationService.clearCurrentSecurityContext();

                return null;
            }
        });
    }

    public void testAutoVersionIncrementOnPropertiesUpdateAfterCheckInAlf14584() throws Exception
    {
        final String name02 = generateDocumentName(DOCUMENT_NAME, "0.2");
        final String name11 = generateDocumentName(DOCUMENT_NAME, "1.1");

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> properties = getAndAssertProperties(document, "0.1");

                Serializable autoVersionProps = properties.get(ContentModel.PROP_AUTO_VERSION_PROPS);
                assertNotNull(("Autoversion property is NULL! NodeRef = '" + document.toString() + "'"), autoVersionProps);
                assertTrue(("Autoversion must be TRUE! NodeRef = '" + document.toString() + "'"), (Boolean) autoVersionProps);

                nodeService.setProperty(document, ContentModel.PROP_NAME, name02);

                return null;
            }
        });

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> properties = getAndAssertProperties(document, "0.2");
                assertEquals(name02, properties.get(ContentModel.PROP_NAME));

                NodeRef workingCopy = checkOutCheckInService.checkout(document);
                contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true).putContent(TEST_CONTENT_10);

                Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
                document = checkOutCheckInService.checkin(workingCopy, versionProperties);

                return null;
            }
        });

        assertDocumentVersionAndName("1.0", name02);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.setProperty(document, ContentModel.PROP_NAME, name11);

                return null;
            }
        });

        assertDocumentVersionAndName("1.1", name11);
    }

    public void testAutoVersionIncrementOnPropertiesUpdateByLockOwnerAlf14584() throws Exception
    {
        final String name = generateDocumentName(DOCUMENT_NAME, "0.2");

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> properties = getAndAssertProperties(document, "0.1");

                Serializable autoVersionProps = properties.get(ContentModel.PROP_AUTO_VERSION_PROPS);
                assertNotNull(("Autoversion property is NULL! NodeRef = '" + document.toString() + "'"), autoVersionProps);
                assertTrue(("Autoversion must be TRUE! NodeRef = '" + document.toString() + "'"), (Boolean) autoVersionProps);

                lockService.lock(document, LockType.WRITE_LOCK);

                LockStatus lockStatus = lockService.getLockStatus(document);
                assertFalse(
                        ("Node with NodeRef = '" + document.toString() + "' must not be locked for " + AuthenticationUtil.getFullyAuthenticatedUser() + " user! The user is lock owner"),
                        isLocked(document));
                assertEquals(LockStatus.LOCK_OWNER, lockService.getLockStatus(document));

                nodeService.setProperty(document, ContentModel.PROP_NAME, name);

                return null;
            }
        });

        assertDocumentVersionAndName("0.2", name);
    }
    
    // Copy of code from VersionableAspect which really should be in LockService
    private boolean isLocked(NodeRef nodeRef)
    {
        LockStatus lockStatus = lockService.getLockStatus(nodeRef);

        return (LockStatus.NO_LOCK != lockStatus) && (LockStatus.LOCK_OWNER != lockStatus);
    }

    private void assertDocumentVersionAndName(final String versionLabel, final String name)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> properties = getAndAssertProperties(document, versionLabel);
                assertEquals(name, properties.get(ContentModel.PROP_NAME));

                return null;
            }
        }, true);
    }

    private Map<QName, Serializable> getAndAssertProperties(NodeRef nodeRef, String versionLabel)
    {
        assertNotNull("NodeRef of document is NULL!", nodeRef);

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        assertNotNull(("Properties must not be NULL! NodeRef = '" + nodeRef.toString() + "'"), properties);
        assertFalse(("Version specific properties can't be found! NodeRef = '" + nodeRef.toString() + "'"), properties.isEmpty());
        assertEquals(versionLabel, properties.get(ContentModel.PROP_VERSION_LABEL));

        return properties;
    }

    private String generateDocumentName(String namePattern, String versionLabel)
    {
        int i = 0;
        String[] nameAndExt = namePattern.split(NAME_AND_EXT_DELIMETER_REGEXP);
        StringBuilder result = new StringBuilder(nameAndExt[i++]).append(versionLabel).append(NAME_AND_EXT_DELIMETER).append(nameAndExt[i++]);
        return result.toString();
    }
}
