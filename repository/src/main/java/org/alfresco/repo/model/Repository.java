/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Provision of Repository Context
 * 
 * @author davidc
 */
public class Repository implements ApplicationContextAware
{
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();

    // dependencies
    private RetryingTransactionHelper retryingTransactionHelper;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private PersonService personService;

    // company home
    private StoreRef companyHomeStore; // ie. workspace://SpaceStore
    private String companyHomePath; // ie. /app:company_home
    private String sharedHomePath; // ie. /app:shared

    private String guestHomePath; // /app:company_home/app:guest_home

    // note: cache is tenant-aware (if using EhCacheAdapter shared cache)
    private SimpleCache<String, NodeRef> singletonCache; // eg. for companyHomeNodeRef
    private final String KEY_COMPANYHOME_NODEREF = "key.companyhome.noderef";
    private final String KEY_GUESTHOME_NODEREF = "key.guesthome.noderef";
    private final String KEY_SHAREDHOME_NODEREF = "key.sharedhome.noderef";

    /**
     * Sets the Company Home Store
     * 
     * @param companyHomeStore
     *            String
     */
    public void setCompanyHomeStore(String companyHomeStore)
    {
        this.companyHomeStore = new StoreRef(companyHomeStore);
    }

    /**
     * Sets the Company Home Path
     * 
     * @param companyHomePath
     *            String
     */
    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }

    /**
     * Sets the Shared Home Path
     * 
     * @param sharedHomePath
     *            String
     */
    public void setSharedHomePath(String sharedHomePath)
    {
        this.sharedHomePath = sharedHomePath;
    }

    public void setSingletonCache(SimpleCache<String, NodeRef> singletonCache)
    {
        this.singletonCache = singletonCache;
    }

    public void setGuestHomePath(String guestHomePath)
    {
        this.guestHomePath = guestHomePath;
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
            // NOOP
        }
    }

    /**
     * Initialise Repository Context
     */
    protected void initContext()
    {
        // NOOP
    }

    /**
     * Gets the root home of the company home store
     * 
     * @return root node ref
     */
    public NodeRef getRootHome()
    {
        // note: store root nodes are cached by the NodeDAO
        return nodeService.getRootNode(companyHomeStore);
    }

    /**
     * Gets the Company Home. Note this is tenant-aware if the correct Cache is supplied.
     * 
     * @return company home node ref
     */
    public NodeRef getCompanyHome()
    {
        NodeRef companyHomeRef = singletonCache.get(KEY_COMPANYHOME_NODEREF);
        if (companyHomeRef == null)
        {
            companyHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
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

            singletonCache.put(KEY_COMPANYHOME_NODEREF, companyHomeRef);
        }
        return companyHomeRef;
    }

    /**
     * Gets the Guest Home. Note this is tenant-aware if the correct Cache is supplied.
     *
     * @return guest home node ref
     */
    public NodeRef getGuestHome()
    {
        NodeRef guestHomeRef = singletonCache.get(KEY_GUESTHOME_NODEREF);
        if (guestHomeRef == null)
        {
            guestHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
                        public NodeRef execute() throws Exception
                        {
                            List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(companyHomeStore), guestHomePath, null, namespaceService, false);
                            if (refs.size() != 1)
                            {
                                throw new IllegalStateException("Invalid guest home path: " + guestHomePath + " - found: " + refs.size());
                            }
                            return refs.get(0);
                        }
                    }, true);
                }
            }, AuthenticationUtil.getSystemUserName());

            singletonCache.put(KEY_COMPANYHOME_NODEREF, guestHomeRef);
        }
        return guestHomeRef;
    }

    /**
     * Gets the Shared Home. Note this is tenant-aware if the correct Cache is supplied.
     * 
     * @return shared home node ref
     */
    public NodeRef getSharedHome()
    {
        NodeRef sharedHomeRef = singletonCache.get(KEY_SHAREDHOME_NODEREF);
        if (sharedHomeRef == null)
        {
            sharedHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
                        public NodeRef execute() throws Exception
                        {
                            List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(companyHomeStore), sharedHomePath, null, namespaceService, false);
                            if (refs.size() != 1)
                            {
                                throw new IllegalStateException("Invalid shared home path: " + sharedHomePath + " - found: " + refs.size());
                            }
                            return refs.get(0);
                        }
                    }, true);
                }
            }, AuthenticationUtil.getSystemUserName());

            singletonCache.put(KEY_SHAREDHOME_NODEREF, sharedHomeRef);
        }
        return sharedHomeRef;
    }

    /**
     * Gets the currently authenticated person Includes any overlay authentication set by runas
     * 
     * @return person node ref
     */
    public NodeRef getPerson()
    {
        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef person = null;
                String currentUserName = AuthenticationUtil.getRunAsUser();
                if (currentUserName != null)
                {
                    if (personService.personExists(currentUserName))
                    {
                        person = personService.getPerson(currentUserName);
                    }
                }
                return person;
            }
        };
        return retryingTransactionHelper.doInTransaction(callback, true);
    }

    /**
     * Gets the currently fully authenticated person, Excludes any overlay authentication set by runas
     * 
     * @return person node ref
     */
    public NodeRef getFullyAuthenticatedPerson()
    {
        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef person = null;
                String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
                if (currentUserName != null)
                {
                    if (personService.personExists(currentUserName))
                    {
                        person = personService.getPerson(currentUserName);
                    }
                }
                return person;
            }
        };
        return retryingTransactionHelper.doInTransaction(callback, true);
    }

    /**
     * Gets the user home of the currently authenticated person
     * 
     * @param person
     *            person
     * @return user home of person
     */
    public NodeRef getUserHome(final NodeRef person)
    {
        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef homeFolderRef = (NodeRef) nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
                if (homeFolderRef != null && nodeService.exists(homeFolderRef))
                {
                    return homeFolderRef;
                }
                else
                {
                    return null;
                }
            }
        };
        return retryingTransactionHelper.doInTransaction(callback, true);
    }

    /**
     * Helper to convert a Web Script Request URL to a Node Ref
     * <p/>
     * 1) Node - {store_type}/{store_id}/{node_id} <br/>
     * Resolve to node via its Node Reference. <br/>
     * 2) Path - {store_type}/{store_id}/{path} <br/>
     * Resolve to node via its display path. <br/>
     * 3) QName - {store_type}/{store_id}/{child_qname_path} TODO: Implement <br/>
     * Resolve to node via its child qname path.
     * 
     * @param referenceType
     *            one of node, path or qname
     * @return reference array of reference segments (as described above for each reference type)
     */
    public NodeRef findNodeRef(String referenceType, String[] reference)
    {
        NodeRef nodeRef = null;

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
                        for (int i = 3; i < reference.length; i++)
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
                // TODO: Allow a root path to be specified - for now, hard-code to Company Home
                // NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
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
                    String[] path = new String[reference.length - /* 2 */3];
                    System.arraycopy(reference, /* 2 */3, path, 0, path.length);

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

            else
            {
                // TODO: Implement 'qname' style
                throw new AlfrescoRuntimeException("Web Script Node URL specified an invalid reference style of '" + referenceType + "'");
            }
        }

        return nodeRef;
    }
}
