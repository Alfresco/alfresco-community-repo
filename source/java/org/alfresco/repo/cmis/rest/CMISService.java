/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantDeployerService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.Repository;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;


/**
 * CMIS Navigation Service
 * 
 * @author davidc
 */
public class CMISService implements ApplicationContextAware, ApplicationListener, TenantDeployer
{
    /**
     * Types Filter
     *  
     * @author davidc
     */
    public enum TypesFilter
    {
        Folders,
        FoldersAndDocuments,
        Documents
    };
        
    /** Query Parameters */
    private static final QName PARAM_PARENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parent");
    private static final QName PARAM_USERNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "username");

    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_FOLDERS =
        "+PARENT:\"${cm:parent}\" " +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+TYPE:\"" + ContentModel.TYPE_FOLDER + "\" ";
    
    /** Shallow search for all files and folders */
    private static final String LUCENE_QUERY_SHALLOW_FILES =
        "+PARENT:\"${cm:parent}\" " +
        "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\" " +
        "+TYPE:\"" + ContentModel.TYPE_CONTENT + "\" ";

    private static final String LUCENE_QUERY_CHECKEDOUT =
        "+@cm\\:workingCopyOwner:${cm:username}";
    
    private static final String LUCENE_QUERY_CHECKEDOUT_IN_FOLDER =
        "+@cm\\:workingCopyOwner:${cm:username} " +
        "+PARENT:\"${cm:parent}\"";
        
    
    // dependencies
    private Repository repository;
    private RetryingTransactionHelper retryingTransactionHelper;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private NodeService nodeService;
    private TenantDeployerService tenantDeployerService;
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();

    // default CMIS store and path
    private String defaultRootPath;
    private Map<String, NodeRef> defaultRootNodeRefs;
    
    // data types for query
    private DataTypeDefinition nodeRefDataType;
    private DataTypeDefinition textDataType;

    
    /**
     * Sets the default root path
     * 
     * store_type/store_id/path...
     * 
     * @param path  store_type/store_id/path...
     */
    public void setDefaultRootPath(String path)
    {
        defaultRootPath = path;
    }
    
    /**
     * Sets the tenant deployer service
     * 
     * @param tenantDeployerService
     */
    public void setTenantDeployerService(TenantDeployerService tenantDeployerService)
    {
        this.tenantDeployerService = tenantDeployerService;
    }

    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
     * @param repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
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
    public void onApplicationEvent(ApplicationEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * Hooks into Spring Application Lifecycle
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            init();
        }
    
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
        tenantDeployerService.register(this);
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
        defaultRootNodeRefs.remove(tenantDeployerService.getCurrentUserDomain());
    }

    /**
     * Gets the default root node path
     * 
     * @return  root node path
     */
    public String getDefaultRootPath()
    {
        return defaultRootPath;
    }
    
    /**
     * Gets the default root node ref
     *  
     * @return  root node ref
     */
    public NodeRef getDefaultRootNodeRef()
    {
        String tenantDomain = tenantDeployerService.getCurrentUserDomain();
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
                            return repository.findNodeRef("path", defaultRootPath.split("/"));
                        };
                    });
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

    /**
     * Gets the default store ref
     * 
     * @return  store ref
     */
    public StoreRef getDefaultRootStoreRef()
    {
        return getDefaultRootNodeRef().getStoreRef();
    }
    
    /**
     * Query for node children
     * 
     * @param parent  node to query children for
     * @param typesFilter  types filter
     * @return  children of node
     */
    public NodeRef[] getChildren(NodeRef parent, TypesFilter typesFilter)
    {
        if (typesFilter == TypesFilter.FoldersAndDocuments)
        {
            NodeRef[] folders = queryChildren(parent, TypesFilter.Folders);
            NodeRef[] docs = queryChildren(parent, TypesFilter.Documents);
            NodeRef[] foldersAndDocs = new NodeRef[folders.length + docs.length];
            System.arraycopy(folders, 0, foldersAndDocs, 0, folders.length);
            System.arraycopy(docs, 0, foldersAndDocs, folders.length, docs.length);
            return foldersAndDocs;
        }
        else if (typesFilter == TypesFilter.Folders)
        {
            NodeRef[] folders = queryChildren(parent, TypesFilter.Folders);
            return folders;
        }
        else if (typesFilter == TypesFilter.Documents)
        {
            NodeRef[] docs = queryChildren(parent, TypesFilter.Documents);
            return docs;
        }
        
        return new NodeRef[0];
    }

    /**
     * Query for checked out items
     *  
     * @param username  for user
     * @param folder  (optional) within folder
     * @param includeDescendants  true => include descendants of folder, false => only children of folder
     * @return  checked out items
     */
    public NodeRef[] getCheckedOut(String username, NodeRef folder, boolean includeDescendants)
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
        
        ResultSet resultSet = searchService.query(params);
        try
        {
            List<NodeRef> results = resultSet.getNodeRefs();
            NodeRef[] nodeRefs = new NodeRef[results.size()];
            return results.toArray(nodeRefs);
        }
        finally
        {
            resultSet.close();
        }
    }

    /**
     * Query children helper
     * 
     * NOTE: Queries for folders only or documents only
     * 
     * @param parent  node to query children for
     * @param typesFilter  folders or documents
     * @return  node children
     */
    private NodeRef[] queryChildren(NodeRef parent, TypesFilter typesFilter)
    {
        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(parent.getStoreRef());
        QueryParameterDefinition parentDef = new QueryParameterDefImpl(PARAM_PARENT, nodeRefDataType, true, parent.toString());
        params.addQueryParameterDefinition(parentDef);
        
        if (typesFilter == TypesFilter.Folders)
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FOLDERS);
        }
        else if (typesFilter == TypesFilter.Documents)
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FILES);
        }
        
        ResultSet resultSet = searchService.query(params);
        try
        {
            List<NodeRef> results = resultSet.getNodeRefs();
            NodeRef[] nodeRefs = new NodeRef[results.size()];
            return results.toArray(nodeRefs);
        }
        finally
        {
            resultSet.close();
        }
    }
    
}
