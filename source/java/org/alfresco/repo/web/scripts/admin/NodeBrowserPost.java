/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * Admin Console NodeBrowser WebScript POST controller.
 * <p>
 * Implements a low-level node browser client for the Admin Console tool.
 * 
 * @author Kevin Roast
 * @since 5.1
 */
public class NodeBrowserPost extends DeclarativeWebScript implements Serializable
{
    private static final long serialVersionUID = 8464392337270665212L;

    // stores and node
    transient private List<StoreRef> stores = null;

    // supporting repository services
    transient private TransactionService transactionService;
    transient private NodeService nodeService;
    transient private DictionaryService dictionaryService;
    transient private SearchService searchService;
    transient private NamespaceService namespaceService;
    transient private PermissionService permissionService;
    transient private OwnableService ownableService;
    transient private LockService lockService;
    transient private CheckOutCheckInService cociService;

    /**
     * @param transactionService        transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    protected TransactionService getTransactionService()
    {
        return transactionService;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    protected NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * @param searchService search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    protected SearchService getSearchService()
    {
        return searchService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    protected DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * @param namespaceService namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    protected NamespaceService getNamespaceService()
    {
        return this.namespaceService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    protected PermissionService getPermissionService()
    {
        return permissionService;
    }

    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    protected OwnableService getOwnableService()
    {
        return ownableService;
    }
    
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }
    
    protected LockService getLockService()
    {
        return this.lockService;
    }
    
    public void setCheckOutCheckInService(CheckOutCheckInService cociService)
    {
        this.cociService = cociService;
    }
    
    protected CheckOutCheckInService getCheckOutCheckInService()
    {
        return this.cociService;
    }

    /**
     * Gets the list of repository stores
     * 
     * @return stores
     */
    public List<StoreRef> getStores()
    {
        if (stores == null)
        {
            stores = getNodeService().getStores();
        }
        return stores;
    }

    /**
     * Gets the current node type
     * 
     * @return node type
     */
    public QName getNodeType(NodeRef nodeRef)
    {
        return getNodeService().getType(nodeRef);
    }

    /**
     * Gets the current node primary path
     * 
     * @return primary path
     */
    public String getPrimaryPath(NodeRef nodeRef)
    {
        Path primaryPath = getNodeService().getPath(nodeRef);
        return ISO9075.decode(primaryPath.toString());
    }

    /**
     * Gets the current node primary path
     * 
     * @return primary path
     */
    public String getPrimaryPrefixedPath(NodeRef nodeRef)
    {
        Path primaryPath = getNodeService().getPath(nodeRef);
        return ISO9075.decode(primaryPath.toPrefixString(getNamespaceService()));
    }

    /**
     * Gets the current node primary parent reference
     * 
     * @return primary parent ref
     */
    public NodeRef getPrimaryParent(NodeRef nodeRef)
    {
        Path primaryPath = getNodeService().getPath(nodeRef);
        Path.Element element = primaryPath.last();
        NodeRef parentRef = ((Path.ChildAssocElement) element).getRef().getParentRef();
        return parentRef;
    }

    /**
     * Gets the current node aspects
     * 
     * @return node aspects
     */
    public List<Aspect> getAspects(NodeRef nodeRef)
    {
        Set<QName> qnames = getNodeService().getAspects(nodeRef);
        List<Aspect> aspects = new ArrayList<Aspect>(qnames.size());
        for (QName qname : qnames)
        {
            aspects.add(new Aspect(qname));
        }
        return aspects;
    }

    /**
     * Gets the current node parents
     * 
     * @return node parents
     */
    public List<ChildAssociation> getParents(NodeRef nodeRef)
    {
        List<ChildAssociationRef> parents = getNodeService().getParentAssocs(nodeRef);
        List<ChildAssociation> assocs = new ArrayList<ChildAssociation>(parents.size());
        for (ChildAssociationRef ref : parents)
        {
            assocs.add(new ChildAssociation(ref));
        }
        return assocs;
    }

