/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.activities.script;

import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.activities.FeedControl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.sync.repo.Client;
import org.alfresco.sync.repo.Client.ClientType;

/**
 * Scripted Activity Service for posting activities.
 */

public final class Activity extends BaseScopableProcessorExtension
{
    private ActivityService activityService;
    private TenantService tenantService;

    /**
     * Set the activity service
     * 
     * @param activityService
     *            the activity service
     */
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }

    /**
     * Set the tenant service
     * 
     * @param tenantService
     *            the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /* Post Activity */

    /**
     * Post a custom activity type
     *
     * @param activityType
     *            - required
     * @param siteId
     *            - optional, if null will be stored as empty string
     * @param appTool
     *            - optional, if null will be stored as empty string
     * @param jsonActivityData
     *            - required
     */
    public void postActivity(String activityType, String siteId, String appTool, String jsonActivityData)
    {
        activityService.postActivity(activityType, siteId, appTool, jsonActivityData, Client.asType(ClientType.webclient));
    }

    /**
     * Post a pre-defined activity type - activity data will be looked-up asynchronously, including:
     * 
     * name displayPath typeQName firstName (of posting user) lastName (of posting user)
     * 
     * @param activityType
     *            - required
     * @param siteId
     *            - optional, if null will be stored as empty string
     * @param appTool
     *            - optional, if null will be stored as empty string
     * @param nodeRef
     *            - required - do not use for deleted (or about to be deleted) nodeRef
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef)
    {
        activityService.postActivity(activityType, siteId, appTool, nodeRef);
    }

    /**
     * Post a pre-defined activity type - eg. for checked-out nodeRef or renamed nodeRef
     * 
     * @param activityType
     *            - required
     * @param siteId
     *            - optional, if null will be stored as empty string
     * @param appTool
     *            - optional, if null will be stored as empty string
     * @param nodeRef
     *            - required - do not use deleted (or about to be deleted) nodeRef
     * @param beforeName
     *            - optional - name of node (eg. prior to name change)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String beforeName)
    {
        activityService.postActivity(activityType, siteId, appTool, nodeRef, beforeName);
    }

    /**
     * Post a pre-defined activity type - eg. for deleted nodeRef
     *
     * @param activityType
     *            - required
     * @param siteId
     *            - optional, if null will be stored as empty string
     * @param appTool
     *            - optional, if null will be stored as empty string
     * @param nodeRef
     *            - required - can be a deleted (or about to be deleted) nodeRef
     * @param name
     *            - optional - name of name
     * @param typeQName
     *            - optional - type of node
     * @param parentNodeRef
     *            - required - used to lookup path/displayPath
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String name, QName typeQName, NodeRef parentNodeRef)
    {
        activityService.postActivity(activityType, siteId, appTool, nodeRef, name, typeQName, parentNodeRef);
    }

    /* Manage User Feed Controls */

    /**
     * For current user, get feed controls
     *
     * @return JavaScript array of user feed controls
     */
    public Scriptable getFeedControls()
    {
        List<FeedControl> feedControls = activityService.getFeedControls();
        Object[] results = new Object[feedControls.size()];

        int i = 0;
        for (FeedControl fc : feedControls)
        {
            results[i] = new FeedControl(this.tenantService.getBaseName(fc.getSiteId()), fc.getAppToolId());
            i++;
        }
        return Context.getCurrentContext().newArray(getScope(), results);
    }

    /**
     * For current user, set feed control (opt-out) for a site or an appTool or a site/appTool combination
     *
     * @param siteId
     *            - required (optional, if appToolId is supplied)
     * @param appToolId
     *            - required (optional, if siteId is supplied)
     */
    public void setFeedControl(String siteId, String appToolId)
    {
        activityService.setFeedControl(new FeedControl(getTenantSpecificSiteId(siteId), appToolId));
    }

    /**
     * For current user, unset feed control
     *
     * @param siteId
     *            - required (optional, if appToolId is supplied)
     * @param appToolId
     *            - required (optional, if siteId is supplied)
     */
    public void unsetFeedControl(String siteId, String appToolId)
    {
        activityService.unsetFeedControl(new FeedControl(getTenantSpecificSiteId(siteId), appToolId));
    }

    // CLOUD-2248
    private String getTenantSpecificSiteId(String siteId)
    {
        return this.tenantService.getName(siteId);
    }
}
