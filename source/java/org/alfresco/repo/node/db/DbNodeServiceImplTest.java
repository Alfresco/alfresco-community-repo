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
package org.alfresco.repo.node.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * @see org.alfresco.repo.node.db.DbNodeServiceImpl
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class DbNodeServiceImplTest extends BaseNodeServiceTest
{
    private TransactionService txnService;
    private NodeDaoService nodeDaoService;
    private DictionaryService dictionaryService;
    
    protected NodeService getNodeService()
    {
        return (NodeService) applicationContext.getBean("nodeService");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        txnService = (TransactionService) applicationContext.getBean("transactionComponent");
        nodeDaoService = (NodeDaoService) applicationContext.getBean("nodeDaoService");
        dictionaryService = (DictionaryService) applicationContext.getBean("dictionaryService");
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
        setComplete();
        endTransaction();

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
        setComplete();
        endTransaction();

        // change property - check status
        TransactionWork<Object> changePropertiesWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                nodeService.setProperty(n6Ref, ContentModel.PROP_CREATED, new Date());
                return null;
            }
        };
        executeAndCheck(n6Ref, changePropertiesWork);
        
        // add an aspect
        TransactionWork<Object> addAspectWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                nodeService.addAspect(n6Ref, ASPECT_QNAME_TEST_MARKER, null);
                return null;
            }
        };
        executeAndCheck(n6Ref, addAspectWork);
        
        // remove an aspect
        TransactionWork<Object> removeAspectWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                nodeService.removeAspect(n6Ref, ASPECT_QNAME_TEST_MARKER);
                return null;
            }
        };
        executeAndCheck(n6Ref, removeAspectWork);
        
        // move the node
        TransactionWork<Object> moveNodeWork = new TransactionWork<Object>()
        {
            public Object doWork()
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
        TransactionWork<Object> deleteNodeWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                nodeService.deleteNode(n6Ref);
                return null;
            }
        };
        executeAndCheck(n6Ref, deleteNodeWork);
        
        // check cascade-deleted nodes
        TransactionWork<Object> checkCascadeWork = new TransactionWork<Object>()
        {
            public Object doWork()
            {
                // check n6
                NodeStatus n6Status = nodeDaoService.getNodeStatus(n6Ref, false);
                if (!n6Status.isDeleted())
                {
                    throw new RuntimeException("Deleted node does not have deleted status");
                }
                // n8 is a primary child - it should be deleted too
                NodeStatus n8Status = nodeDaoService.getNodeStatus(n8Ref, false);
                if (!n8Status.isDeleted())
                {
                    throw new RuntimeException("Cascade-deleted node does not have deleted status");
                }
                return null;
            }
        };
        TransactionUtil.executeInUserTransaction(txnService, checkCascadeWork);
        
        // check node recreation
        TransactionWork<Object> checkRecreateWork = new TransactionWork<Object>()
        {
            public Object doWork()
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
        TransactionUtil.executeInUserTransaction(txnService, checkRecreateWork);
    }
    
    private void executeAndCheck(NodeRef nodeRef, TransactionWork<Object> work) throws Throwable
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
            work.doWork();
            // get the status
            NodeRef.Status newStatus = nodeService.getNodeStatus(nodeRef);
            assertNotNull(newStatus);
            // check
            assertEquals("Change didn't update status", currentTxnId, newStatus.getChangeTxnId());
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
    
    /**
     * Checks that the string_value retrieval against a property type is working
     */
    public void testGetContentDataValues() throws Exception
    {
        final DataTypeDefinition contentDataType = dictionaryService.getDataType(DataTypeDefinition.CONTENT);

        ContentData contentDataSingle = new ContentData("url-single", MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null);
        ContentData contentDataMultiple = new ContentData("url-multiple", MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null);
        // put this in as a random single property
        nodeService.setProperty(
                rootNodeRef,
                QName.createQName(NAMESPACE, "random-single"),
                contentDataSingle);
        
        // create a collection of mixed types
        ArrayList<Serializable> collection = new ArrayList<Serializable>(3);
        collection.add("abc");
        collection.add(new Integer(123));
        collection.add(contentDataMultiple);
        nodeService.setProperty(
                rootNodeRef,
                QName.createQName(NAMESPACE, "random-multiple"),
                collection);
        
        // get a list of all content values
        List<Serializable> allContentDatas = nodeDaoService.getPropertyValuesByActualType(contentDataType);
        assertTrue("At least two instances expected", allContentDatas.size() >= 2);
        assertTrue("Single content data not present in results",
                allContentDatas.contains(contentDataSingle));
        assertTrue("Multi-valued buried content data not present in results",
                allContentDatas.contains(contentDataMultiple));
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
    
    public void testDuplicatePrimaryParentHandling() throws Exception
    {
        Map<QName, ChildAssociationRef> assocRefs = buildNodeGraph();
        // get the node to play with
        ChildAssociationRef n1pn3Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n1_p_n3"));
        ChildAssociationRef n6pn8Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n6_p_n8"));
        final NodeRef n1Ref = n1pn3Ref.getParentRef();
        final NodeRef n8Ref = n6pn8Ref.getChildRef();
        
        // Add a make n1 a second primary parent of n8
        Node n1 = nodeDaoService.getNode(n1Ref);
        Node n8 = nodeDaoService.getNode(n8Ref);
        ChildAssoc assoc = nodeDaoService.newChildAssoc(
                n1,
                n8,
                true,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NAMESPACE, "n1pn8"));
        
        // Now get the node primary parent
        nodeService.getPrimaryParent(n8Ref);
        // Get it again
        nodeService.getPrimaryParent(n8Ref);
    }
}
