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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.search.impl.lucene.ADMLuceneTest.UnknownDataType;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.CachingDateFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Andy
 *
 */
public class DBQueryTest  implements DictionaryListener
{
    protected static ApplicationContext ctx = null;
    
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/lucenetest";

    private static final QName ASSOC_TYPE_QNAME = QName.createQName(TEST_NAMESPACE, "assoc");

    private static QName CREATED_DATE = QName.createQName(TEST_NAMESPACE, "createdDate");
    
    private static QName ORDER_DOUBLE = QName.createQName(TEST_NAMESPACE, "orderDouble");

    private static QName ORDER_FLOAT = QName.createQName(TEST_NAMESPACE, "orderFloat");

    private static QName ORDER_LONG = QName.createQName(TEST_NAMESPACE, "orderLong");

    private static QName ORDER_INT = QName.createQName(TEST_NAMESPACE, "orderInt");

    private static QName ORDER_TEXT = QName.createQName(TEST_NAMESPACE, "orderText");

    private static QName ORDER_ML_TEXT = QName.createQName(TEST_NAMESPACE, "orderMLText");

    private static QName ASPECT_WITH_CHILDREN = QName.createQName(TEST_NAMESPACE, "aspectWithChildren");
    
    private static  QName TEST_CONTENT_TYPE = QName.createQName(TEST_NAMESPACE, "testContentType");

    private static  QName TEST_SUPER_CONTENT_TYPE = QName.createQName(TEST_NAMESPACE, "testSuperContentType");
    
    private static  QName TEST_FOLDER_TYPE = QName.createQName(TEST_NAMESPACE, "testFolderType");

    private static  QName TEST_SUPER_FOLDER_TYPE = QName.createQName(TEST_NAMESPACE, "testSuperFolderType");

    private static  QName TEST_ASPECT = QName.createQName(TEST_NAMESPACE, "testAspect");

    private static  QName TEST_SUPER_ASPECT = QName.createQName(TEST_NAMESPACE, "testSuperAspect");

    
    TransactionService transactionService;

    RetryingTransactionHelper retryingTransactionHelper;

    NodeService nodeService;

    DictionaryService dictionaryService;

    TenantService tenantService;
    
    private DictionaryDAO dictionaryDAO;
    
    private NamespaceDAO namespaceDao;
    
    private ServiceRegistry serviceRegistry;
    
    private AuthenticationComponent authenticationComponent;
    
    private DictionaryNamespaceComponent namespacePrefixResolver;
    
    private UserTransaction txn;
    
    private M2Model model;

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
    
    private NodeRef n15;
    
    private Date testDate;

    private String formattedTestDate;

    private String midCreationDate;

    private String midModificationDate;

    private String midOrderDate;

    private ContentService contentService;

    protected static void startContext()
    {
        ctx = ApplicationContextHelper.getApplicationContext();
    }

    protected static void stopContext()
    {
        ApplicationContextHelper.closeApplicationContext();     
    }
    
