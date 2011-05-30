/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.domain.solr.ibatis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.ChildAssocRefQueryCallback;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.solr.MetaDataResultsFilter;
import org.alfresco.repo.domain.solr.NodeMetaData;
import org.alfresco.repo.domain.solr.NodeMetaDataEntity;
import org.alfresco.repo.domain.solr.NodeMetaDataParameters;
import org.alfresco.repo.domain.solr.NodeParameters;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.domain.solr.SOLRTransactionParameters;
import org.alfresco.repo.domain.solr.Transaction;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * DAO support for SOLR web scripts.
 * 
 * @since 4.0
 */
// TODO Freemarker requires the construction of a model which means that lists and maps need to be built up in memory
// - consider building the JSON in a more streaming manner, without building up of these data structures.
// downside: loss of separation of model and view
// upside: better performance?
public class SOLRDAOImpl implements SOLRDAO
{
    private static final Log logger = LogFactory.getLog(SOLRDAOImpl.class);
    private static final String SELECT_TRANSACTIONS = "alfresco.solr.select_Txns";
    private static final String SELECT_NODES = "alfresco.solr.select_Txn_Nodes";
    
    private DictionaryService dictionaryService;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private OwnableService ownableService;

    private SqlSessionTemplate template;
    
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setQNameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /*
     * Initialize
     */    
    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
    }
    
	@SuppressWarnings("unchecked")
    /**
     * {@inheritDoc}
     */
	public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, int maxResults)
	{
	    if(minTxnId == null && fromCommitTime == null && (maxResults == 0 || maxResults == Integer.MAX_VALUE))
	    {
	        throw new IllegalArgumentException("Must specify at least one parameter");
	    }

	    List<Transaction> txns = null;
	    SOLRTransactionParameters params = new SOLRTransactionParameters();
	    params.setMinTxnId(minTxnId);
	    params.setTxnFromCommitTime(fromCommitTime);

	    if(maxResults != 0 && maxResults != Integer.MAX_VALUE)
	    {
	        txns = (List<Transaction>)template.selectList(SELECT_TRANSACTIONS, params, new RowBounds(0, maxResults));
	    }
	    else
	    {
            txns = (List<Transaction>)template.selectList(SELECT_TRANSACTIONS, params);
	    }
	    
	    return txns;
	}

	@SuppressWarnings("unchecked")
	// TODO should create qnames if don't exist?
    /**
     * {@inheritDoc}
     */
	public void getNodes(NodeParameters nodeParameters, NodeQueryCallback callback)
	{
	    List<NodeEntity> nodes = null;
        NodeQueryRowHandler rowHandler = new NodeQueryRowHandler(callback);

        if(nodeParameters.getIncludeTypeIds() == null && nodeParameters.getIncludeNodeTypes() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getIncludeNodeTypes(), false);
            nodeParameters.setIncludeTypeIds(new ArrayList<Long>(qnamesIds));
        }

        if(nodeParameters.getExcludeTypeIds() == null && nodeParameters.getExcludeNodeTypes() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getExcludeNodeTypes(), false);
            nodeParameters.setExcludeTypeIds(new ArrayList<Long>(qnamesIds));
        }
        
        if(nodeParameters.getExcludeAspectIds() == null && nodeParameters.getExcludeAspects() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getExcludeAspects(), false);
            nodeParameters.setExcludeAspectIds(new ArrayList<Long>(qnamesIds));
        }

        if(nodeParameters.getIncludeAspectIds() == null && nodeParameters.getIncludeAspects() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getIncludeAspects(), false);
            nodeParameters.setIncludeAspectIds(new ArrayList<Long>(qnamesIds));
        }

	    if(nodeParameters.getMaxResults() != 0 && nodeParameters.getMaxResults() != Integer.MAX_VALUE)
	    {
	        nodes = (List<NodeEntity>)template.selectList(SELECT_NODES, nodeParameters,
	                new RowBounds(0, nodeParameters.getMaxResults()));
	    }
	    else
	    {
	        nodes = (List<NodeEntity>)template.selectList(SELECT_NODES, nodeParameters);
	    }
	    
	    for(NodeEntity node : nodes)
	    {
	    	rowHandler.processResult(node);
	    }
	}

	/**
	 * A dumb iterator that iterates over longs in sequence.
	 *
	 */
	private static class SequenceIterator implements Iterable<Long>
	{
	    private long fromId;
	    private long toId;
	    private long counter;

	    SequenceIterator(Long fromId, Long toId)
	    {
	        this.fromId = (fromId == null ? 1 : fromId.longValue());
	        this.toId = (toId == null ? Long.MAX_VALUE : toId.longValue());
	        this.counter = this.fromId;
	    }
	    
        @Override
        public Iterator<Long> iterator()
        {
            counter = this.fromId;
            return new Iterator<Long>() {

                @Override
                public boolean hasNext()
                {
                    return counter <= toId;
                }

                @Override
                public Long next()
                {
                    return counter++;
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
	}
	   
    /**
     * {@inheritDoc}
     */
    public void getNodesMetadata(NodeMetaDataParameters nodeMetaDataParameters, MetaDataResultsFilter resultFilter, NodeMetaDataQueryCallback callback)
    {
        int maxResults = nodeMetaDataParameters.getMaxResults();
        NodeMetaDataQueryRowHandler rowHandler = new NodeMetaDataQueryRowHandler(callback);
        boolean isLimitSet = (maxResults != 0 && maxResults != Integer.MAX_VALUE);
        boolean includeType = (resultFilter == null ? true : resultFilter.getIncludeType());
        boolean includeProperties = (resultFilter == null ? true : resultFilter.getIncludeProperties());
        boolean includeAspects = (resultFilter == null ? true : resultFilter.getIncludeAspects());
        boolean includePaths = (resultFilter == null ? true : resultFilter.getIncludePaths());
        boolean includeNodeRef = (resultFilter == null ? true : resultFilter.getIncludeNodeRef());
        boolean includeAssociations = (resultFilter == null ? true : resultFilter.getIncludeAssociations());
        boolean includeChildAssociations = (resultFilter == null ? true : resultFilter.getIncludeChildAssociations());
        boolean includeOwner = (resultFilter == null ? true : resultFilter.getIncludeOwner());
        
        Iterable<Long> iterable = null;
        if(nodeMetaDataParameters.getNodeIds() != null)
        {
            iterable = nodeMetaDataParameters.getNodeIds();
        }
        else
        {
            iterable = new SequenceIterator(nodeMetaDataParameters.getFromNodeId(), nodeMetaDataParameters.getToNodeId());
        }

        // pre-cache nodes?
        // TODO does this cache acls, etc for the node?
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(100);
        int i = 1;
        for(Long nodeId : iterable)
        {
            if(isLimitSet && i++ > maxResults)
            {
                break;
            }

            if(!nodeDAO.exists(nodeId))
            {
                continue;
            }

            Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeId);
            nodeRefs.add(pair.getSecond());
        }
        if(logger.isDebugEnabled())
        {
            logger.debug("SOLRDAO caching " + nodeRefs.size() + " nodes");
        }
        nodeDAO.cacheNodes(nodeRefs);

        i = 1;
        for(Long nodeId : iterable)
        {
            if(isLimitSet && i++ > maxResults)
            {
                break;
            }

            if(!nodeDAO.exists(nodeId))
            {
                // ignore deleted node?
                // TODO nodeDAO doesn't cache anything for deleted nodes. Should we be ignoring delete node meta data?
                continue;
            }

            NodeMetaDataEntity nodeMetaData = new NodeMetaDataEntity();

            nodeMetaData.setNodeId(nodeId);

            Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeId);

            nodeMetaData.setAclId(nodeDAO.getNodeAclId(nodeId));

            if(includeType)
            {
                QName nodeType = nodeDAO.getNodeType(nodeId);
                nodeMetaData.setNodeType(nodeType);
            }

            if(includeProperties)
            {
                Map<QName, Serializable> props = nodeDAO.getNodeProperties(nodeId);
                nodeMetaData.setProperties(props);
            }

            if(includeAspects)
            {
                Set<QName> aspects = nodeDAO.getNodeAspects(nodeId);
                nodeMetaData.setAspects(aspects);
            }

            // paths may change during get i.e. node moved around in the graph
            if(includePaths)
            {
                List<Path> paths = nodeDAO.getPaths(pair, false);
                nodeMetaData.setPaths(paths);
            }

            if(includeNodeRef)
            {
                nodeMetaData.setNodeRef(pair.getSecond());
            }
        
            if(includeChildAssociations)
            {
                final List<ChildAssociationRef> childAssocs = new ArrayList<ChildAssociationRef>(100);
                nodeDAO.getChildAssocs(nodeId, null, null, null, false, false, new ChildAssocRefQueryCallback()
                {
                    @Override
                    public boolean preLoadNodes()
                    {
                        // already cached above
                        return false;
                    }
                    
                    @Override
                    public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                            Pair<Long, NodeRef> childNodePair)
                    {
                        childAssocs.add(childAssocPair.getSecond());
                        return true;
                    }
                    
                    @Override
                    public void done()
                    {
                    }
                });
                nodeMetaData.setChildAssocs(childAssocs);
            }

            if(includeAssociations)
            {
                // TODO non-child associations
//                Collection<Pair<Long, AssociationRef>> sourceAssocs = nodeDAO.getSourceNodeAssocs(nodeId);
//                Collection<Pair<Long, AssociationRef>> targetAssocs = nodeDAO.getTargetNodeAssocs(nodeId);
//                
//                nodeMetaData.setAssocs();
            }
            
            if(includeOwner)
            {
                // cached in OwnableService
                nodeMetaData.setOwner(ownableService.getOwner(pair.getSecond()));
            }
 
            rowHandler.processResult(nodeMetaData);
        }
    }
    
    /**
     * Class that passes results from a result entity into the client callback
     */
    protected class NodeQueryRowHandler
    {
        private final NodeQueryCallback callback;
        private boolean more;

        private NodeQueryRowHandler(NodeQueryCallback callback)
        {
            this.callback = callback;
            this.more = true;
        }
        
        public void processResult(Node row)
        {
            if (!more)
            {
                // No more results required
                return;
            }
            
            more = callback.handleNode(row);
        }
    }
    
    /**
     * Class that passes results from a result entity into the client callback
     */
    protected class NodeMetaDataQueryRowHandler
    {
        private final NodeMetaDataQueryCallback callback;
        private boolean more;

        private NodeMetaDataQueryRowHandler(NodeMetaDataQueryCallback callback)
        {
            this.callback = callback;
            this.more = true;
        }
        
        public void processResult(NodeMetaData row)
        {
            if (!more)
            {
                // No more results required
                return;
            }
            
            more = callback.handleNodeMetaData(row);
        }
    }
}
