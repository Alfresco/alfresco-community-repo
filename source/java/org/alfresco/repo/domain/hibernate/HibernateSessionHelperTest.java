package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.BaseSpringTest;
import org.hibernate.engine.EntityKey;
import org.springframework.orm.toplink.SessionReadCallback;

public class HibernateSessionHelperTest extends BaseSpringTest
{

    
    protected void onTearDownInTransaction()
    {
        // force a flush to ensure that the database updates succeed
        getSession().flush();
        getSession().clear();
    }
    
    public void testSimpleMark()
    {
        assertEquals(0, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        StoreImpl store = new StoreImpl();
        StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(1, getSession().getStatistics().getEntityCount());
        
        Server server = (Server) getSession().get(ServerImpl.class, new Long(1));
        if (server == null)
        {
            server = new ServerImpl();
            server.setIpAddress("" + "i_" + System.currentTimeMillis());
            getSession().save(server);
        }
        
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        
        TransactionImpl transaction = new TransactionImpl();
        transaction.setServer(server);
        transaction.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        Serializable txID = getSession().save(transaction); 
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        helper.reset();
        
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
        getSession().get(TransactionImpl.class, txID);
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        helper.resetAndRemoveMark();
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        assertEquals(0, helper.getMarks().size());
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
    }
    
    public void testNestedMarks()
    {
        assertEquals(0, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        StoreImpl store = new StoreImpl();
        StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(1, getSession().getStatistics().getEntityCount());
        
        Server server = (Server) getSession().get(ServerImpl.class, new Long(1));
        if (server == null)
        {
            server = new ServerImpl();
            server.setIpAddress("" + "i_" + System.currentTimeMillis());
            getSession().save(server);
        }
        
        TransactionImpl transaction = new TransactionImpl();
        transaction.setServer(server);
        transaction.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        Serializable txID = getSession().save(transaction);
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        
        NodeKey key1 = new NodeKey(store.getKey(), "1");
        createNodeStatus(transaction, key1);
               
        assertEquals(4, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        
        NodeKey key2 = new NodeKey(store.getKey(), "2");
        createNodeStatus(transaction, key2);
        
        assertEquals(5, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        
        NodeKey key3 = new NodeKey(store.getKey(), "3");
        createNodeStatus(transaction, key3);
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        
        NodeKey key4 = new NodeKey(store.getKey(), "4");
        createNodeStatus(transaction, key4);
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        
        NodeKey key5 = new NodeKey(store.getKey(), "5");
        createNodeStatus(transaction, key5);
        
        assertEquals(8, getSession().getStatistics().getEntityCount());
        
        helper.reset();
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        getSession().get(NodeStatusImpl.class, key5);
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        helper.reset();
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        getSession().get(NodeStatusImpl.class, key5);
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        helper.resetAndRemoveMark();
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        helper.resetAndRemoveMark();
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertFalse(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        
        helper.resetAndRemoveMark();
        
        assertEquals(5, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertFalse(sessionContainsNodeStatus(key4));
        assertFalse(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        helper.resetAndRemoveMark();
        
        assertEquals(4, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertFalse(sessionContainsNodeStatus(key4));
        assertFalse(sessionContainsNodeStatus(key3));
        assertFalse(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
   
        helper.resetAndRemoveMark();
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(0, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertFalse(sessionContainsNodeStatus(key4));
        assertFalse(sessionContainsNodeStatus(key3));
        assertFalse(sessionContainsNodeStatus(key2));
        assertFalse(sessionContainsNodeStatus(key1));
        
        try
        {
            helper.reset();
            fail("can not reset");
        }
        catch(HibernateSessionHelperResourceException hshre)
        {
            
        }
    }

    public void testNamedMarks()
    {
        assertEquals(0, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        StoreImpl store = new StoreImpl();
        StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(1, getSession().getStatistics().getEntityCount());
        
        Server server = (Server) getSession().get(ServerImpl.class, new Long(1));
        if (server == null)
        {
            server = new ServerImpl();
            server.setIpAddress("" + "i_" + System.currentTimeMillis());
            getSession().save(server);
        }
        
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark("One");
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        
        TransactionImpl transaction = new TransactionImpl();
        transaction.setServer(server);
        transaction.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        Serializable txID = getSession().save(transaction); 
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        helper.reset("One");
        
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
        getSession().get(TransactionImpl.class, txID);
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        helper.resetAndRemoveMark("One");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        assertEquals(0, helper.getMarks().size());
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
    }
    
    
    public void testNestedNamedMarks()
    {
        assertEquals(0, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        StoreImpl store = new StoreImpl();
        StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(1, getSession().getStatistics().getEntityCount());
        
        Server server = (Server) getSession().get(ServerImpl.class, new Long(1));
        if (server == null)
        {
            server = new ServerImpl();
            server.setIpAddress("" + "i_" + System.currentTimeMillis());
            getSession().save(server);
        }
        
        TransactionImpl transaction = new TransactionImpl();
        transaction.setServer(server);
        transaction.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        Serializable txID = getSession().save(transaction);
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertNull(helper.getCurrentMark());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark("One");
        assertEquals("One", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        
        NodeKey key1 = new NodeKey(store.getKey(), "1");
        createNodeStatus(transaction, key1);
               
        assertEquals(4, getSession().getStatistics().getEntityCount());
        helper.mark("Two");
        assertEquals("Two", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        
        NodeKey key2 = new NodeKey(store.getKey(), "2");
        createNodeStatus(transaction, key2);
        
        assertEquals(5, getSession().getStatistics().getEntityCount());
        helper.mark("Three");
        assertEquals("Three", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        
        NodeKey key3 = new NodeKey(store.getKey(), "3");
        createNodeStatus(transaction, key3);
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        helper.mark("Four");
        assertEquals("Four", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        
        NodeKey key4 = new NodeKey(store.getKey(), "4");
        createNodeStatus(transaction, key4);
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        helper.mark("Five");
        assertEquals("Five", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        
        NodeKey key5 = new NodeKey(store.getKey(), "5");
        createNodeStatus(transaction, key5);
        
        assertEquals(8, getSession().getStatistics().getEntityCount());
        
        helper.reset("Five");
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        getSession().get(NodeStatusImpl.class, key5);
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        helper.reset("Five");
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        getSession().get(NodeStatusImpl.class, key5);
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        assertEquals("Five", helper.getCurrentMark());
        helper.resetAndRemoveMark("Five");
        assertEquals("Four", helper.getCurrentMark());
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertTrue(sessionContainsNodeStatus(key4));
        assertTrue(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
        
        helper.resetAndRemoveMark("Three");
        assertEquals("Two", helper.getCurrentMark());
        
        assertEquals(5, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertFalse(sessionContainsNodeStatus(key4));
        assertFalse(sessionContainsNodeStatus(key3));
        assertTrue(sessionContainsNodeStatus(key2));
        assertTrue(sessionContainsNodeStatus(key1));
   
        helper.resetAndRemoveMark("One");
        assertNull(helper.getCurrentMark());
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(0, helper.getMarks().size());
        assertFalse(sessionContainsNodeStatus(key5));
        assertFalse(sessionContainsNodeStatus(key4));
        assertFalse(sessionContainsNodeStatus(key3));
        assertFalse(sessionContainsNodeStatus(key2));
        assertFalse(sessionContainsNodeStatus(key1));
        
        try
        {
            helper.reset("One");
            fail("can not reset");
        }
        catch(HibernateSessionHelperResourceException hshre)
        {
            
        }
    }
    
    public void voidTestRemove()
    {
        assertEquals(0, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        StoreImpl store = new StoreImpl();
        StoreKey storeKey = new StoreKey(StoreRef.PROTOCOL_WORKSPACE,
                "TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        store.setKey(storeKey);
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(1, getSession().getStatistics().getEntityCount());
        
        Server server = (Server) getSession().get(ServerImpl.class, new Long(1));
        if (server == null)
        {
            server = new ServerImpl();
            server.setIpAddress("" + "i_" + System.currentTimeMillis());
            getSession().save(server);
        }
        
        TransactionImpl transaction = new TransactionImpl();
        transaction.setServer(server);
        transaction.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        Serializable txID = getSession().save(transaction);
        
        assertEquals(3, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark("One"); 
        helper.mark("Two"); 
        helper.mark("Three"); 
        helper.mark("Four"); 
        helper.mark("Five"); 
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertEquals("Five", helper.getCurrentMark());
        
        helper.removeMark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        assertEquals("Four", helper.getCurrentMark());
        
        helper.removeMark("One");
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        assertEquals("Four", helper.getCurrentMark());
        
        helper.removeMark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        assertEquals("Three", helper.getCurrentMark());
        
        helper.removeMark("Two");
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        assertEquals("Three", helper.getCurrentMark());
        
        helper.removeMark("Three");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(0, helper.getMarks().size());
        assertNull(helper.getCurrentMark());
    }
    
    private NodeStatus createNodeStatus(TransactionImpl transaction, NodeKey key)
    {
        NodeStatus nodeStatus = new NodeStatusImpl();
        nodeStatus.setKey(key);
        nodeStatus.setTransaction(transaction);
        getSession().save(nodeStatus);
        return nodeStatus;
    }
    
    @SuppressWarnings("unchecked")
    private boolean sessionContainsNodeStatus(NodeKey nodeKey)
    {
        Set<EntityKey> keys = (Set<EntityKey>)getSession().getStatistics().getEntityKeys();
        for(EntityKey key : keys)
        {
            if(key.getEntityName().equals(NodeStatusImpl.class.getName()))
            {
                if(key.getIdentifier().equals(nodeKey))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
}
