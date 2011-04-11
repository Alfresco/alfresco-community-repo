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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.springframework.context.ApplicationContext;

/**
 * Category tests
 * @author andyh
 *
 */
public class ADMLuceneCategoryTest extends TestCase
{    
    private ServiceRegistry serviceRegistry;
    
    static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    NodeService nodeService;
    DictionaryService dictionaryService;
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
    private NodeRef n11;
    private NodeRef n12;
    private NodeRef n13;
    private NodeRef n14;
    
    private NodeRef catContainer;
    private NodeRef catRoot;
    private NodeRef catACBase;
    private NodeRef catACOne;
    private NodeRef catACTwo;
    private NodeRef catACThree;
    private FullTextSearchIndexer luceneFTS;
    private DictionaryDAO dictionaryDAO;
    private String TEST_NAMESPACE = "http://www.alfresco.org/test/lucenecategorytest";
    private QName regionCategorisationQName;
    private QName assetClassCategorisationQName;
    private QName investmentRegionCategorisationQName;
    private QName marketingRegionCategorisationQName;
    private NodeRef catRBase;
    private NodeRef catROne;
    private NodeRef catRTwo;
    private NodeRef catRThree;
    private SearchService searcher;
    private LuceneIndexerAndSearcher indexerAndSearcher;
    private TenantService tenantService;

    private CategoryService categoryService;

    /**
     * Simple test constructor
     *
     */
    public ADMLuceneCategoryTest()
    {
        super();
    }

