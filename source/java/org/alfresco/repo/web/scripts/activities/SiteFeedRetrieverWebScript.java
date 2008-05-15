/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.web.scripts.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptRequest;

/**
 * Java-backed WebScript to retrieve Activity Site Feed
 */
public class SiteFeedRetrieverWebScript extends DeclarativeWebScript
{
   private ActivityService activityService;
   
   public void setActivityService(ActivityService activityService)
   {
       this.activityService = activityService;
   }
   
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // retrieve requested format
        String format = req.getFormat();
        if (format == null || format.length() == 0)
        {
            format = getDescription().getDefaultFormat();
        }
        
        String extensionPath = req.getExtensionPath();
        String[] extParts = extensionPath == null ? new String[1] : extensionPath.split("/");
        
        String siteId = null;
        if (extParts.length == 1)
        {
           siteId = extParts[0];
        }
        else
        {
            throw new AlfrescoRuntimeException("Unexpected extension: " + extensionPath);
        }
        
        // map feed collection format to feed entry format (if not the same), eg.
        //     atomfeed -> atomentry
        //     atom     -> atomentry
        if (format.equals("atomfeed") || format.equals("atom"))
        {
           format = "atomentry";
        }
        
        // TODO - check if site is public or private
        // if private and user is not a member or not an admin then throw 401 (unauthorised)

        List<Map<String, Object>> feedEntries = activityService.getSiteFeedEntries(siteId, format);
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("feedEntries", feedEntries);
        model.put("siteId", siteId);
        return model;
    }
}
