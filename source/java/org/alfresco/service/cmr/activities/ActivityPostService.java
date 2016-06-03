package org.alfresco.service.cmr.activities;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.Client;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

@AlfrescoPublicApi
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
