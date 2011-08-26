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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the links deleting links-delete.post webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinksDeletePost extends AbstractLinksWebScript
{
   protected static final int RECENT_SEARCH_PERIOD_DAYS = 7;
   protected static final long ONE_DAY_MS = 24*60*60*1000;
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String linkName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Get the requested nodes from the JSON
      // Silently skips over any invalid ones specified
      List<LinkInfo> links = new ArrayList<LinkInfo>();
      try
      {
         if(json.has("items"))
         {
            JSONArray items = json.getJSONArray("items");
            for(int i=0; i<items.length(); i++)
            {
               String name = items.getString(i);
               LinkInfo link = linksService.getLink(site.getShortName(), name);
               if(link != null)
               {
                  links.add(link);
               }
            }
         }
      }
      catch(JSONException je)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + je.getMessage());
      }
      
      
      // Check we got at least one link, and bail if not
      if(links.size() == 0)
      {
         String message = "No valid link names supplied";
         
         status.setCode(Status.STATUS_NOT_FOUND);
         status.setMessage(message);
         model.put(PARAM_MESSAGE, message);
         return model;
      }
      
      
      // Delete each one in turn
      for(LinkInfo link : links)
      {
         // Do the delete
         try
         {
            linksService.deleteLink(link);
         }
         catch(AccessDeniedException e)
         {
            String message = "You don't have permission to delete the link with name '" + link.getSystemName() + "'";
            
            status.setCode(Status.STATUS_FORBIDDEN);
            status.setMessage(message);
            model.put(PARAM_MESSAGE, message);
            return model;
         }
         
         // Generate the activity entry for it
         addActivityEntry("deleted", link, site, req, json);
         
         // Record a message (only the last one is used though!)
         model.put(PARAM_MESSAGE, "Node " + link.getNodeRef() + " deleted");
      }

      
      // All done
      model.put("siteId", site.getShortName());
      model.put("site", site);
      return model;
   }
}
