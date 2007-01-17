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
package org.alfresco.repo.node;

import java.io.InputStream;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
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
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Andy Hind
 */
@SuppressWarnings("unused")
public class ConcurrentNodeServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";

    public static final String TEST_PREFIX = "test";

    public static final QName TYPE_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");

    public static final QName ASPECT_QNAME_TEST_TITLED = QName.createQName(NAMESPACE, "titled");

    public static final QName PROP_QNAME_TEST_TITLE = QName.createQName(NAMESPACE, "title");

    public static final QName PROP_QNAME_TEST_MIMETYPE = QName.createQName(NAMESPACE, "mimetype");

    public static final int COUNT = 10;

    public static final int REPEATS = 10;

    static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;

    private TransactionService transactionService;

    private NodeRef rootNodeRef;

    private FullTextSearchIndexer luceneFTS;

    private AuthenticationComponent authenticationComponent;

    public ConcurrentNodeServiceTest()
    {
        super();
    }

    protected void setUp() throws Exception
    {
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
        luceneFTS = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");
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

    public void test1() throws Exception
    {
        testConcurrent();
    }
    
    public void test2() throws Exception
    {
        testConcurrent();
    }
    public void test3() throws Exception
    {
        testConcurrent();
    }
    public void test4() throws Exception
    {
        testConcurrent();
    }
    public void test5() throws Exception
    {
        testConcurrent();
    }
    public void test6() throws Exception
    {
        testConcurrent();
    }
    public void test7() throws Exception
    {
        testConcurrent();
    }
    public void test8() throws Exception
    {
        testConcurrent();
    }
    public void test9() throws Exception
    {
        testConcurrent();
    }
    public void test10() throws Exception
    {
        testConcurrent();
    }
    
    public void testConcurrent() throws Exception
    {
        luceneFTS.pause();
        // TODO: LUCENE UPDATE ISSUE fix commit lock time out
        // IndexWriter.COMMIT_LOCK_TIMEOUT = 100000;

        Map<QName, ChildAssociationRef> assocRefs = commitNodeGraph();
        Thread runner = null;

        for (int i = 0; i < COUNT; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, REPEATS);
        }
        if (runner != null)
        {
            runner.start();

            try
            {
                runner.join();
                System.out.println("Query thread has waited for " + runner.getName());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        TransactionUtil.executeInUserTransaction(transactionService, new TransactionUtil.TransactionWork<Object>()
        {

            public Object doWork() throws Exception
            {
                // There are two nodes at the base level in each test
                assertEquals(2 * ((COUNT * REPEATS) + 1), nodeService.getChildAssocs(rootNodeRef).size());
                
                SearchService searcher = (SearchService) ctx.getBean(ServiceRegistry.SEARCH_SERVICE.getLocalName());
                assertEquals(2 * ((COUNT * REPEATS) + 1), searcher.selectNodes(rootNodeRef, "/*", null,
                        getNamespacePrefixReolsver(""), false).size());
                ResultSet results = null;
                try
                {
                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"");
                    // n6 has root aspect - there are three things at the root level in the
                    // index
                    assertEquals(3 * ((COUNT * REPEATS) + 1), results.length());
                    results.close();
                    
                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"");
                    // n6 has root aspect - there are three things at the root level in the
                    // index
                    assertEquals(3 * ((COUNT * REPEATS) + 1), results.length());
                    results.close();
                    
                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"");
                    // n6 has root aspect - there are three things at the root level in the
                    // index
                    assertEquals(2 * ((COUNT * REPEATS) + 1), results.length());
                    results.close();
                    
                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*/*\"");
                    // n6 has root aspect - there are three things at the root level in the
                    // index
                    assertEquals(1 * ((COUNT * REPEATS) + 1), results.length());
                    results.close();
                    
                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*/*/*\"");
                    // n6 has root aspect - there are three things at the root level in the
                    // index
                    assertEquals(0 * ((COUNT * REPEATS) + 1), results.length());
                    results.close();
                }
                finally
                {
                    if (results != null)
                    {
                        results.close();
                    }
                }
                return null;
            }

        });

    }

    /**
     * Daemon thread
     */
    private class Nester extends Thread
    {
        Thread waiter;

        int repeats;

        Nester(String name, Thread waiter, int repeats)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
            this.repeats = repeats;
        }

        public void run()
        {
            authenticationComponent.setSystemUserAsCurrentUser();

            if (waiter != null)
            {
                System.out.println("Starting " + waiter.getName());
                waiter.start();
            }
            try
            {
                System.out.println("Start " + this.getName());
                for (int i = 0; i < repeats; i++)
                {
                    Map<QName, ChildAssociationRef> assocRefs = commitNodeGraph();
                    System.out.println(" " + this.getName() + " " + i);
                }
                System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (waiter != null)
            {
                try
                {
                    waiter.join();
                    System.out.println("Thread "
                            + this.getName() + " has waited for " + (waiter == null ? "null" : waiter.getName()));
                }
                catch (InterruptedException e)
                {
                    System.err.println(e);
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
