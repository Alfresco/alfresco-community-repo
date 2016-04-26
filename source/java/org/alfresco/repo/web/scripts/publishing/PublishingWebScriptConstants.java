
package org.alfresco.repo.web.scripts.publishing;

/**
 * @author Nick Smith
 * @since 4.0
 */
public interface PublishingWebScriptConstants
{
    // General Model Keys
    public static final String ID = "id";
    public static final String URL = "url";
    public static final String ICON = "icon";
    public static final String TITLE = "title";

    // Channel Type Model Keys
    public static final String CHANNEL_NODE_TYPE = "channelNodeType";
    public static final String CONTENT_ROOT_NODE_TYPE = "contentRootNodeType";
    public static final String SUPPORTED_CONTENT_TYPES = "supportedContentTypes";
    public static final String SUPPORTED_MIME_TYPES = "supportedMimeTypes";
    public static final String CAN_PUBLISH = "canPublish";
    public static final String CAN_PUBLISH_STATUS_UPDATES = "canPublishStatusUpdates";
    public static final String CAN_UNPUBLISH = "canUnpublish";
    public static final String MAX_STATUS_LENGTH = "maxStatusLength";

    // Channel Keys
    public static final String NAME = "name";
    public static final String CHANNEL_TYPE = "channelType";
    public static final String CHANNEL_AUTH_STATUS = "authorised";
    
    // Publishing Event Model Keys
    public static final String CHANNEL = "channel";
    public static final String STATUS = "status";
    public static final String COMMENT = "comment";
    public static final String SCHEDULED_TIME = "scheduledTime";
    public static final String CREATOR = "creator";
    public static final String CREATED_TIME = "createdTime";
    public static final String PUBLISH_NODES = "publishNodes";
    public static final String UNPUBLISH_NODES = "unpublishNodes";
    public static final String NODEREF = "nodeRef";
    public static final String VERSION = "version";
    public static final String STATUS_UPDATE = "statusUpdate";
    public static final String CHANNEL_NAME = "channelName";
    public static final String CHANNEL_ID = "channelId";

    // Status Update Model Keys
    public static final String CHANNEL_IDS = "channelIds";
    public static final String NODE_REF = "nodeRef";
    public static final String MESSAGE = "message";

    // Publishing Events For Node Modek Keys
    public static final String EVENT_TYPE = "eventType";
    
    // channels.get Model Keys
    public static final String URL_LENGTH = "urlLength";
    public static final String PUBLISHING_CHANNELS = "publishChannels";
    public static final String STATUS_UPDATE_CHANNELS = "statusUpdateChannels";
}
