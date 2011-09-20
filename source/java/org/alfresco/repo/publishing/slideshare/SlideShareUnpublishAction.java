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

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;

public class SlideShareUnpublishAction extends ActionExecuterAbstractBase
{
    public static final String NAME = "unpublish_slideshare";

    private NodeService nodeService;
    private SlideSharePublishingHelper slideShareHelper;

    public void setSlideShareHelper(SlideSharePublishingHelper slideShareHelper)
    {
        this.slideShareHelper = slideShareHelper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef nodeRef)
    {
        if (nodeService.hasAspect(nodeRef, SlideSharePublishingModel.ASPECT_ASSET))
        {
            String assetId = (String) nodeService.getProperty(nodeRef, PublishingModel.PROP_ASSET_ID);
            if (assetId != null)
            {
                Pair<String, String> usernamePassword = slideShareHelper.getSlideShareCredentialsForNode(nodeRef);
                if (usernamePassword == null)
                {
                    throw new AlfrescoRuntimeException("publish.failed.no_credentials_found");
                }
                SlideShareApi api = slideShareHelper.getSlideShareApi(
                        usernamePassword.getFirst(), usernamePassword.getSecond());
        
                api.deleteSlideshow(usernamePassword.getFirst(), usernamePassword.getSecond(), assetId);
                nodeService.removeAspect(nodeRef, SlideSharePublishingModel.ASPECT_ASSET);
                nodeService.removeAspect(nodeRef, PublishingModel.ASPECT_ASSET);
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
