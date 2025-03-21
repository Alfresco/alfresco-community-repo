/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;

@Category({OwnJVMTestsCategory.class, DBTests.class})
public class ACS9167Test
{
    private NodeService nodeService;
    private AuthenticationComponent authenticationComponent;
    private SearchService pubSearchService;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;

    private TransactionalCache<Serializable, AccessControlList> aclCache;
    private TransactionalCache<Serializable, Object> aclEntityCache;
    private TransactionalCache<Serializable, Object> permissionEntityCache;
    private TransactionalCache<Serializable, Object> nodeOwnerCache;

    @Before
    public void setUp() throws Exception
    {
        setupServices();
        txnHelper = new RetryingTransactionHelper();
        txnHelper.setTransactionService(transactionService);
        txnHelper.setReadOnly(false);
        txnHelper.setMaxRetries(1);
        authenticationComponent.setSystemUserAsCurrentUser();
        dropCaches();
    }

    private void setupServices()
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        pubSearchService = (SearchService) ctx.getBean("SearchService");
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        aclCache = (TransactionalCache) ctx.getBean("aclCache");
        aclEntityCache = (TransactionalCache) ctx.getBean("aclEntityCache");
        permissionEntityCache = (TransactionalCache) ctx.getBean("permissionEntityCache");
        nodeOwnerCache = (TransactionalCache) ctx.getBean("nodeOwnerCache");
    }

    private void dropCaches()
    {
        aclCache.clear();
        aclEntityCache.clear();
        permissionEntityCache.clear();
        nodeOwnerCache.clear();
    }

    @After
    public void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
    }

    @Test
    public void testPagination()
    {
        String searchMarker = UUID.randomUUID().toString();
        int contentFilesCount = 185;
        createFolderWithContentNodes(searchMarker, contentFilesCount);

        prepareParametersQueryAndAssertResult(searchMarker, 0, 50, 50, contentFilesCount);
        prepareParametersQueryAndAssertResult(searchMarker, 50, 50, 50, contentFilesCount);
        prepareParametersQueryAndAssertResult(searchMarker, 150, 50, 35, contentFilesCount);
        prepareParametersQueryAndAssertResult(searchMarker, 200, 50, 0, contentFilesCount);

        prepareParametersQueryAndAssertResult(searchMarker, 0, 100, 100, contentFilesCount);
        prepareParametersQueryAndAssertResult(searchMarker, 100, 100, 85, contentFilesCount);
        prepareParametersQueryAndAssertResult(searchMarker, 200, 100, 0, contentFilesCount);

        prepareParametersQueryAndAssertResult(searchMarker, 0, 200, contentFilesCount, contentFilesCount);
    }

    @Test
    public void testLargeFilesCount()
    {
        String searchMarker = UUID.randomUUID().toString();
        int contentFilesCount = 10_000;
        createFolderWithContentNodes(searchMarker, contentFilesCount);

        prepareParametersQueryAndAssertResult(searchMarker, 0, Integer.MAX_VALUE, contentFilesCount, contentFilesCount);
    }

    private void createFolderWithContentNodes(String searchMarker, int contentFilesCount)
    {
        NodeRef testFolder = txnHelper.doInTransaction(this::createFolderNode, false, false);
        int batchSize = 1000;
        int fullBatches = contentFilesCount / batchSize;
        int remainingItems = contentFilesCount % batchSize;

        for (int i = 0; i < fullBatches; i++)
        {
            txnHelper.doInTransaction(() -> {
                for (int j = 0; j < batchSize; j++)
                {
                    createContentNode(searchMarker, testFolder);
                }
                return null;
            }, false, false);
        }

        if (remainingItems > 0)
        {
            txnHelper.doInTransaction(() -> {
                for (int j = 0; j < remainingItems; j++)
                {
                    createContentNode(searchMarker, testFolder);
                }
                return null;
            }, false, false);
        }
    }

    private void prepareParametersQueryAndAssertResult(String searchMarker, int parameterSkipCount, int parameterMaxItems, int expectedLength, int expectedNumberFound)
    {
        txnHelper.doInTransaction(() -> {
            // given
            SearchParameters sp = new SearchParameters();
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
            sp.setQuery("(+TYPE:'cm:content') and !ASPECT:'cm:checkedOut' and !TYPE:'fm:forum' and !TYPE:'fm:topic' and !TYPE:'cm:systemfolder' and !TYPE:'fm:post' and !TYPE:'fm:forums' and =cm:description:'" + searchMarker + "'");
            sp.setSkipCount(parameterSkipCount);
            sp.setMaxItems(parameterMaxItems);
            sp.setMaxPermissionChecks(Integer.MAX_VALUE);
            sp.setMaxPermissionCheckTimeMillis(Duration.ofMinutes(2).toMillis());
            // when
            ResultSet resultSet = pubSearchService.query(sp);
            // then
            assertEquals(expectedLength, resultSet.length());
            assertEquals(expectedNumberFound, resultSet.getNumberFound());
            return null;
        }, false, false);
    }

    private NodeRef createFolderNode()
    {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        Map<QName, Serializable> testFolderProps = new HashMap<>();
        String folderName = "folder" + UUID.randomUUID();
        testFolderProps.put(ContentModel.PROP_NAME, folderName);
        return nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName),
                ContentModel.TYPE_FOLDER,
                testFolderProps).getChildRef();
    }

    private void createContentNode(String searchMarker, NodeRef testFolder)
    {
        Map<QName, Serializable> testContentProps = new HashMap<>();
        String fileName = "content" + UUID.randomUUID();
        testContentProps.put(ContentModel.PROP_NAME, fileName);
        testContentProps.put(ContentModel.PROP_DESCRIPTION, searchMarker);
        nodeService.createNode(
                testFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                ContentModel.TYPE_CONTENT,
                testContentProps);
    }
}
