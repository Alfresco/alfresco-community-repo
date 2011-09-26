/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAOImpl;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.node.NodeBulkLoader;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.results.ChildAssocRefResultSet;
import org.alfresco.repo.search.results.DetachedResultSet;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO9075;
import org.alfresco.util.CachingDateFormat.SimpleDateFormatAndResolution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author andyh
 */
@SuppressWarnings("unused")
public class ADMLuceneTest extends TestCase implements DictionaryListener
{

    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/lucenetest";

    private static final QName ASSOC_TYPE_QNAME = QName.createQName(TEST_NAMESPACE, "assoc");

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private static Log logger = LogFactory.getLog(ADMLuceneTest.class);

    QName createdDate = QName.createQName(TEST_NAMESPACE, "createdDate");

    QName orderDouble = QName.createQName(TEST_NAMESPACE, "orderDouble");

    QName orderFloat = QName.createQName(TEST_NAMESPACE, "orderFloat");

    QName orderLong = QName.createQName(TEST_NAMESPACE, "orderLong");

    QName orderInt = QName.createQName(TEST_NAMESPACE, "orderInt");

    QName orderText = QName.createQName(TEST_NAMESPACE, "orderText");

    QName orderMLText = QName.createQName(TEST_NAMESPACE, "orderMLText");

    QName aspectWithChildren = QName.createQName(TEST_NAMESPACE, "aspectWithChildren");

    TransactionService transactionService;

    RetryingTransactionHelper retryingTransactionHelper;

    NodeService nodeService;

    DictionaryService dictionaryService;

    TenantService tenantService;

    private NodeRef rootNodeRef;

    private NodeRef n1;

    private NodeRef n2;

    private NodeRef n3;

    private NodeRef n4;

    private NodeRef n5;

    private NodeRef n6;

    private NodeRef n7;

    private NodeRef n8;

    private NodeRef n9;

    private NodeRef n10;

    private NodeRef n11;

    private NodeRef n12;

    private NodeRef n13;

    private NodeRef n14;

    private DictionaryDAO dictionaryDAO;

    private FullTextSearchIndexer luceneFTS;

    private QName testType = QName.createQName(TEST_NAMESPACE, "testType");

    private QName testSuperType = QName.createQName(TEST_NAMESPACE, "testSuperType");

    private QName testAspect = QName.createQName(TEST_NAMESPACE, "testAspect");

    private QName testSuperAspect = QName.createQName(TEST_NAMESPACE, "testSuperAspect");

    private ContentService contentService;

    private QueryRegisterComponent queryRegisterComponent;

    private DictionaryNamespaceComponent namespacePrefixResolver;

    private IndexerAndSearcher indexerAndSearcher;

    private ServiceRegistry serviceRegistry;

    private UserTransaction testTX;

    private AuthenticationComponent authenticationComponent;

    private NodeRef[] documentOrder;

    private NamespaceDAOImpl namespaceDao;

    private Date testDate;

    private NodeBulkLoader nodeBulkLoader;

    private QueryEngine queryEngine;

    private NodeRef n15;

    private M2Model model;

    // TODO: pending replacement
    private Dialect dialect;

    private LuceneConfig luceneConfig;

    /**
     *
     */
    public ADMLuceneTest()
    {
        super();
    }
    /**
     * @param arg0
     */
    public ADMLuceneTest(String arg0)
    {
        super(arg0);
    }

    public void afterDictionaryDestroy()
    {
    }

    public void afterDictionaryInit()
    {
    }

    public void onDictionaryInit()
    {
        // Register the test model
        dictionaryDAO.putModel(model);
        namespaceDao.addPrefix("test", TEST_NAMESPACE);
    }

    public void setUp() throws Exception
    {
        dialect = (Dialect) ctx.getBean("dialect");

        nodeService = (NodeService) ctx.getBean("dbNodeService");
        dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        luceneFTS = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");
        contentService = (ContentService) ctx.getBean("contentService");
        queryRegisterComponent = (QueryRegisterComponent) ctx.getBean("queryRegisterComponent");
        namespacePrefixResolver = (DictionaryNamespaceComponent) ctx.getBean("namespaceService");
        indexerAndSearcher = (IndexerAndSearcher) ctx.getBean("admLuceneIndexerAndSearcherFactory");
        luceneConfig = (LuceneConfig)ctx.getBean("admLuceneIndexerAndSearcherFactory");
        ((LuceneConfig) indexerAndSearcher).setMaxAtomicTransformationTime(1000000);
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        retryingTransactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");
        tenantService = (TenantService) ctx.getBean("tenantService");
        queryEngine = (QueryEngine) ctx.getBean("adm.luceneQueryEngineImpl");

        nodeBulkLoader = (NodeBulkLoader) ctx.getBean("nodeDAO");

        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);

        namespaceDao = (NamespaceDAOImpl) ctx.getBean("namespaceDAO");

        I18NUtil.setLocale(Locale.UK);

        this.authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");

        queryRegisterComponent.loadQueryCollection("testQueryRegister.xml");

        assertEquals(true, ctx.isSingleton("LuceneFullTextSearchIndexer"));

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();

        // load in the test model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/search/impl/lucene/LuceneTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDAO.register(this);
        dictionaryDAO.reset();
        assertNotNull(dictionaryDAO.getClass(testSuperType));

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), testSuperType, getOrderProperties()).getChildRef();
        nodeService.setProperty(n1, QName.createQName("{namespace}property-1"), "ValueOne");

        n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), testSuperType, getOrderProperties()).getChildRef();
        nodeService.setProperty(n2, QName.createQName("{namespace}property-1"), "valueone");
        nodeService.setProperty(n2, QName.createQName("{namespace}property-2"), "valuetwo");

        n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), testSuperType, getOrderProperties()).getChildRef();

        ObjectOutputStream oos;
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(n3);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            Object o = ois.readObject();
            ois.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<QName, Serializable> testProperties = new HashMap<QName, Serializable>();
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"), "TEXT THAT IS INDEXED STORED AND TOKENISED ATOMICALLY KEYONE");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic"), "TEXT THAT IS INDEXED STORED AND TOKENISED ATOMICALLY KEYUNSTORED");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic"), "TEXT THAT IS INDEXED STORED AND TOKENISED BUT NOT ATOMICALLY KEYTWO");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "int-ista"), Integer.valueOf(1));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "long-ista"), Long.valueOf(2));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "float-ista"), Float.valueOf(3.4f));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "double-ista"), Double.valueOf(5.6));
        //testDate = new Date(((new Date().getTime() - 10000)));
        Calendar c = new GregorianCalendar();
        c.setTime(new Date(((new Date().getTime() - 10000))));
        //c.add(Calendar.MINUTE, -1);
        //c.set(Calendar.MINUTE, 0);
        //c.set(Calendar.MILLISECOND, 999);
        //c.set(Calendar.SECOND, 1);
        //c.set(Calendar.MILLISECOND, 0);
        //c.set(Calendar.SECOND, 0);
        //c.set(Calendar.MINUTE, 0);
        //c.set(Calendar.HOUR_OF_DAY, 0);
