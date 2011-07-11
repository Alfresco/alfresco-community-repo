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
package org.alfresco.repo.publishing.slideshare;

import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareAPIFactory;

public class SlideSharePublishingHelper
{
    private NodeService nodeService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    public SlideShareAPI getSlideShareApi()
    {
        return SlideShareAPIFactory.getSlideShareAPI("hhjh", "oijkl");
    }
    
    
    public Pair<String, String> getSlideShareCredentialsForNode(NodeRef publishNode)
    {
        Pair<String, String> result = null;
        if (nodeService.exists(publishNode))
        {
            NodeRef parent = nodeService.getPrimaryParent(publishNode).getParentRef();
            if (nodeService.hasAspect(parent, SlideSharePublishingModel.ASPECT_DELIVERY_CHANNEL))
            {
                String username = (String) nodeService.getProperty(parent, PublishingModel.PROP_CHANNEL_USERNAME);
                String password = (String) nodeService.getProperty(parent, PublishingModel.PROP_CHANNEL_PASSWORD);
                if (username != null && password != null)
                {
                    result = new Pair<String, String>(username, password);
                }
            }
        }
        return result;
    }

}
