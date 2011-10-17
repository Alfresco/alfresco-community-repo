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
package org.alfresco.repo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Provision of Repository Context
 * 
 * @author davidc
 */
public class Repository implements ApplicationContextAware, ApplicationListener, TenantDeployer
{
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();

    // dependencies
    private RetryingTransactionHelper retryingTransactionHelper;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private PersonService personService;
    private AVMService avmService;
    private TenantAdminService tenantAdminService;
    
    // company home
    private StoreRef companyHomeStore;
    private String companyHomePath;
    private Map<String, NodeRef> companyHomeRefs;
    private NodeRef rootRef;
    
    
    /**
     * Sets the Company Home Store
     * 
     * @param companyHomeStore
     */
    public void setCompanyHomeStore(String companyHomeStore)
    {
        this.companyHomeStore = new StoreRef(companyHomeStore);
    }
    
    /**
     * Sets the Company Home Path
     * 
     * @param companyHomePath
     */
    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }

    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * Sets the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Sets the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Sets the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Sets the person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Sets the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Sets the AVM service
     */
    public void setAvmService(AVMService avmService)
    {
    	this.avmService = avmService;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

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
            initContext();
        }
    
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }

    /**
     * Initialise Repository Context
     */
    protected void initContext()
    {
        tenantAdminService.register(this);
        
        if (companyHomeRefs == null)
        {
        	companyHomeRefs = new ConcurrentHashMap<String, NodeRef>(4);
        }
        
        getCompanyHome();
    }
    

    /**
     * Gets the root home of the company home store
     * 
     * @return  root node ref
     */
    public NodeRef getRootHome()
    {
        if (rootRef == null)
        {
            rootRef = nodeService.getRootNode(companyHomeStore);
        }
        return rootRef;
    }

    /**
     * Gets the Company Home
     *  
     * @return  company home node ref
     */
    public NodeRef getCompanyHome()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        NodeRef companyHomeRef = companyHomeRefs.get(tenantDomain);
        if (companyHomeRef == null)
        {
        	companyHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
	        {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Exception
                        {
                            List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(companyHomeStore), companyHomePath, null, namespaceService, false);
                            if (refs.size() != 1)
                            {
                                throw new IllegalStateException("Invalid company home path: " + companyHomePath + " - found: " + refs.size());
                                }
                                return refs.get(0);
                            }
                        }, true);
                    }
                }, AuthenticationUtil.getSystemUserName());
        	companyHomeRefs.put(tenantDomain, companyHomeRef);
        }
        return companyHomeRef;
    }

    /**
     * Gets the currently authenticated person
     * 
     * @return  person node ref
     */
    public NodeRef getPerson()
    {
        NodeRef person = null;
        String currentUserName = AuthenticationUtil.getRunAsUser();
        if (personService.personExists(currentUserName))
        {
            person = personService.getPerson(currentUserName);
        }
        return person;
    }

    /**
     * Gets the user home of the currently authenticated person
     * 
     * @param person  person
     * @return  user home of person
     */
    public NodeRef getUserHome(NodeRef person)
    {
        NodeRef homeFolderRef = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
        if (homeFolderRef != null && nodeService.exists(homeFolderRef))
        {
            return homeFolderRef;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Helper to convert a Web Script Request URL to a Node Ref
     * <p/>
     * 1) Node - {store_type}/{store_id}/{node_id} 
     * <br/>
     *    Resolve to node via its Node Reference.
     * <br/>
     * 2) Path - {store_type}/{store_id}/{path}
     * <br/>
     *    Resolve to node via its display path.
     * <br/>
     * 3) AVM Path - {store_id}/{path}
     * <br/>
     *    Resolve to AVM node via its display path
     * <br/>
     * 4) QName - {store_type}/{store_id}/{child_qname_path}  TODO: Implement
     * <br/>
     *    Resolve to node via its child qname path.
     * 
     * @param  referenceType  one of node, path, avmpath or qname
     * @return  reference  array of reference segments (as described above for each reference type)
     */
    public NodeRef findNodeRef(String referenceType, String[] reference)
    {
        NodeRef nodeRef = null;
        
        if (referenceType.equals("avmpath"))
        {
            if (reference.length == 0)
            {
                throw new AlfrescoRuntimeException("Reference " + Arrays.toString(reference) + " is not properly formed");
            }
            String path = reference[0] + ":/";
            if (reference.length > 1)
            {
                Object[] pathElements = ArrayUtils.subarray(reference, 1, reference.length);
                path += StringUtils.join(pathElements, "/");
            }
            AVMNodeDescriptor nodeDesc = avmService.lookup(-1, path);
            if (nodeDesc != null)
            {
                nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
            }
        }
        else
        {
            // construct store reference
            if (reference.length < 3)
            {
                throw new AlfrescoRuntimeException("Reference " + Arrays.toString(reference) + " is not properly formed");
            }
            StoreRef storeRef = new StoreRef(reference[0], reference[1]);
            if (nodeService.exists(storeRef))
            {
                if (referenceType.equals("node"))
                {
                    // find the node the rest of the path is relative to
                    NodeRef relRef = new NodeRef(storeRef, reference[2]);
                    if (nodeService.exists(relRef))
                    {
                        // are there any relative path elements to process?
                        if (reference.length == 3 || reference.length == 4)
                        {
                            // just the NodeRef can be specified
                            nodeRef = relRef;
                        }
                        else
                        {
                            // process optional path elements
                            List<String> paths = new ArrayList<String>(reference.length - 3);
                            for (int i=3; i<reference.length; i++)
                            {
                                paths.add(reference[i]);
                            }
    
                            try
                            {
                                NodeRef parentRef = nodeService.getPrimaryParent(relRef).getParentRef();
                                FileInfo fileInfo = fileFolderService.resolveNamePath(parentRef, paths);
                                nodeRef = fileInfo.getNodeRef();
                            }
                            catch (FileNotFoundException e)
                            {
                                // NOTE: return null node ref
                            }
                        }
                    }
                }
                
                else if (referenceType.equals("path"))
                {
                    // NOTE: special case for avm based path
                    if (reference[0].equals(StoreRef.PROTOCOL_AVM))
                    {
                        String path = reference[1] + ":/";
                        if (reference.length > 2)
                        {
                            Object[] pathElements = ArrayUtils.subarray(reference, 2, reference.length);
                            path += StringUtils.join(pathElements, "/");
                        }
                        AVMNodeDescriptor nodeDesc = avmService.lookup(-1, path);
                        if (nodeDesc != null)
                        {
                            nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
                        }
                    }
                    else
                    {
                        // TODO: Allow a root path to be specified - for now, hard-code to Company Home
                        //NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                        NodeRef rootNodeRef = getCompanyHome();
                        if (reference.length == 3)
                        {
                            if (reference[2].equals(nodeService.getPrimaryParent(rootNodeRef).getQName().toPrefixString(namespaceService)))
                            {
                                nodeRef = rootNodeRef;
                            }
                        }
                        else
                        {
                            String[] path = new String[reference.length - /*2*/3];
                            System.arraycopy(reference, /*2*/3, path, 0, path.length);
                            
                            try
                            {
                                FileInfo fileInfo = fileFolderService.resolveNamePath(rootNodeRef, Arrays.asList(path));
                                nodeRef = fileInfo.getNodeRef();
                            }
                            catch (FileNotFoundException e)
                            {
                                // NOTE: return null node ref
                            }
                        }
                    }
                }
                
                else
                {
                    // TODO: Implement 'qname' style
                    throw new AlfrescoRuntimeException("Web Script Node URL specified an invalid reference style of '" + referenceType + "'");
                }
            }
        }
        
        return nodeRef;
    }    
    
    public void onEnableTenant()
    {
        init();
    }
    
    public void onDisableTenant()
    {
        destroy();
    }
    
    public void init()
    {
        initContext();
    }
    
    public void destroy()
    {
        companyHomeRefs.remove(tenantAdminService.getCurrentUserDomain());
    }
}
