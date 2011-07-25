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

import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public abstract class PublishingWebScript extends DeclarativeWebScript
{
    protected final PublishingJsonParser jsonParser = new PublishingJsonParser();
    protected final PublishingModelBuilder builder= new PublishingModelBuilder();

    protected PublishingService publishingService;
    protected ChannelService channelService;

    protected PublishingQueue getQueue()
    {
        return publishingService.getPublishingQueue();
    }
    
    /**
     * @param publishingService the publishingService to set
     */
    public void setPublishingService(PublishingService publishingService)
    {
        this.publishingService = publishingService;
    }
    
    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}