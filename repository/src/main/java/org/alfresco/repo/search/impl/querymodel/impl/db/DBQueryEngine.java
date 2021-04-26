/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import static org.alfresco.repo.domain.node.AbstractNodeDAOImpl.CACHE_REGION_NODES;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.OptionalPatchApplicationCheckBootstrapBean;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.StoreEntity;
import org.alfresco.repo.domain.permissions.AclCrudDAO;
import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.lucene.PagingLuceneResultSet;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * @author Andy
 */
@NotThreadSafe
public class DBQueryEngine implements QueryEngine
{
    protected static final Log logger = LogFactory.getLog(DBQueryEngine.class);
    
    protected static final String SELECT_BY_DYNAMIC_QUERY = "alfresco.metadata.query.select_byDynamicQuery";
    
    protected SqlSessionTemplate template;

    protected QNameDAO qnameDAO;
    
    private NodeDAO nodeDAO;

    protected DictionaryService dictionaryService;

    protected NamespaceService namespaceService;
    
    protected NodeService nodeService;

    private TenantService tenantService;
    
    private OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck2;

    protected PermissionService permissionService;

    private int maxPermissionChecks;
    
    private long maxPermissionCheckTimeMillis;

    protected EntityLookupCache<Long, Node, NodeRef> nodesCache;
    
    AclCrudDAO aclCrudDAO;

    public void setAclCrudDAO(AclCrudDAO aclCrudDAO)
    {
        this.aclCrudDAO = aclCrudDAO;
    }
    
    public void setMaxPermissionChecks(int maxPermissionChecks)
    {
        this.maxPermissionChecks = maxPermissionChecks;
    }
    
    public void setMaxPermissionCheckTimeMillis(long maxPermissionCheckTimeMillis)
    {
        this.maxPermissionCheckTimeMillis = maxPermissionCheckTimeMillis;
    }

