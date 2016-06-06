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
   protected Map<String, Object> executeImpl(SiteInfo site, String pageTitle,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Try to find the page
      WikiPageInfo page = wikiService.getWikiPage(site.getShortName(), pageTitle);
      if (page == null)
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
