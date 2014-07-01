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
package org.alfresco.service.cmr.activities;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode.Client;

public interface ActivityPostService
{
    
    /*
     * Post Activity
     */
    
    /**
     * Post a custom activity type
     *
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param jsonActivityData - required
     */
    public void postActivity(String activityType, String siteId, String appTool, String jsonActivityData);

    /**
     * Post a custom activity type
     *
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param jsonActivityData - required
     */
    public void postActivity(String activityType, String siteId, String appTool, String jsonActivityData,  Client client);
    
    /**
     * Post a custom activity type
     *
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param jsonActivityData - required
     * @param contentNodeInfo FileInfo
     */
    public void postActivity(String activityType, String siteId, String appTool, String jsonActivityData,  Client client, FileInfo contentNodeInfo);
    
    /**
     * Post a custom activity type
     *
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param jsonActivityData - required
     * @param userId - required
     */
    public void postActivity(String activityType, String siteId, String appTool, String jsonActivityData, String userId);    
    
    /**
     * Post a pre-defined activity type - certain activity data will be looked-up asynchronously, including:
     *
     *   name (of nodeRef)
     *   displayPath
     *   typeQName
     *   firstName (of posting user)
     *   lastName  (of posting user)
     * 
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param nodeRef - required - do not use for deleted (or about to be deleted) nodeRef
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef);
    
    /**
     * Post a pre-defined activity type - eg. for checked-out nodeRef or renamed nodeRef
     * 
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param nodeRef - required - do not use deleted (or about to be deleted) nodeRef
     * @param beforeName - optional - name of node (eg. prior to name change)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String beforeName);
    
    /**
     * Post a pre-defined activity type - eg. for deleted nodeRef
     *
     * @param activityType - required
     * @param siteId - optional, if null will be stored as empty string
     * @param appTool - optional, if null will be stored as empty string
     * @param nodeRef - required - can be a deleted (or about to be deleted) nodeRef
     * @param name - optional - name of name
     * @param typeQName - optional - type of node
     * @param parentNodeRef - required - used to lookup path/displayPath
     */
    public void postActivity(String activityType, String siteId, String appTool,  NodeRef nodeRef, String name, QName typeQName, NodeRef parentNodeRef);
}
