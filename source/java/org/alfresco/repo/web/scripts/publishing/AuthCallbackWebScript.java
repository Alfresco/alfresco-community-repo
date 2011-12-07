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
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.publishing.channels.ChannelType.AuthUrlPair;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Brian
 * @since 4.0
 */
public class AuthCallbackWebScript extends DeclarativeWebScript
{
    private final static Log log = LogFactory.getLog(AuthCallbackWebScript.class);
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
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        Map<String,String[]> params = new TreeMap<String, String[]>();
        Map<String,String[]> headers = new TreeMap<String, String[]>();
        
        for (String paramName : req.getParameterNames())
        {
            params.put(paramName, req.getParameterValues(paramName));
        }

        for (String header : req.getHeaderNames())
        {
            headers.put(header, req.getHeaderValues(header));
        }

        if (log.isDebugEnabled())
        {
            log.debug("templateVars = " + templateVars);
            log.debug("params = " + params);
            log.debug("headers = " + headers);
        }

        String channelNodeUuid = templateVars.get("node_id");
        String channelNodeStoreProtocol = templateVars.get("store_protocol");
        String channelNodeStoreId = templateVars.get("store_id");

        NodeRef channelNodeRef = new NodeRef(channelNodeStoreProtocol, channelNodeStoreId, channelNodeUuid);
        Channel channel = channelService.getChannelById(channelNodeRef.toString());
        
        ChannelType.AuthStatus authStatus = channel.getChannelType().acceptAuthorisationCallback(channel, headers, params);
        
        if (ChannelType.AuthStatus.RETRY.equals(authStatus))
        {
            AuthUrlPair authoriseUrls = channel.getChannelType().getAuthorisationUrls(channel, channelAuthHelper.getAuthoriseCallbackUrl(channelNodeRef));
            String authRequestUrl = authoriseUrls.authorisationRequestUrl;
            if (authRequestUrl == null)
            {
               authRequestUrl = channelAuthHelper.getDefaultAuthoriseUrl(channelNodeRef);
            }
            status.setCode(HttpServletResponse.SC_MOVED_TEMPORARILY);
            status.setLocation(authRequestUrl);
        }
        Map<String,Object> model = new TreeMap<String, Object>();
        model.put("authStatus", authStatus.name());
        return model;
    }
}
