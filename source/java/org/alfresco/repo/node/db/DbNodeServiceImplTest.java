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
package org.alfresco.repo.node.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * @see org.alfresco.repo.node.db.DbNodeServiceImpl
 * 
 * @author Derek Hulley
 */
public class DbNodeServiceImplTest extends BaseNodeServiceTest
{
    private TransactionService txnService;
    private NodeDaoService nodeDaoService;
    
    protected NodeService getNodeService()
    {
        return (NodeService) applicationContext.getBean("NodeService");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        txnService = (TransactionService) applicationContext.getBean("transactionComponent");
        nodeDaoService = (NodeDaoService) applicationContext.getBean("nodeDaoService");
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
    public void testNodeStatus() throws Exception
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
                NodeStatus n6Status = nodeDaoService.getNodeStatus(
                        n6Ref.getStoreRef().getProtocol(),
                        n6Ref.getStoreRef().getIdentifier(),
                        n6Ref.getId());
                if (!n6Status.isDeleted())
                {
                    throw new RuntimeException("Deleted node does not have deleted status");
                }
                // n8 is a primary child - it should be deleted too
                NodeStatus n8Status = nodeDaoService.getNodeStatus(
                        n8Ref.getStoreRef().getProtocol(),
                        n8Ref.getStoreRef().getIdentifier(),
                        n8Ref.getId());
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
    
    private void executeAndCheck(NodeRef nodeRef, TransactionWork<Object> work) throws Exception
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
        catch (Exception e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
}
