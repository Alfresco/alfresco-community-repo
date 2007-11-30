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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
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
       
       
        
        catRBase = nodeService.createNode(catRoot, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "Region"), ContentModel.TYPE_CATEGORY).getChildRef();
        catROne = nodeService.createNode(catRBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "Europe"), ContentModel.TYPE_CATEGORY).getChildRef();
        catRTwo = nodeService.createNode(catRBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "RestOfWorld"), ContentModel.TYPE_CATEGORY).getChildRef();
        catRThree = nodeService.createNode(catRTwo, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "US"), ContentModel.TYPE_CATEGORY).getChildRef();
        
        nodeService.addChild(catRoot, catRBase, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "InvestmentRegion"));
        nodeService.addChild(catRoot, catRBase, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "MarketingRegion"));
        
        
        catACBase = nodeService.createNode(catRoot, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "AssetClass"), ContentModel.TYPE_CATEGORY).getChildRef();
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
        
        regionCategorisationQName = QName.createQName(TEST_NAMESPACE, "Region");
        M2Aspect generalCategorisation = model.createAspect("test:" + regionCategorisationQName.getLocalName());
        generalCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property genCatProp = generalCategorisation.createProperty("test:region");
        genCatProp.setIndexed(true);
        genCatProp.setIndexedAtomically(true);
        genCatProp.setMandatory(true);
        genCatProp.setMultiValued(true);
        genCatProp.setStoredInIndex(true);
        genCatProp.setTokenisedInIndex(false);
        genCatProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());
        
        assetClassCategorisationQName = QName.createQName(TEST_NAMESPACE, "AssetClass");
        M2Aspect assetClassCategorisation = model.createAspect("test:" + assetClassCategorisationQName.getLocalName());
        assetClassCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property acProp = assetClassCategorisation.createProperty("test:assetClass");
        acProp.setIndexed(true);
        acProp.setIndexedAtomically(true);
        acProp.setMandatory(true);
        acProp.setMultiValued(true);
        acProp.setStoredInIndex(true);
        acProp.setTokenisedInIndex(false);
        acProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());
        
        investmentRegionCategorisationQName = QName.createQName(TEST_NAMESPACE, "InvestmentRegion");
        M2Aspect investmentRegionCategorisation = model.createAspect("test:" + investmentRegionCategorisationQName.getLocalName());
        investmentRegionCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property irProp = investmentRegionCategorisation.createProperty("test:investmentRegion");
        irProp.setIndexed(true);
        irProp.setIndexedAtomically(true);
        irProp.setMandatory(true);
        irProp.setMultiValued(true);
        irProp.setStoredInIndex(true);
        irProp.setTokenisedInIndex(false);
        irProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());
        
        marketingRegionCategorisationQName = QName.createQName(TEST_NAMESPACE, "MarketingRegion");
        M2Aspect marketingRegionCategorisation = model.createAspect("test:" + marketingRegionCategorisationQName.getLocalName());
        marketingRegionCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property mrProp =  marketingRegionCategorisation.createProperty("test:marketingRegion");
        mrProp.setIndexed(true);
        mrProp.setIndexedAtomically(true);
        mrProp.setMandatory(true);
        mrProp.setMultiValued(true);
        mrProp.setStoredInIndex(true);
        mrProp.setTokenisedInIndex(false);
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
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"//*\" AND (PATH:\"/test:AssetClass/test:Equity/member\" PATH:\"/test:MarketingRegion/member\")", null, null);
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
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:MarketingRegion\"", null, null);
        //printPaths(results);
        assertEquals(1, results.length());
        results.close();
        
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:MarketingRegion//member\"", null, null);
        //printPaths(results);
        assertEquals(6, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass/member\" ", null, null);
        assertEquals(1, results.length());
        results.close();
        
        
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass/test:Fixed\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass/test:Equity\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Fixed\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:*\"", null, null);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass//test:*\"", null, null);
        assertEquals(3, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Fixed/member\"", null, null);
        //printPaths(results);
        assertEquals(8, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity/member\"", null, null);
        //printPaths(results);
        assertEquals(8, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity/test:SpecialEquity/member//.\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity/test:SpecialEquity/member//*\"", null, null);
        assertEquals(0, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity/test:SpecialEquity/member\"", null, null);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PATH:\"/test:AssetClass/test:Equity/member\" AND +PATH:\"/test:AssetClass/test:Fixed/member\"", null, null);
        //printPaths(results);
        assertEquals(3, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity/member\" PATH:\"/test:AssetClass/test:Fixed/member\"", null, null);
        //printPaths(results);
        assertEquals(13, results.length());
        results.close();
        
        // Region 
        
        assertEquals(4, nodeService.getChildAssocs(catRoot).size());
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:Region\"", null, null);
        //printPaths(results);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:Region/member\"", null, null);
        //printPaths(results);
        assertEquals(1, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:Region/test:Europe/member\"", null, null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:Region/test:RestOfWorld/member\"", null, null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:Region//member\"", null, null);
        //printPaths(results);
        assertEquals(5, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:InvestmentRegion//member\"", null, null);
        //printPaths(results);
        assertEquals(5, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:MarketingRegion//member\"", null, null);
        //printPaths(results);
        assertEquals(6, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PATH:\"/test:AssetClass/test:Fixed/member\" AND +PATH:\"/test:Region/test:Europe/member\"", null, null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "+PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass/test:Fixed/member\" AND +PATH:\"/cm:categoryContainer/cm:categoryRoot/test:Region/test:Europe/member\"", null, null);
        //printPaths(results);
        assertEquals(2, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/test:AssetClass/test:Equity/member\" PATH:\"/test:MarketingRegion/member\"", null, null);
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
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass/*\" ", null, null);
        assertEquals(3, results.length());
        results.close();
        
        results = searcher.query(rootNodeRef.getStoreRef(), "lucene", "PATH:\"/cm:categoryContainer/cm:categoryRoot/test:AssetClass/member\" ", null, null);
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
        
        
        result = impl.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE);
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
        assertEquals(2, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.ANY).size());
        assertEquals(6, categoryService.getClassificationAspects().size());
        assertEquals(2, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass")).size());

        NodeRef newRoot = categoryService.createRootCategory(rootNodeRef.getStoreRef(),QName.createQName(TEST_NAMESPACE, "AssetClass"), "Fruit");
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(3, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass")).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(4, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.ANY).size());
     
        NodeRef newCat = categoryService.createCategory(newRoot, "Banana");
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(3, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass")).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(5, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.ANY).size());
     
        categoryService.deleteCategory(newCat);
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(3, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass")).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(4, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.ANY).size());
     
        categoryService.deleteCategory(newRoot);
        tx.commit();
        tx = transactionService.getUserTransaction();
        tx.begin();
        assertEquals(2, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass")).size());
        assertEquals(2, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.ANY).size());
     
        
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
        @SuppressWarnings("unused")
        ResultSet set = searcher.query(serviceRegistry.getPersonService().getPeopleContainer().getStoreRef(), "lucene", "@"+LuceneQueryParser.escape(ContentModel.ASPECT_GEN_CLASSIFIABLE.toString())+":second*");
        System.out.println("Query complete in "+(System.nanoTime()-start)/1e9f);
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
        assertEquals(2, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.IMMEDIATE).size());
        assertEquals(3, categoryService.getCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), CategoryService.Depth.ANY).size());
        assertEquals(6, categoryService.getClassificationAspects().size());
        assertEquals(2, categoryService.getRootCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass")).size());
        
        List<Pair<NodeRef, Integer>> top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "AssetClass"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }
        
       top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "InvestmentRegion"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }    
        
        top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "MarketingRegion"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }
        
        top = categoryService.getTopCategories(rootNodeRef.getStoreRef(), QName.createQName(TEST_NAMESPACE, "Region"), 10);
        for(Pair<NodeRef, Integer> current : top)
        {
            System.out.println(""+nodeService.getPaths(current.getFirst(), true) + " "+current.getSecond());
        }
        
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
