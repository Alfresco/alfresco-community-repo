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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the wiki page listing page.delete webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPageDelete extends AbstractWikiWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String pageName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Try to find the page
      WikiPageInfo page = wikiService.getWikiPage(site.getShortName(), pageName);
      if(page == null)
      {
         String message = "The Wiki Page could not be found";
         throw new WebScriptException(Status.STATUS_NOT_FOUND, message);
      }
      
      // Have the page deleted
      wikiService.deleteWikiPage(page);
      
      // Generate an activity for this
      addActivityEntry("deleted", page, site, req, json);
      
      // Mark it as gone
      status.setCode(Status.STATUS_NO_CONTENT);
      return model;
   }
}
