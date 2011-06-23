/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.bean.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
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
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Repository;

// TODO: DownloadServlet - use of request parameter for property name?
// TODO: Anyway to switch content view url link / property value text?

/**
 * Backing bean to support the Admin Node Browser
 */
public class AdminNodeBrowseBean implements Serializable
{
    private static final long serialVersionUID = -8702324672426537379L;

    /** selected query language */
    private String queryLanguage = null;

    /** available query languages */
    private static List<SelectItem> queryLanguages = new ArrayList<SelectItem>();
    static
    {
        queryLanguages.add(new SelectItem("noderef"));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_XPATH));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_LUCENE));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_FTS_ALFRESCO));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_CMIS_STRICT));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_CMIS_ALFRESCO));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO));
        queryLanguages.add(new SelectItem(SearchService.LANGUAGE_SOLR_CMIS));
        queryLanguages.add(new SelectItem("selectnodes"));
    }

    // query and results
    private String query = null;
    private SearchResults searchResults = new SearchResults((List<NodeRef>) null);

    private NodeRef nodeRef = null;
    private QName nodeType = null;
    private Path primaryPath = null;
    private Boolean inheritPermissions = null;

    // stores and node
    transient private DataModel stores = null;
    transient private DataModel parents = null;
    transient private DataModel aspects = null;
    transient private DataModel properties = null;
    transient private DataModel children = null;
    transient private DataModel assocs = null;
    transient private DataModel permissions = null;
    transient private DataModel permissionMasks = null;
    transient private DataModel avmStoreProps = null;

    // supporting repository services
    transient private TransactionService transactionService;
    transient private NodeService nodeService;
    transient private DictionaryService dictionaryService;
    transient private SearchService searchService;
    transient private NamespaceService namespaceService;
    transient private PermissionService permissionService;
    transient private AVMService avmService;

    /**
     * @param transactionService        transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    private TransactionService getTransactionService()
    {
        if (transactionService == null)
        {
            transactionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getTransactionService();
        }
        return transactionService;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    private NodeService getNodeService()
    {
        if (nodeService == null)
        {
            nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
        }
        return nodeService;
    }

    /**
     * @param searchService search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    private SearchService getSearchService()
    {
        if (searchService == null)
        {
            searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
        }
        return searchService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    private DictionaryService getDictionaryService()
    {
        if (dictionaryService == null)
        {
            dictionaryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
        }
        return dictionaryService;
    }

    /**
     * @param namespaceService namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    private NamespaceService getNamespaceService()
    {
        if (namespaceService == null)
        {
            namespaceService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
        }
        return namespaceService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    private PermissionService getPermissionService()
    {
        if (permissionService == null)
        {
            permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
        }
        return permissionService;
    }

    /**
     * @param avmService AVM service
     */
    public void setAVMService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    private AVMService getAVMService()
    {
        if (avmService == null)
        {
            avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
        }
        return avmService;
    }

    /**
     * Gets the list of repository stores
     * 
     * @return stores
     */
    public DataModel getStores()
    {
        if (stores == null)
        {
            List<StoreRef> storeRefs = getNodeService().getStores();
            stores = new ListDataModel(storeRefs);
        }
        return stores;
    }

    /**
     * Gets the selected node reference
     * 
     * @return node reference (defaults to system store root)
     */
    public NodeRef getNodeRef()
    {
        if (nodeRef == null)
        {
            nodeRef = getNodeService().getRootNode(new StoreRef("system", "system"));
        }
        return nodeRef;
    }

    /**
     * Sets the selected node reference
     * 
     * @param nodeRef node reference
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
        inheritPermissions = null;
        permissions = null;
        permissionMasks = null;
    }

    /**
     * Gets the current node type
     * 
     * @return node type
     */
    public QName getNodeType()
    {
        if (nodeType == null)
        {
            nodeType = getNodeService().getType(getNodeRef());
        }
        return nodeType;
    }

    /**
     * Gets the current node primary path
     * 
     * @return primary path
     */
    public String getPrimaryPath()
    {
        if (primaryPath == null)
        {
            primaryPath = getNodeService().getPath(getNodeRef());
        }
        return ISO9075.decode(primaryPath.toString());
    }

    /**
     * Gets the current node primary parent reference
     * 
     * @return primary parent ref
     */
    public NodeRef getPrimaryParent()
    {
        getPrimaryPath();
        Path.Element element = primaryPath.last();
        NodeRef parentRef = ((Path.ChildAssocElement) element).getRef().getParentRef();
        return parentRef;
    }

    /**
     * Gets the current node aspects
     * 
     * @return node aspects
     */
    public DataModel getAspects()
    {
        if (aspects == null)
        {
            List<QName> aspectNames = new ArrayList<QName>(getNodeService().getAspects(getNodeRef()));
            aspects = new ListDataModel(aspectNames);
        }
        return aspects;
    }

    /**
     * Gets the current node parents
     * 
     * @return node parents
     */
    public DataModel getParents()
    {
        if (parents == null)
        {
            List<ChildAssociationRef> parentRefs = getNodeService().getParentAssocs(getNodeRef());
            parents = new ListDataModel(parentRefs);
        }
        return parents;
    }

    /**
     * Gets the current node properties
     * 
     * @return properties
     */
    public DataModel getProperties()
    {
        if (properties == null)
        {
            Map<QName, Serializable> propertyValues = getNodeService().getProperties(getNodeRef());
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
     * Gets whether the current node inherits its permissions from a parent node
     * 
     * @return true => inherits permissions
     */
    public boolean getInheritPermissions()
    {
        if (inheritPermissions == null)
        {
            inheritPermissions = this.getPermissionService().getInheritParentPermissions(nodeRef);
        }
        return inheritPermissions.booleanValue();
    }

    /**
     * Gets the current node permissions
     * 
     * @return the permissions
     */
    public DataModel getPermissions()
    {
        if (permissions == null)
        {
            AccessStatus readPermissions = this.getPermissionService().hasPermission(nodeRef, PermissionService.READ_PERMISSIONS);
            if (readPermissions.equals(AccessStatus.ALLOWED))
            {
                List<AccessPermission> nodePermissions = new ArrayList<AccessPermission>(getPermissionService().getAllSetPermissions(nodeRef));
                permissions = new ListDataModel(nodePermissions);
            }
            else
            {
                List<NoReadPermissionGranted> noReadPermissions = new ArrayList<NoReadPermissionGranted>(1);
                noReadPermissions.add(new NoReadPermissionGranted());
                permissions = new ListDataModel(noReadPermissions);
            }
        }
        return permissions;
    }

    /**
     * Gets the current node permissions
     * 
     * @return the permissions
     */
    public DataModel getStorePermissionMasks()
    {
        if (permissionMasks == null)
        {
            if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                List<AccessPermission> nodePermissions = new ArrayList<AccessPermission>(getPermissionService().getAllSetPermissions(nodeRef.getStoreRef()));
                permissionMasks = new ListDataModel(nodePermissions);
            }
            else
            {
                List<NoStoreMask> noReadPermissions = new ArrayList<NoStoreMask>(1);
                noReadPermissions.add(new NoStoreMask());
                permissionMasks = new ListDataModel(noReadPermissions);
            }
        }
        return permissionMasks;
    }

    /**
     * Gets the current node children
     * 
     * @return node children
     */
    public DataModel getChildren()
    {
        if (children == null)
        {
            List<ChildAssociationRef> assocRefs = getNodeService().getChildAssocs(getNodeRef());
            children = new ListDataModel(assocRefs);
        }
        return children;
    }

    /**
     * Gets the current node associations
     * 
     * @return associations
     */
    public DataModel getAssocs()
    {
        if (assocs == null)
        {
            try
            {
                List<AssociationRef> assocRefs = getNodeService().getTargetAssocs(getNodeRef(), RegexQNamePattern.MATCH_ALL);
                assocs = new ListDataModel(assocRefs);
            }
            catch (UnsupportedOperationException err)
            {
               // some stores do not support associations
            }
        }
        return assocs;
    }

    public boolean getInAVMStore()
    {
        return nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM);
    }

    public DataModel getAVMStoreProperties()
    {
        if (avmStoreProps == null)
        {
            // work out the store name from current nodeRef
            String store = nodeRef.getStoreRef().getIdentifier();
            Map<QName, PropertyValue> props = getAVMService().getStoreProperties(store);
            List<Map<String, String>> storeProperties = new ArrayList<Map<String, String>>();

            for (Map.Entry<QName, PropertyValue> property : props.entrySet())
            {
                Map<String, String> map = new HashMap<String, String>(2);
                map.put("name", property.getKey().toString());
                map.put("type", property.getValue().getActualTypeString());
                String val = property.getValue().getStringValue();
                if (val == null)
                {
                    val = "null";
                }
                map.put("value", val);

                storeProperties.add(map);
            }

            avmStoreProps = new ListDataModel(storeProperties);
        }

        return avmStoreProps;
    }

    /**
     * Gets the current query language
     * 
     * @return query language
     */
    public String getQueryLanguage()
    {
        return queryLanguage;
    }

    /**
     * Sets the current query language
     * 
     * @param queryLanguage query language
     */
    public void setQueryLanguage(String queryLanguage)
    {
        this.queryLanguage = queryLanguage;
    }

    /**
     * Gets the current query
     * 
     * @return query statement
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * Set the current query
     * 
     * @param query query statement
     */
    public void setQuery(String query)
    {
        this.query = query;
    }

    /**
     * Gets the list of available query languages
     * 
     * @return query languages
     */
    public List<SelectItem> getQueryLanguages()
    {
        return queryLanguages;
    }

    /**
     * Gets the current search results
     * 
     * @return search results
     */
    public SearchResults getSearchResults()
    {
        return searchResults;
    }

    /**
     * Action to select a store
     * 
     * @return next action
     */
    public String selectStore()
    {
        StoreRef storeRef = (StoreRef) getStores().getRowData();
        NodeRef rootNode = getNodeService().getRootNode(storeRef);
        setNodeRef(rootNode);

        this.avmStoreProps = null;

        return "success";
    }

    /**
     * Action to select stores list
     * 
     * @return next action
     */
    public String selectStores()
    {
        stores = null;
        return "success";
    }

    /**
     * Action to select primary path
     * 
     * @return next action
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
     * @return next action
     */
    public String selectPrimaryParent()
    {
        setNodeRef(getPrimaryParent());
        return "success";
    }

    /**
     * Action to select parent
     * 
     * @return next action
     */
    public String selectParent()
    {
        ChildAssociationRef assocRef = (ChildAssociationRef) getParents().getRowData();
        NodeRef parentRef = assocRef.getParentRef();
        setNodeRef(parentRef);
        return "success";
    }

    /**
     * Action to select association To node
     * 
     * @return next action
     */
    public String selectToNode()
    {
        AssociationRef assocRef = (AssociationRef) getAssocs().getRowData();
        NodeRef targetRef = assocRef.getTargetRef();
        setNodeRef(targetRef);
        return "success";
    }

    /**
     * Action to select node property
     * 
     * @return next action
     */
    public String selectNodeProperty()
    {
        Property property = (Property) getProperties().getRowData();
        Property.Value value = (Property.Value) property.getValues().getRowData();
        NodeRef nodeRef = (NodeRef) value.getValue();
        setNodeRef(nodeRef);
        return "success";
    }

    /**
     * Action to select child
     * 
     * @return next action
     */
    public String selectChild()
    {
        ChildAssociationRef assocRef = (ChildAssociationRef) getChildren().getRowData();
        NodeRef childRef = assocRef.getChildRef();
        setNodeRef(childRef);
        return "success";
    }

    /**
     * Action to select search result node
     * 
     * @return next action
     */
    public String selectResultNode()
    {
        ChildAssociationRef assocRef = (ChildAssociationRef) searchResults.getRows().getRowData();
        NodeRef childRef = assocRef.getChildRef();
        setNodeRef(childRef);
        return "success";
    }

    /**
     * Action to submit search
     * 
     * @return next action
     */
    public String submitSearch()
    {
        RetryingTransactionCallback<String> searchCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                if (queryLanguage.equals("noderef"))
                {
                    // ensure node exists
                    NodeRef nodeRef = new NodeRef(query);
                    boolean exists = getNodeService().exists(nodeRef);
                    if (!exists)
                    {
                        throw new AlfrescoRuntimeException("Node " + nodeRef + " does not exist.");
                    }
                    setNodeRef(nodeRef);
                    return "node";
                }
                else if (queryLanguage.equals("selectnodes"))
                {
                    List<NodeRef> nodes = getSearchService().selectNodes(getNodeRef(), query, null, getNamespaceService(), false);
                    searchResults = new SearchResults(nodes);
                    return "search";
                }

                // perform search
                searchResults = new SearchResults(getSearchService().query(getNodeRef().getStoreRef(), queryLanguage, query));
                return "search";
            }
        };

        try
        {
            return getTransactionService().getRetryingTransactionHelper().doInTransaction(searchCallback, true);
        }
        catch (Throwable e)
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
         * @param name property name
         * @param value property values
         */
        @SuppressWarnings("unchecked")
        public Property(QName name, Serializable value)
        {
            this.name = name;

            PropertyDefinition propDef = getDictionaryService().getProperty(name);
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
            final List<Value> values;
            if (value instanceof Collection)
            {
                Collection<Serializable> oldValues = (Collection<Serializable>) value;
                values = new ArrayList<Value>(oldValues.size());
                isCollection = true;
                for (Serializable multiValue : oldValues)
                {
                    values.add(new Value(multiValue));
                }
            }
            else
            {
                values = Collections.singletonList(new Value(value));
            }
            this.values = new ListDataModel(values);
        }

        /**
         * Gets the property name
         * 
         * @return name
         */
        public QName getName()
        {
            return name;
        }

        /**
         * Gets the property data type
         * 
         * @return data type
         */
        public String getDataType()
        {
            return datatype;
        }

        /**
         * Gets the property value
         * 
         * @return value
         */
        public DataModel getValues()
        {
            return values;
        }

        /**
         * Determines whether the property is residual
         * 
         * @return true => property is not defined in dictionary
         */
        public String getResidual()
        {
            return residual;
        }

        /**
         * Determines whether the property is of ANY type
         * 
         * @return true => is any
         */
        public boolean isAny()
        {
            return (datatype == null) ? false : datatype.equals(DataTypeDefinition.ANY.toString());
        }

        /**
         * Determines whether the property is a collection
         * 
         * @return true => is collection
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
             * @param value value
             */
            public Value(Serializable value)
            {
                this.value = value;
            }

            /**
             * Gets the value
             * 
             * @return the value
             */
            public Serializable getValue()
            {
                return value;
            }

            /**
             * Gets the value datatype
             * 
             * @return the value datatype
             */
            public String getDataType()
            {
                String datatype = Property.this.getDataType();
                if (datatype == null || datatype.equals(DataTypeDefinition.ANY.toString()))
                {
                    if (value != null)
                    {
                        DataTypeDefinition dataTypeDefinition = getDictionaryService().getDataType(value.getClass());
                        if (dataTypeDefinition != null)
                        {
                            datatype = getDictionaryService().getDataType(value.getClass()).getName().toString();
                        }
                    }
                }
                return datatype;
            }

            /**
             * Gets the download url (for content properties)
             * 
             * @return url
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
             * @return true => is content
             */
            public boolean isContent()
            {
                String datatype = getDataType();
                return (datatype == null) ? false : datatype.equals(DataTypeDefinition.CONTENT.toString());
            }

            /**
             * Determines whether the value is a node ref
             * 
             * @return true => is node ref
             */
            public boolean isNodeRef()
            {
                String datatype = getDataType();
                return (datatype == null) ? false : datatype.equals(DataTypeDefinition.NODE_REF.toString()) || datatype.equals(DataTypeDefinition.CATEGORY.toString());
            }

            /**
             * Determines whether the value is null
             * 
             * @return true => value is null
             */
            public boolean isNullValue()
            {
                return value == null;
            }
        }
    }

    /**
     * Permission representing the fact that "Read Permissions" has not been granted
     */
    public static class NoReadPermissionGranted implements Serializable
    {
        private static final long serialVersionUID = -6256369557521402921L;

        public String getPermission()
        {
            return PermissionService.READ_PERMISSIONS;
        }

        public String getAuthority()
        {
            return "[Current Authority]";
        }

        public String getAccessStatus()
        {
            return "Not Granted";
        }
    }

    public static class NoStoreMask implements Serializable
    {
        private static final long serialVersionUID = -6256369557521402921L;

        public String getPermission()
        {
            return "All <No Mask>";
        }

        public String getAuthority()
        {
            return "All";
        }

        public String getAccessStatus()
        {
            return "Allowed";
        }
    }

    /**
     * Wrapper class for Search Results
     */
    public class SearchResults implements Serializable
    {
        private static final long serialVersionUID = 7402906720039176001L;

        private int length = 0;
        private SerialListDataModel rows;

        /**
         * Construct
         * 
         * @param resultSet query result set
         */
        public SearchResults(ResultSet resultSet)
        {
            rows = new SerialListDataModel();
            if (resultSet != null)
            {
                rows.setWrappedData(resultSet.getChildAssocRefs());
                length = resultSet.length();
                resultSet.close();
            }
        }

        /**
         * Construct
         * 
         * @param resultSet query result set
         */
        public SearchResults(List<NodeRef> resultSet)
        {
            rows = new SerialListDataModel();
            if (resultSet != null)
            {
                List<ChildAssociationRef> assocRefs = new ArrayList<ChildAssociationRef>(resultSet.size());
                for (NodeRef nodeRef : resultSet)
                {
                    ChildAssociationRef childAssocRef = getNodeService().getPrimaryParent(nodeRef);
                    assocRefs.add(childAssocRef);
                }
                rows.setWrappedData(assocRefs);
                length = resultSet.size();
            }
        }

        /**
         * Gets the row count
         * 
         * @return count of rows
         */
        public int getLength()
        {
            return length;
        }

        /**
         * Gets the rows
         * 
         * @return the rows
         */
        public DataModel getRows()
        {
            return rows;
        }

        private class SerialListDataModel extends ListDataModel implements Serializable
        {
            private static final long serialVersionUID = 4154583769762846020L;
        }
    }
}
