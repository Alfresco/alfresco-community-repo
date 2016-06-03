package org.alfresco.repo.web.scripts.links;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the link deleting link.delete webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinkDelete extends AbstractLinksWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String linkName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Try to find the link
      LinkInfo link = linksService.getLink(site.getShortName(), linkName);
      if (link == null)
      {
         String message = "No link found with that name";
         throw new WebScriptException(Status.STATUS_NOT_FOUND, message);
      }
      
      // Delete it
      try
      {
         linksService.deleteLink(link);
      }
      catch (AccessDeniedException e)
      {
         String message = "You don't have permission to delete that link";
         throw new WebScriptException(Status.STATUS_FORBIDDEN, message);
      }
      
      // Mark it as gone
      status.setCode(Status.STATUS_NO_CONTENT);
      return model;
   }
}