    /**
     * Named test constructor
     * @param arg0
     */
    public ADMLuceneCategoryTest(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        nodeService = (NodeService)ctx.getBean("dbNodeService");
        dictionaryService = (DictionaryService)ctx.getBean("dictionaryService");
        luceneFTS = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        searcher = (SearchService) ctx.getBean("searchService");
        indexerAndSearcher = (LuceneIndexerAndSearcher) ctx.getBean("admLuceneIndexerAndSearcherFactory");
        categoryService = (CategoryService) ctx.getBean("categoryService");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        tenantService = (TenantService) ctx.getBean("tenantService");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        createTestTypes();
        
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_CONTAINER).getChildRef();
        nodeService.setProperty(n1, QName.createQName("{namespace}property-1"), "value-1");
        n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_CONTAINER).getChildRef();
        nodeService.setProperty(n2, QName.createQName("{namespace}property-1"), "value-1");
        nodeService.setProperty(n2, QName.createQName("{namespace}property-2"), "value-2");
        n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_CONTAINER).getChildRef();
        n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}four"), ContentModel.TYPE_CONTAINER).getChildRef();
        n5 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}five"), ContentModel.TYPE_CONTAINER).getChildRef();
        n6 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}six"), ContentModel.TYPE_CONTAINER).getChildRef();
        n7 = nodeService.createNode(n2, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}seven"), ContentModel.TYPE_CONTAINER).getChildRef();
        n8 = nodeService.createNode(n2, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight-2"), ContentModel.TYPE_CONTAINER).getChildRef();
        n9 = nodeService.createNode(n5, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}nine"), ContentModel.TYPE_CONTAINER).getChildRef();
        n10 = nodeService.createNode(n5, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}ten"), ContentModel.TYPE_CONTAINER).getChildRef();
        n11 = nodeService.createNode(n5, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eleven"), ContentModel.TYPE_CONTAINER).getChildRef();
        n12 = nodeService.createNode(n5, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}twelve"), ContentModel.TYPE_CONTAINER).getChildRef();
        n13 = nodeService.createNode(n12, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}thirteen"), ContentModel.TYPE_CONTAINER).getChildRef();
        n14 = nodeService.createNode(n13, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}fourteen"), ContentModel.TYPE_CONTAINER).getChildRef();
        
        nodeService.addChild(rootNodeRef, n8, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight-0"));
        nodeService.addChild(n1, n8, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight-1"));
        nodeService.addChild(n2, n13, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}link"));
        
        nodeService.addChild(n1, n14, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}common"));
        nodeService.addChild(n2, n14, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}common"));
        nodeService.addChild(n5, n14, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}common"));
        nodeService.addChild(n6, n14, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}common"));
        nodeService.addChild(n12, n14, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}common"));
        nodeService.addChild(n13, n14, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}common"));
        
        // Categories
        
        catContainer = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categoryContainer"), ContentModel.TYPE_CONTAINER).getChildRef();
        catRoot = nodeService.createNode(catContainer, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "categoryRoot"), ContentModel.TYPE_CATEGORYROOT).getChildRef();
       
       
        
        catRBase = nodeService.createNode(catRoot, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "region"), ContentModel.TYPE_CATEGORY).getChildRef();
        catROne = nodeService.createNode(catRBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "Europe"), ContentModel.TYPE_CATEGORY).getChildRef();
        catRTwo = nodeService.createNode(catRBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "RestOfWorld"), ContentModel.TYPE_CATEGORY).getChildRef();
        catRThree = nodeService.createNode(catRTwo, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "US"), ContentModel.TYPE_CATEGORY).getChildRef();
        
        nodeService.addChild(catRoot, catRBase, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "investmentRegion"));
        nodeService.addChild(catRoot, catRBase, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "marketingRegion"));
        
        
        catACBase = nodeService.createNode(catRoot, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "assetClass"), ContentModel.TYPE_CATEGORY).getChildRef();
        catACOne = nodeService.createNode(catACBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "Fixed"), ContentModel.TYPE_CATEGORY).getChildRef();
        catACTwo = nodeService.createNode(catACBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "Equity"), ContentModel.TYPE_CATEGORY).getChildRef();
        catACThree = nodeService.createNode(catACTwo, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "SpecialEquity"), ContentModel.TYPE_CATEGORY).getChildRef();
        
        
       
        nodeService.addAspect(n1, assetClassCategorisationQName, createMap("assetClass", catACBase));
        nodeService.addAspect(n1, regionCategorisationQName, createMap("region", catRBase));
        
        nodeService.addAspect(n2, assetClassCategorisationQName, createMap("assetClass", catACOne));
        nodeService.addAspect(n3, assetClassCategorisationQName, createMap("assetClass", catACOne));
        nodeService.addAspect(n4, assetClassCategorisationQName, createMap("assetClass", catACOne));
        nodeService.addAspect(n5, assetClassCategorisationQName, createMap("assetClass", catACOne));
        nodeService.addAspect(n6, assetClassCategorisationQName, createMap("assetClass", catACOne));
        
        nodeService.addAspect(n7, assetClassCategorisationQName, createMap("assetClass", catACTwo));
        nodeService.addAspect(n8, assetClassCategorisationQName, createMap("assetClass", catACTwo));
        nodeService.addAspect(n9, assetClassCategorisationQName, createMap("assetClass", catACTwo));
        nodeService.addAspect(n10, assetClassCategorisationQName, createMap("assetClass", catACTwo));
        nodeService.addAspect(n11, assetClassCategorisationQName, createMap("assetClass", catACTwo));
        
        nodeService.addAspect(n12, assetClassCategorisationQName, createMap("assetClass", catACOne, catACTwo));
        nodeService.addAspect(n13, assetClassCategorisationQName, createMap("assetClass", catACOne, catACTwo, catACThree));
        nodeService.addAspect(n14, assetClassCategorisationQName, createMap("assetClass", catACOne, catACTwo));
        
        nodeService.addAspect(n2, regionCategorisationQName, createMap("region", catROne));
        nodeService.addAspect(n3, regionCategorisationQName, createMap("region", catROne));
        nodeService.addAspect(n4, regionCategorisationQName, createMap("region", catRTwo));
        nodeService.addAspect(n5, regionCategorisationQName, createMap("region", catRTwo));
        
        nodeService.addAspect(n5, investmentRegionCategorisationQName, createMap("investmentRegion", catRBase));
        nodeService.addAspect(n5, marketingRegionCategorisationQName, createMap("marketingRegion", catRBase));
        nodeService.addAspect(n6, investmentRegionCategorisationQName, createMap("investmentRegion", catRBase));
        nodeService.addAspect(n7, investmentRegionCategorisationQName, createMap("investmentRegion", catRBase));
        nodeService.addAspect(n8, investmentRegionCategorisationQName, createMap("investmentRegion", catRBase));
        nodeService.addAspect(n9, investmentRegionCategorisationQName, createMap("investmentRegion", catRBase));
        nodeService.addAspect(n10, marketingRegionCategorisationQName, createMap("marketingRegion", catRBase));
        nodeService.addAspect(n11, marketingRegionCategorisationQName, createMap("marketingRegion", catRBase));
        nodeService.addAspect(n12, marketingRegionCategorisationQName, createMap("marketingRegion", catRBase));
        nodeService.addAspect(n13, marketingRegionCategorisationQName, createMap("marketingRegion", catRBase));
        nodeService.addAspect(n14, marketingRegionCategorisationQName, createMap("marketingRegion", catRBase));
        
        tx.commit();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        // TODO Auto-generated method stub
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }

    private HashMap<QName, Serializable> createMap(String name, NodeRef[] nodeRefs)
    {
        HashMap<QName, Serializable> map = new HashMap<QName, Serializable>();
        Serializable value = (Serializable) Arrays.asList(nodeRefs);
        map.put(QName.createQName(TEST_NAMESPACE, name), value);
        return map;
    }
    
    private HashMap<QName, Serializable> createMap(String name, NodeRef nodeRef)
    {
        return createMap(name, new NodeRef[]{nodeRef});
    }
    
    private HashMap<QName, Serializable> createMap(String name, NodeRef nodeRef1, NodeRef nodeRef2)
    {
        return createMap(name, new NodeRef[]{nodeRef1, nodeRef2});
    }
    
    private HashMap<QName, Serializable> createMap(String name, NodeRef nodeRef1, NodeRef nodeRef2, NodeRef nodeRef3)
    {
        return createMap(name, new NodeRef[]{nodeRef1, nodeRef2, nodeRef3});
    }
    
    private void createTestTypes()
    {
        M2Model model = M2Model.createModel("test:lucenecategory");
        model.createNamespace(TEST_NAMESPACE, "test");
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, NamespaceService.DICTIONARY_MODEL_PREFIX);
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);
        
        regionCategorisationQName = QName.createQName(TEST_NAMESPACE, "region");
        M2Aspect generalCategorisation = model.createAspect("test:" + regionCategorisationQName.getLocalName());
        generalCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property genCatProp = generalCategorisation.createProperty("test:region");
        genCatProp.setIndexed(true);
        genCatProp.setIndexedAtomically(true);
        genCatProp.setMandatory(true);
        genCatProp.setMultiValued(true);
        genCatProp.setStoredInIndex(true);
        genCatProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
        genCatProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());
        
        assetClassCategorisationQName = QName.createQName(TEST_NAMESPACE, "assetClass");
        M2Aspect assetClassCategorisation = model.createAspect("test:" + assetClassCategorisationQName.getLocalName());
        assetClassCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property acProp = assetClassCategorisation.createProperty("test:assetClass");
        acProp.setIndexed(true);
        acProp.setIndexedAtomically(true);
        acProp.setMandatory(true);
        acProp.setMultiValued(true);
        acProp.setStoredInIndex(true);
        acProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
        acProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());
        
        investmentRegionCategorisationQName = QName.createQName(TEST_NAMESPACE, "investmentRegion");
        M2Aspect investmentRegionCategorisation = model.createAspect("test:" + investmentRegionCategorisationQName.getLocalName());
        investmentRegionCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property irProp = investmentRegionCategorisation.createProperty("test:investmentRegion");
        irProp.setIndexed(true);
        irProp.setIndexedAtomically(true);
        irProp.setMandatory(true);
        irProp.setMultiValued(true);
        irProp.setStoredInIndex(true);
        irProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
        irProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());
        
        marketingRegionCategorisationQName = QName.createQName(TEST_NAMESPACE, "marketingRegion");
        M2Aspect marketingRegionCategorisation = model.createAspect("test:" + marketingRegionCategorisationQName.getLocalName());
        marketingRegionCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property mrProp =  marketingRegionCategorisation.createProperty("test:marketingRegion");
        mrProp.setIndexed(true);
        mrProp.setIndexedAtomically(true);
        mrProp.setMandatory(true);
        mrProp.setMultiValued(true);
        mrProp.setStoredInIndex(true);
        mrProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);
        mrProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());

        dictionaryDAO.putModel(model);
    }
    
    private void buildBaseIndex()
    {
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(rootNodeRef.getStoreRef(), "delta" + System.currentTimeMillis() + "_" + (new Random().nextInt()), indexerAndSearcher);
        indexer.setNodeService(nodeService);
        //indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setDictionaryService(dictionaryService);
        indexer.setTenantService(tenantService);
        indexer.setFullTextSearchIndexer(luceneFTS);
        //indexer.clearIndex();
        indexer.createNode(new ChildAssociationRef(null, null, null, rootNodeRef));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}one"), n1));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}two"), n2));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}three"), n3));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}four"), n4));
        
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, rootNodeRef, QName.createQName("{namespace}categoryContainer"), catContainer));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, catContainer, QName.createQName("{cat}categoryRoot"), catRoot));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, catRoot, QName.createQName(TEST_NAMESPACE, "AssetClass"), catACBase));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_SUBCATEGORIES, catACBase, QName.createQName(TEST_NAMESPACE, "Fixed"), catACOne));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_SUBCATEGORIES, catACBase, QName.createQName(TEST_NAMESPACE, "Equity"), catACTwo));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_SUBCATEGORIES, catACTwo, QName.createQName(TEST_NAMESPACE, "SpecialEquity"), catACThree));
        
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, catRoot, QName.createQName(TEST_NAMESPACE, "Region"), catRBase));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_SUBCATEGORIES, catRBase, QName.createQName(TEST_NAMESPACE, "Europe"), catROne));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_SUBCATEGORIES, catRBase, QName.createQName(TEST_NAMESPACE, "RestOfWorld"), catRTwo));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_SUBCATEGORIES, catRTwo, QName.createQName(TEST_NAMESPACE, "US"), catRThree));
        
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n1, QName.createQName("{namespace}five"), n5));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n1, QName.createQName("{namespace}six"), n6));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n2, QName.createQName("{namespace}seven"), n7));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n2, QName.createQName("{namespace}eight"), n8));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n5, QName.createQName("{namespace}nine"), n9));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n5, QName.createQName("{namespace}ten"), n10));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n5, QName.createQName("{namespace}eleven"), n11));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n5, QName.createQName("{namespace}twelve"), n12));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n12, QName.createQName("{namespace}thirteen"), n13));
        indexer.createNode(new ChildAssociationRef(ContentModel.ASSOC_CATEGORIES, n13, QName.createQName("{namespace}fourteen"), n14));
        indexer.prepare();
        indexer.commit();
    }

    /**
     * Test multiple categories
     * @throws Exception
     */
    public void testMulti() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        buildBaseIndex();
        
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver(""));
        ResultSet results;
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\" AND (PATH:\"/test:assetClass/test:Equity/member\" PATH:\"/test:marketingRegion/member\")", null);
        //printPaths(results);
        assertEquals(9, results.length());
        results.close();
        tx.rollback();
    }
    
    /**
     * Test basic categories.
     * 
     * @throws Exception
     */
    public void testBasic() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        buildBaseIndex();
        
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver(""));
        ResultSet results;
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:marketingRegion\"", null);
        //printPaths(results);
        assertEquals(1, results.length());
        results.close();
        
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:marketingRegion//member\"", null);
        //printPaths(results);
        assertEquals(6, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass/member\" ", null);
        assertEquals(1, results.length());
        results.close();
        
        
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass/test:Fixed\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass/test:Equity\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Fixed\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:*\"", null);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass//test:*\"", null);
        assertEquals(3, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Fixed/member\"", null);
        //printPaths(results);
        assertEquals(8, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity/member\"", null);
        //printPaths(results);
        assertEquals(8, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity/test:SpecialEquity/member//.\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity/test:SpecialEquity/member//*\"", null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity/test:SpecialEquity/member\"", null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PATH:\"/test:assetClass/test:Equity/member\" AND +PATH:\"/test:assetClass/test:Fixed/member\"", null);
        //printPaths(results);
        assertEquals(3, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity/member\" PATH:\"/test:assetClass/test:Fixed/member\"", null);
        //printPaths(results);
        assertEquals(13, results.length());
        results.close();
        
        // Region 
        
        assertEquals(4, nodeService.getChildAssocs(catRoot).size());
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:region\"", null);
        //printPaths(results);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:region/member\"", null);
        //printPaths(results);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:region/test:Europe/member\"", null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:region/test:RestOfWorld/member\"", null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:region//member\"", null);
        //printPaths(results);
        assertEquals(5, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:investmentRegion//member\"", null);
        //printPaths(results);
        assertEquals(5, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:marketingRegion//member\"", null);
        //printPaths(results);
        assertEquals(6, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PATH:\"/test:assetClass/test:Fixed/member\" AND +PATH:\"/test:region/test:Europe/member\"", null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass/test:Fixed/member\" AND +PATH:\"/cm:categoryContainer/cm:categoryRoot/test:region/test:Europe/member\"", null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:assetClass/test:Equity/member\" PATH:\"/test:marketingRegion/member\"", null);
        //printPaths(results);
        assertEquals(9, results.length());
        results.close();
        tx.rollback();
    }
    
    /**
     * Test the catgeory service.
     * 
     * @throws Exception
     */
    public void testCategoryServiceImpl() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        buildBaseIndex();
        
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(rootNodeRef.getStoreRef(), indexerAndSearcher);
        
        searcher.setNodeService(nodeService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setTenantService(tenantService);
        searcher.setNamespacePrefixResolver(getNamespacePrefixReolsver(""));
        
        ResultSet 
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass/*\" ", null);
        assertEquals(3, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:assetClass/member\" ", null);
        assertEquals(1, results.length());
        results.close();
        
        LuceneCategoryServiceImpl impl = new LuceneCategoryServiceImpl();
        impl.setNodeService(nodeService);
        impl.setNamespacePrefixResolver(getNamespacePrefixReolsver(""));
        impl.setIndexerAndSearcher(indexerAndSearcher);
        impl.setTenantService(tenantService);
        impl.setDictionaryService(dictionaryService);
        
        Collection<ChildAssociationRef>
        result = impl.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE);
        assertEquals(1, result.size());
       
        
        result = impl.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE);
        assertEquals(3, result.size());
       
        
        result = impl.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE);
        assertEquals(2, result.size());
        
        
        result = impl.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY);
        assertEquals(14, result.size());
        
        
        result = impl.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.ANY);
        assertEquals(17, result.size());
       
        
        result = impl.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY);
        assertEquals(3, result.size());
        
        
        result = impl.getClassifications(rootNodeRef.getStoreRef());
        assertEquals(2, result.size());
        
        
        result = impl.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE);
        assertEquals(2, result.size());
      
        
        Collection<QName> aspects = impl.getClassificationAspects();
        assertEquals(7, aspects.size());    
       
        tx.rollback();
    }
    
    private NamespacePrefixResolver getNamespacePrefixReolsver(String defaultURI)
    {
        DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver(null);
        nspr.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        nspr.registerNamespace("namespace", "namespace");
        nspr.registerNamespace("test", TEST_NAMESPACE);
        nspr.registerNamespace(NamespaceService.DEFAULT_PREFIX, defaultURI);
        return nspr;
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testCategoryService() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        buildBaseIndex();
        assertEquals(1, categoryService.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE).size());
        assertEquals(2, categoryService.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE).size());
        assertEquals(14, categoryService.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY).size());
        assertEquals(3, categoryService.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY).size());
        assertEquals(17, categoryService.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.ANY).size());
        assertEquals(2, categoryService.getClassifications(rootNodeRef.getStoreRef()).size());
        assertEquals(2, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.ANY).size());
        assertEquals(7, categoryService.getClassificationAspects().size());
        assertEquals(2, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass")).size());

        NodeRef newRoot = categoryService.createRootCategory(rootNodeRef.getStoreRef(),QName.createQName(TEST_NAMESPACE, "assetClass"), "Fruit");
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(3, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass")).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(4, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.ANY).size());
     
        NodeRef newCat = categoryService.createCategory(newRoot, "Banana");
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(3, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass")).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(5, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.ANY).size());
     
        categoryService.deleteCategory(newCat);
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(3, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass")).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(4, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.ANY).size());
     
        categoryService.deleteCategory(newRoot);
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(2, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass")).size());
        assertEquals(2, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.ANY).size());
     
        
        tx.rollback();
    }

    /**
     * 
     * @throws Exception
     */
    public void xtestManyCategories() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();

        tx.begin();
        long start = System.nanoTime();
        int startCount = categoryService.getRootCategories(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE).size();
        System.out.println("1 Query complete in "+(System.nanoTime()-start)/1e9f);
        tx.commit();
        
        tx = transactionService.getUserTransaction();

        tx.begin();
        start = System.nanoTime();
        startCount = categoryService.getRootCategories(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE).size();
        System.out.println("2 Query complete in "+(System.nanoTime()-start)/1e9f);
        tx.commit();

        for (int i = 0; i < 0; i++)
        {
            tx = transactionService.getUserTransaction();
            tx.begin();
            categoryService.createRootCategory(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, "first" + i);
            tx.commit();
        }

        for (int j = 0; j < 10; j++)
        {
            tx = transactionService.getUserTransaction();
            tx.begin();
            for (int i = 0; i < 1; i++)
            {
                NodeRef topref = categoryService.createRootCategory(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(),ContentModel.ASPECT_GEN_CLASSIFIABLE, "third" + (j*100)+ i);
                for(int k = 0; k < 5; k++)
                {
                    NodeRef oneRef = categoryService.createCategory(topref, "child_"+i+"_"+j+"_"+k);
                    for(int l = 0; l < 5; l++)
                    {
                        NodeRef twoRef = categoryService.createCategory(oneRef, "child_"+i+"_"+j+"_"+k+"_"+l);
                        for(int m = 0; m < 5; m++)
                        {
                            NodeRef threeRef = categoryService.createCategory(twoRef, "child_"+i+"_"+j+"_"+k+"_"+l+"_"+m);
                            for(int n = 0; n < 5; n++)
                            {
                                NodeRef fourRef = categoryService.createCategory(threeRef, "child_"+i+"_"+j+"_"+k+"_"+l+"_"+m+"_"+n);
                                for(int o = 0; o < 5; o++)
                                {
                                    NodeRef fiveRef = categoryService.createCategory(fourRef, "child_"+i+"_"+j+"_"+k+"_"+l+"_"+m+"_"+n+"_"+o);
                                    for(int p = 0; p < 5; p++)
                                    {
                                        @SuppressWarnings("unused")
                                        NodeRef sixRef = categoryService.createCategory(fiveRef, "child_"+i+"_"+j+"_"+k+"_"+l+"_"+m+"_"+n+"_"+o+"_"+p);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            tx.commit();
        }

        tx = transactionService.getUserTransaction();
        tx.begin();
        start = System.nanoTime();
        assertEquals(startCount + 10, categoryService.getRootCategories(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        System.out.println("3 Query complete in "+(System.nanoTime()-start)/1e9f);
        tx.commit();
        
        tx = transactionService.getUserTransaction();
        tx.begin();
        start = System.nanoTime();
        assertEquals(startCount + 10, categoryService.getRootCategories(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        System.out.println("4 Query complete in "+(System.nanoTime()-start)/1e9f);
        tx.commit();
        
        tx = transactionService.getUserTransaction();
        tx.begin();
        start = System.nanoTime();
        ResultSet set = searcher.query(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(), "lucene", "@"+LuceneQueryParser.escape(ContentModel.ASPECT_GEN_CLASSIFIABLE.toString())+":second*");
        System.out.println("Query complete in "+(System.nanoTime()-start)/1e9f);
        set.close();
        tx.commit();

    }

    public void testCatCount() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        
        
        assertEquals(1, categoryService.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE).size());
        assertEquals(2, categoryService.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE).size());
        assertEquals(14, categoryService.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY).size());
        assertEquals(3, categoryService.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY).size());
        assertEquals(17, categoryService.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.ANY).size());
        assertEquals(2, categoryService.getClassifications(rootNodeRef.getStoreRef()).size());      
        assertEquals(2, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), CategoryService.Depth.ANY).size());
        assertEquals(7, categoryService.getClassificationAspects().size());
        assertEquals(2, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass")).size());
        
        List<Pair<NodeRef, Integer>> top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "assetClass"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }
        
       top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "investmentRegion"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }    
        
        top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "marketingRegion"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }
        
        top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "region"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }
        
        tx.commit();
    }
    
    /**
     * See ALF-5594 for details.
     */
    public void testCreateMultiLingualCategoryRoots() throws Exception
    {
        TransactionService transactionService = serviceRegistry.getTransactionService();
        UserTransaction tx = transactionService.getUserTransaction();
        tx.begin();
        
        StoreRef storeRef = catRoot.getStoreRef();
        
        // Should initially not have any roots
        assertEquals(0, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        // Setup to mimic how CreateCategoryDialogue works
        // Create the node to use
        nodeService.createNode(
                catRoot, ContentModel.ASSOC_CATEGORIES, 
                ContentModel.ASPECT_GEN_CLASSIFIABLE, 
                ContentModel.TYPE_CATEGORY
        );
        
        // Commit this, as it's all search based and otherwise lucene won't see it
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        
        
        // Should initially not have any roots
        assertEquals(0, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        
        // First up, try a few in English
        String eng1 = "This Is A Name!";
        NodeRef catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, eng1
        );
        
        // Commit this, as it's all search based and otherwise lucene won't see it
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        
        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        // Now add a 2nd English one
        String eng2 = "This Is Also A Name";
        catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, eng2
        );
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(2, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        // Not allowed to add a duplicate one though
        try {
            catRef = categoryService.createRootCategory(
                    storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, eng1
            );
            fail("Shouldn't be allowed to create a duplicate named root category");
        } catch(Exception e) { 
            tx.rollback();
            tx = transactionService.getUserTransaction();
            tx.begin();
        }
        assertEquals(2, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        
        // Do a check that the categories exist
        assertEquals(0, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, "Does Not Exist", false).size());
        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, eng1, false).size());
        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, eng2, false).size());
        
        
        // Now in French and Spanish
        String fr1 = "C'est une tr\u00e8s petite cat\u00e9gorie";
        catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, fr1
        );
        String fr2 = "Une autre cat\u00e9gorie";
        catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, fr2 
        );
        
        String es1 = "Una categor\u00eda";
        catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, es1 
        );
        
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        
        assertEquals(5, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        
        // Check that they have the accents in them
        int also = 0;
        int exclamation = 0;
        int e_acute = 0;
        int e_grave = 0;
        int i_acute = 0;
        for(ChildAssociationRef cref : categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE))
        {
            String name = (String)nodeService.getProperty(cref.getChildRef(), ContentModel.PROP_NAME);
            if(name.indexOf('!') != -1) exclamation++;
            if(name.indexOf("Also") != -1) also++;
            if(name.indexOf('\u00e8') != -1) e_grave++;
            if(name.indexOf('\u00e9') != -1) e_acute++;
            if(name.indexOf('\u00ed') != -1) i_acute++;
        }
        assertEquals(1, exclamation); // This Is A Name!
        assertEquals(1, also); // This Is Also A Name
        assertEquals(1, e_grave); // très, from 1st French one
        assertEquals(2, e_acute); // catégorie, in both French ones
        assertEquals(1, i_acute); // categoría, in Spanish one
        
        
        // Now try to find them by name
        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, fr1, false).size());
        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, fr2, false).size());
        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, es1, false).size());
        
        
        // Ensure we can't add a duplicate French one
        try {
            catRef = categoryService.createRootCategory(
                    storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, fr1
            );
            fail("Shouldn't be allowed to create a duplicate named French root category");
        } catch(Exception e) { 
            tx.rollback();
            tx = transactionService.getUserTransaction();
            tx.begin();
        }
        assertEquals(5, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        
        // Finally add some Japanese ones
        
        //  オペレーション中ににシステム
        // Shortened to 14 Japanese characters for default DB2 install: ALF-6545
        String jp1 = "\u30aa\u30da\u30ec\u30fc\u30b7\u30e7\u30f3\u4e2d\u306b\u306b\u30b7\u30b9\u30c6\u30e0";
        catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, jp1
        );
         //  をクリックしてください。
        String jp2 = "\u3092\u30af\u30ea\u30c3\u30af\u3057\u3066\u304f\u3060\u3055\u3044\u3002";
        catRef = categoryService.createRootCategory(
                storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, jp2
        );
        
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();

// Testing... What is there in the repo after all?
//for(ChildAssociationRef cref : categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE))
//{
//  String name = (String)nodeService.getProperty(cref.getChildRef(), ContentModel.PROP_NAME);
//  System.err.println(name);
//}
        
        // Check they're there
        assertEquals(7, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        // TODO Fix me so we can find these by name!
//        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, jp1, false).size());
//        assertEquals(1, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, jp2, false).size());
        assertEquals(0, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, jp1.substring(0,5), false).size());
        
        // Check we can't add a duplicate Japenese one
        try {
            catRef = categoryService.createRootCategory(
                    storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, jp1
            );
            fail("Shouldn't be allowed to create a duplicate named French root category");
        } catch(Exception e) { 
            tx.rollback();
            tx = transactionService.getUserTransaction();
            tx.begin();
        }
        assertEquals(7, categoryService.getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE).size());
        
        // Finish
        tx.commit();
    }
    
    @SuppressWarnings("unused")
    private int getTotalScore(ResultSet results)
    {
        int totalScore = 0;
        for(ResultSetRow row: results)
        {
            totalScore += row.getScore();
        }
        return totalScore;
    }
}
