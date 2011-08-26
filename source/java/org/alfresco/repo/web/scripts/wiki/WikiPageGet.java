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
package org.alfresco.repo.web.scripts.wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.wiki.WikiServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the wiki page listing page.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPageGet extends AbstractWikiWebScript
{
   // For matching links. Not the best pattern ever...
   private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[([^\\|\\]]+)");
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String pageName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Try to find the page
      WikiPageInfo page = wikiService.getWikiPage(site.getShortName(), pageName);
      if(page == null)
      {
         String message = "The Wiki Page could not be found";
         status.setCode(Status.STATUS_NOT_FOUND);
         status.setMessage(message);
         status.setRedirect(true);
         
         // Grab the container, used in permissions checking
         NodeRef container = siteService.getContainer(
               site.getShortName(), WikiServiceImpl.WIKI_COMPONENT
         );
         // If there's no container yet, the site will do for permissions
         if(container == null)
         {
            container = site.getNodeRef();
         }
         
         // Record these
         model.put("container", container);
         model.put("error", message);
         
         // Bail out
         return model;
      }

      
      // Identify all the internal page links, valid and not
      // TODO This may be a candidate for the service in future
      List<String> links = new ArrayList<String>();
      if(page.getContents() != null)
      {
         Matcher m = LINK_PATTERN.matcher(page.getContents());
         while(m.find())
         {
            String link = m.group(1);
            if(! links.contains(link))
            {
               links.add(link);
            }
         }
      }
      
      
      // Get the list of pages, needed for link matching apparently
      PagingRequest paging = new PagingRequest(MAX_QUERY_ENTRY_COUNT);
      PagingResults<WikiPageInfo> pages = wikiService.listWikiPages(site.getShortName(), paging);
      
      List<String> pageNames = new ArrayList<String>();
      for (WikiPageInfo p : pages.getPage())
      {
         pageNames.add(p.getSystemName());
      }
      
      
      // All done
      model.put("page", page);
      model.put("node", page.getNodeRef());
      model.put("container", page.getContainerNodeRef()); 
      model.put("links", links);
      model.put("pageList", pageNames);
      model.put("tags", page.getTags());
      model.put("siteId", site.getShortName());
      model.put("site", site);
      
      // Double wrap
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("result", model);
      return result;
   }
}
