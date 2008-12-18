package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.QNameDAO;
import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.BaseSpringTest;
import org.hibernate.engine.EntityKey;

public class HibernateSessionHelperTest extends BaseSpringTest
{

    
    protected void onTearDownInTransaction()
    {
        // force a flush to ensure that the database updates succeed
        try
        {
            getSession().flush();
            getSession().clear();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    public void testSimpleMark()
    {
        assertEquals(0, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        
        StoreImpl store = new StoreImpl();
        store.setProtocol(StoreRef.PROTOCOL_WORKSPACE);
        store.setIdentifier("TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
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
        
        QNameDAO qnameDAO = (QNameDAO) getApplicationContext().getBean("qnameDAO");
        Long baseQNameId = qnameDAO.getOrCreateQName(ContentModel.TYPE_BASE).getFirst();
        
        StoreImpl store = new StoreImpl();
        store.setProtocol(StoreRef.PROTOCOL_WORKSPACE);
        store.setIdentifier("TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
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
        getSession().save(transaction);
        
        assertEquals(4, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        
        Node n1 = createNode(transaction, store, "1", baseQNameId);
               
        assertEquals(5, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        
        Node n2 = createNode(transaction, store, "2", baseQNameId);
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        
        Node n3 = createNode(transaction, store, "3", baseQNameId);
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        
        Node n4 = createNode(transaction, store, "4", baseQNameId);
        
        assertEquals(8, getSession().getStatistics().getEntityCount());
        helper.mark();
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        
        Node n5 = createNode(transaction, store, "5", baseQNameId);
        
        assertEquals(9, getSession().getStatistics().getEntityCount());
        
        helper.reset();
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        getSession().get(NodeImpl.class, n5.getId());
        assertEquals(9, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        helper.reset();
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        getSession().get(NodeImpl.class, n5.getId());
        assertEquals(9, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        helper.resetAndRemoveMark();
        
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        helper.resetAndRemoveMark();
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertFalse(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        
        helper.resetAndRemoveMark();
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertFalse(sessionContainsNode(n4));
        assertFalse(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        helper.resetAndRemoveMark();
        
        assertEquals(5, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertFalse(sessionContainsNode(n4));
        assertFalse(sessionContainsNode(n3));
        assertFalse(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
   
        helper.resetAndRemoveMark();
        
        assertEquals(4, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(0, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertFalse(sessionContainsNode(n4));
        assertFalse(sessionContainsNode(n3));
        assertFalse(sessionContainsNode(n2));
        assertFalse(sessionContainsNode(n1));
        
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
        store.setProtocol(StoreRef.PROTOCOL_WORKSPACE);
        store.setIdentifier("TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
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
        
        QNameDAO qnameDAO = (QNameDAO) getApplicationContext().getBean("qnameDAO");
        Long baseQNameId = qnameDAO.getOrCreateQName(ContentModel.TYPE_BASE).getFirst();
        
        StoreImpl store = new StoreImpl();
        store.setProtocol(StoreRef.PROTOCOL_WORKSPACE);
        store.setIdentifier("TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
        // persist so that it is present in the hibernate cache
        getSession().save(store);
        
        assertEquals(2, getSession().getStatistics().getEntityCount());
        
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
        getSession().save(transaction);
        
        assertEquals(4, getSession().getStatistics().getEntityCount());
        
        HibernateSessionHelper helper = (HibernateSessionHelper)getApplicationContext().getBean("hibernateSessionHelper");
        assertNull(helper.getCurrentMark());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        helper.mark("One");
        assertEquals("One", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(1, helper.getMarks().size());
        
        Node n1 = createNode(transaction, store, "1", baseQNameId);
               
        assertEquals(5, getSession().getStatistics().getEntityCount());
        helper.mark("Two");
        assertEquals("Two", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        
        Node n2 = createNode(transaction, store, "2", baseQNameId);
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        helper.mark("Three");
        assertEquals("Three", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(3, helper.getMarks().size());
        
        Node n3 = createNode(transaction, store, "3", baseQNameId);
        
        assertEquals(7, getSession().getStatistics().getEntityCount());
        helper.mark("Four");
        assertEquals("Four", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        
        Node n4 = createNode(transaction, store, "4", baseQNameId);
        
        assertEquals(8, getSession().getStatistics().getEntityCount());
        helper.mark("Five");
        assertEquals("Five", helper.getCurrentMark());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        
        Node n5 = createNode(transaction, store, "5", baseQNameId);
        
        assertEquals(9, getSession().getStatistics().getEntityCount());
        
        helper.reset("Five");
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        getSession().get(NodeImpl.class, n5.getId());
        assertEquals(9, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        helper.reset("Five");
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        getSession().get(NodeImpl.class, n5.getId());
        assertEquals(9, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(5, helper.getMarks().size());
        assertTrue(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        assertEquals("Five", helper.getCurrentMark());
        helper.resetAndRemoveMark("Five");
        assertEquals("Four", helper.getCurrentMark());
        
        assertEquals(8, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(4, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertTrue(sessionContainsNode(n4));
        assertTrue(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
        
        helper.resetAndRemoveMark("Three");
        assertEquals("Two", helper.getCurrentMark());
        
        assertEquals(6, getSession().getStatistics().getEntityCount());
        assertTrue(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(2, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertFalse(sessionContainsNode(n4));
        assertFalse(sessionContainsNode(n3));
        assertTrue(sessionContainsNode(n2));
        assertTrue(sessionContainsNode(n1));
   
        helper.resetAndRemoveMark("One");
        assertNull(helper.getCurrentMark());
        
        assertEquals(4, getSession().getStatistics().getEntityCount());
        assertFalse(SessionSizeResourceManager.isDisableInTransaction());
        assertEquals(0, helper.getMarks().size());
        assertFalse(sessionContainsNode(n5));
        assertFalse(sessionContainsNode(n4));
        assertFalse(sessionContainsNode(n3));
        assertFalse(sessionContainsNode(n2));
        assertFalse(sessionContainsNode(n1));
        
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
        store.setProtocol(StoreRef.PROTOCOL_WORKSPACE);
        store.setIdentifier("TestWorkspace@" + getName() + " - " + System.currentTimeMillis());
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
        getSession().save(transaction);
        
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
    
    private Node createNode(TransactionImpl transaction, Store store, String uuid, Long typeQNameId)
    {
        // Create the Node
        Node node = new NodeImpl();
        node.setStore(store);
        node.setUuid(uuid);
        node.setTypeQNameId(typeQNameId);
        node.setTransaction(transaction);
        node.setDeleted(false);
        node.getAuditableProperties().setAuditValues("system", new Date(), false);
        getSession().save(node);
        
        return node;
    }
    
    @SuppressWarnings("unchecked")
    private boolean sessionContainsNode(Node node)
    {
        Long nodeId = node.getId();
        Set<EntityKey> keys = (Set<EntityKey>)getSession().getStatistics().getEntityKeys();
        for(EntityKey key : keys)
        {
            if(key.getEntityName().equals(NodeImpl.class.getName()))
            {
                if(key.getIdentifier().equals(nodeId))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
