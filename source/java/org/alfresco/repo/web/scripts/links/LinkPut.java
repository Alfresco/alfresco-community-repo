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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the link fetching links.put webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinkPut extends AbstractLinksWebScript
{
   private static final String PARAM_MESSAGE = "message";
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String linkName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Try to find the link
      LinkInfo link = linksService.getLink(site.getShortName(), linkName);
      if(link == null)
      {
         String message = "No link found with that name";
         
         status.setCode(Status.STATUS_NOT_FOUND);
         status.setMessage(message);
         model.put(PARAM_MESSAGE, message);
         return model;
      }
      
      
      // Get the new link details from the JSON
      try
      {
         // Update the main properties
         link.setTitle(getOrNull(json, "title"));
         link.setDescription(getOrNull(json, "description"));
         link.setURL(getOrNull(json, "url"));
         
         // Handle internal / not internal
         if(json.has("internal"))
         {
            link.setInternal(true);
         }
         else
         {
            link.setInternal(false);
         }
         
         // Do the tags
         link.getTags().clear();
         List<String> tags = getTags(json);
         if(tags != null && tags.size() > 0)
         {
            link.getTags().addAll(tags);
         }
      }
      catch(JSONException je)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + je.getMessage());
      }
      
      
      // Update the link
      try
      {
         link = linksService.updateLink(link);
      }
      catch(AccessDeniedException e)
      {
         String message = "You don't have permission to update that link";
         
         status.setCode(Status.STATUS_FORBIDDEN);
         status.setMessage(message);
         model.put(PARAM_MESSAGE, message);
         return model;
      }
      
      // Generate an activity for the change
      addActivityEntry("updated", link, site, req, json);

      
      // Build the model
      model.put(PARAM_MESSAGE, "Node " + link.getNodeRef() + " updated");
      model.put("link", link);
      model.put("site", site);
      model.put("siteId", site.getShortName());
      
      // All done
      return model;
   }
}