    /**
     * Gets the current node properties
     * 
     * @return properties
     */
    public List<Property> getProperties(NodeRef nodeRef)
    {
        Map<QName, Serializable> propertyValues = getNodeService().getProperties(nodeRef);
        List<Property> properties = new ArrayList<Property>(propertyValues.size());
        for (Map.Entry<QName, Serializable> property : propertyValues.entrySet())
        {
            properties.add(new Property(property.getKey(), property.getValue()));
        }
        return properties;
    }

    /**
     * Gets whether the current node inherits its permissions from a parent node
     * 
     * @return true => inherits permissions
     */
    public boolean getInheritPermissions(NodeRef nodeRef)
    {
        Boolean inheritPermissions = this.getPermissionService().getInheritParentPermissions(nodeRef);
        return inheritPermissions.booleanValue();
    }

    /**
     * Gets the current node permissions
     * 
     * @return the permissions
     */
    public List<Permission> getPermissions(NodeRef nodeRef)
    {
        List<Permission> permissions = null;
        AccessStatus readPermissions = this.getPermissionService().hasPermission(nodeRef, PermissionService.READ_PERMISSIONS);
        if (readPermissions.equals(AccessStatus.ALLOWED))
        {
            List<Permission> nodePermissions = new ArrayList<Permission>();
            for (Iterator<AccessPermission> iterator = getPermissionService().getAllSetPermissions(nodeRef).iterator(); iterator
                    .hasNext();)
            {
                AccessPermission ap = iterator.next();
                nodePermissions.add(new Permission(ap.getPermission(), ap.getAuthority(), ap.getAccessStatus().toString()));
            }
            permissions = nodePermissions;
        }
        else
        {
            List<Permission> noReadPermissions = new ArrayList<Permission>(1);
            noReadPermissions.add(new NoReadPermissionGranted());
            permissions = noReadPermissions;
        }
        return permissions;
    }

    /**
     * Gets the current node permissions
     * 
     * @return the permissions
     */
    public List<Permission> getStorePermissionMasks(NodeRef nodeRef)
    {
        List<Permission> permissionMasks = new ArrayList<Permission>(1);
        permissionMasks.add(new NoStoreMask());
        return permissionMasks;
    }

    /**
     * Gets the current node children
     * 
     * @return node children
     */
    public List<ChildAssociation> getChildren(NodeRef nodeRef)
    {
        List<ChildAssociationRef> refs = getNodeService().getChildAssocs(nodeRef);
        List<ChildAssociation> assocs = new ArrayList<ChildAssociation>(refs.size());
        for (ChildAssociationRef ref : refs)
        {
            assocs.add(new ChildAssociation(ref));
        }
        return assocs;
    }

    /**
     * Gets the current node associations
     * 
     * @return associations
     */
    public List<PeerAssociation> getAssocs(NodeRef nodeRef)
    {
        List<AssociationRef> refs = null;
        try
        {
            refs = getNodeService().getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        }
        catch (UnsupportedOperationException err)
        {
           // some stores do not support associations
           // but we doesn't want NPE in code below
           refs = new ArrayList<AssociationRef>();
        }
        List<PeerAssociation> assocs = new ArrayList<PeerAssociation>(refs.size());
        for (AssociationRef ref : refs)
        {
            assocs.add(new PeerAssociation(ref.getTypeQName(), ref.getSourceRef(), ref.getTargetRef()));
        }
        return assocs;
    }

