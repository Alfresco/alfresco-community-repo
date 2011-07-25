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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ChannelPut extends AbstractWebScript
{
    private static final  String CHANNEL_ID = "channel_id";
    
    private final PublishingJsonParser parser = new PublishingJsonParser();
    private ChannelService channelService;
    
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        String channelId = URLDecoder.decode(params.get(CHANNEL_ID));
        Channel channel = channelService.getChannelById(channelId);
        if(channel == null)
        {
            String msg = "No channel found for ID: " + channelId;
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        String content = null;
        try
        {
            content = WebScriptUtil.getContent(req);
            if (content == null || content.isEmpty())
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "No publishing event was posted!");
            }
            parser.updateChannel(channel, content, channelService);
        }
        catch(Exception e)
        {
            String msg = "Failed to Rename Channel: " + channelId + ". POST body: " + content;
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e);
        }
    }

    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}
