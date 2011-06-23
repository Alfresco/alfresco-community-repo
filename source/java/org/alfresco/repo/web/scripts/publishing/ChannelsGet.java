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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ChannelsGet extends DeclarativeWebScript
{
    public static final String SITE_ID = "site_id";
    public static final String STORE_PROTOCOL = "store_protocol";
    public static final String STORE_ID = "store_id";
    public static final String NODE_ID = "node_id";

    // Model Keys
    public static final String DATA_KEY = "data";
    public static final String URL_LENGTH = "urlLength";
    public static final String PUBLISHING_CHANNELS = "publishChannels";
    public static final String STATUS_UPDATE_CHANNELS = "statusUpdateChannels";
    
    private ChannelService channelService;

    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        PublishingModelBuilder builder = new PublishingModelBuilder();
        return buildModel(builder, req, status, cache);
    }

    private Map<String, Object> buildModel(PublishingModelBuilder builder,
            WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        String siteId = params.get(SITE_ID);
        
        List<Channel> publishingChannels;
        List<Channel> statusUpdateChannels;
        if (siteId!= null)
        {
            publishingChannels = channelService.getPublishingChannels(siteId);
            statusUpdateChannels = channelService.getStatusUpdateChannels(siteId);
        }
        else
        {
            NodeRef node = getNodeRef(params);
            if(node == null)
            {
                String msg = "Either a Site ID or valid NodeRef must be specified!";
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
            publishingChannels = channelService.getRelevantPublishingChannels(node);
            statusUpdateChannels = channelService.getStatusUpdateChannels(node);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        
        //TODO Implement URL shortening.
        model.put(URL_LENGTH, 20);
        
        model.put(PUBLISHING_CHANNELS, builder.buildChannels(publishingChannels));
        model.put(STATUS_UPDATE_CHANNELS, builder.buildChannels(statusUpdateChannels));
        return createBaseModel(model);
    }

    private NodeRef getNodeRef(Map<String, String> params)
    {
        String protocol = params.get(STORE_PROTOCOL);
        String storeId= params.get(STORE_ID);
        String nodeId= params.get(NODE_ID);
        if(protocol == null || storeId == null || nodeId==null )
        {
            return null;
        }
        return new NodeRef(protocol, storeId, nodeId);
    }

    protected Map<String, Object> createBaseModel(Map<String, Object> result)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(DATA_KEY, result);
        return model;
    }
    
    protected Map<String, Object> createBaseModel(List<Map<String, Object>> results)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(DATA_KEY, results);
        return model;
    }

    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}
