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
package org.alfresco.repo.web.scripts.wiki;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.wiki.WikiServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.util.UrlUtil;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the wiki page listing pagelist.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPageListGet extends AbstractWikiWebScript
{
   protected static final int RECENT_SEARCH_PERIOD_DAYS = 7;
   protected static final long ONE_DAY_MS = 24*60*60*1000;
   
   // Injected services
   private SysAdminParams sysAdminParams;
   
   public void setSysAdminParams(SysAdminParams sysAdminParams)
   {
      this.sysAdminParams = sysAdminParams;
   }
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String pageTitle,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      // Decide on what kind of request they wanted
      String filter = req.getParameter("filter");
      String strPageMetaOnly = req.getParameter("pageMetaOnly");
      boolean pageMetaOnly = strPageMetaOnly != null ? Boolean.parseBoolean(strPageMetaOnly) : false;
      
      // User?
      boolean userFiltering = false;
      String user = null;
      if ("user".equals(filter) || "myPages".equals(filter))
      {
         userFiltering = true;
         user = AuthenticationUtil.getFullyAuthenticatedUser();
      }
      
      // Date?
      boolean dateFiltering = false;
      boolean dateIsCreated = true;
      Date from = null;
      Date to = null;
      if ("recentlyAdded".equals(filter) || 
          "recentlyCreated".equals(filter) ||
          "recentlyModified".equals(filter))
      {
         dateFiltering = true;
         if ("recentlyModified".equals(filter))
         {
            dateIsCreated = false;
         }
         
         int days = RECENT_SEARCH_PERIOD_DAYS;
         String daysS = req.getParameter("days");
         if (daysS != null && daysS.length() > 0)
         {
            days = Integer.parseInt(daysS);
         }
         
         Date now = new Date();
         from = new Date(now.getTime() - days*ONE_DAY_MS);
         to = new Date(now.getTime() + ONE_DAY_MS);
      }
      
      
      // Get the links for the list
      PagingRequest paging = buildPagingRequest(req);
      PagingResults<WikiPageInfo> pages;
      if (userFiltering)
      {
         pages = wikiService.listWikiPages(site.getShortName(), user, paging);
      }
      else if (dateFiltering)
      {
         if (dateIsCreated)
         {
            pages = wikiService.listWikiPagesByCreated(site.getShortName(), from, to, paging);
         }
         else
         {
            pages = wikiService.listWikiPagesByModified(site.getShortName(), from, to, paging);
         }
      }
      else
      {
         pages = wikiService.listWikiPages(site.getShortName(), paging);
      }

      
      // For each one in our page, grab details of any ignored instances
      List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
      for (WikiPageInfo page : pages.getPage())
      {
         Map<String, Object> result = renderWikiPage(page);
         items.add(result);
      }
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("pages", items);
      data.put("pageSize", paging.getMaxItems());
      data.put("startIndex", paging.getSkipCount());
      data.put("itemCount", items.size());
      
      int total = items.size();
      if (pages.getTotalResultCount() != null && pages.getTotalResultCount().getFirst() != null)
      {
         total = pages.getTotalResultCount().getFirst();
      }
      data.put("total", total);
      
      // We need the container node for permissions checking
      NodeRef container;
      if (pages.getPage().size() > 0)
      {
         container = pages.getPage().get(0).getContainerNodeRef();
      }
      else
      {
         // Find the container (if it's been created yet)
         container = siteService.getContainer(
               site.getShortName(), WikiServiceImpl.WIKI_COMPONENT);
         
         if (container == null)
         {
            // Brand new site, no write operations on links have happened
            // Fudge it for now with the site itself, the first write call
            //  will have the container created
            container = site.getNodeRef();
         }
      }
      
      // All done
      Map<String, Object> wiki = new HashMap<String, Object>();
      wiki.put("pages", items); // Old style
      wiki.put("container", container);
      
      if (userFiltering)
      {
         // We need to get all the wiki pages for "My Pages" filter otherwise 
         // the links for renamed wiki pages won't be rendered correctly, 
         // which were created by other users
         pages = wikiService.listWikiPages(site.getShortName(), paging);
         List<String> pageTitles = new ArrayList<String>(pages.getPage().size());
         for (WikiPageInfo page : pages.getPage())
         {
            pageTitles.add(page.getTitle());
         }
         wiki.put("pageTitles", pageTitles);
      }
      
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("data", data); // New style
      model.put("wiki", wiki);
      model.put("siteId", site.getShortName());
      model.put("site", site);
      model.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(sysAdminParams));
      model.put("pageMetaOnly", pageMetaOnly);
      return model;
   }
}
