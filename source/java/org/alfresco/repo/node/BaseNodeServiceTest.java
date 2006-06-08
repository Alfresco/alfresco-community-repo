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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;

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
    public static final QName TYPE_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");
    public static final QName TYPE_QNAME_TEST_MANY_PROPERTIES = QName.createQName(NAMESPACE, "many-properties");
    public static final QName TYPE_QNAME_EXTENDED_CONTENT = QName.createQName(NAMESPACE, "extendedcontent");
    public static final QName ASPECT_QNAME_TEST_TITLED = QName.createQName(NAMESPACE, "titled");
    public static final QName ASPECT_QNAME_TEST_MARKER = QName.createQName(NAMESPACE, "marker");
    public static final QName ASPECT_QNAME_TEST_MARKER2 = QName.createQName(NAMESPACE, "marker2");
    public static final QName ASPECT_QNAME_MANDATORY = QName.createQName(NAMESPACE, "mandatoryaspect");
    public static final QName ASPECT_QNAME_WITH_DEFAULT_VALUE = QName.createQName(NAMESPACE, "withDefaultValue");
    public static final QName PROP_QNAME_TEST_TITLE = QName.createQName(NAMESPACE, "title");
    public static final QName PROP_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");
    public static final QName PROP_QNAME_BOOLEAN_VALUE = QName.createQName(NAMESPACE, "booleanValue");
    public static final QName PROP_QNAME_INTEGER_VALUE = QName.createQName(NAMESPACE, "integerValue");
    public static final QName PROP_QNAME_LONG_VALUE = QName.createQName(NAMESPACE, "longValue");
    public static final QName PROP_QNAME_FLOAT_VALUE = QName.createQName(NAMESPACE, "floatValue");
    public static final QName PROP_QNAME_DOUBLE_VALUE = QName.createQName(NAMESPACE, "doubleValue");
    public static final QName PROP_QNAME_STRING_VALUE = QName.createQName(NAMESPACE, "stringValue");
    public static final QName PROP_QNAME_DATE_VALUE = QName.createQName(NAMESPACE, "dateValue");
    public static final QName PROP_QNAME_SERIALIZABLE_VALUE = QName.createQName(NAMESPACE, "serializableValue");
    public static final QName PROP_QNAME_NODEREF_VALUE = QName.createQName(NAMESPACE, "nodeRefValue");
    public static final QName PROP_QNAME_QNAME_VALUE = QName.createQName(NAMESPACE, "qnameValue");
    public static final QName PROP_QNAME_CONTENT_VALUE = QName.createQName(NAMESPACE, "contentValue");
    public static final QName PROP_QNAME_PATH_VALUE = QName.createQName(NAMESPACE, "pathValue");
    public static final QName PROP_QNAME_CATEGORY_VALUE = QName.createQName(NAMESPACE, "categoryValue");
    public static final QName PROP_QNAME_NULL_VALUE = QName.createQName(NAMESPACE, "nullValue");
    public static final QName PROP_QNAME_MULTI_VALUE = QName.createQName(NAMESPACE, "multiValue");    
    public static final QName PROP_QNAME_PROP1 = QName.createQName(NAMESPACE, "prop1");
    public static final QName PROP_QNAME_PROP2 = QName.createQName(NAMESPACE, "prop2");
    public static final QName ASSOC_TYPE_QNAME_TEST_CHILDREN = ContentModel.ASSOC_CHILDREN;
    public static final QName ASSOC_TYPE_QNAME_TEST_NEXT = QName.createQName(NAMESPACE, "next");
    
    public static final QName TYPE_QNAME_TEST_MULTIPLE_TESTER = QName.createQName(NAMESPACE, "multiple-tester");
    public static final QName PROP_QNAME_STRING_PROP_SINGLE = QName.createQName(NAMESPACE, "stringprop-single");
    public static final QName PROP_QNAME_STRING_PROP_MULTIPLE = QName.createQName(NAMESPACE, "stringprop-multiple");
    public static final QName PROP_QNAME_ANY_PROP_SINGLE = QName.createQName(NAMESPACE, "anyprop-single");
    public static final QName PROP_QNAME_ANY_PROP_MULTIPLE = QName.createQName(NAMESPACE, "anyprop-multiple");
    
    protected PolicyComponent policyComponent;
    protected DictionaryService dictionaryService;
    protected TransactionService transactionService;
    protected AuthenticationComponent authenticationComponent;
    protected NodeDaoService nodeDaoService;
    protected NodeService nodeService;
    /** populated during setup */
    protected NodeRef rootNodeRef;

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
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
                "Test_" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(storeRef);
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
     * Level 2:     n1_p_n3     n2_p_n4     n1_n4       n2_p_n5
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