    @BeforeClass
    public static void beforeTests()
    {
        startContext();
    }

    
    @AfterClass
    public static void afterTests()
    {
        stopContext();      
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
    
    @Before
    public void setup() throws Exception
    {
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        namespacePrefixResolver = (DictionaryNamespaceComponent) ctx.getBean("namespaceService");
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        retryingTransactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");
        tenantService = (TenantService) ctx.getBean("tenantService");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        namespaceDao = (NamespaceDAO) ctx.getBean("namespaceDAO");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        contentService = (ContentService) ctx.getBean("contentService");

        loadTestModel();
        createTestData();
    }
    
    protected void loadTestModel()
    {
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/search/impl/MetadataQueryTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDAO.registerListener(this);
        dictionaryDAO.reset();
        assertNotNull(dictionaryDAO.getClass(TEST_SUPER_CONTENT_TYPE));
    }
    
    protected void createTestData() throws Exception
    {
        I18NUtil.setLocale(Locale.UK);
        txn = transactionService.getUserTransaction();
        txn.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}top"), TEST_SUPER_FOLDER_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n1,  ContentModel.PROP_NAME, "Folder_1");
    
        n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), TEST_FOLDER_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n2,  ContentModel.PROP_NAME, "Folder 2");

        n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), TEST_SUPER_CONTENT_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n3,  ContentModel.PROP_NAME, "Content 3");

        Map<QName, Serializable> testProperties = new HashMap<QName, Serializable>();
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic"), "TEXT THAT IS INDEXED STORED AND TOKENISED ATOMICALLY KEYONE");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic"), "TEXT THAT IS INDEXED STORED AND TOKENISED ATOMICALLY KEYUNSTORED");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic"), "TEXT THAT IS INDEXED STORED AND TOKENISED BUT NOT ATOMICALLY KEYTWO");
        testProperties.put(QName.createQName(TEST_NAMESPACE, "int-ista"), Integer.valueOf(1));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "long-ista"), Long.valueOf(2));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "float-ista"), Float.valueOf(3.4f));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "double-ista"), Double.valueOf(5.6));
        Calendar c = new GregorianCalendar();
        c.setTime(new Date(((new Date().getTime() - 10000))));
     
        testDate = c.getTime();
        formattedTestDate = CachingDateFormat.getDateFormat().format(testDate);
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
        mlText.addValue(new Locale("ja"), "バナナ");
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

        testProperties.put(ContentModel.PROP_NAME, "Node 4");
        
        n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}four"), TEST_CONTENT_TYPE, testProperties).getChildRef();
        nodeService.setProperty(n4,  ContentModel.PROP_NAME, "Content 4");

        n5 = nodeService.createNode(n1, ASSOC_TYPE_QNAME, QName.createQName("{namespace}five"), TEST_SUPER_FOLDER_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n5,  ContentModel.PROP_NAME, "Folder 5");
        n6 = nodeService.createNode(n1, ASSOC_TYPE_QNAME, QName.createQName("{namespace}six"), TEST_SUPER_FOLDER_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n6,  ContentModel.PROP_NAME, "Folder 6");
        
        synchronized (this)
        {
            wait(1000);
        }
       
        midOrderDate = DefaultTypeConverter.INSTANCE.convert(String.class, orderDate);
        
        n7 = nodeService.createNode(n2, ASSOC_TYPE_QNAME, QName.createQName("{namespace}seven"), TEST_SUPER_CONTENT_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n7,  ContentModel.PROP_NAME, "Content 7");

        midCreationDate = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(n7,  ContentModel.PROP_CREATED));
        midModificationDate = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(n7,  ContentModel.PROP_MODIFIED));
        
        synchronized (this)
        {
            wait(1000);
        }
        
        n8 = nodeService.createNode(n2, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eight-2"), TEST_SUPER_CONTENT_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n8,  ContentModel.PROP_NAME, "Content 8");
        n9 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}nine"), TEST_SUPER_CONTENT_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n9,  ContentModel.PROP_NAME, "Content 9");
        n10 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}ten"), TEST_SUPER_CONTENT_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n10,  ContentModel.PROP_NAME, "Content 10");
        n11 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eleven"), TEST_SUPER_CONTENT_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n11,  ContentModel.PROP_NAME, "Content 11");
        n12 = nodeService.createNode(n5, ASSOC_TYPE_QNAME, QName.createQName("{namespace}twelve"), TEST_SUPER_FOLDER_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n12,  ContentModel.PROP_NAME, "Folder 12");
        n13 = nodeService.createNode(n12, ASSOC_TYPE_QNAME, QName.createQName("{namespace}thirteen"), TEST_SUPER_FOLDER_TYPE, getOrderProperties()).getChildRef();
        nodeService.setProperty(n13,  ContentModel.PROP_NAME, "Folder 13");

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        MLText desc1 = new MLText();
        desc1.addValue(Locale.ENGLISH, "Alfresco tutorial");
        desc1.addValue(Locale.US, "Alfresco tutorial");
        
        Date explicitCreatedDate = new Date();
        Thread.sleep(2000);
        
        properties.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties.put(ContentModel.PROP_DESCRIPTION, desc1);
        properties.put(ContentModel.PROP_CREATED, explicitCreatedDate);
        n14 = nodeService.createNode(n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}fourteen"), ContentModel.TYPE_CONTENT, properties).getChildRef();
        nodeService.setProperty(n14,  ContentModel.PROP_NAME, "Content 14");
        ContentWriter writer = contentService.getWriter(n14, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        writer.putContent("12345678");
        
        n15 = nodeService.createNode(n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}fifteen"), ContentModel.TYPE_THUMBNAIL, getOrderProperties()).getChildRef();
        nodeService.setProperty(n15,  ContentModel.PROP_NAME, "Content 15");
        
        nodeService.addChild(rootNodeRef, n8, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight-0"));
        nodeService.addChild(n1, n8, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eight-1"));
        nodeService.addChild(n2, n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}link"));

        nodeService.addChild(n1, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n2, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n5, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n6, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n12, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n13, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));

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
        testProperties.put(CREATED_DATE, orderDate);
        testProperties.put(ORDER_DOUBLE, orderDoubleCount);
        testProperties.put(ORDER_FLOAT, orderFloatCount);
        testProperties.put(ORDER_LONG, orderLongCount);
        testProperties.put(ORDER_INT, orderIntCount);
        testProperties.put(ORDER_TEXT, new String(new char[] { (char) ('a' + orderTextCount) }) + " cabbage");

        MLText mlText = new MLText();
        mlText.addValue(Locale.ENGLISH, new String(new char[] { (char) ('a' + orderTextCount) }) + " banana");
        mlText.addValue(Locale.FRENCH, new String(new char[] { (char) ('Z' - orderTextCount) }) + " banane");
        mlText.addValue(Locale.CHINESE, new String(new char[] { (char) ('香' + orderTextCount) }) + " 香蕉");
        testProperties.put(ORDER_ML_TEXT, mlText);

        orderDate = Duration.subtract(orderDate, new Duration("P1D"));
        orderDoubleCount += 0.1d;
        orderFloatCount += 0.82f;
        orderLongCount += 299999999999999l;
        orderIntCount += 8576457;
        orderTextCount++;
        return testProperties;
    }

    
    @After
    public void teardown() throws Exception
    {
        if (txn.getStatus() == Status.STATUS_ACTIVE)
        {
            txn.rollback();
        }
        
    }
    
    @Test
    public void testCmisSql() throws InterruptedException
    {
        sqlQueryWithCount("SELECT * FROM cmis:document", 8);
        sqlQueryWithCount("SELECT * FROM cm:thumbnail", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder", 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperContentType", 7);
        sqlQueryWithCount("SELECT * FROM test:testContentType", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperFolderType", 6);
        sqlQueryWithCount("SELECT * FROM test:testFolderType", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect", 14);
        sqlQueryWithCount("SELECT * FROM test:testAspect", 2);
        
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'Folder_1'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'Folder 2'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 3'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 4'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'Folder 5'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'Folder 6'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 7'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 8'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 9'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 10'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 11'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'Folder 12'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'Folder 13'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 14'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name = 'Content 15'", 0);
        sqlQueryWithCount("SELECT * FROM cm:thumbnail where cmis:name = 'Content 15'", 1);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name <> 'Content 7'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name < 'Content 7'", 5);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name <= 'Content 7'", 6);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name > 'Content 7'", 2);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name >= 'Content 7'", 3);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name IN ('Content 3', 'Content 4')", 2);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name NOT IN ('Content 3', 'Content 4')", 6);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name like 'Content _'", 5);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name like 'Content __'", 3);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name not like 'Content __'", 5);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name like 'Content%'", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name like 'Content%4'", 2);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name is not null", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:name is null", 0);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where IN_FOLDER('"+n2+"')", 3);
        sqlQueryWithCount("SELECT * FROM cmis:folder where IN_FOLDER('"+n2+"')", 1);
        
        //sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'folder_1'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where LOWER(cmis:name) = 'folder_1'", 1);
        //sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name = 'FOLDER_1'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where UPPER(cmis:name) = 'FOLDER_1'", 1);
        
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name like 'Folder 1'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name like 'Folder 2'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name like 'Folder_1'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name like 'Folder_2'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name like 'Folder\\_1'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:name like 'Folder\\_2'", 0);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where IN_FOLDER('"+n2+"') and cmis:name = 'Content 7'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where IN_FOLDER('"+n2+"') and cmis:name = 'Content 8'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where IN_FOLDER('"+n2+"') and cmis:name = 'Content 14'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where IN_FOLDER('"+n2+"') and  not cmis:name = 'Content 8'", 2);
        
        sqlQueryWithCount("SELECT * FROM cmis:document d join test:testContentType a on d.cmis:objectId = a.cmis:objectId", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document d join test:testSuperContentType a on d.cmis:objectId = a.cmis:objectId", 7);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:creationDate = '"+midCreationDate+"'", 1);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:lastModificationDate = '"+midModificationDate+"'", 1);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:createdBy = 'System'", 8);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:lastModifiedBy = 'System'", 8);
        
        sqlQueryWithCount("SELECT * FROM cmis:document d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:createdDate = '"+midOrderDate+"'", 1);
        
        try
        {
            sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderDouble = -0.11", 1);
            fail();
        }
        catch(Exception e)
        {
            
        }
        
        try
        {
            sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderFloat = -3.5556", 1);
            fail();
        }
        catch(Exception e)
        {
            
        }
        
        long longValue = -1999999999999999l + (299999999999999l * 6);
        
        sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderLong = -1999999999999999", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong = "+longValue, 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong <>  "+longValue, 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong <  "+longValue, 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong <=  "+longValue, 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong >  "+longValue, 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong >=  "+longValue, 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong IN ( "+longValue+")", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong NOT IN  ("+longValue+")", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong is null", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderLong is not null", 13);
        
        long intValue = -45764576 + (8576457 * 6);
        
        sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderInt = -45764576", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt = "+intValue, 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt <>  "+intValue, 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt <  "+intValue, 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt <=  "+intValue, 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt >  "+intValue, 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt >=  "+intValue, 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt IN ( "+intValue+")", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt NOT IN  ("+intValue+")", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt is null", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderInt is not null", 13);
        
        String stringValue = new String(new char[] { (char) ('a' + 6) }) + " cabbage";
        
        sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderText = 'a cabbage'", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText = '"+stringValue+"'", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText <>  '"+stringValue+"'", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText <  '"+stringValue+"'", 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText <=  '"+stringValue+"'", 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText >  '"+stringValue+"'", 6);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText >=  '"+stringValue+"'", 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText IN ( '"+stringValue+"')", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText NOT IN  ('"+stringValue+"')", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText is null", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderText is not null", 13);
        
        // ML text is essentially multi-valued and gives unuusla results as ther is no locale constraint 
        
        stringValue = new String(new char[] { (char) ('a' + 6) }) + " banana";
        
        sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderMLText = 'a banana'", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText = '"+stringValue+"'", 1);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <>  '"+stringValue+"'", 12);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <  '"+stringValue+"'", 6);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <=  '"+stringValue+"'", 7);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText >  '"+stringValue+"'", 6);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText >=  '"+stringValue+"'", 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText IN ( '"+stringValue+"')", 1);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText NOT IN  ('"+stringValue+"')", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText is null", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText is not null", 13);
        
        stringValue = new String(new char[] { (char) ('Z' - 6) }) + " banane";
        
        sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderMLText = 'Z banane'", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText = '"+stringValue+"'", 1);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <>  '"+stringValue+"'", 12);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <  '"+stringValue+"'", 6);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <=  '"+stringValue+"'", 7);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText >  '"+stringValue+"'", 6);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText >=  '"+stringValue+"'", 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText IN ( '"+stringValue+"')", 1);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText NOT IN  ('"+stringValue+"')", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText is null", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText is not null", 13);
        
        stringValue = new String(new char[] { (char) ('香' + 6) }) + " 香蕉";
        
        sqlQueryWithCount("SELECT * FROM cmis:folder d join test:testSuperAspect a on d.cmis:objectId = a.cmis:objectId where a.test:orderMLText = '香 香蕉'", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText = '"+stringValue+"'", 1);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <>  '"+stringValue+"'", 12);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <  '"+stringValue+"'", 6);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText <=  '"+stringValue+"'", 7);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText >  '"+stringValue+"'", 6);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText >=  '"+stringValue+"'", 7);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText IN ( '"+stringValue+"')", 1);
