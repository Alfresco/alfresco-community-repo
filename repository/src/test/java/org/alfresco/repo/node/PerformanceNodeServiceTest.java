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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;

/**
 * PerformanceNodeServiceTest
 */
@Category(OwnJVMTestsCategory.class)
public class PerformanceNodeServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    public static final QName TYPE_QNAME_TEST = QName.createQName(NAMESPACE, "multiprop");
    public static final QName PROP_QNAME_NAME = QName.createQName(NAMESPACE, "name");
    public static final QName ASSOC_QNAME_CHILDREN = QName.createQName(NAMESPACE, "child");

    private int flushCount = Integer.MAX_VALUE;

    private int testDepth = 3;
    private int testChildCount = 5;
    private int testStringPropertyCount = 10;
    private int testContentPropertyCount = 10;

    private ApplicationContext applicationContext;

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
        applicationContext = ApplicationContextHelper.getApplicationContext();
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
        RetryingTransactionCallback<NodeRef> createStoreWork = new RetryingTransactionCallback<NodeRef>() {
            public NodeRef execute()
            {
                StoreRef storeRef = nodeService.createStore(
                        StoreRef.PROTOCOL_WORKSPACE,
                        "Test_" + System.nanoTime());
                return nodeService.getRootNode(storeRef);
            }
        };
        rootNodeRef = txnService.getRetryingTransactionHelper().doInTransaction(createStoreWork);
    }

    @Override
    protected void tearDown()
    {}

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
        RetryingTransactionCallback<Object> buildChildrenWork = new RetryingTransactionCallback<Object>() {
            public Object execute()
            {
                IntegrityChecker.setWarnInTransaction();
                buildNodeChildren(rootNodeRef, 1, testDepth, testChildCount);
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(buildChildrenWork);

        long endTime = System.currentTimeMillis();

        System.out.println("Test completed: \n" +
                "   Built " + nodeCount + " nodes in " + (endTime - startTime) + "ms \n" +
                "   Depth: " + testDepth + "\n" +
                "   Child count: " + testChildCount);
    }

    public void buildNodeChildren(NodeRef parent, int level, int maxLevel, int childCount)
    {
        for (int i = 0; i < childCount; i++)
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
                writer.putContent(level + "_" + i + "_" + j);
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
                        "   time: " + (double) diffTime / 1000.0 / 60.0 + " minutes \n" +
                        "   average: " + (double) nodeCount / (double) diffTime * 1000.0 + " nodes/s");
            }

            if (level < maxLevel)
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
