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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.solr.MetaDataResultsFilter;
import org.alfresco.repo.solr.NodeMetaData;
import org.alfresco.repo.solr.NodeMetaDataParameters;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.repo.solr.SOLRTrackingComponent.NodeMetaDataQueryCallback;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
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

// todo url parameter to remove whitespace in results - make it the default?
/**
 * Support for SOLR: Get metadata for nodes given IDs, ranges of IDs, etc.
 * <p/>
 * 
 * @since 4.0
 */
public class NodesMetaDataGet extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(NodesMetaDataGet.class);
    private static final int INITIAL_DEFAULT_SIZE = 100;
    private static final int BATCH_SIZE = 50;
    
    private SOLRTrackingComponent solrTrackingComponent;
    private SOLRSerializer solrSerializer;
    
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    public void setSolrSerializer(SOLRSerializer solrSerializer)
    {
        this.solrSerializer = solrSerializer;
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
            
            List<Long> nodeIds = null;
            if(o.has("nodeIds"))
            {
                JSONArray jsonNodeIds =  o.getJSONArray("nodeIds");
                nodeIds = new ArrayList<Long>(jsonNodeIds.length());
                for(int i = 0; i < jsonNodeIds.length(); i++)
                {
                    Long nodeId = jsonNodeIds.getLong(i);
                    nodeIds.add(nodeId);
                }
            }
            
            Long fromNodeId = o.has("fromNodeId") ? o.getLong("fromNodeId") : null;
            Long toNodeId = o.has("toNodeId") ? o.getLong("toNodeId") : null;
            
            // 0 or Integer.MAX_VALUE => ignore
            int maxResults = o.has("maxResults") ? o.getInt("maxResults") : 0;

            int size = 0;
            if(maxResults != 0 && maxResults != Integer.MAX_VALUE)
            {
                size = maxResults;
            }
            else if(nodeIds != null)
            {
                size = nodeIds.size();
            }
            else if(fromNodeId != null && toNodeId != null)
            {
                if((toNodeId.longValue() - fromNodeId.longValue()) > Integer.MAX_VALUE)
                {
                    throw new WebScriptException("Too many nodes expected, try changing the criteria");
                }
                size = (int)(toNodeId - fromNodeId);
            }

            final boolean noSizeCalculated = (size == 0);

            // filters, defaults are 'true'
            MetaDataResultsFilter filter = new MetaDataResultsFilter();
            if(o.has("includeAclId"))
            {
                filter.setIncludeAclId(o.getBoolean("includeAclId"));
            }
            if(o.has("includeAspects"))
            {
                filter.setIncludeAspects(o.getBoolean("includeAspects"));
            }
            if(o.has("includeNodeRef"))
            {
                filter.setIncludeNodeRef(o.getBoolean("includeNodeRef"));
            }
            if(o.has("includeOwner"))
            {
                filter.setIncludeOwner(o.getBoolean("includeOwner"));
            }
            if(o.has("includeProperties"))
            {
                filter.setIncludeProperties(o.getBoolean("includeProperties"));
            }
            if(o.has("includePaths"))
            {
                filter.setIncludePaths(o.getBoolean("includePaths"));
            }
            if(o.has("includeType"))
            {
                filter.setIncludeType(o.getBoolean("includeType"));
            }
            if(o.has("includeParentAssociations"))
            {
                filter.setIncludeParentAssociations(o.getBoolean("includeParentAssociations"));
            }
            if(o.has("includeChildIds"))
            {
                filter.setIncludeChildIds(o.getBoolean("includeChildIds"));
            }
            if(o.has("includeTxnId"))
            {
                filter.setIncludeTxnId(o.getBoolean("includeTxnId"));
            }
            
            final ArrayList<FreemarkerNodeMetaData> nodesMetaData = 
                new ArrayList<FreemarkerNodeMetaData>(size > 0 ? size : INITIAL_DEFAULT_SIZE);
            NodeMetaDataParameters params = new NodeMetaDataParameters();
            params.setNodeIds(nodeIds);
            params.setFromNodeId(fromNodeId);
            params.setToNodeId(toNodeId);
            params.setMaxResults(maxResults);

            solrTrackingComponent.getNodesMetadata(params, filter, new NodeMetaDataQueryCallback()
            {
                private int counter = BATCH_SIZE;
                private int numBatches = 0;

                @Override
                public boolean handleNodeMetaData(NodeMetaData nodeMetaData)
                {
                    // need to perform data structure conversions that are compatible with Freemarker
                    // e.g. Serializable -> String, QName -> String (because map keys must be string, number)
                    try
                    {
                        FreemarkerNodeMetaData fNodeMetaData = new FreemarkerNodeMetaData(solrSerializer, nodeMetaData);
                        nodesMetaData.add(fNodeMetaData);
                    }
                    catch(Exception e)
                    {
                        throw new AlfrescoRuntimeException("Problem converting to Freemarker using node " + nodeMetaData.getNodeRef().toString(), e); 
                    }

                    if(noSizeCalculated && --counter == 0)
                    {
                        counter = BATCH_SIZE;
                        nodesMetaData.ensureCapacity(++numBatches*BATCH_SIZE);
                    }

                    return true;
                }
            });

            Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
            model.put("nodes", nodesMetaData);
            model.put("filter", filter);

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

    /**
     * Bean to store node meta data for use by FreeMarker templates
     * 
     * @since 4.0
     */
    public static class FreemarkerNodeMetaData
    {
        private final Long nodeId;
        private final NodeRef nodeRef;
        private final QName nodeType;
        private final Long aclId;
        private final Map<String, PropertyValue> properties;
        private final Set<QName> aspects;
        private final List<String> paths;
        private final List<String> namePaths;
        private final List<String> childAssocs;
        private final List<String> parentAssocs;
        private final Long parentAssocsCrc;
        private final List<Long> childIds;
        private final String owner;
        private final Long txnId;
        private final Set<String> ancestors;
        private final String tenantDomain;
        
        public FreemarkerNodeMetaData(final SOLRSerializer solrSerializer, final NodeMetaData nodeMetaData)
        		throws IOException, JSONException
        {
            this.nodeId = nodeMetaData.getNodeId();
            this.tenantDomain = nodeMetaData.getTenantDomain();
            this.aclId = nodeMetaData.getAclId();
            this.nodeRef = nodeMetaData.getNodeRef();
            this.nodeType = nodeMetaData.getNodeType();
            this.txnId = nodeMetaData.getTxnId();
            this.parentAssocs = new ArrayList<>();
            this.childAssocs = new ArrayList<>();

            // convert Paths to Strings
            List<String> paths = new ArrayList<String>();
            List<String> ancestorPaths = new ArrayList<String>();
            HashSet<String> ancestors = new HashSet<String>();
            if(nodeMetaData.getPaths() != null)
            {
                for(Pair<Path, QName> pair : nodeMetaData.getPaths())
                {
                	StringBuilder ancestorPath = new StringBuilder();
                    JSONObject o = new JSONObject();
                    o.put("path", solrSerializer.serializeValue(String.class, pair.getFirst()));
                    o.put("qname", solrSerializer.serializeValue(String.class, pair.getSecond()));
                   
                    
                    for (NodeRef ancestor : getAncestors(pair.getFirst()))
                    {
                        ancestors.add(ancestor.toString());
                        ancestorPath.insert(0, ancestor.getId()).insert(0, "/");
                    }
                   
                    o.put("apath",  ancestorPath);
                    paths.add(o.toString(3));
                }
            }
            this.ancestors = ancestors;
            this.paths = paths;

            
            // convert name Paths to Strings
            List<String> namePaths = new ArrayList<String>();
            if(nodeMetaData.getNamePaths() != null)
            {
                for(Collection<String> namePath : nodeMetaData.getNamePaths())
                {
                    JSONObject o = new JSONObject();
                    JSONArray array = new JSONArray();
                    for(String element : namePath)
                    {
                        array.put(solrSerializer.serializeValue(String.class, element));
                    }
                    o.put("namePath", array);
                    namePaths.add(o.toString(3));
                }
            }
            this.namePaths = namePaths;

            this.owner = nodeMetaData.getOwner();
            this.childIds = nodeMetaData.getChildIds();
            this.parentAssocsCrc = nodeMetaData.getParentAssocsCrc();
            this.aspects = nodeMetaData.getAspects();


            if (nodeMetaData.getParentAssocs() != null)
            {
                for( ChildAssociationRef assRef : nodeMetaData.getParentAssocs())
                {
                    parentAssocs.add("\"" + solrSerializer.serializeToJSONString(assRef) + "\"");
                }
            }

            if (nodeMetaData.getChildAssocs() != null)
            {
                for( ChildAssociationRef assRef : nodeMetaData.getChildAssocs() )
                {
                    childAssocs.add("\"" + solrSerializer.serializeToJSONString(assRef) + "\"");
                }
            }

            final Map<QName, Serializable> props = nodeMetaData.getProperties();
            if (props != null)
            {
                final Map<String, PropertyValue> properties = new HashMap<String, PropertyValue>(props.size());
                for (final QName propName : props.keySet())
                {
                	// need to run this in tenant context because types may be in a tenant-specific
                	// dictionary registry
                    TenantUtil.runAsTenant(new TenantRunAsWork<Void>()
                    {
                    	@Override
                    	public Void doWork() throws Exception 
                    	{
                    		Serializable value = props.get(propName);
                    		properties.put(solrSerializer.serializeValue(String.class, propName),
                    				solrSerializer.serialize(propName, value));
                    		return null;
                    	}
                    }, tenantDomain);
                }
                this.properties = properties;
            }
            else
            {
                this.properties = null;
            }
        }
        
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }
        public List<String> getPaths()
        {
            return paths;
        }
        public List<String> getNamePaths()
        {
            return namePaths;
        }
        public QName getNodeType()
        {
            return nodeType;
        }
        public Long getNodeId()
        {
            return nodeId;
        }
        public Long getAclId()
        {
            return aclId;
        }
        public Map<String, PropertyValue> getProperties()
        {
            return properties;
        }
        public Set<QName> getAspects()
        {
            return aspects;
        }
        public List<String> getChildAssocs()
        {
            return childAssocs;
        }
        public List<String> getParentAssocs()
        {
            return parentAssocs;
        }
        public Long getParentAssocsCrc()
        {
            return parentAssocsCrc;
        }
        public Set<String> getAncestors()
        {
            return ancestors;
        }
        public List<Long> getChildIds()
        {
            return childIds;
        }
        public String getOwner()
        {
            return owner;
        }
        public Long getTxnId()
        {
            return txnId;
        }
        public String getTenantDomain()
        {
            return tenantDomain;
        }
        private ArrayList<NodeRef> getAncestors(Path path)
        {
            ArrayList<NodeRef> ancestors = new ArrayList<NodeRef>(8);
            for (Iterator<Path.Element> elit = path.iterator(); elit.hasNext(); /**/)
            {
                Path.Element element = elit.next();
                if (!(element instanceof Path.ChildAssocElement))
                {
                    throw new IndexerException("Confused path: " + path);
                }
                Path.ChildAssocElement cae = (Path.ChildAssocElement) element;
                NodeRef parentRef = cae.getRef().getParentRef();
                if(parentRef != null)
                {
                    ancestors.add(0, parentRef);
                }

            }
            return ancestors;
        }
    }

}
