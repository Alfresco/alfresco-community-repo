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
package org.alfresco.repo.web.scripts.blogs;

import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.nodelocator.SitesHomeNodeLocator;
import org.alfresco.repo.nodelocator.UserHomeNodeLocator;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is a port of a previous JavaScript library.
 * 
 * @author Neil Mc Erlean (based on previous JavaScript)
 * @since 4.0
 * @deprecated Not to be used/extended as this is likely to be refactored.
 */
public class RequestUtilsLibJs
{
    //FIXME It will be refactored when the other services are ported from JavaScript to Java.
    
    /**
     * Gets the NodeRef requested based on the following templates:
     * 
     * <pre>
     * /api/blog/site/{site}/{container}/{path}/posts
     * /api/blog/site/{site}/{container}/posts
     * /api/blog/node/{store_type}/{store_id}/{id}/posts
     * </pre>
     */
    public static NodeRef getRequestNode(WebScriptRequest req, ServiceRegistry services)
    {
        NodeRef result = null;
        
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        
        // If the template args contains a "store_type" then we we have a standard NodeRef URI template pattern
        // check whether we got a node reference or a site related uri
        final String storeType = templateVars.get("store_type");
        final String site = templateVars.get("site");
        if (storeType != null)
        {
            result = findFromReference(templateVars, services);
        }
        else if (site != null)
        {
            result = findNodeInSite(templateVars, services);
        }
        else
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown request parameters (webscript incorrectly configured?)");
        }
        
        if (!services.getNodeService().exists(result))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + result.toString());
        }
        
        return result;
    }
    
    private static NodeRef findFromReference(final Map<String, String> templateVars, ServiceRegistry services)
    {
        NodeRef result = null;
        
        String nodeRefString = templateVars.get("store_type") + "://" + templateVars.get("store_id") + "/" + templateVars.get("id");
        
        // These webscripts support some non-standard NodeRef URI constructions.
        
        NodeLocatorService nodeLocatorService = services.getNodeLocatorService();
        if (nodeRefString.equals("alfresco://company/home"))
        {
            result = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        }
        else if (nodeRefString.equals("alfresco://user/home"))
        {
            result = nodeLocatorService.getNode(UserHomeNodeLocator.NAME, null, null);
        }
        else if (nodeRefString.equals("alfresco://sites/home"))
        {
            result = nodeLocatorService.getNode(SitesHomeNodeLocator.NAME, null, null);
        }
        else if (NodeRef.isNodeRef(nodeRefString))
        {
            result = new NodeRef(nodeRefString);
        }
        else
        {
//           result = new Nodesearch.findNode(nodeRef);
        }
        
        if (result == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Node " + nodeRefString + "does not exist");
        }
        return result;
    }
    
    /**
     * Returns the node as specified by the given arguments.
     *
     * @param siteId the site for which a node is requested
     * @param containerId the component to look in for the node.
     * @param path a path to the node. Returns the root node in case path is null or ''
     *        return null in case no node can be found for the given path
     * @return the node or a json error in case the node could not be fetched. Check with .
     */
    private static NodeRef findNodeInSite(final Map<String, String> templateVars, ServiceRegistry services)
    {
        final String siteId = templateVars.get("site");
        final String containerId = templateVars.get("container");
        String path = templateVars.get("path");
        if (path == null) path = "";
        
        SiteInfo site = services.getSiteService().getSite(siteId);
        if (site == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Site not found: '" + siteId + "'");
        }
        
        NodeRef node = services.getSiteService().getContainer(siteId, containerId);
        if (node == null)
        {
            node = services.getSiteService().createContainer(siteId, containerId, null, null);
            if (node == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to fetch container '" + containerId + "' of site '" + siteId + "'. (No write permission?)");
            }
        }
       
        // try to fetch the the path is there is any
        if (path != null && !path.isEmpty())
        {
            node = childByNamePath(path, node, services);
            if (node == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "No node found for the given path: \"" + path + "\" in container " + containerId + " of site " + siteId);
            }
        }
        
        return node;
    }
    
    
    /**
     * Gets a descendant node by navigating a cm:name-based path e.g. /QA/Testing/Docs
     * 
     * @see ScriptNode#childByNamePath(String)
     */
    private static NodeRef childByNamePath(String path, NodeRef rootNode, ServiceRegistry services)
    {
        // This is based partially on ScriptNode.childByNamePath, but supports less path variations.
        NodeRef result = null;
        QName nodeType = services.getNodeService().getType(rootNode);
        
        if (services.getDictionaryService().isSubClass(nodeType, ContentModel.TYPE_FOLDER))
        {
            /**
             * The current node is a folder.
             * optimized code path for cm:folder and sub-types supporting getChildrenByName() method
             */ 
            StringTokenizer t = new StringTokenizer(path, "/");
            if (t.hasMoreTokens())
            {
                result = rootNode;
                while (t.hasMoreTokens() && result != null)
                {
                    String name = t.nextToken();
                    try
                    {
                        result = services.getNodeService().getChildByName(result, ContentModel.ASSOC_CONTAINS, name);
                    }
                    catch (AccessDeniedException ade)
                    {
                        result = null;
                    }
                }
            }
        }
        
        return result;
    }

}
