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
 * This class is the controller for the link fetching links.post webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinksPost extends AbstractLinksWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String linkName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Get the new link details from the JSON
      String title;
      String description;
      String url;
      boolean internal;
      List<String> tags;
      try
      {
         // Fetch the main properties
         title = getOrNull(json, "title");
         description = getOrNull(json, "description");
         url = getOrNull(json, "url");
         
         // Handle internal / not internal
         internal = json.has("internal");
         
         // Do the tags
         tags = getTags(json);
      }
      catch(JSONException je)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + je.getMessage());
      }
      
      
      // Create the link
      LinkInfo link;
      try
      {
         link = linksService.createLink(site.getShortName(), title, description, url, internal);
      }
      catch(AccessDeniedException e)
      {
         String message = "You don't have permission to create a link";
         
         status.setCode(Status.STATUS_FORBIDDEN);
         status.setMessage(message);
         model.put(PARAM_MESSAGE, message);
         return model;
      }
      
      // Set the tags if required
      if(tags != null && tags.size() > 0)
      {
         link.getTags().addAll(tags);
         linksService.updateLink(link);
      }
      
      // Generate an activity for the change
      addActivityEntry("created", link, site, req, json);

      
      // Build the model
      model.put(PARAM_MESSAGE, link.getSystemName()); // Really!
      model.put(PARAM_ITEM, renderLink(link));
      model.put("node", link.getNodeRef());
      model.put("link", link);
      model.put("site", site);
      model.put("siteId", site.getShortName());
      
      // All done
      return model;
   }
}
