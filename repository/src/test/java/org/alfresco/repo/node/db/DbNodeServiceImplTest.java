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
package org.alfresco.repo.node.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLClusterNDBDialect;
import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.ChildAssocRefQueryCallback;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.domain.node.ibatis.NodeDAOImpl;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.node.cleanup.NodeCleanupRegistry;
import org.alfresco.repo.node.db.NodeStringLengthWorker.NodeStringLengthWorkResult;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.DBTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see org.alfresco.repo.node.db.DbNodeServiceImpl
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
@Transactional
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class DbNodeServiceImplTest extends BaseNodeServiceTest
{
    private TransactionService txnService;
    private NodeDAO nodeDAO;
    private DictionaryService dictionaryService;
    
    protected NodeService getNodeService()
    {
        // Force cascading
        DbNodeServiceImpl dbNodeServiceImpl = (DbNodeServiceImpl) applicationContext.getBean("dbNodeServiceImpl");
        
        return (NodeService) applicationContext.getBean("dbNodeService");
    }

    @Before
    public void before()
    {
        super.before();
        txnService = (TransactionService) applicationContext.getBean("transactionComponent");
        nodeDAO = (NodeDAO) applicationContext.getBean("nodeDAO");
        dictionaryService = (DictionaryService) applicationContext.getBean("dictionaryService");
    }

    /**
     * Ensure that transactionless calls are handled
     */
    public void testCallWithoutTxn()
    {
        TestTransaction.flagForCommit();
        TestTransaction.end();

        nodeService.getAllRootNodes(rootNodeRef.getStoreRef());
    }

    /**
     * Manually trigger the cleanup registry
     */
    @SuppressWarnings("deprecation")
    public void testNodeCleanupRegistry() throws Exception
    {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        // REPO-2963: this test takes a long time in order to pass on a clean DB.
        NodeCleanupRegistry cleanupRegistry = (NodeCleanupRegistry) applicationContext.getBean("nodeCleanupRegistry");
        cleanupRegistry.doClean();
    }

    /**
     * <a href="https://issues.alfresco.com/jira/browse/ALF-14929">ALF-14929</a>
     */
    public synchronized void testTxnCommitTime() throws Exception
    {
        String currentTxn = AlfrescoTransactionSupport.getTransactionId();
        assertNotNull("Must have a txn change UUID for all transactions.");
        
        long start = System.currentTimeMillis();
        this.wait(10L);
        
        // The listener
        final TestTxnCommitTimeTxnListener listener = new TestTxnCommitTimeTxnListener();
        AlfrescoTransactionSupport.bindListener(listener);
        
        // First see what the latest transaction is
        long currentTxnCommitTime = listener.getTxnCommitTime(currentTxn, start);
        assertEquals("Should not have found a written txn", 0L, currentTxnCommitTime);
        
        // Now commit
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // Now check again.  The transaction time must be greater than the last time that
        // the listener wrote through.
        long recordedCommitTimeMs = listener.getTxnCommitTime(currentTxn, start);
        assertTrue(
                "DAO txn write time must be greater than last listener write time",
                recordedCommitTimeMs > listener.lastWriteTime);
    }
    
    /**
     * @see DbNodeServiceImplTest#testTxnCommitTime()
     */
    private class TestTxnCommitTimeTxnListener extends TransactionListenerAdapter
    {
        /*
         * Note: equals hides this instance when listeners are processed
         */
        private String txnIdStr;
        private long lastWriteTime = 0L;
        
        @Override
        public boolean equals(Object obj)
        {
            return false;
        }
        @Override
        public synchronized void beforeCommit(boolean readOnly)
        {
            if (txnIdStr == null)
            {
                txnIdStr = AlfrescoTransactionSupport.getTransactionId();
                // Make a change
                nodeService.setProperty(rootNodeRef, ContentModel.PROP_COUNTER, new Integer(5));
                // Reschedule for removal
                AlfrescoTransactionSupport.bindListener(this);
            }
            else
            {
                nodeService.removeProperty(rootNodeRef, ContentModel.PROP_COUNTER);
            }
            lastWriteTime = System.currentTimeMillis();
            // We wait a bit so that the time differences are significant
            try { this.wait(20L); } catch (InterruptedException e) {}
        }
        public long getTxnCommitTime(String txnId, long fromTime)
        {
            List<Transaction> startTxns = ((NodeDAOImpl) nodeDAO).selectTxns(fromTime, null, Integer.MAX_VALUE, null, null, true);
            long time = 0L;
            for (Transaction txn : startTxns)
            {
                if (txnId.equals(txn.getChangeTxnId()))
                {
                    // Found our transaction
                    time = txn.getCommitTimeMs();
                }
            }
            return time;
        }
    }

    /**
     * Deletes a child node and then iterates over the children of the parent node,
     * getting the QName.  This caused some issues after we did some optimization
     * using lazy loading of the associations.
     */
    public void testLazyLoadIssue() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        // commit results
        TestTransaction.flagForCommit();
        TestTransaction.end();

        UserTransaction userTransaction = txnService.getUserTransaction();
        
        try
        {
            userTransaction.begin();
            
            ChildAssociationRef n6pn8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8"));
            NodeRef n6Ref = n6pn8Ref.getParentRef();
            NodeRef n8Ref = n6pn8Ref.getChildRef();
            
            // delete n8
            nodeService.deleteNode(n8Ref);
            
            // get the parent children
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(n6Ref);
            for (ChildAssociationRef assoc : assocs)
            {
                // just checking
            }
            
            userTransaction.commit();
        }
        catch(Exception e)
        {
            try { userTransaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
    }
    
    /**
     * Checks that the node status changes correctly during:
     * <ul>
     *   <li>creation</li>
     *   <li>property changes</li>
     *   <li>aspect changes</li>
     *   <li>moving</li>
     *   <li>deletion</li>
     * </ul>
     */
    public void testNodeStatus() throws Throwable
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        // get the node to play with
        ChildAssociationRef n6pn8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8"));
        final NodeRef n6Ref = n6pn8Ref.getParentRef();
        final NodeRef n8Ref = n6pn8Ref.getChildRef();
        final Map<QName, Serializable> properties = nodeService.getProperties(n6Ref);

        // commit results
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // change property - check status
        RetryingTransactionCallback<Object> changePropertiesWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                nodeService.setProperty(n6Ref, ContentModel.PROP_CREATED, new Date());
                return null;
            }
        };
        executeAndCheck(n6Ref, changePropertiesWork);
        
        // add an aspect
        RetryingTransactionCallback<Object> addAspectWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                nodeService.addAspect(n6Ref, ASPECT_QNAME_TEST_MARKER, null);
                return null;
            }
        };
        executeAndCheck(n6Ref, addAspectWork);
        
        // remove an aspect
        RetryingTransactionCallback<Object> removeAspectWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                nodeService.removeAspect(n6Ref, ASPECT_QNAME_TEST_MARKER);
                return null;
            }
        };
        executeAndCheck(n6Ref, removeAspectWork);
        
        // move the node
        RetryingTransactionCallback<Object> moveNodeWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                nodeService.moveNode(
                        n6Ref,
                        rootNodeRef,
                        ASSOC_TYPE_QNAME_TEST_CHILDREN,
                        QName.createQName(NAMESPACE, "moved"));
                return null;
            }
        };
        executeAndCheck(n6Ref, moveNodeWork);
        
        // delete the node
        RetryingTransactionCallback<Object> deleteNodeWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                nodeService.deleteNode(n6Ref);
                return null;
            }
        };
        executeAndCheck(n6Ref, deleteNodeWork);
        
        // check cascade-deleted nodes
        RetryingTransactionCallback<Object> checkCascadeCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                // check n6
                NodeRef.Status n6Status = nodeDAO.getNodeRefStatus(n6Ref);
                if (!n6Status.isDeleted())
                {
                    throw new RuntimeException("Deleted node does not have deleted status");
                }
                // n8 is a primary child - it should be deleted too
                NodeRef.Status n8Status = nodeDAO.getNodeRefStatus(n8Ref);
                if (!n8Status.isDeleted())
                {
                    throw new RuntimeException("Cascade-deleted node does not have deleted status");
                }
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(checkCascadeCallback);
        
        // check node recreation
        RetryingTransactionCallback<Object> checkRecreateCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                properties.put(ContentModel.PROP_STORE_PROTOCOL, n6Ref.getStoreRef().getProtocol());
                properties.put(ContentModel.PROP_STORE_IDENTIFIER, n6Ref.getStoreRef().getIdentifier());
                properties.put(ContentModel.PROP_NODE_UUID, n6Ref.getId());

                // recreate n6
                nodeService.createNode(
                        rootNodeRef,
                        ASSOC_TYPE_QNAME_TEST_CHILDREN,
                        QName.createQName(NAMESPACE, "recreated-n6"),
                        ContentModel.TYPE_CONTAINER,
                        properties);
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(checkRecreateCallback);
    }
    
    private void executeAndCheck(NodeRef nodeRef, RetryingTransactionCallback<Object> callback) throws Throwable
    {
        UserTransaction txn = txnService.getUserTransaction();
        txn.begin();
        
        NodeRef.Status currentStatus = nodeService.getNodeStatus(nodeRef);
        assertNotNull(currentStatus);
        String currentTxnId = AlfrescoTransactionSupport.getTransactionId();
        assertNotNull(currentTxnId);
        assertNotSame(currentTxnId, currentStatus.getChangeTxnId());
        try
        {
            callback.execute();
            // get the status
            NodeRef.Status newStatus = nodeService.getNodeStatus(nodeRef);
            assertNotNull(newStatus);
            // check
            assertEquals("Change didn't update status", currentTxnId, newStatus.getChangeTxnId());
            
            // Make sure we can pre-load the node i.e. nodes in all state need to be pre-loadable
            // See CLOUD-1807
            Long nodeId = newStatus.getDbId();
            nodeDAO.getParentAssocs(nodeId, null, null, null, new DummyChildAssocRefQueryCallback());
            nodeDAO.cacheNodesById(Collections.singletonList(nodeId));
            
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }

    /**
     * Dummy implementation that does nothing with the results
     * @author Derek Hulley
     * @since 4.2
     */
    public static class DummyChildAssocRefQueryCallback implements ChildAssocRefQueryCallback
    {
        @Override
        public boolean preLoadNodes()
        {
            return true;
        }
        @Override
        public boolean orderResults()
        {
            return false;
        }
        
        @Override
        public boolean handle(
                Pair<Long, ChildAssociationRef> childAssocPair,
                Pair<Long, NodeRef> parentNodePair, Pair<Long, NodeRef> childNodePair)
        {
            return true;
        }
        
        @Override
        public void done()
        {
        }
    }
    
    public void testMLTextValues() throws Exception
    {
        // Set the server default locale
        Locale.setDefault(Locale.ENGLISH);
        
        MLText mlTextProperty = new MLText();
        mlTextProperty.addValue(Locale.ENGLISH, "Very good!");
        mlTextProperty.addValue(Locale.FRENCH, "Très bon!");
        mlTextProperty.addValue(Locale.GERMAN, "Sehr gut!");

        nodeService.setProperty(
                rootNodeRef,
                BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE,
                mlTextProperty);
        
        // Check unfiltered property retrieval
        Serializable textValueDirect = nodeService.getProperty(
                rootNodeRef,
                BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE);
        assertEquals(
                "MLText type not returned direct",
                mlTextProperty,
                textValueDirect);
        
        // Check unfiltered mass property retrieval
        Map<QName, Serializable> propertiesDirect = nodeService.getProperties(rootNodeRef);
        assertEquals(
                "MLText type not returned direct in Map",
                mlTextProperty,
                propertiesDirect.get(BaseNodeServiceTest.PROP_QNAME_ML_TEXT_VALUE));
    }

    /**
     * Ensure that plain strings going into MLText properties is handled
     */
    @SuppressWarnings("unchecked")
    public void testStringIntoMLTextProperty() throws Exception
    {
        String text = "Hello";
        nodeService.setProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE, text);
        Serializable mlTextCheck = nodeService.getProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE);
        assertTrue("Plain string insertion should be returned as MLText", mlTextCheck instanceof MLText);
        Locale defaultLocale = I18NUtil.getLocale();
        MLText mlTextCheck2 = (MLText) mlTextCheck;
        String mlTextDefaultCheck = mlTextCheck2.getDefaultValue();
        assertEquals("Default MLText value was not set correctly", text, mlTextDefaultCheck);
        
        // Reset the property
        nodeService.setProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE, null);
        Serializable nullValueCheck = nodeService.getProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE);
        
        // Now, just pass a String in
        nodeService.setProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE, text);
        // Now update the property with some MLText
        MLText mlText = new MLText();
        mlText.addValue(Locale.ENGLISH, "Very good!");
        mlText.addValue(Locale.FRENCH, "Très bon!");
        mlText.addValue(Locale.GERMAN, "Sehr gut!");
        nodeService.setProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE, mlText);
        // Get it back and check
        mlTextCheck = nodeService.getProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE);
        assertEquals("Setting of MLText over String failed.", mlText, mlTextCheck);
    }
    
    /**
     * Ensure that plain strings going into MLText properties is handled
     */
    @SuppressWarnings("unchecked")
    public void testSingleStringMLTextProperty() throws Exception
    {
        // Set the property with single-value MLText
        MLText mlText = new MLText();
        mlText.addValue(Locale.GERMAN, "Sehr gut!");
        nodeService.setProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE, mlText);
        // Get it back and check
        MLText mlTextCheck = (MLText) nodeService.getProperty(rootNodeRef, PROP_QNAME_ML_TEXT_VALUE);
        assertEquals("Setting of MLText over String failed.", mlText, mlTextCheck);
    }
    
    /**
     * It would appear that an issue has arisen with creating and deleting nodes
     * in the same transaction.
     */
    public void testInTransactionCreateAndDelete() throws Exception
    {
        // Create a node
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ASSOC_TYPE_QNAME_TEST_CHILDREN,
                QName.createQName(NAMESPACE, this.getName()),
                TYPE_QNAME_TEST_CONTENT).getChildRef();
        // Delete the node
        nodeService.deleteNode(nodeRef);
    }
    
    public void testAspectRemovalWithCommit() throws Throwable
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
       
       // Force a commit here
        TestTransaction.flagForCommit();
        TestTransaction.end();
       
       // start another transaction to remove the aspect
       UserTransaction txn = txnService.getUserTransaction();
       txn.begin();
       
       try
       {
          Set<QName> aspects = nodeService.getAspects(sourceNodeRef); 
          int noAspectsBefore = aspects.size();
          
          // Now remove the aspect
          nodeService.removeAspect(sourceNodeRef, ASPECT_WITH_ASSOCIATIONS);
          
          // Check that the associations were removed
          assertEquals("Expected exactly zero child",
                  0, nodeService.getChildAssocs(sourceNodeRef).size());
          assertEquals("Expected exactly zero target",
                  0, nodeService.getTargetAssocs(sourceNodeRef, RegexQNamePattern.MATCH_ALL).size());
          aspects = nodeService.getAspects(sourceNodeRef); 
          assertEquals("Expected exactly one less aspect",
                  noAspectsBefore-1, aspects.size());
          
          txn.commit();
       }
       catch (Throwable e)
       {
           try { txn.rollback(); } catch (Throwable ee) {}
           throw e;
       }
    }
    
    /**
     * Test Get Child Assocs By Property Value
     * @throws Exception
     */
    public void testGetChildAssocsByPropertyValue() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs;
        
        assocRefs = buildNodeGraph();
        
        ChildAssociationRef rootRef = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root"));
        ChildAssociationRef n1Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n1"));
        ChildAssociationRef n2Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "root_p_n2"));
        
        /**
         *  Positive test - get n1 and n2 by the value of a text property in this case PROP_SUBJECT which 
         *  contains "Hello World"
         */
        {
            NodeRef parentNodeRef = n1Ref.getParentRef();
            NodeRef childNodeRef =   n1Ref.getChildRef();
            assertTrue(nodeService.exists(parentNodeRef));
            assertTrue(nodeService.exists(childNodeRef));
            
            String subject = "Hello World";
            nodeService.setProperty(n1Ref.getChildRef(), ContentModel.PROP_SUBJECT, subject);      
            List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_SUBJECT, subject);
            assertTrue("failed to read one assoc", refs.size() == 1);
            assertTrue("content not correct", refs.contains(n1Ref));
            
            // Now go for another two documents.
            nodeService.setProperty(n2Ref.getChildRef(), ContentModel.PROP_SUBJECT, subject);      
            refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_SUBJECT, subject);
            assertTrue("failed to read two assocs", refs.size() == 2);
            assertTrue("content not correct", refs.contains(n1Ref));
            assertTrue("content not correct", refs.contains(n2Ref));
        }
        
        /**
         * Positive tests of various types that should be accepted by the query 
         */
        {
            NodeRef parentNodeRef = n1Ref.getParentRef();
            NodeRef childNodeRef =   n1Ref.getChildRef();
            assertTrue(nodeService.exists(parentNodeRef));
            assertTrue(nodeService.exists(childNodeRef));
            
            // integer
            int count = 123;
            nodeService.setProperty(n1Ref.getChildRef(), ContentModel.PROP_COUNTER, count);      
            List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_COUNTER, count);
            assertTrue("failed to read one assoc", refs.size() == 1);
            assertTrue("content not correct", refs.contains(n1Ref));
            
            // Double
            Double alfLat = new Double(51.5216666);
            Double alfLon = new Double(0.43);
            nodeService.setProperty(n1Ref.getChildRef(), ContentModel.PROP_LATITUDE, alfLat);      
            refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_LATITUDE, alfLat);
            assertTrue("failed to read one assoc", refs.size() == 1);
            assertTrue("content not correct", refs.contains(n1Ref));
            
            // float
            // not implemeted due to float precision issues with float equals.
            //float score = 1.3f;
            //nodeService.setProperty(n1Ref.getChildRef(), ContentModel.PROP_RATING_SCORE, score);      
            //refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_RATING_SCORE, score);
            //assertTrue("failed to read one assoc", refs.size() == 1);
            //assertTrue("content not correct", refs.contains(n1Ref));
            
            // Boolean TRUE
            Boolean beauty = Boolean.TRUE;
            nodeService.setProperty(n1Ref.getChildRef(), ContentModel.PROP_ENABLED, beauty); 
            assertTrue((Boolean)nodeService.getProperty(n1Ref.getChildRef(), ContentModel.PROP_ENABLED));
            refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_ENABLED, beauty);
            assertTrue("failed to read one assoc", refs.size() == 1);
            assertTrue("content not correct", refs.contains(n1Ref));
            
            // Boolean FALSE
            beauty = Boolean.FALSE;
            nodeService.setProperty(n1Ref.getChildRef(), ContentModel.PROP_ENABLED, beauty); 
            assertTrue(!(Boolean)nodeService.getProperty(n1Ref.getChildRef(), ContentModel.PROP_ENABLED));
            refs = nodeService.getChildAssocsByPropertyValue(parentNodeRef, ContentModel.PROP_ENABLED, beauty);
            assertTrue("failed to read one assoc", refs.size() == 1);
           
        }
        
        /**
         * Negative test - invalid to search on sys:node-dbid
         */
        try
        {
            List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(n1Ref.getParentRef(), ContentModel.PROP_NODE_DBID, "Fail");
            fail("sys:node-dbid not rejected");
        }
        catch (IllegalArgumentException ie)
        {
            // expect to go here
        }
        
        /**
         * Negative test - invalid to search on type MLText
         */
        try
        {
            Serializable title = (String)nodeService.getProperty(n1Ref.getChildRef(), ContentModel.PROP_TITLE);
            List<ChildAssociationRef> refs = nodeService.getChildAssocsByPropertyValue(n1Ref.getParentRef(), ContentModel.PROP_NAME, title);
            fail("MLText type not rejected");
        }
        catch (IllegalArgumentException ie)
        {
            // expect to go here
        }
    }
    
    /**
     * Check that the maximum string lengths can be adjusted up and down.
     * Note that this test ONLY works for MySQL because the other databases cannot support more than 1024 characters
     * in the string_value column and the value may not be set to less than 1024.
     * 
     * @see SchemaBootstrap#DEFAULT_MAX_STRING_LENGTH
     */
    @SuppressWarnings("deprecation")
    public void testMySQLInnoDBNodeStringLengthWorker() throws Exception
    {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        
        // Skip of the dialect if not MySQL (also skip for MySQL Cluster NDB)
        Dialect dialect = (Dialect) applicationContext.getBean("dialect");
        if ((dialect instanceof MySQLClusterNDBDialect) || (! (dialect instanceof MySQLInnoDBDialect)))
        {
            return;
        }
        SchemaBootstrap schemaBootstrap = (SchemaBootstrap) applicationContext.getBean("schemaBootstrap");
        assertEquals("Expected max string length to be MAX", Integer.MAX_VALUE, SchemaBootstrap.getMaxStringLength());
        
        NodeStringLengthWorker worker = (NodeStringLengthWorker) applicationContext.getBean("nodeStringLengthWorker");
        
        // If we run this worker just to get everything into the correct starting state.
        // If it does not work, then that will be detected later anyway
        NodeStringLengthWorkResult result = worker.execute();
        assertTrue(result.getPropertiesProcessed() > 0);
        assertEquals(0, result.getErrors());
        
        // Now set the max string length to DEFAULT_MAX_STRING_LENGTH characters
        schemaBootstrap.setMaximumStringLength(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH);
        schemaBootstrap.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
        // Move any values persisted before the test
        result = worker.execute();
        int firstPassChanged = result.getPropertiesChanged();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH + 1; i++)
        {
            sb.append("A");
        }
        final String longString = sb.toString();
        // Persist the property using the default MAX_VALUE so that it goes into the string_value
        schemaBootstrap.setMaximumStringLength(Integer.MAX_VALUE);
        schemaBootstrap.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
        txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.setProperty(rootNodeRef, PROP_QNAME_STRING_VALUE, longString);
                return null;
            }
        });

        // The worker should do nothing
        result = worker.execute();
        assertEquals(firstPassChanged, result.getPropertiesChanged());
        
        // Now bring the limit down to the match for other DBs
        schemaBootstrap.setMaximumStringLength(SchemaBootstrap.DEFAULT_MAX_STRING_LENGTH);
        schemaBootstrap.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
        result = worker.execute();
        assertEquals(firstPassChanged + 1, result.getPropertiesChanged());
        
        // Put the limit back to the MySQL default and all the large values should go back into MySQL's TEXT field
        schemaBootstrap.setMaximumStringLength(Integer.MAX_VALUE);
        schemaBootstrap.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
        result = worker.execute();
        assertEquals(firstPassChanged + 1, result.getPropertiesChanged());
        
        // Check that our string is still OK
        String checkLongString = txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                return (String) nodeService.getProperty(rootNodeRef, PROP_QNAME_STRING_VALUE);
            }
        });
        assertEquals("String manipulation corrupted the long string value. ", longString, checkLongString);
    }

    @Test
    public void testMNT15655() throws Exception {
    	class TestData
    	{
    		NodeRef rootFolderNodeRef;
    		NodeRef folder1NodeRef;
            NodeRef folder2NodeRef;
            String folder1Name;
            String folder2Name;
    	}
    	final TestData testData = new TestData();

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
            	AuthenticationUtil.runAs(new RunAsWork<Void>()
            	{
					@Override
					public Void doWork() throws Exception {
		                Map<QName, Serializable> properties = new HashMap<>();

		                StoreRef storeRef = nodeService.createStore(
		                        StoreRef.PROTOCOL_WORKSPACE,
		                        "Test_" + System.currentTimeMillis());
		                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

		                testData.rootFolderNodeRef = nodeService.createNode(
		                        rootNodeRef,
		                        ContentModel.ASSOC_CHILDREN,
		                        QName.createQName(NAMESPACE, "rootFolder"),
		                        ContentModel.TYPE_FOLDER,
		                        properties).getChildRef();

		                properties.clear();
		                properties.put(ContentModel.PROP_NAME, "folder");
		                testData.folder1NodeRef = nodeService.createNode(
		                		testData.rootFolderNodeRef,
		                        ContentModel.ASSOC_CONTAINS,
		                        QName.createQName(NAMESPACE, "folder"),
		                        ContentModel.TYPE_FOLDER,
		                        properties).getChildRef();

		                properties.clear();
		                properties.put(ContentModel.PROP_NAME, "folder1");
		                testData.folder2NodeRef = nodeService.createNode(
		                		testData.rootFolderNodeRef,
		                        ContentModel.ASSOC_CONTAINS,
		                        QName.createQName(NAMESPACE, "folder1"),
		                        ContentModel.TYPE_FOLDER,
		                        properties).getChildRef();

		                String name1 = (String)nodeService.getProperty(
		                		testData.folder1NodeRef,
		                        ContentModel.PROP_NAME);
		                String name2 = (String)nodeService.getProperty(
		                		testData.folder2NodeRef,
		                        ContentModel.PROP_NAME);

		                assertEquals("folder", name1);
		                assertEquals("folder1", name2);

						return null;
					}
            		
            	}, AuthenticationUtil.SYSTEM_USER_NAME);

                return null;
            }
        }, false, true);

        try
        {
	        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
	        {
	            @Override
	            public Void execute() throws Throwable
	            {
	            	AuthenticationUtil.runAs(new RunAsWork<Void>()
	            	{
						@Override
						public Void doWork() throws Exception {
			                Map<QName, Serializable> aspectProperties = new HashMap<>();
			                aspectProperties.put(ContentModel.PROP_NAME, "folder");
			                nodeService.addAspect(
			                		testData.folder2NodeRef,
			                        ContentModel.ASPECT_AUTHOR,
			                        aspectProperties);
	
			                fail("Should have generated a DuplicateChildNodeNameException");

							return null;
						}
	            		
	            	}, AuthenticationUtil.SYSTEM_USER_NAME);
	
	                return null;
	            }
	        }, false, true);
        }
        catch(DuplicateChildNodeNameException e)
        {
        	// ok
        }
    }
}