//        // flush and clear
//        getSession().flush();
//        getSession().clear();
        
        // done
        return ret;
    }
    
    private int countNodesByReference(NodeRef nodeRef)
    {
        String query =
                "select count(node.uuid)" +
                " from " +
                NodeImpl.class.getName() + " node" +
                " where" +
                "    node.uuid = ? and" +
                "    node.store.key.protocol = ? and" +
                "    node.store.key.identifier = ?";
        Session session = getSession();
        List results = session.createQuery(query)
            .setString(0, nodeRef.getId())
            .setString(1, nodeRef.getStoreRef().getProtocol())
            .setString(2, nodeRef.getStoreRef().getIdentifier())
            .list();
        Integer count = (Integer) results.get(0);
        return count.intValue();
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
        
        // Now change the type
        this.nodeService.setType(nodeRef, TYPE_QNAME_EXTENDED_CONTENT);
        assertEquals(TYPE_QNAME_EXTENDED_CONTENT, this.nodeService.getType(nodeRef));        
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
        for (QName propertyName : propertyDefs.keySet())
        {
            Serializable value = new Long(System.currentTimeMillis());
            // add it
            properties.put(propertyName, value);
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
        // add the content aspect to the node, but don't supply any properties
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
    
    public void testCreateNodeNoProperties() throws Exception
    {
        // flush to ensure that the pure JDBC query will work
        ChildAssociationRef assocRef = nodeService.createNode(rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("path1"),
                ContentModel.TYPE_CONTAINER);
        NodeRef nodeRef = assocRef.getChildRef();
        // count the nodes with the given id
        int count = countNodesByReference(nodeRef);
        assertEquals("Unexpected number of nodes present", 1, count);
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
    
    public static class BadOnDeleteNodePolicy implements
            NodeServicePolicies.OnDeleteNodePolicy,
            NodeServicePolicies.BeforeDeleteNodePolicy
    {
        private NodeService nodeService;
        private List<NodeRef> deletedNodeRefs;
        
        public BadOnDeleteNodePolicy(NodeService nodeService, List<NodeRef> deletedNodeRefs)
        {
            this.nodeService = nodeService;
            this.deletedNodeRefs = deletedNodeRefs;
        }
        
        public void beforeDeleteNode(NodeRef nodeRef)
        {
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

        public void onDeleteNode(ChildAssociationRef childAssocRef)
        {
            // add the child to the list
            deletedNodeRefs.add(childAssocRef.getChildRef());
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
    
    public void testDelete() throws Exception
    {
        final List<NodeRef> deletedNodeRefs = new ArrayList<NodeRef>(5);
        
        NodeServicePolicies.OnDeleteNodePolicy policy = new BadOnDeleteNodePolicy(nodeService, deletedNodeRefs);
        // bind to listen to the deletion of a node
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                policy,
                new JavaBehaviour(policy, "onDeleteNode"));   
        
        // build the node and commit the node graph
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph(nodeService, rootNodeRef);
        NodeRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1")).getChildRef();
        NodeRef n3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3")).getChildRef();
        NodeRef n4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4")).getChildRef();
        NodeRef n6Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n3_p_n6")).getChildRef();
        NodeRef n8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8")).getChildRef();
        
        // delete n1
        nodeService.deleteNode(n1Ref);
        assertEquals("Node not directly deleted", 0, countNodesByReference(n1Ref));
        assertEquals("Node not cascade deleted", 0, countNodesByReference(n3Ref));
        assertEquals("Node incorrectly cascade deleted", 1, countNodesByReference(n4Ref));
        assertEquals("Node not cascade deleted", 0, countNodesByReference(n6Ref));
        assertEquals("Node not cascade deleted", 0, countNodesByReference(n8Ref));
        
        // commit to check
        setComplete();
        endTransaction();
    }
    
    private int countChildrenOfNode(NodeRef nodeRef)
    {
        String query =
                "select node.childAssocs" +
                " from " +
                NodeImpl.class.getName() + " node" +
                " where node.uuid = ?";
        Session session = getSession();
        List results = session.createQuery(query)
            .setString(0, nodeRef.getId())
            .list();
        int count = results.size();
        return count;
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
        NodeRef childNodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER).getChildRef();
         int countBefore = countChildrenOfNode(rootNodeRef);
         assertEquals("Root children count incorrect", 1, countBefore);
        // associate the two nodes
        nodeService.addChild(
                rootNodeRef,
                childNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathB"));
        // there should now be 2 child assocs on the root
         int countAfter = countChildrenOfNode(rootNodeRef);
         assertEquals("Root children count incorrect", 2, countAfter);
         
         // now attempt to create a cyclical relationship
         try
         {
             nodeService.addChild(
                     childNodeRef,
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
        ChildAssociationRef pathBRef = nodeService.addChild(
                parentRef, childARef, ASSOC_TYPE_QNAME_TEST_CHILDREN, QName.createQName("pathB"));
        ChildAssociationRef pathCRef = nodeService.addChild(
                parentRef, childARef, ASSOC_TYPE_QNAME_TEST_CHILDREN, QName.createQName("pathC"));
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
    
    public void testAddAndRemoveChild() throws Exception
    {
        ChildAssociationRef pathARef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                ContentModel.TYPE_CONTAINER);
        NodeRef childRef = pathARef.getChildRef();
        // make a duplication, but non-primary, child associaton
        nodeService.addChild(
                rootNodeRef,
                pathARef.getChildRef(),
                pathARef.getTypeQName(),
                pathARef.getQName());
        // now remove the association - it will cascade to the child
        nodeService.removeChild(rootNodeRef, childRef);
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
        
        // make sure that our integrity allows this
        AlfrescoTransactionSupport.flush();
        
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
    
    /**
     * Ensures that the type you get out of a <b>d:any</b> property is the type that you
     * put in.
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
    
    public void testMultiProp() throws Exception
    {
        QName undeclaredPropQName = QName.createQName(NAMESPACE, getName());
        // create node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MULTIPLE_TESTER).getChildRef();
        // commit as we will be breaking the transaction in the test
        setComplete();
        endTransaction();
        
        // each of these tests will be in a new transaction started by the NodeService
        
        ArrayList<String> values = new ArrayList<String>(1);
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
        // this should fail as we are passing multiple values into a non-any that is multiple=false
        try
        {
            nodeService.setProperty(nodeRef, PROP_QNAME_STRING_PROP_SINGLE, values);
        }
        catch (DictionaryException e)
        {
            // expected
        }
    }
    
    /**
     * Check that properties go in and come out in the correct format
     */
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
        properties.put(PROP_QNAME_DATE_VALUE, new Date());
        properties.put(PROP_QNAME_SERIALIZABLE_VALUE, "456");
        properties.put(PROP_QNAME_NODEREF_VALUE, rootNodeRef);
        properties.put(PROP_QNAME_QNAME_VALUE, TYPE_QNAME_TEST_CONTENT);
        properties.put(PROP_QNAME_PATH_VALUE, pathProperty);
        properties.put(PROP_QNAME_CONTENT_VALUE, new ContentData("url", "text/plain", 88L, "UTF-8"));
        properties.put(PROP_QNAME_CATEGORY_VALUE, rootNodeRef);
        properties.put(PROP_QNAME_NULL_VALUE, null);
        properties.put(PROP_QNAME_MULTI_VALUE, listProperty);
        
        // create a new node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("pathA"),
                TYPE_QNAME_TEST_MANY_PROPERTIES,
                properties).getChildRef();
        
        // persist
        flushAndClear();
        
        // get the properties back
        Map<QName, Serializable> checkProperties = nodeService.getProperties(nodeRef);
        // check
        for (QName qname : properties.keySet())
        {
            Serializable value = properties.get(qname);
            Serializable checkValue = checkProperties.get(qname);
            assertEquals("Property mismatch - " + qname, value, checkValue);
        }
        
        // check multi-valued properties are created where necessary
        nodeService.setProperty(nodeRef, PROP_QNAME_MULTI_VALUE, "GHI");
        Serializable checkProperty = nodeService.getProperty(nodeRef, PROP_QNAME_MULTI_VALUE);
        assertTrue("Property not converted to a Collection", checkProperty instanceof Collection);
        assertTrue("Collection doesn't contain value", ((Collection)checkProperty).contains("GHI"));
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
        
        assertNotNull("Workspace Protocol property not present", wsProtocol);
        assertNotNull("Workspace Identifier property not present", wsIdentifier);
        assertNotNull("Node UUID property not present", nodeUuid);
        
        assertEquals("Workspace Protocol property incorrect", rootNodeRef.getStoreRef().getProtocol(), wsProtocol);
        assertEquals("Workspace Identifier property incorrect", rootNodeRef.getStoreRef().getIdentifier(), wsIdentifier);
        assertEquals("Node UUID property incorrect", rootNodeRef.getId(), nodeUuid);
        
        // check mass property retrieval
        Map<QName, Serializable> properties = nodeService.getProperties(rootNodeRef);
        assertTrue("Workspace Protocol property not present in map", properties.containsKey(ContentModel.PROP_STORE_PROTOCOL));
        assertTrue("Workspace Identifier property not present in map", properties.containsKey(ContentModel.PROP_STORE_IDENTIFIER));
        assertTrue("Node UUID property not present in map", properties.containsKey(ContentModel.PROP_NODE_UUID));
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
        assertEquals("Incorrect number of parents", 2, parentAssocs.size());
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
        
        // get the parent node's children
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(n1Ref);
        assertEquals("Incorrect number of children", 2, childAssocRefs.size());
        // checks that the order of the children is correct
        assertEquals("First child added to n1 was primary to n3: Order of refs is wrong",
                n1pn3Ref, childAssocRefs.get(0));
        assertEquals("Second child added to n1 was to n4: Order of refs is wrong",
                n1n4Ref, childAssocRefs.get(1));
        // now set the child ordering explicitly - change the order
        nodeService.setChildAssociationIndex(n1pn3Ref, 1);
        nodeService.setChildAssociationIndex(n1n4Ref, 0);
        
        // repeat
        childAssocRefs = nodeService.getChildAssocs(n1Ref);
        assertEquals("Order of refs is wrong", n1pn3Ref, childAssocRefs.get(1));
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
        // move n8 to n5
        ChildAssociationRef assocRef = nodeService.moveNode(
                n8Ref,
                n5Ref,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(BaseNodeServiceTest.NAMESPACE, "n5_p_n8"));
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
     * Creates a named association between two nodes
     * 
     * @return Returns an array of [source real NodeRef][target reference NodeRef][assoc name String]
     */
    private AssociationRef createAssociation() throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(5);
        fillProperties(TYPE_QNAME_TEST_CONTENT, properties);
        fillProperties(ASPECT_QNAME_TEST_TITLED, properties);
        
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(null, "N1"),
                TYPE_QNAME_TEST_CONTENT,
                properties);
        NodeRef sourceRef = childAssocRef.getChildRef();
        childAssocRef = nodeService.createNode(
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
        assertEquals("Incorrect path count", 5, paths.size());  // n6 is a root as well
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
        assertEquals("defaultValue", this.nodeService.getProperty(nodeRef, PROP_QNAME_PROP1));
        this.nodeService.addAspect(nodeRef, ASPECT_QNAME_WITH_DEFAULT_VALUE, null);
        assertEquals("defaultValue", this.nodeService.getProperty(nodeRef, PROP_QNAME_PROP2));
        
        // Ensure that default values do not overrite already set values
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(PROP_QNAME_PROP1, "notDefaultValue");
        NodeRef nodeRef2 = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName("testDefaultValues"),
                TYPE_QNAME_EXTENDED_CONTENT,
                props).getChildRef();                
        assertEquals("notDefaultValue", this.nodeService.getProperty(nodeRef2, PROP_QNAME_PROP1));
        Map<QName, Serializable> prop2 = new HashMap<QName, Serializable>(1);
        prop2.put(PROP_QNAME_PROP2, "notDefaultValue");
        this.nodeService.addAspect(nodeRef2, ASPECT_QNAME_WITH_DEFAULT_VALUE, prop2);
        assertEquals("notDefaultValue", this.nodeService.getProperty(nodeRef2, PROP_QNAME_PROP2));
                
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
}
