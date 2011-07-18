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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;

public class YouTubeUnpublishAction extends ActionExecuterAbstractBase
{
    private final static Log log = LogFactory.getLog(YouTubeUnpublishAction.class);
    public static final String NAME = "unpublish_youtube";

    private NodeService nodeService;
    private YouTubePublishingHelper youTubeHelper;

    public void setYouTubeHelper(YouTubePublishingHelper youTubeHelper)
    {
        this.youTubeHelper = youTubeHelper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        YouTubeService service = youTubeHelper.getYouTubeServiceForNode(actionedUponNodeRef);
        if (service != null)
        {
            try
            {
                removeVideo(service, actionedUponNodeRef);
            }
            catch (Exception ex)
            {
                log.error("Failed to remove asset from YouTube", ex);
                throw new AlfrescoRuntimeException("exception.publishing.youtube.unpublishFailed", ex);
            }
        }
    }

    private void removeVideo(YouTubeService service, NodeRef nodeRef) throws MalformedURLException, IOException,
            ServiceException
    {
        if (nodeService.hasAspect(nodeRef, YouTubePublishingModel.ASPECT_ASSET))
        {
            String youtubeId = (String) nodeService.getProperty(nodeRef, PublishingModel.PROP_ASSET_ID);
            if (youtubeId != null)
            {
                String videoEntryUrl = "https://gdata.youtube.com/feeds/api/users/default/uploads/" + youtubeId;
                VideoEntry videoEntry = service.getEntry(new URL(videoEntryUrl), VideoEntry.class);
                videoEntry.delete();
                nodeService.removeAspect(nodeRef, YouTubePublishingModel.ASPECT_ASSET);
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
