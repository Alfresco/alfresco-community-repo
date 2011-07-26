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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.UrlUtil;

public class ChannelAuthHelper
{
    private String basePath = "/proxy/alfresco/api/publishing/channels/";
    private SysAdminParams sysAdminParams;

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getBaseChannelApiUrl(NodeRef channelId)
    {
        StringBuilder urlBuilder = new StringBuilder(UrlUtil.getShareUrl(sysAdminParams));
        urlBuilder.append(basePath);
        urlBuilder.append(channelId.getStoreRef().getProtocol());
        urlBuilder.append('/');
        urlBuilder.append(channelId.getStoreRef().getIdentifier());
        urlBuilder.append('/');
        urlBuilder.append(channelId.getId());
        urlBuilder.append('/');

        return urlBuilder.toString();
    }

    public String getDefaultAuthoriseUrl(NodeRef channelId)
    {
        return getBaseChannelApiUrl(channelId) + "authform";
    }

    public String getAuthoriseCallbackUrl(NodeRef channelId)
    {
        return getBaseChannelApiUrl(channelId) + "authcallback";
    }
    
    public Map<String, Object> buildAuthorisationModel(Channel channel)
    {
        String callbackUrl = getAuthoriseCallbackUrl(channel.getNodeRef());
        String authoriseUrl = channel.getChannelType().getAuthorisationUrl(channel, callbackUrl);
        if (authoriseUrl == null)
        {
            // If a channel type returns null as the authorise URL then we
            // assume credentials are to be supplied to us directly. We'll point the 
            // user at our own credential-gathering form.
            authoriseUrl = getDefaultAuthoriseUrl(channel.getNodeRef());
        }

        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("authoriseUrl", authoriseUrl);
        model.put("channelId", channel.getId());
        model.put("authCallbackUrl", callbackUrl);
        return model;
    }
}
