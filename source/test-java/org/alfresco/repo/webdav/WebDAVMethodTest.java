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
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

/**
 * Tests for the WebDAVMethod class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class WebDAVMethodTest
{
    private WebDAVMethod method;
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    private @Mock WebDAVHelper davHelper;

    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private TenantService tenantService;
    private TransactionService transactionService;
    private WebDAVHelper webDAVHelper;
    private TenantAdminService tenantAdminService;

    private @Mock LockMethod lockMethod;
    private @Mock PutMethod putMethod;
    private @Mock DeleteMethod deleteMethod;
    private @Mock UnlockMethod unlockMethod;

    public static final String TEST_RUN = System.currentTimeMillis()+"";
    public static final String TEST_TENANT_DOMAIN = TEST_RUN+".my.test";
    public static final String DEFAULT_ADMIN_PW = "admin";

    protected void setUpApplicationContext()
    {
        ApplicationContext appContext = ApplicationContextHelper.getApplicationContext(new String[]
                {
                        "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
                        "classpath:alfresco/remote-api-context.xml"
                });

        this.nodeService = (NodeService) appContext.getBean("NodeService");
        this.searchService = (SearchService) appContext.getBean("SearchService");
        this.namespaceService = (NamespaceService) appContext.getBean("NamespaceService");
        this.tenantService = (TenantService) appContext.getBean("tenantService");
        this.transactionService = (TransactionService) appContext.getBean("transactionService");
        this.webDAVHelper = (WebDAVHelper) appContext.getBean("webDAVHelper");
        this.tenantAdminService = (TenantAdminService) appContext.getBean("tenantAdminService");

        // Authenticate as system to create initial test data set
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) appContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
    }

    private void checkLockedNodeTestWork() throws WebDAVServerException
    {
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();

        String rootPath = "/app:company_home";
        String storeName = "workspace://SpacesStore";
        StoreRef storeRef = new StoreRef(storeName);
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPath, null, namespaceService, false);
        NodeRef defaultRootNode = nodeRefs.get(0);

        lockMethod = new LockMethod();
        NodeRef rootNodeRef = tenantService.getRootNode(nodeService, searchService, namespaceService, rootPath, defaultRootNode);
        String strPath = "/" + "testLockedNode" + GUID.generate();

        lockMethod.createExclusive = true;
        lockMethod.setDetails(req, resp, webDAVHelper, rootNodeRef);
        lockMethod.m_strPath = strPath;

        // Lock the node (will create a new one).
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable {
                lockMethod.executeImpl();
                return null;
            }
        });

        // Prepare for PUT
        req.addHeader(WebDAV.HEADER_IF, "(<" + lockMethod.lockToken + ">)");
        putMethod = new PutMethod();
        putMethod.setDetails(req, resp, webDAVHelper, rootNodeRef);
        putMethod.parseRequestHeaders();
        putMethod.m_strPath = strPath;
        String content = "test content stream";
        req.setContent(content.getBytes());

        // Issue a put request
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                putMethod.executeImpl();
                return null;
            }
        });
    }

    /**
     * Call the org.alfresco.repo.webdav.WebDAVMethod#checkNode(org.alfresco.service.cmr.model.FileInfo, boolean, boolean)
     * for a write locked node for tenant and non-tenant.
     * See ALF-19915.
     */
    @Test
    public void checkLockedNodeTest() throws Exception
    {
        setUpApplicationContext();

        // Create a tenant domain
        TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Object>() {
            public Object doWork() throws Exception {
                if (!tenantAdminService.existsTenant(TEST_TENANT_DOMAIN))
                {
                    tenantAdminService.createTenant(TEST_TENANT_DOMAIN, (DEFAULT_ADMIN_PW + " " + TEST_TENANT_DOMAIN).toCharArray(), null);
                }
                return null;
            }
        }, TenantService.DEFAULT_DOMAIN);

        // run as admin
        try
        {
            TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>()
            {
                @Override
                public Object doWork() throws Exception
                {
                    checkLockedNodeTestWork();
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName(), TenantService.DEFAULT_DOMAIN);
        }
        catch (Exception e)
        {
            fail("Failed to lock and put content as admin with error: " + e.getCause());
        }

        // run as tenant admin
        try
        {
            TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>()
            {
                @Override
                public Object doWork() throws Exception
                {
                    checkLockedNodeTestWork();
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName(), TEST_TENANT_DOMAIN);
        }
        catch (Exception e)
        {
            fail("Failed to lock and put content as tenant admin with error: " + e.getCause());
        }
    }
    
    private void checkLockedNodeTestTenantWork() throws WebDAVServerException
    {
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();

        String rootPath = "/app:company_home";
        String storeName = "workspace://SpacesStore";
        StoreRef storeRef = new StoreRef(storeName);
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPath, null, namespaceService, false);
        NodeRef defaultRootNode = nodeRefs.get(0);

        lockMethod = new LockMethod();
        NodeRef rootNodeRef = tenantService.getRootNode(nodeService, searchService, namespaceService, rootPath, defaultRootNode);
        String strPath = "/" + "testLockedNode" + GUID.generate();

        lockMethod.createExclusive = true;
        lockMethod.setDetails(req, resp, webDAVHelper, rootNodeRef);
        lockMethod.m_strPath = strPath;

        // Lock the node (will create a new one).
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable {
                lockMethod.executeImpl();
                return null;
            }
        });

        // Prepare for DELETE
        String corruptedLockToken = lockMethod.lockToken + "corr";
        req.addHeader(WebDAV.HEADER_IF, "(<" + corruptedLockToken + ">)");
        deleteMethod = new DeleteMethod();
        deleteMethod.setDetails(req, resp, webDAVHelper, rootNodeRef);
        deleteMethod.parseRequestHeaders();
        deleteMethod.m_strPath = strPath;

        // can't be deleted with corrupted lockTocken
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                @Override
                public Object execute() throws Throwable
                {
                    deleteMethod.executeImpl();
                    return null;
                }
            });
            fail("Locked node shouldn't be deleted");
        }
        catch(Exception e)
        {
            if (!(e.getCause() instanceof WebDAVServerException))
            {
                throw e;
            }
        }
        
        req = new MockHttpServletRequest();
        req.addHeader(WebDAV.HEADER_LOCK_TOKEN, lockMethod.lockToken);
        
        unlockMethod = new UnlockMethod();
        unlockMethod.setDetails(req, resp, webDAVHelper, rootNodeRef);
        unlockMethod.parseRequestHeaders();
        unlockMethod.m_strPath = strPath;
        // unlock the node
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                @Override
                public Object execute() throws Throwable
                {
                    unlockMethod.executeImpl();
                    return null;
                }
            });
        }
        catch(Exception e)
        {
            fail("Locked node should be unlocked with correct lockTocken " + e.getCause());
        }
        
        // Lock it again
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable {
                lockMethod.executeImpl();
                return null;
            }
        });
        
        req.addHeader(WebDAV.HEADER_IF, "(<" + lockMethod.lockToken + ">)");
        deleteMethod = new DeleteMethod();
        deleteMethod.setDetails(req, resp, webDAVHelper, rootNodeRef);
        deleteMethod.parseRequestHeaders();
        deleteMethod.m_strPath = strPath;

        // can be deleted with correct lockTocken
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                @Override
                public Object execute() throws Throwable
                {
                    deleteMethod.executeImpl();
                    return null;
                }
            });
        }
        catch(Exception e)
        {
            fail("Locked node should be deleted with correct lockTocken " + e.getCause());
        }
        
    }
    
    /* CLOUD-2204 Test */
    @Test
    public void checkLockedNodeTenantTest()
    {
        setUpApplicationContext();

        // Create a tenant domain
        TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Object>() {
            public Object doWork() throws Exception {
                if (!tenantAdminService.existsTenant(TEST_TENANT_DOMAIN))
                {
                    tenantAdminService.createTenant(TEST_TENANT_DOMAIN, (DEFAULT_ADMIN_PW + " " + TEST_TENANT_DOMAIN).toCharArray(), null);
                }
                return null;
            }
        }, TenantService.DEFAULT_DOMAIN);
        
        TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                checkLockedNodeTestTenantWork();
                return null;
            }
        }, AuthenticationUtil.getAdminUserName(), TEST_TENANT_DOMAIN);
    }

    @Test
    public void canGetStatusForAccessDeniedException()
    {
        // Initially Mac OS X Finder uses a different UA string than for subsequent requests.
        assertStatusCode(500, "WebDAVLib/1.3");
        
        // Current UA string at time of writing test.
        assertStatusCode(500, "WebDAVFS/1.9.0 (01908000) Darwin/11.4.0 (x86_64)");
        
        // A fictitious version number long in the future.
        assertStatusCode(500, "WebDAVFS/100.10.5 (01908000) Darwin/11.4.0 (x86_64)");

        // Other processor architectures, e.g. x86_32 should work too.
        assertStatusCode(500, "WebDAVFS/100.10.5 (01908000) Darwin/109.6.3 (some_other_processor_arch)");
        
        // Other clients should give 403.
        assertStatusCode(403, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6; en-us)");
        // Mozilla-based Windows browser.
        assertStatusCode(403, "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.12)");
        assertStatusCode(403, "SomeBrowser/1.0 (Macintosh; U; Intel Mac OS X 10_6; en-us)");
        assertStatusCode(403, "SomeBrowser/1.9.0 (01908000) Darwin/11.4.0 (x86_64)");
        assertStatusCode(403, "Cyberduck/4.2.1 (Mac OS X/10.7.4) (i386)");
        // Chrome
        assertStatusCode(403, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.54 Safari/536.5");
        // Safari
        assertStatusCode(403, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_4) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2");
    }
    
    private void assertStatusCode(int expectedStatusCode, String userAgent)
    {
        // Fresh objects needed for each status code test.
        createRequestObjects();
        req.addHeader("User-Agent", userAgent);
        method.setDetails(req, resp, davHelper, null);
        
        int statusCode = method.getStatusForAccessDeniedException();
        
        assertEquals("Incorrect status code for user-agent string \"" + userAgent + "\"",
                    expectedStatusCode,
                    statusCode);
    }

    private void createRequestObjects()
    {
        method = new TestWebDAVMethod();
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
    }

    
    /**
     * Empty subclass of abstract base class for testing base class' behaviour.
     */
    private static class TestWebDAVMethod extends WebDAVMethod
    {
        @Override
        protected void executeImpl() throws WebDAVServerException, Exception
        {
        }

        @Override
        protected void parseRequestBody() throws WebDAVServerException
        {
        }

        @Override
        protected void parseRequestHeaders() throws WebDAVServerException
        {
        }   
    }
}
