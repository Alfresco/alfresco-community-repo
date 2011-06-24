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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.youtube.YouTubeService;

public class YouTubePublishingHelper
{
    private static final Log log = LogFactory.getLog(YouTubePublishingHelper.class);
    private NodeService nodeService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    public YouTubeService getYouTubeServiceForNode(NodeRef publishNode)
    {
        YouTubeService service = null;
        if (nodeService.exists(publishNode))
        {
            NodeRef parent = nodeService.getPrimaryParent(publishNode).getParentRef();
            if (nodeService.hasAspect(parent, YouTubePublishingModel.ASPECT_DELIVERY_CHANNEL))
            {
                String youtubeUsername = (String) nodeService.getProperty(parent, YouTubePublishingModel.PROP_USERNAME);
                String youtubePassword = (String) nodeService.getProperty(parent, YouTubePublishingModel.PROP_PASSWORD);
                service = new YouTubeService("Alfresco Kickoff Demo",
                        "AI39si71pRNHkfExcTpqcZewDtI4GHWuPAXyRPL2Xq-RQUBWlE1bqn77ANXEL5lZUWFDz6ZlS_XWCw8hlr2BJY1TnC-EMs4e4g");
                try
                {
                    service.setUserCredentials(youtubeUsername, youtubePassword);
                }
                catch (Exception e)
                {
                    service = null;
                    log.error("Failed to connect to YouTube", e);
                }
            }
        }
        return service;
    }

}
