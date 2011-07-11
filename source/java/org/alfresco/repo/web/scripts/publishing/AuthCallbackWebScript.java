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

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class AuthCallbackWebScript extends AbstractWebScript
{
    private final static Log log = LogFactory.getLog(AuthCallbackWebScript.class);
    private NodeService nodeService;
    private ChannelService channelService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        res.setContentType("text/html");
        res.setContentEncoding("UTF-8");
        
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
        Channel channel = channelService.getChannel(channelNodeRef.toString());
        
        if (channel.getChannelType().acceptAuthorisationCallback(channel, headers, params))
        {
            nodeService.setProperty(channelNodeRef, PublishingModel.PROP_AUTHORISATION_COMPLETE, Boolean.TRUE);
            res.getWriter().write("Authorisation granted!");
        }
        else
        {
            Boolean authorised = (Boolean)nodeService.getProperty(channelNodeRef, PublishingModel.PROP_AUTHORISATION_COMPLETE);
            if (authorised != null && !authorised)
            {
                //If we have not been granted access by the service provider then we 
                //simply delete this publishing channel
                nodeService.deleteNode(channelNodeRef);
            }
            res.getWriter().write("Authorisation denied!");
        }
    }

}
