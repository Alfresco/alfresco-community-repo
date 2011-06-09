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
package org.alfresco.cmis.mapping;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.cmis.CMISConstraintException;
import org.alfresco.cmis.CMISContentAlreadyExistsException;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISFilterNotValidException;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISNotSupportedException;
import org.alfresco.cmis.CMISObjectNotFoundException;
import org.alfresco.cmis.CMISPermissionDeniedException;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISRelationshipDirectionEnum;
import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.cmis.CMISRuntimeException;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISStreamNotSupportedException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.cmis.CMISVersioningException;
import org.alfresco.cmis.CMISVersioningStateEnum;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.cmis.dictionary.CMISFolderTypeDefinition;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * CMIS Services Implementation.
 * 
 * @author davidc
 * @author dward
 */
public class CMISServicesImpl implements CMISServices, ApplicationContextAware, ApplicationListener<ApplicationContextEvent>, TenantDeployer
{
    /** Query Parameters */
    private static final QName PARAM_PARENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parent");
    private static final QName PARAM_USERNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "username");

    private static final String LUCENE_QUERY_CHECKEDOUT =
        "+@cm\\:workingCopyOwner:${cm:username}";
    
    private static final String LUCENE_QUERY_CHECKEDOUT_IN_FOLDER =
        "+@cm\\:workingCopyOwner:${cm:username} " +
        "+PARENT:\"${cm:parent}\"";
    
    private static final int ASSOC_ID_PREFIX_LENGTH = ASSOC_ID_PREFIX.length();
    
    private static final Pattern ORDER_BY_PATTERN = Pattern.compile("^([^\\s,\"'\\\\\\.\\(\\)]+)\\s+(ASC|DESC)$");
    
    // dependencies
    private Repository repository;
    private RetryingTransactionHelper retryingTransactionHelper;
    private DictionaryService dictionaryService;
    private CMISDictionaryService cmisDictionaryService;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private TenantAdminService tenantAdminService;
    private CMISRenditionService cmisRenditionService;
    private CheckOutCheckInService checkOutCheckInService;
    private VersionService versionService;
    private MimetypeService mimetypeService;
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();

    // CMIS supported version
    private String cmisVersion = "[undefined]";
    private String cmisSpecTitle = "[undefined]";
    
    // default CMIS store and path
    private StoreRef defaultStoreRef;
    private String defaultRootPath;
    private Map<String, NodeRef> defaultRootNodeRefs;
    
    // data types for query
    private DataTypeDefinition nodeRefDataType;
    private DataTypeDefinition textDataType;

    
    /**
     * Sets the supported version of the CMIS specification 
     * 
     * @param cmisVersion
     */
    public void setCMISSpecVersion(String cmisVersion)
    {
        this.cmisVersion = cmisVersion;
    }
    
    /**
     * Sets the CMIS specification title 
     * 
     * @param cmisTitle
     */
    public void setCMISSpecTitle(String cmisSpecTitle)
    {
        this.cmisSpecTitle = cmisSpecTitle;
    }
    
    /**
     * Sets the default root store
     * 
     * @param store  store_type://store_id
     */
    public void setDefaultStore(String store)
    {
        this.defaultStoreRef = new StoreRef(store);
    }
    
    /**
     * Sets the default root path
     * 
     * @param path  path within default store
     */
    public void setDefaultRootPath(String path)
    {
        defaultRootPath = path;
    }
    
    /**
     * Sets the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param cmisDictionaryService
     */
    public void setCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }
    
    /**
     * @param searchService
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param fileFolderService
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Sets the content service.
     * 
     * @param contentService
     *            the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Sets the repository.
     * 
     * @param repository
     *            the repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }        
    
    /**
     * Sets the cmis rendition service.
     * 
     * @param cmisRenditionService
     *            the cmis rendition service
     */
    public void setCMISRenditionService(CMISRenditionService cmisRenditionService)
    {
        this.cmisRenditionService = cmisRenditionService;
    }

    /**
     * Sets the check out check in service.
     * 
     * @param checkOutCheckInService
     *            the check out check in service
     */
    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    /**
     * Sets the version service.
     * 
     * @param versionService
     *            the version service
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }
    
    /**
     * Sets the mimetype service.
     * 
     * @param mimetypeService
     *            the mimetype service
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * Hooks into Spring Application Lifecycle.
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            init();
        }
    
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onEnableTenant()
     */
    public void onEnableTenant()
    {
        init();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onDisableTenant()
     */
    public void onDisableTenant()
    {
        destroy();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#init()
     */
    public void init()
    {
        // initialise data types
        nodeRefDataType = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
        textDataType = dictionaryService.getDataType(DataTypeDefinition.TEXT);

        // initialise root node ref
        tenantAdminService.register(this);
        if (defaultRootNodeRefs == null)
        {
            defaultRootNodeRefs = new HashMap<String, NodeRef>(1);
        }
        getDefaultRootNodeRef();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#destroy()
     */
    public void destroy()
    {
        defaultRootNodeRefs.remove(tenantAdminService.getCurrentUserDomain());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getCMISVersion()
     */
    public String getCMISVersion()
    {
        return cmisVersion;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getCMISSpecTitle()
     */
    public String getCMISSpecTitle()
    {
        return cmisSpecTitle;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getDefaultRootPath()
     */
    public String getDefaultRootPath()
    {
        return defaultRootPath;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getDefaultRootNodeRef()
     */
    public NodeRef getDefaultRootNodeRef()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        NodeRef defaultNodeRef = defaultRootNodeRefs.get(tenantDomain);
        if (defaultNodeRef == null)
        {       
            defaultNodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Exception
                        {
                            NodeRef root = nodeService.getRootNode(defaultStoreRef);
                            List<NodeRef> rootNodes = searchService.selectNodes(root, defaultRootPath, null, namespaceService, false);
                            if (rootNodes.size() != 1)
                            {
                                throw new AlfrescoRuntimeException("Unable to locate CMIS root path " + defaultRootPath);
                            }
                            return rootNodes.get(0);
                        };
                    }, true, false);
                }
            }, AuthenticationUtil.getSystemUserName());
            
            if (defaultNodeRef == null)
            {
                throw new AlfrescoRuntimeException("Default root folder path '" + defaultRootPath + "' not found");
            }
            defaultRootNodeRefs.put(tenantDomain, defaultNodeRef);
        }
        return defaultNodeRef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getDefaultRootStoreRef()
     */
    public StoreRef getDefaultRootStoreRef()
    {
        return getDefaultRootNodeRef().getStoreRef();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getNode(java.lang.String, java.lang.String[])
     */
    public NodeRef getNode(String referenceType, String[] reference)
    {
        NodeRef nodeRef = repository.findNodeRef(referenceType, reference);
        return nodeRef;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getRenditions(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Map<String, Object> getRenditions(NodeRef nodeRef, String renditionFilter) throws CMISFilterNotValidException
    {
        Map<String, Object> result = new TreeMap<String, Object>();
        List<CMISRendition> renditions = cmisRenditionService.getRenditions(nodeRef, renditionFilter);
        if (renditions == null)
        {
            renditions = Collections.emptyList();
        }
        result.put("node", nodeRef);
        result.put("renditionFilter", renditionFilter); // Record rendition filter to aid recursion on node maps
        result.put("renditions", renditions);
        List<NodeRef> renditionNodes = new ArrayList<NodeRef>(renditions.size());
        for (CMISRendition rendition : renditions)
        {
            renditionNodes.add(rendition.getNodeRef());
        }
        result.put("renditionNodes", renditionNodes);
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getChildren(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.cmis.CMISTypesFilterEnum, java.lang.String)
     */
    public NodeRef[] getChildren(NodeRef parent, CMISTypesFilterEnum typesFilter, String orderBy)
            throws CMISInvalidArgumentException
    {
        if (typesFilter == CMISTypesFilterEnum.POLICIES)
        {
            return new NodeRef[0];
        }
        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(parent.getStoreRef());
        QueryParameterDefinition parentDef = new QueryParameterDefImpl(PARAM_PARENT, nodeRefDataType, true, parent.toString());
        params.addQueryParameterDefinition(parentDef);

        // Build a query for the appropriate types
        StringBuilder query = new StringBuilder(1024).append("+PARENT:\"${cm:parent}\" -ASPECT:\"").append(
                ContentModel.ASPECT_WORKING_COPY).append("\" +TYPE:(");

        // Include doc type if necessary
        if (typesFilter != CMISTypesFilterEnum.FOLDERS)
        {
            query.append('"').append(ContentModel.TYPE_CONTENT).append('"');
        }
        // Include folder type if necessary
        if (typesFilter != CMISTypesFilterEnum.DOCUMENTS)
        {
            if (typesFilter == CMISTypesFilterEnum.ANY)
            {
                query.append(" ");
            }
            query.append('"').append(ContentModel.TYPE_FOLDER).append('"');
        }
        // Always exclude system folders
        query.append(") -TYPE:\"").append(ContentModel.TYPE_SYSTEM_FOLDER).append("\"");
        params.setQuery(query.toString());
        parseOrderBy(orderBy, params);
        ResultSet resultSet = null;
        try
        {
            resultSet = searchService.query(params);
            List<NodeRef> results = resultSet.getNodeRefs();
            NodeRef[] nodeRefs = new NodeRef[results.size()];
            return results.toArray(nodeRefs);
        }
        finally
        {
            if (resultSet != null) resultSet.close();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getCheckedOut(java.lang.String, org.alfresco.service.cmr.repository.NodeRef,
     * boolean, java.lang.String)
     */
    public NodeRef[] getCheckedOut(String username, NodeRef folder, boolean includeDescendants, String orderBy)
            throws CMISInvalidArgumentException
    {
        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        QueryParameterDefinition usernameDef = new QueryParameterDefImpl(PARAM_USERNAME, textDataType, true, username);
        params.addQueryParameterDefinition(usernameDef);
        
        if (folder == null)
        {
            // get all checked-out items
            params.setQuery(LUCENE_QUERY_CHECKEDOUT);
            params.addStore(getDefaultRootStoreRef());
        }
        else
        {
            // get all checked-out items within folder
            // NOTE: special-case for all descendants in root folder (treat as all checked-out items)
            if (includeDescendants && nodeService.getRootNode(folder.getStoreRef()) == folder)
            {
                // get all checked-out items within specified folder store
                params.setQuery(LUCENE_QUERY_CHECKEDOUT);
                params.addStore(folder.getStoreRef());
            }
            else
            {
                // TODO: implement descendants of folder
                params.setQuery(LUCENE_QUERY_CHECKEDOUT_IN_FOLDER);
                params.addStore(folder.getStoreRef());
                QueryParameterDefinition parentDef = new QueryParameterDefImpl(PARAM_PARENT, nodeRefDataType, true, folder.toString());
                params.addQueryParameterDefinition(parentDef);
            }
        }
        parseOrderBy(orderBy, params);
        
        ResultSet resultSet = null;
        try
        {
            resultSet = searchService.query(params);
            List<NodeRef> results = resultSet.getNodeRefs();
            NodeRef[] nodeRefs = new NodeRef[results.size()];
            return results.toArray(nodeRefs);
        }
        finally
        {
            if (resultSet != null) resultSet.close();
        }
    }

    /**
     * Parses an order by clause and adds its orderings to the given search parameters.
     * 
     * @param orderBy
     *            the order by clause
     * @param params
     *            the search parameters
     * @throws CMISInvalidArgumentException
     *             if the order by clause is invalid
     */
    private void parseOrderBy(String orderBy, SearchParameters params) throws CMISInvalidArgumentException
    {
        if (orderBy == null)
        {
            return;
        }
        for (String token : orderBy.split(","))
        {
            Matcher matcher = ORDER_BY_PATTERN.matcher(token);
            if (!matcher.matches())
            {
                throw new CMISInvalidArgumentException("Invalid order by clause: \"" + orderBy + '"');
            }
            String queryName = matcher.group(1);
            CMISPropertyDefinition propDef = cmisDictionaryService.findPropertyByQueryName(queryName);
            if (propDef == null)
            {
                throw new CMISInvalidArgumentException("No such property: \"" + queryName + '"');
            }
            // We can only order by orderable properties
            if (propDef.isOrderable())
            {
                params.addSort(propDef.getPropertyLuceneBuilder().getLuceneFieldName(), matcher.group(2).equals("ASC"));
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getRelationships(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.cmis.CMISTypeId, boolean, org.alfresco.cmis.CMISRelationshipDirectionEnum)
     */
    public AssociationRef[] getRelationships(NodeRef node, CMISTypeDefinition relDef, boolean includeSubTypes, CMISRelationshipDirectionEnum direction)
            throws CMISInvalidArgumentException
    {
        // by the spec. if typeId=null then it is necessary return ALL associated Relationship objects!
        // establish relationship type to filter on
        if (relDef == null)
        {
            relDef = cmisDictionaryService.findType(CMISDictionaryModel.RELATIONSHIP_TYPE_ID);
        }
        if (!relDef.getBaseType().getTypeId().equals(CMISDictionaryModel.RELATIONSHIP_TYPE_ID))
        {
            throw new AlfrescoRuntimeException("Type Id " + relDef.getTypeId() + " is not a relationship type");
        }

        // retrieve associations
        List<AssociationRef> assocs = new ArrayList<AssociationRef>();
        if (direction == CMISRelationshipDirectionEnum.SOURCE || direction == CMISRelationshipDirectionEnum.EITHER)
        {
            assocs.addAll(nodeService.getTargetAssocs(node, RegexQNamePattern.MATCH_ALL));
        }
        if (direction == CMISRelationshipDirectionEnum.TARGET || direction == CMISRelationshipDirectionEnum.EITHER)
        {
            assocs.addAll(nodeService.getSourceAssocs(node, RegexQNamePattern.MATCH_ALL));
        }

        // filter association by type
        // NOTE: even if typeId = null, we still filter out relationships that do not map to CMIS domain model e.g.
        //       relationships whose source or target are not folders or documents
        Collection<CMISTypeDefinition> subRelDefs = (includeSubTypes ? relDef.getSubTypes(true) : null);
        List<AssociationRef> filteredAssocs = new ArrayList<AssociationRef>(assocs.size());
        for (AssociationRef assoc : assocs)
        {
            CMISTypeDefinition assocTypeDef = cmisDictionaryService.findTypeForClass(assoc.getTypeQName(), CMISScope.RELATIONSHIP);
            QName sourceTypeDef = nodeService.getType(assoc.getSourceRef());
            QName targetTypeDef = nodeService.getType(assoc.getTargetRef());
            if (assocTypeDef == null || cmisDictionaryService.findTypeForClass(sourceTypeDef) == null ||
                    cmisDictionaryService.findTypeForClass(targetTypeDef) == null)
            {
                continue;
            }
            if (assocTypeDef.equals(relDef) || (subRelDefs != null && subRelDefs.contains(assocTypeDef)))
            {
                filteredAssocs.add(assoc);
            }
        }

        AssociationRef[] assocArray = new AssociationRef[filteredAssocs.size()];
        filteredAssocs.toArray(assocArray);
        return assocArray;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getProperty(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Serializable getProperty(NodeRef nodeRef, String propertyName) throws CMISInvalidArgumentException
    {
        return getProperty(nodeRef, getTypeDefinition(nodeRef), propertyName);
    }

    public Serializable getProperty(NodeRef nodeRef, CMISTypeDefinition typeDef, String propertyName)
            throws CMISInvalidArgumentException
    {
        CMISPropertyDefinition propDef = cmisDictionaryService.findProperty(propertyName, typeDef);
        if (propDef == null)
        {
            if (typeDef == null)
            {
                throw new CMISInvalidArgumentException("Property " + propertyName + " not found in CMIS Dictionary");
            }
            else
            {
                throw new CMISInvalidArgumentException("Property " + propertyName + " not found for type "
                        + typeDef.getTypeId() + " in CMIS Dictionary");
            }
        }
        return propDef.getPropertyAccessor().getValue(nodeRef);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getTypeDefinition(org.alfresco.service.cmr.repository.NodeRef)
     */
    public CMISTypeDefinition getTypeDefinition(NodeRef nodeRef) throws CMISInvalidArgumentException
    {
        QName typeQName = nodeService.getType(nodeRef);
        CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(typeQName);
        if (typeDef == null)
        {
            throw new CMISInvalidArgumentException("Type " + typeQName + " not found in CMIS Dictionary");
        }
        return typeDef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getTypeDefinition(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public CMISTypeDefinition getTypeDefinition(AssociationRef associationRef) throws CMISInvalidArgumentException
    {
        QName typeQName = associationRef.getTypeQName();
        CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(typeQName, CMISScope.RELATIONSHIP);
        if (typeDef == null)
        {
            throw new CMISInvalidArgumentException("Association Type " + typeQName + " not found in CMIS Dictionary");
        }
        return typeDef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getTypeDefinition(java.lang.String)
     */
    public CMISTypeDefinition getTypeDefinition(String typeId) throws CMISInvalidArgumentException
    {
        CMISTypeDefinition typeDef = null;
        try
        {
            typeDef = cmisDictionaryService.findType(typeId);
        }
        catch (Exception e)
        {
        }
        if (typeDef == null)
        {
            throw new CMISInvalidArgumentException("Invalid typeId " + typeId);
        }
        return typeDef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getTypeDefinition(java.lang.Object)
     */
    public CMISTypeDefinition getTypeDefinition(Object object) throws CMISInvalidArgumentException
    {
        if (object instanceof Version)
        {
            return getTypeDefinition(((Version) object).getFrozenStateNodeRef());
        }
        else if (object instanceof NodeRef)
        {
            return getTypeDefinition((NodeRef) object);
        }
        else if (object instanceof AssociationRef)
        {
            return getTypeDefinition((AssociationRef) object);
        }
        throw new CMISInvalidArgumentException("Invalid type " + object.getClass());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getBaseTypes()
     */
    public Collection<CMISTypeDefinition> getBaseTypes()
    {
        return cmisDictionaryService.getBaseTypes();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getProperty(org.alfresco.service.cmr.repository.AssociationRef,
     * java.lang.String)
     */
    public Serializable getProperty(AssociationRef assocRef, String propertyName) throws CMISInvalidArgumentException
    {
        CMISTypeDefinition typeDef = getTypeDefinition(assocRef);
        CMISPropertyDefinition propDef = cmisDictionaryService.findProperty(propertyName, typeDef);
        if (propDef == null)
        {
            throw new AlfrescoRuntimeException("Property " + propertyName + " not found for relationship type "
                    + typeDef.getTypeId() + " in CMIS Dictionary");
        }
        return propDef.getPropertyAccessor().getValue(assocRef);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, Serializable> getProperties(NodeRef nodeRef) throws CMISInvalidArgumentException
    {
        return getProperties(nodeRef, getTypeDefinition(nodeRef));
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getProperties(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public Map<String, Serializable> getProperties(AssociationRef assocRef) throws CMISInvalidArgumentException
    {
        CMISTypeDefinition typeDef = getTypeDefinition(assocRef);
        Map<String, CMISPropertyDefinition> propDefs = typeDef.getPropertyDefinitions();
        Map<String, Serializable> values = new HashMap<String, Serializable>(propDefs.size() * 2);
        for (CMISPropertyDefinition propDef : propDefs.values())
        {
            values.put(propDef.getPropertyId().getId(), propDef.getPropertyAccessor().getValue(assocRef));
        }
        return values;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getProperties(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.cmis.CMISTypeDefinition)
     */
    public Map<String, Serializable> getProperties(NodeRef nodeRef, CMISTypeDefinition typeDef)
            throws CMISInvalidArgumentException
    {
        Map<String, CMISPropertyDefinition> propDefs = typeDef.getPropertyDefinitions();
        Map<String, Serializable> values = new HashMap<String, Serializable>(propDefs.size() * 2);
        for (CMISPropertyDefinition propDef : propDefs.values())
        {
            values.put(propDef.getPropertyId().getId(), propDef.getPropertyAccessor().getValue(nodeRef));
        }
        return values;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<CMISTypeDefinition> getAspects(NodeRef nodeRef)
    {
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        Set<CMISTypeDefinition> result = new HashSet<CMISTypeDefinition>(aspects.size() * 2);
        for (QName aspect : aspects)
        {
            CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(aspect, CMISScope.POLICY);
            if (typeDef != null)
            {
                result.add(typeDef);
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#setProperty(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.io.Serializable)
     */
    public void setProperty(NodeRef nodeRef, String propertyName, Serializable value)
            throws CMISInvalidArgumentException, CMISConstraintException
    {
        setProperty(nodeRef, getTypeDefinition(nodeRef), propertyName, value);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#setProperty(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.cmis.CMISTypeDefinition, java.lang.String, java.io.Serializable)
     */
    public void setProperty(NodeRef nodeRef, CMISTypeDefinition typeDef, String propertyName, Serializable value)
            throws CMISInvalidArgumentException, CMISConstraintException
    {
        CMISPropertyDefinition propDef = cmisDictionaryService.findProperty(propertyName, typeDef);
        if (propDef == null)
        {
            if (typeDef == null)
            {
                throw new CMISInvalidArgumentException("Property " + propertyName + " not found in CMIS Dictionary");
            }
            else
            {
                throw new CMISInvalidArgumentException("Property " + propertyName + " not found for type "
                        + typeDef.getTypeId() + " in CMIS Dictionary");
            }
        }

        CMISUpdatabilityEnum updatability = propDef.getUpdatability();
        if (updatability == CMISUpdatabilityEnum.READ_ONLY
                || updatability == CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT
                && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            throw new CMISConstraintException("Unable to update read-only property " + propertyName);
        }

        if (propDef.isRequired() && value == null)
        {
            throw new CMISConstraintException("Property " + propertyName + " is required");
        }

        if (propDef.getDataType() == CMISDataTypeEnum.STRING && propDef.getMaximumLength() > 0 && value != null
                && value.toString().length() > propDef.getMaximumLength())
        {
            throw new CMISConstraintException("Value is too long for property " + propertyName);
        }

        QName property = propDef.getPropertyAccessor().getMappedProperty();
        if (property == null)
        {
            throw new CMISConstraintException("Unable to set property " + propertyName);
        }
        
        if (property.equals(ContentModel.PROP_NAME))
        {
            try
            {
                fileFolderService.rename(nodeRef, value.toString());
            }
            catch (FileExistsException e)
            {
                throw new CMISConstraintException("Object already exists with name " + value.toString());
            }
            catch (FileNotFoundException e)
            {
                throw new CMISInvalidArgumentException("Object with id " + nodeRef.toString() + " not found");
            }
        }
        else
        {
            nodeService.setProperty(nodeRef, property, value);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#setAspects(org.alfresco.service.cmr.repository.NodeRef, java.lang.Iterable,
     * java.lang.Iterable)
     */
    public void setAspects(NodeRef node, Iterable<String> aspectsToRemove, Iterable<String> aspectsToAdd)
            throws CMISInvalidArgumentException
    {
        for (String aspectType : aspectsToRemove)
        {
            try
            {
                nodeService.removeAspect(node, getTypeDefinition(aspectType).getTypeId().getQName());
            }
            catch (InvalidAspectException e)
            {
                throw new CMISInvalidArgumentException("Invalid aspect " + aspectType);
            }
            catch (InvalidNodeRefException e)
            {
                throw new CMISInvalidArgumentException("Invalid node " + node);
            }
        }
        for (String aspectType : aspectsToAdd)
        {
            try
            {
                nodeService.addAspect(node, getTypeDefinition(aspectType).getTypeId().getQName(), Collections
                        .<QName, Serializable> emptyMap());
            }
            catch (InvalidAspectException e)
            {
                throw new CMISInvalidArgumentException("Invalid aspect " + aspectType);
            }
            catch (InvalidNodeRefException e)
            {
                throw new CMISInvalidArgumentException("Invalid node " + node);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#applyVersioningState(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.cmis.CMISVersioningStateEnum)
     */
    public NodeRef applyVersioningState(NodeRef source, CMISVersioningStateEnum versioningState)
            throws CMISConstraintException, CMISInvalidArgumentException
    {
        switch (versioningState)
        {
        case NONE:
            return source;
        case CHECKED_OUT:
            validateVersionable(source);
            if (this.nodeService.hasAspect(source, ContentModel.ASPECT_VERSIONABLE) == false)
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                props.put(ContentModel.PROP_AUTO_VERSION, false);
                this.nodeService.addAspect(source, ContentModel.ASPECT_VERSIONABLE, props);
            }
            return this.checkOutCheckInService.checkout(source);
        default:
            validateVersionable(source);
            this.versionService.createVersion(source, createVersionProperties("Initial Version",
                    versioningState != CMISVersioningStateEnum.MINOR));
            break;
        }
        return source;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#checkOut(java.lang.String)
     */
    public NodeRef checkOut(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef nodeRef = getObject(objectId, NodeRef.class, true, true, false);
        try
        {
            return this.checkOutCheckInService.checkout(nodeRef);
        }
        catch (CheckOutCheckInServiceException e)
        {
            throw new CMISVersioningException(e.getMessage(), e);
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#checkIn(java.lang.String, java.lang.String, boolean)
     */
    public NodeRef checkIn(String objectId, String checkinComment, boolean isMajor) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException
    {
        NodeRef nodeRef = getObject(objectId, NodeRef.class, true, true, true);
        try
        {
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                props.put(ContentModel.PROP_AUTO_VERSION, false);
                this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
            }
            return checkOutCheckInService.checkin(nodeRef, createVersionProperties(checkinComment, isMajor));
        }
        catch (CheckOutCheckInServiceException e)
        {
            throw new CMISVersioningException(e.getMessage(), e);
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#cancelCheckOut(java.lang.String)
     */
    public void cancelCheckOut(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef nodeRef = getObject(objectId, NodeRef.class, true, true, true);
        try
        {
            checkOutCheckInService.cancelCheckout(nodeRef);
        }
        catch (CheckOutCheckInServiceException e)
        {
            throw new CMISVersioningException(e.getMessage(), e);
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getAllVersions(java.lang.String)
     */
    public List<NodeRef> getAllVersions(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef nodeRef = getVersionSeries(objectId, NodeRef.class, true);

        List<NodeRef> objects = new LinkedList<NodeRef>();
        NodeRef pwc = checkOutCheckInService.getWorkingCopy(nodeRef);
        if (pwc != null)
        {
            objects.add(pwc);
        }
        VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        if (versionHistory != null)
        {
            Version current = versionService.getCurrentVersion(nodeRef);
            while (current != null)
            {
                objects.add(current.getFrozenStateNodeRef());
                current = versionHistory.getPredecessor(current);
            }
        }
        else if (pwc == null)
        {
            objects.add(nodeRef);
        }
        return objects;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getObject(java.lang.String, java.lang.Class, boolean, boolean, boolean)
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String objectId, Class<T> requiredType, boolean forUpdate, boolean isVersionable,
            boolean isPwc) throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        try
        {
            int sepIndex = objectId.lastIndexOf(';');
            String nodeRefString;
            // Handle version format IDs
            if (sepIndex != -1 && NodeRef.isNodeRef(nodeRefString = objectId.substring(0, sepIndex)))
            {
                if (isPwc)
                {
                    throw new CMISVersioningException(objectId + " is not a working copy");
                }

                // Allow returning of non-updateable version nodes as noderefs
                if (requiredType.isAssignableFrom(Version.class) || !forUpdate
                        && requiredType.isAssignableFrom(NodeRef.class))
                {
                    NodeRef nodeRef = new NodeRef(nodeRefString);
                    if (!nodeService.exists(nodeRef))
                    {
                        throw new CMISObjectNotFoundException("Unable to find object " + objectId);
                    }
                    VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
                    if (versionHistory == null)
                    {
                        throw new CMISObjectNotFoundException("Unable to find object " + objectId);
                    }
                    try
                    {
                        Version version = versionHistory.getVersion(objectId.substring(sepIndex + 1));

                        // Return as noderef if required
                        return requiredType.isAssignableFrom(Version.class) ? (T) version : (T) version
                                .getFrozenStateNodeRef();
                    }
                    catch (VersionDoesNotExistException e)
                    {
                        throw new CMISObjectNotFoundException("Unable to find object " + objectId);
                    }
                }
                else if (requiredType.isAssignableFrom(NodeRef.class))
                {
                    // We wanted an updateable node but got a history node
                    throw new CMISVersioningException(objectId + " is not a current node");
                }
            }
            // Handle node format IDs
            else if (NodeRef.isNodeRef(objectId))
            {
                if (requiredType.isAssignableFrom(NodeRef.class))
                {
                    NodeRef nodeRef = new NodeRef(objectId);
                    if (!nodeService.exists(nodeRef))
                    {
                        throw new CMISObjectNotFoundException("Unable to find object " + objectId);
                    }
                    if (isVersionable)
                    {
                        validateVersionable(nodeRef);

                        // Check that the PWC status is as we require
                        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
                        {
                            // This is a PWC so make sure we wanted one
                            if (!isPwc)
                            {
                                throw new CMISVersioningException(objectId + " is a working copy");
                            }
                        }
                        else
                        {
                            // This is not a PWC so make sure we didn't want one
                            if (isPwc)
                            {
                                throw new CMISVersioningException(objectId + " is not a working copy");
                            }

                            // If it should be updateable, make sure it's not currently checked out
                            if (forUpdate)
                            {
                                if (checkOutCheckInService.getWorkingCopy(nodeRef) != null)
                                {
                                    throw new CMISVersioningException("Can't update " + objectId + " while checked out");
                                }
                            }
                        }
                    }
                    return (T) new NodeRef(objectId);
                }
            }
            // Handle Assoc IDs
            else if (objectId.startsWith(ASSOC_ID_PREFIX))
            {
                if (isPwc)
                {
                    throw new CMISVersioningException(objectId + " is not a working copy");
                }
                if (isVersionable)
                {
                    throw new CMISConstraintException("Type " + CMISDictionaryModel.RELATIONSHIP_TYPE_ID
                            + " is not versionable");
                }
                if (requiredType.isAssignableFrom(AssociationRef.class))
                {
                    AssociationRef associationRef = nodeService.getAssoc(new Long(objectId.substring(ASSOC_ID_PREFIX_LENGTH)));
                    if (associationRef == null)
                    {
                        throw new CMISObjectNotFoundException("Unable to find object " + objectId);
                    }
                    return (T) associationRef;
                }
            }
            else
            {
                throw new CMISInvalidArgumentException(objectId + " is not an object ID");
            }
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e);
        }
        throw new CMISConstraintException("Object " + objectId + " is not of required type");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getReadableObject(java.lang.String, java.lang.Class)
     */
    public <T> T getReadableObject(String objectId, Class<T> requiredType) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException
    {
        return getObject(objectId, requiredType, false, false, false);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getFolder(java.lang.String)
     */
    public NodeRef getFolder(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef folderRef = getReadableObject(objectId, NodeRef.class);
        CMISTypeDefinition typeDef = getTypeDefinition(folderRef);
        if (typeDef.getTypeId().getBaseTypeId() != CMISDictionaryModel.FOLDER_TYPE_ID)
        {
            throw new CMISInvalidArgumentException("Object " + objectId + " is not a folder");
        }
        return folderRef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getFolderParent(java.lang.String)
     */
    public NodeRef getFolderParent(String folderId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef folderRef = getFolder(folderId);
        if (getDefaultRootNodeRef().equals(folderRef))
        {
            throw new CMISInvalidArgumentException("Root Folder has no parents");
        }
        return nodeService.getPrimaryParent(folderRef).getParentRef();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getVersionSeries(java.lang.String, java.lang.Class, boolean)
     */
    @SuppressWarnings("unchecked")
    public <T> T getVersionSeries(String objectId, Class<T> requiredType, boolean isVersionable)
            throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        // Preserve non-node objects
        if (!requiredType.isAssignableFrom(NodeRef.class))
        {
            return getObject(objectId, requiredType, false, isVersionable, false);
        }

        Object object = getReadableObject(objectId, Object.class);
        Object result;
        // Map version nodes back to their source node
        if (object instanceof Version)
        {
            result = ((Version) object).getVersionedNodeRef();
        }
        else if (object instanceof NodeRef)
        {
            NodeRef nodeRef = (NodeRef) object;
            // Map working copy nodes back to where they were checked out from
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
            {
                result = (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_COPY_REFERENCE);
            }
            // Preserve all other nodes
            else
            {
                result = nodeRef;
            }
            if (isVersionable)
            {
                validateVersionable((NodeRef)result);
            }
        }
        else if (requiredType.isAssignableFrom(object.getClass()))
        {
            if (isVersionable)
            {
                throw new CMISConstraintException(objectId + " is not versionable");
            }
            result = object;
        }
        else
        {
            throw new CMISConstraintException("Object " + objectId + " is not of required type");
        }
        return (T)result;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getLatestVersion(java.lang.String, boolean)
     */
    public NodeRef getLatestVersion(String objectId, boolean major) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException
    {
        NodeRef versionSeries = getVersionSeries(objectId, NodeRef.class, false);
        // If we don't care whether the latest version is major or minor, the latest version is either the working copy
        // or the live node
        if (!major)
        {
            NodeRef nodeRef = checkOutCheckInService.getWorkingCopy(versionSeries);
            if (nodeRef != null)
            {
                return nodeRef;
            }

            return versionSeries;
        }

        // Now check the version history
        VersionHistory versionHistory = versionService.getVersionHistory(versionSeries);
        if (versionHistory == null)
        {
            throw new CMISObjectNotFoundException(objectId + " has no major version");
        }
        Version current = versionService.getCurrentVersion(versionSeries);
        while (current != null && current.getVersionType() != VersionType.MAJOR)
        {
            current = versionHistory.getPredecessor(current);
        }
        if (current == null)
        {
            throw new CMISObjectNotFoundException(objectId + " has no major version");
        }
        return current.getFrozenStateNodeRef();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#deleteContentStream(java.lang.String)
     */
    public void deleteContentStream(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef currentNode = getObject(objectId, NodeRef.class, true, false, false);
        CMISTypeDefinition typeDef = getTypeDefinition(currentNode);
        if (CMISContentStreamAllowedEnum.REQUIRED.equals(typeDef.getContentStreamAllowed()))
        {
            throw new CMISConstraintException(
                    "The 'contentStreamAllowed' attribute of the specified Object-Type definition is set to 'required'.");
        }

        try
        {
            nodeService.setProperty(currentNode, ContentModel.PROP_CONTENT, null);
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#deleteObject(java.lang.String, boolean)
     */
    public void deleteObject(String objectId, boolean allVersions) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException, CMISRuntimeException, CMISServiceException
    {
        try
        {
            Object object = allVersions ? getVersionSeries(objectId, Object.class, false) : getObject(objectId, Object.class, true, false,
                    false);

            // Handle associations
            if (object instanceof AssociationRef)
            {
                AssociationRef assocRef = (AssociationRef) object;
                nodeService
                        .removeAssociation(assocRef.getSourceRef(), assocRef.getTargetRef(), assocRef.getTypeQName());
                return;
            }

            // Handle individual versions
            if (object instanceof Version)
            {
                Version version = (Version) object;
                versionService.deleteVersion(version.getVersionedNodeRef(), version);
                return;
            }
            NodeRef nodeRef = (NodeRef) object;

            // Handle a working copy
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
            {
                checkOutCheckInService.cancelCheckout(nodeRef);
                return;
            }

            // Handle 'real' nodes
            CMISTypeDefinition typeDef = getTypeDefinition(nodeRef);
            if (typeDef.getTypeId().getBaseTypeId() == CMISDictionaryModel.FOLDER_TYPE_ID)
            {
                if (nodeService.getChildAssocs(nodeRef).size() > 0)
                {
                    throw new CMISConstraintException("Could not delete folder with at least one Child");
                }
            }
            // Only honour the allVersions flag for non-folder versionable objects
            else if (typeDef.isVersionable() && allVersions)
            {
                NodeRef workingCopy = checkOutCheckInService.getWorkingCopy(nodeRef);
                if (workingCopy != null)
                {
                    checkOutCheckInService.cancelCheckout(workingCopy);
                }
                versionService.deleteVersionHistory(nodeRef);
            }
            
            // Remove not primary parent associations   
            List<ChildAssociationRef> childAssociations = nodeService.getParentAssocs(nodeRef);
            if (childAssociations != null)
            {
                for (ChildAssociationRef childAssoc : childAssociations)
                {
                    if (!childAssoc.isPrimary())
                    {
                        nodeService.removeChildAssociation(childAssoc);
                    }
                }
            }
            // Attempt to delete the node
            nodeService.deleteNode(nodeRef);
        }
        catch (CMISServiceException e)
        {
            throw e;
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e);
        }
        catch (Exception e)
        {
            throw new CMISRuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#deleteTree(java.lang.String, boolean, boolean, boolean)
     */
    public List<String> deleteTree(String objectId, boolean continueOnFailure, boolean unfile, boolean deleteAllVersions)
            throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        NodeRef folderRef = getFolder(objectId);
        List<String> failedToDelete = new LinkedList<String>();
        deleteTree(nodeService.getPrimaryParent(folderRef), continueOnFailure, unfile, deleteAllVersions,
                failedToDelete);
        return failedToDelete;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#deleteTreeReportLastError(java.lang.String, boolean, boolean, boolean)
     */
    public void deleteTreeReportLastError(String objectId, boolean continueOnFailure, boolean unfile,
            boolean deleteAllVersions) throws CMISServiceException
    {
        NodeRef folderRef = getFolder(objectId);
        List<String> failedToDelete = new LinkedList<String>();
        CMISServiceException lastError = deleteTree(nodeService.getPrimaryParent(folderRef), continueOnFailure, unfile,
                deleteAllVersions, failedToDelete);
        if (lastError != null)
        {
            throw lastError;
        }
    }

    /**
     * Internal recursive helper method for tree deletion. Returns the last error encountered, rather than throwing it,
     * to avoid transaction rollback.
     * 
     * @param parentRef
     *            the parent folder
     * @param continueOnFailure
     *            should we continue if an error occurs with one of the children?
     * @param unfile
     *            should we remove non-primary associations to nodes rather than delete them?
     * @param deleteAllVersions
     *            should we delete all the versions of the documents we delete?
     * @param failedToDelete
     *            list of object IDs of the children we failed to delete
     * @return the last error encountered.
     * @throws CMISInvalidArgumentException
     *             the CMIS invalid argument exception
     */
    private CMISServiceException deleteTree(ChildAssociationRef parentRef, boolean continueOnFailure, boolean unfile,
            boolean deleteAllVersions, List<String> failedToDelete) throws CMISInvalidArgumentException
    {
        CMISServiceException lastError = null;
        NodeRef child = parentRef.getChildRef();

        // Due to multi-filing, it could be that a sub-tree has already been deleted
        if (!nodeService.exists(child))
        {
            return lastError;
        }

        String objectId = (String) getProperty(child, CMISDictionaryModel.PROP_OBJECT_ID);

        // First Delete children
        for (ChildAssociationRef childRef : nodeService.getChildAssocs(child))
        {
            CMISServiceException thisError = deleteTree(childRef, continueOnFailure, unfile, deleteAllVersions,
                    failedToDelete);
            if (thisError != null)
            {
                lastError = thisError;
                if (!continueOnFailure)
                {
                    return lastError;
                }
            }
        }

        // Don't try deleting the parent if we couldn't delete one or more of its children
        if (lastError != null)
        {
            failedToDelete.add(objectId);
            return lastError;
        }

        // Now delete the parent
        try
        {
            if (unfile && !parentRef.isPrimary())
            {
                this.nodeService.removeChildAssociation(parentRef);
            }
            else
            {
                deleteObject(objectId, deleteAllVersions);
            }
        }
        catch (AccessDeniedException t)
        {
            failedToDelete.add(objectId);
            lastError = new CMISPermissionDeniedException(t);
        }
        catch (Throwable t)
        {
            // Before absorbing an exception, check whether it is a transactional one that should be retried further up
            // the stack
            if (RetryingTransactionHelper.extractRetryCause(t) != null)
            {
                throw new AlfrescoRuntimeException("Transactional Error", t);
            }
            failedToDelete.add(objectId);
            lastError = t instanceof CMISServiceException ? (CMISServiceException) t : new CMISRuntimeException(t);
        }

        return lastError;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#addObjectToFolder(java.lang.String, java.lang.String)
     */
    public void addObjectToFolder(String objectId, String folderId) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException
    {
        try
        {
            NodeRef objectNodeRef = getObject(objectId, NodeRef.class, true, false, false);
            NodeRef parentFolderNodeRef = getFolder(folderId);
            CMISTypeDefinition objectType = getTypeDefinition(objectNodeRef);
            CMISTypeDefinition folderType = getTypeDefinition(parentFolderNodeRef);
            if (!folderType.getAllowedTargetTypes().isEmpty()
                    && !folderType.getAllowedTargetTypes().contains(objectType))
            {
                throw new CMISConstraintException("An object of type '" + objectType.getTypeId()
                        + "' can't be a child of a folder of type '" + folderType.getTypeId() + "'");
            }
            QName name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName
                    .createValidLocalName((String) nodeService.getProperty(objectNodeRef, ContentModel.PROP_NAME)));
            nodeService.addChild(parentFolderNodeRef, objectNodeRef, ContentModel.ASSOC_CONTAINS, name);
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#removeObjectFromFolder(java.lang.String, java.lang.String)
     */
    public void removeObjectFromFolder(String objectId, String folderId) throws CMISNotSupportedException,
            CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        try
        {
            if (folderId == null || folderId.length() == 0)
            {
                throw new CMISNotSupportedException(
                        "Unfiling from primary parent folder is not supported. Use deleteObject() instead");
            }
            NodeRef objectNodeRef = getObject(objectId, NodeRef.class, true, false, false);
            NodeRef parentFolderNodeRef = getFolder(folderId);
            if (nodeService.getPrimaryParent(objectNodeRef).getParentRef().equals(parentFolderNodeRef))
            {
                throw new CMISNotSupportedException(
                        "Unfiling from primary parent folder is not supported. Use deleteObject() instead");
            }
            nodeService.removeChild(parentFolderNodeRef, objectNodeRef);
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#moveObject(java.lang.String, java.lang.String, java.lang.String)
     */
    public void moveObject(String objectId, String targetFolderId, String sourceFolderId)
            throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        try
        {
            NodeRef objectNodeRef = getObject(objectId, NodeRef.class, true, false, false);

            NodeRef sourceFolderNodeRef;
            // We have a specific requirement in the spec to throw invalidArgument for missing source folders, rather
            // than objectNotFound
            try
            {
                sourceFolderNodeRef = getFolder(sourceFolderId);
            }
            catch (CMISObjectNotFoundException e)
            {
                throw new CMISInvalidArgumentException(e.getMessage(), e);
            }
            NodeRef targetFolderNodeRef = getFolder(targetFolderId);
            CMISFolderTypeDefinition targetTypeDef = (CMISFolderTypeDefinition) getTypeDefinition(targetFolderNodeRef);

            CMISTypeDefinition objectTypeDef = getTypeDefinition(objectNodeRef);
            if (!targetTypeDef.getAllowedTargetTypes().isEmpty()
                    && !targetTypeDef.getAllowedTargetTypes().contains(objectTypeDef))
            {
                throw new CMISConstraintException("Object with '" + objectTypeDef.getTypeId()
                        + "' Type can't be moved to Folder with '" + targetTypeDef.getTypeId() + "' Type");
            }

            // If this is a primary child node, move it
            ChildAssociationRef primaryParentRef = nodeService.getPrimaryParent(objectNodeRef);
            if (primaryParentRef.getParentRef().equals(sourceFolderNodeRef))
            {
                nodeService.moveNode(objectNodeRef, targetFolderNodeRef, primaryParentRef.getTypeQName(),
                        primaryParentRef.getQName());
            }
            else
            {
                // Otherwise, reparent it
                for (ChildAssociationRef parent : nodeService.getParentAssocs(objectNodeRef,
                        ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
                {
                    if (parent.getParentRef().equals(sourceFolderNodeRef))
                    {
                        nodeService.removeChildAssociation(parent);
                        nodeService.addChild(targetFolderNodeRef, objectNodeRef, ContentModel.ASSOC_CONTAINS, parent
                                .getQName());
                        return;
                    }
                }
                throw new CMISInvalidArgumentException(
                        "The Document is not a Child of the Source Folder that was specified");
            }
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#setContentStream(java.lang.String, org.alfresco.service.namespace.QName,
     * boolean, java.io.InputStream, java.lang.String)
     */
    public boolean setContentStream(String objectId, QName propertyQName, boolean overwriteFlag,
            InputStream contentStream, String mimeType) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISContentAlreadyExistsException, CMISStreamNotSupportedException,
            CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        try
        {
            NodeRef nodeRef = getObject(objectId, NodeRef.class, true, false, false);

            CMISTypeDefinition typeDef = getTypeDefinition(nodeRef);
            if (CMISContentStreamAllowedEnum.NOT_ALLOWED.equals(typeDef.getContentStreamAllowed()))
            {
                throw new CMISStreamNotSupportedException(typeDef);
            }
            // Alfresco extension for setting the content property
            if (propertyQName == null)
            {
                propertyQName = ContentModel.PROP_CONTENT;
            }

            // Determine whether content already exists
            boolean existed = contentService.getReader(nodeRef, propertyQName) != null;
            if (existed && !overwriteFlag)
            {
                throw new CMISContentAlreadyExistsException();
            }

            ContentWriter writer = contentService.getWriter(nodeRef, propertyQName, true);
            writer.guessEncoding();
            writer.setMimetype(mimeType);
            writer.putContent(contentStream);

            return existed;
        }
        catch (AccessDeniedException e)
        {
            throw new CMISPermissionDeniedException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#createPolicy(java.util.Map, java.lang.String, java.util.List)
     */
    public String createPolicy(Map<String, Serializable> properties, String folderId, List<String> policies)
            throws CMISConstraintException, CMISRuntimeException, CMISInvalidArgumentException
    {
        String typeId = (String) properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        if (typeId == null)
        {
            throw new CMISConstraintException("Policy type ID not specified");
        }
        CMISTypeDefinition typeDefinition = getTypeDefinition(typeId);
        if (typeDefinition.getBaseType().getTypeId() != CMISDictionaryModel.POLICY_TYPE_ID)
        {
            throw new CMISConstraintException(typeId + " is not a policy type");
        }
        if (!typeDefinition.isCreatable())
        {
            throw new CMISConstraintException(typeId + " is not a creatable type");
        }
        // Should never get here, as currently no policy types are creatable
        throw new CMISRuntimeException("Internal error");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#applyPolicy(java.lang.String, java.lang.String)
     */
    public void applyPolicy(String policyId, String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        CMISTypeDefinition typeDef = getTypeDefinition(getReadableObject(objectId, Object.class));
        if (!typeDef.isControllablePolicy())
        {
            throw new CMISConstraintException("Type " + typeDef.getTypeId().getId() + " does not allow policies to be applied");
        }
        getReadableObject(policyId, CMISTypeDefinition.class);
        // Expect exception to be throw by now
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#getAppliedPolicies(java.lang.String, java.lang.String)
     */
    public List<CMISTypeDefinition> getAppliedPolicies(String objectId, String filter) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException, CMISFilterNotValidException
    {
        // Get the object
        getReadableObject(objectId, Object.class);
        
        // Parse the filter
        new PropertyFilter(filter);
        
        // Nothing else to do
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISServices#removePolicy(java.lang.String, java.lang.String)
     */
    public void removePolicy(String policyId, String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException
    {
        CMISTypeDefinition typeDef = getTypeDefinition(getReadableObject(objectId, Object.class));
        if (!typeDef.isControllablePolicy())
        {
            throw new CMISConstraintException("Type " + typeDef.getTypeId().getId() + " does not allow policies to be applied");
        }
        getReadableObject(policyId, CMISTypeDefinition.class);
        // Expect exception to be throw by now
    }

    /**
     * Validates that a node is versionable.
     * 
     * @param source
     *            the node
     * @throws CMISConstraintException
     *             if the node is not versionable
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    private void validateVersionable(NodeRef source) throws CMISConstraintException, CMISInvalidArgumentException
    {
        CMISTypeDefinition typeDef = getTypeDefinition(source);
        if (!typeDef.isVersionable())
        {
            throw new CMISConstraintException("Type " + typeDef.getTypeId() + " is not versionable");
        }
    }

    /**
     * Creates a property map for the version service.
     * 
     * @param versionDescription
     *            a version description
     * @param isMajor
     *            is this a major version?
     * @return the property map
     */
    private Map<String, Serializable> createVersionProperties(String versionDescription, boolean isMajor)
    {
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(5);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, isMajor ? VersionType.MAJOR : VersionType.MINOR);
        if (versionDescription != null)
        {
            versionProperties.put(VersionModel.PROP_DESCRIPTION, versionDescription);
        }
        return versionProperties;
    }
}
