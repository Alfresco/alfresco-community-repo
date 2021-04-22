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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the wiki page renaming move.post webscript.
 * 
 * TODO Track links to pages, so we can avoid creating the "This page has been moved"
 *  stubs as now, for cases where nothing links to the page being renamed. (ALF-3844) 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPageMovePost extends AbstractWikiWebScript
{
   private static final String MSG_MOVED = "page-moved";
   private static final String MSG_MOVED_HERE = "page-moved-here";
   private static final String MSG_NOT_FOUND= "page-not-found";
   
   // The 'custom0' key here refers to the org.alfresco.wiki.page-renamed {2} in activity-list.get.properties
   private static final String OLD_TITLE_KEY = "custom0";
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String pageTitle,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      final Map<String, Object> model = new HashMap<String, Object>();
      final ResourceBundle rb = getResources();
      
      // Try to find the page we're renaming
      WikiPageInfo page = wikiService.getWikiPage(site.getShortName(), pageTitle);
      if (page == null)
      {
         String message = "The Wiki Page could not be found";
         status.setCode(Status.STATUS_NOT_FOUND);
         status.setMessage(message);
         
         // Wrap and bail
         model.put("error", rb.getString(MSG_NOT_FOUND));
         Map<String, Object> result = new HashMap<String, Object>();
         result.put("result", model);
         return result;
      }
      
      
      // Grab the new Title
      // The "name" in the JSON is actually the title!
      String newTitle = (String)json.get("name");
         
      
      // Have the page re-named, if possible
      String oldTitle = page.getTitle().length() == 0 ? pageTitle : page.getTitle();
      try
      {
         page.setTitle(newTitle);
         page = wikiService.updateWikiPage(page);
      }
      catch (FileExistsException e)
      {
         throw new WebScriptException(Status.STATUS_CONFLICT, "Duplicate page name");
      }
      
      
      // Create the "This page has been moved" entry for the old page
      String movedContent = rb.getString(MSG_MOVED) + " [[" + page.getTitle() + 
                          "|" + rb.getString(MSG_MOVED_HERE) + "]].";
      wikiService.createWikiPage(site.getShortName(), oldTitle, movedContent); 
      
      Map<String, String> additionalData = new HashMap<String, String>();
      additionalData.put(OLD_TITLE_KEY, oldTitle);
      
      // Add an activity entry for the rename
      addActivityEntry("renamed", page, site, req, json, additionalData);


      // All done
      model.put("name", page.getSystemName());
      model.put("title", page.getTitle());
      model.put("page", page);
      model.put("siteId", site.getShortName());
      model.put("site", site);
      
      // Double wrap
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("result", model);
      return result;
   }
}
