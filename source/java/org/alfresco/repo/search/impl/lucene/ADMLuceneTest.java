/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAOImpl;
import org.alfresco.repo.domain.hibernate.SessionSizeResourceManager;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
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
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.search.QueryParameter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
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
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author andyh
 */
@SuppressWarnings("unused")
public class ADMLuceneTest extends TestCase
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

    private LuceneIndexerAndSearcher indexerAndSearcher;

    private ServiceRegistry serviceRegistry;

    private UserTransaction testTX;

    private AuthenticationComponent authenticationComponent;

    private NodeRef[] documentOrder;

    private NamespaceDAOImpl namespaceDao;

    private Date testDate;

    /**
     * 
     */
    public ADMLuceneTest()
    {
        super();
    }

    public void setUp() throws Exception
    {
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        luceneFTS = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");
        contentService = (ContentService) ctx.getBean("contentService");
        queryRegisterComponent = (QueryRegisterComponent) ctx.getBean("queryRegisterComponent");
        namespacePrefixResolver = (DictionaryNamespaceComponent) ctx.getBean("namespaceService");
        indexerAndSearcher = (LuceneIndexerAndSearcher) ctx.getBean("admLuceneIndexerAndSearcherFactory");
        ((AbstractLuceneIndexerAndSearcherFactory) indexerAndSearcher).setMaxAtomicTransformationTime(1000000);
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        retryingTransactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");
        tenantService = (TenantService) ctx.getBean("tenantService");
        
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
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDAO.putModel(model);

        namespaceDao.addPrefix("test", TEST_NAMESPACE);

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
        testDate = new Date(new Date().getTime() - 10000);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "date-ista"), testDate);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "datetime-ista"), testDate);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "boolean-ista"), Boolean.valueOf(true));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "qname-ista"), QName.createQName("{wibble}wobble"));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "category-ista"), new NodeRef(storeRef, "CategoryId"));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "noderef-ista"), n1);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "path-ista"), nodeService.getPath(n3));
        testProperties.put(QName.createQName(TEST_NAMESPACE, "locale-ista"), Locale.UK);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "null"), null);
        testProperties.put(QName.createQName(TEST_NAMESPACE, "list"), new ArrayList());
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
        testProperties.put(QName.createQName(TEST_NAMESPACE, "nullList"), testList);
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

        properties.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK));
        properties.put(ContentModel.PROP_DESCRIPTION, desc1);

        n14 = nodeService.createNode(n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}fourteen"), ContentModel.TYPE_CONTENT, properties).getChildRef();
        // nodeService.addAspect(n14, DictionaryBootstrap.ASPECT_QNAME_CONTENT,
        // properties);

        ContentWriter writer = contentService.getWriter(n14, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        // InputStream is =
        // this.getClass().getClassLoader().getResourceAsStream("test.doc");
        // writer.putContent(is);
        writer.putContent("The quick brown fox jumped over the lazy dog and ate the Alfresco Tutorial, in pdf format, along with the following stop words;  a an and are"
                + " as at be but by for if in into is it no not of on or such that the their then there these they this to was will with: "
                + " and random charcters \u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF");
        //System.out.println("Size is " + writer.getSize());

        nodeService.addChild(rootNodeRef, n8, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight-0"));
        nodeService.addChild(n1, n8, ASSOC_TYPE_QNAME, QName.createQName("{namespace}eight-1"));
        nodeService.addChild(n2, n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}link"));

        nodeService.addChild(n1, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n2, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n5, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n6, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n12, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));
        nodeService.addChild(n13, n14, ASSOC_TYPE_QNAME, QName.createQName("{namespace}common"));

        documentOrder = new NodeRef[] { rootNodeRef, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14 };

    }

    private double orderDoubleCount = -0.11d;

    private Date orderDate = new Date();

    private float orderFloatCount = -3.5556f;

    private long orderLongCount = -1999999999999999l;

    private int orderIntCount = -45764576;

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
        orderDate = Duration.subtract(orderDate, new Duration("P1D"));
        orderDoubleCount += 0.1d;
        orderFloatCount += 0.82f;
        orderLongCount += 299999999999999l;
        orderIntCount += 8576457;
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

    /**
     * @param arg0
     */
    public ADMLuceneTest(String arg0)
    {
        super(arg0);
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
        assertEquals(15, results.length());

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
        assertEquals(16, results.length());
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + n14 + "\"", null, null);
        assertEquals(1, results.length()); // one node
        results.close();

        nodeService.addAspect(n14, aspectWithChildren, null);
        testTX.commit();

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + n14 + "\"", null, null);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:link\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null, null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//.\"", null, null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/.\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/./.\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//./*\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//././*/././.\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//*\"", null, null);
        assertEquals(9, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:common\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null, null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//.\"", null, null);
        assertEquals(25, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*\"", null, null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/.\"", null, null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/./.\"", null, null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//./*\"", null, null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//././*/././.\"", null, null);
        assertEquals(24, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setQueryRegister(queryRegisterComponent);

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "QNAME:\"namespace:testFind\"");
        assertEquals(1, results.length());
        results.close();

        RetryingTransactionCallback<Object> createAndDeleteCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                SessionSizeResourceManager.setDisableInTransaction();
                for (int i = 0; i < 100; i++)
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setQueryRegister(queryRegisterComponent);

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

        runBaseTests();

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

        pns.setProperty(n1, ContentModel.PROP_TITLE, "cube");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(false);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(15, results.length());
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

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
        sp7.addSort("@" + createdDate, true);
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

        luceneFTS.resume();

        SearchParameters sp17 = new SearchParameters();
        sp17.addStore(rootNodeRef.getStoreRef());
        sp17.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp17.setQuery("PATH:\"//.\"");
        sp17.addSort("cabbage", false);
        results = searcher.query(sp17);
        results.close();

        luceneFTS.resume();
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@\\{namespace\\}property\\-2:\"valuetwo\"", null, null);
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
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_1", indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

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
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_1", indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);

        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        // //indexer.clearIndex();

        indexer.createNode(new ChildAssociationRef(null, null, null, rootNodeRef));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}one"), n1));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}two"), n2));
        indexer.updateNode(n1);
        // indexer.deleteNode(new ChildRelationshipRef(rootNode, "path",
        // newNode));

        indexer.prepare();
        indexer.commit();

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@\\{namespace\\}property\\-2:\"valuetwo\"", null, null);
        simpleResultSetTest(results);

        ChildAssocRefResultSet r2 = new ChildAssocRefResultSet(nodeService, results.getNodeRefs(), null, false);
        simpleResultSetTest(r2);

        ChildAssocRefResultSet r3 = new ChildAssocRefResultSet(nodeService, results.getNodeRefs(), null, true);
        simpleResultSetTest(r3);

        ChildAssocRefResultSet r4 = new ChildAssocRefResultSet(nodeService, results.getChildAssocRefs(), null);
        simpleResultSetTest(r4);

        DetachedResultSet r5 = new DetachedResultSet(results, null);
        simpleResultSetTest(r5);

        DetachedResultSet r6 = new DetachedResultSet(r2, null);
        simpleResultSetTest(r6);

        DetachedResultSet r7 = new DetachedResultSet(r3, null);
        simpleResultSetTest(r7);

        DetachedResultSet r8 = new DetachedResultSet(r4, null);
        simpleResultSetTest(r8);

        DetachedResultSet r9 = new DetachedResultSet(r5, null);
        simpleResultSetTest(r9);

        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@\\{namespace\\}property\\-1:\"valueone\"", null, null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@namespace\\:property\\-1:\"valueone\"", null, null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@property\\-1:\"valueone\"", null, null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@property\\-1:\"Valueone\"", null, null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@property\\-1:ValueOne", null, null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@property\\-1:valueone", null, null);
        assertEquals(2, results.length());
        assertEquals(n2.getId(), results.getNodeRef(0).getId());
        assertEquals(n1.getId(), results.getNodeRef(1).getId());
        assertEquals(1.0f, results.getScore(0));
        assertEquals(1.0f, results.getScore(1));
        results.close();

        QName qname = QName.createQName("", "property-1");

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + n1.toString() + "\"", null, null);

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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@\\{namespace\\}property-1:valueone", null, null);
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

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+ID:\"" + n1.toString() + "\"", null, null);
        try
        {
            assertEquals(2, results.length());
        }
        finally
        {
            results.close();
        }

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ID:\"" + rootNodeRef.toString() + "\"", null, null);
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
                indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
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

    private void runBaseTests()
    {
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setQueryRegister(queryRegisterComponent);
        ResultSet results;
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/.\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*/*\"", null, null);
        assertEquals(8, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//.\"", null, null);
        assertEquals(26, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null, null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*\"", null, null);
        assertEquals(25, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/.\"", null, null);
        assertEquals(25, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/./.\"", null, null);
        assertEquals(25, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//./*\"", null, null);
        assertEquals(25, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//././*/././.\"", null, null);
        assertEquals(25, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//common\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one//common\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//*\"", null, null);
        assertEquals(9, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//.\"", null, null);
        assertEquals(10, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen//.\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen//.//.\"", null, null);
        assertEquals(1, results.length());
        results.close();

        // Type search tests

        QName qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"1\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":1", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"01\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":01", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + escapeQName(qname) + ":\"001\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@test\\:int\\-ista:\"0001\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[0 TO 2]", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{0 TO 1}", null, null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{1 TO 2}", null, null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"2\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"02\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"002\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"0002\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[0 TO 2]", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{0 TO 2}", null, null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "long-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{2 TO 3}", null, null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"3.4\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3 TO 4]", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[3.3 TO 3.4]", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{3.3 TO 3.4}", null, null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"3.40\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"03.4\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "float-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"03.40\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"5.6\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"05.6\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"5.60\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "double-ista")) + ":\"05.60\"", null, null);
        assertEquals(1, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":[5.5 TO 5.7]", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{5.5 TO 5.6}", null, null);
        assertEquals(0, results.length());
        results.close();

        qname = QName.createQName(TEST_NAMESPACE, "double-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":{5.6 TO 5.7}", null, null);
        assertEquals(0, results.length());
        results.close();

        // Dates
        
        PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(TEST_NAMESPACE, "datetime-ista"));
        DataTypeDefinition dataType = propertyDef.getDataType();
        String analyserClassName = dataType.getAnalyserClassName();
        boolean usesDateTimeAnalyser = analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName());
        
        Date date = new Date();
        String sDate = CachingDateFormat.getDateFormat().format(date);
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "date-ista")) + ":\"" + sDate + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":\"" + sDate + "\"", null, null);
        assertEquals(usesDateTimeAnalyser ? 0 : 1 , results.length());
        results.close();
        
        sDate = CachingDateFormat.getDateFormat().format(testDate);
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "date-ista")) + ":\"" + sDate + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "datetime-ista")) + ":\"" + sDate + "\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "boolean-ista")) + ":\"true\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "qname-ista")) + ":\"{wibble}wobble\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "category-ista")) + ":\""
                + DefaultTypeConverter.INSTANCE.convert(String.class, new NodeRef(rootNodeRef.getStoreRef(), "CategoryId")) + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "noderef-ista")) + ":\"" + n1 + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "path-ista")) + ":\"" + nodeService.getPath(n3) + "\"",
                null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        // d:any

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "any-many-ista")) + ":\"100\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "any-many-ista")));
        results.close();

        results = searcher
                .query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "any-many-ista")) + ":\"anyValueAsString\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "any-many-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "any-many-ista")) + ":\"nintc\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "any-many-ista")));
        results.close();

        // proximity searches
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~0", null, null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~1", null, null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~2", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"Tutorial Alfresco\"~3", null, null);
        assertEquals(1, results.length());
        results.close();
        
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString())+":\"Alfresco Tutorial\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString())+":\"Tutorial Alfresco\"", null, null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString())+":\"Tutorial Alfresco\"~0", null, null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString())+":\"Tutorial Alfresco\"~1", null, null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString())+":\"Tutorial Alfresco\"~2", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString())+":\"Tutorial Alfresco\"~3", null, null);
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

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":\"en_GB_\"", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":en_GB_", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":en_*", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":*_GB_*", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(QName.createQName(TEST_NAMESPACE, "locale-ista")) + ":*_gb_*", null, null);
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(QName.createQName(TEST_NAMESPACE, "path-ista")));
        results.close();

        // Type

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testType.toString() + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testType.toPrefixString(namespacePrefixResolver) + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testType.toString() + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testType.toPrefixString(namespacePrefixResolver) + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testSuperType.toString() + "\"", null, null);
        assertEquals(13, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TYPE:\"" + testSuperType.toPrefixString(namespacePrefixResolver) + "\"", null, null);
        assertEquals(13, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testSuperType.toString() + "\"", null, null);
        assertEquals(12, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "EXACTTYPE:\"" + testSuperType.toPrefixString(namespacePrefixResolver) + "\"", null, null);
        assertEquals(12, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toString() + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toPrefixString(namespacePrefixResolver) + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toString() + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "ASPECT:\"" + testAspect.toPrefixString(namespacePrefixResolver) + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        // Test for AR-384

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fox AND TYPE:\"" + ContentModel.PROP_CONTENT.toString() + "\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fox cm\\:name:fox", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fo AND TYPE:\"" + ContentModel.PROP_CONTENT.toString() + "\"", null, null);
        assertEquals(0, results.length());
        results.close();

        // Test stop words are equivalent

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"the\"", null, null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"and\"", null, null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"over the lazy\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"over a lazy\"", null, null);
        assertEquals(1, results.length());
        results.close();

        // Test wildcards in text

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:laz*", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:laz~", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:la?y", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:?a?y", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*azy", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*az*", null, null);
        assertEquals(1, results.length());
        results.close();

        // Accents

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"\u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"aeidnouy\"", null, null);
        assertEquals(1, results.length());
        results.close();

        // FTS test

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"fox\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":\"fox\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher
                .query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".mimetype:\"text/plain\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".locale:\"en_GB\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".locale:en_*", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".locale:e*_GB", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ".size:\"298\"", null, null);
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
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) +":\"alfres??\"");
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
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alfresc*tutorial\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_DESCRIPTION.toString()) + ":\"alf* tut*\"");
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
        sp.setQuery("@" + LuceneQueryParser.escape(mlQName.toString()) + ":バナナ");
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

        // Test ISNULL/ISNOTNULL

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNULL:\"" + QName.createQName(TEST_NAMESPACE, "null").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(1, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage("lucene");
        sp.setQuery("ISNULL:\"" + QName.createQName(TEST_NAMESPACE, "path-ista").toString() + "\"");
        results = searcher.query(sp);
        assertEquals(0, results.length());
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

        // Test non field queries

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fox", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:fo*", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:f*x", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:*ox", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":fox", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":fo*", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":f*x", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toString()) + ":*ox", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":fox",
                null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":fo*",
                null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":f*x",
                null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "@" + LuceneQueryParser.escape(ContentModel.PROP_CONTENT.toPrefixString(namespacePrefixResolver)) + ":*ox",
                null, null);
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
        assertEquals(6, results.length());
        results.close();

        // TODO: should not have a null property type definition
        QueryParameterDefImpl paramDef = new QueryParameterDefImpl(QName.createQName("alf:lemur", namespacePrefixResolver), (DataTypeDefinition) null, true, "fox");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "TEXT:\"${alf:lemur}\"", null, new QueryParameterDefinition[] { paramDef });
        assertEquals(1, results.length());
        results.close();

        paramDef = new QueryParameterDefImpl(QName.createQName("alf:intvalue", namespacePrefixResolver), (DataTypeDefinition) null, true, "1");
        qname = QName.createQName(TEST_NAMESPACE, "int-ista");
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@" + escapeQName(qname) + ":\"${alf:intvalue}\"", null, new QueryParameterDefinition[] { paramDef });
        assertEquals(1, results.length());
        assertNotNull(results.getRow(0).getValue(qname));
        results.close();

    }

    /**
     * @throws Exception
     */
    public void testPathSearch() throws Exception
    {
        luceneFTS.pause();
        buildBaseIndex();

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        // //*

        ResultSet

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//common\"", null, null);
        assertEquals(7, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one//common\"", null, null);
        assertEquals(5, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//*\"", null, null);
        assertEquals(9, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//.\"", null, null);
        assertEquals(10, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        // //*

        ResultSet

        results = searcher.query(rootNodeRef.getStoreRef(), "xpath", "//./*", null, null);
        assertEquals(14, results.length());
        results.close();
        luceneFTS.resume();

        QueryParameterDefinition paramDef = new QueryParameterDefImpl(QName.createQName("alf:query", namespacePrefixResolver), (DataTypeDefinition) null, true, "//./*");
        results = searcher.query(rootNodeRef.getStoreRef(), "xpath", "${alf:query}", null, new QueryParameterDefinition[] { paramDef });
        assertEquals(14, results.length());
        results.close();
    }

    /**
     * @throws Exception
     */
    public void testMissingIndex() throws Exception
    {
        luceneFTS.pause();
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "_missing_");
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(storeRef, indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        // //*

        ResultSet

        results = searcher.query(storeRef, "xpath", "//./*", null, null);
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

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null, null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null, null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//.\"", null, null);
        assertEquals(17, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null, null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*\"", null, null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null, null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/.\"", null, null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null, null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/./.\"", null, null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null, null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//./*\"", null, null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null, null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//././*/././.\"", null, null);
        assertEquals(16, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
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

        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        ChildAssociationRef car = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}" + COMPLEX_LOCAL_NAME), testSuperType);
        indexer.createNode(car);

        indexer.commit();

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:" + ISO9075.encode(COMPLEX_LOCAL_NAME) + "\"", null, null);
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

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        ChildAssociationRef car = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}" + COMPLEX_LOCAL_NAME), testSuperType);
        indexer.createNode(car);

        indexer.commit();

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:" + ISO9075.encode(COMPLEX_LOCAL_NAME) + "\"", null, null);
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

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        testTX = transactionService.getUserTransaction();
        testTX.begin();

        luceneFTS.pause();
        buildBaseIndex();
        runBaseTests();

        nodeService.deleteNode(n13);
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null, null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null, null);
        assertEquals(3, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null, null);
        assertEquals(13, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//.\"", null, null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null, null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null, null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null, null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/./.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null, null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//./*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null, null);
        assertEquals(12, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//././*/././.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
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

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        nodeService.removeChild(n2, n13);
        indexer.deleteChildRelationship(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("{namespace}link"), n13));

        indexer.commit();

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:three\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:four\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:eight-0\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:five\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:one\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:two\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:six\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:seven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-1\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-2\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-2\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-1\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:two/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:eight-0\"", null, null);
        assertEquals(0, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:ten\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:eleven\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:five/namespace:twelve/namespace:thirteen/namespace:fourteen\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/namespace:*/namespace:*/namespace:*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:*/namespace:five/namespace:*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/namespace:*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/*/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/*/*/*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*\"", null, null);
        assertEquals(4, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/*/namespace:five/*\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/namespace:one/*/namespace:nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//.\"", null, null);
        assertEquals(15, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//.\"", null, null);
        assertEquals(23, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/.\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*/./.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//*/./.\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//./*\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//./*\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//././*/././.\"", null, null);
        assertEquals(14, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//././*/././.\"", null, null);
        assertEquals(22, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//common\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//common\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one//common\"", null, null);
        assertEquals(5, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//*\"", null, null);
        assertEquals(6, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//*\"", null, null);
        assertEquals(9, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one/five//.\"", null, null);
        assertEquals(7, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"/one/five//.\"", null, null);
        assertEquals(10, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//five/nine\"", null, null);
        assertEquals(1, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/one//thirteen/fourteen\"", null, null);
        assertEquals(1, results.length());
        results.close();

        indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:link//.\"", null, null);
        assertEquals(2, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//namespace:link//.\"", null, null);
        assertEquals(3, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:renamed_link//.\"", null, null);
        assertEquals(0, results.length());
        results.close();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis(), indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);

        nodeService.removeChild(n2, n13);
        nodeService.addChild(n2, n13, ASSOC_TYPE_QNAME, QName.createQName("{namespace}renamed_link"));

        indexer.updateChildRelationship(new ChildAssociationRef(ASSOC_TYPE_QNAME, n2, QName.createQName("namespace", "link"), n13), new ChildAssociationRef(ASSOC_TYPE_QNAME, n2,
                QName.createQName("namespace", "renamed_link"), n13));

        indexer.commit();

        runBaseTests();

        searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:link//.\"", null, null);
        assertEquals(0, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//namespace:renamed_link//.\"", null, null);
        assertEquals(2, results.length());
        results.close();
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH_WITH_REPEATS:\"//namespace:renamed_link//.\"", null, null);
        assertEquals(3, results.length());
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"KEYONE\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"KEYUNSTORED\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"KEYTWO\"", null, null);
        assertEquals(0, results.length());
        results.close();

        // Do index

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
        indexer.updateFullTextSearch(1000);
        indexer.prepare();
        indexer.commit();

        searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"keyone\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"keytwo\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"keyunstored\"", null, null);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"KEYONE\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"KEYUNSTORED\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"KEYTWO\"", null, null);
        assertEquals(0, results.length());
        results.close();

        // Do index

        searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-atomic")) + ":\"keyone\"", null, null);
        assertEquals(1, results.length());
        results.close();

        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
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
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-stored-tokenised-nonatomic")) + ":\"keytwo\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "\\@"
                + escapeQName(QName.createQName(TEST_NAMESPACE, "text-indexed-unstored-tokenised-atomic")) + ":\"KEYUNSTORED\"", null, null);
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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);

        ResultSet results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PARENT:\"" + rootNodeRef.toString() + "\"", null, null);
        assertEquals(5, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PARENT:\"" + rootNodeRef.toString() + "\" +QNAME:\"one\"", null, null);
        assertEquals(1, results.length());
        results.close();

        results = searcher.query(rootNodeRef.getStoreRef(), "lucene",
                "( +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}name:\"content woof\") OR  TEXT:\"content\"",
                null, null);

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

        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver("namespace"));
        searcher.setQueryRegister(queryRegisterComponent);

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
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
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
        assertEquals(12, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//.\"");
        sp.excludeDataInTheCurrentTransaction(true);
        results = serviceRegistry.getSearchService().query(sp);
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
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
        assertEquals(15, results.length());
        results.close();

    }

    private void runPerformanceTest(double time, boolean clear)
    {
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()),
                indexerAndSearcher);
        indexer.setMaxAtomicTransformationTime(1000000);
        indexer.setNodeService(nodeService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        indexer.setContentService(contentService);
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

    private NamespacePrefixResolver getNamespacePrefixReolsver(String defaultURI)
    {
        DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver(null);
        nspr.registerNamespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI);
        nspr.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        nspr.registerNamespace(NamespaceService.DICTIONARY_MODEL_PREFIX, NamespaceService.DICTIONARY_MODEL_1_0_URI);
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
