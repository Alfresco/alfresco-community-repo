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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the wiki page fetching page.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiPageGet extends AbstractWikiWebScript
{
   private static final String MSG_NOT_FOUND= "page-not-found";
    
   // For matching links. Not the best pattern ever...
   private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[([^\\|#\\]]+)");
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String pageTitle,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      String strMinWikiData = req.getParameter("minWikiData");
      boolean minWikiData = strMinWikiData != null ? Boolean.parseBoolean(strMinWikiData) : false;
       
      final ResourceBundle rb = getResources();
      Map<String, Object> model = new HashMap<>();
      
      // Try to find the page
      WikiPageInfo page = wikiService.getWikiPage(site.getShortName(), pageTitle);
      if (page == null)
      {
         String message = "The Wiki Page could not be found";
         status.setCode(Status.STATUS_NOT_FOUND);
         status.setMessage(message);
         status.setRedirect(true);
         
         // MNT-11595 Downgrading permission from Manager to Consumer, user still allowed to create WIKI pages
         // Record these
         model.put("container", site.getNodeRef());
         model.put("error", rb.getString(MSG_NOT_FOUND));
         
         // Bail out
         return model;
      }

      
      // Identify all the internal page links, valid and not
      // TODO This may be a candidate for the service in future
      List<String> links = new ArrayList<>();
      List<String> pageTitles = new ArrayList<>();
      if (page.getContents() != null)
      {
         Matcher m = LINK_PATTERN.matcher(page.getContents());
         while (m.find())
         {
            String link = m.group(1);
            if (! links.contains(link))
            {
               links.add(link);
               // build the list of available pages
               WikiPageInfo wikiPage = wikiService.getWikiPage(site.getShortName(), StringEscapeUtils.unescapeHtml(link));
               if (wikiPage != null)
               {
                   pageTitles.add(wikiPage.getTitle());
               }
            }
         }
      }
      
      // All done
      model.put("page", page);
      model.put("node", page.getNodeRef());
      model.put("container", page.getContainerNodeRef()); 
      model.put("links", links);
      model.put("pageList", pageTitles);
      model.put("tags", page.getTags());
      model.put("siteId", site.getShortName());
      model.put("site", site);
      model.put("minWikiData", minWikiData);
      
      // Double wrap
      Map<String, Object> result = new HashMap<>();
      result.put("result", model);
      return result;
   }
}
