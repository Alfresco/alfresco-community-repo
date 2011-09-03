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

package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Brian
 * @since 4.0
 */
public class ChannelsPostWebScript extends DeclarativeWebScript
{
    private ChannelService channelService;
    private ChannelAuthHelper channelAuthHelper;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    public void setChannelAuthHelper(ChannelAuthHelper channelAuthHelper)
    {
        this.channelAuthHelper = channelAuthHelper;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String channelType = req.getParameter("channelType");
        String channelName = req.getParameter("channelName");

        Channel newChannel = channelService.createChannel(channelType, channelName, null);

        return channelAuthHelper.buildAuthorisationModel(newChannel);
    }
}
