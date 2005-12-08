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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * PerformanceNodeServiceTest
 */
public class PerformanceNodeServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    public static final QName  TYPE_QNAME_TEST = QName.createQName(NAMESPACE, "multiprop");
    public static final QName  PROP_QNAME_NAME = QName.createQName(NAMESPACE, "name");
    public static final QName  ASSOC_QNAME_CHILDREN = QName.createQName(NAMESPACE, "child");
    
    private int flushCount = Integer.MAX_VALUE;
    
    private int testDepth = 3;
    private int testChildCount = 5;
    private int testStringPropertyCount = 10;
    private int testContentPropertyCount = 10;
    
    private static Log logger = LogFactory.getLog(PerformanceNodeServiceTest.class);
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    
    protected DictionaryService dictionaryService;
    protected NodeService nodeService;
    private ContentService contentService;
    private TransactionService txnService;
    
    private int nodeCount = 0;
    
    private long startTime;
    /** populated during setup */
    protected NodeRef rootNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        
        // load the system model
        ClassLoader cl = PerformanceNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/contentModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDao);
        dictionaryService = loadModel(applicationContext);
        
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        txnService = (TransactionService) applicationContext.getBean("transactionComponent");
        contentService = (ContentService) applicationContext.getBean("contentService");
        
        // create a first store directly
        TransactionWork<NodeRef> createStoreWork = new TransactionWork<NodeRef>()
        {
            public NodeRef doWork()
            {
                StoreRef storeRef = nodeService.createStore(
                        StoreRef.PROTOCOL_WORKSPACE,
                        "Test_" + System.nanoTime());
                return nodeService.getRootNode(storeRef);
            }
        };
        rootNodeRef = TransactionUtil.executeInUserTransaction(txnService, createStoreWork);
    }
    
    @Override
    protected void tearDown()
    {
    }

    /**
     * Loads the test model required for building the node graphs
     */
    public static DictionaryService loadModel(ApplicationContext applicationContext)
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        
        // load the system model
        ClassLoader cl = PerformanceNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/contentModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDao);
        
        return dictionary;
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull("StoreService not set", nodeService);
        assertNotNull("NodeService not set", nodeService);
        assertNotNull("rootNodeRef not created", rootNodeRef);
    }
    
    public void testPerformanceNodeService() throws Exception
    {
        startTime = System.currentTimeMillis();
        
        // ensure that we execute the node tree building in a transaction
        TransactionWork<Object> buildChildrenWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                buildNodeChildren(rootNodeRef, 1, testDepth, testChildCount);
                return null;
            }
        };
        TransactionUtil.executeInUserTransaction(txnService, buildChildrenWork);
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Test completed: \n" +
                "   Built " + nodeCount + " nodes in " + (endTime-startTime) + "ms \n" +
                "   Depth: " + testDepth + "\n" +
                "   Child count: " + testChildCount);
    }
    
    public void buildNodeChildren(NodeRef parent, int level, int maxLevel, int childCount)
    {
        for (int i=0; i < childCount; i++)
        {
            ChildAssociationRef assocRef = this.nodeService.createNode(
                    parent, ASSOC_QNAME_CHILDREN, QName.createQName(NAMESPACE, "child" + i), TYPE_QNAME_TEST);
           
            nodeCount++;
            
            NodeRef childRef = assocRef.getChildRef();
             
            this.nodeService.setProperty(childRef,
                 ContentModel.PROP_NAME, "node" + level + "_" + i);

            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(17);
            for (int j = 0; j < testStringPropertyCount; j++)
            {
                properties.put(
                        QName.createQName(NAMESPACE, "string" + j),
                        level + "_" + i + "_" + j);
            }
            this.nodeService.setProperties(childRef, properties);
            
            for (int j = 0; j < testContentPropertyCount; j++)
            {
                ContentWriter writer = this.contentService.getWriter(
                      childRef, QName.createQName(NAMESPACE, "content" + j), true);
                
                writer.setMimetype("text/plain");
                writer.putContent( level + "_" + i + "_" + j );
            }
            
            long currentTime = System.currentTimeMillis();
            long diffTime = (currentTime - startTime);
            if (nodeCount % flushCount == 0)
            {
               System.out.println("Flushing transaction cache at nodecount: " + nodeCount); 
               System.out.println("At time index " + diffTime + "ms");
               AlfrescoTransactionSupport.flush();
            }
            if (nodeCount % 100 == 0)
            {
                System.out.println("Interim summary: \n" +
                        "   nodes: " + nodeCount + "\n" +
                        "   time: " + (double)diffTime/1000.0/60.0 + " minutes \n" +
                        "   average: " + (double)nodeCount/(double)diffTime*1000.0 + " nodes/s");
            }
            
            if (level <  maxLevel)
            {
                buildNodeChildren(childRef, level + 1, maxLevel, childCount);
            }
        }
    }
    
    /**
     * Runs a test with more depth
     */
    public static void main(String[] args)
    {
        try
        {
            PerformanceNodeServiceTest test = new PerformanceNodeServiceTest();
            test.setUp();
            test.testChildCount = 5;
            test.testDepth = 6;
            test.flushCount = 1000;
            
            test.testPerformanceNodeService();
            
            test.tearDown();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
