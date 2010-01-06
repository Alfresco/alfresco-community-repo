/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.activities.feed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.JSONtoFmModel;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

/**
 * Java-backed WebScript to retrieve Activity User Feed
 */
public class UserFeedRetrieverWebScript extends DeclarativeWebScript
{
    private static final Log logger = LogFactory.getLog(UserFeedRetrieverWebScript.class);
    
    // URL request parameter names
    public static final String PARAM_SITE_ID = "s";
    public static final String PARAM_EXCLUDE_THIS_USER = "exclUser";
    public static final String PARAM_EXCLUDE_OTHER_USERS = "exclOthers";
    
    private ActivityService activityService;
    private AuthorityService authorityService;
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
   
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
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
        
        // process extension 
        String extensionPath = req.getExtensionPath();
        String[] extParts = extensionPath == null ? new String[1] : extensionPath.split("/");
        
        String feedUserId = null;
        if (extParts.length == 1)
        {
            feedUserId = extParts[0];
        }
        else if (extParts.length > 1)
        {
            throw new AlfrescoRuntimeException("Unexpected extension: " + extensionPath);
        }
        
        // process arguments 
        String siteId = req.getParameter(PARAM_SITE_ID); // optional
        String exclThisUserStr = req.getParameter(PARAM_EXCLUDE_THIS_USER); // optional
        String exclOtherUsersStr = req.getParameter(PARAM_EXCLUDE_OTHER_USERS); // optional
        
        boolean exclThisUser = false;
        if ((exclThisUserStr != null) && (exclThisUserStr.equalsIgnoreCase("true") || exclThisUserStr.equalsIgnoreCase("t")))
        {
            exclThisUser = true;
        }
        
        boolean exclOtherUsers = false;
        if ((exclOtherUsersStr != null) && (exclOtherUsersStr.equalsIgnoreCase("true") || exclOtherUsersStr.equalsIgnoreCase("t")))
        {
            exclOtherUsers = true;
        }
        
        if ((feedUserId == null) || (feedUserId.length() == 0))
        {
           feedUserId = AuthenticationUtil.getFullyAuthenticatedUser();
        }
        
        // map feed collection format to feed entry format (if not the same), eg.
        //     atomfeed -> atomentry
        //     atom     -> atomentry
        if (format.equals("atomfeed") || format.equals("atom"))
        {
           format = "atomentry";
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        
        try
        {
            List<String> feedEntries = activityService.getUserFeedEntries(feedUserId, format, siteId, exclThisUser, exclOtherUsers);
            
            if (format.equals("json"))
            { 
                model.put("feedEntries", feedEntries);
                model.put("siteId", siteId);
            }
            else
            {
                List<Map<String, Object>> activityFeedModels = new ArrayList<Map<String, Object>>();
                try
                { 
                    for (String feedEntry : feedEntries)
                    {
                        activityFeedModels.add(JSONtoFmModel.convertJSONObjectToMap(feedEntry));
                    }
                }
                catch (JSONException je)
                {    
                    throw new AlfrescoRuntimeException("Unable to get user feed entries: " + je.getMessage());
                }
                
                model.put("feedEntries", activityFeedModels);
                model.put("feedUserId", feedUserId);
            }
        }
        catch (AccessDeniedException ade)
        {
            status.setCode(Status.STATUS_UNAUTHORIZED);
            logger.warn("Unable to get user feed entries for '" + feedUserId + "' - currently logged in as '" + AuthenticationUtil.getFullyAuthenticatedUser() +"'");
            return null;
        }
        
        return model;
    }
}
