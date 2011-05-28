package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.domain.solr.MetaDataResultsFilter;
import org.alfresco.repo.domain.solr.NodeMetaData;
import org.alfresco.repo.domain.solr.NodeMetaDataParameters;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.domain.solr.SOLRDAO.NodeMetaDataQueryCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.SOLRSerializer;
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

// TODO how to hook into bulk loading of nodes + properties + aspects?
// todo url parameter to remove whitespace in results - make it the default?
public class GetNodesMetaData extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(GetNodesMetaData.class);
    private static final int INITIAL_DEFAULT_SIZE = 100;
    private static final int BATCH_SIZE = 50;
    
    private SOLRDAO solrDAO;
    private SOLRSerializer solrSerializer;
    
    /**
     * @param solrDAO          the solrDAO to set
     */
    public void setSolrDAO(SOLRDAO solrDAO)
    {
        this.solrDAO = solrDAO;
    }
    
    public void setSolrSerializer(SOLRSerializer solrSerializer)
    {
        this.solrSerializer = solrSerializer;
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status)
     */
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

            solrDAO.getNodesMetadata(params, filter, new NodeMetaDataQueryCallback()
            {
                private int counter = BATCH_SIZE;
                private int numBatches = 0;

                public boolean handleNodeMetaData(NodeMetaData nodeMetaData)
                {
                    // need to perform data structure conversions that are compatible with Freemarker
                    // e.g. Serializable -> String, QName -> String (because map keys must be string, number)
                    FreemarkerNodeMetaData fNodeMetaData = new FreemarkerNodeMetaData(solrSerializer, nodeMetaData);
                    nodesMetaData.add(fNodeMetaData);

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
    
    public static class FreemarkerNodeMetaData
    {
        private Long nodeId;
        private NodeRef nodeRef;
        private QName nodeType;
        private Long aclId;
        private Map<String, Object> properties;
        private Set<QName> aspects;
        private List<Path> paths;
        private List<ChildAssociationRef> childAssocs;
        
        public FreemarkerNodeMetaData(SOLRSerializer solrSerializer, NodeMetaData nodeMetaData)
        {
            setNodeId(nodeMetaData.getNodeId());
            setAclId(nodeMetaData.getAclId());
            setNodeRef(nodeMetaData.getNodeRef());
            setNodeType(nodeMetaData.getNodeType());
            
            // TODO need to use SOLRTypeConverter to serialize Path
            for(Path path : nodeMetaData.getPaths())
            {
                
            }
            setPaths(nodeMetaData.getPaths());
            setChildAssocs(nodeMetaData.getChildAssocs());
            setAspects(nodeMetaData.getAspects());
            Map<QName, Serializable> props = nodeMetaData.getProperties();
            Map<String, Object> properties = (props != null ? new HashMap<String, Object>(props.size()) : null);
            for(QName propName : props.keySet())
            {
                Serializable value = props.get(propName);

                if(value instanceof ContentDataWithId)
                {
                    // special case - ContentDataWithId
                    properties.put(propName.toString(), ((ContentDataWithId)value).getId());
                }
                else
                {
                    properties.put(propName.toString(), solrSerializer.serialize(propName, value));
                }
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
        public List<Path> getPaths()
        {
            return paths;
        }
        public void setPaths(List<Path> paths)
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
        public Map<String, Object> getProperties()
        {
            return properties;
        }
        public void setProperties(Map<String, Object> properties)
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