    public void setTemplate(SqlSessionTemplate template)
    {
        this.template = template;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setMetadataIndexCheck2(OptionalPatchApplicationCheckBootstrapBean metadataIndexCheck2)
    {
        this.metadataIndexCheck2 = metadataIndexCheck2;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        this.template = sqlSessionTemplate;
    }

    /**
     * @param qnameDAO
     *            the qnameDAO to set
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param dictionaryService
     *            the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param namespaceService
     *            the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeDAO the nodeDAO to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.QueryEngine#executeQuery(org.alfresco.repo.search.impl.querymodel.Query,
     * org.alfresco.repo.search.impl.querymodel.QueryOptions,
     * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    @Override
    public QueryEngineResults executeQuery(Query query, QueryOptions options, FunctionEvaluationContext functionContext)
    {
        long start = 0;
        if (logger.isDebugEnabled())
        {
            start = System.currentTimeMillis();
            logger.debug("Query request received");
        }

        Set<String> selectorGroup = null;
        if (query.getSource() != null)
        {
            List<Set<String>> selectorGroups = query.getSource().getSelectorGroups(functionContext);

            if (selectorGroups.size() == 0)
            {
                throw new QueryModelException("No selectors");
            }

            if (selectorGroups.size() > 1)
            {
                throw new QueryModelException("Advanced join is not supported");
            }

            selectorGroup = selectorGroups.get(0);
        }

        DBQuery dbQuery = (DBQuery)query;
        
        if (options.getStores().size() > 1)
        {
            throw new QueryModelException("Multi-store queries are not supported");
        }
        
        // MT
        StoreRef storeRef = options.getStores().get(0);
        storeRef = storeRef != null ? tenantService.getName(storeRef) : null;

        Pair<Long, StoreRef> store = nodeDAO.getStore(storeRef);
        if (store == null)
        {
        	  throw new QueryModelException("Unknown store: "+storeRef);
        }
        dbQuery.setStoreId(store.getFirst());
        Pair<Long, QName> sysDeletedType = qnameDAO.getQName(ContentModel.TYPE_DELETED);
        if (sysDeletedType == null)
        {
            dbQuery.setSysDeletedType(-1L);
        }
        else
        {
            dbQuery.setSysDeletedType(sysDeletedType.getFirst());
        }
        
        Long sinceTxId = options.getSinceTxId();
        if (sinceTxId == null)
        {
            // By default, return search results for all transactions.
            sinceTxId = -1L;
        }
        dbQuery.setSinceTxId(sinceTxId);
        
        logger.debug("- query is being prepared");
        dbQuery.prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectorGroup,
                null, functionContext, metadataIndexCheck2.getPatchApplied());

        ResultSet resultSet;
        resultSet = selectNodesWithPermissions(options, dbQuery);
        if (logger.isDebugEnabled())
        {
            long ms = System.currentTimeMillis() - start;
            logger.debug("Selected " + resultSet.length() + " nodes with permission resolution in "+ms+" ms");
        }
        return asQueryEngineResults(resultSet);
    }
    
    protected String pickQueryTemplate(QueryOptions options, DBQuery dbQuery)
    {
        logger.debug("- using standard table for the query");
        return SELECT_BY_DYNAMIC_QUERY;
    }

    private ResultSet selectNodesWithPermissions(QueryOptions options, DBQuery dbQuery)
    {
        Authority authority = aclCrudDAO.getAuthority(AuthenticationUtil.getRunAsUser());
        
        NodePermissionAssessor permissionAssessor = createAssessor(authority);
        int maxPermsChecks = options.getMaxPermissionChecks() < 0 ? maxPermissionChecks : options.getMaxPermissionChecks();
        long maxPermCheckTimeMillis = options.getMaxPermissionCheckTimeMillis() < 0
                ? maxPermissionCheckTimeMillis
                : options.getMaxPermissionCheckTimeMillis();
        permissionAssessor.setMaxPermissionChecks(maxPermsChecks);
        permissionAssessor.setMaxPermissionCheckTimeMillis(maxPermCheckTimeMillis);
        
        FilteringResultSet resultSet = acceleratedNodeSelection(options, dbQuery, permissionAssessor);
        
        PagingLuceneResultSet plrs = new PagingLuceneResultSet(resultSet, options.getAsSearchParmeters(), nodeService);
        plrs.setTrimmedResultSet(true);
        return plrs;
    }

    protected NodePermissionAssessor createAssessor(Authority authority)
    {
        return new NodePermissionAssessor(nodeService, permissionService, authority, nodesCache);
    }

    FilteringResultSet acceleratedNodeSelection(QueryOptions options, DBQuery dbQuery, NodePermissionAssessor permissionAssessor)
    {
        List<Node> nodes = new ArrayList<>();
        int requiredNodes = computeRequiredNodesCount(options);
        
        logger.debug("- query sent to the database");
        template.select(pickQueryTemplate(options, dbQuery), dbQuery, new ResultHandler<Node>()
        {
            @Override
            public void handleResult(ResultContext<? extends Node> context)
            {
                doHandleResult(permissionAssessor, nodes, requiredNodes, context);
            }
            
            private void doHandleResult(NodePermissionAssessor permissionAssessor, List<Node> nodes,
                                        int requiredNodes, ResultContext<? extends Node> context)
            {
                if (nodes.size() >= requiredNodes)
                {
                    context.stop();
                    return;
                }
                
                Node node = context.getResultObject();
                addStoreInfo(node);
                
                boolean shouldCache = nodes.size() >= options.getSkipCount();
                if(shouldCache)
                {
                    logger.debug("- selected node "+nodes.size()+": "+node.getUuid()+" "+node.getId());
                    nodesCache.setValue(node.getId(), node);
                }
                else
                {
                    logger.debug("- skipped node "+nodes.size()+": "+node.getUuid()+" "+node.getId());
                }
                
                if (permissionAssessor.isIncluded(node))
                {
                    nodes.add(shouldCache ? node : null);
                }
                
                if (permissionAssessor.shouldQuitChecks())
                {
                    context.stop();
                    return;
                }
            }
        });

        int numberFound = nodes.size();
        nodes.removeAll(Collections.singleton(null));
        
        DBResultSet rs =  createResultSet(options, nodes, numberFound);
        FilteringResultSet frs = new FilteringResultSet(rs, formInclusionMask(nodes));
        frs.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, rs.getResultSetMetaData().getSearchParameters()));
 
        logger.debug("- query is completed, "+nodes.size()+" nodes loaded");
        return frs;
    }

    private DBResultSet createResultSet(QueryOptions options, List<Node> nodes, int numberFound)
    {
        DBResultSet dbResultSet = new DBResultSet(options.getAsSearchParmeters(), nodes, nodeDAO, nodeService, tenantService, Integer.MAX_VALUE);
        dbResultSet.setNumberFound(numberFound);
        return dbResultSet;
    }

    private int computeRequiredNodesCount(QueryOptions options)
    {
        int maxItems = options.getMaxItems();
        if (maxItems == -1 || maxItems == Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }
        
        return maxItems + options.getSkipCount() + 1;
    }

    private BitSet formInclusionMask(List<Node> nodes)
    {
        BitSet inclusionMask = new BitSet(nodes.size());
        for (int i=0; i < nodes.size(); i++)
        {
            inclusionMask.set(i, true);
        }
        return inclusionMask;
    }

    
    private QueryEngineResults asQueryEngineResults(ResultSet paged)
    {
        HashSet<String> key = new HashSet<>();
        key.add("");
        Map<Set<String>, ResultSet> answer = new HashMap<>();
        answer.put(key, paged);

        return new QueryEngineResults(answer);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryEngine#getQueryModelFactory()
     */
    @Override
    public QueryModelFactory getQueryModelFactory()
    {
        return new DBQueryModelFactory();
    }

    /**
     * Injection of nodes cache for clean-up and warm up when required
     * @param cache The node cache to set
     */
    public void setNodesCache(SimpleCache<Serializable, Serializable> cache)
    {
        this.nodesCache = new EntityLookupCache<>(
                cache,
                CACHE_REGION_NODES,
                new ReadonlyLocalCallbackDAO());
    }

    void setNodesCache(EntityLookupCache<Long, Node, NodeRef> nodesCache) 
    {
        this.nodesCache = nodesCache;
    }
    
    private class ReadonlyLocalCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Node, NodeRef>
    {
        @Override
        public Pair<Long, Node> createValue(Node value)
        {
            throw new UnsupportedOperationException("Node creation is done externally: " + value);
        }

        @Override
        public Pair<Long, Node> findByKey(Long nodeId)
        {
            return null;
        }

        @Override
        public NodeRef getValueKey(Node value)
        {
            return value.getNodeRef();
        }
    }

    private void addStoreInfo(Node node)
    {
        StoreEntity storeEntity = node.getStore();
        logger.debug("Adding store info for store id " + storeEntity.getId());
        List<Pair<Long, StoreRef>> stores = nodeDAO.getStores();
        for (Pair<Long, StoreRef> storeRefPair : stores)
        {
            if (storeEntity.getId() == storeRefPair.getFirst())
            {
                StoreRef storeRef = storeRefPair.getSecond();
                storeEntity.setIdentifier(storeRef.getIdentifier());
                storeEntity.setProtocol(storeRef.getProtocol());
                logger.debug("Added store info" + storeEntity.toString());
                break;
            }
        }
    }
}