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
package org.alfresco.repo.domain.solr;

import java.util.List;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.solr.Acl;
import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.solr.Transaction;
import org.alfresco.service.namespace.QName;

/**
 * DAO support for SOLR web scripts.
 * 
 * @since 4.0
 */
public interface SearchDAO
{
    /**
     * Get the ACL changesets summary (rollup count) with paging options
     * 
     * @param minAclChangeSetId         minimum ACL changeset ID - (inclusive and optional)
     * @param fromCommitTime            minimum ACL commit time - (inclusive and optional)
     * @param maxAclChangeSetId         maximum ACL changeset ID - (exclusive and optional)
     * @param toCommitTime              maximum ACL commit time - (exclusive and optional)
     * @param maxResults                limit the results (must be greater than zero and less than MAX)
     * @return                          list of ACL changesets (no details)
     */
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, Long maxAclChangeSetId, Long toCommitTime, int maxResults);
    
    /**
     * Get the ACLs (no rollup count) for the given ACL ChangeSets
     * 
     * @param aclChangeSetIds           the ACL ChangeSet IDs
     * @param minAclId                  the minimum ACL ID - (inclusive and optional).
     * @param maxResults                the maximum number of results (must be greater than zero and less than MAX)
     * @return                          list of ACLs
     */
    public List<Acl> getAcls(List<Long> aclChangeSetIds, Long minAclId, int maxResults);

    /**
     * Get the transactions from either minTxnId or fromCommitTime, optionally limited to maxResults
     * 
     * @param minTxnId greater than or equal to minTxnId
     * @param fromCommitTime greater than or equal to transaction commit time
     * @param maxTxnId less than maxTxnId
     * @param toCommitTime less than toCommitTime
     * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return list of transactions
     */
	public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, Long maxTxnId, Long toCommitTime, int maxResults);
	
    /**
     * Get the nodes satisfying the constraints in nodeParameters
     * 
     * @param nodeParameters set of constraints for which nodes to return
     * @param shardPropertQName qname of property to use as shard_key
     * @param shardPropertyTypeName type name (text, int, long) of property to use as shard_key
     * @return list of matching nodes
     */
	public List<Node> getNodes(NodeParameters nodeParameters, QName shardPropertQName, QName shardPropertyTypeName);
}
