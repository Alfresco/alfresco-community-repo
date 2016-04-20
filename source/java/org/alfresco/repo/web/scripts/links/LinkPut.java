package org.alfresco.repo.web.scripts.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the link fetching links.put webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class LinkPut extends AbstractLinksWebScript
{
   private static final String MSG_ACCESS_DENIED= "links.err.access.denied";
   private static final String MSG_NOT_FOUND= "links.err.not.found";

    @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String linkName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      final ResourceBundle rb = getResources();
      Map<String, Object> model = new HashMap<String, Object>();
      
      // Try to find the link
      LinkInfo link = linksService.getLink(site.getShortName(), linkName);
      if (link == null)
      {
         String message = "No link found with that name";
         
         status.setCode(Status.STATUS_NOT_FOUND);
         status.setMessage(message);
         model.put(PARAM_MESSAGE, rb.getString(MSG_NOT_FOUND));
         return model;
      }

      // Get the new link details from the JSON
      // Update the main properties
      link.setTitle(getOrNull(json, "title"));
      link.setDescription(getOrNull(json, "description"));
      String url = getOrNull(json, "url");

      link.setURL(url);
      
      // Handle internal / not internal
      if (json.containsKey("internal"))
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
      if (tags != null && tags.size() > 0)
      {
         link.getTags().addAll(tags);
      }
      
      
      // Update the link
      try
      {
         link = linksService.updateLink(link);
      }
      catch (AccessDeniedException e)
      {
         String message = "You don't have permission to update that link";
         
         status.setCode(Status.STATUS_FORBIDDEN);
         status.setMessage(message);
         model.put(PARAM_MESSAGE, rb.getString(MSG_ACCESS_DENIED));
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
