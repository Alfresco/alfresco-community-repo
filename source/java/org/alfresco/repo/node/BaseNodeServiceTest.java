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
package org.alfresco.repo.node;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.SealedObject;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Provides a base set of tests of the various {@link org.alfresco.service.cmr.repository.NodeService}
 * implementations.
 * <p>
 * To test a specific incarnation of the service, the methods {@link #getStoreService()} and
 * {@link #getNodeService()} must be implemented. 
 * 
 * @see #nodeService
 * @see #rootNodeRef
 * @see #buildNodeGraph()
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused") /* its just a test */
public abstract class BaseNodeServiceTest extends BaseSpringTest
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    
    public static final String DEFAULT_VALUE = "defaultValue";
    public static final String NOT_DEFAULT_VALUE = "notDefaultValue";
     
    public static final QName TYPE_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");
    public static final QName TYPE_QNAME_TEST_MANY_PROPERTIES = QName.createQName(NAMESPACE, "many-properties");
    public static final QName TYPE_QNAME_TEST_MANY_PROPERTIES_ENCRYPTED = QName.createQName(NAMESPACE, "many-properties-encrypted");
    public static final QName TYPE_QNAME_TEST_MANY_ML_PROPERTIES = QName.createQName(NAMESPACE, "many-ml-properties");
    public static final QName TYPE_QNAME_EXTENDED_CONTENT = QName.createQName(NAMESPACE, "extendedcontent");
    public static final QName ASPECT_QNAME_TEST_TITLED = QName.createQName(NAMESPACE, "titled");
    public static final QName ASPECT_QNAME_TEST_MARKER = QName.createQName(NAMESPACE, "marker");
    public static final QName ASPECT_QNAME_TEST_MARKER2 = QName.createQName(NAMESPACE, "marker2");
    public static final QName ASPECT_QNAME_MANDATORY = QName.createQName(NAMESPACE, "mandatoryaspect");
    public static final QName ASPECT_QNAME_WITH_DEFAULT_VALUE = QName.createQName(NAMESPACE, "withDefaultValue");
    public static final QName PROP_QNAME_TEST_TITLE = QName.createQName(NAMESPACE, "title");
    public static final QName PROP_QNAME_TEST_DESCRIPTION = QName.createQName(NAMESPACE, "description");
    public static final QName PROP_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");
    public static final QName PROP_QNAME_BOOLEAN_VALUE = QName.createQName(NAMESPACE, "booleanValue");
    public static final QName PROP_QNAME_INTEGER_VALUE = QName.createQName(NAMESPACE, "integerValue");
    public static final QName PROP_QNAME_LONG_VALUE = QName.createQName(NAMESPACE, "longValue");
    public static final QName PROP_QNAME_FLOAT_VALUE = QName.createQName(NAMESPACE, "floatValue");
    public static final QName PROP_QNAME_DOUBLE_VALUE = QName.createQName(NAMESPACE, "doubleValue");
    public static final QName PROP_QNAME_STRING_VALUE = QName.createQName(NAMESPACE, "stringValue");
    public static final QName PROP_QNAME_ML_TEXT_VALUE = QName.createQName(NAMESPACE, "mlTextValue");
    public static final QName PROP_QNAME_DATE_VALUE = QName.createQName(NAMESPACE, "dateValue");
    public static final QName PROP_QNAME_SERIALIZABLE_VALUE = QName.createQName(NAMESPACE, "serializableValue");
    public static final QName PROP_QNAME_NODEREF_VALUE = QName.createQName(NAMESPACE, "nodeRefValue");
    public static final QName PROP_QNAME_QNAME_VALUE = QName.createQName(NAMESPACE, "qnameValue");
    public static final QName PROP_QNAME_CONTENT_VALUE = QName.createQName(NAMESPACE, "contentValue");
    public static final QName PROP_QNAME_PATH_VALUE = QName.createQName(NAMESPACE, "pathValue");
    public static final QName PROP_QNAME_CATEGORY_VALUE = QName.createQName(NAMESPACE, "categoryValue");
    public static final QName PROP_QNAME_LOCALE_VALUE = QName.createQName(NAMESPACE, "localeValue");
    public static final QName PROP_QNAME_NULL_VALUE = QName.createQName(NAMESPACE, "nullValue");
    public static final QName PROP_QNAME_MULTI_VALUE = QName.createQName(NAMESPACE, "multiValue");   
    public static final QName PROP_QNAME_PERIOD_VALUE = QName.createQName(NAMESPACE, "periodValue");
    public static final QName PROP_QNAME_MULTI_ML_VALUE = QName.createQName(NAMESPACE, "multiMLValue");
    public static final QName PROP_QNAME_MARKER_PROP = QName.createQName(NAMESPACE, "markerProp");
    public static final QName PROP_QNAME_PROP1 = QName.createQName(NAMESPACE, "prop1");
    public static final QName PROP_QNAME_PROP2 = QName.createQName(NAMESPACE, "prop2");
    public static final QName ASSOC_TYPE_QNAME_TEST_CHILDREN = ContentModel.ASSOC_CHILDREN;
    public static final QName ASSOC_TYPE_QNAME_TEST_CONTAINS = ContentModel.ASSOC_CONTAINS;
    public static final QName ASSOC_TYPE_QNAME_TEST_NEXT = QName.createQName(NAMESPACE, "next");

    public static final QName ASPECT_WITH_ASSOCIATIONS = QName.createQName(NAMESPACE, "withAssociations");
    public static final QName ASSOC_ASPECT_CHILD_ASSOC = QName.createQName(NAMESPACE, "aspect-child-assoc");
    public static final QName ASSOC_ASPECT_NORMAL_ASSOC = QName.createQName(NAMESPACE, "aspect-normal-assoc");
    
    public static final QName ASPECT_WITH_ASSOCIATIONS_EXTRA = QName.createQName(NAMESPACE, "withAssociationsExtra");
    public static final QName ASSOC_ASPECT_CHILD_ASSOC_01 = QName.createQName(NAMESPACE, "aspect-child-assoc-01");
    public static final QName ASSOC_ASPECT_CHILD_ASSOC_02 = QName.createQName(NAMESPACE, "aspect-child-assoc-02");
    public static final QName ASSOC_ASPECT_NORMAL_ASSOC_01 = QName.createQName(NAMESPACE, "aspect-normal-assoc-01");
    public static final QName ASSOC_ASPECT_NORMAL_ASSOC_02 = QName.createQName(NAMESPACE, "aspect-normal-assoc-02");

    public static final QName TYPE_QNAME_TEST_MULTIPLE_TESTER = QName.createQName(NAMESPACE, "multiple-tester");
    public static final QName PROP_QNAME_STRING_PROP_SINGLE = QName.createQName(NAMESPACE, "stringprop-single");
    public static final QName PROP_QNAME_STRING_PROP_MULTIPLE = QName.createQName(NAMESPACE, "stringprop-multiple");
    public static final QName PROP_QNAME_ANY_PROP_SINGLE = QName.createQName(NAMESPACE, "anyprop-single");
    public static final QName PROP_QNAME_ANY_PROP_MULTIPLE = QName.createQName(NAMESPACE, "anyprop-multiple");
    
    public static final QName ASPECT_WITH_ENCRYPTED = QName.createQName(NAMESPACE, "withEncrypted");
    public static final QName PROP_QNAME_ENCRYPTED_VALUE = QName.createQName(NAMESPACE, "encryptedValue");

    protected PolicyComponent policyComponent;
    protected DictionaryService dictionaryService;
    protected TransactionService transactionService;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected AuthenticationComponent authenticationComponent;
    protected NodeService nodeService;
    protected MetadataEncryptor metadataEncryptor;
    protected Dialect dialect;
    /** populated during setup */
    protected NodeRef rootNodeRef;
    private NodeRef cat;

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        dialect = (Dialect) applicationContext.getBean("dialect");
        metadataEncryptor = (MetadataEncryptor) applicationContext.getBean("metadataEncryptor");
        
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        retryingTransactionHelper = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");
        policyComponent = (PolicyComponent) applicationContext.getBean("policyComponent");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        
        authenticationComponent.setSystemUserAsCurrentUser();
        
        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
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
        
        nodeService = getNodeService();
        
        // create a first store directly
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        StoreRef catStoreRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_cat_" + System.currentTimeMillis());
        NodeRef catRootNodeRef = nodeService.getRootNode(catStoreRef);
        
        cat = nodeService.createNode(catRootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}cat"), ContentModel.TYPE_CATEGORY).getChildRef();
        
        // downgrade integrity checks
        IntegrityChecker.setWarnInTransaction();
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // do nothing
        }
        super.onTearDownInTransaction();
    }



    /**
     * Loads the test model required for building the node graphs
     */
    public static DictionaryService loadModel(ApplicationContext applicationContext)
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
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
        // done
        return dictionary;
    }
    
    /**
     * Usually just implemented by fetching the bean directly from the bean factory,
     * for example:
     * <p>
     * <pre>
     *      return (NodeService) applicationContext.getBean("dbNodeService");
     * </pre>
     * The <tt>NodeService<tt> returned must support cascade deletion.
     * 
     * @return Returns the implementation of <code>NodeService</code> to be
     *      used for this test.  It must have transaction demarcation.
     */
    protected abstract NodeService getNodeService();
    
    public void testSetUp() throws Exception
    {
        assertNotNull("StoreService not set", nodeService);
        assertNotNull("NodeService not set", nodeService);
        assertNotNull("rootNodeRef not created", rootNodeRef);
    }

    /**
     * @see #buildNodeGraph(NodeService, NodeRef)
     */
    public Map<QName, ChildAssociationRef> buildNodeGraph() throws Exception
    {
        return BaseNodeServiceTest.buildNodeGraph(nodeService, rootNodeRef);
    }

    /**
     * Builds a graph of child associations as follows:
     * <pre>
     * Level 0:     root
     * Level 1:     root_p_n1   root_p_n2
     * Level 2:     n1_p_n3     n2_p_n4     n1_n4       n2_p_n5     n1_n8
     * Level 3:     n3_p_n6     n4_n6       n5_p_n7
     * Level 4:     n6_p_n8     n7_n8
     * </pre>
     * <p>
     * <ul>
     *   <li>Apart from the root node having the root aspect, node 6 (<b>n6</b>) also has the
     *       root aspect.</li>
     *   <li><b>n3</b> has properties <code>animal = monkey</code> and
     *       <code>reference = <b>n2</b>.toString()</code>.</li>
     *   <li>All nodes are of type {@link ContentModel#TYPE_CONTAINER container}
     *       with the exception of <b>n8</b>, which is of type {@link #TYPE_QNAME_TEST_CONTENT test:content}</li>
     * </ul>
     * <p>
     * The namespace URI for all associations is <b>{@link BaseNodeServiceTest#NAMESPACE}</b>.
     * <p>
     * The naming convention is:
     * <pre>
     * n2_p_n5
     * n4_n5
     * where
     *      n5 is the node number of the node
     *      n2 is the primary parent node number
     *      n4 is any other non-primary parent
     * </pre>
     * <p>
     * The session is flushed to ensure that persistence occurs correctly.  It is
     * cleared to ensure that fetches against the created data are correct.
     * 
     * @return Returns a map <code>ChildAssocRef</code> instances keyed by qualified assoc name
     */
    public static Map<QName, ChildAssociationRef> buildNodeGraph(
            NodeService nodeService,
            NodeRef rootNodeRef) throws Exception
    {
        String ns = BaseNodeServiceTest.NAMESPACE;
        QName qname = null;
        ChildAssociationRef assoc = null;
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        Map<QName, ChildAssociationRef> ret = new HashMap<QName, ChildAssociationRef>(13);
        
        // LEVEL 0

        // LEVEL 1
        qname = QName.createQName(ns, "root_p_n1");
        assoc = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
        ret.put(qname, assoc);
        NodeRef n1 = assoc.getChildRef();

        qname = QName.createQName(ns, "root_p_n2");
        assoc = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
        ret.put(qname, assoc);
        NodeRef n2 = assoc.getChildRef();

        // LEVEL 2
        
        properties.clear();
        properties.put(QName.createQName(ns, "animal"), "monkey");
        properties.put(QName.createQName(ns, "UPPERANIMAL"), "MONKEY");
        properties.put(QName.createQName(ns, "reference"), n2.toString());
        properties.put(QName.createQName(ns, "text1"), "bun");
        properties.put(QName.createQName(ns, "text2"), "cake");
        properties.put(QName.createQName(ns, "text3"), "biscuit");
        properties.put(QName.createQName(ns, "text12"), "bun, cake");
        properties.put(QName.createQName(ns, "text13"), "bun, biscuit");
        properties.put(QName.createQName(ns, "text23"), "cake, biscuit");
        properties.put(QName.createQName(ns, "text123"), "bun, cake, biscuit");
        ArrayList<String> slist = new ArrayList<String>();
        slist.add("first");
        slist.add("second");
        slist.add("third");
       
        properties.put(QName.createQName(ns, "mvp"), slist);
        
        ArrayList<Integer> ilist = new ArrayList<Integer>();
        ilist.add(new Integer(1));
        ilist.add(new Integer(2));
        ilist.add(new Integer(3));
        
        properties.put(QName.createQName(ns, "mvi"), ilist);
        
        qname = QName.createQName(ns, "n1_p_n3");
        assoc = nodeService.createNode(n1, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER, properties);
        ret.put(qname, assoc);
        NodeRef n3 = assoc.getChildRef();

        qname = QName.createQName(ns, "n2_p_n4");
        assoc = nodeService.createNode(n2, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
        ret.put(qname, assoc);
        NodeRef n4 = assoc.getChildRef();

        qname = QName.createQName(ns, "n1_n4");
        assoc = nodeService.addChild(n1, n4, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname);
        ret.put(qname, assoc);

        qname = QName.createQName(ns, "n2_p_n5");
        assoc = nodeService.createNode(n2, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
        ret.put(qname, assoc);
        NodeRef n5 = assoc.getChildRef();

        // LEVEL 3
        qname = QName.createQName(ns, "n3_p_n6");
        assoc = nodeService.createNode(n3, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
        ret.put(qname, assoc);
        NodeRef n6 = assoc.getChildRef();
        nodeService.addAspect(n6,
                ContentModel.ASPECT_ROOT,
                Collections.<QName, Serializable>emptyMap());

        qname = QName.createQName(ns, "n4_n6");
        assoc = nodeService.addChild(n4, n6, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname);
        ret.put(qname, assoc);

        qname = QName.createQName(ns, "n5_p_n7");
        assoc = nodeService.createNode(n5, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, ContentModel.TYPE_CONTAINER);
        ret.put(qname, assoc);
        NodeRef n7 = assoc.getChildRef();

        // LEVEL 4
        properties.clear();
        properties.put(PROP_QNAME_TEST_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        properties.put(PROP_QNAME_TEST_TITLE, "node8");
        qname = QName.createQName(ns, "n6_p_n8");
        assoc = nodeService.createNode(n6, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname, TYPE_QNAME_TEST_CONTENT, properties);
        ret.put(qname, assoc);
        NodeRef n8 = assoc.getChildRef();

        qname = QName.createQName(ns, "n7_n8");
        assoc = nodeService.addChild(n7, n8, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname);
        ret.put(qname, assoc);

        qname = QName.createQName(ns, "n1_n8");
        assoc = nodeService.addChild(n1, n8, ASSOC_TYPE_QNAME_TEST_CHILDREN, qname);
        ret.put(qname, assoc);

//        // flush and clear
//        getSession().flush();
//        getSession().clear();
        
        // done
        return ret;
    }
    
    /**
     * @return Returns a reference to the created store
     */
    private StoreRef createStore() throws Exception
    {
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                getName() + "_" + System.nanoTime());
        assertNotNull("No reference returned", storeRef);
        // done
        return storeRef;
    }
    
    public void testCreateStore() throws Exception
    {
        StoreRef storeRef = createStore();
        
        // check that it exists
        assertTrue("NodeService reports that store doesn't exist", nodeService.exists(storeRef));
        
        // get the root node
        NodeRef storeRootNode = nodeService.getRootNode(storeRef);
        // make sure that it has the root aspect
        boolean isRoot = nodeService.hasAspect(storeRootNode, ContentModel.ASPECT_ROOT);
        assertTrue("Root node of store does not have root aspect", isRoot);
        // and is of the correct type
        QName rootType = nodeService.getType(storeRootNode);
        assertEquals("Store root node of incorrect type", ContentModel.TYPE_STOREROOT, rootType);
    }
    
    public void testGetStores() throws Exception
    {
        StoreRef storeRef = createStore();
        
        // get all stores
        List<StoreRef> storeRefs = nodeService.getStores();
        
        // check that the store ref is present
        assertTrue("New store not present is list of stores", storeRefs.contains(storeRef));
    }
    
    public void testDeleteStore() throws Exception
    {
        StoreRef storeRef = createStore();
        // get all stores
        List<StoreRef> storeRefs = nodeService.getStores();
        // check that the store ref is present
        assertTrue("New store not present in list of stores", storeRefs.contains(storeRef));
        // Delete it
        nodeService.deleteStore(storeRef);
        storeRefs = nodeService.getStores();
        assertFalse("Deleted store should not present in list of stores", storeRefs.contains(storeRef));
        // Now make sure that none of the stores have the "deleted" protocol
        for (StoreRef retrievedStoreRef : storeRefs)
        {
            if (retrievedStoreRef.getProtocol().equals(StoreRef.PROTOCOL_DELETED))
            {
                fail("NodeService should not have returned 'deleted' stores." + storeRefs);
            }
        }

        // Commit to ensure all is well
        setComplete();
        endTransaction();
    }
    
    public void testExists() throws Exception
    {
        StoreRef storeRef = createStore();
        boolean exists = nodeService.exists(storeRef);
        assertEquals("Exists failed", true, exists);
        // create bogus ref
        StoreRef bogusRef = new StoreRef("What", "the");
        exists = nodeService.exists(bogusRef);
        assertEquals("Exists failed", false, exists);
    }
    
    public void testGetRootNode() throws Exception
    {
        StoreRef storeRef = createStore();
        // get the root node
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        assertNotNull("No root node reference returned", rootNodeRef);
        // get the root node again
        NodeRef rootNodeRefCheck = nodeService.getRootNode(storeRef);
        assertEquals("Root nodes returned different refs", rootNodeRef, rootNodeRefCheck);
    }
    
    public void testCreateNode() throws Exception
    {
        ChildAssociationRef assocRef = nodeService.createNode(rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER);
        assertEquals("Assoc type qname not set", ASSOC_TYPE_QNAME_TEST_CHILDREN, assocRef.getTypeQName());
        assertEquals("Assoc qname not set", QName.createQName("pathA"), assocRef.getQName());
        NodeRef childRef = assocRef.getChildRef();
        QName checkType = nodeService.getType(childRef);
        assertEquals("Child node type incorrect", ContentModel.TYPE_CONTAINER, checkType);
    }

    public void testCreateWithTooLongPathLocalname() throws Exception
    {
        try
        {
            ChildAssociationRef assocRef = nodeService.createNode(rootNodeRef, ASSOC_TYPE_QNAME_TEST_CHILDREN,
                    QName.createQName("Recognize that VSEPR theory states that nonbonded electrons (lone "
                            + "pairs) exert strong electrostatic repulsive forces against the bonded pairs "
                            + "of electrons and, as a result, the electron pairs arrange themselves as far "
                            + "apart as possible in order to minimize the repulsive forces"),
                    ContentModel.TYPE_CONTAINER);
            fail("Expected too-long QName localname to have been kicked out as illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    /**
     * Tests node creation with a pre-determined {@link ContentModel#PROP_NODE_UUID uuid}.
     */
    public void testCreateNodeWithId() throws Exception
    {
        String uuid = GUID.generate();
        // create a node with an explicit UUID
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        properties.put(ContentModel.PROP_NODE_UUID, uuid);
        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER,
                properties);
        // check it
        NodeRef expectedNodeRef = new NodeRef(rootNodeRef.getStoreRef(), uuid);
        NodeRef checkNodeRef = assocRef.getChildRef();
        assertEquals("Failed to create node with a chosen ID", expectedNodeRef, checkNodeRef);
    }

    public void testGetType() throws Exception
    {
        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef = assocRef.getChildRef();
        // get the type
        QName type = nodeService.getType(nodeRef);
        assertEquals("Type mismatch", ContentModel.TYPE_CONTAINER, type);
    }
    
    public void testSetType() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("setTypeTest"),
                TYPE_QNAME_TEST_CONTENT).getChildRef();
        assertEquals(TYPE_QNAME_TEST_CONTENT, this.nodeService.getType(nodeRef));
        
        assertNull(this.nodeService.getProperty(nodeRef, PROP_QNAME_PROP1));
        
        // Now change the type
        this.nodeService.setType(nodeRef, TYPE_QNAME_EXTENDED_CONTENT);
        assertEquals(TYPE_QNAME_EXTENDED_CONTENT, this.nodeService.getType(nodeRef));
        
        // Check new defaults
        Serializable defaultValue = this.nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertNotNull("No default property value assigned", defaultValue);
        assertEquals(DEFAULT_VALUE, defaultValue);
    }
    
    /**
     * Fills the given property map with some values according to the property definitions on the given class
     */
    protected void fillProperties(QName qname, Map<QName, Serializable> properties)
    {
        ClassDefinition classDef = dictionaryService.getClass(qname);
        if (classDef == null)
        {
            throw new RuntimeException("No such class: " + qname);
        }
        Map<QName,PropertyDefinition> propertyDefs = classDef.getProperties();
        // make up a property value for each property
        for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet())
        {
            QName propertyQName = entry.getKey();
            QName propertyTypeQName = entry.getValue().getDataType().getName();
            // Get the property type
            Serializable value = null;
            if (propertyTypeQName.equals(DataTypeDefinition.CONTENT))
            {
                value = new ContentData(null, MimetypeMap.EXTENSION_BINARY, 0L, "UTF-8");
            }
            else if (propertyTypeQName.equals(DataTypeDefinition.LOCALE))
            {
                value = Locale.CHINESE;
            }
            else if (propertyTypeQName.equals(DataTypeDefinition.BOOLEAN))
            {
                value = Boolean.TRUE;
            }
            else if (propertyTypeQName.equals(DataTypeDefinition.PATH))
            {
                value = new Path();
            }
            else if (propertyTypeQName.equals(DataTypeDefinition.QNAME))
            {
                value = TYPE_QNAME_EXTENDED_CONTENT;
            }
            else if (propertyTypeQName.equals(DataTypeDefinition.CATEGORY) || propertyTypeQName.equals(DataTypeDefinition.NODE_REF))
            {
                value = new NodeRef("workspace://SpacesStore/12345");
            }
            else
            {
                value = new Long(System.currentTimeMillis());
            }
            // add it
            properties.put(propertyQName, value);
        }
    }
    
    /**
     * Checks that aspects can be added, removed and queried.  Failure to detect
     * inadequate properties is also checked.
     */
    public void testAspects() throws Exception
    {
        // create a regular base node
        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "test-container"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef = assocRef.getChildRef();
        // add the titled aspect to the node, but don't supply any properties
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(20);
        nodeService.addAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED, properties);

        // get the properties required for the aspect
        fillProperties(BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED, properties);
        // get the node properties before
        Map<QName, Serializable> propertiesBefore = nodeService.getProperties(nodeRef);
        // add the aspect
        nodeService.addAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED, properties);
        // get the properties after and check
        Map<QName, Serializable> propertiesAfter = nodeService.getProperties(nodeRef);
        assertEquals("Aspect properties not added",
                propertiesBefore.size() + 2,
                propertiesAfter.size());
        
        // check that we know that the aspect is present
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        assertTrue("Titled aspect not present",
                aspects.contains(BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED));
        
        // check that hasAspect works
        boolean hasAspect = nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED);
        assertTrue("Aspect not confirmed to be on node", hasAspect);
        
        // remove the aspect
        nodeService.removeAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED);
        hasAspect = nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED);
        assertFalse("Aspect not removed from node", hasAspect);
        
        // check that the associated properties were removed
        propertiesAfter = nodeService.getProperties(nodeRef);
        assertEquals("Aspect properties not removed",
                propertiesBefore.size(),
                propertiesAfter.size());
    }
    
    public void testAspectsAddedAutomatically() throws Exception
    {
        // Add the test:titled properties
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(20);
        fillProperties(BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED, properties);
        // Create a regular base node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "test-container"),
                ContentModel.TYPE_CONTAINER,
                properties).getChildRef();
        // Ensure that the aspect was automatically added
        assertTrue("Aspect not automatically added during 'createNode'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED));
        
        // Remove the aspect and test using setProperties
        nodeService.removeAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED);
        properties = nodeService.getProperties(nodeRef);
        assertFalse("test:titled properties not removed",
                properties.containsKey(BaseNodeServiceTest.PROP_QNAME_TEST_TITLE));
        assertFalse("test:titled properties not removed",
                properties.containsKey(BaseNodeServiceTest.PROP_QNAME_TEST_DESCRIPTION));
        properties.put(BaseNodeServiceTest.PROP_QNAME_TEST_DESCRIPTION, "A description");
        nodeService.setProperties(nodeRef, properties);
        assertTrue("Aspect not automatically added during 'setProperties'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED));
        
        // Remove the aspect and test using addProperties
        nodeService.removeAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED);
        properties = new HashMap<QName, Serializable>(5);
        properties.put(BaseNodeServiceTest.PROP_QNAME_TEST_DESCRIPTION, "A description");
        nodeService.addProperties(nodeRef, properties);
        assertTrue("Aspect not automatically added during 'addProperties'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED));
        
        // Remove the aspect and test using setProperty
        nodeService.removeAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED);
        nodeService.setProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_TEST_DESCRIPTION, "A description");
        assertTrue("Aspect not automatically added during 'setProperty'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_TITLED));
        
        // Check that aspects with further mandatory aspects are added properly
        nodeService.setProperty(nodeRef, BaseNodeServiceTest.PROP_QNAME_MARKER_PROP, "Marker value");
        assertTrue("Aspect not automatically added during 'setProperty'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_MARKER));
        assertTrue("Aspect not automatically added during 'setProperty' (second-level)",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_QNAME_TEST_MARKER2));
        
        // Check that child association creation adds the aspect to the parent
        NodeRef childNodeRef = nodeService.createNode(
                nodeRef,
                BaseNodeServiceTest.ASSOC_ASPECT_CHILD_ASSOC,
                BaseNodeServiceTest.ASSOC_ASPECT_CHILD_ASSOC,
                ContentModel.TYPE_CMOBJECT).getChildRef();
        assertTrue("Aspect not automatically added by child association during 'createNode'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_WITH_ASSOCIATIONS));
        assertFalse("Unexpected 'aspect' added by child association during 'createNode'",
               nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASSOC_ASPECT_CHILD_ASSOC));
        
        nodeService.removeAspect(nodeRef, BaseNodeServiceTest.ASPECT_WITH_ASSOCIATIONS);
        assertFalse("Child node should have been deleted", nodeService.exists(childNodeRef));
        
        // Check that normal association creation adds the aspect to the source
        nodeService.createAssociation(nodeRef, rootNodeRef, BaseNodeServiceTest.ASSOC_ASPECT_NORMAL_ASSOC);
        assertTrue("Aspect not automatically added by peer association during 'createAssociation'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASPECT_WITH_ASSOCIATIONS));
        assertFalse("Unexpected aspect added by peer association during 'createAssociation'",
                nodeService.hasAspect(nodeRef, BaseNodeServiceTest.ASSOC_ASPECT_NORMAL_ASSOC));
    }
    
    public void testAspectRemoval() throws Exception
    {
        // Create a node to add the aspect to
        NodeRef sourceNodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "testAspectRemoval-source"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        // Create a target for the associations
        NodeRef targetNodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "testAspectRemoval-target"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        // Add the aspect to the source
        nodeService.addAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS, null);
        // Make the associations
        nodeService.addChild(
                sourceNodeRef,
                targetNodeRef,
                ASSOC_ASPECT_CHILD_ASSOC,
                QName.createQName(NAMESPACE, "aspect-child"));
        nodeService.createAssociation(sourceNodeRef, targetNodeRef, ASSOC_ASPECT_NORMAL_ASSOC);
        
        // Check that the correct associations are present
        assertEquals("Expected exactly one child",
                1, nodeService.getChildAssocs(sourceNodeRef).size());
        assertEquals("Expected exactly one target",
                1, nodeService.getTargetAssocs(sourceNodeRef, RegexQNamePattern.MATCH_ALL).size());
        
        // Now remove the aspect
        nodeService.removeAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS);
        
        // Check that the associations were removed
        assertEquals("Expected exactly zero child",
                0, nodeService.getChildAssocs(sourceNodeRef).size());
        assertEquals("Expected exactly zero target",
                0, nodeService.getTargetAssocs(sourceNodeRef, RegexQNamePattern.MATCH_ALL).size());

        // Force different cleanup queries:
        //    ALF-5308: SQL error when changing name for record / folder with dispostion schedule applied
        nodeService.addAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS_EXTRA, null);
        // Make the associations
        nodeService.addChild(
                sourceNodeRef,
                targetNodeRef,
                ASSOC_ASPECT_CHILD_ASSOC_01,
                QName.createQName(NAMESPACE, "aspect-child-01"));
        nodeService.addChild(
                sourceNodeRef,
                targetNodeRef,
                ASSOC_ASPECT_CHILD_ASSOC_02,
                QName.createQName(NAMESPACE, "aspect-child-02"));
        nodeService.createAssociation(sourceNodeRef, targetNodeRef, ASSOC_ASPECT_NORMAL_ASSOC_01);
        nodeService.createAssociation(sourceNodeRef, targetNodeRef, ASSOC_ASPECT_NORMAL_ASSOC_02);
        nodeService.removeAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS_EXTRA);
    }
    
    /**
     * Test {@link https://issues.alfresco.com/jira/browse/ALFCOM-2299 ALFCOM-2299}
     */
    public void testAspectRemovalCascadeDelete() throws Exception
    {
        // Create a node to add the aspect to
        NodeRef sourceNodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "testAspectRemovalCascadeDelete"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        // Add the aspect to the source node and add a child using an association defined on the aspect
        nodeService.addAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS, null);
        NodeRef targetNodeRef = nodeService.createNode(
                sourceNodeRef,
                ASSOC_ASPECT_CHILD_ASSOC,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "testAspectRemovalCascadeDelete"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        assertTrue("Child node must exist", nodeService.exists(targetNodeRef));
        // Now remove the aspect from the source node and check that the target node was cascade-deleted
        nodeService.removeAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS);
        assertFalse("Child node must have been cascade-deleted", nodeService.exists(targetNodeRef));
        
        // Commit for good measure
        setComplete();
        endTransaction();
    }
    
    private static final QName ASPECT_QNAME_TEST_RENDERED = QName.createQName(NAMESPACE, "rendered");
    private static final QName ASSOC_TYPE_QNAME_TEST_RENDITION = QName.createQName(NAMESPACE, "rendition-page");
    private static final QName TYPE_QNAME_TEST_RENDITION_PAGE = QName.createQName(NAMESPACE, "rendition-page");
    private static final QName PROP_QNAME_TEST_RENDITION_PAGE_CONTENT = QName.createQName(NAMESPACE, "rendition-page-content");
    public void testAspectWithChildAssociationsCreationAndRetrieval() throws Exception
    {
        // Create a folder.  This is like the user's home folder, say.
        NodeRef folderNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "UserX-" + GUID.generate()),
                ContentModel.TYPE_FOLDER).getChildRef();
        // Create a document.  This is the actual document uploaded by the user.
        NodeRef fileNodeRef = nodeService.createNode(
                folderNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "Uploaded.pdf"),
                ContentModel.TYPE_FOLDER).getChildRef();
        // So, thus far, this is exactly what you have.  Now for the bit to add some renditions.
        // First, we can make some content data pages - spoofed, of course
        List<ContentData> renditionContentPages = new ArrayList<ContentData>(20);
        // This loop is where you will, outside of the transaction, push the page content into the repo
        for(int i = 0; i < 100; i++)
        {
            ContentData contentData = new ContentData(null, MimetypeMap.MIMETYPE_PDF, 10245, "UTF-8");
            renditionContentPages.add(contentData);
        }
        
        nodeService.addAspect(fileNodeRef, ASPECT_QNAME_TEST_RENDERED, null);
        int pageNumber = 0;
        for (ContentData renditionContentPage : renditionContentPages)
        {
            pageNumber++;
            QName renditionQName = makePageAssocName(pageNumber);
            Map<QName, Serializable> properties = Collections.singletonMap(
                    PROP_QNAME_TEST_RENDITION_PAGE_CONTENT,
                    (Serializable) renditionContentPage);
            nodeService.createNode(
                    fileNodeRef,
                    ASSOC_TYPE_QNAME_TEST_RENDITION,
                    renditionQName,
                    TYPE_QNAME_TEST_RENDITION_PAGE,
                    properties);
        }
        
        // That's it for uploading.  Now we retrieve them.
        if (!nodeService.hasAspect(fileNodeRef, ASPECT_QNAME_TEST_RENDERED))
        {
            // Jump to the original rendition retrieval code
            return;
        }
        // It has the aspect, so it's the new model
        List<ChildAssociationRef> fetchedRenditionChildAssocs = nodeService.getChildAssocs(
                fileNodeRef,
                ASSOC_TYPE_QNAME_TEST_RENDITION,
                RegexQNamePattern.MATCH_ALL);
        assertEquals(
                "We didn't get the correct number of pages back",
                renditionContentPages.size(),
                fetchedRenditionChildAssocs.size());
        // Get page ... 5.  This is to prove that they are ordered.
        ChildAssociationRef fetchedRenditionChildAssoc5 = fetchedRenditionChildAssocs.get(4);
        QName page5QName = makePageAssocName(5);
        assertEquals(
                "Local name of page 5 assoc is not correct",
                page5QName,
                fetchedRenditionChildAssoc5.getQName());
        // Now retrieve page 5 using the NodeService
        List<ChildAssociationRef> fetchedRenditionChildAssocsPage5 = nodeService.getChildAssocs(
                fileNodeRef,
                ASSOC_TYPE_QNAME_TEST_RENDITION,
                page5QName);
        assertEquals("Expected exactly one result", 1, fetchedRenditionChildAssocsPage5.size());
        assertEquals("Targeted page retrieval was not correct",
                page5QName,
                fetchedRenditionChildAssocsPage5.get(0).getQName());
    }
    private static final int MAX_RENDITION_PAGES = 100;
    private static QName makePageAssocName(int pageNumber)
    {
        if (pageNumber > MAX_RENDITION_PAGES)
        {
            throw new IllegalArgumentException("Rendition page number may not exceed " + MAX_RENDITION_PAGES);
        }
        String pageLocalName = String.format("renditionpage%05d", pageNumber);
        QName renditionQName = QName.createQName(NAMESPACE, pageLocalName);
        return renditionQName;
    }
    
    public void testCreateNodeNoProperties() throws Exception
    {
        // flush to ensure that the pure JDBC query will work
        ChildAssociationRef assocRef = nodeService.createNode(rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("path1"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef = assocRef.getChildRef();
        assertTrue(nodeService.exists(nodeRef));
    }
    
    public void testLargeStrings() throws Exception
    {
        StringBuilder sb = new StringBuilder(2056);
        for (int i = 0; i < 1024; i++)
        {
            if (dialect instanceof DB2Dialect)
            {
                sb.append("A"); // pending ALF-4300
            }
            else
            {
                sb.append("\u1234");
            }
        }
        String longString = sb.toString();
        int len = longString.length();
        
        // Create a normal node
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        // fill properties
        fillProperties(TYPE_QNAME_TEST_CONTENT, properties);
        fillProperties(ASPECT_QNAME_TEST_TITLED, properties);
        
        // create node for real
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("MyContent"),
                TYPE_QNAME_TEST_CONTENT,
                properties).getChildRef();
        
        // Modify name using the long string
        nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, longString);
    }
    
    /**
     * @see #ASPECT_QNAME_TEST_TITLED
     */
    public void testCreateNodeWithProperties() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        // fill properties
        fillProperties(TYPE_QNAME_TEST_CONTENT, properties);
        fillProperties(ASPECT_QNAME_TEST_TITLED, properties);
        
        // create node for real
        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("MyContent"),
                TYPE_QNAME_TEST_CONTENT,
                properties);
        NodeRef nodeRef = assocRef.getChildRef();
        // check that the titled aspect is present
        assertTrue("Titled aspect not present",
                nodeService.hasAspect(nodeRef, ASPECT_QNAME_TEST_TITLED));
    }
    
    public void testCascadeDelete() throws Exception
    {
        // build the node and commit the node graph
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph(nodeService, rootNodeRef);
        NodeRef n3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();
        NodeRef n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4")).getChildRef();
        NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
        NodeRef n7Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n5_p_n7")).getChildRef();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();

        // control checks
        assertTrue("n6 not present", nodeService.exists(n6Ref));
        assertTrue("n8 not present", nodeService.exists(n8Ref));
        assertTrue("n8 exists failure", nodeService.exists(n8Ref));
        assertEquals("n6 primary parent association not present on n3", 1, countChildrenOfNode(n3Ref));
        assertEquals("n6 secondary parent association not present on n4", 1, countChildrenOfNode(n4Ref));
        assertEquals("n8 secondary parent association not present on n7", 1, countChildrenOfNode(n7Ref));
        
        // delete n6
        nodeService.deleteNode(n6Ref);
        
        // ensure that we can't see cascaded nodes in-transaction
        assertFalse("n8 not cascade deleted in-transaction", nodeService.exists(n8Ref));
        
        // commit to check
        setComplete();
        endTransaction();

        assertFalse("n6 not directly deleted", nodeService.exists(n6Ref));
        assertFalse("n8 not cascade deleted", nodeService.exists(n8Ref));
        assertEquals("n6 primary parent association not removed from n3", 0, countChildrenOfNode(n3Ref));
        assertEquals("n6 secondary parent association not removed from n4", 0, countChildrenOfNode(n4Ref));
        assertEquals("n8 secondary parent association not removed from n7", 0, countChildrenOfNode(n7Ref));
    }
    
    public static class BadOnDeleteNodePolicy implements
            NodeServicePolicies.OnDeleteNodePolicy,
            NodeServicePolicies.BeforeDeleteNodePolicy
    {
        private NodeService nodeService;
        private List<NodeRef> deletedNodeRefs;
        private List<NodeRef> beforeDeleteNodeRefs;
        
        private boolean onDeleteCreateChild = true;
        private boolean beforeDeleteCreateChild = true;
        
        public BadOnDeleteNodePolicy(NodeService nodeService, 
                List<NodeRef> beforeDeleteNodeRefs, 
                List<NodeRef> deletedNodeRefs)
        {

            
            this.nodeService = nodeService;
            this.beforeDeleteNodeRefs = beforeDeleteNodeRefs;
            this.deletedNodeRefs = deletedNodeRefs;
        }
        
        public void beforeDeleteNode(NodeRef nodeRef)
        {
            // add the child to the list
            beforeDeleteNodeRefs.add(nodeRef);
            
            if(beforeDeleteCreateChild)
            {
                System.out.println("before delete node - add child.");
                // add a new child to the child, i.e. just before it is deleted
                ChildAssociationRef assocRef = nodeService.createNode(
                    nodeRef,
                    ASSOC_TYPE_QNAME_TEST_CHILDREN,
                    QName.createQName("pre-delete new child"),
                    ContentModel.TYPE_CONTAINER);
                // set some child node properties
                nodeService.setProperty(nodeRef, PROP_QNAME_BOOLEAN_VALUE, "true");
                // add an aspect to the child
                nodeService.addAspect(nodeRef, ASPECT_QNAME_TEST_TITLED, null);
            }
        
        }

        public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isArchivedNode)
        {
            // add the child to the list
            deletedNodeRefs.add(childAssocRef.getChildRef());
            
            if(onDeleteCreateChild)
            {
                System.out.println("on delete node - add sibling.");
                // now perform some nasties on the node's parent, i.e. add a new child
                NodeRef parentRef = childAssocRef.getParentRef();
                NodeRef childRef = childAssocRef.getChildRef();
                ChildAssociationRef assocRef = nodeService.createNode(
                    parentRef,
                    ASSOC_TYPE_QNAME_TEST_CHILDREN,
                    QName.createQName("post-delete new child"),
                    ContentModel.TYPE_CONTAINER);
            }
        }

        private void setOnDeleteCreateChild(boolean onDeleteCreateChild)
        {
            this.onDeleteCreateChild = onDeleteCreateChild;
        }

        private boolean isOnDeleteCreateChild()
        {
            return onDeleteCreateChild;
        }
        
        private void setBeforeDeleteCreateChild(boolean beforeDeleteCreateChild)
        {
            this.beforeDeleteCreateChild = beforeDeleteCreateChild;
        }

        private boolean isBeforeDeleteCreateChild()
        {
            return beforeDeleteCreateChild;
        }
        
    }
    
    public void testDelete() throws Exception
    {
        final List<NodeRef> beforeDeleteNodeRefs = new ArrayList<NodeRef>(5);
        final List<NodeRef> deletedNodeRefs = new ArrayList<NodeRef>(5);
        
        BadOnDeleteNodePolicy nasty = new BadOnDeleteNodePolicy(nodeService, beforeDeleteNodeRefs, deletedNodeRefs);
        nasty.setOnDeleteCreateChild(false);
        nasty.setBeforeDeleteCreateChild(false);
        NodeServicePolicies.OnDeleteNodePolicy policy = nasty;

        // bind to listen to the deletion of a node
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                policy,
                new JavaBehaviour(policy, "onDeleteNode"));   
        
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                policy,
                new JavaBehaviour(policy, "beforeDeleteNode"));   
        
        // build the node and commit the node graph
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph(nodeService, rootNodeRef);
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
        NodeRef n3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();
        NodeRef n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4")).getChildRef();
        NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        
        // delete n1
        nodeService.deleteNode(n1Ref);
        assertFalse("Node not directly deleted", nodeService.exists(n1Ref));
        assertFalse("Node not cascade deleted", nodeService.exists(n3Ref));
        assertTrue("Node incorrectly cascade deleted", nodeService.exists(n4Ref));
        assertFalse("Node not cascade deleted", nodeService.exists(n6Ref));
        assertFalse("Node not cascade deleted", nodeService.exists(n8Ref));
        
        // check before delete delete policy has been called
        assertTrue("n1Ref before delete policy not called", beforeDeleteNodeRefs.contains(n1Ref));
        assertTrue("n3Ref before delete policy not called", beforeDeleteNodeRefs.contains(n3Ref));
        assertTrue("n6Ref before delete policy not called", beforeDeleteNodeRefs.contains(n6Ref));
        assertTrue("n8Ref before delete policy not called", beforeDeleteNodeRefs.contains(n8Ref));
        
        // check delete policy has been called
        assertTrue("n1Ref delete policy not called", deletedNodeRefs.contains(n1Ref));
        assertTrue("n3Ref delete policy not called", deletedNodeRefs.contains(n3Ref));
        assertTrue("n6Ref delete policy not called", deletedNodeRefs.contains(n6Ref));
        assertTrue("n8Ref delete policy not called", deletedNodeRefs.contains(n8Ref));
        
        // commit to check
        setComplete();
        endTransaction();
    }
    
    /**
     * This test is similar to the test above but onDelete does nasty stuff such as 
     * creating siblings of the soon to be deleted children.  
     * 
     * In particular, it verifies that we don't get stuck in an infinite loop.
     * @throws Exception
     */
    public void testDeleteWithBadlyBehavedOnDeletePolicies() throws Exception
    {
        final List<NodeRef> beforeDeleteNodeRefs = new ArrayList<NodeRef>(5);
        final List<NodeRef> deletedNodeRefs = new ArrayList<NodeRef>(5);
        BadOnDeleteNodePolicy nasty = new BadOnDeleteNodePolicy(nodeService, beforeDeleteNodeRefs, deletedNodeRefs);
        
        try 
        {   
            nasty.setOnDeleteCreateChild(true);
            nasty.setBeforeDeleteCreateChild(false);
            NodeServicePolicies.OnDeleteNodePolicy policy = nasty;

            // bind to listen to the deletion of a node
            policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                policy,
                new JavaBehaviour(policy, "onDeleteNode"));  
            
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                    policy,
                    new JavaBehaviour(policy, "beforeDeleteNode")); 
        
            // build the node and commit the node graph
            Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph(nodeService, rootNodeRef);
            NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
            NodeRef n3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();
            NodeRef n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4")).getChildRef();
            NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
            NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        
            // delete n1
            nodeService.deleteNode(n1Ref);
            
            fail("test has not detected orphan children");
              
            // commit to check
            setComplete();
            endTransaction();
         
        }
        catch (Exception e)
        {
            // We expect to get here with this test.
            //e.printStackTrace();
        }
        finally
        {
            // turn off nasty policy - may upset other tests
            nasty.setOnDeleteCreateChild(false);
            nasty.setBeforeDeleteCreateChild(false);
        }
    }
    /**
     * This test is similar to the test above but beforeDelete does nasty stuff such as 
     * creating children of the soon to be deleted children.  
     * 
     * In particular, it verifies that we don't get stuck in an infinite loop.
     * @throws Exception
     */
    public void testDeleteWithBadlyBehavedBeforeDeletePolicies() throws Exception
    {
        final List<NodeRef> beforeDeleteNodeRefs = new ArrayList<NodeRef>(5);
        final List<NodeRef> deletedNodeRefs = new ArrayList<NodeRef>(5);
        BadOnDeleteNodePolicy nasty = new BadOnDeleteNodePolicy(nodeService, beforeDeleteNodeRefs, deletedNodeRefs);
        
        try 
        {
            nasty.setOnDeleteCreateChild(false);
            nasty.setBeforeDeleteCreateChild(true);
            NodeServicePolicies.OnDeleteNodePolicy policy = nasty;

            // bind to listen to the deletion of a node
            policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                policy,
                new JavaBehaviour(policy, "onDeleteNode"));  
            
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                    policy,
                    new JavaBehaviour(policy, "beforeDeleteNode")); 
        
            // build the node and commit the node graph
            Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph(nodeService, rootNodeRef);
            NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
            NodeRef n3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();
            NodeRef n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4")).getChildRef();
            NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
            NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        
            // delete n1
            nodeService.deleteNode(n1Ref);
            
            fail("test has not detected orphan children");
            
        }
        catch (Exception e)
        {
            // We expect to get here with this test.
            //e.printStackTrace();
        }
        finally
        {
            // turn off nasty policy - may upset other tests
            nasty.setOnDeleteCreateChild(false);
            nasty.setBeforeDeleteCreateChild(false);
        }
    }
    
    private int countChildrenOfNode(NodeRef nodeRef)
    {
        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
        return children.size();
    }
    
    public void testAddBogusChild() throws Exception
    {
        // create a bogus reference
        NodeRef bogusChildRef = new NodeRef(rootNodeRef.getStoreRef(), "BOGUS");
        try
        {
            nodeService.addChild(rootNodeRef, bogusChildRef, ASSOC_TYPE_QNAME_TEST_CHILDREN, QName.createQName("BOGUS_PATH"));
            fail("Failed to detect invalid child node reference");
        }
        catch (InvalidNodeRefException e)
        {
            // expected
        }
    }
    
    public void testAddChild() throws Exception
    {
        NodeRef childNodeRef1 = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("C1"),
                ContentModel.TYPE_CONTAINER).getChildRef();
         int count = countChildrenOfNode(rootNodeRef);
         assertEquals("Root children count incorrect", 1, count);
         NodeRef childNodeRef2 = nodeService.createNode(
                 childNodeRef1,
                 ASSOC_TYPE_QNAME_TEST_CHILDREN,
                 QName.createQName("C2"),
                 ContentModel.TYPE_CONTAINER).getChildRef();
          count = countChildrenOfNode(rootNodeRef);
          assertEquals("Root children count incorrect", 1, count);
        // associate the two nodes
        nodeService.addChild(
                rootNodeRef,
                childNodeRef2,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathB"));
        // there should now be 2 child assocs on the root
         int countAfter = countChildrenOfNode(rootNodeRef);
         assertEquals("Root children count incorrect", 2, countAfter);
         
         // now attempt to create a cyclical relationship
         try
         {
             nodeService.addChild(
                     childNodeRef1,
                     rootNodeRef,
                     ASSOC_TYPE_QNAME_TEST_CHILDREN,
                     QName.createQName("backToRoot"));
             fail("Failed to detect cyclic child relationship during addition of child");
         }
         catch (CyclicChildRelationshipException e)
         {
             // expected
         }
    }

    public void testRemoveSpecificChild() throws Exception
    {
        ChildAssociationRef pathPrimaryRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER);
        NodeRef parentRef = pathPrimaryRef.getParentRef();
        ChildAssociationRef pathARef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER);
        ChildAssociationRef pathBRef = nodeService.addChild(
                parentRef,
                pathARef.getChildRef(),
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathB"));
        ChildAssociationRef pathCRef = nodeService.addChild(
                parentRef,
                pathARef.getChildRef(),
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathC"));
        
        // remove the path B association
        boolean removedB = nodeService.removeChildAssociation(pathBRef);
        assertTrue("Association was not removed", removedB);
        removedB = nodeService.removeChildAssociation(pathBRef);
        assertFalse("Non-existent association was apparently removed", removedB);
        
        // remove the path C association
        boolean removedC = nodeService.removeChildAssociation(pathCRef);
        assertTrue("Association was not removed", removedC);
        removedC = nodeService.removeSeconaryChildAssociation(pathCRef);
        assertFalse("Non-existent association was apparently removed", removedC);
        
        // Now verify that primary associations are caught
        try
        {
            nodeService.removeSeconaryChildAssociation(pathPrimaryRef);
            fail("Primary association not detected");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    public void testRemoveChildByRef() throws Exception
    {
        NodeRef parentRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        ChildAssociationRef pathARef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER);
        NodeRef childARef = pathARef.getChildRef();
        AssociationRef pathDRef = nodeService.createAssociation(
                parentRef, childARef, ASSOC_TYPE_QNAME_TEST_NEXT);
        // remove the child - this must cascade
        nodeService.removeChild(parentRef, childARef);
        
        assertFalse("Primary child not deleted", nodeService.exists(childARef));
        assertEquals("Child assocs not removed",
                0, nodeService.getChildAssocs(
                        parentRef,
                        ASSOC_TYPE_QNAME_TEST_CHILDREN,
                        new RegexQNamePattern(".*", "path*")).size());
        assertEquals("Node assoc not removed",
                0, nodeService.getTargetAssocs(parentRef, RegexQNamePattern.MATCH_ALL).size());
    }
    
    public enum TestEnum
    {
        TEST_ONE,
        TEST_TWO
    }
    
    public void testProperties() throws Exception
    {
        // create a node to play with
        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("playThing"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef = assocRef.getChildRef();

        QName qnameProperty1 = QName.createQName("PROPERTY1");
        String valueProperty1 = "VALUE1";
        QName qnameProperty2 = QName.createQName("PROPERTY2");
        String valueProperty2 = "VALUE2";
        QName qnameProperty3 = QName.createQName("PROPERTY3");
        QName qnameProperty4 = QName.createQName("PROPERTY4");
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        properties.put(qnameProperty1, valueProperty1);
        // add some properties to the root node
        nodeService.setProperties(nodeRef, properties);
        // set a single property
        nodeService.setProperty(nodeRef, qnameProperty2, valueProperty2);
        // set a null property
        nodeService.setProperty(nodeRef, qnameProperty3, null);
        // set an enum property
        nodeService.setProperty(nodeRef, qnameProperty4, TestEnum.TEST_ONE);
        
        // force a flush
        getSession().flush();
        getSession().clear();
        
        // now get them back
        Map<QName, Serializable> checkMap = nodeService.getProperties(nodeRef);
        assertNotNull("Properties were not set/retrieved", checkMap);
        assertNotNull("Name property not set automatically", checkMap.get(ContentModel.PROP_NAME));
        assertEquals("Name property to set to ID of node", nodeRef.getId(), checkMap.get(ContentModel.PROP_NAME));
        assertEquals("Property value incorrect", valueProperty1, checkMap.get(qnameProperty1));
        assertEquals("Property value incorrect", valueProperty2, checkMap.get(qnameProperty2));
        assertTrue("Null property not persisted", checkMap.containsKey(qnameProperty3));
        assertNull("Null value not persisted correctly", checkMap.get(qnameProperty3));
        assertEquals("Enum property not retrieved", TestEnum.TEST_ONE, checkMap.get(qnameProperty4));
        
        // get a single property direct from the node
        Serializable valueCheck = nodeService.getProperty(nodeRef, qnameProperty2);
        assertNotNull("Property value not set", valueCheck);
        assertEquals("Property value incorrect", "VALUE2", valueCheck);
        
        // Remove a property
        nodeService.removeProperty(nodeRef, qnameProperty2);
        valueCheck = nodeService.getProperty(nodeRef, qnameProperty2);
        assertNull("Property not removed", valueCheck);
        
        // set the property value to null
        try
        {
            nodeService.setProperty(nodeRef, qnameProperty2, null);            
        }
        catch (IllegalArgumentException e)
        {
            fail("Null property values are allowed");
        }
        // try setting null value as part of complete set
        try
        {
            properties = nodeService.getProperties(nodeRef);
            properties.put(qnameProperty1, null);
            nodeService.setProperties(nodeRef, properties);
        }
        catch (IllegalArgumentException e)
        {
            fail("Null property values are allowed in the map");
        }
    }
    
    public void testAddProperties() throws Exception
    {
        Map<QName, Serializable> properties = nodeService.getProperties(rootNodeRef);
        // Add an aspect with a default value
        nodeService.addAspect(rootNodeRef, ASPECT_QNAME_TEST_TITLED, null);
        assertNull("Expected null property", nodeService.getProperty(rootNodeRef, PROP_QNAME_TEST_TITLE));
        assertNull("Expected null property", nodeService.getProperty(rootNodeRef, PROP_QNAME_TEST_DESCRIPTION));
        
        // Now add a map of two properties and check
        Map<QName, Serializable> addProperties = new HashMap<QName, Serializable>(11);
        addProperties.put(PROP_QNAME_TEST_TITLE, "Title");
        addProperties.put(PROP_QNAME_TEST_DESCRIPTION, "Description");
        nodeService.addProperties(rootNodeRef, addProperties);
        
        // Check
        Map<QName, Serializable> checkProperties = nodeService.getProperties(rootNodeRef);
        assertEquals("Title", checkProperties.get(PROP_QNAME_TEST_TITLE));
        assertEquals("Description", checkProperties.get(PROP_QNAME_TEST_DESCRIPTION));
    }
    
    public void testDefaultPropertyOverride_AddAspect() throws Exception
    {
        Serializable nullValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNull("Property should not be present", nullValue);
        
        String valueOverride = "VALUE_OVERRIDE";
        Map<QName, Serializable> properties = Collections.singletonMap(PROP_QNAME_PROP2, (Serializable)valueOverride);
        nodeService.addAspect(rootNodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE, properties);

        Serializable checkValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertEquals("Property should not be defaulted", valueOverride, checkValue);
    }
    
    public void testDefaultPropertyOverride_CreateNode() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_EXTENDED_CONTENT).getChildRef();

        Serializable checkValue = nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertEquals("Property should be defaulted", DEFAULT_VALUE, checkValue);

        String valueOverride = "VALUE_OVERRIDE";
        Map<QName, Serializable> properties = Collections.singletonMap(PROP_QNAME_PROP1, (Serializable)valueOverride);
        nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_EXTENDED_CONTENT,
                properties).getChildRef();

        checkValue = nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertEquals("Property should not be defaulted", valueOverride, checkValue);
    }
    
    public void testDefaultPropertyOverride_SpecializeWithoutProperty() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTENT).getChildRef();

        Serializable checkValue = nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertNull("Property should not exist", checkValue);
        
        // Specialize the type
        nodeService.setType(nodeRef, TYPE_QNAME_EXTENDED_CONTENT);

        checkValue = nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertEquals("Property should be defaulted", DEFAULT_VALUE, checkValue);
    }
    
    public void testDefaultPropertyOverride_SpecializeWithProperty() throws Exception
    {
        String valueOverride = "VALUE_OVERRIDE";
        Map<QName, Serializable> properties = Collections.singletonMap(PROP_QNAME_PROP1, (Serializable)valueOverride);
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTENT,
                properties).getChildRef();

        Serializable checkValue = nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertEquals("Property should not be defaulted", valueOverride, checkValue);
        
        // Specialize the type
        nodeService.setType(nodeRef, TYPE_QNAME_EXTENDED_CONTENT);

        checkValue = nodeService.getProperty(nodeRef, PROP_QNAME_PROP1);
        assertEquals("Property should *still* not be defaulted", valueOverride, checkValue);
    }
    
    public void testRemoveProperty() throws Exception
    {
        Map<QName, Serializable> properties = nodeService.getProperties(rootNodeRef);
        // Add an aspect with a default value
        nodeService.addAspect(rootNodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE, null);
        // Get the default value
        Serializable defaultValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNotNull("No default property value assigned", defaultValue);
        assertEquals("Property should be defaulted", DEFAULT_VALUE, defaultValue);
        // Now apply the original node properties which didn't contain the value
        nodeService.setProperties(rootNodeRef, properties);
        // Ensure that it is now null
        Serializable nullValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNull("Property was not removed", nullValue);
        
        // Remove the property by removing the aspect
        nodeService.removeAspect(rootNodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE);
        nullValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNull("Property was not removed", nullValue);
        
        // Do the same, but explicitly set the value to null
        nodeService.addAspect(rootNodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE, null);
        defaultValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNotNull("No default property value assigned", defaultValue);
        nodeService.setProperty(rootNodeRef, PROP_QNAME_PROP2, null);
        nullValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNull("Property was not removed", nullValue);
        
        // Now remove the property directly
        nodeService.removeAspect(rootNodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE);
        nodeService.addAspect(rootNodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE, null);
        defaultValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNotNull("No default property value assigned", defaultValue);
        nodeService.removeProperty(rootNodeRef, PROP_QNAME_PROP2);
        nullValue = nodeService.getProperty(rootNodeRef, PROP_QNAME_PROP2);
        assertNull("Property was not removed", nullValue);
    }
    
    /**
     * Makes a read-only transaction and then looks for a property using a non-existent QName.
     * The QName persistence must not lazily create QNameEntity instances for queries.
     */
    public void testGetUnknownProperty() throws Exception
    {
        // commit to keep the root node
        setComplete();
        endTransaction();

        RetryingTransactionCallback<NodeRef> createCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                NodeRef nodeRef = nodeService.createNode(
                        rootNodeRef,
                        ASSOC_TYPE_QNAME_TEST_CHILDREN,
                        QName.createQName("pathA"),
                        ContentModel.TYPE_CONTAINER).getChildRef();
                return nodeRef;
            }
        };
        final NodeRef nodeRef = retryingTransactionHelper.doInTransaction(createCallback, false, true);
        
        RetryingTransactionCallback<Object> testCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                QName ficticiousQName = QName.createQName(GUID.generate(), GUID.generate());
                Serializable value = nodeService.getProperty(nodeRef, ficticiousQName);
                assertNull("Didn't expect a value back", value);
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(testCallback, true, true);
    }
    
    /**
     * Ensures that the type you get out of a <b>d:any</b> property is the type that you put in.
     */
    public void testSerializableProperties() throws Exception
    {
        ContentData contentData = new ContentData(null, null, 0L, null);
        QName qname = PROP_QNAME_CONTENT_VALUE;
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(17);
        properties.put(PROP_QNAME_CONTENT_VALUE, contentData);
        properties.put(PROP_QNAME_SERIALIZABLE_VALUE, qname);
        // create node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER,
                properties).getChildRef();
        // persist
        flushAndClear();
        
        // get the properties back
        Map<QName, Serializable> checkProperties = nodeService.getProperties(nodeRef);
        Serializable checkPropertyContentData = checkProperties.get(PROP_QNAME_CONTENT_VALUE);
        Serializable checkPropertyQname = checkProperties.get(PROP_QNAME_SERIALIZABLE_VALUE);
        assertTrue("Serialization/deserialization of ContentData failed", checkPropertyContentData instanceof ContentData);
        assertTrue("Serialization/deserialization failed", checkPropertyQname instanceof QName);
    }
    
    /**
     * Check that <b>d:encrypted</b> properties work correctly.
     */
    public void testEncryptedProperties() throws Exception
    {
        QName property = PROP_QNAME_CONTENT_VALUE;
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(17);
        properties.put(PROP_QNAME_ENCRYPTED_VALUE, property);
        
        // We have encrypted properties, so encrypt them
        properties = metadataEncryptor.encrypt(properties);
        Serializable checkProperty = properties.get(PROP_QNAME_ENCRYPTED_VALUE);
        assertTrue("Properties not encrypted", checkProperty instanceof SealedObject);
        
        // create node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER,
                properties).getChildRef();
        // persist
        setComplete();
        endTransaction();
        
        // get the properties back
        Map<QName, Serializable> checkProperties = nodeService.getProperties(nodeRef);
        checkProperty = checkProperties.get(PROP_QNAME_ENCRYPTED_VALUE);
        assertTrue("Encrypted property not persisted", checkProperty instanceof SealedObject);
        
        // Now make sure that the value can be null
        nodeService.setProperty(nodeRef, PROP_QNAME_ENCRYPTED_VALUE, null);
        
        // Finally, make sure that it fails if we don't encrypt
    }
    
    @SuppressWarnings("unchecked")
    public void testMultiProp() throws Exception
    {
        QName undeclaredPropQName = QName.createQName(NAMESPACE, getName());
        // create node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MULTIPLE_TESTER).getChildRef();
        ArrayList<Serializable> values = new ArrayList<Serializable>(1);
        values.add("ABC");
        values.add("DEF");
        // test allowable conditions
        nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, "ABC");
        // nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, values); -- should fail
        nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_MULTIPLE, "ABC");
        nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_MULTIPLE, values);
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_SINGLE, "ABC");
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_SINGLE, values);
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, "ABC");
        nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, values);
        nodeService.setProperty(nodeRef, undeclaredPropQName, "ABC");
        nodeService.setProperty(nodeRef, undeclaredPropQName, values);

        // commit as we will be breaking the transaction in the next test
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            // this should fail as we are passing multiple values into a non-any that is multiple=false
            nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, values);
        }
        catch (DictionaryException e)
        {
            // expected
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable e) {}
        }
        
        txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            // Check that multi-valued d:mltext can be collections of MLText
            values.clear();
            values.add(new MLText("ABC"));
            values.add(new MLText("DEF"));
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE, values);
            List<Serializable> checkValues = (List<Serializable>) nodeService.getProperty(
                    nodeRef, PROP_QNAME_MULTI_ML_VALUE);
            assertEquals("Expected 2 MLText values back", 2, checkValues.size());
            assertTrue("Incorrect type in collection", checkValues.get(0) instanceof MLText);
            assertTrue("Incorrect type in collection", checkValues.get(1) instanceof MLText);
            
            // Check that multi-valued d:any properties can be collections of collections (empty)
            // We put ArrayLists and HashSets into the Collection of d:any, so that is exactly what should come out
            values.clear();
            ArrayList<Serializable> arrayListVal = new ArrayList<Serializable>(2);
            HashSet<Serializable> hashSetVal = new HashSet<Serializable>(2);
            values.add(arrayListVal);
            values.add(hashSetVal);
            nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, values);
            checkValues = (List<Serializable>) nodeService.getProperty(
                    nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE);
            assertEquals("Expected 2 Collection values back", 2, checkValues.size());
            assertTrue("Incorrect type in collection", checkValues.get(0) instanceof ArrayList);  // ArrayList in - ArrayList out
            assertTrue("Incorrect type in collection", checkValues.get(1) instanceof HashSet);  // HashSet in - HashSet out
            
            // Check that multi-valued d:any properties can be collections of collections (with values)
            // We put ArrayLists and HashSets into the Collection of d:any, so that is exactly what should come out
            arrayListVal.add("ONE");
            arrayListVal.add("TWO");
            hashSetVal.add("ONE");
            hashSetVal.add("TWO");
            values.clear();
            values.add(arrayListVal);
            values.add(hashSetVal);
            nodeService.setProperty(nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE, values);
            checkValues = (List<Serializable>) nodeService.getProperty(
                    nodeRef, PROP_QNAME_ANY_PROP_MULTIPLE);
            assertEquals("Expected 2 Collection values back", 2, checkValues.size());
            assertTrue("Incorrect type in collection", checkValues.get(0) instanceof ArrayList);  // ArrayList in - ArrayList out
            assertTrue("Incorrect type in collection", checkValues.get(1) instanceof HashSet);  // HashSet in - HashSet out
            assertEquals("First collection incorrect", 2, ((Collection)checkValues.get(0)).size());
            assertEquals("Second collection incorrect", 2, ((Collection)checkValues.get(1)).size());
        }
        catch (DictionaryException e)
        {
            // expected
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable e) {}
        }
    }
    
    /**
     * Apply any changes to the PROP_QNAME_XXX_VALUE used for checking the following:
     * <pre>
        properties.put(PROP_QNAME_BOOLEAN_VALUE, true);
        properties.put(PROP_QNAME_INTEGER_VALUE, 123);
        properties.put(PROP_QNAME_LONG_VALUE, 123L);
        properties.put(PROP_QNAME_FLOAT_VALUE, 123.0F);
        properties.put(PROP_QNAME_DOUBLE_VALUE, 123.0);
        properties.put(PROP_QNAME_STRING_VALUE, "123.0");
        properties.put(PROP_QNAME_ML_TEXT_VALUE, new MLText("This is ML text in the default language"));
        properties.put(PROP_QNAME_DATE_VALUE, new Date());
        properties.put(PROP_QNAME_SERIALIZABLE_VALUE, "456");
        properties.put(PROP_QNAME_NODEREF_VALUE, rootNodeRef);
        properties.put(PROP_QNAME_QNAME_VALUE, TYPE_QNAME_TEST_CONTENT);
        properties.put(PROP_QNAME_PATH_VALUE, pathProperty);
        properties.put(PROP_QNAME_CONTENT_VALUE, new ContentData("url", "text/plain", 88L, "UTF-8"));
        properties.put(PROP_QNAME_CATEGORY_VALUE, cat);
        properties.put(PROP_QNAME_LOCALE_VALUE, Locale.CHINESE);
        properties.put(PROP_QNAME_NULL_VALUE, null);
        properties.put(PROP_QNAME_MULTI_VALUE, listProperty);
        </pre>
     */
    protected void getExpectedPropertyValues(Map<QName, Serializable> checkProperties)
    {
        // Do nothing with them by default
    }
    
    /**
     * Checks that the 'check' values all match the 'expected' values
     */
    private void checkProperties(Map<QName, Serializable> checkProperties, Map<QName, Serializable> expectedProperties)
    {
        for (QName qname : expectedProperties.keySet())
        {
            Serializable value = expectedProperties.get(qname);
            Serializable checkValue = checkProperties.get(qname);
            assertEquals("Property mismatch - " + qname, value, checkValue);
        }
    }
    
    /**
     * Check that properties go in and come out in the correct format.
     * @see #getCheckPropertyValues(Map)
     */
    @SuppressWarnings("unchecked")
    public void testPropertyTypes() throws Exception
    {
        ArrayList<String> listProperty = new ArrayList<String>(2);
        listProperty.add("ABC");
        listProperty.add("DEF");
        
        Path pathProperty = new Path();
        pathProperty.append(new Path.SelfElement()).append(new Path.AttributeElement(TYPE_QNAME_TEST_CONTENT));
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(17);
        properties.put(PROP_QNAME_BOOLEAN_VALUE, true);
        properties.put(PROP_QNAME_INTEGER_VALUE, 123);
        properties.put(PROP_QNAME_LONG_VALUE, 123L);
        properties.put(PROP_QNAME_FLOAT_VALUE, 123.0F);
        properties.put(PROP_QNAME_DOUBLE_VALUE, 123.0);
        properties.put(PROP_QNAME_STRING_VALUE, "123.0");
        properties.put(PROP_QNAME_ML_TEXT_VALUE, new MLText("This is ML text in the default language"));
        properties.put(PROP_QNAME_DATE_VALUE, new Date());
        properties.put(PROP_QNAME_SERIALIZABLE_VALUE, "456");
        properties.put(PROP_QNAME_NODEREF_VALUE, rootNodeRef);
        properties.put(PROP_QNAME_QNAME_VALUE, TYPE_QNAME_TEST_CONTENT);
        properties.put(PROP_QNAME_PATH_VALUE, pathProperty);
        properties.put(PROP_QNAME_CONTENT_VALUE, new ContentData("url", "text/plain", 88L, "UTF-8"));
        properties.put(PROP_QNAME_CATEGORY_VALUE, cat);
        properties.put(PROP_QNAME_LOCALE_VALUE, Locale.CHINESE);
        properties.put(PROP_QNAME_NULL_VALUE, null);
        properties.put(PROP_QNAME_MULTI_VALUE, listProperty);
        properties.put(PROP_QNAME_PERIOD_VALUE, "period|1");
        // Get the check values
        Map<QName, Serializable> expectedProperties = new HashMap<QName, Serializable>(properties);
        getExpectedPropertyValues(expectedProperties);
        
        // create a new node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_PROPERTIES,
                properties).getChildRef();
        
        // get the properties back
        Map<QName, Serializable> checkProperties = nodeService.getProperties(nodeRef);
        // Check
        checkProperties(checkProperties, expectedProperties);
        
        // check multi-valued properties are created where necessary
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, "GHI");
        Serializable checkProperty = nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
        assertTrue("Property not converted to a Collection", checkProperty instanceof Collection);
        assertTrue("Collection doesn't contain value", ((Collection<?>)checkProperty).contains("GHI"));
    }
    
    public void testPropertyLocaleBehaviour() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(17);
        properties.put(PROP_QNAME_BOOLEAN_VALUE, true);
        properties.put(PROP_QNAME_INTEGER_VALUE, 123);
        properties.put(PROP_QNAME_LONG_VALUE, 123L);
        properties.put(PROP_QNAME_FLOAT_VALUE, 123.0F);
        properties.put(PROP_QNAME_DOUBLE_VALUE, 123.0);
        properties.put(PROP_QNAME_STRING_VALUE, "123.0");
        properties.put(PROP_QNAME_ML_TEXT_VALUE, new MLText("This is ML text in the default language"));
        properties.put(PROP_QNAME_DATE_VALUE, new Date());
        // Get the check values
        Map<QName, Serializable> expectedProperties = new HashMap<QName, Serializable>(properties);
        getExpectedPropertyValues(expectedProperties);

        Locale.setDefault(Locale.JAPANESE);
        
        // create a new node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_PROPERTIES,
                properties).getChildRef();
        
        // Check the properties again
        Map<QName, Serializable> checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        // Change the locale and set the properties again
        I18NUtil.setLocale(Locale.US);
        nodeService.setProperties(nodeRef, properties);

        // Check the properties again
        checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        // Change the locale and set the properties again
        I18NUtil.setLocale(Locale.UK);
        nodeService.setProperties(nodeRef, properties);

        // Check the properties again
        checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        // Change the locale and set the properties again
        I18NUtil.setLocale(Locale.US);
        nodeService.addProperties(nodeRef, properties);

        // Check the properties again
        checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        // Change the locale and set the properties again
        I18NUtil.setLocale(Locale.UK);
        nodeService.addProperties(nodeRef, properties);

        // Check the properties again
        checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        // Change the locale and set the properties again
        I18NUtil.setLocale(Locale.US);
        nodeService.setProperty(nodeRef, PROP_QNAME_DATE_VALUE, properties.get(PROP_QNAME_DATE_VALUE));

        // Check the properties again
        checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        // Change the locale and set the properties again
        I18NUtil.setLocale(Locale.UK);
        nodeService.setProperty(nodeRef, PROP_QNAME_DATE_VALUE, properties.get(PROP_QNAME_DATE_VALUE));

        // Check the properties again
        checkProperties = nodeService.getProperties(nodeRef);
        checkProperties(checkProperties, expectedProperties);
        
        setComplete();
        endTransaction();
    }
    
    /**
     * Checks that empty collections can be persisted
     */
    @SuppressWarnings("unchecked")
    public void testEmptyCollections() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_PROPERTIES).getChildRef();

        List<String> filledCollection = new ArrayList<String>(2);
        filledCollection.add("ABC");
        filledCollection.add("DEF");
        List<String> emptyCollection = Collections.emptyList();
        
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, (Serializable) filledCollection);
        List<String> checkFilledCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
        assertEquals("Filled collection didn't come back with correct values", filledCollection, checkFilledCollection);
        
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, (Serializable) emptyCollection);
        List<String> checkEmptyCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
        assertEquals("Empty collection didn't come back with correct values", emptyCollection, checkEmptyCollection);
        
        // Check that a null value is returned as null
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, null);
        List<String> checkNullCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
        assertNull("Null property should stay null", checkNullCollection);
    }
    
    /**
     * Checks that large collections can be persisted
     */
    @SuppressWarnings("unchecked")
    public void testBigCollections() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_PROPERTIES).getChildRef();

        for (int inc = 0; inc < 5; inc++)
        {
            System.out.println("----------------------------------------------");
            int collectionSize = (int) Math.pow(10, inc);
            List<String> largeCollection = new ArrayList<String>(collectionSize);
            for (int i = 0; i < collectionSize; i++)
            {
                largeCollection.add(String.format("Large-collection-value-%05d", i));
            }
            List<String> emptyCollection = Collections.emptyList();
            
            long t1 = System.nanoTime();
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, (Serializable) largeCollection);
            double tDelta = (double)(System.nanoTime() - t1)/1E6;
            System.out.println("Setting " + collectionSize + " multi-valued property took: " + tDelta + "ms");
            // Now get it back
            t1 = System.nanoTime();
            List<String> checkLargeCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
            tDelta = (double)(System.nanoTime() - t1)/1E6;
            System.out.println("First fetch of " + collectionSize + " multi-valued property took: " + tDelta + "ms");
            assertEquals("Large collection didn't come back with correct values", largeCollection, checkLargeCollection);
            
            // Get it back again
            t1 = System.nanoTime();
            checkLargeCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
            tDelta = (double)(System.nanoTime() - t1)/1E6;
            System.out.println("Second fetch of " + collectionSize + " multi-valued property took: " + tDelta + "ms");
            
            // Add a value
            largeCollection.add("First addition");
            t1 = System.nanoTime();
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, (Serializable) largeCollection);
            tDelta = (double)(System.nanoTime() - t1)/1E6;
            System.out.println("Re-setting " + largeCollection.size() + " multi-valued property took: " + tDelta + "ms");
            
            // Add another value
            largeCollection.add("Second addition");
            t1 = System.nanoTime();
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, (Serializable) largeCollection);
            tDelta = (double)(System.nanoTime() - t1)/1E6;
            System.out.println("Re-setting " + largeCollection.size() + " multi-valued property took: " + tDelta + "ms");
            
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, (Serializable) emptyCollection);
            List<String> checkEmptyCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
            assertEquals("Empty collection didn't come back with correct values", emptyCollection, checkEmptyCollection);
            
            // Check that a null value is returned as null
            nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, null);
            List<String> checkNullCollection = (List<String>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
            assertNull("Null property should stay null", checkNullCollection);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testMultiValueMLTextProperties() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_ML_PROPERTIES).getChildRef();
        
        // Create MLText properties and add to a collection
        List<MLText> mlTextCollection = new ArrayList<MLText>(2);
        MLText mlText0 = new MLText();
        mlText0.addValue(Locale.ENGLISH, "Hello");
        mlText0.addValue(Locale.FRENCH, "Bonjour");
        mlTextCollection.add(mlText0);
        MLText mlText1 = new MLText();
        mlText1.addValue(Locale.ENGLISH, "Bye bye");
        mlText1.addValue(Locale.FRENCH, "Au revoir");
        mlTextCollection.add(mlText1);
        
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE, (Serializable) mlTextCollection);
        Collection<MLText> mlTextCollectionCheck = (Collection<MLText>) nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_ML_VALUE);
        assertEquals("MLText collection didn't come back correctly.", mlTextCollection, mlTextCollectionCheck);
    }
    
    /**
     * Ensures that d:any types are handled correctly when adding values
     */
    public void testMultivaluedSerializable() throws Exception
    {
        ArrayList<String> listProp = new ArrayList<String>();

        listProp.clear();
        nodeService.addProperties(
                    rootNodeRef,
                    Collections.singletonMap(PROP_QNAME_ANY_PROP_MULTIPLE, (Serializable) listProp));
        listProp.add("ONE");
        nodeService.addProperties(
                    rootNodeRef,
                    Collections.singletonMap(PROP_QNAME_ANY_PROP_MULTIPLE, (Serializable) listProp));
        listProp.add("TWO");
        nodeService.addProperties(
                    rootNodeRef,
                    Collections.singletonMap(PROP_QNAME_ANY_PROP_MULTIPLE, (Serializable) listProp));

        listProp.clear();
        nodeService.addProperties(
                    rootNodeRef,
                    Collections.singletonMap(PROP_QNAME_ANY_PROP_SINGLE, (Serializable) listProp));
        listProp.add("ONE");
        nodeService.addProperties(
                    rootNodeRef,
                    Collections.singletonMap(PROP_QNAME_ANY_PROP_SINGLE, (Serializable) listProp));
        listProp.add("TWO");
        nodeService.addProperties(
                    rootNodeRef,
                    Collections.singletonMap(PROP_QNAME_ANY_PROP_SINGLE, (Serializable) listProp));
    }
    
    /**
     * Checks that the {@link ContentModel#ASPECT_REFERENCEABLE referencable} properties
     * are present
     */
    public void testGetReferencableProperties() throws Exception
    {
        // check individual property retrieval
        Serializable wsProtocol = nodeService.getProperty(rootNodeRef, ContentModel.PROP_STORE_PROTOCOL);
        Serializable wsIdentifier = nodeService.getProperty(rootNodeRef, ContentModel.PROP_STORE_IDENTIFIER);
        Serializable nodeUuid = nodeService.getProperty(rootNodeRef, ContentModel.PROP_NODE_UUID);
        Serializable nodeDbId = nodeService.getProperty(rootNodeRef, ContentModel.PROP_NODE_DBID);
        
        assertNotNull("Workspace Protocol property not present", wsProtocol);
        assertNotNull("Workspace Identifier property not present", wsIdentifier);
        assertNotNull("Node UUID property not present", nodeUuid);
        assertNotNull("Node DB ID property not present", nodeDbId);
        
        assertEquals("Workspace Protocol property incorrect", rootNodeRef.getStoreRef().getProtocol(), wsProtocol);
        assertEquals("Workspace Identifier property incorrect", rootNodeRef.getStoreRef().getIdentifier(), wsIdentifier);
        assertEquals("Node UUID property incorrect", rootNodeRef.getId(), nodeUuid);
        
        // check mass property retrieval
        Map<QName, Serializable> properties = nodeService.getProperties(rootNodeRef);
        assertTrue("Workspace Protocol property not present in map", properties.containsKey(ContentModel.PROP_STORE_PROTOCOL));
        assertTrue("Workspace Identifier property not present in map", properties.containsKey(ContentModel.PROP_STORE_IDENTIFIER));
        assertTrue("Node UUID property not present in map", properties.containsKey(ContentModel.PROP_NODE_UUID));
        assertTrue("Node DB ID property not present in map", properties.containsKey(ContentModel.PROP_NODE_DBID));
    }
    
    public void testReferencePropertySet() throws Exception
    {
        Serializable nodeDbId = nodeService.getProperty(rootNodeRef, ContentModel.PROP_NODE_DBID);
        // Now set it
        nodeService.setProperty(rootNodeRef, ContentModel.PROP_NODE_DBID, new Long(-1));
        Serializable nodeDbIdCheck = nodeService.getProperty(rootNodeRef, ContentModel.PROP_NODE_DBID);
        assertEquals("Cannot set Node DB ID", nodeDbId, nodeDbIdCheck);
    }
    
    public void testGetParentAssocs() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        ChildAssociationRef n3pn6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6"));
        ChildAssociationRef n5pn7Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n5_p_n7"));
        ChildAssociationRef n6pn8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8"));
        ChildAssociationRef n7n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n7_n8"));
        // get a child node's parents
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(n8Ref);
        assertEquals("Incorrect number of parents", 3, parentAssocs.size());
        assertTrue("Expected assoc not found", parentAssocs.contains(n6pn8Ref));
        assertTrue("Expected assoc not found", parentAssocs.contains(n7n8Ref));
        
        // check that we can retrieve the primary parent
        ChildAssociationRef primaryParentAssocCheck = nodeService.getPrimaryParent(n8Ref);
        assertEquals("Primary parent assoc not retrieved", n6pn8Ref, primaryParentAssocCheck);
        
        // check that the root node returns a null primary parent
        ChildAssociationRef rootNodePrimaryAssoc = nodeService.getPrimaryParent(rootNodeRef);
        assertNull("Expected null primary parent for root node", rootNodePrimaryAssoc.getParentRef());
        
        // get the parent associations based on pattern
        List<ChildAssociationRef> parentAssocRefsByQName = nodeService.getParentAssocs(
                n8Ref,
                RegexQNamePattern.MATCH_ALL,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "n7_n8"));
        assertEquals("Expected to get exactly one match", 1, parentAssocRefsByQName.size());
        
        // get the parent association based on type pattern
        List<ChildAssociationRef> childAssocRefsByTypeQName = nodeService.getChildAssocs(
                n8Ref,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                RegexQNamePattern.MATCH_ALL);
    }
    
    public void testGetChildAssocs() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"root_p_n1")).getChildRef();
        ChildAssociationRef n1pn3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_p_n3"));
        ChildAssociationRef n1n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_n4"));
        ChildAssociationRef n1n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_n8"));
        
        // get the parent node's children
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(n1Ref);
        assertEquals("Incorrect number of children", 3, childAssocRefs.size());
        // checks that the order of the children is correct
        assertEquals("First child added to n1 was primary to n3: Order of refs is wrong",
                n1pn3Ref, childAssocRefs.get(0));
        assertEquals("Second child added to n1 was to n4: Order of refs is wrong",
                n1n4Ref, childAssocRefs.get(1));
        // now set the child ordering explicitly - change the order
        nodeService.setChildAssociationIndex(n1pn3Ref, 2);
        nodeService.setChildAssociationIndex(n1n8Ref, 1);
        nodeService.setChildAssociationIndex(n1n4Ref, 0);
        
        // repeat
        childAssocRefs = nodeService.getChildAssocs(n1Ref);
        assertEquals("Order of refs is wrong", n1pn3Ref, childAssocRefs.get(2));
        assertEquals("Order of refs is wrong", n1n8Ref, childAssocRefs.get(1));
        assertEquals("Order of refs is wrong", n1n4Ref, childAssocRefs.get(0));
        
        // get the child associations based on pattern
        List<ChildAssociationRef> childAssocRefsByQName = nodeService.getChildAssocs(
                n1Ref,
                RegexQNamePattern.MATCH_ALL,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3"));
        assertEquals("Expected to get exactly one match", 1, childAssocRefsByQName.size());
        
        // get the child association based on type pattern
        List<ChildAssociationRef> childAssocRefsByTypeQName = nodeService.getChildAssocs(
                n1Ref,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                RegexQNamePattern.MATCH_ALL);
    }
    
    public void testDuplicateChildAssocCleanup() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"root_p_n1")).getChildRef();
        ChildAssociationRef n1pn3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_p_n3"));
        // Recreate the association from n1 to n3 i.e. duplicate it
        QName assocQName = QName.createQName(BaseNodeServiceTest.NAMESPACE, "dup");
        ChildAssociationRef dup1 = nodeService.addChild(
                n1pn3Ref.getParentRef(),
                n1pn3Ref.getChildRef(),
                n1pn3Ref.getTypeQName(),
                assocQName);
        ChildAssociationRef dup2 = nodeService.addChild(
                n1pn3Ref.getParentRef(),
                n1pn3Ref.getChildRef(),
                n1pn3Ref.getTypeQName(),
                assocQName);
        assertEquals("Duplicate not created", dup1, dup2);
        List<ChildAssociationRef> dupAssocs = nodeService.getChildAssocs(n1pn3Ref.getParentRef(), n1pn3Ref.getTypeQName(), assocQName);
        assertEquals("Expected duplicates", 2, dupAssocs.size());
        // Now delete the specific association
        nodeService.removeChildAssociation(dup1);
        
        setComplete();
        endTransaction();
    }
    
    public void testGetChildAssocsByChildType() throws Exception
    {
        /*
         * Level 2:     n1_p_n3     n2_p_n4     n1_n4       n2_p_n5     n1_n8
         * Containers: n1, n3, n4
         * Files:      n8
         */
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"root_p_n1")).getChildRef();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n6_p_n8")).getChildRef();
        ChildAssociationRef n1pn3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_p_n3"));
        ChildAssociationRef n1n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_n4"));
        ChildAssociationRef n1n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n1_n8"));
        
        // Get N1's container children
        List<ChildAssociationRef> childAssocRefsContainers =  nodeService.getChildAssocs(
                n1Ref,
                Collections.singleton(ContentModel.TYPE_CONTAINER));
        assertEquals("Incorrect number of cm:container children", 2, childAssocRefsContainers.size());
        assertTrue("Expected assoc not found", childAssocRefsContainers.contains(n1pn3Ref));
        assertTrue("Expected assoc not found", childAssocRefsContainers.contains(n1n4Ref));
        // Get N1's container children
        List<ChildAssociationRef> childAssocRefsFiles =  nodeService.getChildAssocs(
                n1Ref,
                Collections.singleton(BaseNodeServiceTest.TYPE_QNAME_TEST_CONTENT));
        assertEquals("Incorrect number of test:content children", 1, childAssocRefsFiles.size());
        assertTrue("Expected assoc not found", childAssocRefsFiles.contains(n1n8Ref));
    }
    
    public static class MovePolicyTester implements NodeServicePolicies.OnMoveNodePolicy
    {
        public List<ChildAssociationRef> policyAssocRefs = new ArrayList<ChildAssociationRef>(2);
        public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
        {
            policyAssocRefs.add(oldChildAssocRef);
            policyAssocRefs.add(newChildAssocRef);
        }
    };

    
    public void testMoveNode() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        ChildAssociationRef n2pn4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4"));
        ChildAssociationRef n5pn7Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n5_p_n7"));
        ChildAssociationRef n6pn8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8"));
        NodeRef n4Ref = n2pn4Ref.getChildRef();
        NodeRef n5Ref = n5pn7Ref.getParentRef();
        NodeRef n6Ref = n6pn8Ref.getParentRef();
        NodeRef n8Ref = n6pn8Ref.getChildRef();
        
        MovePolicyTester policy = new MovePolicyTester();
        // bind to listen to the deletion of a node
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
                policy,
                new JavaBehaviour(policy, "onMoveNode"));   
        
        // move n8 to n5
        ChildAssociationRef assocRef = nodeService.moveNode(
                n8Ref,
                n5Ref,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "n5_p_n8"));
        
        // check that the move policy was fired
        assertEquals("Move policy not fired", 2, policy.policyAssocRefs.size());
        
        // check that n6 is no longer the parent
        List<ChildAssociationRef> n6ChildRefs = nodeService.getChildAssocs(
                n6Ref,
                RegexQNamePattern.MATCH_ALL, QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8"));
        assertEquals("Primary child assoc is still present", 0, n6ChildRefs.size());
        // check that n5 is the parent
        ChildAssociationRef checkRef = nodeService.getPrimaryParent(n8Ref);
        assertEquals("Primary assoc incorrent", assocRef, checkRef);
        
        // check that cyclic associations are disallowed
        try
        {
            // n6 is a non-primary child of n4.  Move n4 into n6
            nodeService.moveNode(
                    n4Ref,
                    n6Ref,
                    ASSOC_TYPE_QNAME_TEST_CHILDREN,
                    QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n4"));
            fail("Failed to detect cyclic relationship during move");
        }
        catch (CyclicChildRelationshipException e)
        {
            // expected
        }
    }
    
    /**
     * Creates a named association between two new nodes
     */
    private AssociationRef createAssociation() throws Exception
    {
        return createAssociation(null);
    }

    /**
     * Creates an association between a given source and a new target
     */
    private AssociationRef createAssociation(NodeRef sourceRef) throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        fillProperties(TYPE_QNAME_TEST_CONTENT, properties);
        fillProperties(ASPECT_QNAME_TEST_TITLED, properties);
        
        if (sourceRef == null)
        {
            ChildAssociationRef childAssocRef = nodeService.createNode(
                    rootNodeRef,
                    ASSOC_TYPE_QNAME_TEST_CHILDREN,
                    QName.createQName(null, "N1"),
                    TYPE_QNAME_TEST_CONTENT,
                    properties);
            sourceRef = childAssocRef.getChildRef();
        }
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(null, "N2"),
                TYPE_QNAME_TEST_CONTENT,
                properties);
        NodeRef targetRef = childAssocRef.getChildRef();
        
        AssociationRef assocRef = nodeService.createAssociation(
                sourceRef,
                targetRef,
                ASSOC_TYPE_QNAME_TEST_NEXT);
        // done
        return assocRef;
    }
    
    public void testDuplicateAssociationDetection() throws Exception
    {
        AssociationRef assocRef = createAssociation();
        NodeRef sourceRef = assocRef.getSourceRef();
        NodeRef targetRef = assocRef.getTargetRef();
        QName qname = assocRef.getTypeQName();
        try
        {
            // attempt repeat
            nodeService.createAssociation(sourceRef, targetRef, qname);
            fail("Duplicate assocation not detected");
        }
        catch (AssociationExistsException e)
        {
            // expected
        }
    }
    
    public void testCreateAndRemoveAssociation() throws Exception
    {
        AssociationRef assocRef = createAssociation();
        NodeRef sourceRef = assocRef.getSourceRef();
        
        // create another
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        fillProperties(TYPE_QNAME_TEST_CONTENT, properties);
        fillProperties(ASPECT_QNAME_TEST_TITLED, properties);
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(null, "N3"),
                TYPE_QNAME_TEST_CONTENT,
                properties);
        NodeRef anotherTargetRef = childAssocRef.getChildRef();
        AssociationRef anotherAssocRef = nodeService.createAssociation(
                sourceRef,
                anotherTargetRef,
                ASSOC_TYPE_QNAME_TEST_NEXT);
        Long anotherAssocId = anotherAssocRef.getId();
        assertNotNull("Created association does not have an ID", anotherAssocId);
        AssociationRef anotherAssocRefCheck = nodeService.getAssoc(anotherAssocId);
        assertEquals("Assoc fetched by ID is incorrect.", anotherAssocRef, anotherAssocRefCheck);
        
        // remove assocs
        List<AssociationRef> assocs = nodeService.getTargetAssocs(sourceRef, ASSOC_TYPE_QNAME_TEST_NEXT);
        for (AssociationRef assoc : assocs)
        {
            nodeService.removeAssociation(
                    assoc.getSourceRef(),
                    assoc.getTargetRef(),
                    assoc.getTypeQName());
        }
    }
    
    public void testGetTargetAssocs() throws Exception
    {
        AssociationRef assocRef = createAssociation();
        NodeRef sourceRef = assocRef.getSourceRef();
        NodeRef targetRef = assocRef.getTargetRef();
        QName qname = assocRef.getTypeQName();
        // get the target assocs
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(sourceRef, qname);
        assertEquals("Incorrect number of targets", 1, targetAssocs.size());
        assertTrue("Target not found", targetAssocs.contains(assocRef));
        
        // Check that IDs are present
        for (AssociationRef targetAssoc : targetAssocs)
        {
            assertNotNull("Association does not have ID", targetAssoc.getId());
        }
    }
    
    public void testTargetAssoc_Ordering() throws Exception
    {
        AssociationRef assocRef = createAssociation();
        NodeRef sourceRef = assocRef.getSourceRef();
        QName qname = assocRef.getTypeQName();

        for (int i = 0; i < 99; i++)
        {
            assocRef = createAssociation(sourceRef);
        }
        
        // Now get the associations and ensure that they are in order of ID
        // because they should have been inserted in natural order
        List<AssociationRef> assocs = nodeService.getTargetAssocs(sourceRef, ASSOC_TYPE_QNAME_TEST_NEXT);
        Long lastId = 0L;
        for (AssociationRef associationRef : assocs)
        {
            Long id = associationRef.getId();
            assertNotNull("Null association ID: " + associationRef, id);
            assertTrue("Results should be in ID order", id > lastId);
            lastId = id;
        }
        
        // Now invert the association list
        Comparator<AssociationRef> descendingId = new Comparator<AssociationRef>()
        {
            @Override
            public int compare(AssociationRef assoc1, AssociationRef assoc2)
            {
                return (assoc1.getId().compareTo(assoc2.getId()) * -1);
            }
        };
        Collections.sort(assocs, descendingId);
        // Build the target node refs
        List<NodeRef> targetNodeRefs = new ArrayList<NodeRef>(100);
        for (AssociationRef associationRef : assocs)
        {
            targetNodeRefs.add(associationRef.getTargetRef());
        }

        for (int i = targetNodeRefs.size(); i > 0; i--)
        {
            // Reset them
            nodeService.setAssociations(sourceRef, ASSOC_TYPE_QNAME_TEST_NEXT, targetNodeRefs);
            
            // Recheck the order
            assocs = nodeService.getTargetAssocs(sourceRef, ASSOC_TYPE_QNAME_TEST_NEXT);
            assertEquals("Incorrect number of results", i, assocs.size());
            
            lastId = Long.MAX_VALUE;
            for (AssociationRef associationRef : assocs)
            {
                Long id = associationRef.getId();
                assertNotNull("Null association ID: " + associationRef, id);
                assertTrue("Results should be in inverse ID order", id < lastId);
                lastId = id;
            }
            // Remove one of the targets
            targetNodeRefs.remove(0);
        }
        
        setComplete();
        endTransaction();
    }
    
    public void testGetSourceAssocs() throws Exception
    {
        AssociationRef assocRef = createAssociation();
        NodeRef sourceRef = assocRef.getSourceRef();
        NodeRef targetRef = assocRef.getTargetRef();
        QName qname = assocRef.getTypeQName();
        // get the source assocs
        List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(targetRef, qname);
        assertEquals("Incorrect number of source assocs", 1, sourceAssocs.size());
        assertTrue("Source not found", sourceAssocs.contains(assocRef));
        
        // Check that IDs are present
        for (AssociationRef sourceAssoc : sourceAssocs)
        {
            assertNotNull("Association does not have ID", sourceAssoc.getId());
        }
    }
    
    /**
     * @see #buildNodeGraph() 
     */
    public void testGetPath() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE,"n6_p_n8")).getChildRef();

        // get the primary node path for n8
        Path path = nodeService.getPath(n8Ref);
        assertEquals("Primary path incorrect",
                "/{" + BaseNodeServiceTest.NAMESPACE + "}root_p_n1/{" + BaseNodeServiceTest.NAMESPACE + "}n1_p_n3/{" + BaseNodeServiceTest.NAMESPACE + "}n3_p_n6/{" + BaseNodeServiceTest.NAMESPACE + "}n6_p_n8",
                path.toString());
    }
    
    /**
     * @see #buildNodeGraph() 
     */
    public void testGetPaths() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
        NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        
        // get all paths for the root node
        Collection<Path> paths = nodeService.getPaths(rootNodeRef, false);
        assertEquals("Root node must have exactly 1 path", 1, paths.size());
        Path rootPath = paths.toArray(new Path[1])[0];
        assertNotNull("Root node path must have 1 element", rootPath.last());
        assertEquals("Root node path must have 1 element", rootPath.first(), rootPath.last());

        // get all paths for n8
        paths = nodeService.getPaths(n8Ref, false);
        assertEquals("Incorrect path count", 6, paths.size());  // n6 is a root as well
        // check that each path element has parent node ref, qname and child node ref
        for (Path path : paths)
        {
            // get the path elements
            for (Path.Element element : path)
            {
                assertTrue("Path element of incorrect type", element instanceof Path.ChildAssocElement);
                Path.ChildAssocElement childAssocElement = (Path.ChildAssocElement) element;
                ChildAssociationRef ref = childAssocElement.getRef();
                if (childAssocElement != path.first())
                {
                    // for all but the first element, the parent and assoc qname must be set
                    assertNotNull("Parent node ref not set", ref.getParentRef());
                    assertNotNull("QName not set", ref.getQName());
                }
                // all associations must have a child ref
                assertNotNull("Child node ref not set", ref.getChildRef());
            }
        }

        // get primary path for n8
        paths = nodeService.getPaths(n8Ref, true);
        assertEquals("Incorrect path count", 1, paths.size());
        
        // check that a cyclic path is detected - make n6_n1
        try
        {
            nodeService.addChild(n6Ref, n1Ref, ASSOC_TYPE_QNAME_TEST_CHILDREN, QName.createQName("n6_n1"));
            nodeService.getPaths(n6Ref, false);
            fail("Cyclic relationship not detected");
        }
        catch (CyclicChildRelationshipException e)
        {
            // expected
        }
        catch (StackOverflowError e)
        {
            throw e;
        }
    }
    
    public void testPrimaryPathCascadeDelete() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
        NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        
        // delete n1
        nodeService.deleteNode(n1Ref);
        // check that the rest disappeared
        assertFalse("n6 not cascade deleted", nodeService.exists(n6Ref));
        assertFalse("n8 not cascade deleted", nodeService.exists(n8Ref));
    }
    
    /**
     * Test that default values are set when nodes are created and aspects applied
     * 
     * @throws Exception
     */
    public void testDefaultValues() throws Exception
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("testDefaultValues"),
                TYPE_QNAME_EXTENDED_CONTENT).getChildRef();
        assertEquals(DEFAULT_VALUE, this.nodeService.getProperty(nodeRef, PROP_QNAME_PROP1));
        this.nodeService.addAspect(nodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE, null);
        assertEquals(DEFAULT_VALUE, this.nodeService.getProperty(nodeRef, PROP_QNAME_PROP2));
        
        // Ensure that default values do not overrite already set values
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(PROP_QNAME_PROP1, NOT_DEFAULT_VALUE);
        NodeRef nodeRef2 = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("testDefaultValues"),
                TYPE_QNAME_EXTENDED_CONTENT,
                props).getChildRef();                
        assertEquals(NOT_DEFAULT_VALUE, this.nodeService.getProperty(nodeRef2, PROP_QNAME_PROP1));
        Map<QName, Serializable> prop2 = new HashMap<QName, Serializable>(1);
        prop2.put(PROP_QNAME_PROP2, NOT_DEFAULT_VALUE);
        this.nodeService.addAspect(nodeRef2, ASPECT_QNAME_WITH_DEFAULT_VALUE, prop2);
        assertEquals(NOT_DEFAULT_VALUE, this.nodeService.getProperty(nodeRef2, PROP_QNAME_PROP2));
                
    }
    
    public void testMandatoryAspects()
    {
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("testDefaultValues"),
                TYPE_QNAME_TEST_CONTENT).getChildRef();
        
        // Check that the required mandatory aspects have been applied
        assertTrue(this.nodeService.hasAspect(nodeRef, ASPECT_QNAME_TEST_TITLED));
        assertTrue(this.nodeService.hasAspect(nodeRef, ASPECT_QNAME_MANDATORY));
        
        // Add an aspect with dependacies
        this.nodeService.addAspect(nodeRef, ASPECT_QNAME_TEST_MARKER, null);
        
        // Check that the dependant aspect has been applied
        assertTrue(this.nodeService.hasAspect(nodeRef, ASPECT_QNAME_TEST_MARKER));
        assertTrue(this.nodeService.hasAspect(nodeRef, ASPECT_QNAME_TEST_MARKER2));        
    }
    
    private void garbageCollect() throws Exception
    {
        // garbage collect and wait
        for (int i = 0; i < 50; i++)
        {
            Runtime.getRuntime().gc();
            synchronized(this)
            {
                this.wait(20);
            }
        }
    }
    
    private void reportFlushPerformance(
            String msg,
            Map<QName, ChildAssociationRef> lastNodeGraph,
            int testCount,
            long startBytes,
            long startTime) throws Exception
    {
        long endTime = System.nanoTime();
        double deltaTime = (double)(endTime - startTime)/1000000000D;
        System.out.println(msg + "\n" +
                "   Build and flushed " + testCount + " node graphs: \n" +
                "   total time: " + deltaTime + "s \n" +
                "   average: " + (double)testCount/deltaTime + " graphs/s");
        
        garbageCollect();
        long endBytes = Runtime.getRuntime().freeMemory();
        double diffBytes = (double)(startBytes - endBytes);
        System.out.println(
                "   total bytes: " + diffBytes/1024D/1024D + " MB \n" +
                "   average: " + (double)diffBytes/testCount/1024D + " kb/graph");
        
        
        int assocsPerGraph = lastNodeGraph.size();
        int nodesPerGraph = 0;
        for (ChildAssociationRef assoc : lastNodeGraph.values())
        {
            if (assoc.getQName().toString().contains("_p_"))
            {
                nodesPerGraph++;
            }
        }
        int totalAssocs = assocsPerGraph * testCount;
        int totalNodes = nodesPerGraph * testCount;
        System.out.println(
                "   assocs per graph: " + assocsPerGraph + "\n" +
                "   nodes per graph: " + nodesPerGraph + "\n" +
                "   total nodes: " + totalNodes + "\n" +
                "   total assocs: " + totalAssocs);
    }
    
    /**
     * Check that the duplicate child name is detected and thrown correctly
     */
    public void testDuplicateCatch() throws Exception
    {
        NodeRef parentRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        ChildAssociationRef pathARef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTENT);
        // no issue with this
        ChildAssociationRef pathBRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("pathB"),
                ContentModel.TYPE_CONTENT);
        // there should be no issue with a duplicate association names any more
        ChildAssociationRef pathBDuplicateRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("pathB"),
                ContentModel.TYPE_CONTENT);
        // Now create nodes with duplicate cm:name properties
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(5);
        props.put(ContentModel.PROP_NAME, "ABC");
        ChildAssociationRef pathAbcRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("ABC"),
                ContentModel.TYPE_CONTENT,
                props);
        try
        {
            // now check that the duplicate is detected with attention to being case-insensitive
            props.put(ContentModel.PROP_NAME, "abc");
            ChildAssociationRef pathAbcDuplicateRef = nodeService.createNode(
                    parentRef,
                    ASSOC_TYPE_QNAME_TEST_CONTAINS,
                    QName.createQName("ABC-duplicate"),
                    ContentModel.TYPE_CONTENT,
                    props);
            fail("Failed to throw duplicate child name exception");
        }
        catch (DuplicateChildNodeNameException e)
        {
            // expected
        }
    }
    
    /**
     * Create some nodes that have the same <b>cm:name</b> but use associations that don't
     * enforce uniqueness.
     */
    public void testNonDuplicateAssocsWithSuppliedName() throws Throwable
    {
        Map<QName, Serializable> properties = Collections.singletonMap(ContentModel.PROP_NAME, (Serializable) getName());
        NodeRef parentRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        ChildAssociationRef pathARef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTENT,
                properties);
        ChildAssociationRef pathBRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathB"),
                ContentModel.TYPE_CONTENT,
                properties);
    }
    
    /**
     * Create some nodes that have the no <b>cm:name</b> and use associations that enforce uniqueness.
     * <p/>
     * ALF-5001: cm:name uniqueness check can fail if the property is not set
     */
    public void testDuplicateAssocsWithoutSuppliedName() throws Throwable
    {
        Map<QName, Serializable> properties = Collections.emptyMap();
        NodeRef parentRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        ChildAssociationRef pathARef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTENT,
                properties);
        // Add the node to the same parent again
        try
        {
            ChildAssociationRef pathBRef = nodeService.addChild(
                    parentRef,
                    pathARef.getChildRef(),
                    ASSOC_TYPE_QNAME_TEST_CONTAINS,
                    QName.createQName("pathB"));
            fail("Re-added node to parent when cm:name was not set; it should have failed.");
        }
        catch (DuplicateChildNodeNameException e)
        {
            // Expected
        }
    }
    
    /**
     * Checks that the unique constraint doesn't break delete and create within the same
     * transaction.
     */
    public void testDeleteAndAddSameName() throws Exception
    {
        NodeRef parentRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        // create node ABC
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(5);
        props.put(ContentModel.PROP_NAME, "ABC");
        ChildAssociationRef pathAbcRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("ABC"),
                ContentModel.TYPE_CONTENT,
                props);
        NodeRef abcRef = pathAbcRef.getChildRef();
        // delete ABC
        nodeService.deleteNode(abcRef);
        // create it again
        pathAbcRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("ABC"),
                ContentModel.TYPE_CONTENT,
                props);
        // there should not be any failure when doing this in the same transaction
    }
    
    public void testGetByName() throws Exception
    {
        NodeRef parentRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("parent_child"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        // create node ABC
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(5);
        props.put(ContentModel.PROP_NAME, "ABC");
        ChildAssociationRef pathAbcRef = nodeService.createNode(
                parentRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("ABC"),
                ContentModel.TYPE_CONTENT,
                props);
        NodeRef abcRef = pathAbcRef.getChildRef();
        // create node DEF
        props.put(ContentModel.PROP_NAME, "DEF");
        ChildAssociationRef pathDefRef = nodeService.createNode(
                abcRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("DEF"),
                ContentModel.TYPE_CONTENT,
                props);
        NodeRef defRef = pathDefRef.getChildRef();
        // create node KLM
        props.put(ContentModel.PROP_NAME, "KLM");
        ChildAssociationRef pathKlmRef = nodeService.createNode(
                abcRef,
                ASSOC_TYPE_QNAME_TEST_CONTAINS,
                QName.createQName("KLM"),
                ContentModel.TYPE_CONTENT,
                props);
        NodeRef klmRef = pathDefRef.getChildRef();
        
        // now browse down using the node service
        NodeRef checkAbcRef = nodeService.getChildByName(parentRef, ASSOC_TYPE_QNAME_TEST_CONTAINS, "abc");
        assertNotNull("Second level, named node 'ABC' not found", checkAbcRef);
        assertEquals(abcRef, checkAbcRef);
        NodeRef checkDefRef = nodeService.getChildByName(checkAbcRef, ASSOC_TYPE_QNAME_TEST_CONTAINS, "def");
        assertNotNull("Third level, named node 'DEF' not found", checkDefRef);
        assertEquals(defRef, checkDefRef);
        // check that we get null where not present
        NodeRef checkHijRef = nodeService.getChildByName(checkAbcRef, ASSOC_TYPE_QNAME_TEST_CONTAINS, "hij");
        assertNull("Third level, named node 'HIJ' should not have been found", checkHijRef);
        
        // Now search for multiple names
        List<String> namesList = Arrays.asList("ABC", "DEF", "HIJ", "KLM");
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildrenByName(checkAbcRef, ASSOC_TYPE_QNAME_TEST_CONTAINS, namesList);
        assertEquals("Expected exactly 2 results", 2, childAssocRefs.size());
        assertTrue("Expected result not included", childAssocRefs.contains(pathDefRef));
        assertTrue("Expected result not included", childAssocRefs.contains(pathKlmRef));
    }
    
    public void testLocalizedAspect() throws Exception
    {
        nodeService.addAspect(
                rootNodeRef,
                ContentModel.ASPECT_LOCALIZED,
                Collections.<QName, Serializable>singletonMap(ContentModel.PROP_LOCALE, Locale.CANADA_FRENCH));
        // commit to check
        setComplete();
        endTransaction();
    }
        
    public static boolean behaviourExecuted = false;
    
    public void testAR1303() throws Exception
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "test.txt");
        
        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                ContentModel.ASSOC_CHILDREN, 
                ContentModel.TYPE_CONTENT, 
                props).getChildRef();
        
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        
        nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, "my description");
        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "my title");
        
        JavaBehaviour behaviour = new JavaBehaviour(this, "onUpdateProperties");        
        PolicyComponent policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                ContentModel.ASPECT_TITLED, 
                behaviour);        
        
        behaviourExecuted = false;
        
        // Update the title property and check that the behaviour has been fired
        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "changed title");
        assertTrue("The onUpdateProperties behaviour has not been fired.", behaviourExecuted);
    }
    
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        behaviourExecuted = true;       
        assertFalse(before.get(ContentModel.PROP_TITLE).toString().equals(after.get(ContentModel.PROP_TITLE).toString()));
        
        System.out.print("Before values: ");
        for (Map.Entry<QName, Serializable> entry : before.entrySet()) 
        {
            System.out.println(entry.getKey().toString() + " : " + entry.getValue().toString());
        }
        System.out.print("\nAfter values: ");
        for (Map.Entry<QName, Serializable> entry : after.entrySet()) 
        {
            System.out.println(entry.getKey().toString() + " : " + entry.getValue().toString());
        }
    }
    
    /**
     * Checks that unconvertable property values cannot be persisted.
     */
    public void testAR782() throws Exception
    {
        Map<QName, Serializable> properties = nodeService.getProperties(rootNodeRef);
        // Set usr:accountExpiryDate correctly
        properties.put(ContentModel.PROP_ACCOUNT_EXPIRY_DATE, new Date());
        nodeService.setProperties(rootNodeRef, properties);
        try
        {
            // Set usr:accountExpiryDate using something that can't be converted to a Date
            properties.put(ContentModel.PROP_ACCOUNT_EXPIRY_DATE, "blah");
            nodeService.setProperties(rootNodeRef, properties);
            fail("Failed to catch type conversion issue early.");
        }
        catch (TypeConversionException e)
        {
            // Expected
        }
    }
    
    /**
     * Helper test class for {@link BaseNodeServiceTest#testAR1414()}.
     */
    private static class AR1414Blob implements Serializable
    {
        private static final long serialVersionUID = 5616094206968290908L;
        int i = 0;
    }
    
    /**
     * Check that Serializable properties do not remain connected to the L1 session
     */
    public void testAR1414() throws Exception
    {
        AR1414Blob blob = new AR1414Blob();
        
        QName propertyQName = QName.createQName(NAMESPACE, "testAR1414Prop");
        nodeService.setProperty(rootNodeRef, propertyQName, blob);
        // Modify our original blob
        blob.i = 100;
        // Get the property
        AR1414Blob checkBlob = (AR1414Blob) nodeService.getProperty(rootNodeRef, propertyQName);
        assertNotNull(checkBlob);
        assertEquals("Blob was modified while persisted", 0, checkBlob.i);
    }
}
