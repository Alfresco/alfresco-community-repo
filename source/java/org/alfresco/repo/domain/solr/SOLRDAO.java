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
    /**
     * Get the transactions from either minTxnId or fromCommitTime, optionally limited to maxResults
     * 
     * @param minTxnId greater than or equal to minTxnId
     * @param fromCommitTime greater than or equal to transaction commit time
     * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return list of transactions
     */
	public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, int maxResults);
	
    /**
     * Get the nodes satisfying the constraints in nodeParameters
     * 
     * @param nodeParameters set of constraints for which nodes to return
     * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @param callback a callback to receive the results
     */
	public void getNodes(NodeParameters nodeParameters, NodeQueryCallback callback);
	
	/**
	 * Returns metadata for a set of node ids
	 * 
	 * @param nodeIds a set of nodeIds for which to return node metadata
	 * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
	 * @param callback a callback to receive the results
	 */
	public void getNodesMetadata(NodeMetaDataParameters nodeMetaDataParameters, MetaDataResultsFilter resultFilter, NodeMetaDataQueryCallback callback);
	
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
    
    /**
     * The interface that will be used to give query results to the calling code.
     */
    public static interface NodeMetaDataQueryCallback
    {
        /**
         * Handle a node.
         * 
         * @param node                      the node meta data
         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
         */
        boolean handleNodeMetaData(NodeMetaData nodeMetaData);
    }
}
