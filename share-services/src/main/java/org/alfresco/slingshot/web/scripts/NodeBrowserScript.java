/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.slingshot.web.scripts;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.admin.NodeBrowserPost;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Node browser web script to handle search results, node details and workspaces.
 * Extends the NodeBrowserPost script to inherit useful helper classes.
 * 
 * @author dcaruana
 * @author wabson
 */
public class NodeBrowserScript extends NodeBrowserPost implements Serializable
{
    private static final long serialVersionUID = 48743409337475896L;

    private Long searchElapsedTime = null;

    /**
     * Action to submit search
     * 
     * @return next action
     */
    public List<Node> submitSearch(final String store, final String query, final String queryLanguage, final int maxResults) throws IOException
    {
        long start = System.currentTimeMillis();
    	final StoreRef storeRef = new StoreRef(store);
        RetryingTransactionCallback<List<Node>> searchCallback = new RetryingTransactionCallback<List<Node>>()
        {
            public List<Node> execute() throws Throwable
            {
                List<Node> searchResults = null;
                
                if (queryLanguage.equals("storeroot"))
                {
                    NodeRef rootNodeRef = getNodeService().getRootNode(storeRef);
                    searchResults = new ArrayList<Node>(1);
                    searchResults.add(new Node(rootNodeRef));
                    return searchResults;
                }
                else if (queryLanguage.equals("noderef"))
                {
                    // ensure node exists
                    NodeRef nodeRef = new NodeRef(query);
                    boolean exists = getNodeService().exists(nodeRef);
                    if (!exists)
                    {
                        throw new WebScriptException(500, "Node " + nodeRef + " does not exist.");
                    }
                    searchResults = new ArrayList<Node>(1);
                    searchResults.add(new Node(nodeRef));
                    return searchResults;
                }
                SearchParameters sp = new SearchParameters();
                sp.addStore(storeRef);
                sp.setLanguage(queryLanguage);
                sp.setQuery(query);
                if (maxResults > 0)
                {
                    sp.setLimit(maxResults);
                    sp.setLimitBy(LimitBy.FINAL_SIZE);
                }

                // perform search
                List<NodeRef> nodeRefs = getSearchService().query(sp).getNodeRefs();
                searchResults = new ArrayList<Node>(nodeRefs.size());
                for (NodeRef nodeRef : nodeRefs) {
                	searchResults.add(new Node(nodeRef));
				}
                return searchResults;
            }
        };

        try
        {
            List<Node> results = getTransactionService().getRetryingTransactionHelper().doInTransaction(searchCallback, true);
            this.searchElapsedTime = System.currentTimeMillis() - start;
            return results;
        }
        catch (Throwable e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * @return the searchElapsedTime
     */
    protected Long getSearchElapsedTime()
    {
        return this.searchElapsedTime;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
    	if (req.getPathInfo().equals("/slingshot/node/search"))
    	{
    		List<Node> nodes;
    		Map<String, Object> tmplMap = new HashMap<String, Object>(1);
			try
			{
				if (req.getParameter("store") == null || req.getParameter("store").length() == 0)
				{
					status.setCode(HttpServletResponse.SC_BAD_REQUEST);
					status.setMessage("Store name not provided");
					status.setRedirect(true);
					return null;
				}
				if (req.getParameter("q") == null || req.getParameter("q").length() == 0)
				{
					status.setCode(HttpServletResponse.SC_BAD_REQUEST);
					status.setMessage("Search query not provided");
					status.setRedirect(true);
					return null;
				}
				if (req.getParameter("lang") == null || req.getParameter("lang").length() == 0)
				{
					status.setCode(HttpServletResponse.SC_BAD_REQUEST);
					status.setMessage("Search language not provided");
					status.setRedirect(true);
					return null;
				}

                int maxResult = 0;
                try
                {
                    maxResult = Integer.parseInt(req.getParameter("maxResults"));
                }
                catch (NumberFormatException ex)
                {
                }

				nodes = submitSearch(req.getParameter("store"), req.getParameter("q"), req.getParameter("lang"), maxResult);
	    		tmplMap.put("results", nodes);
	    		tmplMap.put("searchElapsedTime", getSearchElapsedTime());
			}
			catch (IOException e)
			{
				status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				status.setMessage(e.getMessage());
				status.setException(e);
				status.setRedirect(true);
			}
    		return tmplMap;
    	}
    	else if (req.getPathInfo().equals("/slingshot/node/stores"))
    	{
    		Map<String, Object> model = new HashMap<String, Object>();
    		model.put("stores", getStores());
    		return model;
    	}
    	else // Assume we are looking for a node
    	{
     		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
			if (templateVars.get("protocol") == null || templateVars.get("protocol").length() == 0 || 
					templateVars.get("store") == null || templateVars.get("store").length() == 0 ||
					templateVars.get("id") == null || templateVars.get("id").length() == 0)
			{
				status.setCode(HttpServletResponse.SC_BAD_REQUEST);
				status.setMessage("Node not provided");
				status.setRedirect(true);
				return null;
			}
        	NodeRef nodeRef = new NodeRef(templateVars.get("protocol"), templateVars.get("store"), templateVars.get("id"));
        	
    		Map<String, Object> permissionInfo = new HashMap<String, Object>(3);
    		permissionInfo.put("entries", getPermissions(nodeRef));
    		permissionInfo.put("owner", this.getOwnableService().getOwner(nodeRef));
    		permissionInfo.put("inherit", this.getInheritPermissions(nodeRef));
    		permissionInfo.put("storePermissions", getStorePermissionMasks(nodeRef));

    		Map<String, Object> model = new HashMap<String, Object>();
    		model.put("node", new Node(nodeRef));
    		model.put("properties", getProperties(nodeRef));
    		model.put("aspects", getAspects(nodeRef));
    		model.put("children", getChildren(nodeRef));
    		model.put("parents", getParents(nodeRef));
    		model.put("assocs", getAssocs(nodeRef));
    		model.put("sourceAssocs", getSourceAssocs(nodeRef));
    		model.put("permissions", permissionInfo);
    		return model;
    	}
    }
}
