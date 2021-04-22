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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the wiki page creating/editing page.put webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPagePut extends AbstractWikiWebScript
{
   // Implementation note: the wiki page webscripts do not follow the standard Alfresco convention whereby
   // POST = CREATE and PUT = UPDATE.
   // In this case, put is used for both create and update.

   private VersionService versionService;
   public void setVersionService(VersionService versionService) 
   {
       this.versionService = versionService;
   }

   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String pageTitle,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      Map<String, Object> model = new HashMap<>();
      
      // Grab the details of the change
      // Fetch the contents
      String contents = (String)json.get("pagecontent");
      
      // Fetch the title, used only when creating
      String title;
      if (json.containsKey("title"))
      {
         title = (String)json.get("title");
      }
      else
      {
         title = pageTitle;
      }
      
      // Fetch the versioning details
      boolean forceSave = json.containsKey("forceSave");
      String currentVersion = null;
      if (json.containsKey("currentVersion"))
      {
         currentVersion = (String)json.get("currentVersion");
      }
      
      // Fetch the tags, if given
      List<String> tags = null;
      if (json.containsKey("tags"))
      {
         tags = new ArrayList<>();
         if (!json.get("tags").equals(""))
         {
            // Array of tags
            JSONArray tagsA = (JSONArray)json.get("tags");
            for (int i=0; i<tagsA.size(); i++)
            {
               tags.add((String)tagsA.get(i));
            }
         }
      }
      
      // Are we creating or editing?
      WikiPageInfo page = wikiService.getWikiPage(site.getShortName(), pageTitle);
      if (page == null)
      {
         // Create the page
         page = wikiService.createWikiPage(site.getShortName(), title, contents);
         
         // Add tags if given
         if (tags != null && !tags.isEmpty())
         {
            page.getTags().addAll(tags);
            wikiService.updateWikiPage(page);
         }
         
         // Make it versioned
         makeVersioned(page);
         
         // Generate the activity
         addActivityEntry("created", page, site, req, json);
      }
      else
      {
         // Updating, check about versioning first
         if (forceSave || pageVersionMatchesSubmitted(page, currentVersion))
         {
            // Update the page
            page.setContents(contents);
            if (tags != null)
            {
               page.getTags().clear();
               page.getTags().addAll(tags);
            }
            wikiService.updateWikiPage(page);
         }
         else
         {
            // Editing the wrong version
            String message = "Repository version is newer.";
            throw new WebScriptException(Status.STATUS_CONFLICT, message);
         }
         
         // Generate the activity
         addActivityEntry("edited", page, site, req, json);
      }

      
      // All done
      model.put("page", page);
      model.put("site", site);
      model.put("siteId", site.getShortName());
      
      // Double wrap
      Map<String, Object> result = new HashMap<>();
      result.put("result", model);
      return result;
   }
   
   private boolean pageVersionMatchesSubmitted(WikiPageInfo page, String currentVersion)
   {
      // If they didn't give version, it can't be right
      if (currentVersion == null)
      {
         return false;
      }
      
      // Grab the current version
      Version version = versionService.getCurrentVersion(page.getNodeRef());
      if (version == null)
      {
         // It should be versioned already, fix that
         makeVersioned(page);
         
         // Wasn't versioned before, so can't detect conflict
         return true;
      }
      
      return version.getVersionLabel().equals(currentVersion);
   }
   
   private void makeVersioned(WikiPageInfo page)
   {
      Map<QName,Serializable> versionProps = new HashMap<>();
      versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
      versionProps.put(ContentModel.PROP_AUTO_VERSION_PROPS, true);
      versionService.ensureVersioningEnabled(page.getNodeRef(), versionProps);
   }
}