//        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText NOT IN  ('"+stringValue+"')", 12);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText is null", 1);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a where a.test:orderMLText is not null", 13);
        
    }

    @Test
    public void testOrdering()
    {
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:name asc", 8, ContentModel.PROP_NAME, true);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:name asc", 6, ContentModel.PROP_NAME, true);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:name desc", 8, ContentModel.PROP_NAME, false);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:name desc", 6, ContentModel.PROP_NAME, false);
        
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:creationDate asc", 8, ContentModel.PROP_CREATED, true);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:creationDate asc", 6, ContentModel.PROP_CREATED, true);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:creationDate desc", 8, ContentModel.PROP_CREATED, false);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:creationDate desc", 6, ContentModel.PROP_CREATED, false);
        
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:lastModificationDate asc", 8, ContentModel.PROP_MODIFIED, true);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:lastModificationDate asc", 6, ContentModel.PROP_MODIFIED, true);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:lastModificationDate desc", 8, ContentModel.PROP_MODIFIED, false);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:lastModificationDate desc", 6, ContentModel.PROP_MODIFIED, false);
        
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:createdBy asc", 8, ContentModel.PROP_CREATOR, true);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:createdBy asc", 6, ContentModel.PROP_CREATOR, true);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:createdBy desc", 8, ContentModel.PROP_CREATOR, false);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:createdBy desc", 6, ContentModel.PROP_CREATOR, false);
        
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:lastModifiedBy asc", 8, ContentModel.PROP_MODIFIER, true);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:lastModifiedBy asc", 6, ContentModel.PROP_MODIFIER, true);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:lastModifiedBy desc", 8, ContentModel.PROP_MODIFIER, false);
        sqlQueryWithCount("SELECT * FROM cmis:folder order by cmis:lastModifiedBy desc", 6, ContentModel.PROP_MODIFIER, false);
       
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:createdDate asc", 14, CREATED_DATE, true);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:createdDate desc", 14, CREATED_DATE, false);
        
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:orderLong asc", 14, ORDER_LONG, true);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:orderLong desc", 14, ORDER_LONG, false);
        
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:orderInt asc", 14, ORDER_INT, true);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:orderInt desc", 14, ORDER_INT, false);
        
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:orderText asc", 14, ORDER_TEXT, true);
        sqlQueryWithCount("SELECT * FROM test:testSuperAspect a order by a.test:orderText desc", 14, ORDER_TEXT, false);
        
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:contentStreamMimeType asc", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:contentStreamMimeType desc", 8);
        
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:contentStreamLength asc", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document order by cmis:contentStreamLength desc", 8);
    }
    
    
    @Test
    public void testOtherCMIS()
    {
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:parentId = '"+ n2 + "'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:parentId IN ('"+ n2 + "')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:parentId <> '"+ n2 + "'", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:parentId NOT IN ('"+ n2 + "')", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:parentId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:parentId IS NOT NULL", 6);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamLength = 8", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName = 'Content 3'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName < 'Content 3'", 3);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName <= 'Content 3'", 4);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName > 'Content 3'", 4);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName >= 'Content 3'", 5);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName <> 'Content 3'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName IN ('Content 3', 'Content 4')", 2);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName NOT IN ('Content 3', 'Content 4')", 6);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamFileName IS NOT NULL", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType = 'text/plain'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType < 'text/plain'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType <= 'text/plain'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType > 'text/plain'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType >= 'text/plain'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType <> 'text/plain'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType IN ('text/plain')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType NOT IN ('text/plain')", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType IS NULL", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType IS NOT NULL", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:contentStreamMimeType like 'text%'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectId = '"+ n2 + "'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectId IN ('"+ n2 + "')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectId <> '"+ n2 + "'", 5);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectId NOT IN ('"+ n2 + "')", 5);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectId IS NOT NULL", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectTypeId = 'cmis:folder'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectTypeId IN ('cmis:folder')", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectTypeId <> 'cmis:folder'", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectTypeId NOT IN ('cmis:folder')", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectTypeId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:objectTypeId IS NOT NULL", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:baseTypeId = 'cmis:folder'", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:baseTypeId IN ('cmis:folder')", 6);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:baseTypeId <> 'cmis:folder'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:baseTypeId NOT IN ('cmis:folder')", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:baseTypeId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:folder where cmis:baseTypeId IS NOT NULL", 6);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId = '"+ n3 + "'", 1); 
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId IN ('"+ n3 + "')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId <> '"+ n3 + "'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId NOT IN ('"+ n3 + "')", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId = '"+ n3.getId() + "'", 1); 
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId IN ('"+ n3.getId() + "')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId <> '"+ n3.getId() + "'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId NOT IN ('"+ n3.getId() + "')", 7);
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId = '"+ n3 + ";1.0'", 1); 
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId IN ('"+ n3 + ";1.0')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId <> '"+ n3 + ";1.0'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId NOT IN ('"+ n3 + ";1.0')", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId = '"+ n3.getId() + ";1.0'", 1); 
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId IN ('"+ n3.getId() + ";1.0')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId <> '"+ n3.getId() + ";1.0'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId NOT IN ('"+ n3.getId() + ";1.0')", 7);
        
        
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectId IS NOT NULL", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectTypeId = 'cmis:document'", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectTypeId IN ('cmis:document')", 1);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectTypeId <> 'cmis:document'", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectTypeId NOT IN ('cmis:document')", 7);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectTypeId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:objectTypeId IS NOT NULL", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:baseTypeId = 'cmis:document'", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:baseTypeId IN ('cmis:document')", 8);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:baseTypeId <> 'cmis:document'", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:baseTypeId NOT IN ('cmis:document')", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:baseTypeId IS NULL", 0);
        sqlQueryWithCount("SELECT * FROM cmis:document where cmis:baseTypeId IS NOT NULL", 8);
    }
    
    public void sqlQueryWithCount(String query, int count)
    {
        sqlQueryWithCount(query, count, null, null);
    }
    
    public void sqlQueryWithCount(String query, int count, QName property, Boolean ascending)
    {
       queryWithCount(SearchService.LANGUAGE_CMIS_ALFRESCO, query, count, property, ascending);
    }
    
    @Test
    public void testAFTS()
    {
        aftsQueryWithCount("=TYPE:\"content\"", 8);
        aftsQueryWithCount("=TYPE:\"cm:content\"", 8);
        aftsQueryWithCount("=TYPE:\"cm:thumbnail\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:folder\"", 6);
        aftsQueryWithCount("=TYPE:\"test:testSuperContentType\"", 7);
        aftsQueryWithCount("=TYPE:\"test:testContentType\"", 1);
        aftsQueryWithCount("=TYPE:\"test:testSuperFolderType\"", 6);
        aftsQueryWithCount("=TYPE:\"test:testFolderType\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\"", 14);
        aftsQueryWithCount("=ASPECT:\"test:testAspect\"", 2);
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =name:\"Folder_1\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =name:\"Folder 2\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 3\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 4\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =name:\"Folder 5\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =name:\"Folder 6\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 7\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 8\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 9\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 10\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 11\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =name:\"Folder 12\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =name:\"Folder 13\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 14\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content 15\"", 0);
        aftsQueryWithCount("=TYPE:\"cm:thumbnail\" AND =name:\"Content 15\"", 1);
        
        aftsQueryWithCount("=TYPE:\"cm:content\" AND NOT =name:\"Content 7\"", 7);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND -=name:\"Content 7\"", 7);
        //aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content ?\"", 5);
        //aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content ??\"", 3);
        //aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content*\"", 8);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:Content*", 8);
        //aftsQueryWithCount("=TYPE:\"cm:content\" AND =name:\"Content*4\"", 2);
        //aftsQueryWithCount("=TYPE:\"cm:content\" AND =EXISTS:name", 8);
        
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =PARENT:\""+n2+"\" AND =name:\"Content 7\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =PARENT:\""+n2+"\" AND =name:\"Content 8\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =PARENT:\""+n2+"\" AND =name:\"Content 14\"", 1);
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =PARENT:\""+n2+"\" AND -=name:\"Content 8\"", 2);
        
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =created:\""+midCreationDate+"\"", 1);
        
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =modified:\""+midModificationDate+"\"", 1);
        
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =creator:System", 8);
        
        aftsQueryWithCount("=TYPE:\"cm:content\" AND =modifier:System", 8);
        
        long longValue = -1999999999999999l + (299999999999999l * 6);
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =ASPECT:\"test:testSuperAspect\" AND =test:orderLong:\"-1999999999999999\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\" AND =test:orderLong:\""+longValue+"\"", 1);
       
        long intValue = -45764576 + (8576457 * 6); 
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =ASPECT:\"test:testSuperAspect\" AND =test:orderInt:\"-45764576\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\" AND =test:orderInt:\""+intValue+"\"", 1);
        
        String stringValue = new String(new char[] { (char) ('a' + 6) }) + " cabbage";
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =ASPECT:\"test:testSuperAspect\" AND =test:orderText:\"a cabbage\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\" AND =test:orderText:\""+stringValue+"\"", 1);
        
        stringValue = new String(new char[] { (char) ('a' + 6) }) + " banana";
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =ASPECT:\"test:testSuperAspect\" AND =test:orderMLText:\"a banana\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\" AND =test:orderMLText:\""+stringValue+"\"", 1);
        
        stringValue = new String(new char[] { (char) ('Z' - 6) }) + " banane";
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =ASPECT:\"test:testSuperAspect\" AND =test:orderMLText:\"Z banane\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\" AND =test:orderMLText:\""+stringValue+"\"", 1);
        
        stringValue = new String(new char[] { (char) ('香' + 6) }) + " 香蕉";
        
        aftsQueryWithCount("=TYPE:\"cm:folder\" AND =ASPECT:\"test:testSuperAspect\" AND =test:orderMLText:\"香 香蕉\"", 1);
        aftsQueryWithCount("=ASPECT:\"test:testSuperAspect\" AND =test:orderMLText:\""+stringValue+"\"", 1);
        
    }
    
    
    public void aftsQueryWithCount(String query, int count)
    {
        queryWithCount(SearchService.LANGUAGE_FTS_ALFRESCO, query, count, null, null);
    }
    
    public void aftsQueryWithCount(String query, int count, QName property, Boolean ascending)
    {
        queryWithCount(SearchService.LANGUAGE_FTS_ALFRESCO, query, count, property, ascending);
    }
    
    public void queryWithCount(String ql, String query, int count, QName property, Boolean ascending)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(ql);
        sp.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
        sp.setQuery(query);
        sp.addStore(rootNodeRef.getStoreRef());
        ResultSet results = serviceRegistry.getSearchService().query(sp);
        HashSet<NodeRef> found = new HashSet<NodeRef>();
        Comparable last = null;
        for(ResultSetRow row :results)
        {
            assertFalse(found.contains( row.getNodeRef()));
            found.add(row.getNodeRef());
            if(property != null)
            {
                Comparable current = (Comparable)nodeService.getProperty(row.getNodeRef(), property);
                if(last != null)
                {
                    if((ascending == null) || (ascending))
                    {
                        assert(last.compareTo(current) >= 0);
                    }
                    else
                    {
                        assert(last.compareTo(current) <= 0);
                    }
                            
                }
                last = current;
            }
        }
        assertEquals(count, results.length());
        results.getResultSetMetaData();
        results.close();
    }
}
