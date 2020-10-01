/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elastic;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.index.shard.ShardState;
import org.alfresco.repo.search.SearchTrackingComponent;
import org.alfresco.repo.solr.Acl;
import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.AclReaders;
import org.alfresco.repo.solr.AlfrescoModel;
import org.alfresco.repo.solr.AlfrescoModelDiff;
import org.alfresco.repo.solr.MetaDataResultsFilter;
import org.alfresco.repo.solr.NodeMetaDataParameters;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.solr.Transaction;
import org.alfresco.service.namespace.QName;

/**
 * Gets search information related from database.
 * TODO This class can be extended from SOLRTrackingComponentImpl, as the could should be similar.
 */
public class ElasticTrackingComponentImpl implements SearchTrackingComponent
{

    @Override
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, Long maxAclChangeSetId, Long toCommitTime, int maxResults)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Acl> getAcls(List<Long> aclChangeSetIds, Long minAclId, int maxResults)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AclReaders> getAclsReaders(List<Long> aclIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, Long maxTxnId, Long toCommitTimeint, int maxResults)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getNodes(NodeParameters nodeParameters, NodeQueryCallback callback)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getNodesMetadata(NodeMetaDataParameters nodeMetaDataParameters, MetaDataResultsFilter resultFilter, NodeMetaDataQueryCallback callback)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AlfrescoModel getModel(QName modelName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AlfrescoModelDiff> getModelDiffs(Map<QName, Long> models)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Long getMaxTxnCommitTime()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getMaxTxnId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getMaxChangeSetCommitTime()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getMaxChangeSetId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerShardState(ShardState shardState)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ShardRegistry getShardRegistry()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getCRC(Long nodeId)
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
