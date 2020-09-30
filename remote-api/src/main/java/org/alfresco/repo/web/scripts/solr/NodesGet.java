/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.SearchTrackingComponent;
import org.alfresco.repo.search.SearchTrackingComponent.NodeQueryCallback;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR: Get a list of nodes in the given transactions.
 * <p/>
 * Supports fromNodeId, toNodeId, count (all optional) to control the number of nodes returned<br/>
 * e.g. (null, null, 1000) will return at most 1000 nodes starting from the first node in the first transaction.<br/>
 * e.g. (1234, null, 1000) will return at most 1000 nodes starting from the node id 1234.<br/>
 * 
 * @since 4.0
 */
public class NodesGet extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(NodesGet.class);
    
    private SearchTrackingComponent searchTrackingComponent;
    
    private TenantService tenantService;
    
    private QNameDAO qnameDAO;
    
    public void setSearchTrackingComponent(SearchTrackingComponent searchTrackingComponent)
    {
        this.searchTrackingComponent = searchTrackingComponent;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }       
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        try
        {
            Content content = req.getContent();
            if(content == null)
            {
                throw new WebScriptException("Failed to convert request to String");
            }
            JSONObject o = new JSONObject(content.getContent());

            JSONArray aTxnIds = o.has("txnIds") ? o.getJSONArray("txnIds") : null;
            Long fromTxnId = o.has("fromTxnId") ? o.getLong("fromTxnId") : null;
            Long toTxnId = o.has("toTxnId") ? o.getLong("toTxnId") : null;

            Long fromNodeId = o.has("fromNodeId") ? o.getLong("fromNodeId") : null;
            Long toNodeId = o.has("toNodeId") ? o.getLong("toNodeId") : null;
            
            Set<QName> excludeAspects = null;
            if(o.has("excludeAspects"))
            {
                JSONArray aExcludeAspects = o.getJSONArray("excludeAspects");
                excludeAspects = new HashSet<QName>(aExcludeAspects.length());
                for(int i = 0; i < aExcludeAspects.length(); i++)
                {
                    excludeAspects.add(QName.createQName(aExcludeAspects.getString(i).trim()));
                }
            }

            Set<QName> includeAspects = null;
            if(o.has("includeAspects"))
            {
                JSONArray aIncludeAspects = o.getJSONArray("includeAspects");
                includeAspects = new HashSet<QName>(aIncludeAspects.length());
                for(int i = 0; i < aIncludeAspects.length(); i++)
                {
                    includeAspects.add(QName.createQName(aIncludeAspects.getString(i).trim()));
                }
            }
            
            Set<QName> excludeNodeTypes = null;
            if(o.has("excludeNodeTypes"))
            {
                JSONArray aExcludeNodeTypes = o.getJSONArray("excludeNodeTypes");
                excludeNodeTypes = new HashSet<QName>(aExcludeNodeTypes.length());
                for(int i = 0; i < aExcludeNodeTypes.length(); i++)
                {
                    excludeNodeTypes.add(QName.createQName(aExcludeNodeTypes.getString(i).trim()));
                }
            }

            Set<QName> includeNodeTypes = null;
            if(o.has("includeNodeTypes"))
            {
                JSONArray aIncludeNodeTypes = o.getJSONArray("includeNodeTypes");
                includeNodeTypes = new HashSet<QName>(aIncludeNodeTypes.length());
                for(int i = 0; i < aIncludeNodeTypes.length(); i++)
                {
                    includeNodeTypes.add(QName.createQName(aIncludeNodeTypes.getString(i).trim()));
                }
            }
            
            // 0 or Integer.MAX_VALUE => ignore
            int maxResults = o.has("maxResults") ? o.getInt("maxResults") : 0;
            
            String storeProtocol = o.has("storeProtocol") ? o.getString("storeProtocol") : null;
            String storeIdentifier = o.has("storeIdentifier") ? o.getString("storeIdentifier") : null;
            String coreName = o.has("coreName") ? o.getString("coreName") : null;
            
            List<Long> txnIds = null;
            if(aTxnIds != null)
            {
                txnIds = new ArrayList<Long>(aTxnIds.length());
                for(int i = 0; i < aTxnIds.length(); i++)
                {
                    txnIds.add(aTxnIds.getLong(i));
                }
            }
            
            String shardProperty = o.has("shardProperty") ? o.getString("shardProperty") : null;
            
            NodeParameters nodeParameters = new NodeParameters();
            nodeParameters.setTransactionIds(txnIds);
            nodeParameters.setFromTxnId(fromTxnId);
            nodeParameters.setToTxnId(toTxnId);
            nodeParameters.setFromNodeId(fromNodeId);
            nodeParameters.setToNodeId(toNodeId);
            nodeParameters.setExcludeAspects(excludeAspects);
            nodeParameters.setIncludeAspects(includeAspects);
            nodeParameters.setExcludeNodeTypes(excludeNodeTypes);
            nodeParameters.setIncludeNodeTypes(includeNodeTypes);
            nodeParameters.setShardProperty(shardProperty);
            nodeParameters.setCoreName(coreName);

            
            StoreRef storeRef = null;
            
            if (AuthenticationUtil.isMtEnabled())
            {
                // MT - use Java filter (post query) and then add tenant context for each node
                storeRef = new StoreRef(storeProtocol, storeIdentifier);
            }
            else
            {
                // non-MT - use DB filter (in query)
                nodeParameters.setStoreProtocol(storeProtocol);
                nodeParameters.setStoreIdentifier(storeIdentifier);
            }
            
            nodeParameters.setMaxResults(maxResults);
            
            WebNodeQueryCallback nodeQueryCallback = new WebNodeQueryCallback(maxResults, storeRef, tenantService, qnameDAO);

            searchTrackingComponent.getNodes(nodeParameters, nodeQueryCallback);
            
            Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
            List<NodeRecord> nodes = nodeQueryCallback.getNodes();
            model.put("nodes", nodes);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
            }
            
            return model;
        }
        catch(IOException e)
        {
            throw new WebScriptException("IO exception parsing request", e);
        }
        catch(JSONException e)
        {
            throw new WebScriptException("Invalid JSON", e);
        }
    }

    public static class NodeRecord
    {
        private final Long id;
        private final Long txnId;
        private final boolean isDeleted;
        private final String nodeRef;
        private final String tenant;
        private final Long aclId; 
        private final String shardPropertyValue;
        private final Integer explicitShardId;

        public NodeRecord(Node node, QNameDAO qnameDAO, TenantService tenantService)
        {
            this.id = node.getId();
            this.txnId = node.getTransaction().getId();
            this.isDeleted = node.getNodeStatus(qnameDAO).isDeleted();
            this.nodeRef = node.getNodeRef().toString();
            this.tenant = tenantService.getDomain(node.getNodeRef().getStoreRef().getIdentifier());
            this.aclId = node.getAclId();
            this.shardPropertyValue = node.getShardKey();
            this.explicitShardId = node.getExplicitShardId();
        }

        public Long getId()
        {
            return id;
        }

        public Long getTxnId()
        {
            return txnId;
        }

        public boolean isDeleted()
        {
            return isDeleted;
        }

        public String getNodeRef()
        {
            return nodeRef;
        }

        public String getTenant()
        {
            return tenant;
        }

        public Long getAclId()
        {
            return aclId;
        }

        public String getShardPropertyValue()
        {
            return this.shardPropertyValue;
        }

        public Integer getExplicitShardId()
        {
            return this.explicitShardId;
        }
    }

    /**
     * Callback for DAO get nodes query
     */
    private class WebNodeQueryCallback implements NodeQueryCallback
    {
        private ArrayList<NodeRecord> nodes;
        
        private StoreRef storeRef;
        
        private TenantService tenantService;
        
        private QNameDAO qnameDAO;
        
        public WebNodeQueryCallback(int count, StoreRef storeRef, TenantService tenantService, QNameDAO qnameDAO)
        {
            super();
            
            this.storeRef = storeRef;
            this.tenantService = tenantService;
            this.qnameDAO = qnameDAO;
           
            nodes = new ArrayList<NodeRecord>(count == 0 || count == Integer.MAX_VALUE ? 100 : count);
        }
        
        @Override
        public boolean handleNode(Node node)
        {
            if (storeRef != null)
            {
                // MT - since storeRef is not null, filter by store here
                StoreRef tenantStoreRef = node.getStore().getStoreRef();
                StoreRef baseStoreRef = new StoreRef(tenantStoreRef.getProtocol(), tenantService.getBaseName(tenantStoreRef.getIdentifier(), true));
                if (storeRef.equals(baseStoreRef))
                {
                    nodes.add(new NodeRecord(node, qnameDAO, tenantService));
                }
            }
            else
            {
                nodes.add(new NodeRecord(node, qnameDAO, tenantService));
            }
            
            // continue - get next node
            return true;
        }
        
        public List<NodeRecord> getNodes()
        {
            return nodes;
        }
    }
}