/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import junit.framework.TestCase;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Category({OwnJVMTestsCategory.class, DBTests.class})
public class ACS1907Test extends TestCase
{

    private ApplicationContext ctx;

    private NodeService nodeService;
    private AuthenticationComponent authenticationComponent;
    private MutableAuthenticationService authenticationService;
    private MutableAuthenticationDao authenticationDAO;
    private SearchService pubSearchService;
    private PermissionService pubPermissionService;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private DBQueryEngine queryEngine;

    private TransactionalCache<Serializable, AccessControlList> aclCache;
    private TransactionalCache<Serializable, Object> aclEntityCache;
    private TransactionalCache<Serializable, Object> permissionEntityCache;

    private NodeRef rootNodeRef;

    @Override
    public void setUp() throws Exception
    {
        setupServices();
        this.authenticationComponent.setSystemUserAsCurrentUser();
        rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        setupTestUsers();
        setupTestContent();
        dropCaches();
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
    }

    private void setupServices()
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        pubSearchService = (SearchService) ctx.getBean("SearchService");
        pubPermissionService = (PermissionService) ctx.getBean("PermissionService");
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        aclCache = (TransactionalCache) ctx.getBean("aclCache");
        aclEntityCache = (TransactionalCache) ctx.getBean("aclEntityCache");
        permissionEntityCache = (TransactionalCache) ctx.getBean("permissionEntityCache");
        SwitchableApplicationContextFactory searchContextFactory = (SwitchableApplicationContextFactory) ctx.getBean("Search");
        ApplicationContext searchCtx = searchContextFactory.getApplicationContext();
        queryEngine = (DBQueryEngine) searchCtx.getBean("search.dbQueryEngineImpl");
        txnHelper = new RetryingTransactionHelper();
        txnHelper.setTransactionService(transactionService);
        txnHelper.setReadOnly(false);
        txnHelper.setMaxRetries(1);
        txnHelper.setMinRetryWaitMs(1);
        txnHelper.setMaxRetryWaitMs(10);
        txnHelper.setRetryWaitIncrementMs(1);
    }

    private void setupTestUser(String userName)
    {
        if (!authenticationDAO.userExists(userName))
        {
            authenticationService.createAuthentication(userName, userName.toCharArray());
        }
    }

    private void setupTestUsers()
    {
        txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                setupTestUser("userA");
                setupTestUser("userB");
                setupTestUser(AuthenticationUtil.getAdminUserName());
                return null;
            }
        }, false, false);
    }

    private void setupTestContent()
    {
        for(int f = 0; f < 5; f++)
        {
            final int ff = f;
            txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                @Override
                public Object execute() throws Throwable {
                    Map<QName, Serializable> testFolderProps = new HashMap<>();
                    testFolderProps.put(ContentModel.PROP_NAME, "folder"+ff);
                    NodeRef testFolder = nodeService.createNode(
                            rootNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            QName.createQName("https://example.com/test", "folder"+ff),
                            ContentModel.TYPE_FOLDER,
                            testFolderProps
                    ).getChildRef();
                    for(int c = 0; c < 5; c++)
                    {
                        Map<QName, Serializable> testContentProps = new HashMap<>();
                        testContentProps.put(ContentModel.PROP_NAME, "content"+c);
                        NodeRef testContent = nodeService.createNode(
                                testFolder,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName("https://example.com/test", "content"+c),
                                ContentModel.TYPE_CONTENT,
                                testContentProps
                        ).getChildRef();
                        String user = c % 2 == 0 ? "userA" : "userB";
                        pubPermissionService.setPermission(testContent, user, "Read", true);
                    }
                    return null;
                }
            }, false, false);
        }
    }

    private void dropCaches()
    {
        aclCache.clear();
        aclEntityCache.clear();
        permissionEntityCache.clear();
    }

    public void testACS1907()
    {
        txnHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                    @Override
                    public Object doWork() throws Exception {
                        SearchParameters sp = new SearchParameters();
                        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                        sp.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
                        sp.setQuery("TYPE:\"cm:content\"");
                        ResultSet rs = pubSearchService.query(sp);
                        int cnt = 0;
                        for (ResultSetRow row : rs)
                        {
                            assertNotNull(row.getValue(ContentModel.PROP_NAME));
                            cnt++;
                        }
                        return null;
                    }
                }, "userA");
                return null;
            }
        }, false, false);
    }

    public void testPaging()
    {
        HashSet<NodeRef> resultPageSize2 = queryNodes(2);
        HashSet<NodeRef> resultPageSize5 = queryNodes(5);
        HashSet<NodeRef> resultPageSize10 = queryNodes(10);
        HashSet<NodeRef> resultPageSizeAll = queryNodes(10000);
        // all result sets must be equal, independent of page size used to retrieve them
        assertTrue(resultPageSize2.size() >= 25);
        assertTrue(resultPageSize5.size() >= 25);
        assertTrue(resultPageSize10.size() >= 25);
        assertTrue(resultPageSizeAll.size() >= 25);
        assertTrue(resultPageSize2.containsAll(resultPageSize5));
        assertTrue(resultPageSize2.containsAll(resultPageSize10));
        assertTrue(resultPageSize2.containsAll(resultPageSizeAll));
        assertTrue(resultPageSize5.containsAll(resultPageSize2));
        assertTrue(resultPageSize5.containsAll(resultPageSize10));
        assertTrue(resultPageSize5.containsAll(resultPageSizeAll));
        assertTrue(resultPageSize10.containsAll(resultPageSize2));
        assertTrue(resultPageSize10.containsAll(resultPageSize5));
        assertTrue(resultPageSize10.containsAll(resultPageSizeAll));
        assertTrue(resultPageSizeAll.containsAll(resultPageSize2));
        assertTrue(resultPageSizeAll.containsAll(resultPageSize5));
        assertTrue(resultPageSizeAll.containsAll(resultPageSize10));
        // reset
        queryEngine.setMinPagingBatchSize(2500);
        queryEngine.setMaxPagingBatchSize(10000);
    }

    HashSet<NodeRef> queryNodes(int pageSize)
    {
        queryEngine.setMinPagingBatchSize(pageSize);
        queryEngine.setMaxPagingBatchSize(pageSize);
        HashSet<NodeRef> result = new HashSet<>();
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
        sp.setQuery("TYPE:\"cm:content\"");
        ResultSet rs = pubSearchService.query(sp);
        int cnt = 0;
        for (ResultSetRow row : rs)
        {
            result.add(row.getNodeRef());
        }
        return result;
    }

}
