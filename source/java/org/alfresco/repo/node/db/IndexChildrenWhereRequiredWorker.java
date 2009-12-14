package org.alfresco.repo.node.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.cleanup.AbstractNodeCleanupWorker;
import org.alfresco.repo.node.db.NodeDaoService.NodeRefQueryCallback;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.Pair;

/**
 * Indexes child nodes where cascade re-indexing is disabled.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public class IndexChildrenWhereRequiredWorker extends AbstractNodeCleanupWorker
{
    /**
     * Default constructor
     */
    public IndexChildrenWhereRequiredWorker()
    {
    }

    /**
     * {@inheritDoc}
     */
    protected List<String> doCleanInternal() throws Throwable
    {
      List<String> indexChildrenResults = indexChildrenWhereRequired();
      
      List<String> allResults = new ArrayList<String>(100);
      allResults.addAll(indexChildrenResults);
      
      // Done
      return allResults;
    }
    
    private List<String> indexChildrenWhereRequired()
    {
        final List<Pair<Long, NodeRef>> parentNodePairs = new ArrayList<Pair<Long, NodeRef>>(100);
        final NodeRefQueryCallback callback = new NodeRefQueryCallback()
        {
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                parentNodePairs.add(nodePair);
                return true;
            }
        };
        RetryingTransactionCallback<Object> getNodesCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                nodeDaoService.getNodesWithAspect(ContentModel.ASPECT_INDEX_CHILDREN, Long.MIN_VALUE, 100, callback);
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(getNodesCallback, true, true);
        // Process the nodes in random order
        Collections.shuffle(parentNodePairs);
        // Iterate and operate
        List<String> results = new ArrayList<String>(100);
        for (final Pair<Long, NodeRef> parentNodePair : parentNodePairs)
        {
            RetryingTransactionCallback<String> indexChildrenCallback = new RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    // Index children without full cascade
                    dbNodeService.indexChildren(parentNodePair, true);
                    // Done
                    return null;
                }
            };
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(1);
            try
            {
                txnHelper.doInTransaction(indexChildrenCallback, false, true);
                String msg = 
                    "Indexed child nodes: \n" +
                    "   Parent node: " + parentNodePair.getFirst();
                results.add(msg);
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to index child nodes." +
                    "  Set log level to WARN for this class to get exception log: \n" +
                    "   Parent node: " + parentNodePair.getFirst() + "\n" +
                    "   Error:       " + e.getMessage();
                // It failed; do a full log in WARN mode
                if (logger.isWarnEnabled())
                {
                    logger.warn(msg, e);
                }
                else
                {
                    logger.error(msg);
                }
                results.add(msg);
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(256);
            sb.append("Indexed child nodes: \n")
              .append("  Results:\n");
            for (String msg : results)
            {
                sb.append("  ").append(msg).append("\n");
            }
            logger.debug(sb.toString());
        }
        return results;
    }
}