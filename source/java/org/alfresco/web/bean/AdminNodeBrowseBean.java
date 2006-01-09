/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.servlet.DownloadContentServlet;


// TODO: DownloadServlet - use of request parameter for property name?
// TODO: Anyway to switch content view url link / property value text?


/**
 * Backing bean to support the Admin Node Browser
 */
public class AdminNodeBrowseBean
{
    /** selected query language */
    private String queryLanguage = null;

    /** available query languages */
    private static List<SelectItem> queryLanguages = new ArrayList<SelectItem>();
    static
    {
        queryLanguages.add(new SelectItem("noderef"));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_XPATH));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_LUCENE));
        queryLanguages.add(new SelectItem("selectnodes"));
    }

    // query and results
    private String query = null;
    private SearchResults searchResults = new SearchResults((List<NodeRef>)null);
    
    // stores and node
    private DataModel stores = null;
    private NodeRef nodeRef = null;
    private QName nodeType = null;
    private Path primaryPath = null;
    private DataModel parents = null;
    private DataModel aspects = null;
    private DataModel properties = null;
    private DataModel children = null;
    private DataModel assocs = null;
    
    // supporting repository services
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    
    /**
     * @param nodeService  node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService  search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * @param dictionaryService   dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param namespaceService   namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Gets the list of repository stores
     * 
     * @return  stores
     */
    public DataModel getStores()
    {
        if (stores == null)
        {
            List<StoreRef> storeRefs = nodeService.getStores();
            stores = new ListDataModel(storeRefs);
        }
        return stores;
    }
    
    /**
     * Gets the selected node reference
     * 
     * @return  node reference  (defaults to system store root)
     */
    public NodeRef getNodeRef()
    {
        if (nodeRef == null)
        {
            nodeRef = nodeService.getRootNode(new StoreRef("system", "system"));
        }
        return nodeRef;
    }

    /**
     * Sets the selected node reference
     * 
     * @param nodeRef  node reference
     */
    private void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;

        // clear cache
        primaryPath = null;
        nodeType = null;
        parents = null;
        aspects = null;
        properties = null;        
        children = null;
        assocs = null;
    }
    
    /**
     * Gets the current node type
     * 
     * @return  node type
     */
    public QName getNodeType()
    {
        if (nodeType == null)
        {
            nodeType = nodeService.getType(getNodeRef());
        }
        return nodeType;
    }
    
    /**
     * Gets the current node primary path
     * 
     * @return  primary path
     */
    public String getPrimaryPath()
    {
        if (primaryPath == null)
        {
            primaryPath = nodeService.getPath(getNodeRef());
        }
        return ISO9075.decode(primaryPath.toString());
    }

    /**
     * Gets the current node primary parent reference
     * 
     * @return  primary parent ref
     */
    public NodeRef getPrimaryParent()
    {
        getPrimaryPath();
        Path.Element element = primaryPath.last();
        NodeRef parentRef = ((Path.ChildAssocElement)element).getRef().getParentRef();
        return parentRef;
    }

    /**
     * Gets the current node aspects
     * 
     * @return  node aspects
     */
    public DataModel getAspects()
    {
        if (aspects == null)
        {
            List<QName> aspectNames = new ArrayList<QName>(nodeService.getAspects(getNodeRef()));
            aspects = new ListDataModel(aspectNames);
        }
        return aspects;
    }
        
    /**
     * Gets the current node parents
     * 
     * @return  node parents
     */
    public DataModel getParents()
    {
        if (parents == null)
        {
            List<ChildAssociationRef> parentRefs = nodeService.getParentAssocs(getNodeRef());
            parents = new ListDataModel(parentRefs);
        }
        return parents;        
    }
    
    /**
     * Gets the current node properties
     * 
     * @return  properties
     */
    public DataModel getProperties()
    {
        if (properties == null)
        {
            Map<QName, Serializable> propertyValues = nodeService.getProperties(getNodeRef());
            List<Property> nodeProperties = new ArrayList<Property>(propertyValues.size());
            for (Map.Entry<QName, Serializable> property : propertyValues.entrySet())
            {
                nodeProperties.add(new Property(property.getKey(), property.getValue()));
            }
            properties = new ListDataModel(nodeProperties);
        }
        return properties;
    }
    
    /**
     * Gets the current node children
     * 
     * @return  node children
     */
    public DataModel getChildren()
    {
        if (children == null)
        {
            List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(getNodeRef());
            children = new ListDataModel(assocRefs);
        }
        return children;
    }

    /**
     * Gets the current node associations
     * 
     * @return  associations
     */
    public DataModel getAssocs()
    {
        if (assocs == null)
        {
            List<AssociationRef> assocRefs = nodeService.getTargetAssocs(getNodeRef(), RegexQNamePattern.MATCH_ALL);
            assocs = new ListDataModel(assocRefs);
        }
        return assocs;
    }

    /**
     * Gets the current query language
     * 
     * @return  query language 
     */
    public String getQueryLanguage()
    {
        return queryLanguage;
    }

    /**
     * Sets the current query language
     * 
     * @param queryLanguage  query language
     */
    public void setQueryLanguage(String queryLanguage)
    {
        this.queryLanguage = queryLanguage;
    }
    
    /**
     * Gets the current query
     * 
     * @return  query statement
     */
    public String getQuery()
    {
        return query;
    }
    
    /**
     * Set the current query
     * 
     * @param query   query statement
     */
    public void setQuery(String query)
    {
        this.query = query;
    }
    
    /**
     * Gets the list of available query languages
     * 
     * @return  query languages
     */
    public List getQueryLanguages()
    {
        return queryLanguages;
    }

    /**
     * Gets the current search results
     * 
     * @return  search results
     */
    public SearchResults getSearchResults()
    {
        return searchResults;
    }
    
    /**
     * Action to select a store
     * 
     * @return  next action
     */
    public String selectStore()
    {
        StoreRef storeRef = (StoreRef)stores.getRowData();
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        setNodeRef(rootNode);
        return "success";
    }

    /**
     * Action to select stores list
     * 
     * @return  next action
     */
    public String selectStores()
    {
        stores = null;
        return "success";
    }

    /**
     * Action to select primary path
     * 
     * @return  next action
     */
    public String selectPrimaryPath()
    {
        // force refresh of self
        setNodeRef(nodeRef);
        return "success";
    }
    
    /**
     * Action to select primary parent
     * 
     * @return  next action
     */
    public String selectPrimaryParent()
    {
        setNodeRef(getPrimaryParent());
        return "success";
    }
    
    /**
     * Action to select parent
     * 
     * @return  next action
     */
    public String selectParent()
    {
        ChildAssociationRef assocRef = (ChildAssociationRef)parents.getRowData();
        NodeRef parentRef = assocRef.getParentRef();
        setNodeRef(parentRef);
        return "success";
    }

    /**
     * Action to select association To node
     * 
     * @return  next action
     */
    public String selectToNode()
    {
        AssociationRef assocRef = (AssociationRef)assocs.getRowData();
        NodeRef targetRef = assocRef.getTargetRef();
        setNodeRef(targetRef);
        return "success";
    }

    /**
     * Action to select node property
     * 
     * @return  next action
     */
    public String selectNodeProperty()
    {
        Property property = (Property)properties.getRowData();
        Property.Value value = (Property.Value)property.getValues().getRowData();
        NodeRef nodeRef = (NodeRef)value.getValue();
        setNodeRef(nodeRef);
        return "success";
    }
    
    /**
     * Action to select child
     * 
     * @return  next action
     */
    public String selectChild()
    {
        ChildAssociationRef assocRef = (ChildAssociationRef)children.getRowData();
        NodeRef childRef = assocRef.getChildRef();
        setNodeRef(childRef);
        return "success";
    }
    
    /**
     * Action to select search result node
     * 
     * @return  next action
     */
    public String selectResultNode()
    {
        ChildAssociationRef assocRef = (ChildAssociationRef)searchResults.getRows().getRowData();
        NodeRef childRef = assocRef.getChildRef();
        setNodeRef(childRef);
        return "success";
    }

    /**
     * Action to submit search
     * 
     * @return  next action
     */
    public String submitSearch()
    {
        try
        {
            if (queryLanguage.equals("noderef"))
            {
                // ensure node exists
                NodeRef nodeRef = new NodeRef(query);
                boolean exists = nodeService.exists(nodeRef);
                if (!exists)
                {
                    throw new AlfrescoRuntimeException("Node " + nodeRef + " does not exist.");
                }
                setNodeRef(nodeRef);
                return "node";
            }
            else if (queryLanguage.equals("selectnodes"))
            {
                List<NodeRef> nodes = searchService.selectNodes(getNodeRef(), query, null, namespaceService, false);
                searchResults = new SearchResults(nodes);
                return "search";
            }
            
            // perform search
            searchResults = new SearchResults(searchService.query(getNodeRef().getStoreRef(), queryLanguage, query));
            return "search";
        }
        catch(Throwable e)
        {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setDetail("Search failed due to: " + e.toString());
            context.addMessage("searchForm:query", message);
            return "error";
        }
    }
    
    /**
     * Property wrapper class
     */
    public class Property
    {
        private QName name;
        private boolean isCollection = false;
        private DataModel values;
        private String datatype;
        private String residual;
    
        /**
         * Construct
         * 
         * @param name  property name
         * @param value  property values
         */
        public Property(QName name, Serializable value)
        {
            this.name = name;
            
            PropertyDefinition propDef = dictionaryService.getProperty(name);
            if (propDef != null)
            {
                datatype = propDef.getDataType().getName().toString();
                residual = "false";
            }
            else
            {
                residual = "true";
            }
            
            // handle multi/single values
            // TODO: perhaps this is not the most efficient way - lots of list creations
            List<Value> values = new ArrayList<Value>();
            if (value instanceof Collection)
            {
                isCollection = true;
                for (Serializable multiValue : (Collection<Serializable>)value)
                {
                    values.add(new Value(multiValue));
                }
            }
            else
            {
                values.add(new Value(value));
            }
            this.values = new ListDataModel(values);
        }
        
        /**
         * Gets the property name
         * 
         * @return  name
         */
        public QName getName()
        {
            return name;
        }
        
        /**
         * Gets the property data type
         * 
         * @return  data type
         */
        public String getDataType()
        {
            return datatype;
        }
        
        /**
         * Gets the property value
         * 
         * @return  value
         */
        public DataModel getValues()
        {
            return values;
        }

        /**
         * Determines whether the property is residual
         * 
         * @return  true => property is not defined in dictionary
         */
        public String getResidual()
        {
            return residual;
        }
        
        /**
         * Determines whether the property is of ANY type
         * 
         * @return  true => is any
         */
        public boolean isAny()
        {
            return (datatype == null) ? false : datatype.equals(DataTypeDefinition.ANY.toString());
        }
        
        /**
         * Determines whether the property is a collection
         * 
         * @return  true => is collection
         */
        public boolean isCollection()
        {
            return isCollection;
        }
        
        
        /**
         * Value wrapper
         */
        public class Value
        {
            private Serializable value;

            /**
             * Construct
             * 
             * @param value  value
             */
            public Value(Serializable value)
            {
                this.value = value;
            }
            
            /**
             * Gets the value
             * 
             * @return  the value
             */
            public Serializable getValue()
            {
                return value;
            }
            
            /**
             * Gets the value datatype
             * 
             * @return  the value datatype
             */
            public String getDataType()
            {
                String datatype = Property.this.getDataType();
                if (datatype == null || datatype.equals(DataTypeDefinition.ANY.toString()))
                {
                    if (value != null)
                    {
                        datatype = dictionaryService.getDataType(value.getClass()).getName().toString();
                    }
                }
                return datatype;
            }
            
            /**
             * Gets the download url (for content properties)
             * 
             * @return  url
             */
            public String getUrl()
            {
                String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
                url += DownloadContentServlet.generateBrowserURL(nodeRef, "file.bin");
                url += "?property=" + name;
                return url;
            }
            
            /**
             * Determines whether the value is content
             * 
             * @return  true => is content
             */
            public boolean isContent()
            {
                String datatype = getDataType();
                return (datatype == null) ? false : datatype.equals(DataTypeDefinition.CONTENT.toString());
            }
            
            /**
             * Determines whether the value is a node ref
             * 
             * @return  true => is node ref
             */
            public boolean isNodeRef()
            {
                String datatype = getDataType();
                return (datatype == null) ? false : datatype.equals(DataTypeDefinition.NODE_REF.toString()) || datatype.equals(DataTypeDefinition.CATEGORY.toString());
            }
            
            /**
             * Determines whether the value is null
             * 
             * @return  true => value is null
             */
            public boolean isNullValue()
            {
                return value == null;
            }
        }
    }

    /**
     * Wrapper class for Search Results
     */
    public class SearchResults
    {
        private int length = 0;
        private DataModel rows;

        /**
         * Construct
         * 
         * @param resultSet  query result set
         */
        public SearchResults(ResultSet resultSet)
        {
            rows = new ListDataModel();
            if (resultSet != null)
            {
                rows.setWrappedData(resultSet.getChildAssocRefs());
                length = resultSet.length();
            }
        }
        
        /**
         * Construct
         * 
         * @param resultSet  query result set
         */
        public SearchResults(List<NodeRef> resultSet)
        {
            rows = new ListDataModel();
            if (resultSet != null)
            {
                List<ChildAssociationRef> assocRefs = new ArrayList<ChildAssociationRef>(resultSet.size());
                for (NodeRef nodeRef : resultSet)
                {
                    ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);
                    assocRefs.add(childAssocRef);
                }
                rows.setWrappedData(assocRefs);
                length = resultSet.size();
            }
        }

        /**
         * Gets the row count
         * 
         * @return  count of rows
         */
        public int getLength()
        {
            return length;
        }

        /**
         * Gets the rows
         * 
         * @return  the rows
         */
        public DataModel getRows()
        {
            return rows;
        }
    }
    
}
