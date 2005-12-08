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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Test persistence and retrieval of Hibernate-specific implementations of the
 * {@link org.alfresco.repo.domain.Node} interface
 * 
 * @author Derek Hulley
 */
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
                "TestWorkspace@" + System.currentTimeMillis());
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
        NodeKey key = new NodeKey("Random Protocol", "Random Identifier", "AAA");
        // create the node status
        NodeStatus nodeStatus = new NodeStatusImpl();
        nodeStatus.setKey(key);
        nodeStatus.setDeleted(false);
        nodeStatus.setChangeTxnId("txn:123");
        getSession().save(nodeStatus);
        // create a new Node
        Node node = new NodeImpl();
		node.setKey(key);
        node.setStore(store);   // not meaningful as it contradicts the key
        node.setTypeQName(ContentModel.TYPE_CONTAINER);
        node.setStatus(nodeStatus);
        // persist it
		try
		{
			Serializable id = getSession().save(node);
			fail("No store exists");
		}
		catch (Throwable e)
		{
			// expected
		}
		// this should not solve the problem
        node.setStore(store);
        // persist it
		try
		{
			Serializable id = getSession().save(node);
			fail("Setting store does not persist protocol and identifier attributes");
		}
		catch (Throwable e)
		{
			// expected
		}
		
		// fix the key
		key = new NodeKey(store.getKey().getProtocol(), store.getKey().getIdentifier(), "AAA");
		node.setKey(key);
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
        nodeStatus.setDeleted(false);
        nodeStatus.setChangeTxnId("txn:123");
        getSession().save(nodeStatus);
        
        // it must be able to exist without the node
        flushAndClear();
        
        // create a new Node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setKey(key);
        node.setStore(store);   // not meaningful as it contradicts the key
        node.setTypeQName(ContentModel.TYPE_CONTAINER);
        node.setStatus(nodeStatus);
        Serializable id = getSession().save(node);
        
        // flush
        flushAndClear();
        
        // is the status retrievable
        node = (Node) getSession().get(NodeImpl.class, id);
        nodeStatus = node.getStatus();
        // change the values
        nodeStatus.setChangeTxnId("txn:456");
        nodeStatus.setDeleted(true);
        // delete the node
        getSession().delete(node);
        
        // flush
        flushAndClear();
    }

    /**
     * Check that properties can be persisted and retrieved
     */
    public void testProperties() throws Exception
    {
        NodeKey key = new NodeKey(store.getKey(), "AAA");
        // create the node status
        NodeStatus nodeStatus = new NodeStatusImpl();
        nodeStatus.setKey(key);
        nodeStatus.setDeleted(false);
        nodeStatus.setChangeTxnId("txn:123");
        getSession().save(nodeStatus);
        // create a new Node
        Node node = new NodeImpl();
		node.setKey(key);
        node.setTypeQName(ContentModel.TYPE_CONTAINER);
        node.setStatus(nodeStatus);
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
        NodeKey key = new NodeKey(store.getKey(), GUID.generate());
        // create the node status
        NodeStatus nodeStatus = new NodeStatusImpl();
        nodeStatus.setKey(key);
        nodeStatus.setDeleted(false);
        nodeStatus.setChangeTxnId("txn:123");
        getSession().save(nodeStatus);
        // make a real node
        Node node = new NodeImpl();
        node.setKey(key);
        node.setStore(store);
        node.setTypeQName(ContentModel.TYPE_CMOBJECT);
        node.setStatus(nodeStatus);
        
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
    
    public void testNodeAssoc() throws Exception
    {
        NodeKey sourceKey = new NodeKey(store.getKey(), GUID.generate());
        // make a source node
        NodeStatus sourceNodeStatus = new NodeStatusImpl();
        sourceNodeStatus.setKey(sourceKey);
        sourceNodeStatus.setDeleted(false);
        sourceNodeStatus.setChangeTxnId("txn:123");
        getSession().save(sourceNodeStatus);
        Node sourceNode = new NodeImpl();
        sourceNode.setKey(sourceKey);
        sourceNode.setStore(store);
        sourceNode.setTypeQName(ContentModel.TYPE_CMOBJECT);
        sourceNode.setStatus(sourceNodeStatus);
        Serializable realNodeKey = getSession().save(sourceNode);
        
        // make a container node
        NodeKey targetKey = new NodeKey(store.getKey(), GUID.generate());
        NodeStatus targetNodeStatus = new NodeStatusImpl();
        targetNodeStatus.setKey(targetKey);
        targetNodeStatus.setDeleted(false);
        targetNodeStatus.setChangeTxnId("txn:123");
        getSession().save(targetNodeStatus);
        Node targetNode = new NodeImpl();
        targetNode.setKey(targetKey);
        targetNode.setStore(store);
        targetNode.setTypeQName(ContentModel.TYPE_CONTAINER);
        targetNode.setStatus(targetNodeStatus);
        Serializable containerNodeKey = getSession().save(targetNode);
        
        // create an association between them
        NodeAssoc assoc = new NodeAssocImpl();
        assoc.setTypeQName(QName.createQName("next"));
        assoc.buildAssociation(sourceNode, targetNode);
        getSession().save(assoc);
        
        // make another association between the same two nodes
        assoc = new NodeAssocImpl();
        assoc.setTypeQName(QName.createQName("helper"));
        assoc.buildAssociation(sourceNode, targetNode);
        getSession().save(assoc);
        
        // flush and clear the session
        getSession().flush();
        getSession().clear();
        
        // reload the source
        sourceNode = (Node) getSession().get(NodeImpl.class, sourceKey);
        assertNotNull("Source node not found", sourceNode);
        // check that the associations are present
        assertEquals("Expected exactly 2 target assocs", 2, sourceNode.getTargetNodeAssocs().size());
        
        // reload the target
        targetNode = (Node) getSession().get(NodeImpl.class, targetKey);
        assertNotNull("Target node not found", targetNode);
        // check that the associations are present
        assertEquals("Expected exactly 2 source assocs", 2, targetNode.getSourceNodeAssocs().size());
    }

    public void testChildAssoc() throws Exception
    {
        // make a content node
        NodeKey key = new NodeKey(store.getKey(), GUID.generate());
        NodeStatus contentNodeStatus = new NodeStatusImpl();
        contentNodeStatus.setKey(key);
        contentNodeStatus.setDeleted(false);
        contentNodeStatus.setChangeTxnId("txn:123");
        getSession().save(contentNodeStatus);
        Node contentNode = new NodeImpl();
		contentNode.setKey(key);
        contentNode.setStore(store);
        contentNode.setTypeQName(ContentModel.TYPE_CONTENT);
        contentNode.setStatus(contentNodeStatus);
        Serializable contentNodeKey = getSession().save(contentNode);

        // make a container node
        key = new NodeKey(store.getKey(), GUID.generate());
        NodeStatus containerNodeStatus = new NodeStatusImpl();
        containerNodeStatus.setKey(key);
        containerNodeStatus.setDeleted(false);
        containerNodeStatus.setChangeTxnId("txn:123");
        getSession().save(containerNodeStatus);
        Node containerNode = new NodeImpl();
		containerNode.setKey(key);
        containerNode.setStore(store);
        containerNode.setTypeQName(ContentModel.TYPE_CONTAINER);
        containerNode.setStatus(containerNodeStatus);
        Serializable containerNodeKey = getSession().save(containerNode);
        // create an association to the content
        ChildAssoc assoc1 = new ChildAssocImpl();
        assoc1.setIsPrimary(true);
        assoc1.setTypeQName(QName.createQName(null, "type1"));
        assoc1.setQname(QName.createQName(null, "number1"));
        assoc1.buildAssociation(containerNode, contentNode);
        getSession().save(assoc1);

        // make another association between the same two parent and child nodes
        ChildAssoc assoc2 = new ChildAssocImpl();
        assoc2.setIsPrimary(true);
        assoc2.setTypeQName(QName.createQName(null, "type1"));
        assoc2.setQname(QName.createQName(null, "number2"));
        assoc2.buildAssociation(containerNode, contentNode);
        getSession().save(assoc2);
        
        assertFalse("Hashcode incorrent", assoc2.hashCode() == 0);
        assertNotSame("Assoc equals failure", assoc1, assoc2);

//        flushAndClear();

        // reload the container
        containerNode = (Node) getSession().get(NodeImpl.class, containerNodeKey);
        assertNotNull("Node not found", containerNode);
        // check
        assertEquals("Expected exactly 2 children", 2, containerNode.getChildAssocs().size());
        for (Iterator iterator = containerNode.getChildAssocs().iterator(); iterator.hasNext(); /**/)
        {
            ChildAssoc assoc = (ChildAssoc) iterator.next();
            // the node id must be known
            assertNotNull("Node not populated on assoc", assoc.getChild());
            assertEquals("Node key on child assoc is incorrect", contentNodeKey,
                    assoc.getChild().getKey());
        }

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
        NodeKey key = new NodeKey(store.getKey(), GUID.generate());
        
        // make a node
        NodeStatus nodeStatus = new NodeStatusImpl();
        nodeStatus.setKey(key);
        nodeStatus.setDeleted(false);
        nodeStatus.setChangeTxnId("txn:123");
        getSession().save(nodeStatus);
        Node node = new NodeImpl();
        node.setKey(key);
        node.setStore(store);
        node.setTypeQName(ContentModel.TYPE_CONTENT);
        node.setStatus(nodeStatus);
        getSession().save(node);
        
        // add some aspects to the node
        Set<QName> aspects = node.getAspects();
        aspects.add(ContentModel.ASPECT_AUDITABLE);
        
        // add some properties
        Map<QName, PropertyValue> properties = node.getProperties();
        properties.put(ContentModel.PROP_NAME, new PropertyValue(DataTypeDefinition.TEXT, "ABC"));
        
        // check that the session hands back the same instance
        Node checkNode = (Node) getSession().get(NodeImpl.class, key);
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
//        assertTrue("Property value instance retrieved not the same", checkProperties)

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
            checkNode = (Node) getSession().get(NodeImpl.class, key);
            assertNotNull(checkNode);
            checkAspects = checkNode.getAspects();
    
//            assertTrue("Node retrieved was not same instance", checkNode == node);
            
            txn.commit();
        }
        catch (Throwable e)
        {
            txn.rollback();
        }
        
    }
}