//        c.set(Calendar.YEAR, 2000);
//        c.set(Calendar.MONTH, 12);
//        c.set(Calendar.DAY_OF_MONTH, 31);
//        c.set(Calendar.HOUR_OF_DAY, 23);
//        c.set(Calendar.MINUTE, 59);
//        c.set(Calendar.SECOND, 59);
//        c.set(Calendar.MILLISECOND, 999);
        testDate = c.getTime();
        testProperties.put(QName.createQName(TEST_NAMESPACE, "date-ista"), testDate);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "datetime-ista"), testDate);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "boolean-ista"), Boolean.valueOf(true));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "qname-ista"), QName.createQName("{wibble}wobble"));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "category-ista"), new NodeRef(storeRef, "CategoryId"));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "noderef-ista"), n1);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "path-ista"), nodeService.getPath(n3));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "locale-ista"), Locale.UK);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "period-ista"), new Period("period|12"));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "null"), null);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "list"), new ArrayList<Object>());
        MLText mlText = new MLText();
        mlText.addValue(Locale.ENGLISH, "banana");
        mlText.addValue(Locale.FRENCH, "banane");
        mlText.addValue(Locale.CHINESE, "香蕉");
        mlText.addValue(new Locale("nl"), "banaan");
        mlText.addValue(Locale.GERMAN, "banane");
        mlText.addValue(new Locale("el"), "μπανάνα");
        mlText.addValue(Locale.ITALIAN, "banana");
        mlText.addValue(new Locale("ja"), "�?ナナ");
        mlText.addValue(new Locale("ko"), "바나나");
        mlText.addValue(new Locale("pt"), "banana");
        mlText.addValue(new Locale("ru"), "банан");
        mlText.addValue(new Locale("es"), "plátano");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "ml"), mlText);
        // Any multivalued
        ArrayList<Serializable> anyValues = new ArrayList<Serializable>();
        anyValues.add(Integer.valueOf(100));
        anyValues.add("anyValueAsString");
        anyValues.add(new UnknownDataType());
        testProperties.put(QName.createQName(TEST_NAMESPACE, "any-many-ista"), anyValues);
        // Content multivalued
        // - note only one the first value is used from the collection
        // - andit has to go in type d:any as d:content is not allowed to be multivalued

        ArrayList<Serializable> contentValues = new ArrayList<Serializable>();
        contentValues.add(new ContentData(null, "text/plain", 0L, "UTF-16", Locale.UK));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "content-many-ista"), contentValues);

        // MLText multivalued

        MLText mlText1 = new MLText();
        mlText1.addValue(Locale.ENGLISH, "cabbage");
        mlText1.addValue(Locale.FRENCH, "chou");

        MLText mlText2 = new MLText();
        mlText2.addValue(Locale.ENGLISH, "lemur");
        mlText2.addValue(new Locale("ru"), "лемур");

        ArrayList<Serializable> mlValues = new ArrayList<Serializable>();
        mlValues.add(mlText1);
        mlValues.add(mlText2);

        testProperties.put(QName.createQName(TEST_NAMESPACE, "mltext-many-ista"), mlValues);

        // null in multi valued

        ArrayList<Object> testList = new ArrayList<Object>();
        testList.add(null);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "nullist"), testList);
        ArrayList<Object> testList2 = new ArrayList<Object>();
        testList2.add("woof");
        testList2.add(null);

        n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}four"), testType, testProperties).getChildRef();

        ContentWriter multiWriter = contentService.getWriter(n4, QName.createQName(TEST_NAMESPACE, "content-many-ista"), true);
        multiWriter.setEncoding("UTF-16");
        multiWriter.setMimetype("text/plain");
        multiWriter.putContent("multicontent");

        nodeService.getProperties(n1);
        nodeService.getProperties(n2);
        nodeService.getProperties(n3);
        nodeService.getProperties(n4);

        n5 = nodeService.createNode(n1, ASSOC_TYPE_QNAME, QName.createQName("{namespace}five"), testSuperType, getOrderProperties()).getChildRef();
        n6 = nodeService.createNode(n1, ASSOC_TYPE_QNAME, QName.createQName("{namespace}six"), testSuperType, getOrderProperties()).getChildRef();
        n7 = nodeService.createNode(n2, ASSOC_TYPE_QNAME, QName.createQName("{namespace}seven"), testSuperType, getOrderProperties()).getChildRef();
        n8 = nodeService.createNode(n2, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eight-2"), testSuperType, getOrderProperties()).getChildRef();
        n9 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}nine"), testSuperType, getOrderProperties()).getChildRef();
        n10 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ten"), testSuperType, getOrderProperties()).getChildRef();
        n11 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eleven"), testSuperType, getOrderProperties()).getChildRef();
        n12 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}twelve"), testSuperType, getOrderProperties()).getChildRef();
        n13 = nodeService.createNode(n12, ASSOC_TYPE_QNAME, QName.createQName("{namespace}thirteen"), testSuperType, getOrderProperties()).getChildRef();

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        MLText desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "Alfresco tutorial");
        desc1.addValue(Locale.US, "Alfresco tutorial");
        
        Date explicitCreatedDate = new Date();
        Thread.sleep(2000);
        
        properties.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties.put(ContentModel.PROP_DESCRIPTION, desc1);
        properties.put(ContentModel.PROP_CREATED, explicitCreatedDate);
        
        //Calendar c = new GregorianCalendar();
        //c.setTime(new Date());
        //c.set(Calendar.MILLISECOND, 0);
        //c.set(Calendar.SECOND, 0);
        //c.set(Calendar.MINUTE, 0);
        //c.set(Calendar.HOUR_OF_DAY, 0);
        //testDate = c.getTime();
        //properties.put(QName.createQName(TEST_NAMESPACE, "date-ista"), testDate);
        //properties.put(QName.createQName(TEST_NAMESPACE, "datetime-ista"), testDate);
        
        // note: cm:content - hence auditable aspect will be applied with any missing mandatory properties (cm:modified, cm:creator, cm:modifier)
        n14 = nodeService.createNode(n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}fourteen"), ContentModel.TYPE_CONTENT, properties).getChildRef();
        // nodeService.addAspect(n14, DictionaryBootstrap.ASPECT_QNAME_CONTENT,
        // properties);
        
        assertEquals(explicitCreatedDate, nodeService.getProperty(n14, ContentModel.PROP_CREATED));
        
        // note: cm:thumbnail - hence auditable aspect will be applied with mandatory properties (cm:created, cm:modified, cm:creator, cm:modifier)
        n15 = nodeService.createNode(n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}fifteen"), ContentModel.TYPE_THUMBNAIL, getOrderProperties()).getChildRef();

        ContentWriter writer = contentService.getWriter(n14, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        // InputStream is =
        // this.getClass().getClassLoader().getResourceAsStream("test.doc");
        // writer.putContent(is);
        writer.putContent("The quick brown fox jumped over the lazy dog and ate the Alfresco Tutorial, in pdf format, along with the following stop words;  a an and are"
                + " as at be but by for if in into is it no not of on or such that the their then there these they this to was will with: "
                + " and random charcters \u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF");
        // System.out.println("Size is " + writer.getSize());

        nodeService.addChild(rootNodeRef, n8, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight-0"));
        nodeService.addChild(n1, n8, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eight-1"));
        nodeService.addChild(n2, n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}link"));

        nodeService.addChild(n1, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n2, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n5, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n6, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n12, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n13, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));

        documentOrder = new NodeRef[] { rootNodeRef, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n3, n1, n2 };
    }

    private double orderDoubleCount = -0.11d;

    private Date orderDate = new Date();

    private float orderFloatCount = -3.5556f;

    private long orderLongCount = -1999999999999999l;

    private int orderIntCount = -45764576;

    private int orderTextCount = 0;

    /**
     * @return properties
     */
    public Map<QName, Serializable> getOrderProperties()
    {
        Map<QName, Serializable> testProperties = new HashMap<QName, Serializable>();
        testProperties.put(createdDate, orderDate);
        testProperties.put(orderDouble, orderDoubleCount);
        testProperties.put(orderFloat, orderFloatCount);
        testProperties.put(orderLong, orderLongCount);
        testProperties.put(orderInt, orderIntCount);
        testProperties.put(orderText, new String(new char[] { (char) ('a' + orderTextCount) }) + " cabbage");

        MLText mlText = new MLText();
        mlText.addValue(Locale.ENGLISH, new String(new char[] { (char) ('a' + orderTextCount) }) + " banana");
        mlText.addValue(Locale.FRENCH, new String(new char[] { (char) ('Z' - orderTextCount) }) + " banane");
        mlText.addValue(Locale.CHINESE, new String(new char[] { (char) ('香' + orderTextCount) }) + " 香蕉");
        testProperties.put(orderMLText, mlText);

        orderDate = Duration.subtract(orderDate, new Duration("P1D"));
        orderDoubleCount += 0.1d;
        orderFloatCount += 0.82f;
        orderLongCount += 299999999999999l;
        orderIntCount += 8576457;
        orderTextCount++;
        return testProperties;
    }

    @Override
    protected void tearDown() throws Exception
    {
        
        if (testTX.getStatus() == Status.STATUS_ACTIVE)
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }

    
    
    /*
     * Not normally run as index backup happens at 3.00 
     */
    public void doNotTestIndexBackAllowsReadOperations() throws Exception
    {
        testTX.commit();
        
        Thread queryThread = new QueryDuringBackupThread("Query", "PATH:\"//*\"", 2*60*1000);
        queryThread.start();
     
        Thread createThread = new CreateDuringBackupThread("Create");
        createThread.start();
        
        try
        {
            queryThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    class QueryDuringBackupThread extends Thread
    {
        String query;

        long period;
        
        QueryDuringBackupThread(String name, String query, long period)
        {
            super(name);
            this.setDaemon(true);
            this.query = query;
            this.period = period;
        }

        public void run()
        {
            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            authenticationComponent.setSystemUserAsCurrentUser();
            try
            {
                System.out.println("Start " + this.getName());
                int i = 0;
                int length = 0;
                while(currentTime - startTime < period)
                {
                    RetryingTransactionCallback<Integer> queryCallback = new RetryingTransactionCallback<Integer>()
                    {
                        public Integer execute() throws Throwable
                        {
                            SearchParameters sp = new SearchParameters();
                            sp.addStore(rootNodeRef.getStoreRef());
                            sp.setLanguage("lucene");
                            sp.setQuery(query);
                          
                            ResultSet results =   serviceRegistry.getSearchService().query(sp);
                            int count = results.length();
                            results.close();
                            return count;
                        }
                    };
                    length = retryingTransactionHelper.doInTransaction(queryCallback);
                    i++;
                    if(i % 1000 == 0)
                    {
                        currentTime = System.currentTimeMillis();
                    }
                    System.out.println("Query "+i+" count = "+length);
                }

                System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                authenticationComponent.clearCurrentSecurityContext();
            }
        }

    }

    class CreateDuringBackupThread extends Thread
    {
       

        CreateDuringBackupThread(String name)
        {
            super(name);
            this.setDaemon(true);
        }

        public void run()
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            try
            {
                System.out.println("Start " + this.getName());
                RetryingTransactionCallback<String> createAndDeleteCallback = new RetryingTransactionCallback<String>()
                {
                    public String execute() throws Throwable
                    {
                       String guid = GUID.generate();
                       ChildAssociationRef test = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}"+guid), testSuperType);
                       return guid;
                    }
                };
                
                while(true)
                {
                    String guid = retryingTransactionHelper.doInTransaction(createAndDeleteCallback);
                    System.out.println("Created " + guid);
                }
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                authenticationComponent.clearCurrentSecurityContext();
            }
        }

    }

    /**
     * Not required to run all the time; it's here for profiling.
     */
    public void restManyReaders() throws Exception
    {
        QName propQName = QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic");

        NodeRef base = rootNodeRef;
        for (int i = 0; i < 10; i++)
        {
            NodeRef dir = nodeService.createNode(base, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}d-" + i), testSuperType, null).getChildRef();
            for (int j = 0; j < 100; j++)
            {
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(propQName, "lemon");
                NodeRef file = nodeService.createNode(dir, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}meep"), testSuperType, properties).getChildRef();
            }
        }
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//meep\"");
        int count = results.length();
        assertTrue(count > 0);
        results.close();
        testTX.commit();

        Thread runner = null;

        // testQuery(searcher, runner, "PATH:\"/d-0/*\"");
        // testQuery(searcher, runner, "PATH:\"/d-0/meep\"");
        // testQuery(searcher, runner, "PATH:\"/d-0//*\"");
        // testQuery(searcher, runner, "PATH:\"/d-0//meep\"");
        testQuery(searcher, runner, "PATH:\"//*\"");
        // testQuery(searcher, runner, "PATH:\"//meep\"");
        // testQuery(searcher, runner, "@"+LuceneQueryParser.escape(propQName.toString())+":\"lemon\"");

    }

    private void testQuery(ADMLuceneSearcherImpl searcher, Thread runner, String query)
    {
        for (int i = 0; i < 1; i++)
        {
            runner = new QueryThread("Concurrent-" + i, runner, searcher, query);
        }
        long start = System.nanoTime();
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
        long end = System.nanoTime();
        System.out.println(query + "\t" + ((end - start) / 1e9f));
    }

    class QueryThread extends Thread
    {
        Thread waiter;

        ADMLuceneSearcherImpl searcher;

        String query;

        QueryThread(String name, Thread waiter, ADMLuceneSearcherImpl searcher, String query)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
            this.searcher = searcher;
            this.query = query;
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
                // System.out.println("Start " + this.getName());

                RetryingTransactionCallback<Object> createAndDeleteCallback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        for (int i = 0; i < 100; i++)
                        {
                            ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", query);
                            int count = results.length();
                            for (ResultSetRow row : results)
                            {
                                NodeRef nr = row.getNodeRef();
                            }
                            results.close();
                        }
                        return null;
                    }
                };
                retryingTransactionHelper.doInTransaction(createAndDeleteCallback);

                // System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                authenticationComponent.clearCurrentSecurityContext();
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

    private IndexReader getIndexReader()
    {
        ADMLuceneSearcherImpl searcher = buildSearcher();
        return searcher.getSearcher().getIndexReader();
    }

    public void testMaskDeletes() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(true);
        ResultSet results = serviceRegistry.getSearchService().query(sp);
        int initialCount = results.length();
        results.close();

        for (int j = 0; j < 20; j++)
        {
            ArrayList<NodeRef> added = new ArrayList<NodeRef>();
            for (int i = 0; i < 50; i++)
            {
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, "Mask " + i);
                added.add(nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mask-" + i), testSuperType,
                        properties).getChildRef());
            }
            testTX.commit();
            testTX = transactionService.getUserTransaction();
            testTX.begin();

            int count = 0;
            IndexReader indexReader = getIndexReader();
            TermDocs termDocs = indexReader.termDocs(new Term("@{http://www.alfresco.org/model/content/1.0}name", "mask"));
            if (termDocs.next())
            {
                count++;
                while (termDocs.skipTo(termDocs.doc()))
                {
                    count++;
                }
            }
            termDocs.close();
            assertEquals(added.size() + j, count);

            sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("PATH:\"//cm:*\" AND @cm\\:name:(0 1 2 3 4 5 6 7 8 9) AND ISNOTNULL:\"cm:name\"");
            sp.addStore(rootNodeRef.getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            results = serviceRegistry.getSearchService().query(sp);
            results.close();

            sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("@cm\\:name:\"mask 1\"");
            sp.addStore(rootNodeRef.getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            results = serviceRegistry.getSearchService().query(sp);
            results.close();

            for (int i = 0; i < added.size() - 1; i++)
            {
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, "Mask " + i);
                nodeService.setProperties(added.get(i), properties);
            }

            testTX.commit();
            testTX = transactionService.getUserTransaction();
            testTX.begin();

            sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("PATH:\"//cm:*\" AND @cm\\:name:(0 1 2 3 4 5 6 7 8 9) AND ISNOTNULL:\"cm:name\"");
            sp.addStore(rootNodeRef.getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            results = serviceRegistry.getSearchService().query(sp);
            results.close();

            count = 0;
            indexReader = getIndexReader();
            termDocs = indexReader.termDocs(new Term("@{http://www.alfresco.org/model/content/1.0}name", "mask"));
            if (termDocs.next())
            {
                count++;
                while (termDocs.skipTo(termDocs.doc()))
                {
                    count++;
                }
            }
            termDocs.close();
            assertEquals(added.size() + j, count);

            sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("@cm\\:name:\"mask 1\"");
            sp.addStore(rootNodeRef.getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            results = serviceRegistry.getSearchService().query(sp);
            results.close();

            for (int i = 0; i < added.size() - 1; i++)
            {
                Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, "Mask " + i);
                nodeService.deleteNode(added.get(i));
            }
            testTX.commit();
            testTX = transactionService.getUserTransaction();
            testTX.begin();

            count = 0;
            indexReader = getIndexReader();
            termDocs = indexReader.termDocs(new Term("@{http://www.alfresco.org/model/content/1.0}name", "mask"));
            if (termDocs.next())
            {
                count++;
                while (termDocs.skipTo(termDocs.doc()))
                {
                    count++;
                }
            }
            termDocs.close();
            assertEquals(j+1, count);

            sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("PATH:\"//cm:*\" AND @cm\\:name:(0 1 2 3 4 5 6 7 8 9) AND ISNOTNULL:\"cm:name\"");
            sp.addStore(rootNodeRef.getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            results = serviceRegistry.getSearchService().query(sp);
            results.close();

            sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery("@cm\\:name:\"mask 1\"");
            sp.addStore(rootNodeRef.getStoreRef());
            sp.excludeDataInTheCurrentTransaction(true);

            results = serviceRegistry.getSearchService().query(sp);
            results.close();
        }

    }

    public void testQuoting() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TEXT:\"te\\\"thing\\\"st\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(true);

        ResultSet results = serviceRegistry.getSearchService().query(sp);
        results.close();
    }

    public void test_ALF_8007() throws Exception
    {
        // Check that updates before and after queries do not produce duplicates

        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        this.authenticationComponent.setCurrentUser("admin");

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        ResultSet results;
        ResultSetMetaData md;

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        properties.put(ContentModel.PROP_NAME, "ALF-8007");
        NodeRef one = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ALF-8007"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(1, results.length());
        results.close();

        MLText desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "ALF 8007");
        desc1.addValue(Locale.US, "ALF 8007");

        nodeService.setProperty(one, ContentModel.PROP_DESCRIPTION, desc1);

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(1, results.length());
        results.close();

        // check delete after update does delete from the index
        // ALF-8007
        // Already seen the delete in the TX and it is skipped (should only skip deletes in the same flush)

        nodeService.deleteNode(one);

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        // Check unreported ... create, query, update, delete
        // ... create, query, move, delete

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-2\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, "ALF-8007-2");
        NodeRef two = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ALF-8007-2"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-2\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(1, results.length());
        results.close();

        desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "ALF 8007 2");
        desc1.addValue(Locale.US, "ALF 8007 2");

        nodeService.setProperty(two, ContentModel.PROP_DESCRIPTION, desc1);
        nodeService.deleteNode(two);

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-2\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        // ... create, query, move, delete

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-3\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, "ALF-8007-3");
        NodeRef three = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ALF-8007-3"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-3\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(1, results.length());
        results.close();

        desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "ALF 8007 3");
        desc1.addValue(Locale.US, "ALF 8007 3");

        nodeService.moveNode(three, n1, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ALF-8007-3"));
        nodeService.deleteNode(three);

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-3\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        // ... create, move, query, delete

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-4\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

        properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, "ALF-8007-4");
        NodeRef four = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ALF-8007-4"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-4\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(1, results.length());
        results.close();

        desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "ALF 8007 4");
        desc1.addValue(Locale.US, "ALF 8007 4");

        nodeService.moveNode(four, n1, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ALF-8007-4"));

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-4\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(1, results.length());
        results.close();

        nodeService.deleteNode(four);

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("cm:name:\"ALF-8007-4\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(0, results.length());
        results.close();

    }

    public void testPublicServiceSearchServicePaging() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        List<NodeRef> expected = new ArrayList<NodeRef>(15);

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("PATH:\"//.\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(true);

        ResultSet results;
        ResultSetMetaData md;

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        md = results.getResultSetMetaData();
        for (ResultSetRow row : results)
        {
            expected.add(row.getNodeRef());
        }
        results.close();
        for (int skip = 0; skip < 20; skip++)
        {
            for (int max = 0; max < 20; max++)
            {
                doPage(expected, skip, max, sp, serviceRegistry.getSearchService());
            }
        }

        this.authenticationComponent.setCurrentUser("admin");
        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(true);

        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        sp.setMaxPermissionChecks(2);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(2, results.length());
        results.close();

        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setMaxPermissionChecks(2);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(2, results.length());
        results.close();

        // Even though it is picked up they go though before the time moves on
        // sp.setMaxPermissionChecks(-1);
        // sp.setMaxPermissionCheckTimeMillis(0);
        // results = serviceRegistry.getSearchService().query(sp);
        // assertEquals(0, results.length());
        // results.close();

    }

    private void doPage(List<NodeRef> expected, int skip, int max, SearchParameters sp, SearchService searcher)
    {
        sp.setSkipCount(skip);
        sp.setMaxItems(max);
        ResultSet results = searcher.query(sp);
        assertEquals("Skip = " + skip + " max  = " + max, skip + max > 16 ? 16 - skip : max, results.length());
        assertEquals("Skip = " + skip + " max  = " + max, (skip + max) < 16, results.hasMore());
        assertEquals("Skip = " + skip + " max  = " + max, skip, results.getStart());
        int actualPosition = skip;
        for (ResultSetRow row : results)
        {
            NodeRef nodeRef = row.getNodeRef();
            assertEquals("Skip = " + skip + " max  = " + max + " actual = " + actualPosition, expected.get(actualPosition), nodeRef);
            actualPosition++;
        }
        results.close();
    }

    public void testNonPublicSearchServicePaging() throws InterruptedException
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        List<NodeRef> expected = new ArrayList<NodeRef>(15);

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("PATH:\"//.\"");
        sp.addStore(rootNodeRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(true);

        ResultSet results;
        ResultSetMetaData md;

        results = searcher.query(sp);
        assertEquals(16, results.length());
        md = results.getResultSetMetaData();
        for (ResultSetRow row : results)
        {
            expected.add(row.getNodeRef());
        }
        results.close();

        for (int skip = 0; skip < 20; skip++)
        {
            for (int max = 0; max < 20; max++)
            {
                doPage(expected, skip, max, sp, searcher);
            }
        }

    }

    private void doPage(List<NodeRef> expected, int skip, int max, SearchParameters sp, ADMLuceneSearcherImpl searcher)
    {
        sp.setSkipCount(skip);
        sp.setMaxItems(max);
        ResultSet results = searcher.query(sp);
        assertEquals("Skip = " + skip + " max  = " + max, skip + max > 16 ? 16 - skip : max, results.length());
        assertEquals("Skip = " + skip + " max  = " + max, (skip + max) < 16, results.hasMore());
        assertEquals("Skip = " + skip + " max  = " + max, skip, results.getStart());
        int actualPosition = skip;
        for (ResultSetRow row : results)
        {
            NodeRef nodeRef = row.getNodeRef();
            assertEquals("Skip = " + skip + " max  = " + max + " actual = " + actualPosition, expected.get(actualPosition), nodeRef);
            actualPosition++;
        }
        results.close();
    }

    public void testAlfrescoSql() throws InterruptedException
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        alfrescoSqlQueryWithCount(searcher, "SELECT * FROM cmis:document", 1);
        alfrescoSqlQueryWithCount(searcher, "SELECT * FROM cmis:document D JOIN cm:ownable O ON D.cmis:objectId = O.cmis:objectId", 0);
    }

    public void alfrescoSqlQueryWithCount(ADMLuceneSearcherImpl searcher, String query, int count)
    {
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), SearchService.LANGUAGE_CMIS_ALFRESCO, query, null);
        assertEquals(count, results.length());
        results.getResultSetMetaData();
        results.close();
    }

    public void testCmisSql() throws InterruptedException
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        sqlQueryWithCount(searcher, "SELECT * FROM cmis:document", 1);
        sqlQueryWithCount(searcher, "SELECT * FROM cmis:document D WHERE CONTAINS(D,'lazy')", 1);
    }

    public void sqlQueryWithCount(ADMLuceneSearcherImpl searcher, String query, int count)
    {
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), SearchService.LANGUAGE_CMIS_STRICT, query, null);
        assertEquals(count, results.length());
        results.getResultSetMetaData();
        results.close();
    }

    public void testFtsSort() throws Throwable
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery("-eager or -dog");
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.addSort(ContentModel.PROP_NODE_UUID.toString(), true);
        ResultSet results = searcher.query(sp);
        assertEquals(16, results.length());

        String f = null;
        for (ResultSetRow row : results)
        {
            String currentBun = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_NODE_UUID));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (f != null)
            {
                assertTrue(f.compareTo(currentBun) <= 0);
            }
            f = currentBun;
        }

        results.close();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery("-eager or -dog");
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.addSort(ContentModel.PROP_NODE_UUID.toString(), false);
        results = searcher.query(sp);
        assertEquals(16, results.length());

        f = null;
        for (ResultSetRow row : results)
        {
            String currentBun = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_NODE_UUID));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (f != null)
            {
                assertTrue(f.compareTo(currentBun) >= 0);
            }
            f = currentBun;
        }

        results.close();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery("-eager or -dog");
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.addSort("@" + ContentModel.PROP_NODE_UUID.toString(), false);
        results = searcher.query(sp);
        assertEquals(16, results.length());

        f = null;
        for (ResultSetRow row : results)
        {
            String currentBun = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_NODE_UUID));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (f != null)
            {
                assertTrue(f.compareTo(currentBun) >= 0);
            }
            f = currentBun;
        }

        results.close();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery("-eager or -dog");
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.addSort("cm:name", false);
        results = searcher.query(sp);
        assertEquals(16, results.length());

        f = null;
        for (ResultSetRow row : results)
        {
            String currentBun = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_NODE_UUID));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (f != null)
            {
                assertTrue(f.compareTo(currentBun) >= 0);
            }
            f = currentBun;
        }

        results.close();

        sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery("-eager or -dog");
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.addSort("test:neverIndexed", false);
        results = searcher.query(sp);
        assertEquals(16, results.length());
        results.close();

    }

    public void testFTS() throws InterruptedException
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        
        ftsQueryWithCount(searcher, "\"lazy\"", 1);
        ftsQueryWithCount(searcher, "lazy and dog", 1);
        ftsQueryWithCount(searcher, "-lazy and -dog", 15);
        ftsQueryWithCount(searcher, "-lazy and dog", 0);
        ftsQueryWithCount(searcher, "lazy and -dog", 0);
        ftsQueryWithCount(searcher, "|lazy and |dog", 1);
        ftsQueryWithCount(searcher, "|eager and |dog", 1);
        ftsQueryWithCount(searcher, "|lazy and |wolf", 1);
        ftsQueryWithCount(searcher, "|eager and |wolf", 0);
        ftsQueryWithCount(searcher, "-lazy or -dog", 15);
        ftsQueryWithCount(searcher, "-eager or -dog", 16);
        ftsQueryWithCount(searcher, "-lazy or -wolf", 16);
        ftsQueryWithCount(searcher, "-eager or -wolf", 16);
        ftsQueryWithCount(searcher, "lazy dog", 1);
        ftsQueryWithCount(searcher, "lazy and not dog", 0);
        ftsQueryWithCount(searcher, "lazy not dog", 16);
        ftsQueryWithCount(searcher, "lazy and !dog", 0);
        ftsQueryWithCount(searcher, "lazy !dog", 16);
        ftsQueryWithCount(searcher, "lazy and -dog", 0);
        ftsQueryWithCount(searcher, "lazy -dog", 16);
        ftsQueryWithCount(searcher, "TEXT:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "cm_content:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "=cm_content:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "~cm_content:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "cm:content:big OR cm:content:lazy", 1);
        ftsQueryWithCount(searcher, "cm:content:big AND cm:content:lazy", 0);
        ftsQueryWithCount(searcher, "{http://www.alfresco.org/model/content/1.0}content:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "=lazy", 1);
        ftsQueryWithCount(searcher, "@cm:content:big OR @cm:content:lazy", 1);
        ftsQueryWithCount(searcher, "@cm:content:big AND @cm:content:lazy", 0);
        ftsQueryWithCount(searcher, "@{http://www.alfresco.org/model/content/1.0}content:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "~@cm:content:big OR ~@cm:content:lazy", 1);
        ftsQueryWithCount(searcher, "brown * quick", 0);
        ftsQueryWithCount(searcher, "brown * dog", 1);
        ftsQueryWithCount(searcher, "brown * dog", 1);
        ftsQueryWithCount(searcher, "brown *(0) dog", 0);
        ftsQueryWithCount(searcher, "brown *(1) dog", 0);
        ftsQueryWithCount(searcher, "brown *(2) dog", 0);
        ftsQueryWithCount(searcher, "brown *(3) dog", 0);
        ftsQueryWithCount(searcher, "brown *(4) dog", 1); // "the" does not count
        ftsQueryWithCount(searcher, "brown *(5) dog", 1);
        ftsQueryWithCount(searcher, "brown *(6) dog", 1);
        ftsQueryWithCount(searcher, "TEXT:(\"lazy\")", 1);
        ftsQueryWithCount(searcher, "TEXT:(lazy and dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(-lazy and -dog)", 15);
        ftsQueryWithCount(searcher, "TEXT:(-lazy and dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(lazy and -dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(|lazy and |dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(|eager and |dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(|lazy and |wolf)", 1);
        ftsQueryWithCount(searcher, "TEXT:(|eager and |wolf)", 0);
        ftsQueryWithCount(searcher, "TEXT:(-lazy or -dog)", 15);
        ftsQueryWithCount(searcher, "TEXT:(-eager or -dog)", 16);
        ftsQueryWithCount(searcher, "TEXT:(-lazy or -wolf)", 16);
        ftsQueryWithCount(searcher, "TEXT:(-eager or -wolf)", 16);
        ftsQueryWithCount(searcher, "TEXT:(lazy dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(lazy and not dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(lazy not dog)", 16);
        ftsQueryWithCount(searcher, "TEXT:(lazy and !dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(lazy !dog)", 16);
        ftsQueryWithCount(searcher, "TEXT:(lazy and -dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(lazy -dog)", 16);
        ftsQueryWithCount(searcher, "cm_content:(\"lazy\")", 1);
        ftsQueryWithCount(searcher, "cm:content:(big OR lazy)", 1);
        ftsQueryWithCount(searcher, "cm:content:(big AND lazy)", 0);
        ftsQueryWithCount(searcher, "{http://www.alfresco.org/model/content/1.0}content:(\"lazy\")", 1);
        ftsQueryWithCount(searcher, "TEXT:(=lazy)", 1);
        ftsQueryWithCount(searcher, "@cm:content:(big) OR @cm:content:(lazy)", 1);
        ftsQueryWithCount(searcher, "@cm:content:(big) AND @cm:content:(lazy)", 0);
        ftsQueryWithCount(searcher, "@{http://www.alfresco.org/model/content/1.0}content:(\"lazy\")", 1);
        ftsQueryWithCount(searcher, "@cm:content:(~big OR ~lazy)", 1);
        ftsQueryWithCount(searcher, "TEXT:(brown * quick)", 0);
        ftsQueryWithCount(searcher, "TEXT:(brown * dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(brown * dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(brown *(0) dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(brown *(1) dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(brown *(2) dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(brown *(3) dog)", 0);
        ftsQueryWithCount(searcher, "TEXT:(brown *(4) dog)", 1); // "the" does not count
        ftsQueryWithCount(searcher, "TEXT:(brown *(5) dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(brown *(6) dog)", 1);
        
        ftsQueryWithCount(searcher, "cm:content.mimetype:\"text/plain\"", 1);
        ftsQueryWithCount(searcher, "cm_content.mimetype:\"text/plain\"", 1);
        ftsQueryWithCount(searcher, "@cm:content.mimetype:\"text/plain\"", 1);
        ftsQueryWithCount(searcher, "@cm_content.mimetype:\"text/plain\"", 1);
        ftsQueryWithCount(searcher, "content.mimetype:\"text/plain\"", 1);
        ftsQueryWithCount(searcher, "@{http://www.alfresco.org/model/content/1.0}content.mimetype:\"text/plain\"", 1);
        ftsQueryWithCount(searcher, "{http://www.alfresco.org/model/content/1.0}content.mimetype:\"text/plain\"", 1);

        try
        {
            ftsQueryWithCount(searcher, "brown..dog", 1); // is this allowed??
            fail("Range query should not be supported against type d:content");
        }
        catch (UnsupportedOperationException e)
        {

        }

        try
        {
            ftsQueryWithCount(searcher, "TEXT:brown..dog", 1);
            fail("Range query should not be supported against type d:content");
        }
        catch (UnsupportedOperationException e)
        {

        }

        try
        {
            ftsQueryWithCount(searcher, "cm:content:brown..dog", 1);
            fail("Range query should not be supported against type d:content");
        }
        catch (UnsupportedOperationException e)
        {

        }

        QName qname = QName.createQName(TEST_NAMESPACE, "float\\-ista");
        ftsQueryWithCount(searcher, qname + ":3.40", 1);
        ftsQueryWithCount(searcher, qname + ":3..4", 1);
        ftsQueryWithCount(searcher, qname + ":3..3.39", 0);
        ftsQueryWithCount(searcher, qname + ":3..3.40", 1);
        ftsQueryWithCount(searcher, qname + ":3.41..3.9", 0);
        ftsQueryWithCount(searcher, qname + ":3.40..3.9", 1);

        ftsQueryWithCount(searcher, qname + ":[3 TO 4]", 1);
        ftsQueryWithCount(searcher, qname + ":[3 TO 3.39]", 0);
        ftsQueryWithCount(searcher, qname + ":[3 TO 3.4]", 1);
        ftsQueryWithCount(searcher, qname + ":[3.41 TO 4]", 0);
        ftsQueryWithCount(searcher, qname + ":[3.4 TO 4]", 1);
        ftsQueryWithCount(searcher, qname + ":[3 TO 3.4>", 0);
        ftsQueryWithCount(searcher, qname + ":<3.4 TO 4]", 0);
        ftsQueryWithCount(searcher, qname + ":<3.4 TO 3.4>", 0);

        ftsQueryWithCount(searcher, qname + ":(3.40)", 1);
        ftsQueryWithCount(searcher, qname + ":(3..4)", 1);
        ftsQueryWithCount(searcher, qname + ":(3..3.39)", 0);
        ftsQueryWithCount(searcher, qname + ":(3..3.40)", 1);
        ftsQueryWithCount(searcher, qname + ":(3.41..3.9)", 0);
        ftsQueryWithCount(searcher, qname + ":(3.40..3.9)", 1);

        ftsQueryWithCount(searcher, qname + ":([3 TO 4])", 1);
        ftsQueryWithCount(searcher, qname + ":([3 TO 3.39])", 0);
        ftsQueryWithCount(searcher, qname + ":([3 TO 3.4])", 1);
        ftsQueryWithCount(searcher, qname + ":([3.41 TO 4])", 0);
        ftsQueryWithCount(searcher, qname + ":([3.4 TO 4])", 1);
        ftsQueryWithCount(searcher, qname + ":([3 TO 3.4>)", 0);
        ftsQueryWithCount(searcher, qname + ":(<3.4 TO 4])", 0);
        ftsQueryWithCount(searcher, qname + ":(<3.4 TO 3.4>)", 0);

        ftsQueryWithCount(searcher, "test:float_x002D_ista:3.40", 1);

        ftsQueryWithCount(searcher, "lazy", 1);
        ftsQueryWithCount(searcher, "laz*", 1);
        ftsQueryWithCount(searcher, "l*y", 1);
        ftsQueryWithCount(searcher, "l??y", 1);
        ftsQueryWithCount(searcher, "?az?", 1);
        ftsQueryWithCount(searcher, "*zy", 1);

        ftsQueryWithCount(searcher, "\"lazy\"", 1);
        ftsQueryWithCount(searcher, "\"laz*\"", 1);
        ftsQueryWithCount(searcher, "\"l*y\"", 1);
        ftsQueryWithCount(searcher, "\"l??y\"", 1);
        ftsQueryWithCount(searcher, "\"?az?\"", 1);
        ftsQueryWithCount(searcher, "\"*zy\"", 1);

        ftsQueryWithCount(searcher, "cm:content:lazy", 1);
        ftsQueryWithCount(searcher, "cm:content:laz*", 1);
        ftsQueryWithCount(searcher, "cm:content:l*y", 1);
        ftsQueryWithCount(searcher, "cm:content:l??y", 1);
        ftsQueryWithCount(searcher, "cm:content:?az?", 1);
        ftsQueryWithCount(searcher, "cm:content:*zy", 1);

        ftsQueryWithCount(searcher, "cm:content:\"lazy\"", 1);
        ftsQueryWithCount(searcher, "cm:content:\"laz*\"", 1);
        ftsQueryWithCount(searcher, "cm:content:\"l*y\"", 1);
        ftsQueryWithCount(searcher, "cm:content:\"l??y\"", 1);
        ftsQueryWithCount(searcher, "cm:content:\"?az?\"", 1);
        ftsQueryWithCount(searcher, "cm:content:\"*zy\"", 1);

        ftsQueryWithCount(searcher, "cm:content:(lazy)", 1);
        ftsQueryWithCount(searcher, "cm:content:(laz*)", 1);
        ftsQueryWithCount(searcher, "cm:content:(l*y)", 1);
        ftsQueryWithCount(searcher, "cm:content:(l??y)", 1);
        ftsQueryWithCount(searcher, "cm:content:(?az?)", 1);
        ftsQueryWithCount(searcher, "cm:content:(*zy)", 1);

        ftsQueryWithCount(searcher, "cm:content:(\"lazy\")", 1);
        ftsQueryWithCount(searcher, "cm:content:(\"laz*\")", 1);
        ftsQueryWithCount(searcher, "cm:content:(\"l*y\")", 1);
        ftsQueryWithCount(searcher, "cm:content:(\"l??y\")", 1);
        ftsQueryWithCount(searcher, "cm:content:(\"?az?\")", 1);
        ftsQueryWithCount(searcher, "cm:content:(\"*zy\")", 1);

        ftsQueryWithCount(searcher, "lazy^2 dog^4.2", 1);

        ftsQueryWithCount(searcher, "lazy~0.7", 1);
        ftsQueryWithCount(searcher, "cm:content:laxy~0.7", 1);
        ftsQueryWithCount(searcher, "laxy~0.7", 1);
        ftsQueryWithCount(searcher, "=laxy~0.7", 1);
        ftsQueryWithCount(searcher, "~laxy~0.7", 1);

        ftsQueryWithCount(searcher, "\"quick fox\"~0", 0);
        ftsQueryWithCount(searcher, "\"quick fox\"~1", 1);
        ftsQueryWithCount(searcher, "\"quick fox\"~2", 1);
        ftsQueryWithCount(searcher, "\"quick fox\"~3", 1);

        ftsQueryWithCount(searcher, "\"fox quick\"~0", 0);
        ftsQueryWithCount(searcher, "\"fox quick\"~1", 0);
        ftsQueryWithCount(searcher, "\"fox quick\"~2", 0);
        ftsQueryWithCount(searcher, "\"fox quick\"~3", 1);

        ftsQueryWithCount(searcher, "lazy", 1);
        ftsQueryWithCount(searcher, "-lazy", 15);
        ftsQueryWithCount(searcher, "lazy -lazy", 16, null, n14);
        ftsQueryWithCount(searcher, "lazy^20 -lazy", 16, n14, null);
        ftsQueryWithCount(searcher, "lazy^20 -lazy^20", 16, null, n14);

        ftsQueryWithCount(searcher, "cm:content:lazy", 1);
        // Simple template
        ftsQueryWithCount(searcher, "ANDY:lazy", 1);
        // default namesapce cm
        ftsQueryWithCount(searcher, "content:lazy", 1);

        ftsQueryWithCount(searcher, "PATH:\"//.\"", 16);

        ftsQueryWithCount(searcher, "+PATH:\"/app:company_home/st:sites/cm:rmtestnew1/cm:documentLibrary//*\"", 0);
        ftsQueryWithCount(searcher, "+PATH:\"/app:company_home/st:sites/cm:rmtestnew1/cm:documentLibrary//*\" -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"", 15);
        ftsQueryWithCount(searcher, "+PATH:\"/app:company_home/st:sites/cm:rmtestnew1/cm:documentLibrary//*\" AND -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"",
                0);

        ftsQueryWithCount(searcher, "(brown *(6) dog)", 1);
        ftsQueryWithCount(searcher, "TEXT:(brown *(6) dog)", 1);
        ftsQueryWithCount(searcher, "\"//.\"", 0);
        ftsQueryWithCount(searcher, "PATH", "\"//.\"", 16);
        ftsQueryWithCount(searcher, "cm:content:brown", 1);
        ftsQueryWithCount(searcher, "ANDY:brown", 1);
        ftsQueryWithCount(searcher, "ANDY", "brown", 1);
        
        // test date ranges - note: expected 2 results = n14 (cm:content) and n15 (cm:thumbnail)
        ftsQueryWithCount(searcher, "modified:*", 2, Arrays.asList(new NodeRef[]{n14,n15}));
        ftsQueryWithCount(searcher, "modified:[MIN TO NOW]", 2, Arrays.asList(new NodeRef[]{n14,n15}));
    }
    
    
    private ADMLuceneSearcherImpl buildSearcher()
    {
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), luceneConfig);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixResolver("namespace"));
        searcher.setQueryRegister(queryRegisterComponent);
        searcher.setQueryLanguages(indexerAndSearcher.getQueryLanguages());
        return searcher;
    }
    
    public void testFTSandSort() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        ADMLuceneSearcherImpl searcher = buildSearcher();
        
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery( "PATH:\"//.\"");
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.addSort("@"+ContentModel.PROP_CONTENT.toString()+".size", true);
        ResultSet results = searcher.query(sp);
        assertEquals(16, results.length());
        results.close();
    }

    public void ftsQueryWithCount(ADMLuceneSearcherImpl searcher, String query, int count)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery(query);
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        ResultSet results = searcher.query(sp);
        assertEquals(count, results.length());
        results.close();
    }

    public void ftsQueryWithCount(ADMLuceneSearcherImpl searcher, String defaultFieldName, String query, int count)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setQuery(query);
        sp.addQueryTemplate("ANDY", "%cm:content");
        sp.setNamespace(NamespaceService.CONTENT_MODEL_1_0_URI);
        sp.excludeDataInTheCurrentTransaction(true);
        sp.setDefaultFieldName(defaultFieldName);
        ResultSet results = searcher.query(sp);
        assertEquals(count, results.length());
        results.close();
    }

    public void ftsQueryWithCount(ADMLuceneSearcherImpl searcher, String query, int count, NodeRef first, NodeRef last)
    {
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), SearchService.LANGUAGE_FTS_ALFRESCO, query, null);
        for (ResultSetRow row : results)
        {
            System.out.println("" + row.getScore() + nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_NAME));
        }
        assertEquals(count, results.length());
        if (first != null)
        {
            assertEquals(first, results.getNodeRef(0));
        }
        if (last != null)
        {
            assertEquals(last, results.getNodeRef(results.length() - 1));
        }
        results.close();
    }
    
    public void ftsQueryWithCount(ADMLuceneSearcherImpl searcher, String query, int count, List<NodeRef> expectedList)
    {
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), SearchService.LANGUAGE_FTS_ALFRESCO, query, null);
        for (ResultSetRow row : results)
        {
            System.out.println("" + row.getScore() + nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_NAME));
        }
        assertEquals(count, results.length());
        
        List<NodeRef> actualList = results.getNodeRefs();
        
        for (NodeRef expected : expectedList)
        {
            assertTrue("did not find "+expected, actualList.contains(expected));
        }
        
        results.close();
    }

    public void testOverWritetoZeroSize() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();
        testTX.commit();

        for (int i = 0; i < 50; i++)
        {
            testTX = transactionService.getUserTransaction();
            testTX.begin();
            runBaseTests();
            nodeService.setProperty(rootNodeRef, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n1, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n2, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n3, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n4, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n5, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n6, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n7, QName.createQName("{namespace}property-A"), "A");
            runBaseTests();
            testTX.commit();

            testTX = transactionService.getUserTransaction();
            testTX.begin();
            runBaseTests();
            nodeService.setProperty(n8, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n9, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n10, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n11, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n12, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n13, QName.createQName("{namespace}property-A"), "A");
            nodeService.setProperty(n14, QName.createQName("{namespace}property-A"), "A");
            runBaseTests();
            testTX.commit();
        }

    }

    public void testBulkResultSet1() throws Exception
    {
        doBulkTest(1);
    }

    public void testBulkResultSet10() throws Exception
    {
        doBulkTest(10);
    }

    public void testBulkResultSet100() throws Exception
    {

        doBulkTest(100);
    }

    public void testBulkResultSet1000() throws Exception
    {
        doBulkTest(1000);
    }

    public void xtestBulkResultSet10000() throws Exception
    {
        doBulkTest(10000);
    }

    private void doBulkTest(int n) throws Exception
    {
        Map<QName, Serializable> testProperties = new HashMap<QName, Serializable>();
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"), "BULK");
        for (int i = 0; i < n; i++)
        {
            nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}texas-" + i), testSuperType, testProperties).getChildRef();
        }
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(false);
        results.setBulkFetchSize(10);
        assertEquals(n, results.length());
        results.close();

        getCold(searcher, n);
        getWarm(searcher, n);
        getCold(searcher, n);
        getCold10(searcher, n);
        getCold100(searcher, n);
        getCold1000(searcher, n);
        getCold10000(searcher, n);

        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

    }

    private void getCold(ADMLuceneSearcherImpl searcher, int n)
    {
        nodeBulkLoader.clear();

        long start;

        long end;

        start = System.nanoTime();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(false);
        results.setBulkFetchSize(0);
        for (ResultSetRow row : results)
        {
            nodeService.getAspects(row.getNodeRef());
            nodeService.getProperties(row.getNodeRef());
        }
        results.close();

        end = System.nanoTime();

        System.out.println(n + " Cold in " + ((end - start) / 10e9));
    }

    private void getWarm(ADMLuceneSearcherImpl searcher, int n)
    {

        long start;

        long end;

        start = System.nanoTime();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(false);
        results.setBulkFetchSize(0);
        for (ResultSetRow row : results)
        {
            nodeService.getAspects(row.getNodeRef());
            nodeService.getProperties(row.getNodeRef());
        }
        results.close();

        end = System.nanoTime();

        System.out.println(n + " Warm in " + ((end - start) / 10e9));
    }

    private void getCold10(ADMLuceneSearcherImpl searcher, int n)
    {
        nodeBulkLoader.clear();

        long start;

        long end;

        start = System.nanoTime();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(true);
        results.setBulkFetchSize(10);
        for (ResultSetRow row : results)
        {
            nodeService.getAspects(row.getNodeRef());
            nodeService.getProperties(row.getNodeRef());
        }
        results.close();

        end = System.nanoTime();

        System.out.println(n + " Prefetch 10 in " + ((end - start) / 10e9));
    }

    private void getCold100(ADMLuceneSearcherImpl searcher, int n)
    {
        nodeBulkLoader.clear();

        long start;

        long end;

        start = System.nanoTime();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(true);
        results.setBulkFetchSize(100);
        for (ResultSetRow row : results)
        {
            nodeService.getAspects(row.getNodeRef());
            nodeService.getProperties(row.getNodeRef());
        }
        results.close();

        end = System.nanoTime();

        System.out.println(n + " Prefetch 100 in " + ((end - start) / 10e9));
    }

    private void getCold1000(ADMLuceneSearcherImpl searcher, int n)
    {
        nodeBulkLoader.clear();

        long start;

        long end;

        start = System.nanoTime();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(true);
        results.setBulkFetchSize(1000);
        for (ResultSetRow row : results)
        {
            nodeService.getAspects(row.getNodeRef());
            nodeService.getProperties(row.getNodeRef());
        }
        results.close();

        end = System.nanoTime();

        System.out.println(n + " Prefetch 1000 in " + ((end - start) / 10e9));
    }

    private void getCold10000(ADMLuceneSearcherImpl searcher, int n)
    {
        nodeBulkLoader.clear();

        long start;

        long end;

        start = System.nanoTime();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"BULK\"");
        ResultSet results = searcher.query(sp);
        results.setBulkFetch(true);
        results.setBulkFetchSize(10000);
        for (ResultSetRow row : results)
        {
            nodeService.getAspects(row.getNodeRef());
            nodeService.getProperties(row.getNodeRef());
        }
        results.close();

        end = System.nanoTime();

        System.out.println(n + " Prefetch 10000 in " + ((end - start) / 10e9));
    }

    /**
     * Test bug fix
     * 
     * @throws Exception
     */
    public void testSortIssue_AR_1515__AND__AR_1466() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        runBaseTests();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        sp.addSort("ID", true);
        ResultSet results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());

        String current = null;
        for (ResultSetRow row : results)
        {
            String id = row.getNodeRef().getId();

            if (current != null)
            {
                if (current.compareTo(id) > 0)
                {
                    fail();
                }
            }
            current = id;
        }
        results.close();

        assertEquals(5, serviceRegistry.getNodeService().getChildAssocs(rootNodeRef).size());
        serviceRegistry.getNodeService().createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}texas"), testSuperType).getChildRef();
        assertEquals(6, serviceRegistry.getNodeService().getChildAssocs(rootNodeRef).size());

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        sp.addSort("ID", true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(17, results.length());
        current = null;
        for (ResultSetRow row : results)
        {
            String id = row.getNodeRef().getId();

            if (current != null)
            {
                if (current.compareTo(id) > 0)
                {
                    fail();
                }
            }
            current = id;
        }
        results.close();

        assertEquals(6, serviceRegistry.getNodeService().getChildAssocs(rootNodeRef).size());
        serviceRegistry.getNodeService().createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}texas"), testSuperType).getChildRef();
        assertEquals(7, serviceRegistry.getNodeService().getChildAssocs(rootNodeRef).size());

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        sp.addSort("ID", true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(18, results.length());
        current = null;
        for (ResultSetRow row : results)
        {
            String id = row.getNodeRef().getId();

            if (current != null)
            {
                if (current.compareTo(id) > 0)
                {
                    fail();
                }
            }
            current = id;
        }
        results.close();

        testTX.rollback();

    }

    /**
     * @throws Exception
     */
    public void testAuxDataIsPresent() throws Exception
    {
        luceneFTS.pause();
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + n14 + "\"", null);
        assertEquals(1, results.length()); // one node
        results.close();

        nodeService.addAspect(n14, aspectWithChildren, null);
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        searcher = buildSearcher();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + n14 + "\"", null);
        assertEquals(10, results.length()); // one node + 9 aux paths to n14
        results.close();
    }

    /**
     * @throws Exception
     */
    public void testFirst() throws Exception
    {
        testReadAgainstDelta();
    }

    /**
     * @throws Exception
     */
    public void test0() throws Exception
    {
       
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();

        
    }

    /**
     * @throws Exception
     */
    public void testDeleteSecondaryAssocToContainer() throws Exception
    {
        luceneFTS.pause();

        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runBaseTests();
        nodeService.removeChild(n2, n13);
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:link\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//.\"", null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/.\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/./.\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//./*\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//././*/././.\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//*\"", null);
        assertEquals(10, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testDeleteSecondaryAssocToLeaf() throws Exception
    {
        luceneFTS.pause();

        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        runBaseTests();
        nodeService.removeChild(n12, n14);
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:common\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//.\"", null);
        assertEquals(27, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*\"", null);
        assertEquals(26, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/.\"", null);
        assertEquals(26, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/./.\"", null);
        assertEquals(26, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//./*\"", null);
        assertEquals(26, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//././*/././.\"", null);
        assertEquals(26, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//*\"", null);
        assertEquals(9, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testDeleteIssue() throws Exception
    {

        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        ChildAssociationRef testFind = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}testFind"), testSuperType);
        testTX.commit();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();

        RetryingTransactionCallback<Object> createAndDeleteCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                for (int i = 0; i < 100; i += 10)
                {
                    HashSet<ChildAssociationRef> refs = new HashSet<ChildAssociationRef>();
                    for (int j = 0; j < i; j++)
                    {
                        ChildAssociationRef test = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"), testSuperType);
                        refs.add(test);
                    }

                    for (ChildAssociationRef car : refs)
                    {
                        nodeService.deleteNode(car.getChildRef());
                    }
                }
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(createAndDeleteCallback);

        UserTransaction tx3 = transactionService.getUserTransaction();
        tx3.begin();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();
        tx3.commit();
    }

    /**
     * @throws Exception
     */
    public void testMTDeleteIssue() throws Exception
    {
        luceneFTS.pause();
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        ChildAssociationRef testFind = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}testFind"), testSuperType);
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();
        testTX.commit();

        Thread runner = null;

        for (int i = 0; i < 20; i++)
        {
            runner = new Nester("Concurrent-" + i, runner);
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

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();
        testTX.commit();
    }

    class Nester extends Thread
    {
        Thread waiter;

        Nester(String name, Thread waiter)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
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
                RetryingTransactionCallback<Object> createAndDeleteCallback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        for (int i = 0; i < 20; i++)
                        {
                            HashSet<ChildAssociationRef> refs = new HashSet<ChildAssociationRef>();
                            for (int j = 0; j < i; j++)
                            {
                                ChildAssociationRef test = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test_"
                                        + getName() + "_" + i + "_" + j), testSuperType);
                                refs.add(test);
                            }

                            for (ChildAssociationRef car : refs)
                            {
                                nodeService.deleteNode(car.getChildRef());
                            }
                        }
                        return null;
                    }
                };
                retryingTransactionHelper.doInTransaction(createAndDeleteCallback);
                System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                authenticationComponent.clearCurrentSecurityContext();
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

    /**
     * @throws Exception
     */
    public void testDeltaIssue() throws Exception
    {
        luceneFTS.pause();
        final NodeService pns = (NodeService) ctx.getBean("NodeService");

        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        testTX.commit();

        Thread thread = new Thread(new Runnable()
        {

            public void run()
            {
                try
                {
                    authenticationComponent.setSystemUserAsCurrentUser();
                    UserTransaction tx = transactionService.getUserTransaction();
                    tx = transactionService.getUserTransaction();
                    tx.begin();

                    SearchParameters sp = new SearchParameters();
                    sp.addStore(rootNodeRef.getStoreRef());
                    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                    sp.setQuery("PATH:\"//.\"");
                    sp.excludeDataInTheCurrentTransaction(false);
                    ResultSet results = serviceRegistry.getSearchService().query(sp);
                    for(ResultSetRow row : results) {
                       System.out.println("row = " + row.getQName());
                    }
                    assertEquals(15, results.length());
                    results.close();

                    sp = new SearchParameters();
                    sp.addStore(rootNodeRef.getStoreRef());
                    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                    sp.setQuery("PATH:\"//.\"");
                    sp.excludeDataInTheCurrentTransaction(false);
                    results = serviceRegistry.getSearchService().query(sp);
                    assertEquals(15, results.length());
                    results.close();

                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_TITLE, "woof");
                    pns.addAspect(n1, ContentModel.ASPECT_TITLED, props);

                    sp = new SearchParameters();
                    sp.addStore(rootNodeRef.getStoreRef());
                    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                    sp.setQuery("PATH:\"//.\"");
                    sp.excludeDataInTheCurrentTransaction(false);
                    results = serviceRegistry.getSearchService().query(sp);
                    assertEquals(15, results.length());
                    results.close();

                    tx.rollback();
                }
                catch (Throwable e)
                {
                    throw new RuntimeException(e);
                }

            }

        });

        thread.start();
        thread.join();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        ResultSet results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        runBaseTests();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_TITLE, "woof");
        pns.addAspect(n1, ContentModel.ASPECT_TITLED, props);

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        pns.setProperty(n1, ContentModel.PROP_TITLE, "cube");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

    }

    /**
     * @throws Exception
     */
    public void testRepeatPerformance() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        String query = "ID:\"" + rootNodeRef + "\"";
        // check that we get the result
        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        ResultSet results = searcher.query(sp);
        assertEquals("No results found from query", 1, results.length());

        long start = System.nanoTime();
        int count = 1000;
        // repeat
        for (int i = 0; i < count; i++)
        {
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query);
            results = searcher.query(sp);
        }
        long end = System.nanoTime();
        // dump results
        double duration = ((double) (end - start)) / 1E6; // duration in ms
        double average = duration / (double) count;
        System.out.println("Searched for identifier: \n"
                + "   count: " + count + "\n" + "   average: " + average + " ms/search \n" + "   a million searches could take: " + (1E6 * average) / 1E3 / 60D + " minutes");
        // anything over 10ms is dire
        if (average > 10.0)
        {
            logger.error("Search taking longer than 10ms: " + query);
        }
    }

    /**
     * @throws Exception
     */
    public void testSort() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addSort("ID", true);
        ResultSet results = searcher.query(sp);

        String current = null;
        for (ResultSetRow row : results)
        {
            String id = row.getNodeRef().getId();

            if (current != null)
            {
                if (current.compareTo(id) > 0)
                {
                    fail();
                }
            }
            current = id;
        }
        results.close();

        SearchParameters sp2 = new SearchParameters();
        sp2.addStore(rootNodeRef.getStoreRef());
        sp2.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp2.setQuery("PATH:\"//.\"");
        sp2.addSort("ID", false);
        results = searcher.query(sp2);

        current = null;
        for (ResultSetRow row : results)
        {
            String id = row.getNodeRef().getId();
            if (current != null)
            {
                if (current.compareTo(id) < 0)
                {
                    fail();
                }
            }
            current = id;
        }
        results.close();

        luceneFTS.resume();

        SearchParameters sp3 = new SearchParameters();
        sp3.addStore(rootNodeRef.getStoreRef());
        sp3.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp3.setQuery("PATH:\"//.\"");
        sp3.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_ASCENDING);
        results = searcher.query(sp3);

        int count = 0;
        for (ResultSetRow row : results)
        {
            assertEquals(documentOrder[count++], row.getNodeRef());
        }
        results.close();

        SearchParameters sp4 = new SearchParameters();
        sp4.addStore(rootNodeRef.getStoreRef());
        sp4.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp4.setQuery("PATH:\"//.\"");
        sp4.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_DESCENDING);
        results = searcher.query(sp4);

        count = 1;
        for (ResultSetRow row : results)
        {
            assertEquals(documentOrder[documentOrder.length - (count++)], row.getNodeRef());
        }
        results.close();

        SearchParameters sp5 = new SearchParameters();
        sp5.addStore(rootNodeRef.getStoreRef());
        sp5.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp5.setQuery("PATH:\"//.\"");
        sp5.addSort(SearchParameters.SORT_IN_SCORE_ORDER_ASCENDING);
        results = searcher.query(sp5);

        float score = 0;
        for (ResultSetRow row : results)
        {
            assertTrue(score <= row.getScore());
            score = row.getScore();
        }
        results.close();

        SearchParameters sp6 = new SearchParameters();
        sp6.addStore(rootNodeRef.getStoreRef());
        sp6.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp6.setQuery("PATH:\"//.\"");
        sp6.addSort(SearchParameters.SORT_IN_SCORE_ORDER_DESCENDING);
        results = searcher.query(sp6);

        score = 1.0f;
        for (ResultSetRow row : results)
        {
            assertTrue(score >= row.getScore());
            score = row.getScore();
        }
        results.close();

        // sort by created date

        SearchParameters sp7 = new SearchParameters();
        sp7.addStore(rootNodeRef.getStoreRef());
        sp7.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp7.setQuery("PATH:\"//.\"");
        sp7.addSort("@" + createdDate.getPrefixedQName(namespacePrefixResolver), true);
        results = searcher.query(sp7);

        Date date = null;
        for (ResultSetRow row : results)
        {
            Date currentBun = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(row.getNodeRef(), createdDate));
            // System.out.println(currentBun);
            if (date != null)
            {
                assertTrue(date.compareTo(currentBun) <= 0);
            }
            date = currentBun;
        }
        results.close();

        SearchParameters sp8 = new SearchParameters();
        sp8.addStore(rootNodeRef.getStoreRef());
        sp8.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp8.setQuery("PATH:\"//.\"");
        sp8.addSort("@" + createdDate, false);
        results = searcher.query(sp8);

        date = null;
        for (ResultSetRow row : results)
        {
            Date currentBun = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(row.getNodeRef(), createdDate));
            // System.out.println(currentBun);
            if ((date != null) && (currentBun != null))
            {
                assertTrue(date.compareTo(currentBun) >= 0);
            }
            date = currentBun;
        }
        results.close();

        SearchParameters sp_7 = new SearchParameters();
        sp_7.addStore(rootNodeRef.getStoreRef());
        sp_7.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp_7.setQuery("PATH:\"//.\"");
        sp_7.addSort("@" + ContentModel.PROP_MODIFIED, true);
        results = searcher.query(sp_7);

        date = null;
        for (ResultSetRow row : results)
        {
            Date currentBun = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_MODIFIED));
            if (currentBun != null)
            {
                Calendar c = new GregorianCalendar();
                c.setTime(currentBun);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.HOUR_OF_DAY, 0);
                currentBun = c.getTime();
            }
            if ((date != null) && (currentBun != null))
            {
                assertTrue(date.compareTo(currentBun) <= 0);
            }
            date = currentBun;
        }
        results.close();

        SearchParameters sp_8 = new SearchParameters();
        sp_8.addStore(rootNodeRef.getStoreRef());
        sp_8.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp_8.setQuery("PATH:\"//.\"");
        sp_8.addSort("@" + ContentModel.PROP_MODIFIED, false);
        results = searcher.query(sp_8);

        date = null;
        for (ResultSetRow row : results)
        {
            Date currentBun = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_MODIFIED));
            // System.out.println(currentBun);
            if (currentBun != null)
            {
                Calendar c = new GregorianCalendar();
                c.setTime(currentBun);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.HOUR_OF_DAY, 0);
                currentBun = c.getTime();
            }

            if ((date != null) && (currentBun != null))
            {
                assertTrue(date.compareTo(currentBun) >= 0);
            }
            date = currentBun;
        }
        results.close();

        // sort by double

        SearchParameters sp9 = new SearchParameters();
        sp9.addStore(rootNodeRef.getStoreRef());
        sp9.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp9.setQuery("PATH:\"//.\"");
        sp9.addSort("@" + orderDouble, true);
        results = searcher.query(sp9);

        Double d = null;
        for (ResultSetRow row : results)
        {
            Double currentBun = DefaultTypeConverter.INSTANCE.convert(Double.class, nodeService.getProperty(row.getNodeRef(), orderDouble));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (d != null)
            {
                assertTrue(d.compareTo(currentBun) <= 0);
            }
            d = currentBun;
        }
        results.close();

        SearchParameters sp10 = new SearchParameters();
        sp10.addStore(rootNodeRef.getStoreRef());
        sp10.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp10.setQuery("PATH:\"//.\"");
        sp10.addSort("@" + orderDouble, false);
        results = searcher.query(sp10);

        d = null;
        for (ResultSetRow row : results)
        {
            Double currentBun = DefaultTypeConverter.INSTANCE.convert(Double.class, nodeService.getProperty(row.getNodeRef(), orderDouble));
            // System.out.println(currentBun);
            if ((d != null) && (currentBun != null))
            {
                assertTrue(d.compareTo(currentBun) >= 0);
            }
            d = currentBun;
        }
        results.close();

        // sort by float

        SearchParameters sp11 = new SearchParameters();
        sp11.addStore(rootNodeRef.getStoreRef());
        sp11.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp11.setQuery("PATH:\"//.\"");
        sp11.addSort("@" + orderFloat, true);
        results = searcher.query(sp11);

        Float f = null;
        for (ResultSetRow row : results)
        {
            Float currentBun = DefaultTypeConverter.INSTANCE.convert(Float.class, nodeService.getProperty(row.getNodeRef(), orderFloat));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (f != null)
            {
                assertTrue(f.compareTo(currentBun) <= 0);
            }
            f = currentBun;
        }
        results.close();

        SearchParameters sp12 = new SearchParameters();
        sp12.addStore(rootNodeRef.getStoreRef());
        sp12.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp12.setQuery("PATH:\"//.\"");
        sp12.addSort("@" + orderFloat, false);
        results = searcher.query(sp12);

        f = null;
        for (ResultSetRow row : results)
        {
            Float currentBun = DefaultTypeConverter.INSTANCE.convert(Float.class, nodeService.getProperty(row.getNodeRef(), orderFloat));
            // System.out.println(currentBun);
            if ((f != null) && (currentBun != null))
            {
                assertTrue(f.compareTo(currentBun) >= 0);
            }
            f = currentBun;
        }
        results.close();

        // sort by long

        SearchParameters sp13 = new SearchParameters();
        sp13.addStore(rootNodeRef.getStoreRef());
        sp13.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp13.setQuery("PATH:\"//.\"");
        sp13.addSort("@" + orderLong, true);
        results = searcher.query(sp13);

        Long l = null;
        for (ResultSetRow row : results)
        {
            Long currentBun = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(row.getNodeRef(), orderLong));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (l != null)
            {
                assertTrue(l.compareTo(currentBun) <= 0);
            }
            l = currentBun;
        }
        results.close();

        SearchParameters sp14 = new SearchParameters();
        sp14.addStore(rootNodeRef.getStoreRef());
        sp14.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp14.setQuery("PATH:\"//.\"");
        sp14.addSort("@" + orderLong, false);
        results = searcher.query(sp14);

        l = null;
        for (ResultSetRow row : results)
        {
            Long currentBun = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(row.getNodeRef(), orderLong));
            // System.out.println(currentBun);
            if ((l != null) && (currentBun != null))
            {
                assertTrue(l.compareTo(currentBun) >= 0);
            }
            l = currentBun;
        }
        results.close();

        // sort by int

        SearchParameters sp15 = new SearchParameters();
        sp15.addStore(rootNodeRef.getStoreRef());
        sp15.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp15.setQuery("PATH:\"//.\"");
        sp15.addSort("@" + orderInt, true);
        results = searcher.query(sp15);

        Integer i = null;
        for (ResultSetRow row : results)
        {
            Integer currentBun = DefaultTypeConverter.INSTANCE.convert(Integer.class, nodeService.getProperty(row.getNodeRef(), orderInt));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if (i != null)
            {
                assertTrue(i.compareTo(currentBun) <= 0);
            }
            i = currentBun;
        }
        results.close();

        SearchParameters sp16 = new SearchParameters();
        sp16.addStore(rootNodeRef.getStoreRef());
        sp16.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp16.setQuery("PATH:\"//.\"");
        sp16.addSort("@" + orderInt, false);
        results = searcher.query(sp16);

        i = null;
        for (ResultSetRow row : results)
        {
            Integer currentBun = DefaultTypeConverter.INSTANCE.convert(Integer.class, nodeService.getProperty(row.getNodeRef(), orderInt));
            // System.out.println(currentBun);
            if ((i != null) && (currentBun != null))
            {
                assertTrue(i.compareTo(currentBun) >= 0);
            }
            i = currentBun;
        }
        results.close();

        // sort by text

        SearchParameters sp17 = new SearchParameters();
        sp17.addStore(rootNodeRef.getStoreRef());
        sp17.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp17.setQuery("PATH:\"//.\"");
        sp17.addSort("@" + orderText, true);
        results = searcher.query(sp17);

        String text = null;
        for (ResultSetRow row : results)
        {
            String currentBun = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row.getNodeRef(), orderText));
            // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ " "+currentBun);
            if ((text != null) && (currentBun != null))
            {
                assertTrue(text.compareTo(currentBun) <= 0);
            }
            text = currentBun;
        }
        results.close();

        SearchParameters sp18 = new SearchParameters();
        sp18.addStore(rootNodeRef.getStoreRef());
        sp18.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp18.setQuery("PATH:\"//.\"");
        sp18.addSort("@" + orderText, false);
        results = searcher.query(sp18);

        text = null;
        for (ResultSetRow row : results)
        {
            String currentBun = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(row.getNodeRef(), orderText));
            // System.out.println(currentBun);
            if ((text != null) && (currentBun != null))
            {
                assertTrue(text.compareTo(currentBun) >= 0);
            }
            text = currentBun;
        }
        results.close();

        // sort by content size
        
        
        
        // sort by ML text

        // Locale[] testLocales = new Locale[] { I18NUtil.getLocale(), Locale.ENGLISH, Locale.FRENCH, Locale.CHINESE };
        Locale[] testLocales = new Locale[] { I18NUtil.getLocale(), Locale.ENGLISH, Locale.FRENCH };
        for (Locale testLocale : testLocales)
        {

            SearchParameters sp19 = new SearchParameters();
            sp19.addStore(rootNodeRef.getStoreRef());
            sp19.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp19.setQuery("PATH:\"//.\"");
            sp19.addSort("@" + orderMLText, true);
            sp19.addLocale(testLocale);
            results = searcher.query(sp19);

            text = null;
            for (ResultSetRow row : results)
            {
                MLText mltext = DefaultTypeConverter.INSTANCE.convert(MLText.class, nodeService.getProperty(row.getNodeRef(), orderMLText));
                if (mltext != null)
                {
                    String currentBun = mltext.getValue(testLocale);
                    // System.out.println( (currentBun == null ? "null" : NumericEncoder.encode(currentBun))+ "
                    // "+currentBun);
                    if ((text != null) && (currentBun != null))
                    {
                        assertTrue(text.compareTo(currentBun) <= 0);
                    }
                    text = currentBun;
                }
            }
            results.close();

            SearchParameters sp20 = new SearchParameters();
            sp20.addStore(rootNodeRef.getStoreRef());
            sp20.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp20.setQuery("PATH:\"//.\"");
            sp20.addSort("@" + orderMLText, false);
            sp20.addLocale(testLocale);
            results = searcher.query(sp20);

            text = null;
            for (ResultSetRow row : results)
            {
                MLText mltext = DefaultTypeConverter.INSTANCE.convert(MLText.class, nodeService.getProperty(row.getNodeRef(), orderMLText));
                if (mltext != null)
                {
                    String currentBun = mltext.getValue(testLocale);
                    if ((text != null) && (currentBun != null))
                    {
                        assertTrue(text.compareTo(currentBun) >= 0);
                    }
                    text = currentBun;
                }
            }
            results.close();

        }

        luceneFTS.resume();

        SearchParameters spN = new SearchParameters();
        spN.addStore(rootNodeRef.getStoreRef());
        spN.setLanguage(SearchService.LANGUAGE_LUCENE);
        spN.setQuery("PATH:\"//.\"");
        spN.addSort("cabbage", false);
        results = searcher.query(spN);
        results.close();

        // test sort on unkown properties ALF-4193

        spN = new SearchParameters();
        spN.addStore(rootNodeRef.getStoreRef());
        spN.setLanguage(SearchService.LANGUAGE_LUCENE);
        spN.setQuery("PATH:\"//.\"");
        spN.addSort("PARENT", false);
        results = searcher.query(spN);
        results.close();

        spN = new SearchParameters();
        spN.addStore(rootNodeRef.getStoreRef());
        spN.setLanguage(SearchService.LANGUAGE_LUCENE);
        spN.setQuery("PATH:\"//.\"");
        spN.addSort("@PARENT:PARENT", false);
        results = searcher.query(spN);
        results.close();

        luceneFTS.resume();
        

        // sort by content size
        
        SearchParameters sp20 = new SearchParameters();
        sp20.addStore(rootNodeRef.getStoreRef());
        sp20.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp20.setQuery("PATH:\"//.\"");
        sp20.addSort("@" + ContentModel.PROP_CONTENT+".size", false);
        results = searcher.query(sp20);

        Long size = null;
        for (ResultSetRow row : results)
        {
            ContentData currentBun = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_CONTENT));
            // System.out.println(currentBun);
            if ((size != null) && (currentBun != null))
            {
                assertTrue(size.compareTo(currentBun.getSize()) >= 0);
            }
            if(currentBun != null)
            {
                size = currentBun.getSize();
            }
        }
        results.close();
        
        // sort by content mimetype
        
        SearchParameters sp21 = new SearchParameters();
        sp21.addStore(rootNodeRef.getStoreRef());
        sp21.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp21.setQuery("PATH:\"//.\"");
        sp21.addSort("@" + ContentModel.PROP_CONTENT+".mimetype", false);
        results = searcher.query(sp21);

        String mimetype = null;
        for (ResultSetRow row : results)
        {
            ContentData currentBun = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(row.getNodeRef(), ContentModel.PROP_CONTENT));
            // System.out.println(currentBun);
            if ((mimetype != null) && (currentBun != null))
            {
                assertTrue(mimetype.compareTo(currentBun.getMimetype()) >= 0);
            }
            if(currentBun != null)
            {
                mimetype = currentBun.getMimetype();
            }
        }
        results.close();

    }

    /**
     * @throws Exception
     */
    public void test1() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void test2() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void test3() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void test4() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@\\{namespace\\}property\\-2:\"valuetwo\"", null);
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void test5() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();
    }
    
    /**
     * @throws Exception
     */
    public void test6() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testNoOp() throws Exception
    {
        luceneFTS.pause();
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_1", luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        indexer.prepare();
        indexer.commit();
        luceneFTS.resume();
    }

    /**
     * Test basic index and search
     * 
     * @throws Exception
     * @throws InterruptedException
     */

    public void testStandAloneIndexerCommit() throws Exception
    {
        luceneFTS.pause();
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_1", luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);

        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        // //indexer.clearIndex();

        indexer.createNode(new ChildAssociationRef(null, null, null, rootNodeRef));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}one"), n1));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}two"), n2));
        indexer.updateNode(n1);
        // indexer.deleteNode(new ChildRelationshipRef(rootNode, "path",
        // newNode));

        indexer.prepare();
        indexer.commit();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@\\{namespace\\}property\\-2:\"valuetwo\"", null);
        simpleResultSetTest(results);

        ChildAssocRefResultSet r2 = new ChildAssocRefResultSet(nodeService, results.getNodeRefs(), false);
        simpleResultSetTest(r2);

        ChildAssocRefResultSet r3 = new ChildAssocRefResultSet(nodeService, results.getNodeRefs(), true);
        simpleResultSetTest(r3);

        ChildAssocRefResultSet r4 = new ChildAssocRefResultSet(nodeService, results.getChildAssocRefs());
        simpleResultSetTest(r4);

        DetachedResultSet r5 = new DetachedResultSet(results);
        simpleResultSetTest(r5);

        DetachedResultSet r6 = new DetachedResultSet(r2);
        simpleResultSetTest(r6);

        DetachedResultSet r7 = new DetachedResultSet(r3);
        simpleResultSetTest(r7);

        DetachedResultSet r8 = new DetachedResultSet(r4);
        simpleResultSetTest(r8);

        DetachedResultSet r9 = new DetachedResultSet(r5);
        simpleResultSetTest(r9);

        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@\\{namespace\\}property\\-1:\"valueone\"", null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@namespace\\:property\\-1:\"valueone\"", null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@namespace\\:property\\-1:\"valueone\"", null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@namespace\\:property\\-1:\"Valueone\"", null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@namespace\\:property\\-1:ValueOne", null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@namespace\\:property\\-1:valueone", null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        QName qname = QName.createQName("", "property-1");

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + n1.toString() + "\"", null);

        assertEquals(2, results.length());

        results.close();
        luceneFTS.resume();
    }

    private void simpleResultSetTest(ResultSet results)
    {
        assertEquals(1, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n2, results.getNodeRef(0));
        assertEquals(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}two"), n2), results.getChildAssocRef(0));
        assertEquals(1, results.getChildAssocRefs().size());
        assertNotNull(results.getChildAssocRefs());
        assertEquals(0, results.getRow(0).getIndex());
        assertEquals(1.0f, results.getRow(0).getScore());
        assertEquals(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}two"), n2), results.getRow(0).getChildAssocRef());
        assertEquals(n2, results.getRow(0).getNodeRef());
        assertEquals(QName.createQName("{namespace}two"), results.getRow(0).getQName());
        assertEquals("valuetwo", results.getRow(0).getValue(QName.createQName("{namespace}property-2")));
        for (ResultSetRow row : results)
        {
            assertNotNull(row);
        }
    }

    /**
     * @throws Exception
     */
    public void testStandAlonePathIndexer() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@\\{namespace\\}property-1:valueone", null);
        try
        {
            assertEquals(2, results.length());
            assertEquals(n1.getId(), results.getNodeRef(0).getId());
            assertEquals(n2.getId(), results.getNodeRef(1).getId());
            // assertEquals(1.0f, results.getScore(0));
            // assertEquals(1.0f, results.getScore(1));

            QName qname = QName.createQName("", "property-1");

        }
        finally
        {
            results.close();
        }

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+ID:\"" + n1.toString() + "\"", null);
        try
        {
            assertEquals(2, results.length());
        }
        finally
        {
            results.close();
        }

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + rootNodeRef.toString() + "\"", null);
        try
        {
            assertEquals(1, results.length());
        }
        finally
        {
            results.close();
        }
        luceneFTS.resume();
    }

    private void buildBaseIndex()
    {
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);
        // indexer.clearIndex();
        indexer.createNode(new ChildAssociationRef(null, null, null, rootNodeRef));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}one"), n1));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}two"), n2));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}three"), n3));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}four"), n4));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n1, QName.createQName("{namespace}five"), n5));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n1, QName.createQName("{namespace}six"), n6));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("{namespace}seven"), n7));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("{namespace}eight"), n8));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n5, QName.createQName("{namespace}nine"), n9));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n5, QName.createQName("{namespace}ten"), n10));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n5, QName.createQName("{namespace}eleven"), n11));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n5, QName.createQName("{namespace}twelve"), n12));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n12, QName.createQName("{namespace}thirteen"), n13));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n13, QName.createQName("{namespace}fourteen"), n14));
        indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, n13, QName.createQName("{namespace}fifteen"), n15));
        indexer.updateNode(n3);
        indexer.updateNode(n1);
        indexer.updateNode(n2);
        indexer.prepare();
        indexer.commit();
    }

    /**
     * @throws Exception
     */
    public void testAllPathSearch() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        runBaseTests();
        luceneFTS.resume();
    }

    
    
    
    private void runBaseTests() throws LuceneIndexException, IOException
    {
        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results;
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/.\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(9, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*/*\"", null);
        assertEquals(9, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//.\"", null);
        assertEquals(28, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*\"", null);
        assertEquals(27, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/.\"", null);
        assertEquals(27, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/./.\"", null);
        assertEquals(27, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//./*\"", null);
        assertEquals(27, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//././*/././.\"", null);
        assertEquals(27, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//common\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one//common\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//*\"", null);
        assertEquals(10, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//.\"", null);
        assertEquals(11, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen/.\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen//.\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen//.//.\"", null);
        assertEquals(1, results.length());
        results.close();

        // QNames

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"nine\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PRIMARYASSOCTYPEQNAME:\"test:assoc\"", null);
        assertEquals(11, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASSOCTYPEQNAME:\"test:assoc\"", null);
        assertEquals(11, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PRIMARYASSOCTYPEQNAME:\"sys:children\"", null);
        assertEquals(4, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASSOCTYPEQNAME:\"sys:children\"", null);
        assertEquals(5, results.length());
        results.close();

        // Type search tests

        QName qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"1\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":1", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"01\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":01", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + escapeQName(qname) + ":\"001\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@test\\:int\\-ista:\"0001\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[A TO 2]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[0 TO 2]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[0 TO A]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 1}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{0 TO 1}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{0 TO A}", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 2}", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{1 TO 2}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{1 TO A}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"2\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"02\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"002\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"0002\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[A TO 2]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[0 TO 2]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[0 TO A]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 2}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{0 TO 2}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{0 TO A}", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 3}", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{2 TO 3}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{2 TO A}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"3.4\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[A TO 4]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3 TO 4]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3 TO A]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[A TO 3.4]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3.3 TO 3.4]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3.3 TO A]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 3.4}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3.3 TO 3.4]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3.3 TO A]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"3.40\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"03.4\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"03.40\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"5.6\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"05.6\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"5.60\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"05.60\"", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[A TO 5.7]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[5.5 TO 5.7]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[5.5 TO A]", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 5.6}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{5.5 TO 5.6}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{5.5 TO A}", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{A TO 5.7}", null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{5.6 TO 5.7}", null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{5.6 TO A}", null);
        assertEquals(0, results.length());
        results.close();

        // Dates

        PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(TEST_NAMESPACE, "datetime-ista"));
        DataTypeDefinition dataType = propertyDef.getDataType();
        String analyserClassName = propertyDef.resolveAnalyserClassName();
        boolean usesDateTimeAnalyser = analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName());

        Date date = new Date();
        for (SimpleDateFormatAndResolution df : CachingDateFormat.getLenientFormatters())
        {
            if(!usesDateTimeAnalyser && (df.getResolution() < Calendar.DAY_OF_MONTH))
            {
                continue;
            }
            System.out.println("Date format: "+df.getSimpleDateFormat());

//            if(usesDateTimeAnalyser && (df.getSimpleDateFormat().format(date).length() < 22))
//            {
//                continue;
//            }

//            String sDate = df.getSimpleDateFormat().format(date);
//            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "DATE-ista")) + ":\"" + sDate + "\"", null);
//            assertEquals(1, results.length());
//            results.close();
//
//            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":\"" + sDate + "\"", null);
//            assertEquals(usesDateTimeAnalyser ? 0 : 1, results.length());
//            results.close();

            
            String sDate = df.getSimpleDateFormat().format(testDate);

            if(sDate.length() >= 9)
            {
                results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "date-ista")) + ":\"" + sDate + "\"", null);
                assertEquals(1, results.length());
                results.close();
            }
            
            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":\"" + sDate + "\"", null);
            assertEquals(1, results.length());
            results.close();

            // short and long field ranges

            // note: expected 2 results = n14 (cm:content) and n15 (cm:thumbnail)
            
            sDate = df.getSimpleDateFormat().format(date);
            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@cm\\:CrEaTeD:[MIN TO " + sDate + "]", null);
            assertEquals(2, results.length());
            assertTrue("n14 not in results", (results.getNodeRef(0).equals(n14) || results.getNodeRef(1).equals(n14)));
            assertTrue("n15 not in results", (results.getNodeRef(0).equals(n15) || results.getNodeRef(1).equals(n15)));
            results.close();
            
         
            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@cm\\:created:[MIN TO NOW]", null);
            assertEquals(2, results.length());
            assertTrue("n14 not in results", (results.getNodeRef(0).equals(n14) || results.getNodeRef(1).equals(n14)));
            assertTrue("n15 not in results", (results.getNodeRef(0).equals(n15) || results.getNodeRef(1).equals(n15)));
            results.close();

            
            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@cm\\:created:[MIN TO NOW]", null);
            assertEquals(2, results.length());           
            assertTrue("n14 not in results", (results.getNodeRef(0).equals(n14) || results.getNodeRef(1).equals(n14)));
            assertTrue("n15 not in results", (results.getNodeRef(0).equals(n15) || results.getNodeRef(1).equals(n15)));
            results.close();
            
            
            sDate = df.getSimpleDateFormat().format(date);
            results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(ContentModel.PROP_CREATED) + ":[MIN TO " + sDate + "]", null);
            assertEquals(2, results.length());
            assertTrue("n14 not in results", (results.getNodeRef(0).equals(n14) || results.getNodeRef(1).equals(n14)));
            assertTrue("n15 not in results", (results.getNodeRef(0).equals(n15) || results.getNodeRef(1).equals(n15)));
            results.close();
            
            // Date ranges
            // Test date collapses but date time does not

            if(sDate.length() >= 9)
            {
                sDate = df.getSimpleDateFormat().format(testDate);
                results = searcher.query(rootNodeRef.getStoreRef(), "lucene",
                        "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "date-ista")) + ":[" + sDate + " TO " + sDate + "]", null);
                assertEquals(1, results.length());
                results.close();

                sDate = df.getSimpleDateFormat().format(testDate);
                results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "date-ista")) + ":[MIN  TO " + sDate + "]", null);
                assertEquals(1, results.length());
                results.close();

                sDate = df.getSimpleDateFormat().format(testDate);
                results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "date-ista")) + ":[" + sDate + " TO MAX]", null);
                assertEquals(1, results.length());
                results.close();
            }

            if(!usesDateTimeAnalyser)
            {
                // with date time the result is indeterminate
                sDate = CachingDateFormat.getDateFormat().format(testDate);
                results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                        + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":[" + sDate + " TO " + sDate + "]", null);
                assertEquals(1, results.length());
                results.close();
            }

            sDate = CachingDateFormat.getDateFormat().format(testDate);
            results = searcher
                    .query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":[MIN TO " + sDate + "]", null);
            assertEquals(1, results.length());
            results.close();

          
            
            sDate = CachingDateFormat.getDateFormat().format(testDate);
            System.out.println("SD = " + sDate);
            System.out.println("D = " +  date);
            
            if(!usesDateTimeAnalyser)
            {
                results = searcher
                .query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":\"" + sDate+"\"", null);
                //TODO: Fix date time resolution - uses 000 MS
                assertEquals(1, results.length());
                results.close();
            }
            
            if(!usesDateTimeAnalyser)
            {
                results = searcher
                .query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":[" + sDate + " TO MAX]", null);
                assertEquals(1, results.length());
                results.close();
            }

            if (usesDateTimeAnalyser)
            {
                sDate = df.getSimpleDateFormat().format(testDate);
                System.out.println("Test Date = " + testDate);
                System.out.println("Formatted = " + sDate);

                for (long i : new long[] { 333, 20000, 20 * 60 * 1000, 8 * 60 * 60 * 1000, 10 * 24 * 60 * 60 * 1000, 4 * 30 * 24 * 60 * 60 * 1000,
                        10 * 12 * 30 * 24 * 60 * 60 * 1000 })
                {
                    System.out.println("I = "+i);
                    
                    String startDate = df.getSimpleDateFormat().format(new Date(testDate.getTime() - i));
                    System.out.println("\tStart = " + startDate);

                    String endDate = df.getSimpleDateFormat().format(new Date(testDate.getTime() + i));
                    System.out.println("\tEnd = " + endDate);

                    boolean equalsStart = startDate.equals(sDate);
                    boolean equalsEnd = endDate.equals(sDate);
                    boolean equalsStartOrEnd = equalsStart || equalsEnd; 
                  
                    
                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                            + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":[" + startDate + " TO " + endDate + "]", null);
                    assertEquals(1, results.length());
                    results.close();

                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                            + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":[" + sDate + " TO " + endDate + "]", null);
                    assertEquals(1, results.length());
                    results.close();

                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                            + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":[" + startDate + " TO " + sDate + "]", null);
                    assertEquals(1, results.length());
                    results.close();

                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                            + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":{" + sDate + " TO " + endDate + "}", null);
                    assertEquals(0 , results.length());
                    results.close();

                    results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                            + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":{" + startDate + " TO " + sDate + "}", null);
                    assertEquals(0 , results.length());
                    results.close();
                }
            }
        }

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "boolean-ista")) + ":\"true\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "qname-ista")) + ":\"{wibble}wobble\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "category-ista")) + ":\""
                + DefaultTypeConverter.INSTANCE.convert(String.class, new NodeRef(rootNodeRef.getStoreRef(), "CategoryId")) + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "noderef-ista")) + ":\"" + n1 + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "path-ista")) + ":\"" + nodeService.getPath(n3) + "\"",
                null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        // d:any

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "any-many-ista")) + ":\"100\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "any-many-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "any-many-ista")) + ":\"anyValueAsString\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "any-many-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "any-many-ista")) + ":\"nintc\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "any-many-ista")));
        results.close();

        // proximity searches

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~0", null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~1", null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~2", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~3", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Alfresco Tutorial\"", null);

        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Tutorial Alfresco\"", null);

        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Tutorial Alfresco\"~0", null);

        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Tutorial Alfresco\"~1", null);

        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Tutorial Alfresco\"~2", null);

        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Tutorial Alfresco\"~3", null);

        assertEquals(1, results.length());
        results.close();

        // multi ml text

        QName multimlQName = QName.createQName(TEST_NAMESPACE, "mltext-many-ista");

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":лемур");
        sp.addLocale(new Locale("ru"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":lemur");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":chou");
        sp.addLocale(new Locale("fr"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":cabbage");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":cabba*");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":ca*ge");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":*bage");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":cabage~");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":*b?ag?");
        sp.addLocale(new Locale("en"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(multimlQName.toString()) + ":cho*");
        sp.setMlAnalaysisMode(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES);
        sp.addLocale(new Locale("fr"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // multivalued content in type d:any
        // This should not be indexed as we can not know what to do with content here.

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(QName.createQName(TEST_NAMESPACE, "content-many-ista").toString()) + ":multicontent");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        // locale

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":\"en_GB_\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":en_GB_", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":en_*", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":*_GB_*", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":*_gb_*", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        // Period

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "period-ista")) + ":\"period|12\"", null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        // Type

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testType.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testType.toPrefixString(namespacePrefixResolver) + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testType.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testType.toPrefixString(namespacePrefixResolver) + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testSuperType.toString() + "\"", null);
        assertEquals(13, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testSuperType.toPrefixString(namespacePrefixResolver) + "\"", null);
        assertEquals(13, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + ContentModel.TYPE_CONTENT.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"cm:content\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"cm:CONTENT\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"CM:CONTENT\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"CONTENT\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"content\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + ContentModel.TYPE_THUMBNAIL.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\""
                + ContentModel.TYPE_THUMBNAIL.toString() + "\" TYPE:\"" + ContentModel.TYPE_CONTENT.toString() + "\"", null);
        assertEquals(2, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testSuperType.toString() + "\"", null);
        assertEquals(12, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testSuperType.toPrefixString(namespacePrefixResolver) + "\"", null);
        assertEquals(12, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toPrefixString(namespacePrefixResolver) + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toPrefixString(namespacePrefixResolver) + "\"", null);
        assertEquals(1, results.length());
        results.close();

        // Test for AR-384

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fox AND TYPE:\"" + ContentModel.PROP_CONTENT.toString() + "\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fox cm\\:name:fox", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fo AND TYPE:\"" + ContentModel.PROP_CONTENT.toString() + "\"", null);
        assertEquals(0, results.length());
        results.close();

        // Test stop words are equivalent

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"the\"", null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"and\"", null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"over the lazy\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"over a lazy\"", null);
        assertEquals(1, results.length());
        results.close();

        // Test wildcards in text

        //ALF-2389
        //results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":*en*", null);
        //assertEquals(0, results.length());
        //results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":*a*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":*A*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":\"*a*\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":\"*A\"*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":*s*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":*S*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":\"*s*\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"))+":\"*S\"*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*A*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"*a*\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"*A*\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*a*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*Z*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*z*", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"*Z*\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"*z*\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:laz*", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:laz~", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:la?y", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:?a?y", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*azy", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*az*", null);
        assertEquals(1, results.length());
        results.close();

        // Accents

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"\u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"aeidnouy\"", null);
        assertEquals(1, results.length());
        results.close();

        // FTS test

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"fox\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":\"fox\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".mimetype:\"text/plain\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".locale:\"en_GB\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".locale:en_*", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".locale:e*_GB", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".size:\"298\"", null);
        assertEquals(1, results.length());
        results.close();

        QName queryQName = QName.createQName("alf:test1", namespacePrefixResolver);
        results = searcher.query(rootNodeRef.getStoreRef(), queryQName, null);
        assertEquals(1, results.length());
        results.close();

        // Configuration of TEXT

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":\"fox\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"fox\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"fox\"");
        sp.addTextAttribute("@" + ContentModel.PROP_NAME.toString());
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp.addTextAttribute("@" + ContentModel.PROP_CONTENT.toString());
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"cabbage\"");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"cab*\"");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*bage\"");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*ba*\"");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        // term

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:cabbage");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:*cab*");
        sp.addTextAttribute("@" + orderText.toString());
        sp.addLocale(Locale.ENGLISH);
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:*bage");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:*ba*");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:dabbage~0.8");
        sp.addTextAttribute("@" + orderText.toString());
        results = searcher.query(sp);
        assertEquals(13, results.length());
        results.close();

        // Wild cards in TEXT phrase

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfresc?\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfres??\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfre???\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfr????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alf?????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"al??????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"a???????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"a??re???\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"?lfresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"??fresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"???resco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"???res?o\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"????e?co\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"????e?c?\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"???????o\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"???re???\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfresc*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfres*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfre*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfr*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alf*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"al*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"a*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"a****\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*lfresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*fresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*resco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*esco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*sco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*co\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*o\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"****lf**sc***\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*??*lf**sc***\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alfresc*tutorial\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"alf* tut*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("TEXT:\"*co *al\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // Wild cards in ML phrases

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfresc?\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfres??\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfre???\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfr????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alf?????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"al??????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"a???????\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"a??re???\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"?lfresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"??fresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"???resco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"???res?o\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"????e?co\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"????e?c?\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"???????o\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"???re???\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfresc*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfres*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfre*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfr*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alf*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"al*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"a*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"a****\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*lfresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*fresco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*resco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*esco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*sco\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*co\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*o\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"****lf**sc***\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*??*lf**sc***\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Alfresc*tutorial\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"Alf* tut*\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"*co *al\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // ALL and its configuration

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ALL:\"fox\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ALL:\"fox\"");
        sp.addAllAttribute("@" + ContentModel.PROP_NAME.toString());
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp.addAllAttribute("@" + ContentModel.PROP_CONTENT.toString());
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ALL:\"5.6\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // Search by data type

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("d\\:double:\"5.6\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("d\\:content:\"fox\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // locale serach in en_US for en_UK

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("d\\:content:\"fox\"");
        sp.addLocale(Locale.US);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // Direct ML tests

        QName mlQName = QName.createQName(TEST_NAMESPACE, "ml");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setMlAnalaysisMode(MLAnalysisMode.ALL_ONLY);
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":and");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setMlAnalaysisMode(MLAnalysisMode.ALL_ONLY);
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":\"and\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setMlAnalaysisMode(MLAnalysisMode.ALL_ONLY);
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        sp.addLocale(Locale.UK);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        sp.setMlAnalaysisMode(MLAnalysisMode.LOCALE_AND_ALL_CONTAINING_LOCALES);
        sp.addLocale(Locale.UK);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        sp.addLocale(Locale.ENGLISH);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banane");
        sp.addLocale(Locale.FRENCH);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":香蕉");
        sp.addLocale(Locale.CHINESE);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banaan");
        sp.addLocale(new Locale("nl"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banane");
        sp.addLocale(Locale.GERMAN);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":μπανάνα");
        sp.addLocale(new Locale("el"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        sp.addLocale(Locale.ITALIAN);
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":�?ナナ");
        sp.addLocale(new Locale("ja"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":바나나");
        sp.addLocale(new Locale("ko"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":banana");
        sp.addLocale(new Locale("pt"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":банан");
        sp.addLocale(new Locale("ru"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":plátano");
        sp.addLocale(new Locale("es"));
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        // Test ISNULL/ISUNSET/ISNOTNULL

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISUNSET:\"" + QName.createQName(TEST_NAMESPACE, "null").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNULL:\"" + QName.createQName(TEST_NAMESPACE, "null").toString() + "\"");
        results = searcher.query(sp);
        // assertEquals(62, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISUNSET:\"" + QName.createQName(TEST_NAMESPACE, "path-ista").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNULL:\"" + QName.createQName(TEST_NAMESPACE, "path-ista").toString() + "\"");
        results = searcher.query(sp);
        // assertEquals(61, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNOTNULL:\"" + QName.createQName(TEST_NAMESPACE, "null").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNOTNULL:\"" + QName.createQName(TEST_NAMESPACE, "path-ista").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISUNSET:\"" + QName.createQName(TEST_NAMESPACE, "aspectProperty").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNULL:\"" + QName.createQName(TEST_NAMESPACE, "aspectProperty").toString() + "\"");
        results = searcher.query(sp);
        // assertEquals(62, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNOTNULL:\"" + QName.createQName(TEST_NAMESPACE, "aspectProperty").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        // Test non field queries

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fox", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fo*", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:f*x", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*ox", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":fox", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":fo*", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":f*x", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":*ox", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":fox",
                null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":fo*",
                null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":f*x",
                null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":*ox",
                null);
        assertEquals(1, results.length());
        results.close();

        // Parameters

        queryQName = QName.createQName("alf:test2", namespacePrefixResolver);
        results = searcher.query(rootNodeRef.getStoreRef(), queryQName, null);
        assertEquals(1, results.length());
        results.close();

        queryQName = QName.createQName("alf:test2", namespacePrefixResolver);
        QueryParameter qp = new QueryParameter(QName.createQName("alf:banana", namespacePrefixResolver), "woof");
        results = searcher.query(rootNodeRef.getStoreRef(), queryQName, new QueryParameter[] { qp });
        assertEquals(0, results.length());
        results.close();

        queryQName = QName.createQName("alf:test3", namespacePrefixResolver);
        qp = new QueryParameter(QName.createQName("alf:banana", namespacePrefixResolver), "/one/five//*");
        results = searcher.query(rootNodeRef.getStoreRef(), queryQName, new QueryParameter[] { qp });
        assertEquals(7, results.length());
        results.close();

        // TODO: should not have a null property type definition
        QueryParameterDefImpl paramDef = new QueryParameterDefImpl(QName.createQName("alf:lemur", namespacePrefixResolver), (DataTypeDefinition) null, true, "fox");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"${alf:lemur}\"", new QueryParameterDefinition[] { paramDef });
        assertEquals(1, results.length());
        results.close();

        paramDef = new QueryParameterDefImpl(QName.createQName("alf:intvalue", namespacePrefixResolver), (DataTypeDefinition) null, true, "1");
        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"${alf:intvalue}\"", new QueryParameterDefinition[] { paramDef });
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        // Open ended ranges

        qname = QName.createQName("{namespace}property-1");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[v TO w]", null);
        assertEquals(2, results.length());
        results.close();

        qname = QName.createQName("{namespace}property-1");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[v TO \uFFFF]", null);
        assertEquals(2, results.length());
        results.close();

        qname = QName.createQName("{namespace}property-1");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[\u0000 TO w]", null);
        assertEquals(2, results.length());
        results.close();
    }

    /**
     * @throws Exception
     */
    public void testPathSearch() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        // //*

        ResultSet

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//common\"", null);
        assertEquals(7, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one//common\"", null);
        assertEquals(5, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//*\"", null);
        assertEquals(10, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//.\"", null);
        assertEquals(11, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testXPathSearch() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        // //*

        ResultSet

        results = searcher.query(rootNodeRef.getStoreRef(), "xpath", "//./*", null);
        assertEquals(15, results.length());
        results.close();
        luceneFTS.resume();

        QueryParameterDefinition paramDef = new QueryParameterDefImpl(QName.createQName("alf:query", namespacePrefixResolver), (DataTypeDefinition) null, true, "//./*");
        results = searcher.query(rootNodeRef.getStoreRef(), "xpath", "${alf:query}", new QueryParameterDefinition[] { paramDef });
        assertEquals(15, results.length());
        results.close();
    }

    /**
     * @throws Exception
     */
    public void testMissingIndex() throws Exception
    {
        luceneFTS.pause();
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "_missing_");
        ADMLuceneSearcherImpl searcher = buildSearcher();

        // //*

        ResultSet

        results = searcher.query(storeRef, "xpath", "//./*", null);
        assertEquals(0, results.length());
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testUpdateIndex() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        runBaseTests();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        indexer.updateNode(rootNodeRef);
        indexer.updateNode(n1);
        indexer.updateNode(n2);
        indexer.updateNode(n3);
        indexer.updateNode(n4);
        indexer.updateNode(n5);
        indexer.updateNode(n6);
        indexer.updateNode(n7);
        indexer.updateNode(n8);
        indexer.updateNode(n9);
        indexer.updateNode(n10);
        indexer.updateNode(n11);
        indexer.updateNode(n12);
        indexer.updateNode(n13);
        indexer.updateNode(n14);

        indexer.commit();

        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testDeleteLeaf() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        testTX.commit();

        // Delete

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        nodeService.deleteNode(n14);
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//.\"", null);
        assertEquals(19, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*\"", null);
        assertEquals(18, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/.\"", null);
        assertEquals(18, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/./.\"", null);
        assertEquals(18, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//./*\"", null);
        assertEquals(18, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//././*/././.\"", null);
        assertEquals(18, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(0, results.length());
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testAddEscapedChild() throws Exception
    {
        String COMPLEX_LOCAL_NAME = "\u0020\u0060\u00ac\u00a6\u0021\"\u00a3\u0024\u0025\u005e\u0026\u002a\u0028\u0029\u002d\u005f\u003d\u002b\t\n\\\u0000\u005b\u005d\u007b\u007d\u003b\u0027\u0023\u003a\u0040\u007e\u002c\u002e\u002f\u003c\u003e\u003f\\u007c\u005f\u0078\u0054\u0036\u0035\u0041\u005f";
        if (dialect instanceof PostgreSQLDialect)
        {
            // Note: PostgreSQL does not support \u0000 char embedded in a string
            // http://archives.postgresql.org/pgsql-jdbc/2007-02/msg00115.php
            COMPLEX_LOCAL_NAME = "\u0020\u0060\u00ac\u00a6\u0021\"\u00a3\u0024\u0025\u005e\u0026\u002a\u0028\u0029\u002d\u005f\u003d\u002b\t\n\\\u005b\u005d\u007b\u007d\u003b\u0027\u0023\u003a\u0040\u007e\u002c\u002e\u002f\u003c\u003e\u003f\\u007c\u005f\u0078\u0054\u0036\u0035\u0041\u005f";
        }

        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        ChildAssociationRef car = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}" + COMPLEX_LOCAL_NAME), testSuperType);
        indexer.createNode(car);

        indexer.commit();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:" + ISO9075.encode(COMPLEX_LOCAL_NAME) + "\"", null);
        assertEquals(1, results.length());
        results.close();
    }

    /**
     * @throws Exception
     */
    public void testNumericInPath() throws Exception
    {
        String COMPLEX_LOCAL_NAME = "Woof12";

        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        ChildAssociationRef car = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}" + COMPLEX_LOCAL_NAME), testSuperType);
        indexer.createNode(car);

        indexer.commit();

        ADMLuceneSearcherImpl searcher =  buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:" + ISO9075.encode(COMPLEX_LOCAL_NAME) + "\"", null);
        assertEquals(1, results.length());
        results.close();
    }

    /**
     * @throws Exception
     */
    public void testDeleteContainer() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        testTX.commit();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        nodeService.deleteNode(n13);
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/.\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/./.\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//./*\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//././*/././.\"", null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(0, results.length());
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testDeleteAndAddReference() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        nodeService.removeChild(n2, n13);
        indexer.deleteChildRelationship(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("{namespace}link"), n13));

        indexer.commit();

        ADMLuceneSearcherImpl searcher = buildSearcher();
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/namespace:*/namespace:*/namespace:*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/*/*/*\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//.\"", null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/.\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//*/./.\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//./*\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//././*/././.\"", null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//common\"", null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one//common\"", null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//*\"", null);
        assertEquals(10, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"/one/five//.\"", null);
        assertEquals(11, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null);
        assertEquals(1, results.length());
        results.close();

        indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        nodeService.addChild(n2, n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}link"));
        indexer.createChildRelationship(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("{namespace}link"), n13));

        indexer.commit();

        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testRenameReference() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:link//.\"", null);
        assertEquals(3, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//namespace:link//.\"", null);
        assertEquals(4, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:renamed_link//.\"", null);
        assertEquals(0, results.length());
        results.close();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);

        nodeService.removeChild(n2, n13);
        nodeService.addChild(n2, n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}renamed_link"));

        indexer.updateChildRelationship(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("namespace", "link"), n13), new ChildAssociationRef(ASSOC_TYPE_QNAME, n2,
                QName.createQName("namespace", "renamed_link"), n13));

        indexer.commit();

        runBaseTests();

        searcher = buildSearcher();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:link//.\"", null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:renamed_link//.\"", null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATHWITHREPEATS:\"//namespace:renamed_link//.\"", null);
        assertEquals(4, results.length());
        results.close();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testDelayIndex() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"KEYONE\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"KEYUNSTORED\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"KEYTWO\"", null);
        assertEquals(0, results.length());
        results.close();

        // Do index

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);
        indexer.updateFullTextSearch(1000);
        indexer.prepare();
        indexer.commit();

        searcher = buildSearcher();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"keyone\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"keytwo\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"keyunstored\"", null);
        assertEquals(1, results.length());
        results.close();

        runBaseTests();
        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testWaitForIndex() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"KEYONE\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"KEYUNSTORED\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"KEYTWO\"", null);
        assertEquals(0, results.length());
        results.close();

        // Do index

        searcher = buildSearcher();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"keyone\"", null);
        assertEquals(1, results.length());
        results.close();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);
        indexer.updateFullTextSearch(1000);
        indexer.prepare();
        indexer.commit();

        luceneFTS.resume();
        // luceneFTS.requiresIndex(rootNodeRef.getStoreRef());
        // luceneFTS.index();
        // luceneFTS.index();
        // luceneFTS.index();

        Thread.sleep(35000);

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"keytwo\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"KEYUNSTORED\"", null);
        assertEquals(1, results.length());
        results.close();

        runBaseTests();
    }

    private String escapeQName(QName qname)
    {
        return LuceneQueryParser.escape(qname.toString());
    }

    /**
     * @throws Exception
     */
    public void testForKev() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PARENT:\"" + rootNodeRef.toString() + "\"", null);
        assertEquals(5, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PARENT:\"" + rootNodeRef.toString() + "\" +QNAME:\"one\"", null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene",
                "( +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}name:\"content woof\") OR  TEXT:\"content\"",
                null);

        luceneFTS.resume();
    }

    /**
     * @throws Exception
     */
    public void testIssueAR47() throws Exception
    {
        // This bug arose from repeated deletes and adds creating empty index
        // segments.
        // Two segements each containing one deletyed entry were merged together
        // producing a single empty entry.
        // This seemed to be bad for lucene - I am not sure why

        // So we add something, add and delete someting repeatedly and then
        // check we can still do the search.

        // Running in autocommit against the index
        testTX.commit();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        ChildAssociationRef testFind = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}testFind"), testSuperType);
        tx.commit();

        ADMLuceneSearcherImpl searcher = buildSearcher();

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();

        for (int i = 0; i < 100; i++)
        {
            UserTransaction tx1 = transactionService.getUserTransaction();
            tx1.begin();
            ChildAssociationRef test = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"), testSuperType);
            tx1.commit();

            UserTransaction tx2 = transactionService.getUserTransaction();
            tx2.begin();
            nodeService.deleteNode(test.getChildRef());
            tx2.commit();
        }

        UserTransaction tx3 = transactionService.getUserTransaction();
        tx3.begin();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();
        tx3.commit();
    }

    // public void testMany() throws Exception
    // {
    // for(int i = 0; i < 100; i++)
    // {
    // testReadAgainstDelta();
    // System.out.println("At "+i);
    // testTX.rollback();
    // testTX = transactionService.getUserTransaction();
    // testTX.begin();
    // }
    // }

    /**
     * @throws Exception
     */
    public void testReadAgainstDelta() throws Exception
    {
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();
        testTX.commit();

        // Delete

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        runBaseTests();

        serviceRegistry.getNodeService().deleteNode(n1);
        assertFalse(serviceRegistry.getNodeService().exists(n13));

        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        ResultSet results = serviceRegistry.getSearchService().query(sp);
        assertEquals(6, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addSort("ID", true);
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

        // Delete

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        runBaseTests();

        serviceRegistry.getNodeService().deleteNode(n2);
        assertTrue(serviceRegistry.getNodeService().exists(n13));
        assertFalse(serviceRegistry.getNodeService().exists(n7));

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(13, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addSort("ID", true);
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

        // Create

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        runBaseTests();

        assertEquals(5, serviceRegistry.getNodeService().getChildAssocs(rootNodeRef).size());
        serviceRegistry.getNodeService().createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}texas"), testSuperType).getChildRef();
        assertEquals(6, serviceRegistry.getNodeService().getChildAssocs(rootNodeRef).size());

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(17, results.length());
        results.close();

        testTX.rollback();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addSort("ID", true);
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

        // update property

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        runBaseTests();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("\\@\\{namespace\\}property\\-1:\"valueone\"");
        sp.addSort("ID", true);
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);

        assertEquals(2, results.length());
        results.close();

        nodeService.setProperty(n1, QName.createQName("{namespace}property-1"), "Different");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("\\@\\{namespace\\}property\\-1:\"valueone\"");
        sp.addSort("ID", true);
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);

        assertEquals(1, results.length());
        results.close();

        testTX.rollback();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("\\@\\{namespace\\}property\\-1:\"valueone\"");
        sp.excludeDataInTheCurrentTransaction(false);
        sp.addSort("ID", true);
        results = serviceRegistry.getSearchService().query(sp);

        assertEquals(2, results.length());
        results.close();

        testTX.rollback();

        // Add and delete

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        runBaseTests();

        serviceRegistry.getNodeService().deleteNode(n1);

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(6, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        NodeRef created = serviceRegistry.getNodeService().createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}texas"), testSuperType).getChildRef();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(7, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        serviceRegistry.getNodeService().deleteNode(created);

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(6, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

        testTX.rollback();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.addSort("ID", true);
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(16, results.length());
        results.close();

    }

    private void runPerformanceTest(double time, boolean clear)
    {
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                luceneConfig);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.setTransactionService(transactionService);
        if (clear)
        {
            // indexer.clearIndex();
        }
        indexer.createNode(new ChildAssociationRef(null, null, null, rootNodeRef));

        long startTime = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < 10000000; i++)
        {
            if (i % 10 == 0)
            {
                if (System.currentTimeMillis() - startTime > time)
                {
                    count = i;
                    break;
                }
            }

            QName qname = QName.createQName("{namespace}a_" + i);
            NodeRef ref = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME, qname, ContentModel.TYPE_CONTAINER).getChildRef();
            indexer.createNode(new ChildAssociationRef(ASSOC_TYPE_QNAME, rootNodeRef, qname, ref));

        }
        indexer.commit();
        float delta = ((System.currentTimeMillis() - startTime) / 1000.0f);
        // System.out.println("\tCreated " + count + " in " + delta + " = " +
        // (count / delta));
    }

    private NamespacePrefixResolver getNamespacePrefixResolver(String defaultURI)
    {
        DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver(null);
        nspr.registerNamespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI);
        nspr.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        nspr.registerNamespace(NamespaceService.DICTIONARY_MODEL_PREFIX, NamespaceService.DICTIONARY_MODEL_1_0_URI);
        nspr.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        nspr.registerNamespace("namespace", "namespace");
        nspr.registerNamespace("test", TEST_NAMESPACE);
        nspr.registerNamespace(NamespaceService.DEFAULT_PREFIX, defaultURI);
        return nspr;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        ADMLuceneTest test = new ADMLuceneTest();
        test.setUp();
        // test.testForKev();
        // test.testDeleteContainer();

        // test.testReadAgainstDelta();

        NodeRef targetNode = test.rootNodeRef;
        Path path = test.serviceRegistry.getNodeService().getPath(targetNode);

        SearchParameters sp = new SearchParameters();
        sp.addStore(test.rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"" + path + "//." + "\"");
        ResultSet results = test.serviceRegistry.getSearchService().query(sp);

        results.close();

        // test.dictionaryService.getType(test.nodeService.getType(test.rootNodeRef)).getDefaultAspects();
    }

    /**
     * @author andyh
     */
    public static class UnknownDataType implements Serializable
    {

        /**
         *
         */
        private static final long serialVersionUID = -6729690518573349055L;

    }
}
