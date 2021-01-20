/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.node;

import java.io.InputStream;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.DBTests;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

@Category({OwnJVMTestsCategory.class, DBTests.class})
public class ConcurrentNodeServiceSearchTest extends TestCase
{
    
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";

    public static final String TEST_PREFIX = "test";

    public static final QName TYPE_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");

    public static final QName ASPECT_QNAME_TEST_TITLED = QName.createQName(NAMESPACE, "titled");

    public static final QName PROP_QNAME_TEST_TITLE = QName.createQName(NAMESPACE, "title");

    public static final QName PROP_QNAME_TEST_MIMETYPE = QName.createQName(NAMESPACE, "mimetype");

    ApplicationContext ctx;

    private NodeService nodeService;

    private TransactionService transactionService;

    private NodeRef rootNodeRef;

    private AuthenticationComponent authenticationComponent;

    public ConcurrentNodeServiceSearchTest()
    {
        super();
    }

    protected void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        DictionaryDAO dictionaryDao = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/systemModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);

        nodeService = (NodeService) ctx.getBean("dbNodeService");
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        this.authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");

        this.authenticationComponent.setSystemUserAsCurrentUser();

        // create a first store directly
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        tx.commit();
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }

    protected Map<QName, ChildAssociationRef> buildNodeGraph() throws Exception
    {
        return BaseNodeServiceTest.buildNodeGraph(nodeService, rootNodeRef);
    }

    protected Map<QName, ChildAssociationRef> commitNodeGraph() throws Exception
    {
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        Map<QName, ChildAssociationRef> answer = buildNodeGraph();
        tx.commit();

        return null;// answer;
    }

    public void testConcurrent() throws Exception
    {
        int count = 10;
        int repeats = 10;

        SearchService searcher = (SearchService) ctx.getBean(ServiceRegistry.SEARCH_SERVICE.getLocalName());

        Map<QName, ChildAssociationRef> assocRefs = commitNodeGraph();
        Thread runner = null;

        for (int i = 0; i < count; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, repeats, searcher);
        }
        if (runner != null)
        {
            runner.start();

            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        assertEquals(2, searcher.selectNodes(rootNodeRef, "/*", null,
                getNamespacePrefixReolsver(""), false).size());
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"");
        // n6 has root aspect - there are three things at the root level in the
        // index
        assertEquals(3, results.length());
        results.close();
    }

    /**
     * Daemon thread
     */
    class Nester extends Thread
    {
        Thread waiter;

        int repeats;

        SearchService searcher;

        Nester(String name, Thread waiter, int repeats, SearchService searcher)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
            this.repeats = repeats;
            this.searcher = searcher;
        }

        public void run()
        {
            authenticationComponent.setSystemUserAsCurrentUser();

            if (waiter != null)
            {
                waiter.start();
            }
            try
            {
                System.out.println("Start " + this.getName());
                for (int i = 0; i < repeats; i++)
                {
                    // Map<QName, ChildAssociationRef> assocRefs = commitNodeGraph();
                    if (i % 50 == 0)
                    {
                        System.out.println(" " + this.getName() + " " + i);
                    }
                    ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"");
                    results.close();
                }
                System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(12);
            }
            if (waiter != null)
            {
                try
                {
                    waiter.join();
                }
                catch (InterruptedException e)
                {
                }
            }
        }

    }

    private NamespacePrefixResolver getNamespacePrefixReolsver(String defaultURI)
    {
        DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver(null);
        nspr.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        nspr.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        nspr.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
        nspr.registerNamespace("namespace", "namespace");
        nspr.registerNamespace(NamespaceService.DEFAULT_PREFIX, defaultURI);
        return nspr;
    }
}
