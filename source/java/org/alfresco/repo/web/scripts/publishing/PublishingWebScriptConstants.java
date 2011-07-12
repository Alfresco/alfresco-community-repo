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

package org.alfresco.repo.web.scripts.publishing;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public interface PublishingWebScriptConstants
{
    // URL Template Keys.
    public static final String SITE_ID = "site_id";

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

    // Status Update Model Keys
    public static final String CHANNEL_NAMES = "channelNames";
    public static final String NODE_REF = "nodeRef";
    public static final String MESSAGE = "message";

    // Publishing Event Filter Modek Keys
    public static final String IDS = "ids";
    
    // channels.get Model Keys
    public static final String URL_LENGTH = "urlLength";
    public static final String PUBLISHING_CHANNELS = "publishChannels";
    public static final String STATUS_UPDATE_CHANNELS = "statusUpdateChannels";

}
