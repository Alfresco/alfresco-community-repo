/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.activities.feed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.util.JSONtoFmModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

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
    public static final String PARAM_ONLY_FOLLOWING = "following";
    public static final String PARAM_ACTIVITY_FILTER = "activityFilter";
    
    private ActivityService activityService;
    private SubscriptionService subscriptionService;
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    public void setSubscriptionService(SubscriptionService subscriptionService)
    {
        this.subscriptionService = subscriptionService;
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
        String onlyFollowingStr = req.getParameter(PARAM_ONLY_FOLLOWING); // optional
        String activityFilterStr = req.getParameter(PARAM_ACTIVITY_FILTER); // optional
        
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
        
        Set<String> userFilter = null;
        if ((onlyFollowingStr != null) && (onlyFollowingStr.equalsIgnoreCase("true") || onlyFollowingStr.equalsIgnoreCase("t")))
        {
            PagingFollowingResults following = subscriptionService.getFollowing(AuthenticationUtil.getRunAsUser(), new PagingRequest(-1, null));
            if (following.getPage() != null)
            {
                userFilter = new HashSet<String>();
                userFilter.addAll(following.getPage());
            }
        }
        
        Set<String> activityFilter = null;
        if (activityFilterStr != null)
        {
            activityFilter = new HashSet<String>();
            String[] activities = activityFilterStr.split(",");
            for (String s : activities)
            {
                if (s.trim().length() > 0)
                {
                    activityFilter.add(s.trim());
                }
            }
            if (activityFilter.size() == 0)
            {
                activityFilter = null;
            }
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
            List<String> feedEntries = activityService.getUserFeedEntries(feedUserId, format, siteId, exclThisUser, exclOtherUsers, userFilter, activityFilter);
            
            if (format.equals(FeedTaskProcessor.FEED_FORMAT_JSON))
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
