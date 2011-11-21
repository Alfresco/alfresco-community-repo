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
package org.alfresco.repo.web.scripts.links;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.links.LinksServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the links listing links.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinksListGet extends AbstractLinksWebScript
{
   protected static final int RECENT_SEARCH_PERIOD_DAYS = 7;
   protected static final long ONE_DAY_MS = 24*60*60*1000;
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String linkName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      // Decide on what kind of request they wanted
      String filter = req.getParameter("filter");
      
      // Tagging?
      boolean tagFiltering = true;
      String tag = req.getParameter("tag");
      if (tag == null || tag.length() == 0)
      {
         tagFiltering = false;
      }
      else
      {
         // Tags can be full unicode strings, so decode
         tag = URLDecoder.decode(tag);
      }
      
      // User?
      boolean userFiltering = false;
      String user = null;
      if ("user".equals(filter))
      {
         userFiltering = true;
         user = AuthenticationUtil.getFullyAuthenticatedUser();
      }
      
      // Date?
      boolean dateFiltering = false;
      Date from = null;
      Date to = null;
      if ("recent".equals(filter))
      {
         dateFiltering = true;
         Date now = new Date();
         from = new Date(now.getTime() - RECENT_SEARCH_PERIOD_DAYS*ONE_DAY_MS);
         to = new Date(now.getTime() + ONE_DAY_MS);
      }
      
      
      // Get the links for the list
      PagingRequest paging = buildPagingRequest(req);
      PagingResults<LinkInfo> links;
      if (tagFiltering)
      {
         links = linksService.findLinks(site.getShortName(), user, from, to, tag, paging);
      }
      else
      {
         if (userFiltering)
         {
            links = linksService.listLinks(site.getShortName(), user, paging);
         }
         else if (dateFiltering)
         {
            links = linksService.listLinks(site.getShortName(), from, to, paging);
         }
         else
         {
            links = linksService.listLinks(site.getShortName(), paging);
         }
      }

      
      // For each one in our page, grab details of any ignored instances
      List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
      for (LinkInfo link : links.getPage())
      {
         Map<String, Object> result = renderLink(link);
         items.add(result);
      }
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("items", items);
      data.put("pageSize", paging.getMaxItems());
      data.put("startIndex", paging.getSkipCount());
      data.put("itemCount", items.size());
      
      int total = items.size();
      if (links.getTotalResultCount() != null && links.getTotalResultCount().getFirst() != null)
      {
         total = links.getTotalResultCount().getFirst();
      }
      data.put("total", total);
      
      // We need the container node for permissions checking
      NodeRef container;
      if (links.getPage().size() > 0)
      {
         container = links.getPage().get(0).getContainerNodeRef();
      }
      else
      {
         // Find the container (if it's been created yet)
         container = siteService.getContainer(
               site.getShortName(), LinksServiceImpl.LINKS_COMPONENT);
         
         if (container == null)
         {
            // Brand new site, no write operations on links have happened
            // Fudge it for now with the site itself, the first write call
            //  will have the container created
            container = site.getNodeRef();
         }
      }
      
      // All done
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("data", data);
      model.put("links", container); 
      model.put("siteId", site.getShortName());
      model.put("site", site);
      return model;
   }
}