    /**
     * Gets the current source associations
     * 
     * @return associations
     */
    public List<PeerAssociation> getSourceAssocs(NodeRef nodeRef)
    {
        List<AssociationRef> refs = null;
        try
        {
            refs = getNodeService().getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        }
        catch (UnsupportedOperationException err)
        {
           // some stores do not support associations
           // but we doesn't want NPE in code below
           refs = new ArrayList<AssociationRef>(); 
        }
        List<PeerAssociation> assocs = new ArrayList<PeerAssociation>(refs.size());
        for (AssociationRef ref : refs)
        {
            assocs.add(new PeerAssociation(ref.getTypeQName(), ref.getSourceRef(), ref.getTargetRef()));
        }
        return assocs;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> result = new HashMap<>(16);
        
        // gather inputs
        Map<String, String> returnParams = new HashMap<>(16);
        String store        = req.getParameter("nodebrowser-store");
        String searcher     = req.getParameter("nodebrowser-search");
        String query        = req.getParameter("nodebrowser-query");
        String maxResults   = req.getParameter("nodebrowser-query-maxresults");
        String skipCount    = req.getParameter("nodebrowser-query-skipcount");
        String error = null;
        
        StoreRef storeRef = new StoreRef(store);
        
        // always a list of assoc refs from some result
        List<ChildAssociationRef> assocRefs = Collections.<ChildAssociationRef>emptyList();
        NodeRef currentNode = null;
        
        // what action should be processed?
        long timeStart = System.currentTimeMillis();
        String actionValue = req.getParameter("nodebrowser-action-value");
        String action = req.getParameter("nodebrowser-action");
        final String execute = req.getParameter("nodebrowser-execute");
        final String executeValue = req.getParameter("nodebrowser-execute-value");
        String message = null;
        try
        {
            // 'execute' is an action that performs an operation on a node e.g. delete
            // the 'executeValue' param provides context
            // this is done before the view action to ensure node state is correct
            if (execute != null)
            {
                switch (execute)
                {
                    case "delete":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                // delete the node using the standard NodeService
                                nodeService.deleteNode(new NodeRef(executeValue));
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.delete";
                        break;
                    }
                    case "fdelete":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                // delete the node - but ensure that it is not archived
                                NodeRef ref = new NodeRef(executeValue);
                                nodeService.addAspect(ref, ContentModel.ASPECT_TEMPORARY, null);
                                nodeService.deleteNode(ref);
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.delete";
                        break;
                    }
                    case "restore":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                nodeService.restoreNode(new NodeRef(executeValue), null, null, null);
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.restore";
                        break;
                    }
                    case "take-ownership":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                ownableService.takeOwnership(new NodeRef(executeValue));
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.take-ownership";
                        break;
                    }
                    case "delete-permissions":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                NodeRef ref = new NodeRef(executeValue);
                                permissionService.deletePermissions(ref);
                                permissionService.setInheritParentPermissions(ref, true);
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.delete-permissions";
                        break;
                    }
                    case "delete-property":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                // argument value contains "NodeRef|QName" packed string
                                String[] parts = executeValue.split("\\|");
                                nodeService.removeProperty(new NodeRef(parts[0]), QName.createQName(parts[1]));
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.delete-property";
                        break;
                    }
                    case "unlock":
                    {
                        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                NodeRef ref = new NodeRef(executeValue);
                                if (cociService.isCheckedOut(ref))
                                {
                                    NodeRef wcRef = cociService.getWorkingCopy(ref);
                                    if (wcRef != null)
                                    {
                                        cociService.cancelCheckout(wcRef);
                                    }
                                }
                                else
                                {
                                    lockService.unlock(ref);
                                }
                                return null;
                            }
                        }, false, true);
                        message = "nodebrowser.message.unlock";
                        break;
                    }
                }
            }
            
            // 'action' is a view action that request an update of the admin console view state e.g. 'search' or 'children'
            // the 'actionValue' param provides context as may other parameters such as 'query'
            switch (action)
            {
                // on Execute btn press and query present, perform search
                case "search":
                {
                    if (query != null && query.trim().length() != 0)
                    {
                        switch (searcher)
                        {
                            case "noderef":
                            {
                                // ensure node exists - or throw error
                                NodeRef nodeRef = new NodeRef(query);
                                boolean exists = getNodeService().exists(nodeRef);
                                if (!exists)
                                {
                                    throw new AlfrescoRuntimeException("Node " + nodeRef + " does not exist.");
                                }
                                currentNode = nodeRef;
                                // this is not really a search for results, it is a direct node reference
                                // so gather the child assocs as usual and update the action value for the UI location
                                assocRefs = getNodeService().getChildAssocs(currentNode);
                                actionValue = query;
                                action = "parent";
                                break;
                            }
                            case "selectnodes":
                            {
                                List<NodeRef> nodes = getSearchService().selectNodes(
                                        getNodeService().getRootNode(storeRef), query, null, getNamespaceService(), false);
                                assocRefs = new ArrayList<>(nodes.size());
                                for (NodeRef node: nodes)
                                {
                                    assocRefs.add(getNodeService().getPrimaryParent(node));
                                }
                                break;
                            }
                            default:
                            {
                                // perform search
                                SearchParameters params = new SearchParameters();
                                params.setQuery(query);
                                params.addStore(storeRef);
                                params.setLanguage(searcher);
                                if (maxResults != null && maxResults.length() != 0)
                                {
                                    params.setMaxItems(Integer.parseInt(maxResults));
                                    params.setLimit(Integer.parseInt(maxResults));
                                }
                                if (skipCount != null && skipCount.length() != 0)
                                {
                                    params.setSkipCount(Integer.parseInt(skipCount));
                                }
                                ResultSet rs = getSearchService().query(params);
                                assocRefs = rs.getChildAssocRefs();
                                break;
                            }
                        }
                    }
                    break;
                }
                case "root":
                {
                    // iterate the properties and children of a store root node
                    currentNode = getNodeService().getRootNode(storeRef);
                    assocRefs = getNodeService().getChildAssocs(currentNode);
                    break;
                }
                case "parent":
                case "children":
                {
                    currentNode = new NodeRef(actionValue);
                    assocRefs = getNodeService().getChildAssocs(currentNode);
                    break;
                }
            }
            
            // get the required information from the assocRefs list and wrap objects
            List<ChildAssocRefWrapper> wrappers = new ArrayList<>(assocRefs.size());
            for (ChildAssociationRef ref : assocRefs)
            {
                wrappers.add(new ChildAssocRefWrapper(ref));
            }
            result.put("children", wrappers);
        }
        catch (Throwable e)
        {
            // empty child list on error - current node will still be null
            result.put("children", new ArrayList<>(0));
            error = e.getMessage();
        }
        
        // current node info if any
        if (currentNode != null)
        {
            // node info
            Map<String, Object> info = new HashMap<>(8);
            info.put("nodeRef", currentNode.toString());
            info.put("path", getNodeService().getPath(currentNode).toPrefixString(getNamespaceService()));
            info.put("type", getNodeService().getType(currentNode).toPrefixString(getNamespaceService()));
            ChildAssociationRef parent = getNodeService().getPrimaryParent(currentNode);
            info.put("parent", parent.getParentRef() != null ? parent.getParentRef().toString() : "");
            result.put("info", info);
            
            // node properties
            result.put("properties", getProperties(currentNode));
            
            // parents
            List<ChildAssociationRef> parents = getNodeService().getParentAssocs(currentNode);
            List<ChildAssociation> assocs = new ArrayList<ChildAssociation>(parents.size());
            for (ChildAssociationRef ref : parents)
            {
                assocs.add(new ChildAssociation(ref));
            }
            result.put("parents", assocs);
            
            // aspects
            List<Aspect> aspects = getAspects(currentNode);
            result.put("aspects", aspects);
            
            // target assocs
            List<PeerAssociation> targetAssocs = getAssocs(currentNode);
            result.put("assocs", targetAssocs);
            
            // source assocs
            List<PeerAssociation> sourceAssocs = getSourceAssocs(currentNode);
            result.put("sourceAssocs", sourceAssocs);
            
            // permissions
            Map<String, Object> permissionInfo = new HashMap<String, Object>();
            permissionInfo.put("entries", getPermissions(currentNode));
            permissionInfo.put("owner", getOwnableService().getOwner(currentNode));
            permissionInfo.put("inherit", getInheritPermissions(currentNode));
            result.put("permissions", permissionInfo);
        }
        
        // store result in session for the resulting GET request webscript
        final String resultId = GUID.generate();
        HttpServletRequest request = ((WebScriptServletRequest)req).getHttpServletRequest();
        HttpSession session = request.getSession();
        session.putValue(resultId, result);
        
        // return params
        returnParams.put("resultId", resultId);
        returnParams.put("action", action);
        returnParams.put("actionValue", actionValue);
        returnParams.put("query", query);
        returnParams.put("store", store);
        returnParams.put("searcher", searcher);
        returnParams.put("maxResults", maxResults);
        returnParams.put("skipCount", skipCount);
        returnParams.put("in", Long.toString(System.currentTimeMillis()-timeStart));
        returnParams.put("e", error);
        returnParams.put("m", message);
        
        // redirect as all admin console pages do (follow standard pattern)
        // The logic to generate the navigation section and server meta-data is all tied into alfresco-common.lib.js
        // which is great for writing JS based JMX surfaced pages, but not so great for Java backed WebScripts. 
        status.setCode(301);
        status.setRedirect(true);
        status.setLocation(buildUrl(req, returnParams, execute != null && execute.length() != 0 ? execute : action));
        
        return null;
    }
    
    private static String buildUrl(WebScriptRequest req, Map<String, String> params, String hash)
    {
        StringBuilder url = new StringBuilder(256);
        
        url.append(req.getServicePath());
        if (!params.isEmpty())
        {
            boolean first = true;
            for (String key: params.keySet())
            {
                String val = params.get(key);
                if (val != null && val.length() != 0)
                {
                    url.append(first ? '?' : '&');
                    url.append(key);
                    url.append('=');
                    url.append(URLEncoder.encode(val));
                    first = false;
                }
            }
        }
        if (hash != null && hash.length() != 0)
        {
            url.append('#').append(hash);
        }
        
        return url.toString();
    }
    
    /**
     * Node wrapper class
     */
    public class Node implements Serializable
    {
        private static final long serialVersionUID = 12608347204513848L;

        private String qnamePath;
        
        private String prefixedQNamePath;
        
        private NodeRef nodeRef;
        
        private NodeRef parentNodeRef;
        
        private QNameBean childAssoc;
        
        private QNameBean type;
        
        public Node(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
            Path path = getNodeService().getPath(nodeRef);
            this.qnamePath = path.toString();
            this.prefixedQNamePath = path.toPrefixString(getNamespaceService());
            this.parentNodeRef = getPrimaryParent(nodeRef);
            ChildAssociationRef ref = getNodeService().getPrimaryParent(nodeRef);
            this.childAssoc = ref.getQName() != null ? new QNameBean(ref.getQName()) : null;
            this.type = new QNameBean(getNodeService().getType(nodeRef));
        }

        public String getQnamePath()
        {
            return qnamePath;
        }

        public String getPrefixedQNamePath()
        {
            return prefixedQNamePath;
        }

        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        public String getId()
        {
            return nodeRef.getId();
        }

        public String getName()
        {
            return childAssoc != null ? childAssoc.getName() : "";
        }

        public String getPrefixedName()
        {
            return childAssoc != null ? childAssoc.getPrefixedName() : "";
        }

        public QNameBean getType()
        {
            return type;
        }

        public void setNodeRef(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }

        public NodeRef getParentNodeRef()
        {
            return parentNodeRef;
        }

        public void setParentNodeRef(NodeRef parentNodeRef)
        {
            this.parentNodeRef = parentNodeRef;
        }
    }

    /**
     * Qname wrapper class
     */
    public class QNameBean implements Serializable
    {
        private static final long serialVersionUID = 6982292337846270774L;
        
        protected QName name;
        private String prefixString = null;

        public QNameBean(QName name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return name.toString();
        }
        
        public String getPrefixedName()
        {
            try
            {
                return prefixString != null ? prefixString : (prefixString = name.toPrefixString(getNamespaceService()));
            }
            catch(NamespaceException e)
            {
                return name.getLocalName();
            }
        }
        
        public String toString()
        {
            return getName();
        }
    }

    /**
     * Aspect wrapper class
     */
    public class Aspect extends QNameBean implements Serializable
    {
        private static final long serialVersionUID = -6448182941386934326L;

        public Aspect(QName name)
        {
            super(name);
        }
    }

    /**
     * Association wrapper class
     */
    public class Association implements Serializable
    {
        private static final long serialVersionUID = 1078430803027004L;
        
        protected QNameBean name;
        protected QNameBean typeName;
        
        public Association(QName name, QName typeName)
        {
            this.name = name != null ? new QNameBean(name) : null;
            this.typeName = new QNameBean(typeName);
        }

        public QNameBean getName()
        {
            return name;
        }

        public QNameBean getTypeName()
        {
            return typeName;
        }
    }

    /**
     * Child assoc wrapper class
     */
    public class ChildAssociation extends Association implements Serializable
    {
        private static final long serialVersionUID = -52439282250891063L;
        
        protected NodeRef childRef;
        protected NodeRef parentRef;
        protected QNameBean childType;
        protected QNameBean parentType;
        protected boolean primary;
        
        // from Association
        protected QNameBean name;
        protected QNameBean typeName;

        public ChildAssociation(ChildAssociationRef ref)
        {
            super(ref.getQName() != null ? ref.getQName() : null,
                    ref.getTypeQName() != null ? ref.getTypeQName() : null);
            
            this.childRef = ref.getChildRef();
            this.parentRef = ref.getParentRef(); // could be null
            if (childRef != null)
                this.childType = new QNameBean(getNodeType(childRef));
            if (parentRef != null)
                this.parentType = new QNameBean(getNodeType(parentRef));
            this.primary = ref.isPrimary();
        }

        public NodeRef getChildRef()
        {
            return childRef;
        }

        public QNameBean getChildTypeName()
        {
            return childType;
        }

        public NodeRef getParentRef()
        {
            return parentRef;
        }

        public QNameBean getParentTypeName()
        {
            return parentType;
        }

        public boolean isPrimary()
        {
            return primary;
        }

        public boolean getPrimary()
        {
            return this.isPrimary();
        }
    }

    /**
     * Wrapper to resolve Assoc Type and QName to short form with resolved prefix 
     */
    public class ChildAssocRefWrapper implements Serializable
    {
        private static final long serialVersionUID = 4321292337846270665L;
        
        final private ChildAssociationRef ref;
        private String qname = null;
        private String typeqname = null;
        
        public ChildAssocRefWrapper(ChildAssociationRef r)
        {
            ref = r;
        }
        
        public String getTypeQName()
        {
            return typeqname != null ? typeqname : (
                    typeqname = ref.getTypeQName() != null ? ref.getTypeQName().toPrefixString(getNamespaceService()) : "");
        }
    
        public String getQName()
        {
            return qname != null ? qname : (
                    qname = ref.getQName() != null ? ref.getQName().toPrefixString(getNamespaceService()) : "");
        }
    
        public NodeRef getChildRef()
        {
            return ref.getChildRef();
        }
    
        public NodeRef getParentRef()
        {
            return ref.getParentRef();
        }
    
        public boolean isPrimary()
        {
            return ref.isPrimary();
        }
        
        public boolean isChildLocked()
        {
            return lockService != null && lockService.getLockType(ref.getChildRef()) != null;
        }
    }
    
    /**
     * Peer assoc wrapper class
     */
    public class PeerAssociation extends Association implements Serializable
    {
        private static final long serialVersionUID = 4833278311416507L;
        
        protected NodeRef sourceRef;
        protected NodeRef targetRef;
        protected QNameBean sourceType;
        protected QNameBean targetType;
        
        // from Association
        protected QNameBean name;
        protected QNameBean typeName;
        
        public PeerAssociation(QName typeName, NodeRef sourceRef, NodeRef targetRef)
        {
            super(null, typeName);
            
            this.sourceRef = sourceRef;
            this.targetRef = targetRef;
            if (sourceRef != null)
                this.sourceType = new QNameBean(getNodeType(sourceRef));
            if (targetRef != null)
                this.targetType = new QNameBean(getNodeType(targetRef));
        }

        public NodeRef getSourceRef()
        {
            return sourceRef;
        }

        public QNameBean getSourceTypeName()
        {
            return sourceType;
        }

        public NodeRef getTargetRef()
        {
            return targetRef;
        }

        public QNameBean getTargetTypeName()
        {
            return targetType;
        }
    }

    /**
     * Property wrapper class
     */
    public class Property implements Serializable
    {
        private static final long serialVersionUID = 7755924782250077L;
        
        private QNameBean name;

        private boolean isCollection = false;

        private List<Value> values;

        private boolean residual;
        
        private QNameBean typeName;

        /**
         * Construct
         * 
         * @param name property name
         * @param value property values
         */
        @SuppressWarnings("unchecked")
        public Property(QName qname, Serializable value)
        {
            this.name = new QNameBean(qname);

            PropertyDefinition propDef = getDictionaryService().getProperty(qname);
            if (propDef != null)
            {
                QName qn = propDef.getDataType().getName();
                typeName = qn != null ? new QNameBean(propDef.getDataType().getName()) : null;
                residual = false;
            }
            else
            {
                residual = true;
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
                    values.add(new Value(multiValue instanceof QName ? new QNameBean((QName) multiValue) : multiValue));
                }
            }
            else
            {
                values = Collections.singletonList(new Value(value instanceof QName ? new QNameBean((QName) value) : value));
            }
            this.values = values;
        }

        /**
         * Gets the property name
         * 
         * @return name
         */
        public QNameBean getName()
        {
            return name;
        }

        public QNameBean getTypeName()
        {
            return typeName;
        }

        /**
         * Gets the prefixed property name
         * 
         * @return prefixed name
         */
        public String getPrefixedName()
        {
            return name.getPrefixedName();
        }

        /**
         * Gets the property value
         * 
         * @return value
         */
        public List<Value> getValues()
        {
            return values;
        }

        /**
         * Determines whether the property is residual
         * 
         * @return true => property is not defined in dictionary
         */
        public boolean getResidual()
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
            return (getTypeName() == null) ? false : getTypeName().getName().equals(DataTypeDefinition.ANY.toString());
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
        public class Value implements Serializable
        {
            private static final long serialVersionUID = 47235536691732705L;
            
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
                String datatype = null;
                if (Property.this.getTypeName() != null)
                {
                    datatype = Property.this.getTypeName().getName();
                }
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
     * Permission bean
     */
    public static class Permission implements Serializable
    {
        private static final long serialVersionUID = 1235536691732705L;
        
        private final String permission;
        private final String authority;
        private final String accessStatus;
        
        public Permission(String permission, String authority, String accessStatus)
        {
            this.permission = permission;
            this.authority = authority;
            this.accessStatus = accessStatus;
        }

        public String getPermission()
        {
            return permission;
        }
        
        public String getAuthority()
        {
            return authority;
        }
        
        public String getAccessStatus()
        {
            return accessStatus;
        }
    }

    /**
     * Permission representing the fact that "Read Permissions" has not been granted
     */
    public static class NoReadPermissionGranted extends Permission implements Serializable
    {
        private static final long serialVersionUID = 1236786691732705L;
        
        public NoReadPermissionGranted()
        {
            super(PermissionService.READ_PERMISSIONS, "[Current Authority]", "Not Granted");
        }
    }

    public static class NoStoreMask extends Permission implements Serializable
    {
        private static final long serialVersionUID = 3125536691732705L;
        
        public NoStoreMask()
        {
            super("All <No Mask>", "All", "Allowed");
        }
    }
}
