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
package org.alfresco.repo.search;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardState;
import org.alfresco.repo.solr.Acl;
import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.AclReaders;
import org.alfresco.repo.solr.AlfrescoModel;
import org.alfresco.repo.solr.AlfrescoModelDiff;
import org.alfresco.repo.solr.MetaDataResultsFilter;
import org.alfresco.repo.solr.NodeMetaData;
import org.alfresco.repo.solr.NodeMetaDataParameters;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.solr.Transaction;
import org.alfresco.service.namespace.QName;

/**
 * Interface for component to provide tracking data for Search Subsystem.
 * 
 * @since 4.0
 */
public interface SearchTrackingComponent
{   
    /**
     * Get the ACL changesets for given range parameters
     * 
     * @param minAclChangeSetId         minimum ACL changeset ID - (inclusive and optional)
     * @param fromCommitTime            minimum ACL commit time - (inclusive and optional)
     * @param maxAclChangeSetId         max ACL changeset ID - (exclusive and optional)
     * @param toCommitTime              max ACL commit time - (exclusive and optional)
     * @param maxResults                limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return                          list of ACL changesets
     */
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, Long maxAclChangeSetId, Long toCommitTime, int maxResults);
    
    /**
     * Get the ACLs with paging options for a specific ACL ChangeSet
     * 
     * @param aclChangeSetIds           the ACL ChangeSet IDs
     * @param minAclId                  the minimum ACL ID - (inclusive and optional).
     * @param maxResults                the maximum number of results (must be greater than zero and less than MAX)
     * @return                          list of ACLs
     */
    public List<Acl> getAcls(List<Long> aclChangeSetIds, Long minAclId, int maxResults);
    
    /**
     * Get the ACL readers ("authorities who can read this ACL") for a given set of ACL IDs
     * 
     * @param aclIds                    the ACL IDs
     * @return                          Returns the list of ACL readers (includes original ACL IDs)
     */
    public List<AclReaders> getAclsReaders(List<Long> aclIds);
    
    /**
     * Get the transactions from either minTxnId or fromCommitTime, optionally limited to maxResults
     * 
     * @param minTxnId greater than or equal to minTxnId
     * @param fromCommitTime greater than or equal to transaction commit time
     * @param maxTxnId less than maxTxnId
     * @param toCommitTimeint less then toCommitTimeint
     * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return list of transactions
     */
    public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, Long maxTxnId, Long toCommitTimeint, int maxResults);
    
    /**
     * Get the nodes satisfying the constraints in nodeParameters
     * 
     * @param nodeParameters set of constraints for which nodes to return
     * @param callback a callback to receive the results
     */
	public void getNodes(NodeParameters nodeParameters, NodeQueryCallback callback);
	
	/**
	 * Returns metadata for a set of node ids
	 * 
	 * @param nodeMetaDataParameters NodeMetaDataParameters
	 * @param resultFilter MetaDataResultsFilter
	 * @param callback a callback to receive the results
	 */
	public void getNodesMetadata(NodeMetaDataParameters nodeMetaDataParameters, MetaDataResultsFilter resultFilter, NodeMetaDataQueryCallback callback);

	/**
	 * Returns the Alfresco model given by the name modelName
	 * 
	 * @param modelName the name of the model
     * @return the model plus a checksum
	 */
	public AlfrescoModel getModel(QName modelName);

	/**
	 * Returns a list of diffs representing differences between the current Repository models
	 * and those passed in the models parameter.
	 * 
	 * @param models a set of mappings of model names to checksums
     * @return a list of diffs between those in the repository and those passed in the models parameter
	 */
    public List<AlfrescoModelDiff> getModelDiffs(Map<QName, Long> models);

    /**
     * The interface that will be used to give query results to the calling code.
     */
    public interface NodeQueryCallback
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
    public interface NodeMetaDataQueryCallback
    {
        /**
         * Handle a node.
         * 
         * @param nodeMetaData                      the node meta data
         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
         */
        boolean handleNodeMetaData(NodeMetaData nodeMetaData);
    }

    /**
     * @return boolean
     */
    boolean isEnabled();

    /**
     * @param enabled boolean
     */
    void setEnabled(boolean enabled);
    
    /**
     * Get the last transaction timestamp from the repo
     * @return Long
     */
    public Long getMaxTxnCommitTime();
    
    /**
     * Get the last transaction id from the repo
     * @return Long
     */
    public Long getMaxTxnId();

    /**
     * @return Long
     */
    public Long getMaxChangeSetCommitTime();

    /**
     * @return Long
     */
    public Long getMaxChangeSetId();
    
    /**
     * Register and update a shard state 
     * @param shardState
     */
    public void registerShardState(ShardState shardState); 
    
    /**
     * Get the shard registry
     * @return the shard registry or null if one is not registered.
     * This is an optional feature.
     */
    public ShardRegistry getShardRegistry();
    
    /**
     * Compute the CRC for the parent associations to this node that can cause its PATH to change
     * - primary & secondary associations and virtual associations from categories.
     * @param nodeId
     * @return
     */
    public long getCRC(Long nodeId);
}
