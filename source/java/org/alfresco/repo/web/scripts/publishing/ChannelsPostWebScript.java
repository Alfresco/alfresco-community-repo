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
import java.util.TreeMap;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.UrlUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ChannelsPostWebScript extends DeclarativeWebScript
{
    private ChannelService channelService;
    private SysAdminParams sysAdminParams;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String channelType = req.getParameter("channelType");
        String channelName = req.getParameter("channelName");

        Channel newChannel = channelService.createChannel(channelType, channelName, null);

        NodeRef channelNodeRef = newChannel.getNodeRef();
        StringBuilder urlBuilder = new StringBuilder(UrlUtil.getShareUrl(sysAdminParams));
        urlBuilder.append("/proxy/alfresco/api/publishing/channels/");
        urlBuilder.append(channelNodeRef.getStoreRef().getProtocol());
        urlBuilder.append('/');
        urlBuilder.append(channelNodeRef.getStoreRef().getIdentifier());
        urlBuilder.append('/');
        urlBuilder.append(channelNodeRef.getId());
        urlBuilder.append('/');

        String baseUrl = urlBuilder.toString();
        String callbackUrl = baseUrl + "authcallback";

        String authoriseUrl = channelService.getChannelType(channelType).getAuthorisationUrl(newChannel, callbackUrl);
        if (authoriseUrl == null)
        {
            // If a channel type returns null as the authorise URL then we
            // assume credentials are to be supplied to us directly. We'll point the 
            // user at our own credential-gathering form.
            authoriseUrl = baseUrl + "authform";
        }

        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("authoriseUrl", authoriseUrl);
        model.put("channelId", newChannel.getId());
        model.put("authCallbackUrl", callbackUrl);

        return model;
    }
}
