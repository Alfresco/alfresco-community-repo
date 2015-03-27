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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.slingshot.web.scripts.NodeBrowserScript;
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * Admin Console NodeBrowser WebScript POST controller.
 * <p>
 * Implements a low-level node browser client for the Admin Console tool. Extends
 * the slingshot NodeBrowserScript WebScript to share the useful value wrapper classes.
 * 
 * @author Kevin Roast
 * @since 5.1
 */
public class NodeBrowserPost extends NodeBrowserScript implements Serializable
{
    private static final long serialVersionUID = 8464392337270665212L;
    
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
        try
        {
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
        
        // redirect as all admin console pages do (follow standard pattern)
        // The logic to generate the navigation section and server meta-data is all tied into alfresco-common.lib.js
        // which is great for writing JS based JMX surfaced pages, but not so great for Java backed WebScripts. 
        status.setCode(301);
        status.setRedirect(true);
        status.setLocation(buildUrl(req, returnParams, action));
        
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
    }
}
