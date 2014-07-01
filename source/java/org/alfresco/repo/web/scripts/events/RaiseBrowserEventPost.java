/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.events;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.events.EventPublisher;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StringUtils;

/**
* Raises an alfresco browser event, e.g /api/events/mysite/documentdetails/view
* 
* @author Gethin James
* @since 5.0
 */
public class RaiseBrowserEventPost extends AbstractWebScript
{
    private EventPublisher eventPublisher;
    private SiteService siteService;
    
    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
       Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
       SiteInfo siteInfo = null;
       String component = null;
       String action = null;

       if (templateVars != null)
       {
         if (templateVars.containsKey("siteId"))
         {
             //Validates the site id
             siteInfo = siteService.getSite(templateVars.get("siteId"));
             if (siteInfo == null)
             {
                 throw new AccessDeniedException("No such site: " + templateVars.get("siteId"));
             }
             
         }
         component = templateVars.get("component");
         action = templateVars.get("action");
       }
       
       String attributes = req.getContent().getContent();
       if (attributes != null)
       {
           if (StringUtils.hasText(attributes))
           {
               if (!validJsonMap(attributes))
               {
                   throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON Object: " + attributes);
               }            
           }
           else
           {
               //No valid attributes passed in so reset it
               attributes = null;
           }

       }
       
       if (StringUtils.hasText(component) && StringUtils.hasText(action))
       {
           eventPublisher.publishBrowserEvent(req, siteInfo==null?null:siteInfo.getShortName(), component, action, attributes);           
       }
       
       res.setStatus(Status.STATUS_OK);
    }
    
    /**
     * Validate Json to ensure its a Map.
     * @param attributes jsonmap
     * @return boolean true if valid
     */
    private boolean validJsonMap(String attributes)
    {
        try
        {
            @SuppressWarnings("unused")
            JSONObject json = new JSONObject(new JSONTokener(attributes));
            return true;
        }
        catch (JSONException error)
        {
            return false;
        }

    }

}
