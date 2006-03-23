/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class SearchServiceTest extends TestCase
{

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;

    private AuthenticationService authenticationService;

    private MutableAuthenticationDao authenticationDAO;

    private UserTransaction tx;

    private SearchService pubSearchService;

    private NodeRef rootNodeRef;

    private NodeRef n1;

    private NodeRef n2;

    private NodeRef n3;

    private NodeRef n4;

    private NodeRef n6;

    private NodeRef n5;

    private NodeRef n7;

    private NodeRef n8;

    private NodeRef n9;

    private NodeRef n10;

    private NodeService nodeService;

    private PermissionService pubPermissionService;

    public SearchServiceTest()
    {
        super();
    }

    public void setUp() throws Exception
    {
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponentImpl");
        authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("alfDaoImpl");
        pubSearchService = (SearchService) ctx.getBean("SearchService");
        pubPermissionService = (PermissionService) ctx.getBean("PermissionService");

        this.authenticationComponent.setSystemUserAsCurrentUser();

        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE
                .getLocalName());
        tx = transactionService.getUserTransaction();
        tx.begin();

        if (!authenticationDAO.userExists("andy"))
        {
            authenticationService.createAuthentication("andy", "andy".toCharArray());
        }

        if (!authenticationDAO.userExists("admin"))
        {
            authenticationService.createAuthentication("admin", "admin".toCharArray());
        }

        if (!authenticationDAO.userExists("administrator"))
        {
            authenticationService.createAuthentication("administrator", "administrator".toCharArray());
        }

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}01"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        pubPermissionService.setPermission(n1, "andy", "Read", true);
        n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}02"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        pubPermissionService.setPermission(n2, "andy", "Read", true);
        n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}03"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        pubPermissionService.setPermission(n3, "andy", "Read", true);
        n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}04"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        pubPermissionService.setPermission(n4, "andy", "Read", true);
        n5 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}05"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        pubPermissionService.setPermission(n5, "andy", "Read", true);
        n6 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}06"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        n7 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}07"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        n8 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}08"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        n9 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}09"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        n10 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}10"),
                ContentModel.TYPE_CONTAINER).getChildRef();

    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        tx.rollback();
        super.tearDown();
    }

    public void testAdmim()
    {
        authenticationComponent.setCurrentUser("admin");
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//*\"");
        sp.addStore(rootNodeRef.getStoreRef());
        ResultSet results = pubSearchService.query(sp);
        assertEquals(results.length(), 10);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.UNLIMITED);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();

        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(20);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 10);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.UNLIMITED);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();

        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(10);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 10);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.UNLIMITED);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();

        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(9);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 9);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.FINAL_SIZE);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();
        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(5);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 5);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.FINAL_SIZE);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();
    }

    public void testAndy()
    {
        authenticationComponent.setCurrentUser("andy");
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//*\"");
        sp.addStore(rootNodeRef.getStoreRef());
        ResultSet results = pubSearchService.query(sp);
        assertEquals(results.length(), 5);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.UNLIMITED);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();

        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(20);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 5);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.UNLIMITED);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();

        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(5);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 5);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.UNLIMITED);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();

        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(4);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 4);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.FINAL_SIZE);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();
        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(2);
        results = pubSearchService.query(sp);
        assertEquals(results.length(), 2);
        assertNotNull(results.getResultSetMetaData());
        assertEquals(results.getResultSetMetaData().getLimitedBy(), LimitBy.FINAL_SIZE);
        assertEquals(results.getResultSetMetaData().getPermissionEvaluationMode(), PermissionEvaluationMode.EAGER);
        results.close();
    }
}
