/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.publishing.youtube;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.CollectionUtils;

public class YouTubeChannelType extends AbstractChannelType
{
    private final static Set<String> DEFAULT_SUPPORTED_MIME_TYPES = CollectionUtils.unmodifiableSet(
            MimetypeMap.MIMETYPE_VIDEO_MPG,
            MimetypeMap.MIMETYPE_VIDEO_MP4,
            MimetypeMap.MIMETYPE_VIDEO_FLV,
            MimetypeMap.MIMETYPE_VIDEO_3GP,
            MimetypeMap.MIMETYPE_VIDEO_AVI,
            MimetypeMap.MIMETYPE_VIDEO_QUICKTIME,
            MimetypeMap.MIMETYPE_VIDEO_WMV
            );
    
    private Set<String> supportedMimeTypes = DEFAULT_SUPPORTED_MIME_TYPES;

    public final static String ID = "youtube";
    private NodeService nodeService;
    private ActionService actionService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setSupportedMimeTypes(Set<String> supportedMimeTypes)
    {
        this.supportedMimeTypes = Collections.unmodifiableSet(new TreeSet<String>(supportedMimeTypes));
    }

    @Override
    public boolean canPublish()
    {
        return true;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    @Override
    public boolean canUnpublish()
    {
        return true;
    }

    @Override
    public QName getChannelNodeType()
    {
        return YouTubePublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedMimeTypes()
    {
        return supportedMimeTypes;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        Action youtubePublishAction = actionService.createAction(YouTubePublishAction.NAME);
        actionService.executeAction(youtubePublishAction, nodeToPublish);
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        Action youtubeUnpublishAction = actionService.createAction(YouTubeUnpublishAction.NAME);
        actionService.executeAction(youtubeUnpublishAction, nodeToUnpublish);
    }

    @Override
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        String url = null;
        if (node != null && nodeService.exists(node) && nodeService.hasAspect(node, PublishingModel.ASPECT_ASSET))
        {
            url = (String)nodeService.getProperty(node, PublishingModel.PROP_ASSET_URL);
        }
        return url;
    }
}
