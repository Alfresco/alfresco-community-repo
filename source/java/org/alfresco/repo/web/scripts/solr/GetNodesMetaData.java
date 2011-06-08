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
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.solr.MetaDataResultsFilter;
import org.alfresco.repo.solr.NodeMetaData;
import org.alfresco.repo.solr.NodeMetaDataParameters;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.repo.solr.SOLRTrackingComponent.NodeMetaDataQueryCallback;
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
public class GetNodesMetaData extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(GetNodesMetaData.class);
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
            if(o.has("includeAssociations"))
            {
                filter.setIncludeAssociations(o.getBoolean("includeAssociations"));
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
                        throw new AlfrescoRuntimeException("Problem converting to Freemarker", e);
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
        private Long nodeId;
        private NodeRef nodeRef;
        private QName nodeType;
        private Long aclId;
        private Map<String, PropertyValue> properties;
        private Set<QName> aspects;
        private List<String> paths;
        private List<ChildAssociationRef> childAssocs;

        public FreemarkerNodeMetaData(SOLRSerializer solrSerializer, NodeMetaData nodeMetaData) throws IOException, JSONException
        {
            setNodeId(nodeMetaData.getNodeId());
            setAclId(nodeMetaData.getAclId());
            setNodeRef(nodeMetaData.getNodeRef());
            setNodeType(nodeMetaData.getNodeType());
            
            // convert Paths to Strings
            List<String> paths = new ArrayList<String>();
            for(Pair<Path, QName> pair : nodeMetaData.getPaths())
            {
                JSONObject o = new JSONObject();
                o.put("path", solrSerializer.serializeValue(String.class, pair.getFirst()));
                o.put("qname", solrSerializer.serializeValue(String.class, pair.getSecond()));
                paths.add(o.toString(3));
            }
            setPaths(paths);

            setChildAssocs(nodeMetaData.getChildAssocs());
            setAspects(nodeMetaData.getAspects());
            Map<QName, Serializable> props = nodeMetaData.getProperties();
            Map<String, PropertyValue> properties = (props != null ? new HashMap<String, PropertyValue>(props.size()) : null);
            for(QName propName : props.keySet())
            {
                Serializable value = props.get(propName);
                properties.put(solrSerializer.serializeValue(String.class, propName),
                        solrSerializer.serialize(propName, value));
            }
            setProperties(properties);
        }
        
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }
        public void setNodeRef(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }
        public List<String> getPaths()
        {
            return paths;
        }
        public void setPaths(List<String> paths)
        {
            this.paths = paths;
        }
        public QName getNodeType()
        {
            return nodeType;
        }
        public void setNodeType(QName nodeType)
        {
            this.nodeType = nodeType;
        }
        public Long getNodeId()
        {
            return nodeId;
        }
        public void setNodeId(Long nodeId)
        {
            this.nodeId = nodeId;
        }
        public Long getAclId()
        {
            return aclId;
        }
        public void setAclId(Long aclId)
        {
            this.aclId = aclId;
        }
        public Map<String, PropertyValue> getProperties()
        {
            return properties;
        }
        public void setProperties(Map<String, PropertyValue> properties)
        {
            this.properties = properties;
        }
        public Set<QName> getAspects()
        {
            return aspects;
        }
        public void setAspects(Set<QName> aspects)
        {
            this.aspects = aspects;
        }
        public List<ChildAssociationRef> getChildAssocs()
        {
            return childAssocs;
        }
        public void setChildAssocs(List<ChildAssociationRef> childAssocs)
        {
            this.childAssocs = childAssocs;
        }
    }

}
