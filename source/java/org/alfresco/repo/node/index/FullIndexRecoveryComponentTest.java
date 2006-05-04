/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.node.index;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Checks that full index recovery is possible
 * 
 * @author Derek Hulley
 */
public class FullIndexRecoveryComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private FullIndexRecoveryComponent indexRecoverer;
    private NodeService nodeService;
    private TransactionService txnService;
    private Indexer indexer;
    
    private List<StoreRef> storeRefs;
    
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        indexRecoverer = (FullIndexRecoveryComponent) ctx.getBean("indexRecoveryComponent");
        txnService = (TransactionService) ctx.getBean("transactionComponent");
        nodeService = (NodeService) ctx.getBean("nodeService");
        indexer = (Indexer) ctx.getBean("indexerComponent");
        
        // create 2 stores
        TransactionWork<List<StoreRef>> createStoresWork = new TransactionWork<List<StoreRef>>()
        {
            public List<StoreRef> doWork() throws Exception
            {
                List<StoreRef> storeRefs = new ArrayList<StoreRef>(2);
                storeRefs.add(nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.nanoTime()));
                storeRefs.add(nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.nanoTime()));
                return storeRefs;
            }
        };
        storeRefs = TransactionUtil.executeInUserTransaction(transactionService, createStoresWork);
    }
    
    public void testNothing() throws Exception
    {
        
    }
    
    public void xtestReindexing() throws Exception
    {
        // don't do anything if the component has already started
        if (FullIndexRecoveryComponent.isStarted())
        {
            return;
        }
        // deletes a content node from the index
        final List<String> storeRefStrings = new ArrayList<String>(2);
        TransactionWork<String> dropNodeIndexWork = new TransactionWork<String>()
        {
            public String doWork()
            {
                // create a node in each store and drop it from the index
                for (StoreRef storeRef : storeRefs)
                {
                    try
                    {
                        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                        ChildAssociationRef assocRef = nodeService.createNode(
                                rootNodeRef,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.ALFRESCO_URI, "unindexedChild" + System.currentTimeMillis()),
                                ContentModel.TYPE_BASE);
                        // this will have indexed it, so remove it from the index
                        indexer.deleteNode(assocRef);
                        // make the string version of the storeRef
                        storeRefStrings.add(storeRef.toString());
                    }
                    catch (InvalidStoreRefException e)
                    {
                        // just ignore stores that are invalid
                    }
                }
                return AlfrescoTransactionSupport.getTransactionId();
            }
        };
        
        // create un-indexed nodes
        String txnId = TransactionUtil.executeInNonPropagatingUserTransaction(txnService, dropNodeIndexWork);
        
        indexRecoverer.setExecuteFullRecovery(true);
        indexRecoverer.setStores(storeRefStrings);
        // reindex
        indexRecoverer.reindex();

        // check that reindexing fails
        try
        {
            indexRecoverer.reindex();
            fail("Reindexer failed to prevent reindex from being called twice");
        }
        catch (RuntimeException e)
        {
            // expected
        }
        
        // loop for some time, giving it a chance to do its thing
        String lastProcessedTxnId = null;
        for (int i = 0; i < 60; i++)
        {
            lastProcessedTxnId = FullIndexRecoveryComponent.getCurrentTransactionId();
            if (lastProcessedTxnId.equals(txnId))
            {
                break;
            }
            // wait for a second
            synchronized(this)
            {
                this.wait(1000L);
            }
        }
        // check that the index was recovered
        assertEquals("Index transaction not up to date", txnId, lastProcessedTxnId);
    }
}
