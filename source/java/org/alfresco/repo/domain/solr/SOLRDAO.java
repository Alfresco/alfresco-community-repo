package org.alfresco.repo.domain.solr;

import java.util.List;

import org.alfresco.repo.domain.node.Node;

/**
 * DAO support for SOLR web scripts.
 * 
 * @since 4.0
 */
// TODO - permit shortened form of QNames for e.g. aspects i.e. cm:content vs {http://www.alfresco.org/model/content/1.0}content?
public interface SOLRDAO
{
	public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, int maxResults);
	public void getNodes(NodeParameters nodeParameters, int maxResults, NodeQueryCallback callback);
	
    /**
     * The interface that will be used to give query results to the calling code.
     */
    public static interface NodeQueryCallback
    {
        /**
         * Handle a node.
         * 
         * @param node                      the node
         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
         */
        boolean handleNode(Node node);
    }
}
