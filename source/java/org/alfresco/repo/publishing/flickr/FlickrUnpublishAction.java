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
package org.alfresco.repo.publishing.flickr;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.social.connect.Connection;

public class FlickrUnpublishAction extends ActionExecuterAbstractBase
{
    public static final String NAME = "unpublish_flickr";

    private NodeService nodeService;
    private FlickrPublishingHelper flickrHelper;

    public void setFlickrHelper(FlickrPublishingHelper helper)
    {
        this.flickrHelper = helper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef nodeRef)
    {
        if (nodeService.hasAspect(nodeRef, FlickrPublishingModel.ASPECT_ASSET))
        {
            String assetId = (String) nodeService.getProperty(nodeRef, PublishingModel.PROP_ASSET_ID);
            if (assetId != null)
            {
                Connection<Flickr> connection = flickrHelper.getConnectionForPublishNode(nodeRef);
                MediaOperations mediaOps = connection.getApi().mediaOperations();
                mediaOps.deletePhoto(assetId);
                nodeService.removeAspect(nodeRef, FlickrPublishingModel.ASPECT_ASSET);
                nodeService.removeAspect(nodeRef, PublishingModel.ASPECT_ASSET);
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        //Deliberately empty
    }
}
