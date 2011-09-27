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
package org.alfresco.repo.domain.node.ibatis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.alfresco.ibatis.IdsEntity;
import org.alfresco.repo.domain.node.AbstractNodeDAOImpl;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.node.ChildPropertyEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeAspectsEntity;
import org.alfresco.repo.domain.node.NodeAssocEntity;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodeIdAndAclId;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.node.NodeUpdateEntity;
import org.alfresco.repo.domain.node.PrimaryChildrenAclUpdateEntity;
import org.alfresco.repo.domain.node.ServerEntity;
import org.alfresco.repo.domain.node.StoreEntity;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.domain.node.TransactionEntity;
import org.alfresco.repo.domain.node.TransactionQueryEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.util.Assert;


/**
 * iBatis-specific extension of the Node abstract DAO 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeDAOImpl extends AbstractNodeDAOImpl
{
    private static final String SELECT_SERVER_BY_IPADDRESS = "alfresco.node.select_ServerByIpAddress";
    private static final String INSERT_SERVER = "alfresco.node.insert.insert_Server";
    private static final String INSERT_TRANSACTION = "alfresco.node.insert.insert_Transaction";
    private static final String UPDATE_TRANSACTION_COMMIT_TIME = "alfresco.node.update_TransactionCommitTime";
    private static final String DELETE_TRANSACTION_BY_ID = "alfresco.node.delete_TransactionById";
    private static final String INSERT_STORE = "alfresco.node.insert.insert_Store";
    private static final String UPDATE_STORE_ROOT = "alfresco.node.update_StoreRoot";
    private static final String UPDATE_STORE = "alfresco.node.update_Store";
    private static final String SELECT_STORE_ALL = "alfresco.node.select_StoreAll";
    private static final String SELECT_STORE_ROOT_NODE_BY_ID = "alfresco.node.select_StoreRootNodeById";
    private static final String SELECT_STORE_ROOT_NODE_BY_REF = "alfresco.node.select_StoreRootNodeByRef";
    private static final String INSERT_NODE = "alfresco.node.insert.insert_Node";
    private static final String UPDATE_NODE = "alfresco.node.update_Node";
    private static final String UPDATE_NODE_PATCH_ACL = "alfresco.node.update_NodePatchAcl";
    private static final String DELETE_NODE_BY_ID = "alfresco.node.delete_NodeById";
    private static final String DELETE_NODES_BY_TXN_COMMIT_TIME = "alfresco.node.delete_NodesByTxnCommitTime";
    private static final String SELECT_NODE_BY_ID = "alfresco.node.select_NodeById";
    private static final String SELECT_NODE_BY_NODEREF = "alfresco.node.select_NodeByNodeRef";
    private static final String SELECT_NODES_BY_UUIDS = "alfresco.node.select_NodesByUuids";
    private static final String SELECT_NODES_BY_IDS = "alfresco.node.select_NodesByIds";
    private static final String SELECT_NODE_PROPERTIES = "alfresco.node.select_NodeProperties";
    private static final String SELECT_PROPERTIES_BY_TYPES = "alfresco.node.select_PropertiesByTypes";
    private static final String SELECT_NODE_ASPECTS = "alfresco.node.select_NodeAspects";
    private static final String INSERT_NODE_PROPERTY = "alfresco.node.insert.insert_NodeProperty";
    private static final String UPDATE_PRIMARY_CHILDREN_SHARED_ACL = "alfresco.node.update.update_PrimaryChildrenSharedAcl";
    private static final String INSERT_NODE_ASPECT = "alfresco.node.insert.insert_NodeAspect";
    private static final String DELETE_NODE_ASPECTS = "alfresco.node.delete_NodeAspects";
    private static final String DELETE_NODE_PROPERTIES = "alfresco.node.delete_NodeProperties";
    private static final String SELECT_NODES_WITH_ASPECT_IDS = "alfresco.node.select_NodesWithAspectIds";
    private static final String INSERT_NODE_ASSOC = "alfresco.node.insert.insert_NodeAssoc";
    private static final String UPDATE_NODE_ASSOC = "alfresco.node.update_NodeAssoc";
    private static final String DELETE_NODE_ASSOC = "alfresco.node.delete_NodeAssoc";
    private static final String DELETE_NODE_ASSOCS_TO_AND_FROM = "alfresco.node.delete_NodeAssocsToAndFrom";
    private static final String DELETE_NODE_ASSOCS = "alfresco.node.delete_NodeAssocs";
    private static final String SELECT_NODE_ASSOCS_BY_SOURCE = "alfresco.node.select_NodeAssocsBySource";
    private static final String SELECT_NODE_ASSOCS_BY_TARGET = "alfresco.node.select_NodeAssocsByTarget";
    private static final String SELECT_NODE_ASSOC_BY_ID = "alfresco.node.select_NodeAssocById";
    private static final String SELECT_NODE_ASSOCS_MAX_INDEX = "alfresco.node.select_NodeAssocsMaxId";
    private static final String SELECT_NODE_PRIMARY_CHILD_ACLS = "alfresco.node.select_NodePrimaryChildAcls";
    private static final String INSERT_CHILD_ASSOC = "alfresco.node.insert.insert_ChildAssoc";
    private static final String DELETE_CHILD_ASSOC_BY_ID = "alfresco.node.delete_ChildAssocById";
    private static final String UPDATE_CHILD_ASSOCS_INDEX = "alfresco.node.update_ChildAssocsIndex";
    private static final String UPDATE_CHILD_ASSOCS_UNIQUE_NAME = "alfresco.node.update_ChildAssocsUniqueName";
    private static final String DELETE_CHILD_ASSOCS_TO_AND_FROM = "alfresco.node.delete_ChildAssocsToAndFrom";
    private static final String SELECT_CHILD_ASSOC_BY_ID = "alfresco.node.select_ChildAssocById";
    private static final String SELECT_CHILD_ASSOCS_BY_PROPERTY_VALUE = "alfresco.node.select_ChildAssocsByPropertyValue";
    private static final String SELECT_CHILD_ASSOCS_OF_PARENT = "alfresco.node.select_ChildAssocsOfParent";
    private static final String SELECT_CHILD_ASSOCS_OF_PARENT_LIMITED = "alfresco.node.select_ChildAssocsOfParent_Limited";
    private static final String SELECT_CHILD_ASSOC_OF_PARENT_BY_NAME = "alfresco.node.select_ChildAssocOfParentByName";
    private static final String SELECT_CHILD_ASSOCS_OF_PARENT_WITHOUT_PARENT_ASSOCS_OF_TYPE =
            "alfresco.node.select_ChildAssocsOfParentWithoutParentAssocsOfType";
    private static final String SELECT_PARENT_ASSOCS_OF_CHILD = "alfresco.node.select_ParentAssocsOfChild";
    private static final String UPDATE_PARENT_ASSOCS_OF_CHILD = "alfresco.node.update_ParentAssocsOfChild";
    private static final String DELETE_SUBSCRIPTIONS = "alfresco.node.delete_NodeSubscriptions";
    
    private static final String UPDATE_MOVE_PARENT_ASSOCS = "alfresco.node.update_MoveParentAssocs";
    private static final String UPDATE_MOVE_CHILD_ASSOCS = "alfresco.node.update_MoveChildAssocs";
    private static final String UPDATE_MOVE_SOURCE_ASSOCS = "alfresco.node.update_MoveSourceAssocs";
    private static final String UPDATE_MOVE_TARGET_ASSOCS = "alfresco.node.update_MoveTargetAssocs";
    private static final String UPDATE_MOVE_PROPERTIES = "alfresco.node.update_MoveProperties";
    private static final String UPDATE_MOVE_ASPECTS = "alfresco.node.update_MoveAspects";
    
    private static final String SELECT_TXN_LAST = "alfresco.node.select_TxnLast";
    private static final String SELECT_TXN_NODES = "alfresco.node.select_TxnNodes";
    private static final String SELECT_TXNS = "alfresco.node.select_Txns";
    private static final String SELECT_TXN_COUNT = "alfresco.node.select_TxnCount";
    private static final String SELECT_TXN_NODE_COUNT = "alfresco.node.select_TxnNodeCount";
    private static final String SELECT_TXNS_UNUSED = "alfresco.node.select_TxnsUnused";
    private static final String SELECT_TXN_MIN_COMMIT_TIME = "alfresco.node.select_TxnMinCommitTime";
    private static final String SELECT_TXN_MAX_COMMIT_TIME = "alfresco.node.select_TxnMaxCommitTime";
    
    private QNameDAO qnameDAO;
    private DictionaryService dictionaryService;

    private SqlSessionTemplate template;
    
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }

    @Override
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
        super.setQnameDAO(qnameDAO);
    }

    @Override
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
        super.setDictionaryService(dictionaryService);
    }

    public void startBatch()
    {
        // TODO
        /*
        try
        {
            template.getSqlMapClient().startBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start DAO batch.", e);
        }
        */
    }

    public void executeBatch()
    {
        // TODO
        /*
        try
        {
            template.getSqlMapClient().executeBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start DAO batch.", e);
        }
        */
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ServerEntity selectServer(String ipAddress)
    {
        ServerEntity entity = new ServerEntity();
        entity.setIpAddress(ipAddress);
        // Potentially more results if there is a case issue (unlikely)
        List<ServerEntity> results = (List<ServerEntity>) template.selectList(SELECT_SERVER_BY_IPADDRESS, entity);
        for (ServerEntity serverEntity : results)
        {
            // Take the first one that matches regardless of case
            if (serverEntity.getIpAddress().equalsIgnoreCase(ipAddress))
            {
                return serverEntity;
            }
        }
        // There was no match
        return null;
    }

    @Override
    protected Long insertServer(String ipAddress)
    {
        ServerEntity entity = new ServerEntity();
        entity.setVersion(1L);
        entity.setIpAddress(ipAddress);
        template.insert(INSERT_SERVER, entity);
        return entity.getId();
    }

    @Override
    protected Long insertTransaction(Long serverId, String changeTxnId, Long commitTimeMs)
    {
        ServerEntity server = new ServerEntity();
        server.setId(serverId);
        TransactionEntity transaction = new TransactionEntity();
        transaction.setServer(server);
        transaction.setVersion(1L);
        transaction.setChangeTxnId(changeTxnId);
        transaction.setCommitTimeMs(commitTimeMs);
        template.insert(INSERT_TRANSACTION, transaction);
        return transaction.getId();
    }

    @Override
    protected int updateTransaction(Long txnId, Long commitTimeMs)
    {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(txnId);
        transaction.setCommitTimeMs(commitTimeMs);
        return template.update(UPDATE_TRANSACTION_COMMIT_TIME, transaction);
    }

    @Override
    protected int deleteTransaction(Long txnId)
    {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(txnId);
        return template.delete(DELETE_TRANSACTION_BY_ID, transaction);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<StoreEntity> selectAllStores()
    {
        return (List<StoreEntity>) template.selectList(SELECT_STORE_ALL);
    }

    @Override
    protected NodeEntity selectStoreRootNode(Long storeId)
    {
        StoreEntity store = new StoreEntity();
        store.setId(storeId);
        return (NodeEntity) template.selectOne(SELECT_STORE_ROOT_NODE_BY_ID, store);
    }

    @Override
    protected NodeEntity selectStoreRootNode(StoreRef storeRef)
    {
        StoreEntity store = new StoreEntity();
        store.setProtocol(storeRef.getProtocol());
        store.setIdentifier(storeRef.getIdentifier());
        return (NodeEntity) template.selectOne(SELECT_STORE_ROOT_NODE_BY_REF, store);
    }

    @Override
    protected Long insertStore(StoreEntity store)
    {
        store.setVersion(1L);
        template.insert(INSERT_STORE, store);
        return store.getId();
    }

    @Override
    protected int updateStoreRoot(StoreEntity store)
    {
        return template.update(UPDATE_STORE_ROOT, store);
    }

    @Override
    protected int updateStore(StoreEntity store)
    {
        return template.update(UPDATE_STORE, store);
    }

    @Override
    protected Long insertNode(NodeEntity node)
    {
        node.setVersion(1L);
        template.insert(INSERT_NODE, node);
        return node.getId();
    }

    @Override
    protected int updateNode(NodeUpdateEntity nodeUpdate)
    {
        // Increment the version
        nodeUpdate.incrementVersion();
        return template.update(UPDATE_NODE, nodeUpdate);
    }
    
    @Override
    protected int updateNodePatchAcl(NodeUpdateEntity nodeUpdate)
    {
        return template.update(UPDATE_NODE_PATCH_ACL, nodeUpdate);
    }

    @Override
    protected void updatePrimaryChildrenSharedAclId(
            Long primaryParentNodeId,
            Long optionalOldSharedAlcIdInAdditionToNull,
            Long newSharedAlcId)
    {
        PrimaryChildrenAclUpdateEntity primaryChildrenAclUpdateEntity = new PrimaryChildrenAclUpdateEntity();
        primaryChildrenAclUpdateEntity.setPrimaryParentNodeId(primaryParentNodeId);
        primaryChildrenAclUpdateEntity.setOptionalOldSharedAclIdInAdditionToNull(optionalOldSharedAlcIdInAdditionToNull);
        primaryChildrenAclUpdateEntity.setNewSharedAclId(newSharedAlcId);
        
        template.update(UPDATE_PRIMARY_CHILDREN_SHARED_ACL, primaryChildrenAclUpdateEntity);
    }

    @Override
    protected int deleteNodeById(Long nodeId, boolean deletedOnly)
    {
        NodeEntity node = new NodeEntity();
        node.setId(nodeId);
        // Do we delete everything (false) or just nodes already marked as deleted (true)
        node.setDeleted(deletedOnly);
        return template.delete(DELETE_NODE_BY_ID, node);
    }

    @Override
    protected int deleteNodesByCommitTime(boolean deletedOnly, long maxTxnCommitTimeMs)
    {
        TransactionQueryEntity query = new TransactionQueryEntity();
        query.setDeletedNodes(Boolean.TRUE);
        query.setMaxCommitTime(maxTxnCommitTimeMs);
        return template.delete(DELETE_NODES_BY_TXN_COMMIT_TIME, query);
    }

    @Override
    protected NodeEntity selectNodeById(Long id, Boolean deleted)
    {
        NodeEntity node = new NodeEntity();
        node.setId(id);
        // Deleted
        if (deleted != null)
        {
            node.setDeleted(deleted);
        }
        
        return (NodeEntity) template.selectOne(SELECT_NODE_BY_ID, node);
    }

    @Override
    protected NodeEntity selectNodeByNodeRef(NodeRef nodeRef, Boolean deleted)
    {
        StoreEntity store = new StoreEntity();
        StoreRef storeRef = nodeRef.getStoreRef();
        store.setProtocol(storeRef.getProtocol());
        store.setIdentifier(storeRef.getIdentifier());
        
        NodeEntity node = new NodeEntity();
        // Store
        node.setStore(store);
        // UUID
        String uuid = nodeRef.getId();
        if (uuid.length() > 36)
        {
            return null;            // Avoid DB2 query failure if someone passes in a made-up UUID
        }
        node.setUuid(uuid);
        // Deleted
        if (deleted != null)
        {
            node.setDeleted(deleted);
        }
        
        return (NodeEntity) template.selectOne(SELECT_NODE_BY_NODEREF, node);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Node> selectNodesByUuids(Long storeId, SortedSet<String> uuids)
    {
        NodeBatchLoadEntity nodeBatchLoadEntity = new NodeBatchLoadEntity();
        nodeBatchLoadEntity.setStoreId(storeId);
        nodeBatchLoadEntity.setUuids(new ArrayList<String>(uuids));
        
        return (List<Node>) template.selectList(SELECT_NODES_BY_UUIDS, nodeBatchLoadEntity);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Node> selectNodesByIds(SortedSet<Long> ids)
    {
        NodeBatchLoadEntity nodeBatchLoadEntity = new NodeBatchLoadEntity();
        nodeBatchLoadEntity.setIds(new ArrayList<Long>(ids));
        
        return (List<Node>) template.selectList(SELECT_NODES_BY_IDS, nodeBatchLoadEntity);
    }

    
    /**
     * Pull out the key-value pairs from the rows
     */
    private Map<Long, Map<NodePropertyKey, NodePropertyValue>> makePersistentPropertiesMap(List<NodePropertyEntity> rows)
    {
        Map<Long, Map<NodePropertyKey, NodePropertyValue>> results = new HashMap<Long, Map<NodePropertyKey, NodePropertyValue>>(3);
        for (NodePropertyEntity row : rows)
        {
            Long nodeId = row.getNodeId();
            if (nodeId == null)
            {
                throw new RuntimeException("Expect results with a Node ID: " + row);
            }
            Map<NodePropertyKey, NodePropertyValue> props = results.get(nodeId);
            if (props == null)
            {
                props = new HashMap<NodePropertyKey, NodePropertyValue>(17);
                results.put(nodeId, props);
            }
            props.put(row.getKey(), row.getValue());
        }
        // Done
        return results;
    }
    
    /**
     * Convert key-value pairs into rows
     */
    private List<NodePropertyEntity> makePersistentRows(Long nodeId, Map<NodePropertyKey, NodePropertyValue> map)
    {
        List<NodePropertyEntity> rows = new ArrayList<NodePropertyEntity>(map.size());
        for (Map.Entry<NodePropertyKey, NodePropertyValue> entry : map.entrySet())
        {
            NodePropertyEntity row = new NodePropertyEntity();
            row.setNodeId(nodeId);
            row.setKey(entry.getKey());
            row.setValue(entry.getValue());
            rows.add(row);
        }
        // Done
        return rows;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected List<NodeAspectsEntity> selectNodeAspects(Set<Long> nodeIds)
    {
        if (nodeIds.size() == 0)
        {
            return Collections.emptyList();
        }
        NodeAspectsEntity aspects = new NodeAspectsEntity();
        aspects.setNodeIds(new ArrayList<Long>(nodeIds));

        List<NodeAspectsEntity> rows = (List<NodeAspectsEntity>) template.selectList(SELECT_NODE_ASPECTS, aspects);
        return rows;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected Map<Long, Map<NodePropertyKey, NodePropertyValue>> selectNodeProperties(Set<Long> nodeIds)
    {
        if (nodeIds.size() == 0)
        {
            return Collections.emptyMap();
        }
        NodePropertyEntity prop = new NodePropertyEntity();
        prop.setNodeIds(new ArrayList<Long>(nodeIds));

        List<NodePropertyEntity> rows = (List<NodePropertyEntity>) template.selectList(SELECT_NODE_PROPERTIES, prop);
        return makePersistentPropertiesMap(rows);
    }
    @Override
    protected Map<NodePropertyKey, NodePropertyValue> selectNodeProperties(Long nodeId)
    {
        return selectNodeProperties(nodeId, Collections.<Long>emptySet());
    }
    @Override
    @SuppressWarnings("unchecked")
    protected Map<NodePropertyKey, NodePropertyValue> selectNodeProperties(Long nodeId, Set<Long> qnameIds)
    {
        NodePropertyEntity prop = new NodePropertyEntity();
        // Node
        prop.setNodeId(nodeId);
        // QName(s)
        switch(qnameIds.size())
        {
        case 0:
            // Ignore
            break;
        case 1:
            prop.setKey(new NodePropertyKey());
            prop.getKey().setQnameId(qnameIds.iterator().next());
            break;
        default:
            prop.setQnameIds(new ArrayList<Long>(qnameIds));
        }

        List<NodePropertyEntity> rows = (List<NodePropertyEntity>) template.selectList(SELECT_NODE_PROPERTIES, prop);
        Map<Long, Map<NodePropertyKey, NodePropertyValue>> results = makePersistentPropertiesMap(rows);
        Map<NodePropertyKey, NodePropertyValue> props = results.get(nodeId);
        if (props == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            return props;
        }
    }

    @Override
    protected int deleteNodeProperties(Long nodeId, Set<Long> qnameIds)
    {
        NodePropertyEntity prop = new NodePropertyEntity();
        // Node
        prop.setNodeId(nodeId);
        // QNames
        if (qnameIds != null)
        {
            if (qnameIds.isEmpty())
            {
                return 0;         // Nothing to do
            }
            prop.setQnameIds(new ArrayList<Long>(qnameIds));
        }
        
        return template.delete(DELETE_NODE_PROPERTIES, prop);
    }

    @Override
    protected int deleteNodeProperties(Long nodeId, List<NodePropertyKey> propKeys)
    {
        Assert.notNull(nodeId, "Must have 'nodeId'");
        Assert.notNull(nodeId, "Must have 'propKeys'");
        
        if (propKeys.size() == 0)
        {
            return 0;
        }
        
        NodePropertyEntity prop = new NodePropertyEntity();
        // Node
        prop.setNodeId(nodeId);
        
        startBatch();
        int count = 0;
        try
        {
            for (NodePropertyKey propKey : propKeys)
            {
                prop.setKey(propKey);
                count += template.delete(DELETE_NODE_PROPERTIES, prop);
            }
        }
        finally
        {
            executeBatch();
        }
        return count;
    }

    @Override
    protected void insertNodeProperties(Long nodeId, Map<NodePropertyKey, NodePropertyValue> persistableProps)
    {
        if (persistableProps.isEmpty())
        {
            return;
        }
        
        List<NodePropertyEntity> rows = makePersistentRows(nodeId, persistableProps);
        
        startBatch();
        try
        {
            for (NodePropertyEntity row : rows)
            {
                template.insert(INSERT_NODE_PROPERTY, row);
            }
        }
        finally
        {
            executeBatch();
        }
    }

    @Override
    protected Set<Long> selectNodeAspectIds(Long nodeId)
    {
    	Set<Long> aspectIds = new HashSet<Long>();
    	Set<Long> nodeIds = new HashSet<Long>();
    	nodeIds.add(nodeId);
    	List<NodeAspectsEntity> nodeAspectEntities = selectNodeAspects(nodeIds);
    	if(nodeAspectEntities.size() > 0)
    	{
    		NodeAspectsEntity nodeAspects = nodeAspectEntities.get(0);
    		aspectIds.addAll(nodeAspects.getAspectQNameIds());
    	}
		return aspectIds;
    }

    @Override
    protected void insertNodeAspect(Long nodeId, Long qnameId)
    {
        Map<String, Long> aspectParameters = new HashMap<String, Long>(5);
        aspectParameters.put("nodeId", nodeId);
        aspectParameters.put("qnameId", qnameId);
        template.insert(INSERT_NODE_ASPECT, aspectParameters);
    }

    @Override
    protected int deleteNodeAspects(Long nodeId, Set<Long> qnameIds)
    {
        NodeAspectsEntity nodeAspects = new NodeAspectsEntity();
        nodeAspects.setNodeId(nodeId);
        if (qnameIds != null && !qnameIds.isEmpty())
        {
            nodeAspects.setAspectQNameIds(new ArrayList<Long>(qnameIds));                // Null means all
        }
        return template.delete(DELETE_NODE_ASPECTS, nodeAspects);
    }

    @Override
    protected void selectNodesWithAspects(
            List<Long> qnameIds,
            Long minNodeId, Long maxNodeId,
            final NodeRefQueryCallback resultsCallback)
    {
        ResultHandler resultHandler = new ResultHandler()
        {
            public void handleResult(ResultContext context)
            {
                NodeEntity entity = (NodeEntity) context.getResultObject();
                Pair<Long, NodeRef> nodePair = new Pair<Long, NodeRef>(entity.getId(), entity.getNodeRef());
                resultsCallback.handle(nodePair);
            }
        };
        
        IdsEntity parameters = new IdsEntity();
        parameters.setIdOne(minNodeId);
        parameters.setIdTwo(maxNodeId);
        parameters.setIds(qnameIds);
        template.select(SELECT_NODES_WITH_ASPECT_IDS, parameters, resultHandler);
    }

    @Override
    protected Long insertNodeAssoc(Long sourceNodeId, Long targetNodeId, Long assocTypeQNameId, int assocIndex)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        assoc.setVersion(1L);
        assoc.setTypeQNameId(assocTypeQNameId);
        // Source
        NodeEntity sourceNode = new NodeEntity();
        sourceNode.setId(sourceNodeId);
        assoc.setSourceNode(sourceNode);
        // Target
        NodeEntity targetNode = new NodeEntity();
        targetNode.setId(targetNodeId);
        assoc.setTargetNode(targetNode);
        // Index
        assoc.setAssocIndex(assocIndex);
        
        template.insert(INSERT_NODE_ASSOC, assoc);
        return assoc.getId();
    }

    @Override
    protected int updateNodeAssoc(Long id, int assocIndex)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        assoc.setId(id);
        assoc.setAssocIndex(assocIndex);
        
        return template.update(UPDATE_NODE_ASSOC, assoc);
    }

    @Override
    protected int deleteNodeAssoc(Long sourceNodeId, Long targetNodeId, Long assocTypeQNameId)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        assoc.setTypeQNameId(assocTypeQNameId);
        // Source
        NodeEntity sourceNode = new NodeEntity();
        sourceNode.setId(sourceNodeId);
        assoc.setSourceNode(sourceNode);
        // Target
        NodeEntity targetNode = new NodeEntity();
        targetNode.setId(targetNodeId);
        assoc.setTargetNode(targetNode);
        
        return template.delete(DELETE_NODE_ASSOC, assoc);
    }

    @Override
    protected int deleteNodeAssocsToAndFrom(Long nodeId)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        // Source
        NodeEntity sourceNode = new NodeEntity();
        sourceNode.setId(nodeId);
        assoc.setSourceNode(sourceNode);
        // Target
        NodeEntity targetNode = new NodeEntity();
        targetNode.setId(nodeId);
        assoc.setTargetNode(targetNode);
        
        return template.delete(DELETE_NODE_ASSOCS_TO_AND_FROM, assoc);
    }

    @Override
    protected int deleteNodeAssocsToAndFrom(Long nodeId, Set<Long> assocTypeQNameIds)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        assoc.setTypeQNameIds(new ArrayList<Long>(assocTypeQNameIds));
        // Source
        NodeEntity sourceNode = new NodeEntity();
        sourceNode.setId(nodeId);
        assoc.setSourceNode(sourceNode);
        // Target
        NodeEntity targetNode = new NodeEntity();
        targetNode.setId(nodeId);
        assoc.setTargetNode(targetNode);
        
        return template.delete(DELETE_NODE_ASSOCS_TO_AND_FROM, assoc);
    }

    @Override
    protected int deleteNodeAssocs(List<Long> ids)
    {
        IdsEntity param = new IdsEntity();
        param.setIds(ids);
        return template.delete(DELETE_NODE_ASSOCS, param);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<NodeAssocEntity> selectNodeAssocsBySource(Long sourceNodeId, Long typeQNameId)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        // Source
        NodeEntity sourceNode = new NodeEntity();
        sourceNode.setId(sourceNodeId);
        assoc.setSourceNode(sourceNode);
        // Type
        assoc.setTypeQNameId(typeQNameId);
        
        return (List<NodeAssocEntity>) template.selectList(SELECT_NODE_ASSOCS_BY_SOURCE, assoc);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<NodeAssocEntity> selectNodeAssocsByTarget(Long targetNodeId, Long typeQNameId)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        // Target
        NodeEntity targetNode = new NodeEntity();
        targetNode.setId(targetNodeId);
        assoc.setTargetNode(targetNode);
        // Type
        assoc.setTypeQNameId(typeQNameId);
        
        return (List<NodeAssocEntity>) template.selectList(SELECT_NODE_ASSOCS_BY_TARGET, assoc);
    }

    @Override
    protected NodeAssocEntity selectNodeAssocById(Long assocId)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        assoc.setId(assocId);
        
        return (NodeAssocEntity) template.selectOne(SELECT_NODE_ASSOC_BY_ID, assoc);
    }

    @Override
    protected int selectNodeAssocMaxIndex(Long sourceNodeId, Long assocTypeQNameId)
    {
        NodeAssocEntity assoc = new NodeAssocEntity();
        // Source
        NodeEntity sourceNode = new NodeEntity();
        sourceNode.setId(sourceNodeId);
        assoc.setSourceNode(sourceNode);
        // Assoc
        assoc.setTypeQNameId(assocTypeQNameId);
        
        Integer maxIndex = (Integer) template.selectOne(SELECT_NODE_ASSOCS_MAX_INDEX, assoc);
        return maxIndex == null ? 0 : maxIndex.intValue();
    }

    @Override
    protected Long insertChildAssoc(ChildAssocEntity assoc)
    {
        assoc.setVersion(1L);
        template.insert(INSERT_CHILD_ASSOC, assoc);
        return assoc.getId();
    }

    @Override
    protected int deleteChildAssocById(Long assocId)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // ID
        assoc.setId(assocId);
        
        return template.delete(DELETE_CHILD_ASSOC_BY_ID, assoc);
    }

    @Override
    protected int updateChildAssocIndex(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            int index)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        // Type QName
        assoc.setTypeQNameAll(qnameDAO, assocTypeQName, true);
        // QName
        assoc.setQNameAll(qnameDAO, assocQName, true);
        // Index
        assoc.setAssocIndex(index);
        
        return template.update(UPDATE_CHILD_ASSOCS_INDEX, assoc);
    }

    @Override
    protected int updateChildAssocsUniqueName(Long childNodeId, String name)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        // Name
        assoc.setChildNodeNameAll(null, null, name);
        
        return template.update(UPDATE_CHILD_ASSOCS_UNIQUE_NAME, assoc);
    }

    @Override
    protected int deleteChildAssocsToAndFrom(Long nodeId)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(nodeId);
        assoc.setParentNode(parentNode);
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(nodeId);
        assoc.setChildNode(childNode);
        
        return template.delete(DELETE_CHILD_ASSOCS_TO_AND_FROM, assoc);
    }

    @Override
    protected ChildAssocEntity selectChildAssoc(Long assocId)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        assoc.setId(assocId);
        
        return (ChildAssocEntity) template.selectOne(SELECT_CHILD_ASSOC_BY_ID, assoc);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<NodeIdAndAclId> selectPrimaryChildAcls(Long nodeId)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(nodeId);
        assoc.setParentNode(parentNode);
        // Primary
        assoc.setPrimary(true);

        return (List<NodeIdAndAclId>) template.selectList(SELECT_NODE_PRIMARY_CHILD_ACLS, assoc);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<ChildAssocEntity> selectChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        // Type QName
        if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
        {
            return Collections.emptyList();     // Shortcut
        }
        // QName
        if (!assoc.setQNameAll(qnameDAO, assocQName, false))
        {
            return Collections.emptyList();     // Shortcut
        }
        
        return (List<ChildAssocEntity>) template.selectList(SELECT_CHILD_ASSOCS_OF_PARENT, assoc);
    }

    /**
     * Filter to allow the {@link ChildAssocResultHandler} to filter results.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private interface ChildAssocResultHandlerFilter
    {
        boolean isResult(ChildAssocEntity assoc);
    }
    
    /**
     * Class that pushes results to a {@link ChildAssocRefQueryCallback}.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class ChildAssocResultHandler implements ResultHandler
    {
        private final ChildAssocResultHandlerFilter filter;
        private final ChildAssocRefQueryCallback resultsCallback;
        private boolean more = true;
        
        private ChildAssocResultHandler(ChildAssocRefQueryCallback resultsCallback)
        {
            this(null, resultsCallback);
        }
        
        private ChildAssocResultHandler(ChildAssocResultHandlerFilter filter, ChildAssocRefQueryCallback resultsCallback)
        {
            this.filter = filter;
            this.resultsCallback = resultsCallback;
        }
        
        public void handleResult(ResultContext context)
        {
            // Do nothing if no further results are required
            // TODO: Use iBatis' new feature (when we upgrade) to kill the resultset walking
            if (!more)
            {
                return;
            }
            ChildAssocEntity assoc = (ChildAssocEntity) context.getResultObject();
            if (filter != null && !filter.isResult(assoc))
            {
                // Filtered out
                return;
            }
            Pair<Long, ChildAssociationRef> childAssocPair = assoc.getPair(qnameDAO);
            Pair<Long, NodeRef> parentNodePair = assoc.getParentNode().getNodePair();
            Pair<Long, NodeRef> childNodePair = assoc.getChildNode().getNodePair();
            // Call back
            boolean more = resultsCallback.handle(childAssocPair, parentNodePair, childNodePair);
            if (!more)
            {
                this.more = false;
            }
        }
    }

    @Override
    protected void selectChildAssocs(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            Boolean sameStore,
            ChildAssocRefQueryCallback resultsCallback)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Child
        if (childNodeId != null)
        {
            NodeEntity childNode = new NodeEntity();
            childNode.setId(childNodeId);
            assoc.setChildNode(childNode);
        }
        // Type QName
        if (assocTypeQName != null)
        {
            if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
            {
                return;                     // Shortcut
            }
        }
        // QName
        if (assocQName != null)
        {
            if (!assoc.setQNameAll(qnameDAO, assocQName, false))
            {
                return;                     // Shortcut
            }
        }
        // Primary
        if (isPrimary != null)
        {
            assoc.setPrimary(isPrimary);
        }
        // Same store
        if (sameStore != null)
        {
            assoc.setSameStore(sameStore);
        }
        
        ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(resultsCallback);
        
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        template.select(SELECT_CHILD_ASSOCS_OF_PARENT, assoc, resultHandler);
        
        resultsCallback.done();
    }

    public List<ChildAssociationRef> getChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            final int maxResults,
            boolean preload)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);

        // Type QName
        if (assocTypeQName != null)
        {
            if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
            {
                return Collections.emptyList();                     // Shortcut
            }
        }
        // QName
        if (assocQName != null)
        {
            if (!assoc.setQNameAll(qnameDAO, assocQName, false))
            {
                return Collections.emptyList();                     // Shortcut
            }
        }

        final List<ChildAssociationRef> result = new LinkedList<ChildAssociationRef>();
        final List<NodeRef> toLoad = new LinkedList<NodeRef>();

        // We can't invoke the row handler whilst the limited query is running as it's illegal on some databases (MySQL)
        List<?> entities = template.selectList(SELECT_CHILD_ASSOCS_OF_PARENT_LIMITED, assoc, new RowBounds(0,
                maxResults));
        ChildAssocResultHandler rowHandler = new ChildAssocResultHandler(new ChildAssocRefQueryCallback(){

            @Override
            public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                result.add(childAssocPair.getSecond());
                toLoad.add(childNodePair.getSecond());
                return true;
            }

            @Override
            public void done()
            {
            }

            @Override
            public boolean preLoadNodes()
            {
                return false;
            }});
        final DefaultResultContext resultContext = new DefaultResultContext();
        for (Object entity : entities)
        {
              resultContext.nextResultObject(entity);
              rowHandler.handleResult(resultContext);
        }
        if (preload && !toLoad.isEmpty())
        {
            cacheNodes(toLoad);
        }
        
        return result;
    }

    @Override
    protected void selectChildAssocs(
            Long parentNodeId,
            Set<QName> assocTypeQNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Type QNames
        Set<Long> assocTypeQNameIds = qnameDAO.convertQNamesToIds(assocTypeQNames, false);
        if (assocTypeQNameIds.size() == 0)
        {
            return;                         // Shortcut as they don't exist
        }
        assoc.setTypeQNameIds(new ArrayList<Long>(assocTypeQNameIds));
        
        ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(resultsCallback);
        
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        template.select(SELECT_CHILD_ASSOCS_OF_PARENT, assoc, resultHandler);
        
        resultsCallback.done();
    }

    @Override
    protected ChildAssocEntity selectChildAssoc(Long parentNodeId, QName assocTypeQName, String childName)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Type QName
        if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
        {
            return null;                    // Shortcut
        }
        // Child name
        assoc.setChildNodeNameAll(null, assocTypeQName, childName);
        
        // Note: This single results was assumed from inception of the original method.  It's correct.
        return (ChildAssocEntity) template.selectOne(SELECT_CHILD_ASSOC_OF_PARENT_BY_NAME, assoc);
    }

    @Override
    protected void selectChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            Collection<String> childNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        if (childNames.size() == 0)
        {
            return;
        }
        else if (childNames.size() > 1000)
        {
            throw new IllegalArgumentException("Unable to process more than 1000 child names in getChildAssocs");
        }
        // Work out the child names to query on
        final Set<String> childNamesShort = new HashSet<String>(childNames.size());
        final List<Long> childNamesCrc = new ArrayList<Long>(childNames.size());
        for (String childName : childNames)
        {
            String childNameLower = childName.toLowerCase();
            String childNameShort = ChildAssocEntity.getChildNodeNameShort(childNameLower);
            Long childNameCrc = ChildAssocEntity.getChildNodeNameCrc(childNameLower);
            childNamesShort.add(childNameShort);
            childNamesCrc.add(childNameCrc);
        }
        // Create a filter that checks that the name CRC is present
        ChildAssocResultHandlerFilter filter = new ChildAssocResultHandlerFilter()
        {
            public boolean isResult(ChildAssocEntity assoc)
            {
                return childNamesShort.contains(assoc.getChildNodeName());
            }
        };
        
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Type QName
        if (assocTypeQName != null)
        {
            if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
            {
                return;                         // Shortcut
            }
        }
        // Child names
        assoc.setChildNodeNameCrcs(childNamesCrc);
        
        ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(filter, resultsCallback);
        
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        template.select(SELECT_CHILD_ASSOCS_OF_PARENT, assoc, resultHandler);
        
        resultsCallback.done();
    }

    @Override
    protected void selectChildAssocsByChildTypes(
            Long parentNodeId,
            Set<QName> childNodeTypeQNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Child Node Type QNames
        Set<Long> childNodeTypeQNameIds = qnameDAO.convertQNamesToIds(childNodeTypeQNames, false);
        if (childNodeTypeQNameIds.size() == 0)
        {
            return;                         // Shortcut as they don't exist
        }
        assoc.setChildNodeTypeQNameIds(new ArrayList<Long>(childNodeTypeQNameIds));
        
        ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(resultsCallback);
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        
        template.select(SELECT_CHILD_ASSOCS_OF_PARENT, assoc, resultHandler);
        resultsCallback.done();
    }
    
    @Override
    protected void selectChildAssocsWithoutParentAssocsOfType(
            Long parentNodeId,
            QName assocTypeQName,
            ChildAssocRefQueryCallback resultsCallback)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Type QName
        if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
        {
            return;                         // Shortcut
        }

        ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(resultsCallback);
        
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        
        template.select(SELECT_CHILD_ASSOCS_OF_PARENT_WITHOUT_PARENT_ASSOCS_OF_TYPE, assoc, resultHandler);
        resultsCallback.done();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<ChildAssocEntity> selectPrimaryParentAssocs(Long childNodeId)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        // Primary
        assoc.setPrimary(Boolean.TRUE);
        
        return (List<ChildAssocEntity>) template.selectList(SELECT_PARENT_ASSOCS_OF_CHILD, assoc);
    }

    @Override
    protected void selectParentAssocs(
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            ChildAssocRefQueryCallback resultsCallback)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        // Type QName
        if (assocTypeQName != null)
        {
            if (!assoc.setTypeQNameAll(qnameDAO, assocTypeQName, false))
            {
                return;                         // Shortcut
            }
        }
        // QName
        if (assocQName != null)
        {
            if (!assoc.setQNameAll(qnameDAO, assocQName, false))
            {
                return;                         // Shortcut
            }
        }
        // Primary
        if (isPrimary != null)
        {
            assoc.setPrimary(isPrimary);
        }
        
        ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(resultsCallback);
        
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        template.select(SELECT_PARENT_ASSOCS_OF_CHILD, assoc, resultHandler);
        
        resultsCallback.done();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<ChildAssocEntity> selectParentAssocs(Long childNodeId)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        
        // TODO MyBatis workaround - see also http://code.google.com/p/mybatis/issues/detail?id=58 (and #139, #234, ...)
        template.clearCache();
        return (List<ChildAssocEntity>) template.selectList(SELECT_PARENT_ASSOCS_OF_CHILD, assoc);
    }

    @Override
    protected int updatePrimaryParentAssocs(
            Long childNodeId,
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            String childNodeName)
    {
        ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent
        NodeEntity parentNode = new NodeEntity();
        parentNode.setId(parentNodeId);
        assoc.setParentNode(parentNode);
        // Child
        NodeEntity childNode = new NodeEntity();
        childNode.setId(childNodeId);
        assoc.setChildNode(childNode);
        // Type QName
        if (assocTypeQName != null)
        {
            assoc.setTypeQNameAll(qnameDAO, assocTypeQName, true);
            // Have to recalculate the crc values for the association
            assoc.setChildNodeNameAll(dictionaryService, assocTypeQName, childNodeName);
        }
        // QName
        if (assocQName != null)
        {
            assoc.setQNameAll(qnameDAO, assocQName, true);
        }
        // Primary
        assoc.setPrimary(Boolean.TRUE);
        
        return template.update(UPDATE_PARENT_ASSOCS_OF_CHILD, assoc);
    }

    @Override
    protected void moveNodeData(Long fromNodeId, Long toNodeId)
    {
        IdsEntity params = new IdsEntity();
        params.setIdOne(fromNodeId);
        params.setIdTwo(toNodeId);
        
        
        int countPA = template.update(UPDATE_MOVE_PARENT_ASSOCS, params);
        int countCA = template.update(UPDATE_MOVE_CHILD_ASSOCS, params);
        int countSA = template.update(UPDATE_MOVE_SOURCE_ASSOCS, params);
        int countTA = template.update(UPDATE_MOVE_TARGET_ASSOCS, params);
        int countP = template.update(UPDATE_MOVE_PROPERTIES, params);
        int countA = template.update(UPDATE_MOVE_ASPECTS, params);
        if (isDebugEnabled)
        {
            logger.debug(
                    "Moved node data: \n" +
                    "   From: " + fromNodeId + "\n" +
                    "   To:   " + toNodeId + "\n" +
                    "   PA:   " + countPA + "\n" +
                    "   CA:   " + countCA + "\n" +
                    "   SA:   " + countSA + "\n" +
                    "   TA:   " + countTA + "\n" +
                    "   P:    " + countP + "\n" +
                    "   A:    " + countA);
        }
    }

    /**
     * The default implementation relies on <b>ON DELETE CASCADE</b> and the
     * subscriptions avoiding deleted nodes - NoOp.
     */
    @Override
    protected void deleteSubscriptions(Long nodeId)
    {
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Transaction selectLastTxnBeforeCommitTime(Long maxCommitTime)
    {
        Assert.notNull(maxCommitTime, "maxCommitTime");
        
        TransactionQueryEntity query = new TransactionQueryEntity();
        query.setMaxCommitTime(maxCommitTime);
        
        List<Transaction> txns = (List<Transaction>) template.selectList(SELECT_TXN_LAST, query, new RowBounds(0, 1));
        if (txns.size() > 0)
        {
            return txns.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    protected int selectTransactionCount()
    {
        return (Integer) template.selectOne(SELECT_TXN_COUNT);
    }

    @Override
    protected Transaction selectTxnById(Long txnId)
    {
        TransactionQueryEntity query = new TransactionQueryEntity();
        query.setId(txnId);
        
        return (Transaction) template.selectOne(SELECT_TXNS, query);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<NodeEntity> selectTxnChanges(Long txnId, Long storeId)
    {
        TransactionQueryEntity query = new TransactionQueryEntity();
        query.setId(txnId);
        if (storeId != null)
        {
            query.setStoreId(storeId);
        }
        
        // TODO: Return List<Node> for quicker node_deleted access
        return (List<NodeEntity>) template.selectList(SELECT_TXN_NODES, query);
    }

    @Override
    protected int selectTxnNodeChangeCount(Long txnId, Boolean updates)
    {
        NodeEntity node = new NodeEntity();
        // Updates or deletes
        if (updates != null)
        {
            node.setDeleted(Boolean.valueOf(!updates));
        }
        // Transaction
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(txnId);
        node.setTransaction(transaction);

        return (Integer) template.selectOne(SELECT_TXN_NODE_COUNT, node);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Transaction> selectTxns(
            Long fromTimeInclusive,
            Long toTimeExclusive,
            Integer count,
            List<Long> includeTxnIds,
            List<Long> excludeTxnIds,
            Long excludeServerId,
            Boolean ascending)
    {
        TransactionQueryEntity query = new TransactionQueryEntity();
        query.setMinCommitTime(fromTimeInclusive);
        query.setMaxCommitTime(toTimeExclusive);
        
        if ((includeTxnIds != null) && (includeTxnIds.size() > 0))
        { 
            query.setIncludeTxnIds(includeTxnIds);
        }
        
        if ((excludeTxnIds != null) && (excludeTxnIds.size() > 0))
        {
            query.setExcludeTxnIds(excludeTxnIds);
        }
        
        query.setExcludeServerId(excludeServerId);
        query.setAscending(ascending);
        
        if (count == null)
        {
            return (List<Transaction>) template.selectList(SELECT_TXNS, query);
        }
        else
        {
            return (List<Transaction>) template.selectList(SELECT_TXNS, query, new RowBounds(0, count));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> selectTxnsUnused(Long minTxnId, Long maxCommitTime, Integer count)
    {
        TransactionQueryEntity query = new TransactionQueryEntity();
        query.setMinId(minTxnId);
        query.setMaxCommitTime(maxCommitTime);
        if (count == null)
        {
            return (List<Long>) template.selectList(SELECT_TXNS_UNUSED, query);
        }
        else
        {
            return (List<Long>) template.selectList(SELECT_TXNS_UNUSED, query, new RowBounds(0, count));
        }
    }

    @Override
    protected Long selectMinTxnCommitTime()
    {
        return (Long) template.selectOne(SELECT_TXN_MIN_COMMIT_TIME);
    }

    @Override
    protected Long selectMaxTxnCommitTime()
    {
        return (Long) template.selectOne(SELECT_TXN_MAX_COMMIT_TIME);
    }

    @Override
    protected void selectChildAssocsByPropertyValue(Long parentNodeId,
            QName propertyQName, 
            NodePropertyValue nodeValue,
            ChildAssocRefQueryCallback resultsCallback)
    {
        ChildPropertyEntity assocProp = new ChildPropertyEntity();
     
        // Parent
        assocProp.setParentNodeId(parentNodeId);
        
        // Property name
        Pair<Long,QName> propName = qnameDAO.getQName(propertyQName);
        
        if(propName != null)
        {
            // Property
            assocProp.setValue(nodeValue);
            assocProp.setPropertyQNameId(propName.getFirst());
        
            ChildAssocResultHandler resultHandler = new ChildAssocResultHandler(resultsCallback);
            template.select(SELECT_CHILD_ASSOCS_BY_PROPERTY_VALUE, assocProp, resultHandler);
            resultsCallback.done();
        }
    }
    
    @Override
    public List<NodePropertyEntity> selectProperties(Collection<PropertyDefinition> propertyDefs)
    {
		final List<NodePropertyEntity> properties = new ArrayList<NodePropertyEntity>();

    	Set<QName> qnames = new HashSet<QName>();
		for(PropertyDefinition propDef : propertyDefs)
		{
			qnames.add(propDef.getName());
		}

		// qnames of properties that are encrypted
		Set<Long> qnameIds = qnameDAO.convertQNamesToIds(qnames, false);
		if(qnameIds.size() > 0)
		{
	        IdsEntity param = new IdsEntity();
	        param.setIds(new ArrayList<Long>(qnameIds));
		    // TODO - use a callback approach
	        template.select(SELECT_PROPERTIES_BY_TYPES, param, new ResultHandler()
	        {
				@Override
				public void handleResult(ResultContext context)
				{
					properties.add((NodePropertyEntity)context.getResultObject());
				}
	        });
		}

		return properties;
    }
    
    /*
     * DAO OVERRIDES
     */
    
    /**
     * MSSQL requires some overrides to handle specific behaviour.
     */
    public static class MSSQL extends NodeDAOImpl
    {
        private SqlSessionTemplate template;
        
        public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
        {
            super.setSqlSessionTemplate(sqlSessionTemplate);
            this.template = sqlSessionTemplate;
        }

        /**
         * Overrides the super class's NO-OP to cascade-delete subscriptions in code.
         */
        @Override
        protected void deleteSubscriptions(Long nodeId)
        {
            template.delete(DELETE_SUBSCRIPTIONS, nodeId);
        }
    }
}