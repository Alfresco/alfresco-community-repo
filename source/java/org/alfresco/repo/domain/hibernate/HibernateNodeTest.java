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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.hibernate.CacheMode;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Test persistence and retrieval of Hibernate-specific implementations of the
 * {@link org.alfresco.repo.domain.Node} interface
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class HibernateNodeTest extends BaseSpringTest
{
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/HibernateNodeTest";
    
    private Store store;
    
    public HibernateNodeTest()
    {
    }
    
    protected void onSetUpInTransaction() throws Exception
    {
        store = new StoreImpl();
		StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + System.currentTimeMillis() + " - " + System.nanoTime());
		store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
    }
    
    protected void onTearDownInTransaction()
    {
        // force a flush to ensure that the database updates succeed
        getSession().flush();
        getSession().clear();
    }

    public void testSetUp() throws Exception
    {
        assertNotNull("Workspace not initialised", store);
    }
    
	public void testGetStore() throws Exception
	{
        // create a new Node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setUuid(GUID.generate());
        node.setTypeQName(ContentModel.TYPE_CONTAINER);

        // now it should work
		Serializable id = getSession().save(node);

        // throw the reference away and get the a new one for the id
        node = (Node) getSession().load(NodeImpl.class, id);
        assertNotNull("Node not found", node);
		// check that the store has been loaded
		Store loadedStore = node.getStore();
		assertNotNull("Store not present on node", loadedStore);
		assertEquals("Incorrect store key", store, loadedStore);
	}
    
    public void testNodeStatus()
    {
        NodeKey key = new NodeKey(store.getKey(), "AAA");
        // create the node status
        NodeStatus nodeStatus = new NodeStatusImpl();
        nodeStatus.setKey(key);
        nodeStatus.setChangeTxnId("txn:123");
        getSession().save(nodeStatus);
        
        // create a new Node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setUuid(GUID.generate());
        node.setTypeQName(ContentModel.TYPE_CONTAINER);
        Serializable nodeId = getSession().save(node);

        // This should all be fine.  The node does not HAVE to have a status.
        flushAndClear();

        // set the node
        nodeStatus = (NodeStatus) getSession().get(NodeStatusImpl.class, key);
        nodeStatus.setNode(node);
        flushAndClear();

        // is the node retrievable?
        nodeStatus = (NodeStatus) getSession().get(NodeStatusImpl.class, key);
        node = nodeStatus.getNode();
        assertNotNull("Node was not attached to status", node);
        // change the values
        nodeStatus.setChangeTxnId("txn:456");
        // delete the node
        getSession().delete(node);
        
        try
        {
            flushAndClear();
            fail("Node status may not refer to non-existent node");
        }
        catch(ConstraintViolationException e)
        {
            // expected
        }
    }

    /**
     * Check that properties can be persisted and retrieved
     */
    public void testProperties() throws Exception
    {
        // create a new Node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setUuid(GUID.generate());
        node.setTypeQName(ContentModel.TYPE_CONTAINER);
        // give it a property map
        Map<QName, PropertyValue> propertyMap = new HashMap<QName, PropertyValue>(5);
        QName propertyQName = QName.createQName("{}A");
        PropertyValue propertyValue = new PropertyValue(DataTypeDefinition.TEXT, "AAA");
        propertyMap.put(propertyQName, propertyValue);
        node.getProperties().putAll(propertyMap);
        // persist it
        Serializable id = getSession().save(node);

        // throw the reference away and get the a new one for the id
        node = (Node) getSession().load(NodeImpl.class, id);
        assertNotNull("Node not found", node);
        // extract the Map
        propertyMap = node.getProperties();
        assertNotNull("Map not persisted", propertyMap);
        // ensure that the value is present
        assertNotNull("Property value not present in map", QName.createQName("{}A"));
    }

    /**
     * Check that aspect qnames can be added and removed from a node and that they
     * are persisted correctly 
     */
    public void testAspects() throws Exception
    {
        // make a real node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setUuid(GUID.generate());
        node.setTypeQName(ContentModel.TYPE_CMOBJECT);
        
        // add some aspects
        QName aspect1 = QName.createQName(TEST_NAMESPACE, "1");
        QName aspect2 = QName.createQName(TEST_NAMESPACE, "2");
        QName aspect3 = QName.createQName(TEST_NAMESPACE, "3");
        QName aspect4 = QName.createQName(TEST_NAMESPACE, "4");
        Set<QName> aspects = node.getAspects();
        aspects.add(aspect1);
        aspects.add(aspect2);
        aspects.add(aspect3);
        aspects.add(aspect4);
        assertFalse("Set did not eliminate duplicate aspect qname", aspects.add(aspect4));
        
        // persist
        Serializable id = getSession().save(node);
        
        // flush and clear
        flushAndClear();
        
        // get node and check aspects
        node = (Node) getSession().get(NodeImpl.class, id);
        assertNotNull("Node not persisted", node);
        aspects = node.getAspects();
        assertEquals("Not all aspects persisted", 4, aspects.size());
    }
    
    public void testChildAssoc() throws Exception
    {
        // make a content node
        Node contentNode = new NodeImpl();
        contentNode.setStore(store);
        contentNode.setUuid(GUID.generate());
        contentNode.setTypeQName(ContentModel.TYPE_CONTENT);
        Serializable contentNodeId = getSession().save(contentNode);

        // make a container node
        Node containerNode = new NodeImpl();
        containerNode.setStore(store);
        containerNode.setUuid(GUID.generate());
        containerNode.setTypeQName(ContentModel.TYPE_CONTAINER);
        Serializable containerNodeId = getSession().save(containerNode);
        // create an association to the content
        ChildAssoc assoc1 = new ChildAssocImpl();
        assoc1.setIsPrimary(true);
        assoc1.setTypeQName(QName.createQName(null, "type1"));
        assoc1.setQname(QName.createQName(null, "number1"));
        assoc1.setChildNodeName("number1");
        assoc1.setChildNodeNameCrc(1);
        assoc1.buildAssociation(containerNode, contentNode);
        getSession().save(assoc1);

        // make another association between the same two parent and child nodes
        ChildAssoc assoc2 = new ChildAssocImpl();
        assoc2.setIsPrimary(true);
        assoc2.setTypeQName(QName.createQName(null, "type2"));
        assoc2.setQname(QName.createQName(null, "number2"));
        assoc2.setChildNodeName("number2");
        assoc2.setChildNodeNameCrc(2);
        assoc2.buildAssociation(containerNode, contentNode);
        getSession().save(assoc2);
        
        assertFalse("Hashcode incorrent", assoc2.hashCode() == 0);
        assertNotSame("Assoc equals failure", assoc1, assoc2);

        // reload the container
        containerNode = (Node) getSession().get(NodeImpl.class, containerNodeId);
        assertNotNull("Node not found", containerNode);

        // check that we can traverse the association from the child
        Collection<ChildAssoc> parentAssocs = contentNode.getParentAssocs();
        assertEquals("Expected exactly 2 parent assocs", 2, parentAssocs.size());
        parentAssocs = new HashSet<ChildAssoc>(parentAssocs);
        for (ChildAssoc assoc : parentAssocs)
        {
            // maintain inverse assoc sets
            assoc.removeAssociation();
            // remove the assoc
            getSession().delete(assoc);
        }
        
        // check that the child now has zero parents
        parentAssocs = contentNode.getParentAssocs();
        assertEquals("Expected exactly 0 parent assocs", 0, parentAssocs.size());
    }
    
    /**
     * Allows tracing of L2 cache
     */
    public void testCaching() throws Exception
    {
        // make a node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setUuid(GUID.generate());
        node.setTypeQName(ContentModel.TYPE_CONTENT);
        Serializable nodeId = getSession().save(node);
        
        // add some aspects to the node
        Set<QName> aspects = node.getAspects();
        aspects.add(ContentModel.ASPECT_AUDITABLE);
        
        // add some properties
        Map<QName, PropertyValue> properties = node.getProperties();
        properties.put(ContentModel.PROP_NAME, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        
        // check that the session hands back the same instance
        Node checkNode = (Node) getSession().get(NodeImpl.class, nodeId);
        assertNotNull(checkNode);
        assertTrue("Node retrieved was not same instance", checkNode == node);
        
        Set<QName> checkAspects = checkNode.getAspects();
        assertTrue("Aspect set retrieved was not the same instance", checkAspects == aspects);
        assertEquals("Incorrect number of aspects", 1, checkAspects.size());
        QName checkQName = (QName) checkAspects.toArray()[0];
        assertTrue("QName retrieved was not the same instance", checkQName == ContentModel.ASPECT_AUDITABLE);
        
        Map<QName, PropertyValue> checkProperties = checkNode.getProperties();
        assertTrue("Propery map retrieved was not the same instance", checkProperties == properties);
        assertTrue("Property not found", checkProperties.containsKey(ContentModel.PROP_NAME));

        flushAndClear();
        // commit the transaction
        setComplete();
        endTransaction();
        
        TransactionService transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            // check that the L2 cache hands back the same instance
            checkNode = (Node) getSession().get(NodeImpl.class, nodeId);
            assertNotNull(checkNode);
            checkAspects = checkNode.getAspects();
    
            txn.commit();
        }
        catch (Throwable e)
        {
            txn.rollback();
        }
    }
    
    /**
     * Create some simple parent-child relationships and flush them.  Then read them back in without
     * using the L2 cache.
     */
    public void testQueryJoins() throws Exception
    {
        getSession().setCacheMode(CacheMode.IGNORE);
        
        // make a container node
        Node containerNode = new NodeImpl();
        containerNode.setStore(store);
        containerNode.setUuid(GUID.generate());
        containerNode.setTypeQName(ContentModel.TYPE_CONTAINER);
        containerNode.getProperties().put(ContentModel.PROP_AUTHOR, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        containerNode.getProperties().put(ContentModel.PROP_ARCHIVED_BY, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        containerNode.getAspects().add(ContentModel.ASPECT_AUDITABLE);
        Serializable containerNodeId = getSession().save(containerNode);
        NodeKey containerNodeKey = new NodeKey(containerNode.getNodeRef());
        NodeStatus containerNodeStatus = new NodeStatusImpl();
        containerNodeStatus.setKey(containerNodeKey);
        containerNodeStatus.setNode(containerNode);
        containerNodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        getSession().save(containerNodeStatus);
        // make content node 1
        Node contentNode1 = new NodeImpl();
        contentNode1.setStore(store);
        contentNode1.setUuid(GUID.generate());
        contentNode1.setTypeQName(ContentModel.TYPE_CONTENT);
        contentNode1.getProperties().put(ContentModel.PROP_AUTHOR, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        contentNode1.getProperties().put(ContentModel.PROP_ARCHIVED_BY, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        contentNode1.getAspects().add(ContentModel.ASPECT_AUDITABLE);
        Serializable contentNode1Id = getSession().save(contentNode1);
        NodeKey contentNodeKey1 = new NodeKey(contentNode1.getNodeRef());
        NodeStatus contentNodeStatus1 = new NodeStatusImpl();
        contentNodeStatus1.setKey(contentNodeKey1);
        contentNodeStatus1.setNode(contentNode1);
        contentNodeStatus1.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        getSession().save(contentNodeStatus1);
        // make content node 2
        Node contentNode2 = new NodeImpl();
        contentNode2.setStore(store);
        contentNode2.setUuid(GUID.generate());
        contentNode2.setTypeQName(ContentModel.TYPE_CONTENT);
        Serializable contentNode2Id = getSession().save(contentNode2);
        contentNode2.getProperties().put(ContentModel.PROP_AUTHOR, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        contentNode2.getProperties().put(ContentModel.PROP_ARCHIVED_BY, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        contentNode2.getAspects().add(ContentModel.ASPECT_AUDITABLE);
        NodeKey contentNodeKey2 = new NodeKey(contentNode2.getNodeRef());
        NodeStatus contentNodeStatus2 = new NodeStatusImpl();
        contentNodeStatus2.setKey(contentNodeKey2);
        contentNodeStatus2.setNode(contentNode2);
        contentNodeStatus2.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        getSession().save(contentNodeStatus2);
        // create an association to content 1
        ChildAssoc assoc1 = new ChildAssocImpl();
        assoc1.setIsPrimary(true);
        assoc1.setTypeQName(QName.createQName(null, "type1"));
        assoc1.setQname(QName.createQName(null, "number1"));
        assoc1.setChildNodeName("number1");
        assoc1.setChildNodeNameCrc(1);
        assoc1.buildAssociation(containerNode, contentNode1);
        getSession().save(assoc1);
        // create an association to content 2
        ChildAssoc assoc2 = new ChildAssocImpl();
        assoc2.setIsPrimary(true);
        assoc2.setTypeQName(QName.createQName(null, "type2"));
        assoc2.setQname(QName.createQName(null, "number2"));
        assoc2.setChildNodeName("number2");
        assoc2.setChildNodeNameCrc(2);
        assoc2.buildAssociation(containerNode, contentNode2);
        getSession().save(assoc2);
        
        // make sure that there are no entities cached in either L1 or L2
        getSession().flush();
        getSession().clear();

        // now read the structure back in from the container down
        containerNodeStatus = (NodeStatus) getSession().get(NodeStatusImpl.class, containerNodeKey);
        containerNode = containerNodeStatus.getNode();
        
        // clear out again
        getSession().clear();

        // expect that just the specific property gets removed in the delete statement
        getSession().flush();
        getSession().clear();
        
        // Create a second association to content 2
        // create an association to content 2
        containerNodeStatus = (NodeStatus) getSession().get(NodeStatusImpl.class, containerNodeKey);
        containerNode = containerNodeStatus.getNode();
        contentNodeStatus2 = (NodeStatus) getSession().get(NodeStatusImpl.class, contentNodeKey2);
        contentNode2 = contentNodeStatus2.getNode();
        ChildAssoc assoc3 = new ChildAssocImpl();
        assoc3.setIsPrimary(false);
        assoc3.setTypeQName(QName.createQName(null, "type3"));
        assoc3.setQname(QName.createQName(null, "number3"));
        assoc3.setChildNodeName("number3");
        assoc3.setChildNodeNameCrc(2);
        assoc3.buildAssociation(containerNode, contentNode2);  // check whether the children are pulled in for this
        getSession().save(assoc3);

        // flush it
        getSession().flush();
        getSession().clear();
    }
}